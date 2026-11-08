package com.vin.easyknack.ui;
import com.vin.easyknack.compiler.CompiledManualTargets;
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

class SpectralHoldTest {
    static SpectralUniformTest.Fixture fixture(boolean external){
        var asset=SpectralGroups.create();var graph=new GLNodeGraph(asset.graph());
        var context=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinContextNode).findFirst().orElseThrow();
        var output=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinOutputNode).findFirst().orElseThrow();
        var hold=add(graph,new BinHoldNode());
        graph.edit("Hold",e->{for(var w:graph.snapshot().connections())e.removeConnection(w.id());
            e.addConnection(SpectralGroupsTest.wire(context.id(),"real",hold,"in"));e.addConnection(SpectralGroupsTest.wire(hold,"out",output.id(),"a"));});
        var descriptor=asset.compositeDescriptor();var ports=new ArrayList<>(descriptor.portMappings());
        ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input("capture",SignalCategory.TRIGGER.valueTypeId(),false,1),new GLNodeEndpoint(hold,"trigger")));
        descriptor=new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),1,ports);
        var metadata=new HashMap<>(asset.metadata());metadata.put(SpectralGroups.SKIP_EMPTY,GLNodeValue.of("true"));
        asset=new GLNodeGraphAsset(asset.id(),1,asset.kind(),graph.snapshot(),asset.viewState(),metadata,descriptor);
        var root=new GLNodeGraph();var trigger=add(root,new TriggerInputNode());var instance=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());
        root.edit("Capture",e->{e.addNode(instance);if(external)e.addConnection(SpectralGroupsTest.wire(trigger,"trigger-out",instance.id(),"capture"));});
        return new SpectralUniformTest.Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),instance);
    }
    @Test void compilesTriggerBoundaryAndManualAliases(){
        var node=SpectralUniformTest.compiled(fixture(true));
        assertEquals("0",new NodeState(node.properties()).text("triggerSlots",""));
        String program=new String(HexFormat.of().parseHex(new NodeState(node.properties()).text("program","")),java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(program.contains("binhold"));assertTrue(program.contains("bintrigger"));
        assertFalse(CompiledManualTargets.read(node).isEmpty());
        var f=fixture(true);assertNotNull(GroupSourceExporter.create(f.document(),f.instance()));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void capturesAllBinsAtFrameTimeInRuntimeAndExport()throws Exception{
        for(boolean external:List.of(false,true)){
            var f=fixture(external);var node=SpectralUniformTest.compiled(f);var def=NodeRegistry.require(node.typeId());
            String payload="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(node.properties()),p->p.equals(SpectralGraphNode.IN)?0:p.equals(SpectralGraphNode.external(0))?1:-1);
            var export=GroupSourceExporter.create(f.document(),f.instance());var d=export.definition();
            String exported="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->p.value().equals("input_0")?0:external?1:-1);
            var dir=Files.createTempDirectory(Path.of("target"),"spectral-hold-").toAbsolutePath();
            Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
            var defs=new ArrayList<>(NodeRegistry.builtinDefinitions());defs.add(d);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(defs));
            String code=Files.readString(Path.of("native/audio-host/tests/SpectralHoldTest.cpp"))
                .replace("EXPORT_HEADER",export.className()+".hpp").replace("EXPORT_TYPE",export.className()).replace("DIRECT_PAYLOAD",quote(payload)).replace("EXPORT_PAYLOAD",quote(exported)).replace("EXTERNAL_TRIGGER",external?"true":"false");
            var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
        }
    }
}
