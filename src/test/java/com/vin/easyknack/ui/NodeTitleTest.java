package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class NodeTitleTest {
    @Test void renamedNodeSurvivesDiagnosticsRebuildAndDocumentRoundTrip()throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try(var controller=new WorkspaceController()){
                controller.openDocument("Names",StarterDocument.create());
                var view=(BasicNodeView)controller.workspace().editor().getNodes().get(0);
                var id=view.nodeId();view.setCustomTitle("Meine Größe");
                view.executionArea("Shared");assertEquals("Meine Größe [Shared]",view.getTitle());
                view.executionArea("");assertEquals("Meine Größe",view.getTitle());
                controller.refreshAppearance();
                var current=controller.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView b&&b.nodeId().equals(id)).findFirst().orElseThrow();
                assertEquals("Meine Größe",current.getTitle());
                var codec=new KnackDocumentCodec();var bytes=new ByteArrayOutputStream();
                codec.write(controller.snapshotDocument(),bytes);
                var document=codec.read(new ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
                controller.openDocument("Restored",document);
                assertEquals("Meine Größe",controller.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView b&&b.nodeId().equals(id)).findFirst().orElseThrow().getTitle());
            }catch(IOException failure){throw new java.io.UncheckedIOException(failure);}
        });
    }
}
