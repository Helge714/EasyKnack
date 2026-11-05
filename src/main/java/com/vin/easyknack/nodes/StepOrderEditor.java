package com.vin.easyknack.nodes;

import java.util.*;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.layout.GLConstraints;

/** Presentation order keeps stable step IDs, indices, values and cables together. */
public record StepOrderEditor(NodeState state) implements Editor {
    public GLNodeValue defaultValue(){return GLNodeValue.of(List.of());}
    public String viewKey(){return getClass().getName()+StepSwitchNode.count(state);}
    public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
        var panel=new Panel(state,changed);return new Control(panel,v->{});
    }
    public static final class Panel extends GLLayoutGroup {
        private final java.util.function.Consumer<GLNodeValue> changed;
        private Object signature;
        Panel(NodeState state,java.util.function.Consumer<GLNodeValue> changed){this.changed=changed;configure(state);}
        public void configure(NodeState state){
        var order=new ArrayList<>(StepSwitchNode.order(state));var key=List.of(List.copyOf(order),order.stream().map(i->StepSwitchNode.label(state,i)).toList());
        if(key.equals(signature))return;signature=key;removeAllGL();
        for(int row=0;row<order.size();row++){
            int at=row;int slot=order.get(row);var name=new GLLabel((slot+1)+": "+StepSwitchNode.label(state,slot));
            var up=new GLButton("↑");var down=new GLButton("↓");up.setName("step-up-"+slot);down.setName("step-down-"+slot);
            up.setToolTipText(NodeText.text("viewer.series.up","Move up"));down.setToolTipText(NodeText.text("viewer.series.down","Move down"));
            up.setEnabled(row>0);down.setEnabled(row+1<order.size());
            up.addActionListener(e->{Collections.swap(order,at,at-1);changed.accept(GLNodeValue.of(order));});
            down.addActionListener(e->{Collections.swap(order,at,at+1);changed.accept(GLNodeValue.of(order));});
            addGL(name,GLConstraints.at(0,row).weight(1,0).fillBoth());
            addGL(up,GLConstraints.at(1,row).fillBoth());addGL(down,GLConstraints.at(2,row).fillBoth());
        }
        setPreferredGLSize(240,order.size()*30);
        }
    }
}
