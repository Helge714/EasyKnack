package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioGraphEncoder;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;

class AutomaticRatesTest {
    static GLNode view(WorkspaceController c,GLNodeId id){return c.workspace().editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();}
    @Test void autoFollowsConnectionsInBothDirectionsAndManualOverrideSurvivesUndoAndSave()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController();var reopened=new WorkspaceController()){
            var graph=c.workspace().currentGraph();
            var ids=List.of(GLNodeId.random(),GLNodeId.random(),GLNodeId.random(),GLNodeId.random());
            List<BasicNode> definitions=List.of(new ContextInputNode(),new MapRangeNode(),new MathNode(),new OscillatorNode());
            graph.edit("Auto chain",e->{
                for(var old:graph.snapshot().nodes())e.removeNode(old.id());
                for(int i=0;i<ids.size();i++)e.addNode(new GLGraphNode(ids.get(i),definitions.get(i).registration().id(),definitions.get(i).defaults()));
            });
            var editor=c.workspace().editor();
            editor.connect(view(c,ids.get(0)).findPort("channel"),view(c,ids.get(1)).findPort("audio-in"));
            editor.connect(view(c,ids.get(1)).findPort("audio-out"),view(c,ids.get(2)).findPort("a"));
            assertEquals(GLNodePort.Shape.DIAMOND,view(c,ids.get(1)).findPort("audio-out").getShape());
            assertTrue(c.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("@ c ccccc"));assertTrue(c.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("@ c cc"));
            var mapView=view(c,ids.get(1));
            var audioWire=editor.connect(view(c,ids.get(3)).findPort("audio-out"),view(c,ids.get(1)).findPort("from-min"));
            assertSame(mapView,view(c,ids.get(1)),"Rate changes must preserve focus, captures and placement history");
            assertEquals(GLNodePort.Shape.CIRCLE,view(c,ids.get(2)).findPort("audio-out").getShape());
            assertEquals("auto",graph.snapshot().findNode(ids.get(1)).orElseThrow().properties().get("rate").toJavaValue());
            editor.setSelectedNodes(List.of());editor.setSelectedConnection(audioWire);
            editor.keyPressed(new com.vingame.glswing.input.GLKeyEvent(new java.awt.event.KeyEvent(new java.awt.Canvas(),
                    java.awt.event.KeyEvent.KEY_PRESSED,System.currentTimeMillis(),0,java.awt.event.KeyEvent.VK_DELETE,java.awt.event.KeyEvent.CHAR_UNDEFINED)));
            assertEquals(GLNodePort.Shape.DIAMOND,view(c,ids.get(2)).findPort("audio-out").getShape());
            c.history().undo();assertEquals(GLNodePort.Shape.CIRCLE,view(c,ids.get(2)).findPort("audio-out").getShape());
            c.history().redo();assertEquals(GLNodePort.Shape.DIAMOND,view(c,ids.get(2)).findPort("audio-out").getShape());
            c.history().undo();
            c.setNodeProperty(ids.get(1),MapRangeNode.RATE,GLNodeValue.of("control"));
            assertTrue(c.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("@ c ccccc"));
            c.setNodeProperty(ids.get(1),MapRangeNode.RATE,GLNodeValue.of("auto"));
            assertTrue(c.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("@ a caccc"));
            var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(c.snapshotDocument(),bytes);
            reopened.openDocument("Auto",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
            assertEquals(c.audioGraph(),reopened.audioGraph());
            c.setNodeProperty(ids.get(1),MapRangeNode.RATE,GLNodeValue.of("event"));
            editor.setSelectedNode(view(c,ids.get(1)));c.actions().extractComposite();
            assertTrue(c.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("@ a ac"));
            assertEquals(GLNodePort.Shape.CIRCLE,view(c,ids.get(2)).findPort("audio-out").getShape());
            c.actions().openSelectedComposite();
            c.setNodeProperty(ids.get(1),MapRangeNode.RATE,GLNodeValue.of("auto"));
            // Grouping the manual-rate node captured explicit Audio boundary rates.
            // Returning its internal processor to Auto must not override that public contract.
            var grouped=c.snapshotDocument().embeddedAssets().values().iterator().next();
            for(var port:grouped.compositeDescriptor().portMappings())if(port.publicPort().direction()==GLPortDirection.INPUT){
                assertEquals(SignalCategory.VALUE.valueTypeId(),port.publicPort().valueTypeId());
                assertEquals(ProcessingRate.AUDIO,GroupInputSettings.selectedRate(port));
            }
            assertTrue(c.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("@ a aaccc"),c.audioGraph());
            assertEquals(GLNodePort.Shape.CIRCLE,view(c,ids.get(1)).findPort("audio-out").getShape());
        }catch(java.io.IOException error){throw new AssertionError(error);}});
    }
    @Test void eventAndAudioPrecedencePropagateAndStaleCachesAreIgnored(){
        var graph=new GLNodeGraph();var source=GLNodeId.random();var math=GLNodeId.random();var map=GLNodeId.random();
        graph.edit("Reverse storage order",e->{
            e.addNode(new GLGraphNode(map,new MapRangeNode().registration().id(),new MapRangeNode().defaults()));
            e.addNode(new GLGraphNode(math,new MathNode().registration().id(),new MathNode().defaults()).withProperty(ProcessingRate.EFFECTIVE,GLNodeValue.of("audio")));
            e.addNode(new GLGraphNode(source,new MidiNoteNode().registration().id(),new MidiNoteNode().defaults()));
            connect(e,source,"frequency",math,"a");connect(e,math,"audio-out",map,"audio-in");
        });
        var encoded=AudioGraphEncoder.encode(graph.snapshot());
        assertTrue(encoded.contains("@ a ac"));assertTrue(encoded.contains("@ a acccc"));
        for(BasicNode n:List.of(new MathNode(),new MapRangeNode(),new MixNode(),new CurveNode(),new UnitConverterNode()))
            assertEquals("auto",n.defaults().get("rate").toJavaValue());
    }
    @Test void channelNodesEncodeCrossedOutputsAndSurviveDocumentRoundTrip()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController();var reopened=new WorkspaceController()){
            var graph=c.workspace().currentGraph();var ids=List.of(GLNodeId.random(),GLNodeId.random(),GLNodeId.random(),GLNodeId.random());
            List<BasicNode> definitions=List.of(new ContextInputNode(),new ChannelSeparateNode(),new ChannelCombineNode(),new OutputNode());
            graph.edit("Swap",e->{
                for(var old:graph.snapshot().nodes())e.removeNode(old.id());
                for(int i=0;i<ids.size();i++)e.addNode(new GLGraphNode(ids.get(i),definitions.get(i).registration().id(),definitions.get(i).defaults()));
                connect(e,ids.get(0),"channel",ids.get(1),"audio-in");
                connect(e,ids.get(1),"right",ids.get(2),"left");connect(e,ids.get(1),"left",ids.get(2),"right");
                connect(e,ids.get(2),"audio-out",ids.get(3),"audio-in");
            });
            assertTrue(c.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("channelcombine 257 1 @ a aa"));
            reopened.openDocument("Swap",c.snapshotDocument());assertEquals(c.audioGraph(),reopened.audioGraph());
        }});
    }
    static void connect(GLNodeGraph.Editor e,GLNodeId from,String out,GLNodeId to,String in){e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(from,out),new GLNodeEndpoint(to,in)));}
}
