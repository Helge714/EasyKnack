package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;

class EventRateTest {
    private static GLNode view(WorkspaceController controller,GLNodeId id){return controller.workspace().editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();}
    @Test void legacyNumericEventMigratesToAudioAndSurvivesUndoCopyAndSave() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try(var controller=new WorkspaceController();var reopened=new WorkspaceController()){
                var workspace=controller.workspace();var graph=workspace.currentGraph();
                var source=graph.snapshot().nodes().get(0).id();var id=GLNodeId.random();var math=new MathNode();
                graph.edit("Event math",e->e.addNode(new GLGraphNode(id,math.registration().id(),math.defaults())));
                controller.setNodeProperty(id,MathNode.RATE,GLNodeValue.of("event"));
                workspace.editor().connect(view(controller,source).findPort("audio-out"),view(controller,id).findPort("a"));
                assertEquals(GLNodePort.Shape.CIRCLE,view(controller,id).findPort("a").getShape());
                assertFalse(view(controller,id).findPort("a").getToolTipText().contains("AudioRate source connected"));
                assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("@ a aa"));
                assertEquals("audio",graph.snapshot().findNode(id).orElseThrow().properties().get("rate").toJavaValue());
                controller.setNodeProperty(id,MathNode.RATE,GLNodeValue.of("control"));
                assertEquals(GLNodePort.Shape.DIAMOND,view(controller,id).findPort("a").getShape());
                controller.history().undo();assertEquals(GLNodePort.Shape.CIRCLE,view(controller,id).findPort("a").getShape());
                var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(controller.snapshotDocument(),bytes);
                reopened.openDocument("Event",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
                assertEquals(controller.audioGraph(),reopened.audioGraph());
                workspace.editor().setSelectedNode(view(controller,id));controller.actions().copy();controller.actions().paste(1200,350);
                assertTrue(controller.audioGraph().contains("@ a aa"));
                controller.actions().extractComposite();assertTrue(controller.audioGraph().contains("@ a aa"));
                controller.setNodeProperty(id,MathNode.RATE,GLNodeValue.of("audio"));
                assertFalse(view(controller,id).findPort("a").getToolTipText().contains("AudioRate source connected"));
            }catch(java.io.IOException error){throw new AssertionError(error);}
        });
    }
    @Test void midiPortsAndConverterOutputsAreEventsButOnlySupportedNodesOfferEventEvaluation() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            for(BasicNode node:List.of(new MidiInputNode(),new MidiTransposeNode(),new MidiDelayNode(),new MidiNoteNode(),new MidiCCNode())){
                var layout=node.layout(new NodeState(node.defaults()));
                for(var row:layout.rows())if(row.kind()!=NodeBuilder.RowKind.EDITOR)assertEquals(row.dataType()==PortDataType.MIDI||row.dataType()==PortDataType.TRIGGER?ProcessingRate.EVENT:ProcessingRate.AUDIO,row.rate());
            }
            var registry=new NodeRegistry(new GLNodeTypeRegistry(),new GLNodeCatalog());
            for(BasicNode node:List.of(new MathNode(),new MapRangeNode())){
                var rate=(GLEnumNodeProperty)registry.schemas().properties(node.registration().id()).stream().filter(p->p.id().equals("rate")).findFirst().orElseThrow();
                assertEquals(List.of("auto","audio","control"),rate.values());assertTrue(rate.toolTipText().contains("individual rates"));
                var values=new HashMap<>(node.defaults());values.put("rate",GLNodeValue.of("event"));
                var model=new GLGraphNode(GLNodeId.random(),node.registration().id(),values);
                var nodeView=node.createView(new NodeViewContext((id,k,v)->{},new com.vingame.glswing.history.GLHistoryManager()),model);
                for(var port:nodeView.getPorts())assertEquals(GLNodePort.Shape.CIRCLE,port.getShape());
            }
            var curve=(GLEnumNodeProperty)registry.schemas().properties(new CurveNode().registration().id()).stream().filter(p->p.id().equals("rate")).findFirst().orElseThrow();
            assertEquals(List.of("auto","audio","control"),curve.values());
            var group=new GLNode("MIDI group");group.addOutput("midi",new com.vingame.glswing.ui.GLLabel("MIDI"));
            CompositePortColors.apply(group,new com.vingame.glswing.node.composite.GLCompositeNodeDescriptor("test.group","test.asset",1,List.of(new com.vingame.glswing.node.composite.GLCompositePortMapping(GLNodePortDescriptor.output("midi",SignalCategory.MIDI.valueTypeId()),null))));
            assertEquals(GLNodePort.Shape.DOUBLE_DIAMOND,group.findPort("midi").getShape());
        });
    }
}
