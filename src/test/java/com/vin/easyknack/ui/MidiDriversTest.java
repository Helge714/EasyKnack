package com.vin.easyknack.ui;

import com.vin.easyknack.audio.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MidiDriversTest {
    static MidiDriverBinding binding(boolean cc){return new MidiDriverBinding("1","Computer keyboard",0,7,cc,new GLDriverMapping(0,1,false,false,false),false,64);}
    static void send(MidiDrivers drivers,int... messages){MidiDriverInput.publish(Arrays.stream(messages).mapToObj(m->new MidiDriverInput.Event("1",m)).toList());drivers.flush();}
    static int cc(int value){return 0xb0|7<<8|value<<16;}
    static int note(int value){return 0x90|7<<8|value<<16;}
    static double value(WorkspaceController c,GLNodeId id,String property){return ((Number)c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get(property).toJavaValue()).doubleValue();}
    @Test void ccDoesNotEnterHistoryAndGraphUndoPreservesLatestValues()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var definition=new ValueNode();var id=GLNodeId.random();var graph=c.workspace().currentGraph();
            graph.edit("Value",e->e.addNode(new GLGraphNode(id,definition.registration().id(),definition.defaults())));
            var driver=MidiDrivers.of(c);var address=new MidiDrivers.Address("",id,"value");driver.assign(address,binding(true));
            c.history().reset();send(driver,cc(64));assertEquals(64./127,value(c,id,"value"),1e-9);assertFalse(c.history().canUndo());assertTrue(c.history().isModified());
            var extra=GLNodeId.random();c.history().perform("Add",()->graph.edit("Add",e->e.addNode(new GLGraphNode(extra,definition.registration().id(),definition.defaults()))),()->graph.edit("Remove",e->e.removeNode(extra)));
            send(driver,cc(100));String name=c.history().getUndoName();assertEquals("Add",name);c.history().undo();assertEquals(100./127,value(c,id,"value"),1e-9);assertTrue(c.history().canRedo());
            // A snapshot-based graph undo must also preserve the live parameter.
            var before=graph.snapshot();c.history().perform("Snapshot",()->graph.edit("Add",e->e.addNode(new GLGraphNode(extra,definition.registration().id(),definition.defaults()))),()->graph.replace("Restore snapshot",before));
            send(driver,cc(30));c.history().undo();assertEquals(30./127,value(c,id,"value"),1e-9);
            // A normal inspector-originated edit supersedes the external overlay.
            graph.edit("Inspector",e->e.setExplicitProperty(id,"value",GLNodeValue.of(.2)));assertEquals(.2,value(c,id,"value"),1e-9);
            driver.assign(address,null);assertNull(driver.binding(address));
        }});
    }
    @Test void connectedInputIgnoresMidiAndResumesOnlyWithANewMessage()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var graph=c.workspace().currentGraph();var osc=new OscillatorNode();var id=GLNodeId.random();var source=GLNodeId.random();
            graph.edit("Nodes",e->{e.addNode(new GLGraphNode(id,osc.registration().id(),osc.defaults()));e.addNode(new GLGraphNode(source,new ValueNode().registration().id(),new ValueNode().defaults()));});
            var driver=MidiDrivers.of(c);var address=new MidiDrivers.Address("",id,"frequency");
            driver.assign(address,new MidiDriverBinding("1","Keyboard",0,7,true,new GLDriverMapping(100,1000,false,false,false),false,64));send(driver,cc(0));assertEquals(100,value(c,id,"frequency"));
            var wire=new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(source,"value-out"),new GLNodeEndpoint(id,"frequency"));graph.edit("Connect",e->e.addConnection(wire));send(driver,cc(127));assertEquals(100,value(c,id,"frequency"));
            graph.edit("Disconnect",e->e.removeConnection(wire.id()));assertNotNull(driver.binding(address));assertEquals(100,value(c,id,"frequency"));send(driver,cc(127));assertEquals(1000,value(c,id,"frequency"));
            var view=c.workspace().editor().getNodes().stream().filter(v->AudioNodeFactory.idOf(v).filter(id::equals).isPresent()).findFirst().orElseThrow();
            assertTrue(view.getContents().stream().filter(GLControl.class::isInstance).map(GLControl.class::cast).anyMatch(GLControl::hasDriverIndicator));
        }});
    }
    @Test void noteCyclesChoicesButNoteOffDoesNotAndAssignmentsRoundTrip()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var d=new OscillatorNode();var graph=c.workspace().currentGraph();var id=GLNodeId.random();graph.edit("Osc",e->e.addNode(new GLGraphNode(id,d.registration().id(),d.defaults())));
            var drivers=MidiDrivers.of(c);var address=new MidiDrivers.Address("",id,"waveform");drivers.assign(address,binding(false));
            var start=graph.snapshot().findNode(id).orElseThrow().properties().get("waveform");send(drivers,note(100));var next=graph.snapshot().findNode(id).orElseThrow().properties().get("waveform");assertNotEquals(start,next);
            send(drivers,note(0));assertEquals(next,graph.snapshot().findNode(id).orElseThrow().properties().get("waveform"));
            var encoded=drivers.binding(address).value();assertEquals(binding(false),MidiDriverBinding.read(encoded));
            var bytes=new java.io.ByteArrayOutputStream();var codec=new KnackDocumentCodec();codec.write(c.snapshotDocument(),bytes);var restored=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
            assertEquals(encoded,restored.root().graph().findNode(id).orElseThrow().properties().get(MidiDriverBinding.key("waveform")));
        }catch(Exception ex){throw new AssertionError(ex);}});
    }
    @Test void learningIgnoresClockAndReleaseAndGroupDriversRemainExecutable()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Group",GroupUnitsTest.document(false));var drivers=MidiDrivers.of(c);
            var address=new MidiDrivers.Address("",GroupUnitsTest.GROUP,GroupUnits.key("Pitch"));assertTrue(drivers.beginLearn(address));
            send(drivers,0xf8,note(0),note(100));assertNull(drivers.binding(address));
            send(drivers,cc(20));assertNotNull(drivers.binding(address));assertTrue(c.audioGraph().startsWith("GRAPH"));
            send(drivers,cc(127));assertEquals(20000,value(c,GroupUnitsTest.GROUP,GroupUnits.key("Pitch")));
            assertTrue(GroupUnitsTest.slider(c).hasDriverIndicator());
        }});
    }
    @Test void gateAndTriggerEdgesAreNotLostByCcCoalescing()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var osc=new OscillatorNode();var id=GLNodeId.random();c.workspace().currentGraph().edit("Osc",e->e.addNode(new GLGraphNode(id,osc.registration().id(),osc.defaults())));
            var drivers=MidiDrivers.of(c);var address=new MidiDrivers.Address("",id,"reset");drivers.assign(address,binding(true));
            int[] count={0};ManualTriggers.receiver(target->count[0]++);
            send(drivers,cc(127),cc(0),cc(127));assertEquals(2,count[0]);
            drivers.assign(address,binding(false));send(drivers,note(100),note(0));assertEquals(3,count[0]);
        }finally{ManualTriggers.receiver(v->{});}});
    }
    @Test @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
    void midiFrameAndSettingsRender()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            var root=new com.vingame.glswing.component.GLContainer(){protected void record(com.vingame.glswing.rendering.GLContext2D c){c.fillRect(0,0,getWidth(),getHeight(),getGLTheme().menuBarStyle().background());}};
            root.setGLTheme(ApplicationTheme.current().theme());root.setBounds(0,0,720,440);
            var slider=GLSlider.fixed("Remote",0,1,.5);slider.setDriverIndicator(true,"easyknack.signal.MIDI");root.addGL(slider);slider.setBounds(20,20,240,30);
            var panel=new MidiDriverSettingsPanel(binding(true),GLDriverTarget.number(()->.5,v->{},0,1,false),v->{});root.addGL(panel);panel.setBounds(280,20,420,400);
            var image=GraphImageExport.renderPixels(root,720,440,720,440,false);
            var color=ApplicationTheme.current().signal(SignalCategory.MIDI);int count=0;
            for(int y=20;y<50;y++)for(int x=20;x<260;x++){var p=new java.awt.Color(image.getRGB(x,y));if(Math.abs(p.getRed()-color.getRed())<20&&Math.abs(p.getGreen()-color.getGreen())<20&&Math.abs(p.getBlue()-color.getBlue())<20)count++;}
            assertTrue(count>50,"Driver border should use MIDI theme color");
            try{javax.imageio.ImageIO.write(image,"png",new java.io.File("target/midi-driver-ui.png"));}catch(java.io.IOException e){throw new AssertionError(e);}
        });
    }
    @Test void pickupAndNoteHoldRespectManualValues()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var id=GLNodeId.random();var d=new ValueNode();c.workspace().currentGraph().edit("Value",e->e.addNode(new GLGraphNode(id,d.registration().id(),d.defaults())));
            var drivers=MidiDrivers.of(c);var a=new MidiDrivers.Address("",id,"value");c.setNodeProperty(id,ValueNode.VALUE,GLNodeValue.of(.5));
            drivers.assign(a,new MidiDriverBinding("1","Keyboard",0,7,true,new GLDriverMapping(0,1,false,false,true),false,64));
            send(drivers,cc(0));assertEquals(.5,value(c,id,"value"));send(drivers,cc(127));assertEquals(1,value(c,id,"value"));
            c.setNodeProperty(id,ValueNode.VALUE,GLNodeValue.of(.5));send(drivers,cc(0));assertEquals(.5,value(c,id,"value"));
            var gate=new MidiDrivers.Address("",id,"use-minimum");drivers.assign(gate,new MidiDriverBinding("1","Keyboard",0,7,false,new GLDriverMapping(0,1,false,false,false),true,64));
            send(drivers,note(100));assertEquals(true,c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("use-minimum").toJavaValue());
            send(drivers,note(0));assertEquals(false,c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("use-minimum").toJavaValue());
        }});
    }
}
