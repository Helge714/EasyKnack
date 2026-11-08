package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;
class GroupCheckboxTest {
 @Test void checkboxSurvivesSpaceRenameToggleUndoSaveAndExport()throws Exception{
  javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController();var reopened=new WorkspaceController()){
   c.openDocument("Checkbox",GroupUnitsTest.document(false));c.workspace().openComposite("test.asset");c.workspace().inspectorTabs().setSelectedIndex(3);var ui=c.workspace().interfaceEditor();
   GroupEditingTest.descendants(ui).filter(n->n instanceof GLList<?> l&&l.getModel().getSize()>0&&l.getModel().getElementAt(0) instanceof GLCompositePortMapping m&&m.publicPort().direction()==GLPortDirection.INPUT).map(n->(GLList<?>)n).findFirst().orElseThrow().setSelectedIndex(0);
   var settings=GroupEditingTest.descendants(ui).filter(GroupInputSettings.class::isInstance).map(GroupInputSettings.class::cast).findFirst().orElseThrow();settings.kind.setSelectedItem(GroupInputSettings.Kind.CHECKBOX);
   var port=ui.selectedPort();assertTrue(GroupInputSettings.checkbox(port));
   var named=new GLCompositePortMapping(GLNodePortDescriptor.input("Enable Tone",port.publicPort().valueTypeId(),false,1),port.internalEndpoint(),port.presentation(),port.additionalEndpoints());ui.updateSelectedPort(named);
   var saved=c.snapshotDocument();reopened.openDocument("Restored",saved);
   var check=GroupUnitsTest.group(reopened).getContents().stream().filter(GLCheckBox.class::isInstance).map(GLCheckBox.class::cast).findFirst().orElseThrow();assertEquals("Enable Tone",check.getText());assertFalse(check.isSelected());check.doClick();assertTrue(check.isSelected());
   String graph=reopened.audioGraph().replaceAll(" ![0-9a-f]{16} "," ");assertTrue(graph.contains("unitconvert -1 1.0 factor factor"),graph);
   reopened.history().undo();assertFalse(check.isSelected());
   var snapshot=reopened.snapshotDocument();var instance=snapshot.root().graph().findNode(GroupUnitsTest.GROUP).orElseThrow();
   var export=GroupSourceExporter.create(snapshot,instance);assertTrue(export.files().values().stream().anyMatch(text->text.contains("new Editor.Toggle(")));
   try{var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(snapshot,bytes);assertEquals(snapshot,codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));}catch(java.io.IOException e){throw new AssertionError(e);}
  }});
 }
 @Test void gainHasAdaptiveNumericInput(){
  var def=new GainNode();var row=def.layout(new NodeState(def.defaults())).rows().stream().filter(r->r.key().equals(GainNode.GAIN_DB)).findFirst().orElseThrow();assertEquals(NodeBuilder.RowKind.INPUT,row.kind());assertTrue(def.processor().contract().inputs().contains(GainNode.GAIN_DB));
 }
}
