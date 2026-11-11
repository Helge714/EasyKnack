package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.vin.easyknack.nodes.OscillatorNode;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.model.GLNodeValue;

class StartupCheckpointTest {
    @TempDir Path directory;

    private static boolean modified(DocumentActions documents) throws Exception {
        var method = DocumentActions.class.getDeclaredMethod("modified");
        method.setAccessible(true);
        return (boolean) method.invoke(documents);
    }

    @Test void firstThemedLayoutIsCleanButLaterEditsRemainUnsaved() throws Exception {
        check(false, false);
    }

    @Test void firstLayoutDoesNotSaveModelEdits() throws Exception {
        check(true, false);
    }

    @Test void firstLayoutDoesNotSaveRecoveredDocuments() throws Exception {
        check(false, true);
    }

    @Test void loadingAndResizingDoNotPromptButNodeAndModelEditsRemainDirty()throws Exception{
        var source=directory.resolve("ordinary.knack");
        try(var out=java.nio.file.Files.newOutputStream(source)){new com.vin.easyknack.document.KnackDocumentCodec().write(StarterDocument.create(),out);}
        var c=new WorkspaceController[1];var documents=new DocumentActions[1];var frame=new JFrame[1];
        try{
            SwingUtilities.invokeAndWait(()->{
                frame[0]=new JFrame();c[0]=new WorkspaceController();
                var owner=new GLContainer(){protected void record(com.vingame.glswing.rendering.GLContext2D context){}};
                documents[0]=new DocumentActions(c[0],owner,frame[0],s->{},directory.resolve("recovery"));
                owner.setGLTheme(ApplicationTheme.current().theme());owner.addGL(c[0].workspace());
                c[0].workspace().setBounds(0,0,1280,734);documents[0].initializeViewCheckpoint();
            });
            SwingUtilities.invokeAndWait(()->{try{
                documents[0].openInitial(source);c[0].workspace().setBounds(0,0,900,600);
                c[0].workspace().editor().setZoom(.7f);
                assertNotNull(c[0].captureDocument().root().viewState().camera(),"Camera still persists on save");
                assertFalse(modified(documents[0]));
                assertTrue(documents[0].requestClose().toCompletableFuture().getNow(false),"No unsaved-changes dialog");
                var node=c[0].workspace().editor().getNodes().get(0);node.setLocation(node.getX()+20,node.getY());
                assertTrue(modified(documents[0]),"Actual node movement must remain unsaved");
                documents[0].openInitial(source);assertFalse(modified(documents[0]));
                changeFrequency(c[0]);assertTrue(modified(documents[0]),"Actual parameter edits must remain unsaved");
            }catch(Exception failure){throw new AssertionError(failure);}});
        }finally{SwingUtilities.invokeAndWait(()->{if(documents[0]!=null)documents[0].close();if(c[0]!=null)c[0].close();if(frame[0]!=null)frame[0].dispose();});}
    }

    private void check(boolean edit, boolean recovered) throws Exception {
        var controller = new WorkspaceController[1];
        var documents = new DocumentActions[1];
        var window = new JFrame[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                window[0] = new JFrame();
                controller[0] = new WorkspaceController();
                var owner = new GLContainer() {
                    @Override protected void record(com.vingame.glswing.rendering.GLContext2D context) { }
                };
                documents[0] = new DocumentActions(controller[0], owner, window[0], s -> {}, directory);
                owner.setGLTheme(ApplicationTheme.current().theme());
                owner.addGL(controller[0].workspace());
                controller[0].workspace().setBounds(0, 0, 1280, 734);
                if (edit) changeFrequency(controller[0]);
                if (recovered) {
                    try {
                        var field = DocumentActions.class.getDeclaredField("checkpoint");
                        field.setAccessible(true);
                        field.set(documents[0], null);
                    } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
                }
                documents[0].initializeViewCheckpoint();
            });
            SwingUtilities.invokeAndWait(() -> {
                try {
                    assertEquals(edit || recovered, modified(documents[0]));
                    if (!edit && !recovered) changeFrequency(controller[0]);
                    documents[0].initializeViewCheckpoint();
                } catch (Exception e) { throw new AssertionError(e); }
            });
            SwingUtilities.invokeAndWait(() -> {
                try { assertTrue(modified(documents[0])); }
                catch (Exception e) { throw new AssertionError(e); }
            });
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                if (documents[0] != null) documents[0].close();
                if (controller[0] != null) controller[0].close();
                if (window[0] != null) window[0].dispose();
            });
        }
    }

    private static void changeFrequency(WorkspaceController controller) {
        var node = controller.workspace().currentGraph().snapshot().nodes().stream()
                .filter(n -> n.typeId().equals(new OscillatorNode().registration().id())).findFirst().orElseThrow();
        controller.setNodeProperty(node.id(), OscillatorNode.FREQUENCY, GLNodeValue.of(330.0));
    }
}
