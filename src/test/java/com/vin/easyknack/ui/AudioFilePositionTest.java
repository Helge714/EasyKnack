package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.GLSlider;
import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;

class AudioFilePositionTest {
    @Test void telemetryMovesSliderWithoutWritingGraphAndUserChangesStillSeek()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            try{
                var definition=new AudioFileNode();var id=GLNodeId.random();var writes=new java.util.ArrayList<GLNodeValue>();
                var view=definition.createView(new NodeViewContext((n,p,v)->writes.add(v),new com.vingame.glswing.history.GLHistoryManager()),new GLGraphNode(id,definition.registration().id(),definition.defaults()));
                var slider=((AudioFileView)view).positionSlider();
                AudioAnalysis.publish(java.util.Map.of(id.toString(),new AudioAnalysis.Frame(.35,0,0,new float[]{.35f})));
                assertEquals(.35,slider.getValue(),1e-6);assertTrue(writes.isEmpty());
                slider.setValue(.6);assertEquals(1,writes.size());
                AudioAnalysis.publish(java.util.Map.of(id.toString(),new AudioAnalysis.Frame(.4,0,0,new float[]{.4f})));
                assertEquals(.6,slider.getValue(),1e-6);assertEquals(1,writes.size());
            }finally{AudioAnalysis.clear();}
        });
    }
}
