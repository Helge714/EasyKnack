package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.node.document.GLNodeGraphDocumentCodec;
import com.vingame.glswing.ui.GLCheckBox;
import com.vingame.glswing.ui.GLUiKeys;

class NodeMirroringTest {
    @Test void boundaryOrientationIsEditableAndSurvivesReopening() throws Exception {
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Mirroring",GroupUnitsTest.document(false));c.workspace().openComposite("test.asset");
            var editor=c.workspace().editor();var boundary=editor.getNodes().stream().filter(n->n.getAutoLayoutRole()==com.vingame.glswing.node.GLNodeAutoLayout.Role.INPUT_BOUNDARY).findFirst().orElseThrow();
            editor.setSelectedNode(boundary);
            var checkbox=GroupEditingTest.descendants(c.workspace().propertyInspector()).filter(GLCheckBox.class::isInstance).map(GLCheckBox.class::cast).filter(b->b.getText().equals(boundary.getGLLocalizer().text(GLUiKeys.NODE_MIRRORED))).findFirst().orElseThrow();
            checkbox.doClick();assertTrue(boundary.isMirrored());
            c.workspace().navigateBack();c.workspace().openComposite("test.asset");
            var restored=editor.getNodes().stream().filter(n->n.getAutoLayoutRole()==com.vingame.glswing.node.GLNodeAutoLayout.Role.INPUT_BOUNDARY).findFirst().orElseThrow();
            assertTrue(restored.isMirrored());assertTrue(c.history().undo());assertFalse(restored.isMirrored());
        }});
    }

    @Test void menuInspectorUndoAndNavigationChangeOnlyPresentation() throws Exception {
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Mirroring",GroupUnitsTest.document(false));
            var group=GroupUnitsTest.group(c);var editor=c.workspace().editor();editor.setSelectedNode(group);
            var graph=c.workspace().currentGraph().snapshot();
            group.getContextMenu().getItems().stream().filter(i->i.getText().equals(group.getGLLocalizer().text(GLUiKeys.NODE_MIRROR))).findFirst().orElseThrow().doClick();
            assertTrue(group.isMirrored());assertEquals(graph,c.workspace().currentGraph().snapshot());
            assertTrue(c.history().undo());assertFalse(group.isMirrored());assertTrue(c.history().redo());assertTrue(group.isMirrored());
            var checkbox=GroupEditingTest.descendants(c.workspace().propertyInspector()).filter(GLCheckBox.class::isInstance).map(GLCheckBox.class::cast).filter(b->b.getText().equals(group.getGLLocalizer().text(GLUiKeys.NODE_MIRRORED))).findFirst().orElseThrow();
            assertTrue(checkbox.isSelected());
            c.workspace().setGLLocalizer(com.vingame.glswing.ui.GLLocalizer.GERMAN);assertEquals("Gespiegelt",checkbox.getText());
            c.workspace().setGLLocalizer(com.vingame.glswing.ui.GLLocalizer.ENGLISH);assertEquals("Mirrored",checkbox.getText());
            checkbox.doClick();assertFalse(group.isMirrored());assertEquals(graph,c.workspace().currentGraph().snapshot());
            assertTrue(c.history().undo());assertTrue(group.isMirrored());assertTrue(c.history().redo());assertFalse(group.isMirrored());
            editor.getEditingActions().toggleMirrored();assertTrue(group.isMirrored());
            assertTrue(c.workspace().openComposite("test.asset"));assertTrue(c.workspace().navigateBack());assertTrue(GroupUnitsTest.group(c).isMirrored());
        }});
    }
    @Test void duplicateAndDocumentRoundTripKeepMirroring() throws Exception {
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Mirroring",GroupUnitsTest.document(false));
            var editor=c.workspace().editor();editor.setSelectedNode(GroupUnitsTest.group(c));editor.getEditingActions().toggleMirrored();
            editor.getEditingActions().duplicate();assertEquals(1,editor.getSelectedNodes().size());assertTrue(editor.getSelectedNodes().get(0).isMirrored());
            try {
                var document=c.snapshotDocument();var codec=new GLNodeGraphDocumentCodec();var bytes=new ByteArrayOutputStream();codec.write(document,bytes);
                var decoded=codec.read(new ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());assertEquals(document,decoded);
                c.openDocument("Reloaded",decoded);assertTrue(GroupUnitsTest.group(c).isMirrored());
            } catch(IOException ex){throw new AssertionError(ex);}
        }});
    }
}
