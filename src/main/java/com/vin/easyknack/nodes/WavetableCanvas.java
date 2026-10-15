package com.vin.easyknack.nodes;

import com.vingame.glswing.ui.*;
import com.vingame.glswing.input.GLMouseEvent;
import com.vingame.glswing.rendering.*;
import java.util.function.Consumer;

/** Resource mode draws sample segments, never a fitted curve. Commit a pen stroke once. */
public final class WavetableCanvas extends GLCurveEditor {
    private float[] samples;
    private boolean drawing;
    private int lastIndex;
    private float lastValue;
    private Consumer<String> changed=v->{};
    public void onValue(Consumer<String> listener){changed=listener;}
    public boolean resourceMode(){return samples!=null;}
    public void value(String value){
        if(value.startsWith(WaveformSamples.PREFIX))samples=WaveformSamples.fromValue(value);
        else {samples=null;setCurve(WaveformEditor.periodic(CurveData.decode(value)));}
        invalidateRecording();repaintGL();
    }
    public void curveMode(){
        if(samples==null)return;var curve=WaveformSamples.curve(samples);samples=null;setCurve(curve);changed.accept(CurveData.encode(curve));invalidateRecording();
    }
    public void resourceModeFromCurve(){
        if(samples!=null)return;samples=WaveformSamples.fromValue(CurveData.encode(getCurve()));commit();
    }
    public void resource(float[] data){samples=data.clone();commit();}
    private void commit(){changed.accept(WaveformSamples.PREFIX+WaveformSamples.encode(samples));invalidateRecording();repaintGL();}
    public void actions(GLPopupMenu menu){
        menu.addItem(NodeText.text("waveform.mode.curve","Curve mode (64 points maximum)"),e->curveMode());
        menu.addItem(NodeText.text("waveform.mode.resource","Resource mode (draw samples)"),e->resourceModeFromCurve());
        menu.addItem(NodeText.text("waveform.copy.resource","Copy wavetable resource"),e->WaveformSamples.copy(samples!=null?samples:WaveformSamples.fromValue(CurveData.encode(getCurve()))));
        menu.addItem(NodeText.text("waveform.paste.resource","Paste wavetable resource"),e->{var data=WaveformSamples.copied();if(data!=null)resource(data);});
    }
    @Override protected void record(GLContext2D c){
        if(samples==null){super.record(c);return;}
        SampleGraph.draw(this,c,samples);
    }
    private void point(GLMouseEvent e,boolean interpolate){
        int index=(int)Math.round(Math.max(0,Math.min(1,(e.getLocalX()-9)/Math.max(1,getWidth()-18)))*(WaveformSamples.SIZE-1));
        float value=(float)Math.max(-1,Math.min(1,1-2*(e.getLocalY()-9)/Math.max(1,getHeight()-18)));
        if(interpolate&&index!=lastIndex){int lo=Math.min(index,lastIndex),hi=Math.max(index,lastIndex);for(int i=lo;i<=hi;i++)samples[i]=lastValue+(value-lastValue)*(i-lastIndex)/(index-lastIndex);}
        else samples[index]=value;
        lastIndex=index;lastValue=value;invalidateRecording();repaintGL();
    }
    @Override protected void controlMousePressed(GLMouseEvent e){
        if(samples==null){super.controlMousePressed(e);return;}
        if(isPreview()||!isEnabled())return;
        if(e.getButton()==3){var root=getGLParent();while(root instanceof com.vingame.glswing.component.GLContainer p&&p.getGLParent()!=null)root=p.getGLParent();if(root!=null){var menu=new GLPopupMenu();actions(menu);menu.show(root,e.getScreenX(),e.getScreenY());}e.consume();}
        else if(e.getButton()==1){drawing=true;point(e,false);e.consume();}
    }
    @Override protected void controlMouseDragged(GLMouseEvent e){if(samples==null){super.controlMouseDragged(e);return;}if(drawing){point(e,true);e.consume();}}
    @Override protected void controlMouseReleased(GLMouseEvent e){if(samples==null){super.controlMouseReleased(e);return;}if(drawing){drawing=false;commit();e.consume();}}
    @Override protected void controlMouseCaptureLost(GLMouseEvent e){if(drawing){drawing=false;commit();}else super.controlMouseCaptureLost(e);}
    @Override protected void controlMouseClicked(GLMouseEvent e){if(samples==null)super.controlMouseClicked(e);else e.consume();}
    @Override protected void controlKeyPressed(com.vingame.glswing.input.GLKeyEvent e){if(samples==null)super.controlKeyPressed(e);}
    @Override public void mouseWheelMoved(com.vingame.glswing.input.GLMouseWheelEvent e){if(samples==null)super.mouseWheelMoved(e);else e.consume();}
}
