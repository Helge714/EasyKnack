package com.vin.easyknack.nodes;

import java.util.function.Consumer;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.*;

/** Curve widget adapter: graph properties own the curve; no view-local undo entries. */
public record CurveEditor(GLFunctionCurve initial,boolean fixedPointPositions) implements Editor {
    public CurveEditor(GLFunctionCurve initial){this(initial,false);}
    @Override public GLNodeValue defaultValue(){return GLNodeValue.of(CurveData.encode(prepare(initial)));}
    @Override public void validate(GLNodeValue value){
        if(!(value instanceof GLNodeValue.TextValue text))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Curve text required"));
        CurveData.decode(text.value());
    }
    @Override public Control create(String label,Consumer<GLNodeValue> changed){
        var editor=new GLCurveEditor();
        editor.setFixedPointPositions(fixedPointPositions);
        if(fixedPointPositions){editor.setGridColumns(3);editor.setPlotPadding(0);}
        editor.setCurve(prepare(initial));
        editor.setResetCurve(prepare(initial));
        editor.setChangeListener(curve->changed.accept(GLNodeValue.of(CurveData.encode(prepare(curve)))));
        return new Control(editor,value->editor.setCurve(prepare(CurveData.decode((String)value.toJavaValue()))));
    }
    private GLFunctionCurve prepare(GLFunctionCurve curve){return fixedPointPositions?CurveData.envelopePhases(curve):curve;}
    public static void bindHistory(GLCurveEditor editor,java.util.function.Supplier<com.vingame.glswing.history.GLHistoryManager> history){
        var transaction=new com.vingame.glswing.history.GLHistoryManager.Transaction[1];
        editor.setEditListeners(()->{var manager=history.get();if(manager!=null)transaction[0]=manager.beginTransaction("Edit curve");},
                ()->{if(transaction[0]!=null){transaction[0].close();transaction[0]=null;}});
    }
}
