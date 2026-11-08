package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;

class WorkspaceEditingRegressionTest {
 @Test void stepRowsEditNamesAndValuesWithoutDuplicateInspectorControls()throws Exception{
  SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
   var definition=new StepSwitchNode();var id=GLNodeId.random();
   c.workspace().currentGraph().edit("Step Switch",e->e.addNode(new GLGraphNode(id,definition.registration().id(),definition.defaults())));
   c.workspace().editor().setSelectedNode(c.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView view&&view.nodeId().equals(id)).findFirst().orElseThrow());
   var panel=GroupEditingTest.descendants(c.workspace().propertyInspector()).filter(StepOrderEditor.Panel.class::isInstance).map(StepOrderEditor.Panel.class::cast).findFirst().orElseThrow();
   assertEquals(4,panel.getGLChildren().size());
   var row=(com.vingame.glswing.ui.GLLayoutGroup)panel.getGLChildren().get(0);
   var name=(com.vingame.glswing.ui.GLTextEditor)row.getGLChildren().get(0);name.setText("Lead");assertTrue(name.commit());
   assertEquals("Lead",c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("step-name-0").toJavaValue());
   var value=(com.vingame.glswing.ui.GLSlider)row.getGLChildren().get(1);value.setValue(.75);
   assertEquals(GLNodeValue.of(.75),c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("step-0"));
   assertNotNull(((com.vingame.glswing.ui.GLButton)row.getGLChildren().get(2)).getIcon());
   assertTrue(c.history().undo());assertEquals(GLNodeValue.of(0.),c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("step-0"));
   assertTrue(GroupEditingTest.descendants(c.workspace().propertyInspector()).filter(InspectorEditor.class::isInstance).noneMatch(control->control.getName().equals("step-0")||control.getName().equals("step-name-0")));
  }});
 }
 @Test void groupHistoryStaysLocalAndSurvivesReopening()throws Exception{
  SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
   c.openDocument("Scopes",GroupUnitsTest.document(false));var root=c.workspace().currentGraph().snapshot().nodes().get(0).id();
   c.setNodeProperty(root,new PropertyId(GroupUnits.key("Pitch")),GLNodeValue.of(220.));
   c.workspace().openComposite("test.asset");assertFalse(c.history().canUndo());
   c.setNodeProperty(GroupUnitsTest.OSC,OscillatorNode.PHASE,GLNodeValue.of(.25));
   c.workspace().navigateBack();assertTrue(c.history().undo());
   assertEquals(.25,((Number)c.snapshotDocument().embeddedAssets().get("test.asset").graph().findNode(GroupUnitsTest.OSC).orElseThrow().properties().get("phase").toJavaValue()).doubleValue());
   c.workspace().openComposite("test.asset");assertTrue(c.history().undo());
   assertEquals(0.,((Number)c.workspace().currentGraph().snapshot().findNode(GroupUnitsTest.OSC).orElseThrow().properties().get("phase").toJavaValue()).doubleValue());
  }});
 }
 @Test void insertionRetainsBothSegmentsAfterProjectionAndNavigation()throws Exception{
  for(boolean incoming:List.of(true,false)){
   var controller=new WorkspaceController[1];var inserted=GLNodeId.random();
   SwingUtilities.invokeAndWait(()->{
    var c=controller[0]=new WorkspaceController();c.openDocument("Boundary",GroupUnitsTest.document(false));c.workspace().openComposite("test.asset");var ed=c.workspace().editor();
    var math=new MathNode();c.workspace().currentGraph().edit("New math",e->e.addNode(new GLGraphNode(inserted,math.registration().id(),math.defaults())));
    var node=ed.getNodes().stream().filter(n->n instanceof BasicNodeView view&&view.nodeId().equals(inserted)).findFirst().orElseThrow();
    var edge=ed.getConnections().stream().filter(w->incoming?w.getOutput().getId().equals("Pitch"):w.getInput().getId().equals("Out")).findFirst().orElseThrow();
    assertTrue(ed.getConnectionInsertionHandler().insert(node,edge,node.getX(),node.getY()));
   });
   SwingUtilities.invokeAndWait(()->{try(var c=controller[0]){
    assertEquals(1,c.workspace().currentGraph().snapshot().connections().size());
    assertEquals(3,c.workspace().editor().getConnections().size());
    c.workspace().navigateBack();c.workspace().openComposite("test.asset");
    assertEquals(3,c.workspace().editor().getConnections().size());
    assertTrue(c.history().undo());assertEquals(2,c.workspace().editor().getConnections().size());
   }});
  }
 }
 @Test void batchImportCopiesSharedDependenciesOnceAndUndoesTogether()throws Exception{
  var source=GroupUnitsTest.document(true);var batch=GroupImport.copyMany(source,List.of("test.outer.asset","test.asset"),"Imported");
  assertEquals(2,batch.assets().size());assertEquals(2,batch.selected().size());
  assertEquals(batch.selected().get(1).compositeDescriptor().nodeTypeId(),batch.selected().get(0).graph().nodes().get(0).typeId());
  SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
   int count=c.workspace().currentGraph().snapshot().nodes().size();c.importGroups(batch);
   assertEquals(count+2,c.workspace().currentGraph().snapshot().nodes().size());assertTrue(c.history().undo());assertEquals(count,c.workspace().currentGraph().snapshot().nodes().size());assertTrue(c.history().redo());
  }});
 }
 @Test void copyListExcludesNestedControlsWithSockets(){
  var source=GroupUnitsTest.document(true);var groups=new GLCompositeGraphRegistry();source.embeddedAssets().values().forEach(groups::registerAsset);
  var outer=source.embeddedAssets().get("test.outer.asset");
  assertTrue(GroupInputSettings.availableSources(outer,ProcessingRate.CONTROL,"Pitch",groups).isEmpty());
 }
}
