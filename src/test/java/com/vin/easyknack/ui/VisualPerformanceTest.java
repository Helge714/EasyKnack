package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

class VisualPerformanceTest {
    @Test void historyRevisionChangesOnlyWhenItsDataOrSettingsChange(){
        var history=new ValueHistory();var packet=ValueHistoryTest.packet(20,10,0,9,1,2,3,2);
        history.accept(packet);long revision=history.revision();
        history.accept(packet);history.accept(ValueHistoryTest.packet(20,10,0,9,1,2,3,2));
        assertEquals(revision,history.revision());
        history.accept(ValueHistoryTest.packet(30,10,4,5,4));assertTrue(history.revision()>revision);
        revision=history.revision();history.configure(1,20);assertTrue(history.revision()>revision);
        history.resetTransport();revision=history.revision();history.resetTransport();assertEquals(revision,history.revision());
    }
    @Test void screenCopiesAreReusedAndInvalidatedWithoutChangingExportsOrFreeze()throws Exception{
        SwingUtilities.invokeAndWait(()->{try{
            var panel=new ValueViewerPanel(v->{});panel.bind("visual-perf");panel.setSize(300,220);
            AudioAnalysis.publish(Map.of("visual-perf",ValueHistoryTest.packet(20,10,0,9,1,2,3,2)));
            var first=panel.snapshot(true);
            for(int i=0;i<500;i++){assertSame(first,panel.snapshot(true));assertSame(first,panel.visibleSnapshot(true));}
            AudioAnalysis.publish(Map.of("unrelated",ValueHistoryTest.packet(100,10,0,0,0)));
            assertSame(first,panel.snapshot(true));
            AudioAnalysis.publish(Map.of("visual-perf",ValueHistoryTest.packet(20,10,0,9,1,2,3,2)));
            assertSame(first,panel.snapshot(true));
            AudioAnalysis.publish(Map.of("visual-perf",ValueHistoryTest.packet(30,10,4,5,4)));
            var next=panel.snapshot(true);assertNotSame(first,next);assertEquals(.03,next.end(),1e-12);assertEquals(9,first.maximum()[0][0]);
            panel.setSize(500,220);assertNotSame(next,panel.snapshot(true));
            panel.configure(new NodeState(Map.of("name-0",GLNodeValue.of("Amplitude"),"unit-0",GLNodeValue.of("percent"))));
            assertEquals("Amplitude",panel.snapshot(true).names()[0]);assertEquals("%",panel.snapshot(true).units()[0]);
            var export=panel.snapshot(false);var independent=panel.snapshot(false);assertNotSame(export,independent);
            export.value()[0][0]=12345;assertNotEquals(12345,panel.snapshot(true).value()[0][0]);assertNotEquals(12345,independent.value()[0][0]);
            panel.setPaused(true);var frozen=panel.snapshot(true);
            AudioAnalysis.publish(Map.of("visual-perf",ValueHistoryTest.packet(40,10,8,9,8)));
            assertSame(frozen,panel.snapshot(true));panel.setPaused(false);assertEquals(.04,panel.snapshot(true).end(),1e-12);
            AudioAnalysis.clear();assertEquals(0,panel.snapshot(true).size());
        }finally{AudioAnalysis.clear();}});
    }
    @RegisterNode(id="test.visualperformance",title="Measured",category=NodeCategory.MATH,kind=NodeKind.VALUE)
    public static class MeasuredNode extends BasicNode {
        int layouts;
        @Override public void define(NodeBuilder node,NodeState state){layouts++;node.addInput(new PropertyId("in"),"In",Editor.Slider.button(0,0d,1d));}
        @Override public NativeProcessor processor(){return new NativeProcessor("math","nodes/MathNode.hpp");}
    }
    @Test void unchangedBindingsAvoidLayoutWorkButConnectionsAndParametersStillUpdate()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            var definition=new MeasuredNode();var model=new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults());
            var view=new BasicNodeView(definition,new NodeViewContext((id,key,value)->{},new GLHistoryManager()),model);
            var editor=new GLNodeEditor();editor.addNode(view,0,0);definition.layouts=0;
            for(int i=0;i<500;i++)view.update(model);
            assertEquals(0,definition.layouts);
            var source=new GLNode("Source");var output=source.addOutput("out",new GLLabel("Out"));editor.addNode(source,0,100);
            editor.connect(output,view.findPort("in"));view.update(model);assertEquals(1,definition.layouts);
            view.update(model);assertEquals(1,definition.layouts);
            view.update(model.withProperty("in",GLNodeValue.of(.25)));assertEquals(2,definition.layouts);
            assertEquals(.25,((GLSlider)view.getContents().get(0)).getValue(),1e-12);
        });
    }
}
