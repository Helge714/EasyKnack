package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.binding.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class PartialsTest {
    static GLNodeGraphDocument document(boolean shifted){
        var group=PartialGroups.create();
        if(shifted){var g=new GLNodeGraph(group.graph());var context=g.snapshot().nodes().stream().filter(n->n.typeId().equals(new PartialContextNode().registration().id())).findFirst().orElseThrow();var out=g.snapshot().nodes().stream().filter(n->n.typeId().equals(new PartialOutputNode().registration().id())).findFirst().orElseThrow();
            var math=add(g,new MathNode(),Map.of("operation",GLNodeValue.of("add"),"b",GLNodeValue.of(100.),"clamp",GLNodeValue.of(false)));
            g.edit("Shift",e->{for(var w:g.snapshot().connections())if(w.input().portId().equals("frequency"))e.removeConnection(w.id());});connect(g,context.id(),"frequency",math,"a");connect(g,math,"audio-out",out.id(),"frequency");
            group=new GLNodeGraphAsset(group.id(),group.version(),group.kind(),g.snapshot(),group.viewState(),group.metadata(),group.compositeDescriptor());
        }
        var g=new GLNodeGraph();var osc=add(g,new OscillatorNode());var fft=add(g,new SpectrumNode(),Map.of("synchronous",GLNodeValue.of(true),"size",GLNodeValue.of("1024")));
        var analysis=add(g,new PartialAnalysisNode());var synthesis=add(g,new PartialsSpectrumNode());var mix=add(g,new SpectrumMathNode());var ifft=add(g,new IFFTNode());var output=add(g,new OutputNode());
        var instance=GLNodeId.random();var finalGroup=group;g.edit("Partial group",e->e.addNode(new GLGraphNode(instance,finalGroup.compositeDescriptor().nodeTypeId(),Map.of())));
        connect(g,osc,"audio-out",fft,"audio-in");connect(g,fft,"analysis",analysis,"spectrum");connect(g,analysis,"partials",instance,"partials");connect(g,instance,"out",synthesis,"partials");connect(g,analysis,"residual",mix,"b");connect(g,synthesis,"spectrum",mix,"a");connect(g,mix,"spectrum",ifft,"spectrum");connect(g,ifft,"out",output,"audio-in");
        return new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,g.snapshot(),GLNodeEditorViewState.empty()),Map.of(group.id(),group),List.of());
    }
    static GLGraphNode compiled(GLNodeGraphDocument doc){var r=new GLCompositeGraphRegistry();r.registerDocument(doc);var flat=SpectralGroups.flatten(GroupCompiler.prepare(doc.root(),r),new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(flat.isValid(),flat.diagnostics().toString());assertDoesNotThrow(()->com.vin.easyknack.audio.AudioGraphEncoder.encode(flat.graph().orElseThrow()));return flat.graph().orElseThrow().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof PartialGraphNode).findFirst().orElseThrow();}
    @Test void groupsCompilePersistAndHaveASeparatePalette()throws Exception{
        for(boolean shift:List.of(false,true)){var doc=document(shift);compiled(doc);var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(doc,bytes);compiled(NodeDocuments.prepare(codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion())));}
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){c.addPartialGroup(0,0);c.actions().openSelectedComposite();var ids=c.availableNodes().stream().map(e->e.type().id()).toList();assertTrue(ids.contains(new PartialContextNode().registration().id()));assertTrue(ids.contains(new DigitalNoiseNode().registration().id()));assertTrue(ids.contains(new PartialNoiseNode().registration().id()));assertFalse(ids.contains(new BinContextNode().registration().id()));assertFalse(ids.contains(new OscillatorNode().registration().id()));}});
    }
    static GLNodeGraphDocument noiseDocument(){
        var doc=document(false);var asset=doc.embeddedAssets().values().iterator().next();var graph=new GLNodeGraph(asset.graph());
        var out=graph.snapshot().nodes().stream().filter(n->n.typeId().equals(new PartialOutputNode().registration().id())).findFirst().orElseThrow();
        var noise=add(graph,new PartialNoiseNode());graph.edit("Partial noise",e->{for(var w:graph.snapshot().connections())if(w.input().nodeId().equals(out.id())&&w.input().portId().equals("amplitude"))e.removeConnection(w.id());});
        connect(graph,noise,"out",out.id(),"amplitude");
        var context=graph.snapshot().nodes().stream().filter(n->n.typeId().equals(new PartialContextNode().registration().id())).findFirst().orElseThrow();
        connect(graph,context.id(),"seed",noise,"seed");
        var changed=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),asset.compositeDescriptor());
        return new GLNodeGraphDocument(doc.root(),Map.of(changed.id(),changed),List.of());
    }
    @Test void partialNoiseIsContextOnlyAndCompilesWithStableIds(){
        compiled(noiseDocument());assertTrue(new PartialNoiseNode().requiresPartialContext());
        assertEquals(List.of("frequency","amplitude","phase","id","age","index","time","seed","tonality"),new PartialContextNode().layout(new NodeState(Map.of())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).map(NodeBuilder.Row::id).toList());
        assertEquals("New Partial Group",AppText.text("partials.new"));
    }
    @Test void partialResourcesCanBeExportedAtGroupBoundaries(){
        var doc=noiseDocument();var instance=doc.root().graph().nodes().stream().filter(n->doc.embeddedAssets().values().stream().anyMatch(a->a.compositeDescriptor().nodeTypeId().equals(n.typeId()))).findFirst().orElseThrow();
        var exported=GroupSourceExporter.create(doc,instance);
        assertFalse(exported.files().isEmpty());
        assertTrue(exported.files().values().stream().anyMatch(s->s.contains("PartialGraphNode")));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void reconstructionPhaseTrackingAndPreparedStorage()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"partials-native-").toAbsolutePath();var def=new PartialGraphNode();
        var analysis=new PartialAnalysisNode();var values=new HashMap<>(analysis.defaults());values.put("maximum-partials",GLNodeValue.of(400.));values.put("threshold",GLNodeValue.of(-80.));
        String wide="!"+NativeContractGenerator.fingerprint(analysis)+" "+analysis.encode(new NodeState(values),p->-1);
        var identity=compiled(document(false));var shift=compiled(document(true));var noisy=compiled(noiseDocument());
        String prefix="!"+NativeContractGenerator.fingerprint(def)+" ";
        Files.writeString(dir.resolve("PartialsTestParameters.hpp"),"#pragma once\nstatic const char* wideAnalysisParameters=R\"params("+wide+")params\";\nstatic const char* identityParameters=R\"params("+prefix+def.encode(new NodeState(identity.properties()),p->0)+")params\";\nstatic const char* shiftParameters=R\"params("+prefix+def.encode(new NodeState(shift.properties()),p->0)+")params\";\nstatic const char* noiseParameters=R\"params("+prefix+def.encode(new NodeState(noisy.properties()),p->0)+")params\";\n");
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/PartialsTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/LorisAnalysisTest.cpp").toAbsolutePath(),dir.resolve("classification.exe"),dir);
    }
}
