package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.swing.SwingUtilities;
import java.nio.file.*;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.node.model.*;

class WorkspaceFileOperationsRegressionTest {
 @TempDir Path dir;
 @Test void importedGroupsRetainTheirMenuCategory()throws Exception{
  SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
   var doc=GroupUnitsTest.document(false);var old=doc.embeddedAssets().get("test.asset");var metadata=new HashMap<>(old.metadata());metadata.put("easyknack.category",GLNodeValue.of("Analysis"));
   var asset=new com.vingame.glswing.node.document.GLNodeGraphAsset(old.id(),old.version(),old.kind(),old.graph(),old.viewState(),metadata,old.compositeDescriptor());
   doc=new com.vingame.glswing.node.document.GLNodeGraphDocument(doc.root(),Map.of(asset.id(),asset),List.of());var imported=GroupImport.copyMany(doc,List.of(asset.id()),"Import");c.importGroups(imported);
   try{var field=WorkspaceController.class.getDeclaredField("catalog");field.setAccessible(true);var catalog=(GLNodeCatalog)field.get(c);assertEquals(NodeCategory.ANALYSIS.path(),catalog.find(imported.selected().get(0).compositeDescriptor().nodeTypeId()).orElseThrow().category());}catch(ReflectiveOperationException ex){throw new AssertionError(ex);}
  }});
 }
 @Test void selectAllImportsDefinitionsSeparatelyFromTheRoot()throws Exception{
  SwingUtilities.invokeAndWait(()->{var d=GroupUnitsTest.document(true);var panel=new GroupImportPanel(d,ids->{},()->{});panel.all.doClick();assertEquals(2,panel.selectedIds().size());assertFalse(panel.choices.get(0).isSelected());assertTrue(panel.add.isEnabled());panel.choices.get(0).doClick();panel.all.doClick();assertEquals(List.of(d.root().id()),panel.selectedIds());});
 }
 @Test void recordingCopyPreservesBytesAndSaveIsDisabledUntilCompleted()throws Exception{
  var source=Files.write(dir.resolve("source.wav"),new byte[]{1,2,3,4});var dest=Files.write(dir.resolve("copy.wav"),new byte[]{9});RecordingExportEditor.copy(source,dest);assertArrayEquals(Files.readAllBytes(source),Files.readAllBytes(dest));
  SwingUtilities.invokeAndWait(()->{try{var editor=new RecordingExportEditor.Save();editor.bind("save-test");assertFalse(editor.button.isEnabled());
   byte[] path=source.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);float[] values=new float[4+path.length];values[0]=-9151;values[1]=1;values[3]=path.length;for(int i=0;i<path.length;i++)values[4+i]=path[i]&255;
   AudioAnalysis.publish(Map.of("save-test",new AudioAnalysis.Frame(1,0,0,values)));assertFalse(editor.button.isEnabled());values=values.clone();values[1]=2;AudioAnalysis.publish(Map.of("save-test",new AudioAnalysis.Frame(1,0,0,values)));assertTrue(editor.button.isEnabled());
   var definition=new AudioRecorderNode();assertTrue(definition.layout(new NodeState(definition.defaults())).rows().stream().filter(r->r.key().equals(AudioRecorderNode.DIGITS)).findFirst().orElseThrow().isInspectorOnly());
  }finally{AudioAnalysis.clear();}});
 }
 @Test void boundaryInputReroutesToTheFreeOperandAndPersists()throws Exception{
  var ref=new WorkspaceController[1];var math=GLNodeId.random();var value=GLNodeId.random();
  SwingUtilities.invokeAndWait(()->{var c=ref[0]=new WorkspaceController();c.openDocument("Boundary",GroupUnitsTest.document(false));c.workspace().openComposite("test.asset");var g=c.workspace().currentGraph();var m=new MathNode();var v=new ValueNode();g.edit("Nodes",e->{e.addNode(new GLGraphNode(math,m.registration().id(),m.defaults()));e.addNode(new GLGraphNode(value,v.registration().id(),v.defaults()));});
   var ed=c.workspace().editor();var boundary=ed.getConnections().stream().filter(edge->edge.getOutput().getId().equals("Pitch")).findFirst().orElseThrow();var port=boundary.getOutput();ed.removeConnection(boundary);var mv=view(c,math);ed.connect(port,mv.findPort("a"));
  });
  SwingUtilities.invokeAndWait(()->{var c=ref[0];var ed=c.workspace().editor();ed.connect(view(c,value).getPorts().get(0),view(c,math).findPort("a"));});
  SwingUtilities.invokeAndWait(()->{try(var c=ref[0]){var asset=c.snapshotDocument().embeddedAssets().get("test.asset");var mapping=asset.compositeDescriptor().portMappings().stream().filter(p->p.publicPort().id().equals("Pitch")).findFirst().orElseThrow();assertEquals(new GLNodeEndpoint(math,"b"),mapping.internalEndpoint());c.workspace().navigateBack();c.workspace().openComposite("test.asset");assertTrue(c.workspace().editor().getConnections().stream().anyMatch(edge->edge.getOutput().getId().equals("Pitch")&&edge.getInput()==view(c,math).findPort("b")));}});
 }
 private static BasicNodeView view(WorkspaceController c,GLNodeId id){return c.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView b&&b.nodeId().equals(id)).map(BasicNodeView.class::cast).findFirst().orElseThrow();}
}
