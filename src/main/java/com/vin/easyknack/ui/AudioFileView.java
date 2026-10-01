package com.vin.easyknack.ui;

import com.vin.easyknack.audio.AudioAnalysis;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.input.GLMouseEvent;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.GLSlider;
import com.vingame.glswing.ui.GLUnitSlider;

/** Playback position is telemetry, not a continuous stream of graph edits. */
public final class AudioFileView extends BasicNodeView {
    private PositionSlider position;
    private final Runnable refresh=this::refreshPosition;
    public AudioFileView(AudioFileNode node,NodeViewContext context,GLGraphNode model){super(node,context,model);AudioAnalysis.listen(refresh);refreshPosition();}
    GLSlider positionSlider(){return position;}
    private void refreshPosition(){
        if(position==null||position.dragging||position.isValueEditing()||position.connected||System.nanoTime()<position.holdUntil)return;
        var frame=AudioAnalysis.frame(nodeId().toString());
        if(frame!=null&&Double.isFinite(frame.frequency()))position.display(frame.frequency());
    }
    @Override protected GLContainer control(NodeBuilder.Row row){
        if(!row.key().equals(AudioFileNode.POSITION))return super.control(row);
        position=new PositionSlider(row.label(),(FilePositionEditor)row.editor());
        position.setStoredValueListener(e->{if(!position.displaying){position.holdUntil=System.nanoTime()+200_000_000L;write(row.key(),GLNodeValue.of(position.getStoredValue()));}});
        onStateChanged(state->{position.connected=state.isConnected(row.key());if(!position.dragging&&!position.isValueEditing()&&System.nanoTime()>=position.holdUntil){var frame=AudioAnalysis.frame(nodeId().toString());position.display(!position.connected&&frame!=null?frame.frequency():state.number(row.key(),0));}});
        return position;
    }
    private static final class PositionSlider extends GLUnitSlider<FilePositionEditor.Unit> {
        boolean displaying,dragging,connected;
        long holdUntil;
        PositionSlider(String label,FilePositionEditor editor){super(label,0,0.0,1.0,.001,false,Presentation.BAR,editor.formats(),editor.formats().initial());}
        void display(double value){displaying=true;try{setStoredValue(Math.max(0,Math.min(1,value)));}finally{displaying=false;}}
        @Override protected void controlMousePressed(GLMouseEvent e){dragging=true;super.controlMousePressed(e);}
        @Override protected void controlMouseReleased(GLMouseEvent e){try{super.controlMouseReleased(e);}finally{dragging=false;}}
        @Override protected void controlMouseCaptureLost(GLMouseEvent e){try{super.controlMouseCaptureLost(e);}finally{dragging=false;}}
    }
}
