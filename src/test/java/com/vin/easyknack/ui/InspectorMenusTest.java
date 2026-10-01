package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;
class InspectorMenusTest {
    @Test void audioOutputCustomEditorsAppearAndStayLive()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            var previous=AudioEndpoints.current();
            var speakers=new AudioEndpoint("out:inspector-speakers","Speakers",AudioEndpoint.Type.HARDWARE_OUTPUT,false,true,false);
            try(var c=new WorkspaceController()){
                AudioEndpoints.publish(new AudioEndpoints.Snapshot(previous.capabilities(),List.of(AudioEndpoint.DEFAULT,speakers)));
                AudioDeviceVolumes.publish(Map.of("default",new AudioDeviceVolumes.Value(.4,false,.1),speakers.id(),new AudioDeviceVolumes.Value(.1,false,.2)));
                var ws=c.workspace();ws.editor().setSelectedNode(ws.editor().getNodes().stream().filter(n->n.getTitle().equals("Audio Output")).findFirst().orElseThrow());
                java.util.function.Function<String,InspectorEditor> control=id->ws.propertyInspector().getGLChildren().stream().filter(n->n instanceof InspectorEditor&&n.getName().equals(id)).map(InspectorEditor.class::cast).findFirst().orElseThrow();
                var chooser=(GLComboBox<AudioEndpoint>)control.apply(OutputNode.ENDPOINT.value()).control();
                chooser.setSelectedItem(speakers);
                var volume=(GLSlider)control.apply(OutputNode.DEVICE_VOLUME.value()).control();
                assertEquals(10,volume.getValue());assertEquals(GLSlider.Presentation.BAR,volume.getPresentation());
                assertNotNull(control.apply(OutputNode.LEVEL.value()));
                AudioDeviceVolumesTest.drainRequests();volume.setValue(25);
                assertEquals(Map.of(speakers.id(),.25),AudioDeviceVolumesTest.drainRequests());
                AudioDeviceVolumes.publish(Map.of(speakers.id(),new AudioDeviceVolumes.Value(.6,false,.3)));
                assertEquals(60,volume.getValue());
            }finally{AudioDeviceVolumes.publish(Map.of());AudioEndpoints.publish(previous);}
        });
    }
    @Test void unitMenusWorkInNodeAndInspectorAndUndo()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var ws=c.workspace();var id=ws.currentGraph().snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof GainNode).findFirst().orElseThrow().id();
            java.util.function.Supplier<GLNode> node=()->ws.editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();
            ws.editor().setSelectedNode(node.get());
            java.util.function.Supplier<GLSlider> slider=()->node.get().getContents().stream().filter(GLSlider.class::isInstance).map(GLSlider.class::cast).findFirst().orElseThrow();
            var menu=slider.get().getContextMenu().getItems().stream().filter(i->i.getText().equals("Unit")).findFirst().orElseThrow().getSubMenu();
            menu.getItems().stream().filter(i->i.getText().equals("Decibels")).findFirst().orElseThrow().doClick();
            assertEquals("dB",slider.get().getUnit());
            var inspector=ws.propertyInspector().getGLChildren().stream().filter(GLSlider.class::isInstance).map(GLSlider.class::cast).findFirst().orElseThrow();
            assertFalse(ws.propertyInspector().getGLChildren().stream().anyMatch(n->n instanceof GLLabel label&&label.getText().equals("Gain unit")));
            inspector.getContextMenu().getItems().stream().filter(i->i.getText().equals("Unit")).findFirst().orElseThrow().getSubMenu().getItems().stream().filter(i->i.getText().equals("Percent")).findFirst().orElseThrow().doClick();
            assertEquals("%",slider.get().getUnit());c.history().undo();assertEquals("dB",slider.get().getUnit());
        }});
    }
    @Test void converterPoolsAndMergeContractAreUnambiguous(){
        for(var converter:List.of(new GainConverterNode(),new PitchConverterNode(),new FrequencyConverterNode(),new TimeConverterNode(),new PhaseConverterNode())){
            var rows=converter.layout(new NodeState(converter.defaults())).rows();
            var from=(Editor.ComboBox<?>)rows.stream().filter(r->r.key().equals(UnitConverterNode.FROM)).findFirst().orElseThrow().editor();
            var to=(Editor.ComboBox<?>)rows.stream().filter(r->r.key().equals(UnitConverterNode.TO)).findFirst().orElseThrow().editor();
            for(var a:from.items())for(var b:to.items())assertTrue(((UnitFormat)a).compatible((UnitFormat)b));
        }
        var def=new VoiceMergeNode();var layout=def.layout(new NodeState(def.defaults()));
        assertEquals(List.of("audio-out","mode","audio-in"),layout.rows().stream().map(NodeBuilder.Row::id).toList());
        assertEquals(List.of("sum","mean"),((Editor.ComboBox<?>)layout.rows().get(1).editor()).items().stream().map(i->((VoiceMergeNode.Mode)i).id()).toList());
        var registry=new NodeRegistry(new GLNodeTypeRegistry(),new GLNodeCatalog());
        var legacy=new GLGraphNode(GLNodeId.random(),"easyknack.math.unitconverter",Map.of("from",GLNodeValue.of("hz"),"to",GLNodeValue.of("khz")));
        assertTrue(NodeRegistry.require(registry.normalize(legacy).typeId()) instanceof FrequencyConverterNode);
    }
}
