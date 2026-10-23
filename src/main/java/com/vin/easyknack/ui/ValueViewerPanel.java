package com.vin.easyknack.ui;

import com.vin.easyknack.audio.AudioAnalysis;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.component.GLDialog;
import com.vingame.glswing.input.*;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.rendering.*;
import com.vingame.glswing.ui.*;
import java.awt.Color;
import java.util.function.Consumer;

/** Numeric and time-domain inspection of the same captured inputs. */
public final class ValueViewerPanel extends GLContainer {
    private static final class Session {
        final ValueHistory history=new ValueHistory();
        String id="";String[] names={"Value 1"},units={""};
        double window=10;boolean paused,numbers,overlay;
        ValueHistory.Snapshot frozen;
    }
    private final Session session;
    private final Consumer<GLNodeValue> changed;
    private final GLButton mode=new GLButton(""),pause=new GLButton(""),expand=new GLButton(""),export=new GLButton("");
    private final GLLabel status=new GLLabel("");
    private final Canvas chart=new Canvas();
    private final Runnable listener=this::dataReceived;
    private double zoom=1,pan=0,cursorA=Double.NaN,cursorB=Double.NaN;
    private int drag;
    private boolean expanded;
    private double pressX;
    public ValueViewerPanel(Consumer<GLNodeValue> changed){this(new Session(),changed,false);}
    private ValueViewerPanel(Session session,Consumer<GLNodeValue> changed,boolean expanded){
        this.session=session;this.changed=changed;this.expanded=expanded;
        for(var c:java.util.List.of(mode,pause,expand,export,chart,status))addGL(c);
        mode.addActionListener(e->{session.numbers=!session.numbers;saveOptions();});
        pause.addActionListener(e->setPaused(!session.paused));
        expand.addActionListener(e->openExpanded());
        export.addActionListener(e->ValueViewerExport.show(snapshot(false),visibleSnapshot(false),session.numbers,session.overlay,colors(),this));
        setPreferredGLSize(210,220);AudioAnalysis.listen(listener);refresh();
    }
    public void bind(String id){session.id=id;receive();refresh();}
    public void configure(NodeState state){
        int count=ValueViewerNode.count(state);session.window=state.number(ValueViewerNode.WINDOW,10);
        session.names=new String[count];session.units=new String[count];
        for(int i=0;i<count;i++){
            session.names[i]=state.text("name-"+i,AppText.literal("Value")+" "+(i+1));
            String unit=state.text("unit-"+i,"none");session.units[i]=unit.equals("none")?"":UnitFormat.fromId(unit).suffix();
        }
        session.history.configure(count,session.window);receive();refresh();
    }
    public void options(String options){session.numbers=options.contains("values");session.overlay=options.contains("overlay");refresh();}
    private void saveOptions(){changed.accept(GLNodeValue.of((session.numbers?"values":"history")+(session.overlay?";overlay":"")));refresh();}
    public boolean isPaused(){return session.paused;}
    public void setPaused(boolean value){
        if(value==session.paused)return;
        if(value)session.frozen=session.history.snapshot(session.names,session.units,true);
        session.paused=value;if(!value){session.frozen=null;zoom=1;pan=0;}refresh();
    }
    private void receive(){
        var packet=AudioAnalysis.frame(session.id);
        if(packet==null)session.history.resetTransport();else session.history.accept(packet);
    }
    private void dataReceived(){receive();if(!session.paused)refresh();}
    private void refresh(){
        mode.setText(AppText.text(session.numbers?"viewer.history":"viewer.values"));
        pause.setText(AppText.text(session.paused?"analysis.resume":"analysis.pause"));
        expand.setText(AppText.text("viewer.expand"));expand.setVisible(!expanded);
        export.setText(AppText.text("analysis.exportMenu"));
        var s=visibleSnapshot(true);export.setEnabled(s.size()>0);
        String info=AppText.text(session.paused?"analysis.frozen":"analysis.live");
        if(s.size()>0){
            double duration=s.end()-s.time()[0];info+=" · "+AnalysisChart.number(duration)+" s";
            if(duration+2*s.interval()<session.window&&s.size()>=ValueHistory.MAX_RECORDS/session.names.length-4)info+=" · "+AppText.text("viewer.memoryLimit");
        }
        if(Double.isFinite(cursorA))info+=" · "+measurement(s,"A",cursorA);
        if(Double.isFinite(cursorB))info+=" · "+measurement(s,"B",cursorB);
        if(Double.isFinite(cursorA)&&Double.isFinite(cursorB))info+=" · Δt "+AnalysisChart.number(Math.abs(cursorB-cursorA))+" s";
        status.setText(info);status.setToolTipText(info);chart.invalidateRecording();chart.repaintGL();
    }
    private String measurement(ValueHistory.Snapshot s,String name,double at){
        if(s.size()==0)return name+" —";
        int nearest=java.util.Arrays.binarySearch(s.time(),at);if(nearest<0)nearest=Math.min(s.size()-1,Math.max(0,-nearest-1));
        String result=name+" "+AnalysisChart.number(at-s.end())+" s";
        for(int p=0;p<s.channels();p++)result+=" / "+s.names()[p]+" "+AnalysisChart.number(s.value()[p][nearest])+" "+s.units()[p];return result;
    }
    public ValueHistory.Snapshot snapshot(boolean screen){
        if(session.paused&&session.frozen!=null)return session.frozen;
        return session.history.snapshot(session.names,session.units,false,screen?Math.max(100,(int)chart.getWidth()*2):Integer.MAX_VALUE);
    }
    public ValueHistory.Snapshot visibleSnapshot(boolean screen){
        var s=snapshot(screen);if(s.size()==0||zoom==1)return s;
        double span=s.end()-s.time()[0],end=s.end()-pan*span,start=end-span/zoom;
        return s.range(start,end);
    }
    private Color[] colors(){
        var result=new Color[8];for(int i=0;i<8;i++)result[i]=ApplicationTheme.current().color("viewer.trace"+(i+1));return result;
    }
    private void openExpanded(){
        var panel=new ValueViewerPanel(session,changed,true);panel.zoom=zoom;panel.pan=pan;panel.cursorA=cursorA;panel.cursorB=cursorB;
        var dialog=new GLDialog(AppText.text("viewer.title"),panel);dialog.setDecoration(GLDialog.Decoration.THEMED);dialog.setSize(1100,720);
        dialog.closedFuture().thenRun(()->{zoom=panel.zoom;pan=panel.pan;cursorA=panel.cursorA;cursorB=panel.cursorB;refresh();});dialog.show(this);
    }
    @Override protected void resized(float w,float h){
        float bw=Math.max(0,w-6)/4;
        mode.setBounds(0,0,bw,26);pause.setBounds(bw+2,0,bw,26);expand.setBounds(2*bw+4,0,bw,26);export.setBounds(3*bw+6,0,bw,26);
        chart.setBounds(0,30,w,Math.max(20,h-56));status.setBounds(4,Math.max(0,h-24),Math.max(0,w-8),24);
    }
    @Override protected void record(GLContext2D c){}
    private final class Canvas extends GLControl {
        Canvas(){setFocusable(false);}
        private double fraction(double x){return Math.max(0,Math.min(1,(x-Math.min(76,getWidth()*.24))/Math.max(1,getWidth()-12-Math.min(76,getWidth()*.24))));}
        @Override public void mouseWheelMoved(GLMouseWheelEvent e){
            if(session.numbers)return;double factor=Math.pow(1.2,-e.getPreciseWheelRotation()),old=zoom;
            zoom=Math.max(1,Math.min(10000,zoom*factor));pan=Math.max(0,Math.min(1-1/zoom,pan+(1-fraction(e.getLocalX()))*(1/old-1/zoom)));
            refresh();e.consume();
        }
        private void move(double x){var s=visibleSnapshot(true);if(s.size()==0)return;double t=s.time()[0]+fraction(x)*(s.end()-s.time()[0]);if(drag==1)cursorA=t;else cursorB=t;refresh();}
        @Override protected void controlMousePressed(GLMouseEvent e){
            GLToolTipManager.leave(this);if(e.getButton()==2){drag=3;pressX=e.getLocalX();e.consume();return;}
            if(e.getButton()!=1||session.numbers)return;
            if(!session.paused)setPaused(true);
            var s=visibleSnapshot(true);double at=s.size()==0?0:s.time()[0]+fraction(e.getLocalX())*(s.end()-s.time()[0]);
            drag=e.getOriginalEvent().isShiftDown()?2:!Double.isFinite(cursorA)?1:!Double.isFinite(cursorB)?2:Math.abs(at-cursorA)<=Math.abs(at-cursorB)?1:2;move(e.getLocalX());e.consume();
        }
        @Override protected void controlMouseDragged(GLMouseEvent e){
            if(drag==0)return;
            if(drag==3){pan=Math.max(0,Math.min(1-1/zoom,pan+(e.getLocalX()-pressX)/Math.max(1,getWidth())/zoom));pressX=e.getLocalX();refresh();}
            else move(e.getLocalX());e.consume();
        }
        @Override protected void controlMouseReleased(GLMouseEvent e){if(drag!=0){drag=0;e.consume();}}
        @Override protected void controlMouseCaptureLost(GLMouseEvent e){drag=0;}
        @Override protected void controlMouseClicked(GLMouseEvent e){
            if(e.getButton()==1&&e.getOriginalEvent().getClickCount()==2&&!expanded){openExpanded();e.consume();return;}
            if(e.getButton()!=3)return;
            var menu=new GLPopupMenu();
            menu.addItem(AppText.text(session.paused?"analysis.resume":"analysis.pause"),a->setPaused(!session.paused));
            menu.addItem(AppText.text(session.numbers?"viewer.history":"viewer.values"),a->{session.numbers=!session.numbers;saveOptions();});
            menu.addItem(AppText.text(session.overlay?"viewer.separate":"viewer.overlay"),a->{session.overlay=!session.overlay;saveOptions();});
            menu.addItem(AppText.text("analysis.resetView"),a->{zoom=1;pan=0;refresh();});
            if(Double.isFinite(cursorA))menu.addItem(AppText.text("analysis.clearCursorA"),a->{cursorA=Double.NaN;refresh();});
            if(Double.isFinite(cursorB))menu.addItem(AppText.text("analysis.clearCursorB"),a->{cursorB=Double.NaN;refresh();});
            menu.addItem(AppText.text("viewer.expand"),a->openExpanded());
            menu.addItem(AppText.text("analysis.exportMenu"),a->ValueViewerExport.show(snapshot(false),visibleSnapshot(false),session.numbers,session.overlay,colors(),ValueViewerPanel.this));
            menu.show(ValueViewerPanel.this,e.getScreenX(),e.getScreenY());e.consume();
        }
        @Override protected void record(GLContext2D c){
            var style=getGLTheme().sliderStyle();c.fillUIRoundRect(0,0,getWidth(),getHeight(),style.cornerRadius(),style.background());
            var font=getGLFont(getGLTheme().labelStyle().font());var stroke=c.getStroke();
            try{
                ValueHistoryChart.draw(new AnalysisChart.Painter(){
                    public void line(double a,double b,double d,double e,Color color,float width){c.setStroke(new GLStroke(width).withWidthSpace(GLStroke.WidthSpace.SCREEN));c.drawLine((float)a,(float)b,(float)d,(float)e,color);}
                    public void text(String text,double x,double y,Color color){c.drawUIText(font,text,(float)x,(float)y,color);}
                    public double textWidth(String text){return font.measureText(text);}
                    public void clip(double x,double y,double w,double h){c.beginClip((float)x,(float)y,(float)w,(float)h);}
                    public void unclip(){c.endClip();}
                },visibleSnapshot(true),getWidth(),getHeight(),session.numbers,session.overlay,style.borderColor(),getGLTheme().labelStyle().textColor(),colors(),cursorA,cursorB);
            }finally{c.setStroke(stroke);}
        }
    }
}
