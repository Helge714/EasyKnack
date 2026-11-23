package com.vin.easyknack.nodes;

import com.vin.easyknack.audio.AudioAnalysis;
import com.vin.easyknack.i18n.AppText;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.layout.GLSize;
import com.vingame.glswing.rendering.*;
import java.awt.Color;

public record AnalysisEditor(boolean correlation,boolean resource) implements Editor {
    public interface Host { void openAnalysis(Plot source); }
    public AnalysisEditor(boolean correlation){this(correlation,false);}
    public GLNodeValue defaultValue(){return GLNodeValue.of("");}
    public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
        var plot=new Plot(correlation,resource);plot.optionsChanged=()->changed.accept(GLNodeValue.of(plot.options()));
        return new Control(plot,value->plot.options(String.valueOf(value.toJavaValue())));
    }
    public static final class Plot extends GLControl {
        private final boolean correlation,resource;
        private String nodeId="";
        private boolean preview,triggersVisible=true,logarithmic,paused;
        private double range=1,low=-.1,high=.1;
        private Plot source;
        private AudioAnalysis.Frame frozen,lastReceived,lastDisplayed;
        private String receivedAt="",frozenAt="";
        private Runnable optionsChanged=()->{};
        private final Runnable refresh=this::dataReceived;
        private final GLLabel caption=new GLLabel(AppText.literal("Waiting for audio"));
        private double pointer=-1;
        private AnalysisChart.View viewport=AnalysisChart.View.FULL;
        private double cursorA=Double.NaN,cursorB=Double.NaN,dragX,dragY;
        private int dragMode;
        public void resetView(){viewport=AnalysisChart.View.FULL;updateReadout();invalidateRecording();}
        public void clearCursors(){cursorA=cursorB=Double.NaN;updateReadout();invalidateRecording();}
        public void clearCursor(boolean first){if(first)cursorA=Double.NaN;else cursorB=Double.NaN;updateReadout();invalidateRecording();}
        @Override protected String toolTipTextAt(com.vingame.glswing.input.GLMouseEvent e){return dragMode!=0?null:super.toolTipTextAt(e);}
        public AnalysisChart.Snapshot visibleSnapshot(){return snapshot().withView(source==null?AnalysisChart.View.FULL:viewport,cursorA,cursorB);}
        private double mouseFraction(double x){return Math.max(0,Math.min(1,(x-78)/Math.max(1,getWidth()-102)));}
        private void moveCursor(double x){var s=visibleSnapshot();double value=s.xAt(mouseFraction(x));value=Math.copySign(s.sampleX(s.nearestIndex(value)),value);if(dragMode==1)cursorA=value;else cursorB=value;updateReadout();invalidateRecording();}
        @Override public void mouseWheelMoved(com.vingame.glswing.input.GLMouseWheelEvent e){
            if(source==null)return;
            boolean vertical=e.getOriginalEvent().isShiftDown();
            double anchor=vertical?1-Math.max(0,Math.min(1,(e.getLocalY()-36)/Math.max(1,getHeight()-120))):mouseFraction(e.getLocalX());
            viewport=viewport.zoom(anchor,Math.pow(1.2,e.getPreciseWheelRotation()),vertical);updateReadout();invalidateRecording();e.consume();
        }
        @Override protected void controlMousePressed(com.vingame.glswing.input.GLMouseEvent e){
            GLToolTipManager.leave(this);
            if(source==null||!hasFrame()||e.getLocalX()<78||e.getLocalX()>getWidth()-24||e.getLocalY()<36||e.getLocalY()>getHeight()-84)return;
            dragX=e.getLocalX();dragY=e.getLocalY();
            if(e.getButton()==2){dragMode=3;e.consume();return;}
            if(e.getButton()!=1)return;
            var s=visibleSnapshot();double x=mouseFraction(dragX);
            double distanceA=Double.isFinite(cursorA)?Math.abs(s.position(cursorA)-x):Double.POSITIVE_INFINITY;
            double distanceB=Double.isFinite(cursorB)?Math.abs(s.position(cursorB)-x):Double.POSITIVE_INFINITY;
            boolean near=Math.min(distanceA,distanceB)*Math.max(1,getWidth()-102)<=10;
            dragMode=e.getOriginalEvent().isShiftDown()?2:near?(distanceA<=distanceB?1:2):!Double.isFinite(cursorA)?1:!Double.isFinite(cursorB)?2:distanceA<=distanceB?1:2;
            moveCursor(dragX);e.consume();
        }
        @Override protected void controlMouseDragged(com.vingame.glswing.input.GLMouseEvent e){
            if(dragMode==0)return;
            if(dragMode==3){viewport=viewport.pan(-(e.getLocalX()-dragX)/Math.max(1,getWidth()-102)*(viewport.x1()-viewport.x0()),(e.getLocalY()-dragY)/Math.max(1,getHeight()-120)*(viewport.y1()-viewport.y0()));dragX=e.getLocalX();dragY=e.getLocalY();invalidateRecording();}
            else moveCursor(e.getLocalX());updateReadout();e.consume();
        }
        @Override protected void controlMouseReleased(com.vingame.glswing.input.GLMouseEvent e){if(dragMode!=0){dragMode=0;e.consume();}}
        @Override protected void controlMouseCaptureLost(com.vingame.glswing.input.GLMouseEvent e){dragMode=0;}
        public Plot(boolean correlation){this(correlation,false);}
        public Plot(boolean correlation,boolean resource){
            this.correlation=correlation;this.resource=resource;addGL(caption);setFocusable(false);metrics();AudioAnalysis.listen(refresh);
        }
        private Plot root(){return source==null?this:source.root();}
        public Plot expandedCopy(){var copy=new Plot(correlation,resource);copy.source=root();copy.viewport=root().viewport;copy.cursorA=root().cursorA;copy.cursorB=root().cursorB;copy.refresh();return copy;}
        public void retainInspection(){var r=root();r.viewport=viewport;r.cursorA=cursorA;r.cursorB=cursorB;}
        private Host analysisHost(){
            for(var p=getGLParent();p!=null;p=p instanceof com.vingame.glswing.component.GLContainer c?c.getGLParent():null)if(p instanceof Host host)return host;return null;
        }
        public boolean openExpanded(){var host=analysisHost();if(host==null)return false;host.openAnalysis(root());return true;}
        public boolean isPaused(){return root().paused;}
        public void setPaused(boolean value){
            var r=root();if(r.paused==value)return;
            var frame=AudioAnalysis.frame(r.nodeId);if(value&&frame==null)return;
            r.paused=value;r.frozen=value?copy(frame):null;r.frozenAt=r.receivedAt;r.refresh();if(this!=r)refresh();
        }
        private static AudioAnalysis.Frame copy(AudioAnalysis.Frame f){return f==null?null:new AudioAnalysis.Frame(f.frequency(),f.confidence(),f.span(),f.symmetric(),f.values().clone());}
        public boolean hasFrame(){return displayedFrame()!=null;}
        public AudioAnalysis.Frame displayedFrame(){var r=root();return r.paused?r.frozen:AudioAnalysis.frame(r.nodeId);}
        public boolean isSpectrum(){var f=displayedFrame();return resource&&f!=null&&!f.symmetric();}
        public boolean isLogarithmic(){return root().logarithmic;}
        public void setLogarithmic(boolean value){var r=root();r.logarithmic=value;r.optionsChanged.run();r.refresh();if(r!=this)refresh();}
        public boolean isTriggersVisible(){return root().triggersVisible;}
        public void setTriggersVisible(boolean value){root().triggersVisible=value;root().refresh();if(source!=null)refresh();}
        private String options(){return (triggersVisible?"":"hide-triggers")+(logarithmic?";log-frequency":"");}
        private void options(String value){triggersVisible=!value.contains("hide-triggers");logarithmic=value.contains("log-frequency");refresh();}
        public void configure(double range,double low,double high){
            var r=root();double safe=Double.isFinite(range)&&range>0?range:1;
            if(r.range==safe&&r.low==low&&r.high==high)return;r.range=safe;r.low=low;r.high=high;r.refresh();
        }
        public double verticalRange(){return root().range;}
        public float ordinate(double value,float height){return (float)(height*(1-value/verticalRange())/2);}
        public void bind(String id){nodeId=id;refresh();}
        public void setPreview(boolean value){preview=value;caption.setVisible(!value);metrics();updateReadout();}
        private void metrics(){setIntrinsicPreferredGLSize(new GLSize(160,getGLTheme().values().getFloat(preview?"curve.previewHeight":"curve.height",preview?36:120)));}
        @Override protected void themeChanged(){super.themeChanged();metrics();}
        private void receive(){
            var r=root();var current=AudioAnalysis.frame(r.nodeId);
            if(current!=r.lastReceived){r.lastReceived=current;if(current!=null)r.receivedAt=java.time.Instant.now().toString();}
        }
        private void dataReceived(){receive();if(displayedFrame()!=lastDisplayed)redraw();}
        private void refresh(){receive();redraw();}
        private void redraw(){
            lastDisplayed=displayedFrame();
            updateReadout();
            invalidateRecording();
        }
        public String description(){
            var f=displayedFrame();if(f==null)return AppText.literal("Waiting for audio");
            String result=AppText.text(isPaused()?"analysis.frozen":"analysis.live")+" · "+f.values().length+" "+AppText.text("analysis.values");
            if(isSpectrum()){
                result+=" · Δf "+AnalysisChart.number(f.values().length>1?f.span()/(f.values().length-1):0)+" Hz";
                if(isLogarithmic()&&f.values().length>0)result+=" · DC = "+AnalysisChart.number(f.values()[0]);
            }else result+=" · "+AnalysisChart.number(f.span()*1000)+" ms";
            return result;
        }
        public AnalysisChart.Snapshot snapshot(){
            var r=root();var f=displayedFrame();
            // Scope span describes N sample intervals; the last sample is at (N-1)*dt.
            if(f!=null&&!resource&&!correlation&&f.values().length>0)f=new AudioAnalysis.Frame(f.frequency(),f.confidence(),f.span()*(f.values().length-1)/f.values().length,f.symmetric(),f.values());
            return new AnalysisChart.Snapshot(f,isSpectrum(),r.logarithmic,r.range,!correlation&&r.triggersVisible,r.low,r.high,r.paused?r.frozenAt:r.receivedAt,r.paused);
        }
        @Override protected void controlMouseClicked(com.vingame.glswing.input.GLMouseEvent e){
            if(e.getButton()==java.awt.event.MouseEvent.BUTTON1&&e.getOriginalEvent().getClickCount()==2){if(source==null&&openExpanded())e.consume();return;}
            if(e.getButton()!=java.awt.event.MouseEvent.BUTTON3)return;
            var host=getGLParent();while(host instanceof com.vingame.glswing.component.GLContainer p&&p.getGLParent()!=null)host=p.getGLParent();if(host==null)return;
            var menu=new GLPopupMenu();
            if(source!=null){menu.addItem(AppText.text("analysis.resetView"),a->resetView());
                if(Double.isFinite(cursorA))menu.addItem(AppText.text("analysis.clearCursorA"),a->clearCursor(true));
                if(Double.isFinite(cursorB))menu.addItem(AppText.text("analysis.clearCursorB"),a->clearCursor(false));
                if(Double.isFinite(cursorA)&&Double.isFinite(cursorB))menu.addItem(AppText.text("analysis.clearCursors"),a->clearCursors());}
            if(analysisHost()!=null&&source==null)menu.addItem(AppText.literal("Open enlarged graph"),a->openExpanded());
            menu.addItem(AppText.text(isPaused()?"analysis.resume":"analysis.pause"),a->setPaused(!isPaused()));
            if(isSpectrum())menu.addItem(AppText.text(isLogarithmic()?"analysis.linear":"analysis.log"),a->setLogarithmic(!isLogarithmic()));
            if(!correlation)menu.addItem(AppText.literal(isTriggersVisible()?"Hide trigger lines":"Show trigger lines"),a->{setTriggersVisible(!isTriggersVisible());root().optionsChanged.run();});
            menu.show(host,e.getScreenX(),e.getScreenY());e.consume();
        }
        @Override protected void controlMouseMoved(com.vingame.glswing.input.GLMouseEvent e){pointer=e.getLocalX();updateReadout();invalidateRecording();}
        @Override protected void controlMouseExited(com.vingame.glswing.input.GLMouseEvent e){pointer=-1;updateReadout();invalidateRecording();}
        // Update retained controls from input/data events, never while recording a frame.
        private void updateReadout(){
            var displayed=visibleSnapshot();double w=getWidth();
            double left=source!=null?78:0,right=source!=null?24:0;
            if(!preview&&pointer>=left&&pointer<w-right&&hasFrame()){
                var s=displayed;var f=s.frame();
                if(f!=null&&f.values().length>0){
                    double ratio=Math.max(0,Math.min(1,(pointer-left)/Math.max(1,w-left-right)));
                    double x=s.xAt(ratio);
                    int index=s.nearestIndex(x);
                    String readout=(isSpectrum()?"Bin "+index+" · "+Double.toString(s.sampleX(index))+" Hz":"i = "+index+" · "+AnalysisChart.number(Math.copySign(s.sampleX(index),x))+" ms")+" · y = "+Float.toString(f.values()[index]);
                    caption.setText(readout);
                    setToolTipText(readout);
                }
            }else {caption.setText(hasFrame()?description():AppText.literal("Waiting for audio"));setToolTipText(AppText.text(source!=null?"analysis.navigation":"analysis.hint"));}
            String measurements=displayed.measurements();if(!measurements.isEmpty()){caption.setText(measurements);if(pointer<left||pointer>=w-right)setToolTipText(measurements+" · "+AppText.text("analysis.navigation"));}
        }
        @Override protected void record(GLContext2D c){
            var displayed=visibleSnapshot();
            var style=getGLTheme().sliderStyle();int w=(int)getWidth(),h=(int)Math.max(1,getHeight()-(preview?0:22));
            c.fillUIRoundRect(0,0,w,getHeight(),style.cornerRadius(),style.background());
            var font=getGLFont(getGLTheme().labelStyle().font());
            var palette=new AnalysisChart.Palette(style.borderColor(),getGLTheme().labelStyle().textColor(),
                getGLTheme().values().getColor("easyknack.analysis.curve",com.vin.easyknack.ui.ApplicationTheme.current().color("analysis.curve")),
                getGLTheme().values().getColor("easyknack.analysis.triggerLow",com.vin.easyknack.ui.ApplicationTheme.current().color("analysis.triggerLow")),
                getGLTheme().values().getColor("easyknack.analysis.triggerHigh",com.vin.easyknack.ui.ApplicationTheme.current().color("analysis.triggerHigh")));
            var old=c.getStroke();
            try{
                AnalysisChart.draw(new AnalysisChart.Painter(){
                    public void line(double x1,double y1,double x2,double y2,Color color,float width){c.setStroke(new GLStroke(width).withWidthSpace(GLStroke.WidthSpace.SCREEN));c.drawLine((float)x1,(float)y1,(float)x2,(float)y2,color);}
                    public void text(String text,double x,double y,Color color){c.drawUIText(font,text,(float)x,(float)y,color);}
                    public double textWidth(String text){return font.measureText(text);}
                    public void clip(double x,double y,double width,double height){c.beginClip((float)x,(float)y,(float)width,(float)height);}
                    public void unclip(){c.endClip();}
                },displayed,w,h,source!=null,Math.max(.001,c.getTransform().getMaximumScale()),palette);
            }finally{c.setStroke(old);}
            caption.setBounds(4,h,Math.max(0,w-8),22);
        }
    }
}
