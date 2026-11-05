package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.StepSwitchNode;
import com.vin.easyknack.nodes.NodeState;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.node.model.GLGraphNode;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.layout.GLConstraints;
import com.vin.easyknack.nodes.NodeText;
import java.util.*;
import com.vin.easyknack.nodes.NodeBuilder;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.input.GLMouseEvent;
import com.vingame.glswing.ui.GLSlider;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.node.model.GLNodeValue;

/** Editable step labels and persisted selection, with revision-checked runtime feedback. */
public final class StepSwitchView extends BasicNodeView {
    private int active;
    private NodeState current;
    private Map<Integer,Runnable> nameActions;
    private Map<Integer,SelectionButton> selections;
    private final Runnable refresh=this::refresh;
    public StepSwitchView(StepSwitchNode definition,NodeViewContext context,GLGraphNode model){
        super(definition,context,model);
        for(int i:selections.keySet())findPort(StepSwitchNode.input(i).value()).setContentHiddenWhenConnected(false);
        current=new NodeState(model.properties());onStateChanged(state->{current=state;refresh();});
        AudioAnalysis.listen(refresh);refresh();
    }
    private void refresh(){
        if(current==null)return;
        var frame=AudioAnalysis.frame(nodeId().toString());
        int next=StepSwitchNode.selected(current);
        if(StepSwitchNode.indexed(current)&&!current.isConnected(StepSwitchNode.NEXT))next=(int)current.number(StepSwitchNode.NEXT,0);
        else if(frame!=null&&StepSwitchStates.reportedRevision(frame)==StepSwitchNode.revision(current))next=(int)frame.frequency();
        active=Math.max(0,Math.min(StepSwitchNode.count(current)-1,next));
        selections.forEach((i,button)->button.selected(i==active));
    }
    void selectStep(int index){
        if(current==null)return;
        int next=Math.max(0,Math.min(StepSwitchNode.count(current)-1,index));
        long revision=StepSwitchNode.nextRevision(current);
        write(StepSwitchNode.SELECTION,StepSwitchNode.selection(next,revision));
    }
    void renameStep(int index){var action=nameActions.get(index);if(action!=null)action.run();}
    public int activeStep(){return active;}
    @Override protected GLContainer portContent(NodeBuilder.Row row){
        return row.kind()==NodeBuilder.RowKind.INPUT&&row.id().startsWith("step-")?control(row):super.portContent(row);
    }
    @Override protected GLContainer control(NodeBuilder.Row row){
        if((row.key().equals(StepSwitchNode.NEXT)||row.key().equals(StepSwitchNode.RESET))&&row.editor() instanceof com.vin.easyknack.nodes.Editor.Trigger){
            var button=new GLButton(row.label());button.addActionListener(e->selectStep(row.key().equals(StepSwitchNode.RESET)?0:(active+1)%StepSwitchNode.count(current)));return button;
        }
        if(row.id().startsWith("step-")&&!row.id().startsWith("step-name-")){
            int index=Integer.parseInt(row.id().substring(5));
            boolean resource=row.editor()==null;
            var slider=resource?new NodeTooltipLabel(row.label(),false,true):super.control(row);
            var nameEditor=new GLTextEditor();nameEditor.setHistoryEnabled(false);nameEditor.setVisible(false);nameEditor.setName("step-name-editor-"+index);
            final boolean[] editing={false};
            var label=new NodeTooltipLabel(row.label(),false,true){
                @Override public void mousePressed(GLMouseEvent event){
                    if(event.getButton()==java.awt.event.MouseEvent.BUTTON1){renameStep(index);event.consume();}else super.mousePressed(event);
                }
            };label.setVisible(false);label.setToolTipText(AppText.text("stepswitch.renameHint"));
            Runnable begin=()->{editing[0]=true;nameEditor.setText(StepSwitchNode.label(current,index));slider.setVisible(false);label.setVisible(false);nameEditor.setVisible(true);requestGLFocus(nameEditor);nameEditor.selectAll();};
            Runnable finish=()->{editing[0]=false;nameEditor.setVisible(false);slider.setVisible(!resource&&!current.isConnected(row.key()));label.setVisible(resource||current.isConnected(row.key()));};
            nameEditor.addActionListener(e->{write(StepSwitchNode.name(index),GLNodeValue.of(nameEditor.getText()));finish.run();});
            nameEditor.addCancelListener(e->finish.run());
            if(nameActions==null)nameActions=new LinkedHashMap<>();nameActions.put(index,begin);
            if(slider instanceof GLSlider value)value.getContextMenu().addItem(AppText.text("stepswitch.rename"),e->begin.run());
            if(slider instanceof GLSlider value)value.getContextMenu().addItem(AppText.text("name.reset"),e->write(StepSwitchNode.name(index),GLNodeValue.of("")));
            var button=new SelectionButton(Integer.toString(index+1));
            button.setToolTipText(NodeText.text("easyknack.routing.stepswitch.select.tooltip","Select this step."));
            button.addActionListener(e->selectStep(index));
            if(selections==null)selections=new LinkedHashMap<>();selections.put(index,button);
            var group=new GLLayoutGroup();
            group.addGL(slider,GLConstraints.at(0,0).weight(1,1).fillBoth().connectAll());
            group.addGL(label,GLConstraints.at(0,0).weight(1,1).fillBoth().connectAll());
            group.addGL(nameEditor,GLConstraints.at(0,0).weight(1,1).fillBoth().connectAll());
            group.addGL(button,GLConstraints.at(1,0).fillBoth().connectAll());
            onStateChanged(state->{
                current=state;String text=StepSwitchNode.label(state,index);
                button.setEnabled(!StepSwitchNode.indexed(state));
                if(slider instanceof GLSlider value){value.setText(text);value.setName(text);}
                label.setText(text);
                slider.setVisible(!resource&&!editing[0]&&!state.isConnected(row.key()));label.setVisible(!editing[0]&&(resource||state.isConnected(row.key())));
            });
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
