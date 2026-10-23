package com.vin.easyknack.nodes;

import com.vingame.glswing.node.model.GLNodeValue;

public record ValueViewerEditor() implements Editor {
    @Override public GLNodeValue defaultValue(){return GLNodeValue.of("history");}
    @Override public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
        var panel=new com.vin.easyknack.ui.ValueViewerPanel(changed);
        return new Control(panel,v->panel.options(String.valueOf(v.toJavaValue())));
    }
}
