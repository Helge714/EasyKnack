package com.vin.easyknack.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.SwingUtilities;
import com.jogamp.opengl.*;
import com.vingame.glswing.node.GLNodeEditor;
import com.vingame.glswing.rendering.*;

class SettingsPreviewTest {
    @Test void languagePreviewDoesNotPersistPreferences(){
        var previous=ApplicationSettings.locale();var selected=previous.getLanguage().equals("de")?java.util.Locale.ENGLISH:java.util.Locale.GERMAN;
        try{
            ApplicationSettings.previewLocale(selected);AppText.reload();
            assertEquals(selected,ApplicationSettings.locale());
            assertEquals(selected.getLanguage().equals("de")?"Neu":"New",AppText.text("new"));
        }finally{ApplicationSettings.previewLocale(null);AppText.reload();}
        assertEquals(previous,ApplicationSettings.locale());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
    void curvaturePreviewInvalidatesTheRenderedEditorImmediately()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            var profile=GLProfile.get(GLProfile.GL3);var caps=new GLCapabilities(profile);caps.setOnscreen(false);
            var drawable=GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,caps,null,480,300);
            drawable.display();var context=drawable.getContext();context.makeCurrent();GLRenderer2D renderer=null;
            try{
                var gl=drawable.getGL().getGL3();renderer=GLRenderer2D.create(gl);
                var editor=new GLNodeEditor();editor.setBounds(0,0,480,300);
                for(double curvature:new double[]{0,.5,1}){
                    var frame=renderer.beginFrame(gl,480,300,Projection2D.TOP_LEFT_PIXELS.create(480,300));
                    editor.renderComponent(frame);renderer.endFrame();assertFalse(editor.isRecordingDirty());
                    ConnectionAppearance.apply(editor,curvature);assertTrue(editor.isRecordingDirty());
                }
            }finally{if(renderer!=null)renderer.dispose(drawable.getGL().getGL3());context.release();drawable.destroy();}
        });
    }
}
