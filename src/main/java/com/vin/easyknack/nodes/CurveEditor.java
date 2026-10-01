package com.vin.easyknack.nodes;

import java.util.function.Consumer;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.*;

/** Curve widget adapter: graph properties own the curve; no view-local undo entries. */
public record CurveEditor(GLFunctionCurve initial) implements Editor {
    @Override public GLNodeValue defaultValue(){return GLNodeValue.of(CurveData.encode(initial));}
    @Override public void validate(GLNodeValue value){
        if(!(value instanceof GLNodeValue.TextValue text))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Curve text required"));
        CurveData.decode(text.value());
    }
    @Override public Control create(String label,Consumer<GLNodeValue> changed){
        var editor=new GLCurveEditor();
        editor.setCurve(initial);
        editor.setResetCurve(initial);
        editor.setChangeListener(curve->changed.accept(GLNodeValue.of(CurveData.encode(curve))));
        return new Control(editor,value->editor.setCurve(CurveData.decode((String)value.toJavaValue())));
    }
    public static void bindHistory(GLCurveEditor editor,java.util.function.Supplier<com.vingame.glswing.history.GLHistoryManager> history){
        var transaction=new com.vingame.glswing.history.GLHistoryManager.Transaction[1];
        editor.setEditListeners(()->{var manager=history.get();if(manager!=null)transaction[0]=manager.beginTransaction("Edit curve");},
                ()->{if(transaction[0]!=null){transaction[0].close();transaction[0]=null;}});
    }
}
