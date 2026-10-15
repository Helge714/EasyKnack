package com.vin.easyknack.ui;
import javax.swing.SwingUtilities;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;
import com.vingame.glswing.component.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.ui.*;

@EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
class CurvatureDialogRenderingTest {
    static java.util.stream.Stream<GLContainer> descendants(GLParent p){return p.getGLChildren().stream().flatMap(c->java.util.stream.Stream.concat(java.util.stream.Stream.of(c),descendants(c)));}
    static java.util.stream.Stream<GLParent> surfaces(java.awt.Component c){
        if(c instanceof GLParent p)return java.util.stream.Stream.of(p);
        return c instanceof java.awt.Container parent?java.util.Arrays.stream(parent.getComponents()).flatMap(CurvatureDialogRenderingTest::surfaces):java.util.stream.Stream.empty();
    }
    static void await(java.util.function.BooleanSupplier condition)throws Exception{
        long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(15);
        while(System.nanoTime()<until){var ok=new boolean[1];SwingUtilities.invokeAndWait(()->ok[0]=condition.getAsBoolean());if(ok[0])return;Thread.sleep(50);}
        fail("Timed out waiting for rendered state");
    }
    @Test void actualPreferencesUpdatesWorkspaceBeforeSaving()throws Exception{
        var frames=new javax.swing.JFrame[1];var views=new WorkspaceView[1];var editors=new GLNodeEditor[1];var dialog=new java.awt.Window[1];
        var app=com.vingame.glswing.application.GLApplication.builder().title("Preferences regression")
            .backend(com.vingame.glswing.application.GLBackend.CANVAS).size(1000,700)
            .surface(context->{
                frames[0]=context.window();var view=views[0]=new WorkspaceView(context.window());
                editors[0]=descendants(view).filter(GLNodeEditor.class::isInstance).map(GLNodeEditor.class::cast).findFirst().orElseThrow();
                view.previewCurvature(1);return view;
            }).launch();
        app.readyFuture().get(30,TimeUnit.SECONDS);
        try{
            await(()->editors[0].getLastCurveSegmentCount()>0);
            SwingUtilities.invokeAndWait(()->SettingsDialog.show(views[0]));
            await(()->{dialog[0]=java.util.Arrays.stream(java.awt.Window.getWindows()).filter(w->w.getOwner()==frames[0]&&w.isShowing()).findFirst().orElse(null);return dialog[0]!=null;});
            SwingUtilities.invokeAndWait(()->{
                var slider=surfaces(dialog[0]).flatMap(CurvatureDialogRenderingTest::descendants).filter(c->c instanceof GLSlider s&&s.getText().equals(AppText.text("settings.curvature"))).map(GLSlider.class::cast).findFirst().orElseThrow();
                slider.setValue(slider.getValue()==0?.5:0);slider.setValue(0);
                assertEquals(0,editors[0].getConnectionHandleFactor(),"Actual preferences did not update editor geometry");
            });
            await(()->editors[0].getLastCurveSegmentCount()==0);
            assertTrue(dialog[0].isShowing());
        }finally{
            SwingUtilities.invokeAndWait(()->{if(dialog[0]!=null)dialog[0].dispatchEvent(new java.awt.event.WindowEvent(dialog[0],java.awt.event.WindowEvent.WINDOW_CLOSING));views[0].close();app.close();});
            app.closedFuture().get(10,TimeUnit.SECONDS);
        }
    }
}
