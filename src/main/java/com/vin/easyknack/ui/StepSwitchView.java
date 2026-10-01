package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.StepSwitchNode;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.node.model.GLGraphNode;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.layout.GLConstraints;
import com.vin.easyknack.audio.ManualTriggers;
import com.vin.easyknack.nodes.NodeText;
import java.util.*;
import com.vin.easyknack.nodes.NodeBuilder;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.input.GLMouseEvent;
import com.vingame.glswing.ui.GLSlider;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.node.model.GLNodeValue;

/** Runtime selection is presentation only and never changes the saved graph or undo history. */
public final class StepSwitchView extends BasicNodeView {
    private int active;
    private Map<Integer,SelectionButton> selections;
    private final Runnable refresh=this::refresh;
    public StepSwitchView(StepSwitchNode definition,NodeViewContext context,GLGraphNode model){
        super(definition,context,model);
        for(int i:selections.keySet())findPort(StepSwitchNode.input(i).value()).setContentHiddenWhenConnected(false);
        AudioAnalysis.listen(refresh);refresh();
    }
    private void refresh(){
        var frame=AudioAnalysis.frame(nodeId().toString());
        int next=frame==null?0:(int)frame.frequency();
        if(findPort(StepSwitchNode.input(next).value())==null)next=0;
        active=next;
        selections.forEach((i,button)->button.selected(i==active));
    }
    public int activeStep(){return active;}
    @Override protected GLContainer control(NodeBuilder.Row row){
        if(row.id().startsWith("step-")){
            int index=Integer.parseInt(row.id().substring(5));
            var slider=super.control(row);
            var label=new NodeTooltipLabel(row.label(),false,true);label.setVisible(false);
            var button=new SelectionButton(Integer.toString(index+1));
            button.setToolTipText(NodeText.text("easyknack.routing.stepswitch.select.tooltip","Select this step at the next audio block."));
            button.addActionListener(e->ManualTriggers.fire(nodeId().toString(),"select-"+index));
            if(selections==null)selections=new LinkedHashMap<>();selections.put(index,button);
            var group=new GLLayoutGroup();
            group.addGL(slider,GLConstraints.at(0,0).weight(1,1).fillBoth().connectAll());
            group.addGL(label,GLConstraints.at(0,0).weight(1,1).fillBoth().connectAll());
            group.addGL(button,GLConstraints.at(1,0).fillBoth().connectAll());
            onStateChanged(state->{slider.setVisible(!state.isConnected(row.key()));label.setVisible(state.isConnected(row.key()));});
            return group;
        }
        if(!row.key().equals(StepSwitchNode.STEPS))return super.control(row);
        // Rebuilding ports during a drag would detach the slider holding mouse capture.
        class StepsSlider extends GLSlider {
            private boolean dragging;
            StepsSlider(){super(row.label(),1,StepSwitchNode.MAX_STEPS,4);}
            @Override protected void controlMousePressed(GLMouseEvent e){dragging=true;super.controlMousePressed(e);}
            @Override protected void controlMouseReleased(GLMouseEvent e){try{super.controlMouseReleased(e);}finally{commit();}}
            @Override protected void controlMouseCaptureLost(GLMouseEvent e){try{super.controlMouseCaptureLost(e);}finally{commit();}}
            private void commit(){if(dragging){dragging=false;write(row.key(),GLNodeValue.of(getValue()));}}
        }
        var slider=new StepsSlider();slider.setIntegerValuesOnly(true);slider.setStep(1);slider.setDragSensitivity(1);
        slider.setGLHistoryManager(new GLHistoryManager());
        slider.addChangeListener(e->{if(!slider.dragging)write(row.key(),GLNodeValue.of(slider.getValue()));});
        onStateChanged(s->{if(!slider.dragging)slider.setValue(s.number(row.key(),4));});
        return slider;
    }
    private static final class SelectionButton extends GLButton {
        private boolean selected;
        SelectionButton(String text){super(text);setPreferredGLSize(26,getPreferredGLSize().height());}
        void selected(boolean value){if(selected==value)return;selected=value;refreshStyle();}
        private void refreshStyle(){
            if(!selected){clearGLStyle();return;}
            var style=getGLTheme().buttonStyle();
            setGLStyle(style.withBackground(style.pressedDarkColor()).withHoverBackground(style.pressedLightColor()));
        }
        @Override protected void themeChanged(){super.themeChanged();refreshStyle();}
    }
}
