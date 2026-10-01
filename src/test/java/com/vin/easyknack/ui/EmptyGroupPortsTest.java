package com.vin.easyknack.ui;

import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class EmptyGroupPortsTest {
    record Fixture(GLNodeGraphDocument document,GLGraphNode group){}
    static Fixture fixture(GLNodeGraphAsset asset,boolean allEmpty){
        var d=asset.compositeDescriptor();var ports=new ArrayList<GLCompositePortMapping>();
        for(var p:d.portMappings())ports.add(allEmpty&&!MultipleGroups.countPort(p)?p.withEndpoints(List.of()):p);
        ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input("unused",SignalCategory.VALUE.valueTypeId(),false,1),null));
        ports.add(new GLCompositePortMapping(GLNodePortDescriptor.output("empty",SignalCategory.VALUE.valueTypeId()),null));
        asset=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),asset.metadata(),new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports,d.properties(),d.propertyMappings()));
        var root=new GLNodeGraph();var group=new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(group));
        var value=add(root,new ValueNode());var math=add(root,new MathNode());var output=add(root,new OutputNode());
        connect(root,value,"value-out",group.id(),"unused");connect(root,group.id(),"empty",math,"a");connect(root,math,"audio-out",output,"audio-in");
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),group);
    }
    @Test void ordinaryInstanceAndSpectralGroupsAllowUnusedBoundaries()throws Exception{
        var ordinary=GroupUnitsTest.document(false).embeddedAssets().get("test.asset");
        for(var asset:List.of(ordinary,MultipleGroups.create(),SpectralGroups.create()))for(boolean empty:List.of(false,true)){
            var f=fixture(asset,empty);
            SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
                c.openDocument("Empty ports",f.document());assertDoesNotThrow(c::audioGraph);
                assertEquals(f.document().root().graph().connections().size(),c.snapshotDocument().root().graph().connections().size());
            }});
            assertDoesNotThrow(()->GroupSourceExporter.create(f.document(),f.group()));
        }
    }
    @Test void nestedEmptyMappingIsValidButMissingMappingRemainsAnError(){
        var inner=fixture(SpectralGroups.create(),true).document().embeddedAssets().values().iterator().next();
        // Use an ordinary wrapper around intentionally empty ordinary ports.
        inner=new GLNodeGraphAsset(inner.id(),inner.version(),inner.kind(),new GLNodeGraph().snapshot(),inner.viewState(),Map.of(),new GLCompositeNodeDescriptor(inner.compositeDescriptor().nodeTypeId(),inner.id(),1,List.of(new GLCompositePortMapping(GLNodePortDescriptor.output("empty",SignalCategory.VALUE.valueTypeId()),null))));
        var child=new GLGraphNode(GLNodeId.random(),inner.compositeDescriptor().nodeTypeId(),Map.of());var graph=new GLNodeGraph();graph.edit("Child",e->e.addNode(child));
        var descriptor=new GLCompositeNodeDescriptor("wrapper.type","wrapper",1,List.of(output("out",SignalCategory.VALUE,child.id(),"empty")));
        var wrapper=new GLNodeGraphAsset("wrapper",1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var f=fixture(wrapper,false);var registry=new GLCompositeGraphRegistry();registry.registerAsset(inner);registry.registerDocument(f.document());
        assertTrue(GLCompositeGraphFlattener.flatten(f.document().root(),registry,GLCompositeExpansionLimits.DEFAULT).isValid());
        var broken=new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),1,List.of(output("out",SignalCategory.VALUE,child.id(),"missing")));
        registry.replaceAsset(new GLNodeGraphAsset(wrapper.id(),1,wrapper.kind(),wrapper.graph(),wrapper.viewState(),Map.of(),broken));
        assertFalse(GLCompositeGraphFlattener.flatten(f.document().root(),registry,GLCompositeExpansionLimits.DEFAULT).isValid());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void hostKeepsRunningAndAnEmptyExportOutputsSilence()throws Exception{
        var ordinary=GroupUnitsTest.document(false).embeddedAssets().get("test.asset");
        try(var host=new com.vin.easyknack.audio.AudioHostClient()){
            host.start(com.vin.easyknack.audio.AudioHostClient.defaultExecutable(),true);
            for(var asset:List.of(ordinary,MultipleGroups.create(),SpectralGroups.create())){
                var f=fixture(asset,true);var command=new java.util.concurrent.atomic.AtomicReference<String>();
                SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){c.openDocument("Empty",f.document());command.set(c.audioGraph());}});
                var encoded=command.get();assertDoesNotThrow(()->{assertEquals("OK",host.command(encoded));assertEquals("OK",host.command("PLAY"));assertEquals("OK",host.command("STOP"));},asset.id()+": "+encoded);
            }
        }
        var f=fixture(ordinary,true);var export=GroupSourceExporter.create(f.document(),f.group());
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(export.definition());
        var dir=Files.createTempDirectory(Path.of("target"),"empty-export-").toAbsolutePath();
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var payload="!"+NativeContractGenerator.fingerprint(export.definition())+" "+export.definition().encode(new NodeState(export.definition().defaults()),p->-1);
        var source=dir.resolve("test.cpp");Files.writeString(source,"""
            #include "%s.hpp"
            #include <cassert>
            int main(){easyknack::%s node;std::istringstream p(%s);node.configure(p);node.prepare(48000,256);
                float out[256]{},other[256]{},silence[256]{};float* outputs[]{out,other};const float* inputs[16]{};
                easyknack::ProcessContext c{inputs,16,out,256,48000,silence,256};c.outputs=outputs;c.outputCount=2;
                node.process(c);for(int i=0;i<256;i++)assert(out[i]==0&&other[i]==0);
            }
            """.formatted(export.className(),export.className(),quote(payload)));
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
