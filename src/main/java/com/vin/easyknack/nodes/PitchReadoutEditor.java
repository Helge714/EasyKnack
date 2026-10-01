package com.vin.easyknack.nodes;

import java.util.Locale;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.rendering.GLContext2D;
import com.vingame.glswing.ui.GLLabel;

/** Optional relative telemetry path also works for generated source plugins. */
public record PitchReadoutEditor(String relativePath) implements Editor {
    public static final String GROUP_MONITOR="easyknack.pitchMonitor";
    public GLNodeValue defaultValue(){return GLNodeValue.of("");}
    public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){return new Control(new Readout(relativePath),v->{});}
    public static final class Readout extends GLContainer {
        private final String relativePath;
        private String id="";
        private final GLLabel detected=new GLLabel(com.vin.easyknack.ui.AppText.literal("Detected: --")),target=new GLLabel(com.vin.easyknack.ui.AppText.literal("Target: --")),confidence=new GLLabel(com.vin.easyknack.ui.AppText.literal("Confidence: --"));
        private final Runnable refresh=this::refresh;
        public Readout(String relativePath){
            this.relativePath=relativePath;addGL(detected);addGL(target);addGL(confidence);
            setPreferredGLSize(180,66);AudioAnalysis.listen(refresh);
        }
        public void bind(String nodeId){id=nodeId+(relativePath.isEmpty()?"":"/"+relativePath);refresh();}
        private void refresh(){
            var f=AudioAnalysis.frame(id);
            detected.setText(com.vin.easyknack.ui.AppText.literal("Detected: ")+(f==null?"--":pitch(f.frequency())));
            target.setText(com.vin.easyknack.ui.AppText.literal("Target: ")+(f==null||f.values().length==0?"--":pitch(f.values()[0])));
            confidence.setText(f==null?"Confidence: --":String.format(Locale.ROOT,"Confidence: %.0f %%",100*f.confidence()));
        }
        public String detectedText(){return detected.getText();}
        public String targetText(){return target.getText();}
        public String confidenceText(){return confidence.getText();}
        @Override protected void resized(float width,float height){float row=height/3;detected.setBounds(0,0,width,row);target.setBounds(0,row,width,row);confidence.setBounds(0,2*row,width,row);}
        @Override protected void record(GLContext2D context){}
    }
    public static String pitch(double hz){
        if(!Double.isFinite(hz)||hz<=0)return "--";
        int note=(int)Math.round(69+12*Math.log(hz/440)/Math.log(2));
        String name=new String[]{"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"}[Math.floorMod(note,12)]+(Math.floorDiv(note,12)-1);
        return String.format(Locale.ROOT,"%s / %.1f Hz",name,hz);
    }
}
