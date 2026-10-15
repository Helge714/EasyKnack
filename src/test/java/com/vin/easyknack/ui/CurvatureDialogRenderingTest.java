package com.vin.easyknack.ui;

import javax.swing.SwingUtilities;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;
import com.vingame.glswing.component.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.rendering.*;

@EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
class CurvatureDialogRenderingTest {
    @Test void ownerRendersChangedCurvesWhileModalDialogRemainsOpen()throws Exception{
        var rendered=new CountDownLatch(1);var initial=new CountDownLatch(1);
        var frames=new GLFrame[1];var dialogs=new GLDialog[1];var sliders=new GLSlider[1];
        SwingUtilities.invokeAndWait(()->{
            var editor=new GLNodeEditor(){
                @Override protected void render(GLContext2D c){
                    super.render(c);
                    if(getConnections().isEmpty())return;
                    if(getConnectionHandleFactor()==0)rendered.countDown();else initial.countDown();
                }
            };
            var source=new GLNode("Source");var target=new GLNode("Target");
            var out=source.addOutput("out",new GLLabel("Out"));var in=target.addInput("in",new GLLabel("In"));
            editor.addNode(source,30,30);editor.addNode(target,350,180);editor.connect(out,in);
            var frame=frames[0]=new GLFrame("Curvature preview regression");frame.setSize(640,420);frame.addGL(editor);frame.setVisible(true);
            var slider=sliders[0]=GLSlider.fixed("Curvature",0,1,1);slider.addChangeListener(e->ConnectionAppearance.apply(editor,slider.getValue()));
            var panel=new GLContainer(){protected void record(GLContext2D c){}protected void resized(float w,float h){slider.setBounds(10,10,w-20,30);}};panel.addGL(slider);
            dialogs[0]=new GLDialog("Preferences regression",panel);dialogs[0].setDecoration(GLDialog.Decoration.THEMED);dialogs[0].setSize(300,80);
        });
        try{
            assertTrue(initial.await(15,TimeUnit.SECONDS),"Owner did not render initial connections");
            SwingUtilities.invokeAndWait(()->dialogs[0].show(frames[0]));
            var ready=new CompletableFuture<Void>();
            SwingUtilities.invokeAndWait(()->{var timer=new javax.swing.Timer(50,null);timer.addActionListener(e->{if(dialogs[0].isShowing()){timer.stop();sliders[0].setValue(0);ready.complete(null);}});timer.setInitialDelay(300);timer.start();});
            ready.get(15,TimeUnit.SECONDS);
            assertTrue(rendered.await(5,TimeUnit.SECONDS),"Owner kept the old curves while the modal dialog was open");
            assertTrue(dialogs[0].isShowing());
        }finally{
            SwingUtilities.invokeAndWait(()->{dialogs[0].close();frames[0].close();});
            frames[0].closedFuture().get(10,TimeUnit.SECONDS);
        }
    }
}
