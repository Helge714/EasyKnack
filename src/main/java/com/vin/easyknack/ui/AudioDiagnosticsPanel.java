package com.vin.easyknack.ui;
import com.vin.easyknack.audio.*;
import com.vin.easyknack.nodes.NodeText;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.rendering.GLContext2D;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.file.*;
import java.util.*;
/** Expandable GLCanvas-only diagnostics. No polling or file IO on the render thread. */
final class AudioDiagnosticsPanel extends GLContainer {
    private final AudioDiagnosticHistory history=new AudioDiagnosticHistory();
    private final GLLabel[] rows={new GLLabel(""),new GLLabel(""),new GLLabel(""),new GLLabel(""),new GLLabel("")};
    private final GLButton reset=new GLButton(text("reset","Reset measurement")),save=new GLButton(text("save","Save report…"));
    private final Plot plot=new Plot();
    private AudioDiagnostics latest;
    private AudioHostService.State state=AudioHostService.State.STOPPED;
    private String message="";
    private boolean resetPending;
    static String text(String key,String fallback){return NodeText.text("diagnostics."+key,fallback);}
    AudioDiagnosticsPanel(Runnable resetMeasurement){
        for(var row:rows)addGL(row);addGL(reset);addGL(save);addGL(plot);setVisible(false);
        reset.addActionListener(e->{resetPending=true;message=text("reset.pending","Reset requested; waiting for host");refresh();resetMeasurement.run();});
        save.addActionListener(e->saveReport());
        plot.setToolTipText(text("help","Render and Peak measure complete processing cycles: audio blocks plus all active FFT hops. Includes graph IO and joined spectral workers. Over budget counts cycles above 100%, not dropouts. Background analysis is excluded. Ringbuffer starvation and ASIO overloads have separate counters."));refresh();
    }
    void state(AudioHostService.State value){state=value;refresh();}
    void accept(AudioDiagnostics value){accept(value,System.nanoTime());}
    void accept(AudioDiagnostics value,long observedTime){
        boolean resetAcknowledged=value!=null&&latest!=null&&resetPending&&latest.epoch()!=value.epoch();
        if(value!=null&&latest!=null&&state!=AudioHostService.State.RUNNING&&!resetAcknowledged){refresh();return;}
        if(value==null){latest=null;history.clear();resetPending=false;message=text("unavailable","Diagnostics unavailable / waiting for host");}
        else{if(latest==null||latest.epoch()!=value.epoch()){message="";history.clear();resetPending=false;}latest=value;if(state==AudioHostService.State.RUNNING&&value.frames()>0)history.add(observedTime,value);}
        refresh();plot.invalidateRecording();
    }
    private void refresh(){
        rows[0].setText(text("title","Render diagnostics")+" — "+state+"  "+message);
        var d=latest;reset.setEnabled(d!=null&&state==AudioHostService.State.RUNNING&&!resetPending);if(d==null){for(int i=1;i<rows.length;i++)rows[i].setText("");return;}
        rows[1].setText(String.format(Locale.ROOT,text("timing","Mean %.1f %% | Peak %.1f %% | p99 %.3f ms | Session max %.3f ms"),d.mean(),d.peak(),d.p99us()/1000,d.maxUs()/1000));
        rows[2].setText(String.format(Locale.ROOT,text("budget","Cycle %d samples at %.0f Hz | Budget %.3f ms | Over budget %d | Telemetry lost %d"),d.frames(),d.sampleRate(),d.budgetUs()/1000,d.overBudget(),d.dropped()));
        rows[3].setText(String.format(Locale.ROOT,text("io","Capture under/over %d/%d | Playback under/over %d/%d | ASIO overloads %d | Priming blocks %d"),d.captureUnder(),d.captureOver(),d.playbackUnder(),d.playbackOver(),d.driverOverload(),d.priming()));
        rows[4].setText(d.clock()+" | "+text("legend","Last 60 s: mean / peak, red = error increase. Grid = 100%. Stopped values are stale."));
    }
    private void saveReport(){
        var host=com.vin.easyknack.plugins.PluginManager.activeHost();
        var executable=AudioHostClient.defaultExecutable();
        String metadata="# application="+Optional.ofNullable(AudioDiagnosticsPanel.class.getPackage().getImplementationVersion()).orElse("0.0.1-SNAPSHOT / development")+"\n# plugin_build="+(host==null?"builtin":host.getParent().getFileName())+"\n";
        String report=metadata+history.report(state.name());
        var request=GLFileDialogRequest.save().title(text("save","Save report…")).filter(com.vin.easyknack.ui.AppText.literal("CSV report"),"csv").suggestedFileName("easyknack-diagnostics.csv").build();
        new RememberingFileDialogs().show(request,this).whenComplete((choice,error)->javax.swing.SwingUtilities.invokeLater(()->{
            if(error!=null){message=error.getMessage();refresh();return;}if(!choice.approved())return;
            java.util.concurrent.CompletableFuture.runAsync(()->{try{
                String fingerprint="unavailable";
                try(var input=java.nio.file.Files.newInputStream(executable)){
                    var digest=java.security.MessageDigest.getInstance("SHA-256");byte[] buffer=new byte[65536];int count;while((count=input.read(buffer))!=-1)digest.update(buffer,0,count);fingerprint=HexFormat.of().formatHex(digest.digest());
                }catch(java.io.IOException|java.security.NoSuchAlgorithmException unavailable){}
                java.nio.file.Files.writeString(choice.selectedPath(),"# host_sha256="+fingerprint+"\n"+report);
            }catch(java.io.IOException e){throw new java.util.concurrent.CompletionException(e);}}).whenComplete((ignored,failure)->javax.swing.SwingUtilities.invokeLater(()->{message=failure==null?text("saved","Report saved"):"Report: "+failure.getMessage();refresh();}));
        }));
    }
    @Override protected void record(GLContext2D c){}
    @Override protected void resized(float w,float h){for(int i=0;i<rows.length;i++)rows[i].setBounds(18,i*23,Math.max(0,w-36),23);plot.setBounds(18,120,Math.max(0,w-36),Math.max(0,h-163));reset.setBounds(18,h-35,190,30);save.setBounds(220,h-35,170,30);}
    private final class Plot extends GLControl {
        @Override protected void record(GLContext2D c){
            var style=getGLTheme().sliderStyle();float w=getWidth(),h=getHeight();var values=getGLTheme().values();
            var mean=values.getColor("easyknack.diagnostics.mean",style.progressLightColor());var peak=values.getColor("easyknack.diagnostics.peak",style.borderColor());var error=values.getColor("easyknack.diagnostics.error",ApplicationTheme.current().color("diagnostics.error"));
            c.fillUIRoundRect(0,0,w,h,style.cornerRadius(),style.background());var points=history.points();double max=200;for(var p:points)max=Math.max(max,p.value().peak()*1.1);
            c.beginClip(0,0,w,h);float line=(float)(h*(1-100/max));c.drawLine(0,line,w,line,style.borderColor());
            long end=points.isEmpty()?0:points.get(points.size()-1).time();
            for(int i=1;i<points.size();i++){var a=points.get(i-1);var b=points.get(i);float x=w*(1-(end-a.time())/60_000_000_000f),xx=w*(1-(end-b.time())/60_000_000_000f);
                c.drawLine(x,(float)(h*(1-a.value().mean()/max)),xx,(float)(h*(1-b.value().mean()/max)),mean);
                c.drawLine(x,(float)(h*(1-a.value().peak()/max)),xx,(float)(h*(1-b.value().peak()/max)),peak);
                if(errors(b.value())>errors(a.value()))c.drawLine(xx,0,xx,h,error);
            }c.endClip();
        }
        private long errors(AudioDiagnostics d){return d.overBudget()+d.captureUnder()+d.captureOver()+d.playbackUnder()+d.playbackOver()+d.driverOverload();}
    }
}

