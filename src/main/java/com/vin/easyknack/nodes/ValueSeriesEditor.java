package com.vin.easyknack.nodes;

import com.vingame.glswing.node.model.GLNodeValue;

/** A single document property keeps a whole metadata/reorder edit undoable. */
public record ValueSeriesEditor(NodeState state,int count) implements Editor {
    public GLNodeValue defaultValue(){return new GLNodeValue.ListValue(java.util.List.of());}
    @Override public String viewKey(){return getClass().getName()+":"+count;}
    public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
        var panel=new com.vin.easyknack.ui.ValueSeriesSettingsPanel(state,count,changed);
        return new Control(panel,panel::update);
    }
}
