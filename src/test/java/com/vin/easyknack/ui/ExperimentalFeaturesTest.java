package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.vin.easyknack.nodes.*;

class ExperimentalFeaturesTest {
    @TempDir Path directory;
    @Test void optInControlsCreationWithoutRemovingExistingGraphs()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            String pitch=new PitchHoldNode().registration().id();
            assertTrue(new PitchHoldNode().isExperimental());assertFalse(new MathNode().isExperimental());
            assertTrue(new PitchHoldNode().title().contains(AppText.text("experimental")));
            assertFalse(c.experimentalFeatures);assertFalse(c.availableNodes().stream().anyMatch(e->e.type().id().equals(pitch)));
            c.experimentalFeatures=true;
            var entry=c.availableNodes().stream().filter(e->e.type().id().equals(pitch)).findFirst().orElseThrow();
            c.experimentalFeatures=false;int count=c.workspace().currentGraph().snapshot().nodes().size();
            c.workspace().createNode(entry,100,100);assertEquals(count,c.workspace().currentGraph().snapshot().nodes().size());
            var document=HarmonizerExample.create();assertTrue(ExperimentalFeatures.requiresOptIn(document));
            c.openDocument("Existing experiment",document);assertTrue(c.experimentalFeatures);
            assertEquals(document.embeddedAssets().keySet(),c.snapshotDocument().embeddedAssets().keySet());
        }});
    }
    @Test void exampleDiscoveryUsesExplicitDocumentState()throws Exception{
        var file=directory.resolve("Renamed experiment.knack");
        try(var out=Files.newOutputStream(file)){new com.vin.easyknack.document.KnackDocumentCodec().write(HarmonizerExample.create(),out);}
        assertTrue(ExampleLibrary.scan(directory,false).isEmpty());
        var entries=ExampleLibrary.scan(directory,true);assertEquals(1,entries.size());assertTrue(entries.get(0).name().contains(AppText.text("experimental")));
        try(var out=Files.newOutputStream(file)){new com.vin.easyknack.document.KnackDocumentCodec().write(StarterDocument.create(),out);}
        assertEquals(1,ExampleLibrary.scan(directory,false).size());
    }
    @Test void legacyMigrationAndWarningRespectExplicitDocumentDecisions()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var first=new WorkspaceController();var second=new WorkspaceController()){
            first.openDocument("Legacy",HarmonizerExample.create());assertTrue(first.experimentalFeatures);assertFalse(second.experimentalFeatures);
            assertTrue(DocumentSettings.needsExperimentalWarning(first,false));assertFalse(DocumentSettings.needsExperimentalWarning(first,true));
            var saved=first.snapshotDocument();assertTrue(DocumentSettings.experimental(saved));
            first.experimentalFeatures=false;saved=first.snapshotDocument();
            second.openDocument("Explicitly disabled",saved);assertFalse(second.experimentalFeatures);
            assertTrue(ExperimentalFeatures.usedInDocument(second.snapshotDocument()));
            first.openDocument("Ordinary",StarterDocument.create());assertFalse(first.experimentalFeatures);
            first.experimentalFeatures=true;assertFalse(DocumentSettings.needsExperimentalWarning(first,false));
        }});
    }
    @Test void exportedPluginRetainsExperimentalMetadata()throws Exception{
        var document=HarmonizerExample.create();
        var group=document.root().graph().nodes().stream().filter(n->n.typeId().equals("easyknack.group.harmonizer")).findFirst().orElseThrow();
        var export=GroupSourceExporter.create(document,group);assertTrue(export.definition().isExperimental());
        var source=directory.resolve(export.className()+".java");Files.writeString(source,export.files().get("java/com/vin/easyknack/nodes/"+export.className()+".java"));
        var log=new java.io.ByteArrayOutputStream();
        assertEquals(0,javax.tools.ToolProvider.getSystemJavaCompiler().run(null,log,log,"-proc:none","-classpath",System.getProperty("java.class.path"),"-d",directory.toString(),source.toString()),log.toString());
        try(var loader=new java.net.URLClassLoader(new java.net.URL[]{directory.toUri().toURL()},getClass().getClassLoader())){
            var compiled=(BasicNode)loader.loadClass("com.vin.easyknack.nodes."+export.className()).getConstructor().newInstance();
            assertTrue(compiled.isExperimental());
        }
    }
    @Test void documentFlagRoundTripsAndStoredUnusedGroupsAreDetected()throws Exception{
        var legacy=HarmonizerExample.create();
        var unused=new com.vingame.glswing.node.document.GLNodeGraphDocument(StarterDocument.create().root(),legacy.embeddedAssets(),List.of());
        assertTrue(DocumentSettings.experimental(DocumentSettings.initializeExperimental(unused)));
        var enabled=DocumentSettings.initializeExperimental(legacy);
        var codec=new com.vin.easyknack.document.KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(enabled,bytes);
        assertTrue(DocumentSettings.experimental(NodeDocuments.prepare(codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()))));
        var root=enabled.root();var metadata=new HashMap<>(root.metadata());metadata.put(DocumentSettings.EXPERIMENTAL,com.vingame.glswing.node.model.GLNodeValue.of(false));
        var disabled=new com.vingame.glswing.node.document.GLNodeGraphDocument(new com.vingame.glswing.node.document.GLNodeGraphAsset(root.id(),root.version(),root.kind(),root.graph(),root.viewState(),metadata,root.compositeDescriptor()),enabled.embeddedAssets(),enabled.externalAssets());
        bytes.reset();codec.write(disabled,bytes);
        assertFalse(DocumentSettings.experimental(NodeDocuments.prepare(codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()))));
    }
}
