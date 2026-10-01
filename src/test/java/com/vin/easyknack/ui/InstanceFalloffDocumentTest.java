package com.vin.easyknack.ui;

import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in, non-destructive migration of the user's falloff experiment. */
class InstanceFalloffDocumentTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.falloffDocument",matches="true")
    void convert() throws Exception {
        GLNodeGraphDocument document;
        try(var documents=new NodeDocuments()){
            document=documents.read(Path.of(System.getProperty("easyknack.benchmark.document"))).toCompletableFuture().join();
        }
        var asset=document.embeddedAssets().values().stream().filter(MultipleGroups::multiple).findFirst().orElseThrow();
        var d=asset.compositeDescriptor();
        var falloff=d.portMappings().stream().filter(p->p.publicPort().id().equals("Falloff")).findFirst().orElseThrow();
        var graph=new GLNodeGraph(asset.graph());
        var nodes=graph.snapshot().nodes();
        var subtract=nodes.stream().filter(n->n.id().equals(falloff.internalEndpoint().nodeId())).findFirst().orElseThrow();
        assertEquals("subtract",new NodeState(subtract.properties()).text("operation",""));
        var power=nodes.stream().filter(n->new NodeState(n.properties()).text("operation","").equals("power")).findFirst().orElseThrow();
        var harmonic=nodes.stream().filter(n->new NodeState(n.properties()).text("operation","").equals("add")).findFirst().orElseThrow();
        var reciprocal=graph.snapshot().connections().stream().filter(w->w.input().nodeId().equals(power.id())&&w.input().portId().equals("a")).findFirst().orElseThrow().output().nodeId();
        // Preserve node identities and layout: -slope -> +0 -> harmonic^(-slope) -> *1.
        var finalDivide=graph.snapshot().connections().stream().filter(w->w.output().nodeId().equals(power.id())).findFirst().orElseThrow().input().nodeId();
        graph.edit("Complex oscillator falloff",e->{
            e.setProperty(subtract.id(),"operation",GLNodeValue.of("multiply"));e.setProperty(subtract.id(),"b",GLNodeValue.of(-1.));
            e.setProperty(reciprocal,"operation",GLNodeValue.of("add"));e.setProperty(reciprocal,"a",GLNodeValue.of(0.));
            e.setProperty(finalDivide,"operation",GLNodeValue.of("multiply"));e.setProperty(finalDivide,"a",GLNodeValue.of(1.));
            for(var w:graph.snapshot().connections())if(w.input().nodeId().equals(power.id()))e.removeConnection(w.id());
            e.addConnection(SpectralGroupsTest.wire(harmonic.id(),"audio-out",power.id(),"a"));
            e.addConnection(SpectralGroupsTest.wire(reciprocal,"audio-out",power.id(),"b"));
        });
        var options=new HashMap<>(falloff.presentation().editorOptions());
        options.put("maximum",GLNodeValue.of(4.));options.put("default",GLNodeValue.of(1.));
        options.keySet().removeIf(k->k.startsWith("easyknack.unit."));
        var replacement=new GLCompositePortMapping(falloff.publicPort(),falloff.internalEndpoint(),new GLCompositePortPresentation(falloff.presentation().inputEditorId(),falloff.presentation().socketVisible(),options),falloff.additionalEndpoints());
        var descriptor=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings().stream().map(p->p==falloff?replacement:p).toList(),d.properties(),d.propertyMappings());
        var changed=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),descriptor);
        var assets=new HashMap<>(document.embeddedAssets());assets.put(asset.id(),changed);
        var root=document.root();var metadata=new HashMap<>(root.metadata());metadata.remove(GroupBakes.SAVED);
        root=new GLNodeGraphAsset(root.id(),root.version(),root.kind(),root.graph(),root.viewState(),metadata,root.compositeDescriptor());
        var result=new GLNodeGraphDocument(root,assets,document.externalAssets());
        NodeDocuments.prepare(result);
        Path output=Path.of("target/InstanceGroup-falloff.knack");
        try(var out=Files.newOutputStream(output)){new KnackDocumentCodec().write(result,out);}
        try(var in=Files.newInputStream(output)){var codec=new KnackDocumentCodec();assertEquals(NodeDocuments.comparable(result),NodeDocuments.comparable(codec.read(in,codec.currentSchemaVersion())));}
    }
}
