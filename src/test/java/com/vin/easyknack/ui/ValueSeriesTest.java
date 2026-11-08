package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.ui.*;
import java.util.*;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValueSeriesTest {
    private static NodeState settings(){return new NodeState(Map.of("inputs",GLNodeValue.of(3.),"name-0",GLNodeValue.of("Pitch"),"unit-0",GLNodeValue.of("hz")));}
    private static NodeState reordered(NodeState original){var values=new HashMap<>(original.values());values.put(ValueSeries.SETTINGS.value(),ValueSeries.encode(ValueSeries.move(ValueSeries.all(original),3,2,-1)));return new NodeState(values);}
    @Test void stableInputIdsLegacyMetadataAndNativeParametersArePreserved(){
        var before=settings();var after=reordered(before);
        assertEquals(List.of(0,2,1),ValueSeries.visible(after,3).stream().map(ValueSeries.Entry::input).toList());
        assertEquals("Pitch [Hz]",ValueSeries.visible(after,3).get(0).heading());
        assertEquals(8,ValueSeries.all(after).size());assertEquals(1,ValueSeries.visible(after,1).size());
        for(BasicNode node:List.of(new ValueViewerNode(),new MeasurementRecorderNode())){
            var a=new HashMap<>(node.defaults());a.putAll(before.values());var b=new HashMap<>(a);b.putAll(after.values());
            assertEquals(node.encode(new NodeState(a),p->p.value().equals("record")?-1:Integer.parseInt(p.value().substring(6))),node.encode(new NodeState(b),p->p.value().equals("record")?-1:Integer.parseInt(p.value().substring(6))));
            assertEquals(List.of("value-0","value-2","value-1"),node.layout(new NodeState(b)).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.INPUT&&r.id().startsWith("value-")).map(NodeBuilder.Row::id).toList());
            assertTrue(node.layout(new NodeState(b)).rows().stream().filter(r->r.id().startsWith("value-")).allMatch(r->r.editor()==null));
            assertFalse(node.layout(new NodeState(b)).rows().stream().anyMatch(r->r.id().startsWith("name-")||r.id().startsWith("unit-")));
        }
    }
    @Test void tableAndViewerExportsFollowDisplayOrderWithoutMutatingRecordedData()throws Exception{
        var state=reordered(settings());var entries=ValueSeries.visible(state,3);
        var table=MeasurementTable.merge(MeasurementTable.empty(),MeasurementRecorderTest.frame(3,1,7,"0"));
        var original=MeasurementTable.rows(table);var csv=new StringWriter();
        MeasurementTable.csv(table,entries.stream().map(ValueSeries.Entry::heading).toArray(String[]::new),entries.stream().mapToInt(ValueSeries.Entry::input).toArray(),csv,false);
        assertTrue(csv.toString().contains("\"Pitch [Hz]\",\"Value 3\",\"Value 2\""));assertTrue(csv.toString().endsWith(",0.0,2.0,1.0\r\n"));assertEquals(original,MeasurementTable.rows(table));
        float[][] values={{10},{20},{30}};var raw=new ValueHistory.Snapshot(new double[]{1},values,values,values,.01,new String[]{"A","B","C"},new String[]{"","",""},true,"test");
        var view=ValueSeries.reorder(raw,entries);assertArrayEquals(new float[]{30},view.value()[1]);assertArrayEquals(new float[]{20},raw.value()[1]);assertTrue(view.frozen());
        var out=new StringWriter();ValueViewerExport.encodeCsv(view,out,true,false);assertTrue(out.toString().contains("10.0,30.0,20.0"));
        var definition=new MeasurementRecorderNode();var properties=new HashMap<>(definition.defaults());properties.putAll(state.values());properties.put("table",table);
        var id=GLNodeId.random();var graph=new GLNodeGraph();graph.edit("Recorder",e->e.addNode(new GLGraphNode(id,definition.registration().id(),properties)));
        var document=new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty()));
        var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(document,bytes);
        var loaded=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
        var restored=loaded.root().graph().findNode(id).orElseThrow().properties();assertEquals(table,restored.get("table"));
        assertEquals(entries,ValueSeries.visible(new NodeState(restored),3));
    }
    @Test void inspectorPairsAndReorderButtonsEditOneSharedProperty()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            var changed=new AtomicReference<GLNodeValue>();var panel=new ValueSeriesSettingsPanel(settings(),3,changed::set);panel.setSize(280,122);
            var groups=panel.getGLChildren().stream().filter(GLLayoutGroup.class::isInstance).map(GLLayoutGroup.class::cast).toList();assertEquals(3,groups.size());
            var children=groups.stream().flatMap(g->g.getGLChildren().stream()).toList();
            var fields=children.stream().filter(GLTextEditor.class::isInstance).map(GLTextEditor.class::cast).toList();
            var units=children.stream().filter(GLComboBox.class::isInstance).map(GLComboBox.class::cast).toList();
            assertEquals(3,fields.size());assertEquals(fields.get(0).getY(),units.get(0).getY());assertTrue(units.get(0).getX()>fields.get(0).getX());
            assertFalse(groups.get(0).getOutlineShape().isEmpty());
            assertTrue(units.get(0).getPopupMenu().getItems().stream().filter(i->i.getSubMenu()!=null).count()>3);
            var down=children.stream().filter(c->c instanceof GLButton&&"series-down-0".equals(c.getName())).map(GLButton.class::cast).findFirst().orElseThrow();
            assertNotNull(down.getIcon());assertEquals("",down.getText());down.doClick();
            var s=new NodeState(Map.of(ValueSeries.SETTINGS.value(),changed.get()));assertEquals(List.of(1,0,2),ValueSeries.visible(s,3).stream().map(ValueSeries.Entry::input).toList());
        });
    }
    @Test void reorderingPreservesFrozenViewerConnectionsAndUndo()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var node=new ValueViewerNode();var properties=new HashMap<>(node.defaults());properties.putAll(settings().values());
            var id=GLNodeId.random();var source=GLNodeId.random();var graph=new GLNodeGraph();var value=new ValueNode();
            graph.edit("Viewer",e->{e.addNode(new GLGraphNode(id,node.registration().id(),properties));e.addNode(new GLGraphNode(source,value.registration().id(),value.defaults()));e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(source,"value-out"),new GLNodeEndpoint(id,"value-2")));});
            c.openDocument("Values",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty())));
            var view=c.workspace().editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();
            var panel=view.getContents().stream().filter(ValueViewerPanel.class::isInstance).map(ValueViewerPanel.class::cast).findFirst().orElseThrow();
            float[] packet={1,10,1,0,10,0,0,0,10,10,10,20,20,20,30,30,30};
            AudioAnalysis.publish(Map.of(id.toString(),new AudioAnalysis.Frame(3,1000,0,false,packet)));panel.setPaused(true);
            assertEquals(20,panel.snapshot(false).value()[1][0]);var connections=c.workspace().currentGraph().snapshot().connections();
            c.setNodeProperty(id,ValueSeries.SETTINGS,reordered(settings()).values().get(ValueSeries.SETTINGS.value()));
            java.util.function.Supplier<com.vingame.glswing.node.GLNode> current=()->c.workspace().editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();
            java.util.function.Supplier<ValueViewerPanel> currentPanel=()->current.get().getContents().stream().filter(ValueViewerPanel.class::isInstance).map(ValueViewerPanel.class::cast).findFirst().orElseThrow();
            assertEquals(List.of("value-0","value-2","value-1"),current.get().getPorts().stream().map(p->p.getId()).toList());
            assertEquals(30,currentPanel.get().snapshot(false).value()[1][0]);assertTrue(currentPanel.get().isPaused());assertEquals(connections,c.workspace().currentGraph().snapshot().connections());
            c.history().undo();assertEquals(20,currentPanel.get().snapshot(false).value()[1][0]);
            assertEquals(List.of("value-0","value-1","value-2"),current.get().getPorts().stream().map(p->p.getId()).toList());
        }finally{AudioAnalysis.publish(Map.of());}});
    }
}
