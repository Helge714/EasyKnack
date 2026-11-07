package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.ui.GLSlider;
import com.vingame.glswing.node.model.GLNodeValue;

class GroupInspectorCameraTest {
 @Test void inspectorUpdatesSelectedInstancesTogetherAndUndoesOnce()throws Exception{
  SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
   c.openDocument("Selection",GroupUnitsTest.document(false));var second=com.vingame.glswing.node.model.GLNodeId.random();c.workspace().currentGraph().edit("Instance",e->e.addNode(new com.vingame.glswing.node.model.GLGraphNode(second,"test.group",java.util.Map.of())));
   c.workspace().editor().setSelectedNodes(c.workspace().editor().getNodes().stream().filter(GroupViewFactory.GroupView.class::isInstance).toList());
   var slider=GroupEditingTest.descendants(c.workspace().propertyInspector()).filter(GLSlider.class::isInstance).map(GLSlider.class::cast).filter(s->s.getName().equals("Pitch")).findFirst().orElseThrow();slider.setValue(880);
   for(var id:java.util.List.of(second,GroupUnitsTest.GROUP))assertEquals(GLNodeValue.of(880.),c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get(GroupUnits.key("Pitch")));
   assertTrue(c.history().undo());for(var id:java.util.List.of(second,GroupUnitsTest.GROUP))assertFalse(c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().containsKey(GroupUnits.key("Pitch")));
  }});
 }
 @Test void occupiedBinaryInputReroutesInTheModelAndUndoesAsOneGesture()throws Exception{
  SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
   c.openDocument("Binary",new com.vingame.glswing.node.document.GLNodeGraphDocument(new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,new com.vingame.glswing.node.model.GLNodeGraph().snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty())));
   var graph=c.workspace().currentGraph();var first=com.vingame.glswing.node.model.GLNodeId.random();var second=com.vingame.glswing.node.model.GLNodeId.random();var math=com.vingame.glswing.node.model.GLNodeId.random();var value=new com.vin.easyknack.nodes.ValueNode();var operation=new com.vin.easyknack.nodes.MathNode();
   graph.edit("Nodes",e->{e.addNode(new com.vingame.glswing.node.model.GLGraphNode(first,value.registration().id(),value.defaults()));e.addNode(new com.vingame.glswing.node.model.GLGraphNode(second,value.registration().id(),value.defaults()));e.addNode(new com.vingame.glswing.node.model.GLGraphNode(math,operation.registration().id(),operation.defaults()));});
   var editor=c.workspace().editor();java.util.function.Function<com.vingame.glswing.node.model.GLNodeId,BasicNodeView> view=id->editor.getNodes().stream().filter(n->n instanceof BasicNodeView b&&b.nodeId().equals(id)).map(BasicNodeView.class::cast).findFirst().orElseThrow();
   var a=view.apply(math).findPort("a");var b=view.apply(math).findPort("b");editor.connect(view.apply(first).getPorts().stream().filter(p->p.getDirection()==com.vingame.glswing.node.GLNodePort.Direction.OUTPUT).findFirst().orElseThrow(),a);
   var before=editor.getConnections();editor.connect(view.apply(second).getPorts().stream().filter(p->p.getDirection()==com.vingame.glswing.node.GLNodePort.Direction.OUTPUT).findFirst().orElseThrow(),a);
   assertEquals(2,graph.snapshot().connections().size());assertTrue(graph.snapshot().connections().stream().anyMatch(edge->edge.output().nodeId().equals(first)&&edge.input().portId().equals("b")));
   try{var record=com.vingame.glswing.node.GLNodeEditor.class.getDeclaredMethod("recordConnectionChange",java.util.List.class,java.util.List.class);record.setAccessible(true);record.invoke(editor,before,editor.getConnections());}catch(ReflectiveOperationException ex){throw new AssertionError(ex);}
   assertTrue(c.history().undo());assertEquals(1,graph.snapshot().connections().size());assertEquals("a",graph.snapshot().connections().get(0).input().portId());
  }});
 }
 @Test void everyPublicGroupEditorKeepsItsPresentationAndDefault(){
  var template=new EditorTemplates.Template("choice","Choice",java.util.List.of(new EditorTemplates.Entry("a","A",GLNodeValue.of(0.)),new EditorTemplates.Entry("b","B",GLNodeValue.of(1.))),"a");
  var presentations=java.util.List.of(GroupUnitsTest.slider(),
   new com.vingame.glswing.node.composite.GLCompositePortPresentation(com.vingame.glswing.node.composite.GLNodeInputEditorRegistry.SLIDER,true,java.util.Map.of("presentation",GLNodeValue.of("checkbox"),"default",GLNodeValue.of(1.))),
   new com.vingame.glswing.node.composite.GLCompositePortPresentation(GroupInputSettings.BUTTON,true,java.util.Map.of()),
   new com.vingame.glswing.node.composite.GLCompositePortPresentation(EditorTemplates.PREFIX+"choice",true,java.util.Map.of("template",template.value(),GroupInputs.CHOICE,GLNodeValue.of("choice"))),
   new com.vingame.glswing.node.composite.GLCompositePortPresentation(GroupVectorEditors.ID,true,java.util.Map.of(GroupUnits.BASE,GLNodeValue.of("metre"),GroupUnits.ALLOWED,GLNodeValue.of(java.util.List.of("metre")),"default",com.vin.easyknack.nodes.VectorEditor.value(1,2,3,com.vin.easyknack.nodes.UnitFormat.METRE))));
  for(var presentation:presentations){
   var doc=GroupUnitsTest.document(false);var original=doc.embeddedAssets().get("test.asset");var p=original.compositeDescriptor().portMappings().get(0);
   p=new com.vingame.glswing.node.composite.GLCompositePortMapping(p.publicPort(),p.internalEndpoint(),presentation);
   var d=new com.vingame.glswing.node.composite.GLCompositeNodeDescriptor("test.group","test.asset",1,java.util.List.of(p));
   var asset=new com.vingame.glswing.node.document.GLNodeGraphAsset(original.id(),1,original.kind(),original.graph(),original.viewState(),original.metadata(),d);
   var groups=new com.vingame.glswing.node.composite.GLCompositeGraphRegistry();groups.registerDocument(new com.vingame.glswing.node.document.GLNodeGraphDocument(doc.root(),java.util.Map.of(asset.id(),asset),java.util.List.of()));
   var writes=new java.util.ArrayList<GLNodeValue>();var node=doc.root().graph().findNode(GroupUnitsTest.GROUP).orElseThrow();
   var control=new GroupInspectorInputs.Control(groups,d,p,node,writes::add,new com.vingame.glswing.history.GLHistoryManager(),(id,key,value)->writes.add(value));
   assertFalse(control.getGLChildren().isEmpty());
   if(presentation==presentations.get(1)){var check=(com.vingame.glswing.ui.GLCheckBox)control.getGLChildren().get(0);assertTrue(check.isSelected());check.doClick();assertEquals(GLNodeValue.of(0.),writes.get(0));}
   if(presentation==presentations.get(3))assertTrue(control.getGLChildren().stream().anyMatch(com.vingame.glswing.ui.GLComboBox.class::isInstance));
   if(presentation==presentations.get(4)){var vector=(com.vingame.glswing.ui.GLSliderGroup)control.getGLChildren().get(0);assertEquals(3,vector.getSliders().size());assertEquals(1,vector.getSliders().get(0).getValue());}
  }
 }
 @Test void inheritedSliderIsEditableInInspectorWithoutAnInstanceOverride()throws Exception{
  SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
   c.openDocument("Defaults",GroupUnitsTest.document(false));c.workspace().editor().setSelectedNode(GroupUnitsTest.group(c));
   var slider=GroupEditingTest.descendants(c.workspace().propertyInspector()).filter(GLSlider.class::isInstance).map(GLSlider.class::cast).filter(s->s.getName().equals("Pitch")).findFirst().orElseThrow();
   assertEquals(440,slider.getValue());assertEquals("Hz",slider.getUnit());slider.setValue(880);
   assertEquals(GLNodeValue.of(880.),c.workspace().currentGraph().snapshot().findNode(GroupUnitsTest.GROUP).orElseThrow().properties().get(GroupUnits.key("Pitch")));
   assertTrue(c.history().undo());assertEquals(440,slider.getValue());
  }});
 }
 @Test void navigationRestoresGraphCentersAndKeepsTheSharedZoom()throws Exception{
  SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController();var restored=new WorkspaceController()){
   c.openDocument("Camera",GroupUnitsTest.document(false));var w=c.workspace();w.setBounds(0,0,1000,700);var editor=w.editor();
   editor.setZoom(1);editor.setPan(120,80);var root=c.snapshotDocument().root().viewState().camera();assertNotNull(root);
   w.openComposite("test.asset");editor.setPan(-250,180);editor.setZoom(2);var group=c.snapshotDocument().embeddedAssets().get("test.asset").viewState().camera();
   w.navigateBack();assertEquals(2,editor.getZoom());assertEquals(root.x(),c.snapshotDocument().root().viewState().camera().x(),.01);
   w.openComposite("test.asset");assertEquals(group.x(),c.snapshotDocument().embeddedAssets().get("test.asset").viewState().camera().x(),.01);
   var saved=c.snapshotDocument();restored.openDocument("Restored",saved);restored.workspace().setBounds(0,0,1000,700);assertEquals(root,restored.snapshotDocument().root().viewState().camera());
  }});
 }
}
