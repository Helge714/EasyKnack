package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.ui.*;
class GroupVectorCopyTest {
 @Test void standaloneVectorCopiesCoupleAndRoundTrip() throws Exception {
  var a=GroupAuthoringTest.asset(new VectorNode(),"position");var p=a.compositeDescriptor().portMappings().get(0);
  assertTrue(GroupVectorEditors.isVector(p));assertEquals(2,GroupEditorCopies.targets(p).size());
  assertTrue(GroupInputSettings.availableSources(a,ProcessingRate.AUTO).stream().anyMatch(s->s.property().equals("position")));
  var value=VectorEditor.value(2,3,4,UnitFormat.CENTIMETRE);var doc=GroupAuthoringTest.document(a,value);var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(doc,bytes);doc=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
  var registry=new GLCompositeGraphRegistry();registry.registerDocument(doc);var prepared=GroupCompiler.prepare(doc.root(),registry);var flat=GLCompositeGraphFlattener.flatten(prepared.root(),prepared.registry(),new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(flat.isValid(),flat.diagnostics().toString());
  for(var n:flat.graph().orElseThrow().nodes())assertEquals(value,n.properties().get("position"));
  var loaded=doc;SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){c.openDocument("Vector",loaded);var view=GroupUnitsTest.group(c);var group=(GLSliderGroup)view.getContents().stream().filter(GLSliderGroup.class::isInstance).findFirst().orElseThrow();assertEquals(200,group.getSliders().get(0).getValue());c.workspace().openComposite(a.id());for(var node:c.workspace().editor().getNodes())if(node instanceof BasicNodeView basic){basic.controlledByGroup(Set.of(VectorNode.VALUE));for(var row:node.getContents())if(row instanceof GLSliderGroup xyz)assertTrue(xyz.getSliders().stream().noneMatch(GLSlider::isEnabled));}}});
 }
 @Test void connectedVectorEditorCreatesTypedFallback(){
  var def=new VectorRotateNode();var id=GLNodeId.random();var graph=new GLNodeGraph();graph.edit("Rotate",e->e.addNode(new GLGraphNode(id,def.registration().id(),def.defaults())));
  var endpoint=new GLNodeEndpoint(id,"rotation");var port=new GLCompositePortMapping(GLNodePortDescriptor.input("Rotation",SignalCategory.VECTOR.valueTypeId(),false,1),endpoint);
  var d=new GLCompositeNodeDescriptor("test.rotation","test.rotation.asset",1,List.of(port));var a=new GLNodeGraphAsset(d.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),d);var registry=new GLCompositeGraphRegistry();registry.registerAsset(a);
  port=GroupEditorCopies.copy(port,endpoint,a,registry);assertTrue(GroupVectorEditors.isVector(port));assertEquals(UnitFormat.DEGREE,GroupVectorEditors.editor(port).formats().stored());
  a=GroupInputs.descriptor(a,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,List.of(port)));var doc=GroupAuthoringTest.document(a,VectorEditor.value(0,0,90,UnitFormat.DEGREE));registry=new GLCompositeGraphRegistry();registry.registerDocument(doc);var prepared=GroupCompiler.prepare(doc.root(),registry);var flat=GLCompositeGraphFlattener.flatten(prepared.root(),prepared.registry(),new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(flat.isValid(),flat.diagnostics().toString());assertEquals(2,flat.graph().orElseThrow().nodes().size());
  assertNotNull(com.vin.easyknack.audio.AudioGraphEncoder.encode(flat.graph().orElseThrow()));
 }
}
