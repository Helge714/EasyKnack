package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;

class EqualPowerMixTest {
    @Test void oldDocumentsUseLinearAndModeRoundTrips() throws Exception {
        var mix=new MixNode();assertEquals("linear",new NodeState(mix.defaults()).text(MixNode.MODE,""));
        assertTrue(mix.encode(new NodeState(Map.of()),p->-1).endsWith(" linear"));
        var f=fixture();var bytes=new java.io.ByteArrayOutputStream();var codec=new KnackDocumentCodec();codec.write(f.document(),bytes);
        var loaded=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
        var node=loaded.embeddedAssets().values().iterator().next().graph().nodes().stream().filter(n->n.typeId().equals(mix.registration().id())).findFirst().orElseThrow();
        assertEquals("equalpower",new NodeState(node.properties()).text(MixNode.MODE,""));
    }
    static Fixture fixture(){
        var inner=new GLNodeGraph();var osc=add(inner,new OscillatorNode());
        var mix=add(inner,new MixNode(),Map.of("mode",GLNodeValue.of("equalpower"),"b",GLNodeValue.of(.2),"factor",GLNodeValue.of(.25),"rate",GLNodeValue.of("audio")));
        var math=add(inner,new MathNode(),Map.of("b",GLNodeValue.of(1),"rate",GLNodeValue.of("audio")));
        connect(inner,osc,"audio-out",mix,"a");connect(inner,mix,"audio-out",math,"a");
        var descriptor=new GLCompositeNodeDescriptor("test.equalpower","test.equalpower.asset",1,List.of(output("Out",SignalCategory.VALUE,math,"audio-out")));
        var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var root=new GLNodeGraph();var group=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(group));var sink=add(root,new OutputNode());connect(root,group.id(),"Out",sink,"audio-in");
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),group,"",false);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void fusedEqualPowerExportMatchesOriginalAndActuallyFuses() throws Exception {
        var f=fixture();var export=GroupSourceExporter.create(f.document(),f.group());var name=export.className();var d=export.definition();
        var dir=Files.createTempDirectory(Path.of("target"),"equal-power-").toAbsolutePath();
        Files.writeString(dir.resolve(name+".hpp"),export.files().get("native/"+name+".hpp"));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(d);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->-1);
        String code="#include \"AudioGraph.hpp\"\n#include \""+name+".hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){"+name+" fused;std::istringstream config("+quote(payload)+");fused.configure(config);fused.prepare(48000,256);auto original=AudioGraph::parse("+quote(reference(f))+");std::array<float,256>a{},b{},silence{};ProcessContext c{nullptr,0,a.data(),256,48000,silence.data(),256};for(unsigned block=0;block<30;block++){for(unsigned channel=0;channel<2;channel++){c.channelIndex=channel;fused.process(c);original.process(b.data(),256,channel,nullptr);for(unsigned i=0;i<256;i++)assert(std::abs(a[i]-b[i])<1e-6);}}assert(fused.renderSampleRegion0(256,nullptr,nullptr));}";
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
