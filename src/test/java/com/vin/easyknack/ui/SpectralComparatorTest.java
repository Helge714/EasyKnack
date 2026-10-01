package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class SpectralComparatorTest {
    @Test void sampleSupportDoesNotImplyIndependentBinSupport(){
        assertTrue(SpectralGroups.supports(new ComparatorNode()));
        assertTrue(new OscillatorNode().supportsSampleFeedback());
        assertFalse(new OscillatorNode().supportsSpectralBins());
        assertTrue(SpectralGroups.supports(new OscillatorNode())); // Separate persistent audio state per bin.
        assertFalse(SpectralGroups.supports(new DelayNode()));
        assertFalse(SpectralGroups.supports(new FilterNode()));
    }

    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void comparatorEvaluatesEachBinIndependentlyForEveryOperation()throws Exception{
        var code=new StringBuilder("#include \"AudioGraph.hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){\n");
        String[] operations={"less","less_equal","greater","greater_equal","equal","not_equal"};
        int[][] expected={{1,0,0,1,0},{1,1,0,1,0},{0,0,1,0,1},{0,1,1,0,1},{0,1,0,0,0},{1,0,1,1,1}};
        for(int op=0;op<operations.length;op++){
            var asset=SpectralGroups.create();var graph=new GLNodeGraph(asset.graph());
            var context=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinContextNode).findFirst().orElseThrow();
            var output=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinOutputNode).findFirst().orElseThrow();
            var compare=add(graph,new ComparatorNode());String operation=operations[op];
            graph.edit("Compare bins",e->{
                for(var w:graph.snapshot().connections())e.removeConnection(w.id());
                e.setProperty(compare,"operation",GLNodeValue.of(operation));e.setProperty(compare,"b",GLNodeValue.of(.5));
                e.addConnection(SpectralGroupsTest.wire(context.id(),"real",compare,"a"));
                e.addConnection(SpectralGroupsTest.wire(compare,"value-out",output.id(),"a"));
            });
            asset=new GLNodeGraphAsset(asset.id(),1,asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),asset.compositeDescriptor());
            var rootGraph=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of());rootGraph.edit("Group",e->e.addNode(instance));
            var root=new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,rootGraph.snapshot(),GLNodeEditorViewState.empty());
            var registry=new GLCompositeGraphRegistry();registry.registerAsset(asset);
            var flat=SpectralGroups.flatten(GroupCompiler.prepare(root,registry),new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(flat.isValid());
            var compiled=flat.graph().orElseThrow().nodes().get(0);var def=NodeRegistry.require(compiled.typeId());
            String payload="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(compiled.properties()),p->0);
            code.append("{SpectralGraphNode node;std::istringstream params(").append(quote(payload)).append(");node.configure(params);node.prepare(48000,256);")
                .append("SpectrumStream input;input.prepare(256);input.size=input.windowSize=8;input.sampleRate=48000;input.count=1;input.hop=4;float values[]{.25f,.5f,.75f,-1.f,2.f};float expected[]{")
                .append(Arrays.stream(expected[op]).mapToObj(Integer::toString).collect(java.util.stream.Collectors.joining(","))).append("};")
                .append("float silence=0,out=0;const DspResource* resources[]{&input};ProcessContext c{nullptr,1,&out,1,48000,&silence,1};c.resourceInputs=resources;")
                .append("for(int frame=0;frame<3;frame++){for(int k=0;k<5;k++)input.frames[0].bins[k]={values[k],0};node.process(c);for(int k=0;k<5;k++){assert(node.stream.frames[0].bins[k].real()==expected[k]);assert(node.stream.frames[0].bins[k].imag()==0);}}}\n");
        }
        code.append("}");var dir=Files.createTempDirectory(Path.of("target"),"spectral-comparator-").toAbsolutePath();var source=dir.resolve("test.cpp");Files.writeString(source,code);
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
