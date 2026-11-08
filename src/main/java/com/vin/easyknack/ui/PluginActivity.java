package com.vin.easyknack.ui;

import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.rendering.GLContext2D;
import com.vingame.glswing.ui.GLLabel;

/** Persistent Canvas feedback, independent of the native window title and audio status. */
final class PluginActivity extends GLContainer implements AutoCloseable {
    private final GLLabel label=new GLLabel("");
    private final Runnable layoutChanged;
    private final javax.swing.Timer timer=new javax.swing.Timer(500,event->refresh());
    private String phase="";
    private long started;
    PluginActivity(Runnable layoutChanged){this.layoutChanged=layoutChanged;addGL(label);setVisible(false);}
    void setPhase(String value){
        boolean wasRunning=!phase.isEmpty();phase=value==null?"":value;
        if(phase.isEmpty()){timer.stop();setVisible(false);}
        else {if(!wasRunning)started=System.nanoTime();setVisible(true);timer.start();refresh();}
        if(wasRunning!=!phase.isEmpty())layoutChanged.run();
    }
    private void refresh(){
        long seconds=(System.nanoTime()-started)/1_000_000_000;
        label.setText(com.vin.easyknack.i18n.AppText.literal("Plugin installation / build")+".".repeat(1+(int)(seconds%3))+"   "+phase+"   ("+seconds+" s)");
    }
    @Override protected void record(GLContext2D context){}
    @Override protected void resized(float width,float height){label.setBounds(24,0,Math.max(0,width-48),height);}
    @Override public void close(){timer.stop();}
}
