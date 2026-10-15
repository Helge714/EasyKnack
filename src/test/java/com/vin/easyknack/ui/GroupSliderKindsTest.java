package com.vin.easyknack.ui;

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

class GroupSliderKindsTest {
    @Test void copiedIntegerSliderRetainsStepAndLegacySnapshotsRemainUsable()throws Exception {
        SwingUtilities.invokeAndWait(()->{
            var def=new AdditiveOscillatorNode();var node=new GLGraphNode(GLNodeId.random(),def.registration().id(),def.defaults());
            var row=def.layout(new NodeState(node.properties())).rows().stream().filter(r->r.editor() instanceof Editor.Slider s&&s.integral()||r.editor() instanceof UnitSlider u&&u.spec().integral()).findFirst().orElseThrow();
            var snapshot=GroupEditorCapabilities.snapshot(row,node);var options=new HashMap<>(snapshot.capabilities().options());
            assertTrue(new NodeState(options).number("step",0)>=1);
            for(boolean legacy:List.of(false,true)){
                if(legacy)options.remove("step");
                var p=new GLCompositePortMapping(GLNodePortDescriptor.input("Count",SignalCategory.VALUE.valueTypeId(),false,1),null,new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,options));
                var d=new GLCompositeNodeDescriptor("test.integer","test.integer.asset",1,List.of(p));
                var a=new GLNodeGraphAsset(d.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,new GLNodeGraph().snapshot(),GLNodeEditorViewState.empty(),Map.of(),d);
                var registry=new GLCompositeGraphRegistry();registry.registerAsset(a);
                var values=new ArrayList<GLNodeValue>();var factory=new GroupViewFactory(registry,new GLNodeCatalog(),(id,key,v)->values.add(v),new com.vingame.glswing.history.GLHistoryManager(),()->{});
                var view=factory.createNode(new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of()),d.nodeType());
                var slider=(GLUnitSlider<?>)view.getContents().stream().filter(GLSlider.class::isInstance).findFirst().orElseThrow();
                double before=slider.getStoredValue();assertTrue(slider.getStep()>=1);
                slider.increment();
                assertEquals(before+slider.getStep(),slider.getStoredValue());assertFalse(values.isEmpty());
                slider.setPresentation(GLSlider.Presentation.BUTTONS);slider.setBounds(0,0,160,28);
                before=slider.getStoredValue();
                slider.mousePressed(mouse(slider,java.awt.event.MouseEvent.MOUSE_PRESSED,0));
                slider.mouseDragged(mouse(slider,java.awt.event.MouseEvent.MOUSE_DRAGGED,8));
                slider.mouseReleased(mouse(slider,java.awt.event.MouseEvent.MOUSE_RELEASED,0));
                assertTrue(slider.getStoredValue()>before,"Integer button-slider must respond to dragging");
            }
        });
    }
    private static com.vingame.glswing.input.GLMouseEvent mouse(GLSlider slider,int kind,float dx){
        float x=80,y=14;
        return new com.vingame.glswing.input.GLMouseEvent(new java.awt.event.MouseEvent(new javax.swing.JPanel(),kind,0,0,(int)x,(int)y,1,false,java.awt.event.MouseEvent.BUTTON1),new com.vingame.glswing.input.GLPickResult(0,slider,x,y,x,y,1,0,0,1,0,0),dx,0);
    }
    @Test void integerSettingRoundsDefaultAndRejectsEmptyIntegralRange(){
        var options=Map.of("default",GLNodeValue.of(2.4),"minimum",GLNodeValue.of(1.2),"maximum",GLNodeValue.of(5.));
        var integers=GroupInputSettings.integerOptions(options,true);assertEquals(2.,new NodeState(integers).number("default",0));assertEquals(1.,new NodeState(integers).number("step",0));
        assertEquals("float",new NodeState(GroupInputSettings.integerOptions(integers,false)).text("numericType",""));
        assertThrows(IllegalArgumentException.class,()->GroupInputSettings.integerOptions(Map.of("minimum",GLNodeValue.of(.2),"maximum",GLNodeValue.of(.8)),true));
    }
    @Test void newGroupsInheritFirstOutputAndKeepManualKind()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var def=new ValueNode();var id=GLNodeId.random();c.workspace().currentGraph().edit("Value",e->e.addNode(new GLGraphNode(id,def.registration().id(),def.defaults())));
            var view=c.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView b&&b.nodeId().equals(id)).findFirst().orElseThrow();
            c.workspace().editor().setSelectedNode(view);c.actions().extractComposite();
            var asset=c.snapshotDocument().embeddedAssets().values().stream().filter(a->a.graph().findNode(id).isPresent()).findFirst().orElseThrow();
            assertEquals("VALUE",new NodeState(asset.metadata()).text("easyknack.kind",""));
            var metadata=new HashMap<>(asset.metadata());metadata.put("easyknack.kind",GLNodeValue.of("TRIGGER"));
            var changed=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),metadata,asset.compositeDescriptor());
            assertSame(changed,GroupKinds.initialize(changed));
        }});
    }
    @Test void everySignalHasAMatchingNodePalette(){
        var theme=ApplicationTheme.load(null);
        for(var signal:SignalCategory.values()){
            var kind=NodeKind.fromPort(signal.valueTypeId());assertEquals(signal,kind.signal());
            assertEquals(theme.signal(signal),theme.title(kind).light());assertNotNull(theme.title(kind).dark());
        }
        assertEquals(NodeKind.TRIGGER,new TriggerInputNode().registration().kind());
    }
}
