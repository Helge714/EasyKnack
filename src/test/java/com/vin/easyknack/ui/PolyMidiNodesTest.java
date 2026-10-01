package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.*;
import com.vingame.glswing.input.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

class PolyMidiNodesTest {
    private static GLKeyEvent key(int id,int code,int modifiers){return new GLKeyEvent(new KeyEvent(new Canvas(),id,1,modifiers,code,KeyEvent.CHAR_UNDEFINED));}
    @Test void keyboardDebouncesReleasesOnFocusLossAndKeepsItsPressedPitch() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            MidiDevices.drainKeyboard();var keyboard=new KeyboardMidiEditor.Keyboard();keyboard.focusGained();
            var press=key(KeyEvent.KEY_PRESSED,KeyEvent.VK_A,0);keyboard.keyPressed(press);keyboard.keyPressed(press);
            keyboard.keyPressed(key(KeyEvent.KEY_PRESSED,KeyEvent.VK_S,0));
            assertTrue(press.isConsumed());assertEquals(List.of(0x90|(60<<8)|(100<<16),0x90|(62<<8)|(100<<16)),MidiDevices.drainKeyboard());
            keyboard.focusLost();assertEquals(List.of(0x80|(60<<8),0x80|(62<<8)),MidiDevices.drainKeyboard());
            keyboard.focusGained();keyboard.setTranspose(12);keyboard.keyPressed(key(KeyEvent.KEY_PRESSED,KeyEvent.VK_A,0));
            assertEquals(List.of(0x90|(72<<8)|(100<<16)),MidiDevices.drainKeyboard());
            keyboard.setTranspose(-12);assertEquals(List.of(0x80|(72<<8)),MidiDevices.drainKeyboard());
            keyboard.keyReleased(key(KeyEvent.KEY_RELEASED,KeyEvent.VK_A,0));assertTrue(MidiDevices.drainKeyboard().isEmpty());
            keyboard.keyPressed(key(KeyEvent.KEY_PRESSED,KeyEvent.VK_S,KeyEvent.CTRL_DOWN_MASK));assertTrue(MidiDevices.drainKeyboard().isEmpty());
            keyboard.focusLost();
        });
    }
    @Test void exclusiveKeyboardFocusBypassesAncestorCaptureInBothCoreBackends() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            class CapturingParent extends com.vingame.glswing.component.GLContainer implements GLKeyPressCaptureTarget {
                boolean captured;
                public boolean capturesKeyPress(GLKeyEvent e){return true;}
                public void keyPressed(GLKeyEvent e){captured=true;e.consume();}
                @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
            }
            var parent=new CapturingParent();var keyboard=new KeyboardMidiEditor.Keyboard();parent.addGL(keyboard);
            for(String backend:List.of("GLContainerInputDispatcher","GLSwingPanel"))try{
                var method=Class.forName("com.vingame.glswing.component."+backend).getDeclaredMethod("dispatchKeyPressed",GLKeyTarget.class,GLKeyEvent.class);method.setAccessible(true);
                MidiDevices.drainKeyboard();var event=key(KeyEvent.KEY_PRESSED,KeyEvent.VK_A,0);method.invoke(null,keyboard,event);
                assertFalse(parent.captured);assertTrue(event.isConsumed());assertEquals(1,MidiDevices.drainKeyboard().size());
                keyboard.keyReleased(key(KeyEvent.KEY_RELEASED,KeyEvent.VK_A,0));
            }catch(ReflectiveOperationException e){throw new AssertionError(e);}
            MidiDevices.drainKeyboard();
        });
    }
    @Test void tooltipsDescribeMidiRangesAndDeviceSwitchKeepsRuntimeControls() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            var definition=new MidiNoteNode();var view=definition.createView(new NodeViewContext((id,k,v)->{},new com.vingame.glswing.history.GLHistoryManager()),new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults()));
            var tips=view.getContents().stream().filter(GLControl.class::isInstance).map(GLControl.class::cast).map(GLControl::getToolTipText).filter(Objects::nonNull).toList();
            assertTrue(tips.stream().anyMatch(t->t.contains("One sample")));assertTrue(tips.stream().anyMatch(t->t.contains("0 = released")));assertTrue(tips.stream().anyMatch(t->t.contains("Range: 1.0 to 32.0")));
            var input=new MidiInputNode();var model=new GLGraphNode(GLNodeId.random(),input.registration().id(),input.defaults());
            assertEquals(MidiDevices.KEYBOARD.reference(),model.properties().get("device").toJavaValue());
            var inputView=input.createView(new NodeViewContext((id,k,v)->{},new com.vingame.glswing.history.GLHistoryManager()),model);
            var keyboard=inputView.getContents().stream().filter(KeyboardMidiEditor.Keyboard.class::isInstance).map(KeyboardMidiEditor.Keyboard.class::cast).findFirst().orElseThrow();
            assertTrue(keyboard.isEnabled());var properties=new HashMap<>(model.properties());properties.put("device",GLNodeValue.of(MidiDevices.NONE.reference()));inputView.update(new GLGraphNode(model.id(),model.typeId(),properties));assertFalse(keyboard.isEnabled());
        });
    }
    @Test @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void polyphonicKeyboardExampleRoundTripsAndRunsOnNativeHost() throws Exception {
        var command=new java.util.concurrent.atomic.AtomicReference<String>();
        var nestedCommand=new java.util.concurrent.atomic.AtomicReference<String>();
        SwingUtilities.invokeAndWait(()->{
            try(var controller=new WorkspaceController();var reopened=new WorkspaceController()){
                var graph=controller.workspace().currentGraph();
                List<BasicNode> defs=List.of(new MidiInputNode(),new MidiTransposeNode(),new MidiDelayNode(),new MidiNoteNode(),new OscillatorNode(),new EnvelopeNode(),new MathNode(),new VoiceMergeNode(),new FilterNode(),new OutputNode());
                var ids=defs.stream().map(n->GLNodeId.random()).toList();
                graph.edit("Polyphonic keyboard",e->{
                    for(var n:graph.snapshot().nodes())e.removeNode(n.id());
                    for(int i=0;i<ids.size();i++)e.addNode(new GLGraphNode(ids.get(i),defs.get(i).registration().id(),defs.get(i).defaults()));
                    e.setProperty(ids.get(2),MidiDelayNode.VALUE.value(),GLNodeValue.of(0));
                    e.setProperty(ids.get(3),MidiNoteNode.VOICES.value(),GLNodeValue.of(8));
                    e.setProperty(ids.get(4),OscillatorNode.WAVEFORM.value(),GLNodeValue.of("saw"));
                    connect(e,ids,0,MidiInputNode.OUT,1,MidiTransposeNode.IN);connect(e,ids,1,MidiTransposeNode.OUT,2,MidiDelayNode.IN);connect(e,ids,2,MidiDelayNode.OUT,3,MidiNoteNode.IN);
                    connect(e,ids,3,MidiNoteNode.FREQUENCY,4,OscillatorNode.FREQUENCY);connect(e,ids,3,MidiNoteNode.GATE,5,EnvelopeNode.GATE);connect(e,ids,3,MidiNoteNode.TRIGGER,5,EnvelopeNode.TRIGGER);
                    connect(e,ids,4,OscillatorNode.OUT,6,MathNode.A);connect(e,ids,5,EnvelopeNode.OUT,6,MathNode.B);connect(e,ids,6,MathNode.OUT,7,VoiceMergeNode.IN);connect(e,ids,7,VoiceMergeNode.OUT,8,FilterNode.IN);connect(e,ids,8,FilterNode.OUT,9,OutputNode.IN);
                });
                float[][] positions={{20,40},{260,40},{500,40},{740,40},{980,20},{980,300},{1220,100},{1460,100},{1700,100},{1940,100}};
                for(var view:controller.workspace().editor().getNodes()){int i=ids.indexOf(AudioNodeFactory.idOf(view).orElse(null));if(i>=0)view.setLocation(positions[i][0],positions[i][1]);}
                var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(controller.snapshotDocument(),bytes);
                reopened.openDocument("Polyphonic keyboard",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
                command.set(controller.audioGraph());assertEquals(command.get(),reopened.audioGraph());
                java.nio.file.Files.write(java.nio.file.Path.of("target/poly-midi.knack"),bytes.toByteArray());
                controller.workspace().editor().setSelectedNodes(controller.workspace().editor().getNodes().stream().filter(v->AudioNodeFactory.idOf(v).map(id->ids.indexOf(id)>=3&&ids.indexOf(id)<=6).orElse(false)).toList());
                controller.actions().extractComposite();
                // Wrap the voice subgraph again; flattening preserves its voice domain.
                controller.actions().extractComposite();
                nestedCommand.set(controller.audioGraph());
            }catch(java.io.IOException e){throw new AssertionError(e);}
        });
        try(var client=new AudioHostClient()){
            client.start(AudioHostClient.defaultExecutable(),true);assertEquals("OK",client.command(command.get()));assertEquals("OK",client.command("PLAY"));
            assertEquals("OK",client.command(nestedCommand.get()));
            assertEquals("OK",client.command("MIDI_KEYBOARD 2 "+(0x90|(60<<8)|(100<<16))+" "+(0x90|(64<<8)|(100<<16))));
            assertEquals("OK",client.command("MIDI_KEYBOARD 1 255"));assertEquals("OK",client.command("STOP"));
        }
    }
    private static void connect(GLNodeGraph.Editor e,List<GLNodeId> ids,int from,PropertyId output,int to,PropertyId input){e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(ids.get(from),output.value()),new GLNodeEndpoint(ids.get(to),input.value())));}
}
