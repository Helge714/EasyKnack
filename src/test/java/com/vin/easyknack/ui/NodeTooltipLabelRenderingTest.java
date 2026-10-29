package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.jogamp.opengl.*;
import com.vingame.glswing.rendering.*;

@EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
class NodeTooltipLabelRenderingTest {
    @Test void changedUnitInvalidatesCachedDrawing() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            var profile=GLProfile.get(GLProfile.GL3);var caps=new GLCapabilities(profile);caps.setOnscreen(false);
            var drawable=GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,caps,null,240,50);
            drawable.display();var context=drawable.getContext();context.makeCurrent();GLRenderer2D renderer=null;
            try {
                var gl=drawable.getGL().getGL3();renderer=GLRenderer2D.create(gl);
                var label=new NodeTooltipLabel("Out [Hz]",true);label.setBounds(0,0,240,50);
                var frame=renderer.beginFrame(gl,240,50,Projection2D.TOP_LEFT_PIXELS.create(240,50));
                label.renderComponent(frame);renderer.endFrame();assertFalse(label.isRecordingDirty());
                label.setText("Out [ms]");assertTrue(label.isRecordingDirty());
                frame=renderer.beginFrame(gl,240,50,Projection2D.TOP_LEFT_PIXELS.create(240,50));
                label.renderComponent(frame);renderer.endFrame();assertFalse(label.isRecordingDirty());
                assertEquals("Out [ms]",label.getText());
            } finally {if(renderer!=null)renderer.dispose(drawable.getGL().getGL3());context.release();drawable.destroy();}
        });
    }
}
