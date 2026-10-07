package com.vin.easyknack.nodes;
import java.util.*;
@RegisterNode(id="easyknack.spatial.receiver",title="Spatial Receiver",category=NodeCategory.EFFECT,kind=NodeKind.AUDIO)
public final class SpatialReceiverNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("audio-in"),OUT=new PropertyId("audio-out"),DISTANCE=new PropertyId("distance"),SOURCE=new PropertyId("source"),RECEIVER=new PropertyId("receiver"),ANGLE=new PropertyId("angle"),SPACING=new PropertyId("spacing"),MODE=new PropertyId("mode"),ATTENUATION=new PropertyId("attenuation"),REFERENCE=new PropertyId("reference"),PROPAGATION=new PropertyId("propagation");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Audio Out");
        n.addOutput(DISTANCE,"Distance [m]").signal(SignalCategory.VALUE).rate(ProcessingRate.fromId(s.text(ProcessingRate.EFFECTIVE+".output_1","control")));
        n.addEditor(MODE,"Channels",Editor.comboBox(List.of("mono","stereo"),"stereo",x->x));
        n.addInput(IN,"Audio In");VectorSupport.input(n,SOURCE,"Source Position",0,0,1);VectorSupport.input(n,RECEIVER,"Receiver Position",0,0,0);
        n.addInput(ANGLE,"Yaw",Editor.Slider.button(0).formats(new UnitFormats(UnitFormat.DEGREE,UnitFormat.DEGREE,List.of(UnitFormat.DEGREE,UnitFormat.RADIAN,UnitFormat.TURN)))).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        if(s.text(MODE,"stereo").equals("stereo"))n.addInput(SPACING,"Receiver Spacing",Editor.Slider.button(.18,0.0,null).formats(UnitFormats.LENGTH)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addEditor(ATTENUATION,"Distance Attenuation",Editor.checkBox(true));
        n.addEditor(REFERENCE,"Reference Distance",Editor.Slider.button(1,.001,null).formats(UnitFormats.LENGTH)).inspectorOnly();
        n.addEditor(PROPAGATION,"Sound Travel Time",Editor.checkBox(false)).inspectorOnly().tooltip(NodeText.text("spatial.travel.tooltip","343 m/s. Up to 2 seconds (686 m); more distant sources are silent when enabled. Moving sources change delay and pitch. This is not a room or HRTF simulation."));
    }
    @Override public Map<String,com.vingame.glswing.node.model.GLNodeValue> defaults(){var v=new HashMap<>(super.defaults());v.putIfAbsent(SPACING.value(),com.vingame.glswing.node.model.GLNodeValue.of(.18));return v;}
    public List<ProcessingRate> resolveAutomaticOutputRates(NodeState s,Map<PropertyId,ProcessingRate> in){var r=ProcessingRate.CONTROL;for(var id:List.of(SOURCE,RECEIVER,ANGLE,SPACING))r=ProcessingRate.maximum(r,in.getOrDefault(id,ProcessingRate.CONTROL));return List.of(ProcessingRate.AUDIO,r);}
    public NativeProcessor processor(){return new NativeProcessor("spatialreceiver","nodes/SpatialReceiverNode.hpp",VectorSupport.vector(VectorSupport.vector(ParameterContract.builder().source(IN),SOURCE,0,0,1),RECEIVER,0,0,0).source(ANGLE).value(ANGLE).source(SPACING,s->s.text(MODE,"stereo").equals("stereo")).value(SPACING).value(MODE).value(ATTENUATION).value(REFERENCE).value(PROPAGATION).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
