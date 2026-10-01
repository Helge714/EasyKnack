package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.audio.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

class FeedbackWarningAudioTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void fallbackValuesAndHealthyBranchesContinueRendering()throws Exception{
        var graph=new GLNodeGraph();var a=add(graph,new MathNode());var b=add(graph,new MathNode(),Map.of("a",GLNodeValue.of(.4),"b",GLNodeValue.of(.5)));
        var healthy=add(graph,new ValueNode(),Map.of("value",GLNodeValue.of(.6)));var mix=add(graph,new MathNode(),Map.of("operation",GLNodeValue.of("add")));var out=add(graph,new OutputNode());
        connect(graph,a,"audio-out",b,"a");connect(graph,b,"audio-out",a,"a");connect(graph,b,"audio-out",mix,"a");connect(graph,healthy,"value-out",mix,"b");connect(graph,mix,"audio-out",out,"audio-in");
        var types=new GLNodeTypeRegistry();new NodeRegistry(types,new GLNodeCatalog());var resolved=FeedbackConnections.resolve(graph.snapshot(),types);
        assertEquals(1,resolved.ignored().size());assertEquals(4,resolved.graph().connections().size());assertEquals(5,graph.snapshot().connections().size());
        var patch=AudioGraphEncoder.encode(resolved.graph());var dir=Files.createTempDirectory(Path.of("target"),"feedback-warning-").toAbsolutePath();
        var cpp="#include \"AudioGraph.hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){auto graph=AudioGraph::parse("+quote(patch)+");std::array<float,256> out{};for(int block=0;block<8;block++)for(int channel=0;channel<2;channel++){graph.process(out.data(),256,channel,nullptr);for(float value:out)assert(std::abs(value-.8f)<1e-5f);}}";
        var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void groupingMutedDelayPreservesAudio()throws Exception{
        var patches=new ArrayList<String>();
        javax.swing.SwingUtilities.invokeAndWait(()->{
            for(int selection=0;selection<3;selection++)try(var c=new WorkspaceController()){
                var g=new GLNodeGraph();var a=add(g,new MathNode(),Map.of("a",GLNodeValue.of(.4),"b",GLNodeValue.of(.5)));
                var b=add(g,new MathNode(),Map.of("b",GLNodeValue.of(.5)));var delay=add(g,new DelayNode());var out=add(g,new OutputNode());
                connect(g,a,"audio-out",b,"a");connect(g,b,"audio-out",delay,"audio-in");connect(g,delay,"audio-out",a,"a");connect(g,delay,"audio-out",out,"audio-in");
                c.openDocument("Audio",new com.vingame.glswing.node.document.GLNodeGraphDocument(new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,g.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty())));
                c.workspace().currentGraph().edit("Mute",e->e.setProperty(delay,GLNodeState.MUTED,GLNodeValue.of(true)));patches.add(c.audioGraph());
                var selected=selection==0?Set.of(a,b,delay):selection==1?Set.of(a,b):Set.of(delay);
                c.workspace().editor().setSelectedNodes(c.workspace().editor().getNodes().stream().filter(v->v instanceof BasicNodeView n&&selected.contains(n.nodeId())).toList());
                c.actions().extractComposite();patches.add(c.audioGraph());
            }
        });
        var dir=Files.createTempDirectory(Path.of("target"),"feedback-group-audio-").toAbsolutePath();
        var cpp=new StringBuilder("#include \"AudioGraph.hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){");
        for(var patch:patches)cpp.append("{auto graph=AudioGraph::parse(").append(quote(patch)).append(");std::array<float,256> out{};for(int block=0;block<8;block++)for(int channel=0;channel<2;channel++){graph.process(out.data(),256,channel,nullptr);for(float value:out)assert(std::abs(value-.1f)<1e-5f);}}");
        cpp.append("}");var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
