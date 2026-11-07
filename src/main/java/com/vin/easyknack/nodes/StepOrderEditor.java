package com.vin.easyknack.nodes;

import java.util.*;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.layout.GLConstraints;
import com.vingame.glswing.rendering.GLIcon;
import com.vingame.glswing.rendering.GLPath2D;
import com.vingame.glswing.component.GLContainer;

/** One connected control group per step; stable slots retain values and cables. */
public record StepOrderEditor(NodeState state) implements Editor {
    public GLNodeValue defaultValue(){return GLNodeValue.of(List.of());}
    public String viewKey(){return getClass().getName()+StepSwitchNode.count(state)+StepSwitchNode.type(state);}
    public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
        var panel=new Panel(state,changed);return new Control(panel,v->{});
    }
    public static final class Panel extends GLContainer {
        private static final GLIcon UP=GLIcon.fromPath(new GLPath2D().moveTo(0,8).lineTo(5,2).lineTo(10,8).lineTo(8,10).lineTo(5,6).lineTo(2,10).closePath());
        private static final GLIcon DOWN=GLIcon.fromPath(new GLPath2D().moveTo(0,2).lineTo(5,8).lineTo(10,2).lineTo(8,0).lineTo(5,4).lineTo(2,0).closePath());
        private record Row(int slot,GLLayoutGroup group,GLTextEditor name,GLSlider value,GLButton up,GLButton down){}
        private final java.util.function.Consumer<GLNodeValue> changed;
        private final List<Row> rows=new ArrayList<>();
        private NodeState state;
        private List<Integer> order=List.of();
        private boolean updating;
        Panel(NodeState state,java.util.function.Consumer<GLNodeValue> changed){this.changed=changed;configure(state);}
        private void write(PropertyId property,GLNodeValue value){
            if(updating)return;
            for(var parent=getGLParent();parent instanceof GLContainer container;parent=container.getGLParent())
                if(container instanceof com.vingame.glswing.node.GLNodePropertyInspector inspector){inspector.setSelectedProperty(property.value(),value);return;}
        }
        public void configure(NodeState next){
            updating=true;
            try{
                state=next;order=StepSwitchNode.order(next);boolean numeric=StepSwitchNode.type(next)==StepSwitchNode.Type.VALUE;
                if(rows.size()!=order.size()||(!rows.isEmpty()&&(rows.get(0).value()!=null)!=numeric)){
                    removeAllGL();rows.clear();
                    for(int slot=0;slot<order.size();slot++){
                        int id=slot;var group=new GLLayoutGroup();group.setName("step-row-"+slot);
                        var name=new GLTextEditor();name.setName("step-name-"+slot);name.setHistoryEnabled(false);name.setPreferredGLSize(0,28);
                        GLSlider slider=null;
                        var up=new GLButton("");var down=new GLButton("");up.setIcon(UP);down.setIcon(DOWN);
                        up.setName("step-up-"+slot);down.setName("step-down-"+slot);
                        up.setPreferredGLSize(24,28);down.setPreferredGLSize(24,28);
                        up.setToolTipText(NodeText.text("viewer.series.up","Move up"));down.setToolTipText(NodeText.text("viewer.series.down","Move down"));
                        name.addActionListener(e->write(StepSwitchNode.name(id),GLNodeValue.of(name.getText())));
                        group.addGL(name,GLConstraints.at(0,0).weight(1,1).fillBoth().connectAll());
                        if(numeric){
                            var spec=Editor.Slider.button(0);var control=spec.create("",v->write(StepSwitchNode.input(id),v));
                            slider=(GLSlider)control.component();slider.setName("step-value-"+slot);slider.setPreferredGLSize(70,28);
                            group.addGL(slider,GLConstraints.at(1,0).weight(1,1).fillBoth().connectAll());
                        }
                        group.addGL(up,GLConstraints.at(2,0).fillBoth().connectAll());group.addGL(down,GLConstraints.at(3,0).fillBoth().connectAll());
                        up.addActionListener(e->move(id,-1));down.addActionListener(e->move(id,1));
                        rows.add(new Row(slot,group,name,slider,up,down));addGL(group);
                    }
                }
                for(var row:rows){
                    String label=StepSwitchNode.label(next,row.slot());if(!row.name().getText().equals(label))row.name().setText(label);
                    if(row.value()!=null)row.value().setValue(next.number(StepSwitchNode.input(row.slot()),0));
                    int index=order.indexOf(row.slot());row.up().setEnabled(index>0);row.down().setEnabled(index+1<order.size());
                }
                setPreferredGLSize(240,order.size()*32);resized(getWidth(),getHeight());
            }finally{updating=false;}
        }
        private void move(int slot,int delta){var replacement=new ArrayList<>(order);int index=replacement.indexOf(slot);int target=index+delta;if(target<0||target>=replacement.size())return;Collections.swap(replacement,index,target);changed.accept(GLNodeValue.of(replacement));}
        @Override protected void record(com.vingame.glswing.rendering.GLContext2D context){}
        @Override protected void resized(float w,float h){for(int index=0;index<order.size();index++){int slot=order.get(index);var row=rows.get(slot);row.group().setBounds(0,index*32,w,28);row.group().refreshConnections();}}
    }
}
