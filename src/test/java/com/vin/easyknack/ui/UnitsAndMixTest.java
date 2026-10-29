package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioGraphEncoder;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

class UnitsAndMixTest {
    @Test void samplesShareTheTimeDimensionAndPreserveStoredDuration() {
        assertEquals(480,UnitFormat.MS.convert(10,UnitFormat.SAMPLE),1e-9);
        var buffer=new HistoryBufferNode();var row=buffer.layout(new NodeState(buffer.defaults())).rows().stream().filter(r->r.key().equals(HistoryBufferNode.SECONDS)).findFirst().orElseThrow();
        var size=(GLUnitSlider<UnitFormat>)row.editor().create("Size",v->{}).component();
        size.setDisplayUnit(UnitFormat.SAMPLE);size.setValue(256);assertEquals(256,size.getValue(),1e-8);assertEquals("smp",size.getUnit());
        size.setValue(1);assertEquals(256,size.getValue(),1e-8);
        assertEquals(.01,UnitFormat.SAMPLE.convert(480,UnitFormat.SECOND),1e-12);
        assertThrows(IllegalArgumentException.class,()->UnitFormat.SAMPLE.convert(480,UnitFormat.HZ));
        var spec=Editor.Slider.button(2,0.0,30.0).formats(UnitFormats.TIME_SECONDS);
        var slider=(GLUnitSlider<UnitFormat>)spec.create("Time",v->{}).component();
        slider.setDisplayUnit(UnitFormat.SAMPLE);assertEquals(96000,slider.getValue(),1e-8);
        slider.setValue(480);assertEquals(.01,slider.getStoredValue(),1e-12);
        slider.setDisplayUnit(UnitFormat.MS);assertEquals(10,slider.getValue(),1e-8);
    }

    @Test void equivalentFormatsAndLegacyDefaultsAreStable(){
        assertEquals(1,UnitFormat.DB.convert(0,UnitFormat.FACTOR),1e-12);
        assertEquals(100,UnitFormat.FACTOR.convert(1,UnitFormat.PERCENT),1e-12);
        assertEquals(1200,UnitFormat.FACTOR.convert(2,UnitFormat.CENT),1e-9);
        assertEquals(-1,UnitFormat.FACTOR.convert(.5,UnitFormat.OCTAVE),1e-12);
        assertEquals(.5,UnitFormat.DEGREE.convert(180,UnitFormat.TURN),1e-12);
        assertThrows(IllegalArgumentException.class,()->UnitFormat.HZ.convert(440,UnitFormat.DB));
        var gain=new GainNode();
        assertEquals(UnitFormat.FACTOR,((UnitSlider)gain.layout(new NodeState(gain.defaults())).rows().stream().filter(r->r.key().equals(GainNode.GAIN_DB)).findFirst().orElseThrow().editor()).selected());
        assertEquals(UnitFormat.DB,((UnitSlider)gain.layout(new NodeState(Map.of("gainDb",GLNodeValue.of(-12)))).rows().stream().filter(r->r.key().equals(GainNode.GAIN_DB)).findFirst().orElseThrow().editor()).selected());
    }
    @Test void typedSliderDeclarationsUseCoreUnitsAndExplicitPools() {
        var fixed=Editor.Slider.button(440,0.0,20000.0).unit(UnitFormat.HZ);
        var control=fixed.create("Frequency",value->{});
        assertInstanceOf(GLUnitSlider.class,control.component());
        var slider=(GLUnitSlider<?>)control.component();
        assertEquals(List.of(UnitFormat.HZ),slider.getUnitFormats().choices());
        assertEquals("Hz",slider.getUnit());
        assertTrue(slider.getContextMenu().getItems().stream().noneMatch(i->i.getText().equals("Unit")));
        var gain=Editor.Slider.button(-12,-160.0,6.0).formats(UnitFormats.GAIN_DB);
        var gainControl=(GLUnitSlider<?>)gain.create("Gain",value->{}).component();
        assertEquals(UnitFormat.FACTOR,gainControl.getDisplayUnit());
        assertEquals(Math.pow(10,-12./20),gainControl.getValue(),1e-12);
        assertEquals(-12.,gainControl.getStoredValue(),1e-12);
        assertDoesNotThrow(()->Editor.Slider.button(1,null,10.0).unit(UnitFormat.Q).create("Q",value->{}));
    }
    @Test void inspectorFormatAndConnectedPortLabelTrackUnitWithoutChangingStoredValue() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try(var controller=new WorkspaceController()){
                var ws=controller.workspace();var graph=ws.currentGraph();var def=new OscillatorNode();var value=new ValueNode();
                var id=GLNodeId.random();var source=GLNodeId.random();var props=new HashMap<>(def.defaults());props.put("frequency",GLNodeValue.of(440));
                graph.edit("Add",e->{e.addNode(new GLGraphNode(id,def.registration().id(),props));e.addNode(new GLGraphNode(source,value.registration().id(),value.defaults()));});
                graph.edit("Units",e->e.setProperty(id,"frequency.unit",GLNodeValue.of("khz")));
                var view=ws.editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();
                ws.editor().setSelectedNode(view);
                var slider=view.getContents().stream().filter(c->c instanceof GLSlider s&&s.getText().equals("Frequency")).map(GLSlider.class::cast).findFirst().orElseThrow();
                assertEquals(.44,slider.getValue(),1e-10);assertEquals("Frequency [kHz]",slider.getName());
                var inspector=ws.propertyInspector().getGLChildren().stream().filter(c->c instanceof GLSlider s&&s.getText().equals("Frequency")).map(GLSlider.class::cast).findFirst().orElseThrow();
                assertEquals(slider.getPresentation(),inspector.getPresentation());assertEquals(slider.getUnit(),inspector.getUnit());
                inspector.setValue(.5);
                assertEquals(500,new NodeState(graph.snapshot().findNode(id).orElseThrow().properties()).number(OscillatorNode.FREQUENCY,0),1e-8);
                graph.edit("Connect",e->e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(source,ValueNode.OUT.value()),new GLNodeEndpoint(id,"frequency"))));
                String encoded=AudioGraphEncoder.encode(graph.snapshot());assertTrue(encoded.contains("unitconvert"));assertTrue(encoded.contains("khz hz @ c c"));
                assertEquals(1,view.findPort("frequency").getConnectionCount());
            }
        });
    }
    @Test void exclusionClosesLargeLinearGapToThemeSpacing() throws Exception {
        SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
            var ws=controller.workspace();var editor=ws.editor();
            var source=editor.getNodes().stream().filter(n->n.getTitle().equals("Oscillator")).findFirst().orElseThrow();
            var middle=editor.getNodes().stream().filter(n->n.getTitle().equals("Gain")).findFirst().orElseThrow();
            var target=editor.getNodes().stream().filter(n->n!=source&&n!=middle).findFirst().orElseThrow();
            source.setLocation(0,0);middle.setLocation(600,0);target.setLocation(1500,0);
            editor.setSelectedNode(middle);controller.actions().exclude();
            assertEquals(source.getX()+source.getWidth()+editor.getHorizontalNodeSpacing(),target.getX(),.01);
            controller.history().undo();assertEquals(1500,target.getX(),.01);
        }});
    }
}
