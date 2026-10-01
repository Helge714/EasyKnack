package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.*;

class SpectralInputMigrationTest {
    @Test void legacyNestedReaderGetsExplicitSpectrumConnectionButNewReaderDoesNot(){
        var definition=new BinReadNode();
        var legacy=new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults()).withoutProperty(BinReadNode.EXPLICIT);
        var fresh=new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults());
        var innerGraph=new GLNodeGraph();innerGraph.edit("Readers",e->{e.addNode(legacy);e.addNode(fresh);});
        var innerDescriptor=new GLCompositeNodeDescriptor("test.bin.subgroup","bin-subgroup",1,List.of());
        var inner=new GLNodeGraphAsset("bin-subgroup",1,GLNodeGraphAssetKind.COMPOSITE,innerGraph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),innerDescriptor);
        var outer=SpectralGroups.create();var outerGraph=new GLNodeGraph(outer.graph());
        var instance=new GLGraphNode(GLNodeId.random(),innerDescriptor.nodeTypeId(),Map.of());outerGraph.edit("Nested reader",e->e.addNode(instance));
        outer=new GLNodeGraphAsset(outer.id(),1,outer.kind(),outerGraph.snapshot(),outer.viewState(),outer.metadata(),outer.compositeDescriptor());
        var root=new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,new GLNodeGraph().snapshot(),GLNodeEditorViewState.empty());
        var document=new GLNodeGraphDocument(root,Map.of(inner.id(),inner,outer.id(),outer),List.of());
        var migrated=SpectralInputMigration.migrate(document);
        var migratedInner=migrated.embeddedAssets().get(inner.id());
        assertEquals(GLNodeValue.of(true),migratedInner.graph().findNode(legacy.id()).orElseThrow().properties().get(BinReadNode.EXPLICIT));
        var port=migratedInner.compositeDescriptor().portMappings().get(0);
        assertEquals(List.of(new GLNodeEndpoint(legacy.id(),"spectrum")),port.internalEndpoints());
        var outerPort=migrated.embeddedAssets().get(outer.id()).compositeDescriptor().portMappings().stream().filter(p->p.publicPort().id().equals("spectrum")).findFirst().orElseThrow();
        assertTrue(outerPort.internalEndpoints().contains(new GLNodeEndpoint(instance.id(),port.publicPort().id())));
        assertEquals(NodeDocuments.comparable(migrated),NodeDocuments.comparable(SpectralInputMigration.migrate(migrated)));
    }
}
