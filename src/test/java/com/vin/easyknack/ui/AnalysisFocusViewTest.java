package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import com.vin.easyknack.nodes.AnalysisEditor;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.input.*;

class AnalysisFocusViewTest {
    @Test void doubleClickOpensSharedLivePlotAndReturningKeepsOriginal()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            class Host extends GLContainer implements AnalysisEditor.Host {
                final GLContainer editor=new GLContainer(){@Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}};
                @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
                final AnalysisFocusView focus=new AnalysisFocusView(()->{focusClear();});
                Host(){addGL(editor);addGL(focus);focus.setBounds(0,0,1000,700);}
                void focusClear(){focus.clear();editor.setVisible(true);}
                public void openAnalysis(AnalysisEditor.Plot source){editor.setVisible(false);focus.open(source);}
            }
            var host=new Host();var source=new AnalysisEditor.Plot(false);source.bind("focus-test");
            source.configure(2,-.2,.4);source.setPreview(true);host.editor.addGL(source);
            var awt=new java.awt.event.MouseEvent(new java.awt.Canvas(),java.awt.event.MouseEvent.MOUSE_CLICKED,0,0,20,20,2,false,java.awt.event.MouseEvent.BUTTON1);
            var event=new GLMouseEvent(awt,new GLPickResult(1,source,20,20,20,20,1,0,0,1,0,0),0,0);
            source.mouseClicked(event);assertTrue(event.isConsumed());assertFalse(host.editor.isVisible());
            var expanded=host.focus.plot();assertNotNull(expanded);assertNotSame(source,expanded);assertSame(host.editor,source.getGLParent());
            // Core walks ancestors before clearing keyboard focus on a plot/background click.
            try{
                var dispatcher=Class.forName("com.vingame.glswing.component.GLContainerInputDispatcher");
                var preserve=dispatcher.getDeclaredMethod("preservesKeyboardFocus",GLMouseTarget.class);preserve.setAccessible(true);
                assertEquals(true,preserve.invoke(null,expanded));assertEquals(true,preserve.invoke(null,host.focus));
            }catch(ReflectiveOperationException e){throw new AssertionError(e);}
            assertEquals(984,expanded.getWidth());assertEquals(652,expanded.getHeight());assertEquals(2,expanded.verticalRange());
            source.configure(3,-.4,.6);source.setTriggersVisible(false);
            AudioAnalysis.publish(java.util.Map.of("focus-test",new AudioAnalysis.Frame(0,0,.01,new float[]{0,1,0})));
            assertEquals(3,expanded.verticalRange());assertFalse(expanded.isTriggersVisible());
            host.focusClear();assertNull(host.focus.plot());assertTrue(host.editor.isVisible());assertSame(host.editor,source.getGLParent());
            AudioAnalysis.clear();
        });
    }
}
