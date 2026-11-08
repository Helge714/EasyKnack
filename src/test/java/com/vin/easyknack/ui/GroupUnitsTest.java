package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

class GroupUnitsTest {
    static final GLNodeId OSC=GLNodeId.random(),GROUP=GLNodeId.random();
    static GLCompositePortPresentation slider(){return new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,Map.of(
        "minimum",GLNodeValue.of(0),"maximum",GLNodeValue.of(20000),"default",GLNodeValue.of(440.0),
        GroupUnits.BASE,GLNodeValue.of("hz"),GroupUnits.ALLOWED,GLNodeValue.of(List.of("hz","khz"))));}
    static GLNodeGraphDocument document(boolean nested){
        var osc=new OscillatorNode();var inner=new GLNodeGraph();inner.edit("Osc",e->e.addNode(new GLGraphNode(OSC,osc.registration().id(),osc.defaults())));
        var d=new GLCompositeNodeDescriptor("test.group","test.asset",1,List.of(
            new GLCompositePortMapping(GLNodePortDescriptor.input("Pitch",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(OSC,"frequency"),slider()),
            new GLCompositePortMapping(GLNodePortDescriptor.output("Out",SignalCategory.AUDIO.valueTypeId()),new GLNodeEndpoint(OSC,"audio-out"))));
        var asset=new GLNodeGraphAsset("test.asset",1,GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),GLNodeEditorViewState.empty(),Map.of("easyknack.kind",GLNodeValue.of("AUDIO")),d);
        var assets=new LinkedHashMap<String,GLNodeGraphAsset>();assets.put(asset.id(),asset);String type="test.group";
        if(nested){
            var outer=new GLNodeGraph();var child=GLNodeId.random();outer.edit("Inner",e->e.addNode(new GLGraphNode(child,"test.group",Map.of(GroupUnits.unitKey("Pitch"),GLNodeValue.of("khz")))));
            var descriptor=new GLCompositeNodeDescriptor("test.outer","test.outer.asset",1,List.of(
                new GLCompositePortMapping(GLNodePortDescriptor.input("Pitch",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(child,"Pitch"),slider()),
                new GLCompositePortMapping(GLNodePortDescriptor.output("Out",SignalCategory.AUDIO.valueTypeId()),new GLNodeEndpoint(child,"Out"))));
            assets.put("test.outer.asset",new GLNodeGraphAsset("test.outer.asset",1,GLNodeGraphAssetKind.COMPOSITE,outer.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor));type="test.outer";
        }
        var root=new GLNodeGraph();String groupType=type;var out=GLNodeId.random();root.edit("Group",e->{e.addNode(new GLGraphNode(GROUP,groupType,Map.of()));e.addNode(new GLGraphNode(out,new OutputNode().registration().id(),new OutputNode().defaults()));e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(GROUP,"Out"),new GLNodeEndpoint(out,"audio-in")));});
        return new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),assets,List.of());
    }
    static GLNode group(WorkspaceController c){return c.workspace().editor().getNodes().stream().filter(n->n instanceof GroupViewFactory.GroupView).findFirst().orElseThrow();}
    static GLSlider slider(WorkspaceController c){return group(c).getContents().stream().filter(GLSlider.class::isInstance).map(GLSlider.class::cast).findFirst().orElseThrow();}
    @Test void localAndConnectedUnitsRoundTripAndPreserveTheInternalHzContract()throws Exception{
        SwingUtilities.invokeAndWait(()->{for(boolean nested:new boolean[]{false,true})try(var c=new WorkspaceController();var reopened=new WorkspaceController()){
            c.openDocument("Group units",document(nested));group(c).setLocation(321,234);c.workspace().editor().setSelectedNode(group(c));assertEquals(440,slider(c).getValue());assertEquals("Hz",slider(c).getUnit());
            slider(c).getContextMenu().getItems().stream().filter(i->i.getText().equals("Unit")).findFirst().orElseThrow().getSubMenu().getItems().stream().filter(i->i.getText().equals("Kilohertz")).findFirst().orElseThrow().doClick();
            assertEquals(321,group(c).getX());assertEquals(234,group(c).getY());assertTrue(c.workspace().editor().getSelectedNodes().contains(group(c)));
            assertEquals(.44,slider(c).getValue(),1e-9);assertEquals("kHz",slider(c).getUnit());
            assertTrue(c.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains(" unitconvert -1 0.44 khz "),c.audioGraph());
            var same=slider(c);slider(c).setValue(.88);assertSame(same,slider(c));
            assertEquals(880.0,((Number)c.workspace().currentGraph().snapshot().findNode(GROUP).orElseThrow().properties().get(GroupUnits.key("Pitch")).toJavaValue()).doubleValue(),1e-8);
            c.history().undo();assertEquals(.44,slider(c).getValue(),1e-9);
            var value=GLNodeId.random();c.workspace().currentGraph().edit("Source",e->{e.addNode(new GLGraphNode(value,new ValueNode().registration().id(),new ValueNode().defaults()));e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(value,"value-out"),new GLNodeEndpoint(GROUP,"Pitch")));});
            String encoded=c.audioGraph().replaceAll(" ![0-9a-f]{16} "," ");assertTrue(encoded.contains("khz hz"),encoded);assertFalse(encoded.contains("unitconvert -1 0.44 khz"),encoded);
            var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(c.snapshotDocument(),bytes);reopened.openDocument("Again",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));assertEquals(encoded,reopened.audioGraph().replaceAll(" ![0-9a-f]{16} "," "));
        }catch(java.io.IOException e){throw new AssertionError(e);}});
    }
    @Test void editorCompatibilityAndGroupTypeSettingsAreApplicationOwned()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Group",document(false));var ui=c.workspace().interfaceEditor();
            for(var type:List.of(SignalCategory.AUDIO,SignalCategory.VALUE,SignalCategory.WAVETABLE))assertTrue(ui.inputEditors().compatibleWith(type.valueTypeId()).stream().anyMatch(e->e.id().equals(GLNodeInputEditorRegistry.SLIDER)));
            assertTrue(ui.inputEditors().compatibleWith(SignalCategory.MIDI.valueTypeId()).stream().noneMatch(e->e.id().equals(GLNodeInputEditorRegistry.SLIDER)));
            assertTrue(ui.valueTypes().entries().stream().noneMatch(t->t.displayName().equals("Wellenform")||t.id().equals("easyknack.waveform")));
            assertTrue(c.workspace().openComposite("test.asset"));
            ui.selectSection(0);
            var kind=(GLComboBox<NodeKind>)GroupEditingTest.descendants(ui).filter(n->n instanceof GLComboBox<?> box&&box.getItems().contains(NodeKind.AUDIO)).findFirst().orElseThrow();
            kind.setSelectedItem(NodeKind.MIDI);
            assertEquals("MIDI",c.snapshotDocument().embeddedAssets().get("test.asset").metadata().get("easyknack.kind").toJavaValue());
            c.history().undo();assertEquals(NodeKind.AUDIO,kind.getSelectedItem());
        }});
    }
    @Test @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeHostAcceptsNestedUnitAdapters()throws Exception{
        var command=new java.util.concurrent.atomic.AtomicReference<String>();
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){c.openDocument("Nested",document(true));c.setNodeProperty(GROUP,new PropertyId(GroupUnits.unitKey("Pitch")),GLNodeValue.of("khz"));command.set(c.audioGraph());}});
        try(var host=new com.vin.easyknack.audio.AudioHostClient()){host.start(com.vin.easyknack.audio.AudioHostClient.defaultExecutable(),true);assertTrue(host.command(command.get()).startsWith("OK"));}
    }
    @Test void catalogCategoryAndTitleKindAreIndependent(){
        assertEquals(NodeCategory.ROUTING,new ChannelSeparateNode().registration().category());assertEquals(NodeKind.AUDIO,new ChannelSeparateNode().registration().kind());
        assertEquals(NodeCategory.MIDI,new MidiNoteNode().registration().category());assertEquals(NodeKind.MIDI,new MidiNoteNode().registration().kind());
        assertTrue(new MixNode().layout(new NodeState(new MixNode().defaults())).rows().stream().filter(r->r.kind()!=NodeBuilder.RowKind.EDITOR).allMatch(r->r.signal()==SignalCategory.VALUE));
    }
}
