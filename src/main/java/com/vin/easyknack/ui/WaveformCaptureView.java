package com.vin.easyknack.ui;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.ui.GLButton;
public final class WaveformCaptureView extends BasicNodeView {
    private String saved="empty";
    private GLButton copy;
    private AudioAnalysis.Frame lastFrame;
    private final Runnable refresh=this::refreshCapture;
    public WaveformCaptureView(BasicNode definition,NodeViewContext context,GLGraphNode model){
        super(definition,context,model);saved=new NodeState(model.properties()).text(SpectrumWaveformNode.SAVED,"empty");
        onStateChanged(s->{saved=s.text(SpectrumWaveformNode.SAVED,"empty");copy.setEnabled(!saved.equals("empty"));});copy.setEnabled(!saved.equals("empty"));AudioAnalysis.listen(refresh);
    }
    @Override protected GLContainer control(NodeBuilder.Row row){
        if(row.key().equals(SpectrumWaveformNode.SAVED))return new com.vingame.glswing.ui.GLLabel("");
        if(row.key().equals(SpectrumWaveformNode.COPY)){
            copy=new GLButton(row.label());copy.setEnabled(false);copy.addActionListener(e->{if(!saved.equals("empty")){float[] values=new float[2048];for(int i=0;i<values.length;i++)values[i]=Float.intBitsToFloat((int)Long.parseLong(saved.substring(i*8,i*8+8),16));WaveformEditor.copyCapture(values);}});return copy;
        }
        return super.control(row);
    }
    private void refreshCapture(){
        var frame=AudioAnalysis.frame(nodeId().toString());if(frame==null||frame==lastFrame||frame.values().length!=2048)return;lastFrame=frame;
        copy.setEnabled(true);StringBuilder hex=new StringBuilder(16384);
        for(float v:frame.values())hex.append(String.format(java.util.Locale.ROOT,"%08x",Float.floatToIntBits(v)));
        String next=hex.toString();if(!next.equals(saved)){saved=next;write(SpectrumWaveformNode.SAVED,GLNodeValue.of(next));}
    }
}
