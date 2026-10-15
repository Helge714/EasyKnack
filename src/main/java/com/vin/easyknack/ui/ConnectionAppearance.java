package com.vin.easyknack.ui;

import com.vingame.glswing.node.GLNodeEditor;

/** Scales both Bezier handles from the current theme, preserving exact straight lines at zero. */
final class ConnectionAppearance {
    static void apply(GLNodeEditor editor,double curvature){
        if(!Double.isFinite(curvature)||curvature<0||curvature>1)throw new IllegalArgumentException("Curvature must be between zero and one");
        if(curvature==1){editor.clearConnectionHandleFactor();editor.clearMinimumConnectionHandleLength();}
        else{
            editor.setConnectionHandleFactor((float)(editor.getGLStyle().connectionHandleFactor()*curvature));
            editor.setMinimumConnectionHandleLength((float)(editor.getGLStyle().minimumConnectionHandleLength()*curvature));
        }
        editor.repaintAndRebuildPicking(editor);
    }
}
