package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.SignalCategory;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class ForwardedInstanceCountTest {
    private record Fixture(GLNodeGraphDocument document, GLNodeGraphAsset wrapper,
                           GLNodeGraphAsset instance, GLNodeId child, GLCompositePortMapping count) {}
    private static Fixture fixture() {
        var instance = MultipleGroups.create();
        var child = new GLGraphNode(GLNodeId.random(), instance.compositeDescriptor().nodeTypeId(), Map.of());
        var graph = new GLNodeGraph();
        graph.edit("Nested instance", edit -> edit.addNode(child));
        // Reproduce older extraction copying the private role and managed flag.
        var fixed = MultipleGroups.countPort("Voices");
        var count = new GLCompositePortMapping(fixed.publicPort(),new GLNodeEndpoint(child.id(), "Count"),fixed.presentation());
        var output = new GLCompositePortMapping(GLNodePortDescriptor.output("out", SignalCategory.AUDIO.valueTypeId()),new GLNodeEndpoint(child.id(), "out"));
        String assetId = "forwarded-count", typeId = "test.forwarded.count";
        var wrapper = new GLNodeGraphAsset(assetId, 1, GLNodeGraphAssetKind.COMPOSITE, graph.snapshot(),
                GLNodeEditorViewState.empty(), Map.of("easyknack.title", GLNodeValue.of("Count wrapper"),
                "easyknack.kind", GLNodeValue.of("AUDIO")),
                new GLCompositeNodeDescriptor(typeId, assetId, 1, List.of(output, count)));
        var root = new GLNodeGraph();
        root.edit("Two wrapper instances", edit -> {
            edit.addNode(new GLGraphNode(GLNodeId.random(), typeId, Map.of()));
            edit.addNode(new GLGraphNode(GLNodeId.random(), typeId, Map.of()));
        });
        var rootAsset = new GLNodeGraphAsset("root", 1, GLNodeGraphAssetKind.PROJECT, root.snapshot(), GLNodeEditorViewState.empty());
        return new Fixture(new GLNodeGraphDocument(rootAsset,Map.of(assetId,wrapper,instance.id(),instance),List.of()),wrapper,instance,child.id(),count);
    }

    @Test void copyingCountKeepsItsEditorAndTargetButNotItsPrivateRole() {
        var f=fixture();var registry=new GLCompositeGraphRegistry();registry.registerDocument(f.document());
        var copied=GroupEditorCopies.inheritPort(f.count(),new GLNodeEndpoint(f.child(),"Count"),f.wrapper(),registry);
        assertFalse(MultipleGroups.countPort(copied));
        assertFalse(copied.presentation().editorOptions().containsKey("applicationManaged"));
        assertEquals("integer",copied.presentation().editorOptions().get("numericType").toJavaValue());
        assertEquals(f.count().internalEndpoints(),copied.internalEndpoints());
    }

    @Test void legacyWrapperShowsBothViewsAndPreservesCountWiringAcrossSaveReload() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var f=fixture();
            try(var controller=new WorkspaceController()) {
                controller.openDocument("Nested Count",f.document());
                assertEquals(2,controller.workspace().editor().getNodes().size());
                for(var view:controller.workspace().editor().getNodes())assertNotNull(view.findPort("Voices"));
                assertDoesNotThrow(controller::audioGraph);
                var saved=controller.snapshotDocument();
                var outer=saved.embeddedAssets().get(f.wrapper().id());
                var port=outer.compositeDescriptor().portMappings().stream().filter(p->p.publicPort().id().equals("Voices")).findFirst().orElseThrow();
                assertFalse(MultipleGroups.countPort(port));
                assertEquals(f.count().internalEndpoints(),port.internalEndpoints());
                var inner=saved.embeddedAssets().get(f.instance().id());
                assertTrue(inner.compositeDescriptor().portMappings().stream().anyMatch(MultipleGroups::countPort));
                var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(saved,bytes);
                controller.openDocument("Reload",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
                assertEquals(2,controller.workspace().editor().getNodes().size());
                assertDoesNotThrow(controller::audioGraph);
            } catch(java.io.IOException failure) { throw new AssertionError(failure); }
        });
    }
}
