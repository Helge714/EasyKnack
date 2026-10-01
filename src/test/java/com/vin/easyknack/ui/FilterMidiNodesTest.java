package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

class FilterMidiNodesTest {
    @Test @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void playableMidiExampleCompilesOnTheRealHost() throws Exception {
        var command=new java.util.concurrent.atomic.AtomicReference<String>();
        SwingUtilities.invokeAndWait(()->{
            try(var controller=new WorkspaceController()){
                var graph=controller.workspace().currentGraph();
                List<BasicNode> definitions=List.of(new MidiInputNode(),new MidiNoteNode(),new OscillatorNode(),new FilterNode(),new EnvelopeNode(),new MathNode(),new OutputNode());
                var ids=definitions.stream().map(n->GLNodeId.random()).toList();
                graph.edit("Monophonic MIDI synth",e->{
                    for(var n:graph.snapshot().nodes())e.removeNode(n.id());
                    for(int i=0;i<ids.size();i++)e.addNode(new GLGraphNode(ids.get(i),definitions.get(i).registration().id(),definitions.get(i).defaults()));
                    e.setProperty(ids.get(2),OscillatorNode.WAVEFORM.value(),GLNodeValue.of("saw"));
                    e.setProperty(ids.get(3),FilterNode.CUTOFF.value(),GLNodeValue.of(2000));
                    e.setProperty(ids.get(4),EnvelopeNode.DURATION.value(),GLNodeValue.of(80));
                    connect(e,ids.get(0),MidiInputNode.OUT,ids.get(1),MidiNoteNode.IN);
                    connect(e,ids.get(1),MidiNoteNode.FREQUENCY,ids.get(2),OscillatorNode.FREQUENCY);
                    connect(e,ids.get(1),MidiNoteNode.GATE,ids.get(4),EnvelopeNode.GATE);
                    connect(e,ids.get(1),MidiNoteNode.TRIGGER,ids.get(4),EnvelopeNode.TRIGGER);
                    connect(e,ids.get(2),OscillatorNode.OUT,ids.get(3),FilterNode.IN);
                    connect(e,ids.get(3),FilterNode.OUT,ids.get(5),MathNode.A);
                    connect(e,ids.get(4),EnvelopeNode.OUT,ids.get(5),MathNode.B);
                    connect(e,ids.get(5),MathNode.OUT,ids.get(6),OutputNode.IN);
                });
                float[][] positions={{40,100},{250,100},{480,40},{710,40},{480,340},{930,120},{1160,120}};
                for(var view:controller.workspace().editor().getNodes()){
                    int index=ids.indexOf(AudioNodeFactory.idOf(view).orElse(null));
                    if(index>=0)view.setLocation(positions[index][0],positions[index][1]);
                }
                command.set(controller.audioGraph());
                try(var out=java.nio.file.Files.newOutputStream(java.nio.file.Path.of("target/mono-midi.knack"))){new KnackDocumentCodec().write(controller.snapshotDocument(),out);}
                catch(java.io.IOException error){throw new AssertionError(error);}
            }
        });
        try(var client=new AudioHostClient()){
            client.start(AudioHostClient.defaultExecutable(),true);
            assertEquals("OK",client.command(command.get()));assertEquals("OK",client.command("PLAY"));
        }
    }
    @Test void midiOutputsKeepPortIndicesAndDocumentsKeepDeviceReferences() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            var previous=MidiDevices.current();
            try(var controller=new WorkspaceController();var reopened=new WorkspaceController()){
                var graph=controller.workspace().currentGraph();
                List<BasicNode> definitions=List.of(new MidiInputNode(),new MidiNoteNode(),new OscillatorNode(),new EnvelopeNode(),new FilterNode(),new MidiCCNode());
                var ids=definitions.stream().map(n->GLNodeId.random()).toList();
                graph.edit("MIDI patch",e->{
                    for(var n:graph.snapshot().nodes())e.removeNode(n.id());
                    for(int i=0;i<ids.size();i++)e.addNode(new GLGraphNode(ids.get(i),definitions.get(i).registration().id(),definitions.get(i).defaults()));
                    connect(e,ids.get(0),MidiInputNode.OUT,ids.get(1),MidiNoteNode.IN);
                    connect(e,ids.get(0),MidiInputNode.OUT,ids.get(5),MidiCCNode.IN);
                    connect(e,ids.get(1),MidiNoteNode.FREQUENCY,ids.get(2),OscillatorNode.FREQUENCY);
                    connect(e,ids.get(1),MidiNoteNode.GATE,ids.get(3),EnvelopeNode.GATE);
                    connect(e,ids.get(1),MidiNoteNode.TRIGGER,ids.get(3),EnvelopeNode.TRIGGER);
                    connect(e,ids.get(2),OscillatorNode.OUT,ids.get(4),FilterNode.IN);
                });
                String encoded=controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ");
                assertTrue(encoded.contains("osc 1 440.0"));assertTrue(encoded.contains("envelope 257 0.0 513 0.0"));
                assertEquals(4,new MidiNoteNode().layout(new NodeState(new MidiNoteNode().defaults())).ports().stream().filter(p->p.direction()==GLPortDirection.OUTPUT).count());
                var endpoint=new MidiDevices.Device("123456789","Test MIDI",true);MidiDevices.publish(List.of(MidiDevices.NONE,endpoint));
                controller.setNodeProperty(ids.get(0),MidiInputNode.DEVICE,GLNodeValue.of(endpoint.reference()));
                var node=controller.workspace().editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(ids.get(0)::equals).isPresent()).findFirst().orElseThrow();
                var combo=(GLComboBox<?>)node.getContents().stream().filter(GLComboBox.class::isInstance).findFirst().orElseThrow();
                assertEquals("Test MIDI",combo.getSelectedItem().toString());
                var before=controller.snapshotDocument();MidiDevices.publish(List.of(MidiDevices.NONE));
                assertTrue(combo.getSelectedItem().toString().contains("unavailable"));assertEquals(before,controller.snapshotDocument());
                try{var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(before,bytes);
                    reopened.openDocument("MIDI",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
                    assertEquals(controller.audioGraph(),reopened.audioGraph());
                }catch(java.io.IOException error){throw new AssertionError(error);}
                graph.edit("Mute filter",e->e.setProperty(ids.get(4),GLNodeState.MUTED,GLNodeValue.of(true)));
                assertFalse(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains(" filter "));
            }finally{MidiDevices.publish(previous);}
        });
    }
    private static void connect(GLNodeGraph.Editor e,GLNodeId from,PropertyId output,GLNodeId to,PropertyId input){
        e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(from,output.value()),new GLNodeEndpoint(to,input.value())));
    }
    @Test void resizePolicyAndCurveGrowthLeaveOtherRowsUnchanged() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            for(BasicNode definition:NodeRegistry.definitionsList()){
                var view=definition.createView(new NodeViewContext((id,key,value)->{},new com.vingame.glswing.history.GLHistoryManager()),
                        new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults()));
                view.packGL();boolean curve=definition instanceof CurveNode||definition instanceof EnvelopeNode;
                assertEquals(curve||definition instanceof OscilloscopeNode||definition instanceof AutocorrelationNode||definition instanceof AnalysisViewerNode||definition instanceof SpectrumViewerNode?GLNodeResizeMode.BOTH:GLNodeResizeMode.HORIZONTAL,view.getResizeMode());
                if(curve){
                    var editor=view.getContents().stream().filter(GLCurveEditor.class::isInstance).map(GLCurveEditor.class::cast).findFirst().orElseThrow();
                    float initial=editor.getHeight(),width=editor.getWidth();
                    var rows=view.getContents().stream().filter(c->c!=editor).map(c->c.getHeight()).toList();
                    view.setSize(view.getWidth()+80,view.getHeight()+140);
                    assertEquals(initial+140,editor.getHeight(),.01);assertEquals(width+80,editor.getWidth(),.01);
                    assertEquals(rows,view.getContents().stream().filter(c->c!=editor).map(c->c.getHeight()).toList());
                    float expandedHeight=view.getHeight();view.setContentHidden(true);assertEquals(GLNodeResizeMode.HORIZONTAL,view.getResizeMode());
                    view.setContentHidden(false);assertEquals(GLNodeResizeMode.BOTH,view.getResizeMode());assertEquals(expandedHeight,view.getHeight(),.01);
                    assertEquals(initial+140,editor.getHeight(),.01);
                }
            }
        });
    }
    @Test void midiDiscoveryRejectsMalformedAndDuplicateDevices(){
        assertEquals(List.of(MidiDevices.KEYBOARD,MidiDevices.NONE),MidiDevices.parse("OK 0"));
        assertEquals("Keys",MidiDevices.parse("OK 1 123 4b657973").get(2).name());
        assertThrows(IllegalArgumentException.class,()->MidiDevices.parse("OK 2 123 41 123 42"));
        assertThrows(IllegalArgumentException.class,()->MidiDevices.parse("OK 1 -1 41"));
        assertThrows(IllegalArgumentException.class,()->MidiDevices.parse("OK 1 18446744073709551616 41"));
    }
}
