package com.vin.easyknack.ui;

import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.ui.*;

/** Rebinds only strings issued by AppText, never user-entered text or model names. */
final class LiveLanguage {
    static void refresh(GLContainer component){refresh(component,java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()));}
    private static void refresh(GLContainer c,java.util.Set<GLContainer> seen){
        if(!seen.add(c))return;
        if(c instanceof NodeTooltipLabel v)v.refreshLanguage();
        else if(c instanceof GLLabel v)v.setText(AppText.translateExisting(v.getText()));
        else if(c instanceof GLButton v)v.setText(AppText.translateExisting(v.getText()));
        else if(c instanceof GLCheckBox v)v.setText(AppText.translateExisting(v.getText()));
        else if(c instanceof GLSlider v){v.setText(AppText.translateExisting(v.getText()));refresh(v.getContextMenu(),seen);}
        else if(c instanceof GLMenuBarItem v){v.setText(AppText.translateExisting(v.getText()));refresh(v.getMenu(),seen);}
        else if(c instanceof GLMenuItem v){v.setText(AppText.translateExisting(v.getText()));if(v.getSubMenu()!=null)refresh(v.getSubMenu(),seen);}
        if(c instanceof GLControl v)v.setToolTipText(AppText.translateExisting(v.getToolTipText()));
        for(var child:c.getGLChildren())refresh(child,seen);
    }
}
