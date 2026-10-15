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
        editor.invalidateRecording();
        editor.repaintAndRebuildPicking(editor);
        // A preferences dialog renders on a different surface. Invalidate the owner
        // scene as well, rather than relying on the dialog's interactive repaint.
        com.vingame.glswing.component.GLParent root=editor;
        while(root instanceof com.vingame.glswing.component.GLContainer c && c.getGLParent()!=null)root=c.getGLParent();
        root.repaintGL();
    }
}
