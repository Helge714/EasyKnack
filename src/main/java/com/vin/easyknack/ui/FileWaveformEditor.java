package com.vin.easyknack.ui;
import com.vin.easyknack.audio.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.input.GLMouseEvent;
import com.vingame.glswing.rendering.*;
import com.vingame.glswing.layout.GLSize;
import com.vingame.glswing.node.model.GLNodeValue;
import java.util.concurrent.*;
/** Waveform edits commit on release: dragging never repeatedly reloads large buffers. */
public record FileWaveformEditor() implements Editor {
 public GLNodeValue defaultValue(){return GLNodeValue.of("");}
 public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){return new Control(new Plot(),v->{});}
 public static final class Plot extends GLControl implements com.vingame.glswing.input.GLMouseWheelCaptureTarget, com.vingame.glswing.input.GLMousePressCaptureTarget {
  @Override public boolean capturesMousePress(int button){return button==java.awt.event.MouseEvent.BUTTON2;}
  private static final ExecutorService WORKER=Executors.newSingleThreadExecutor(r->{var t=new Thread(r,"file-waveform");t.setDaemon(true);return t;});
  private String reference=null,message="";private FileWaveform waveform;private volatile long generation;
  private double viewStart=0,viewEnd=1,panX;private boolean snapping;
  private double start,end,length=1;private int dragging;private double pending;private long edits;
  private java.util.function.BiConsumer<Boolean,Double> changed;
  public Plot(){setFocusable(false);metrics();setToolTipText(AppText.text("buffer.waveformNavigation"));
  }
  @Override protected void controlMouseClicked(GLMouseEvent e){
   if(e.getButton()!=3)return;var host=getGLParent();while(host instanceof com.vingame.glswing.component.GLContainer p&&p.getGLParent()!=null)host=p.getGLParent();if(host==null)return;var menu=new GLPopupMenu();
menu.addItem(AppText.text("buffer.showAll"),action->view(0,1));
   menu.addItem(AppText.text("buffer.showSelection"),action->view(start/length,end/length));
   menu.addItem(AppText.text("buffer.audition"),action->{var path=AudioFileDecoder.cached(reference);if(path!=null)FileAudition.play(path,start,end,error->{if(error!=null)setToolTipText(AppText.text("buffer.auditionError")+error.getMessage());});});
   menu.addItem(AppText.text("buffer.auditionStop"),action->FileAudition.stop());
   menu.show(host,e.getScreenX(),e.getScreenY());e.consume();
  }
  public boolean hasWaveform(){return waveform!=null;}
  public void onEdit(java.util.function.BiConsumer<Boolean,Double> changed){this.changed=changed;}
  private void metrics(){setIntrinsicPreferredGLSize(new GLSize(160,getGLTheme().values().getFloat("curve.height",120)));}
  @Override protected void themeChanged(){super.themeChanged();metrics();}
  public void configure(String ref,double first,double last,double duration){
   start=first;end=last;length=duration;
   if(!java.util.Objects.equals(reference,ref)){dragging=0;reference=ref;viewStart=0;viewEnd=1;loadView();}
   invalidateRecording();repaintGL();
  }
  public void setSnapping(boolean value){snapping=value;}
  public double viewStart(){return viewStart;}
  public double viewEnd(){return viewEnd;}
  public void view(double first,double last){
   double span=Math.min(1,Math.max(1/(length*48000),last-first));viewStart=Math.max(0,Math.min(1-span,first));viewEnd=viewStart+span;loadView();
  }
  private void loadView(){
   waveform=null;long token=++generation;double first=viewStart,last=viewEnd;message=AppText.text("buffer.waveformLoading");
   var path=reference==null?null:AudioFileDecoder.cached(reference);
   if(path==null)message=AppText.text("player.choose");else CompletableFuture.supplyAsync(()->{try{return FileWaveform.read(path,first,last,()->generation!=token);}catch(Exception ex){throw new CompletionException(ex);}},WORKER).whenComplete((data,error)->java.awt.EventQueue.invokeLater(()->{if(generation!=token)return;waveform=data;message=error==null?"":AppText.text("buffer.waveformError");invalidateRecording();repaintGL();}));
   invalidateRecording();repaintGL();
  }
  @Override public void mouseWheelMoved(com.vingame.glswing.input.GLMouseWheelEvent e){
   double span=viewEnd-viewStart;if(e.getOriginalEvent().isShiftDown()){double shift=e.getPreciseWheelRotation()*span*.1;view(viewStart+shift,viewEnd+shift);}
   else{double anchor=fraction(e.getLocalX()),size=Math.min(1,Math.max(1/(length*48000),span*Math.pow(1.25,e.getPreciseWheelRotation())));double point=viewStart+anchor*span;view(point-anchor*size,point+(1-anchor)*size);}e.consume();
  }  private double fraction(float x){return Math.max(0,Math.min(1,(x-6)/Math.max(1,getWidth()-12)));}
  private void move(float x){double value=Math.round((viewStart+fraction(x)*(viewEnd-viewStart))*length*48000)/48000.;pending=dragging==1?Math.max(0,Math.min(end-1./48000,value)):Math.min(length,Math.max(start+1./48000,value));invalidateRecording();repaintGL();}
  @Override protected void controlMousePressed(GLMouseEvent e){
   if(e.getButton()==2||e.getButton()==1&&e.getOriginalEvent().isShiftDown()){dragging=3;panX=e.getLocalX();e.consume();return;}
   if(e.getButton()!=1||waveform==null||changed==null)return;double value=(viewStart+fraction(e.getLocalX())*(viewEnd-viewStart))*length;++edits;dragging=Math.abs(value-start)<=Math.abs(value-end)?1:2;move(e.getLocalX());e.consume();
  }
  @Override protected void controlMouseDragged(GLMouseEvent e){if(dragging==3){double shift=-(e.getLocalX()-panX)/Math.max(1,getWidth()-12)*(viewEnd-viewStart);panX=e.getLocalX();view(viewStart+shift,viewEnd+shift);e.consume();}else if(dragging!=0){move(e.getLocalX());e.consume();}}
  @Override protected void controlMouseReleased(GLMouseEvent e){
   if(dragging==3){dragging=0;e.consume();return;}if(dragging==0)return;
   long edit=edits;boolean first=dragging==1;double value=pending,oldStart=start,oldEnd=end;String ref=reference;dragging=0;
   var path=AudioFileDecoder.cached(ref);
   if(!snapping||path==null)changed.accept(first,value);
   else CompletableFuture.supplyAsync(()->{try{return FileWaveform.snap(path,value,first?0:oldStart+1./48000,first?oldEnd-1./48000:length);}catch(Exception ex){return value;}},WORKER).thenAccept(result->java.awt.EventQueue.invokeLater(()->{if(edit==edits&&java.util.Objects.equals(ref,reference)&&start==oldStart&&end==oldEnd&&dragging==0)changed.accept(first,result);}));
   invalidateRecording();repaintGL();e.consume();
  }  @Override protected void controlMouseCaptureLost(GLMouseEvent e){dragging=0;invalidateRecording();repaintGL();}
  static void paintSelection(GLContext2D c,float x1,float x2,float left,float width,float top,float height){
   float first=Math.max(left,x1),last=Math.min(left+width,x2),visible=last-first;
   if(!Float.isFinite(first)||!Float.isFinite(visible)||!Float.isFinite(top)||!Float.isFinite(height)||visible<=0||height<=0)return;
   c.fillUIRoundRect(first,top,visible,height,0,ApplicationTheme.current().color("buffer.selection"));
  }  static String fitText(String text,double width,java.util.function.ToDoubleFunction<String> measure){
   if(width<=0)return "";if(measure.applyAsDouble(text)<=width)return text;
   String suffix="…";if(measure.applyAsDouble(suffix)>width)return "";
   int low=0,high=text.length();while(low<high){int mid=(low+high+1)/2;if(measure.applyAsDouble(text.substring(0,mid)+suffix)<=width)low=mid;else high=mid-1;}
   return text.substring(0,low)+suffix;
  }  @Override protected void record(GLContext2D c){
   float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;c.beginClip(0,0,w,h);
   try{var style=getGLTheme().sliderStyle();c.fillUIRoundRect(0,0,w,h,style.cornerRadius(),style.background());
   var font=getGLFont(getGLTheme().labelStyle().font());var text=getGLTheme().labelStyle().textColor();
   if(waveform==null){c.drawUIText(font,fitText(message,w-12,font::measureText),6,h/2,text);return;}
   float left=6,width=Math.max(1,w-12),top=18,height=Math.max(1,h-36);double a=dragging==1?pending:start,b=dragging==2?pending:end;
   float x1=left+(float)((a/length-viewStart)/(viewEnd-viewStart))*width,x2=left+(float)((b/length-viewStart)/(viewEnd-viewStart))*width;
   paintSelection(c,x1,x2,left,width,top,height);
   var old=c.getStroke();c.setStroke(new GLStroke(1).withWidthSpace(GLStroke.WidthSpace.SCREEN));
   try{int buckets=waveform.low()[0].length,pixels=Math.max(1,(int)width);
    for(int ch=0;ch<waveform.channels();ch++){float center=top+height*(ch+.5f)/waveform.channels(),scale=height/waveform.channels()*.46f;c.drawLine(left,center,left+width,center,style.borderColor());
     for(int x=0;x<pixels;x++){int from=x*buckets/pixels,to=Math.min(buckets,Math.max(from+1,(x+1)*buckets/pixels));float low=1,high=-1;for(int i=from;i<to;i++){low=Math.min(low,waveform.low()[ch][i]);high=Math.max(high,waveform.high()[ch][i]);}c.drawLine(left+x,center-Math.max(-1,Math.min(1,high))*scale,left+x,center-Math.max(-1,Math.min(1,low))*scale,style.progressLightColor());}
    }
    c.setStroke(new GLStroke(2).withWidthSpace(GLStroke.WidthSpace.SCREEN));if(x1>=left&&x1<=left+width)c.drawLine(x1,top,x1,top+height,ApplicationTheme.current().color("buffer.boundary"));if(x2>=left&&x2<=left+width)c.drawLine(x2,top,x2,top+height,ApplicationTheme.current().color("buffer.boundary"));
   }finally{c.setStroke(old);}
   c.drawUIText(font,fitText(String.format(java.util.Locale.ROOT,"%.3f – %.3f s",a,b),w-12,font::measureText),6,12,text);
   c.drawUIText(font,fitText(String.format(java.util.Locale.ROOT,"%.6f – %.6f s",viewStart*length,viewEnd*length),w-12,font::measureText),6,h-4,text);
   }finally{c.endClip();}
  }
 }
}
