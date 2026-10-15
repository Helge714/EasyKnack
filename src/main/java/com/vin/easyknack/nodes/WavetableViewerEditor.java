package com.vin.easyknack.nodes;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.input.GLMouseEvent;
import com.vingame.glswing.layout.GLSize;
import com.vingame.glswing.rendering.GLContext2D;

public final class WavetableViewerEditor implements Editor {
    public GLNodeValue defaultValue(){return GLNodeValue.of("");}
    public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){return new Control(new Plot(),v->{});}
    public static final class Plot extends GLControl {
        private String id="";private float[] samples;
        private AudioAnalysis.Frame lastFrame;
        private final Runnable refresh=this::refresh;
        public Plot(){metrics();AudioAnalysis.listen(refresh);}
        public void bind(String value){id=value;refresh();}
        private void metrics(){setIntrinsicPreferredGLSize(new GLSize(160,getGLTheme().values().getFloat("curve.height",120)));}
        @Override protected void themeChanged(){super.themeChanged();metrics();}
        private void refresh(){var frame=AudioAnalysis.frame(id);if(frame==lastFrame)return;lastFrame=frame;samples=frame==null?null:frame.values().clone();invalidateRecording();repaintGL();}
        @Override protected void record(GLContext2D c){SampleGraph.draw(this,c,samples);}
        @Override protected void controlMouseMoved(GLMouseEvent e){
            if(samples==null||samples.length==0)return;int i=(int)Math.round(Math.max(0,Math.min(1,(e.getLocalX()-9)/Math.max(1,getWidth()-18)))*(samples.length-1));
            setToolTipText("["+i+"] = "+Float.toString(samples[i]));
        }
        @Override protected void controlMouseExited(GLMouseEvent e){GLToolTipManager.leave(this);}
        @Override protected void controlMousePressed(GLMouseEvent e){
            if(e.getButton()!=3||samples==null||samples.length!=WaveformSamples.SIZE)return;
            var root=getGLParent();while(root instanceof com.vingame.glswing.component.GLContainer p&&p.getGLParent()!=null)root=p.getGLParent();
            if(root!=null){var menu=new GLPopupMenu();menu.addItem(NodeText.text("waveform.copy.resource","Copy wavetable resource"),a->WaveformSamples.copy(samples));menu.show(root,e.getScreenX(),e.getScreenY());}e.consume();
        }
    }
}
