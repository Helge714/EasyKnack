package com.vin.easyknack.nodes;


@RegisterNode(id="easyknack.audio.envelope",title="Envelope",category=NodeCategory.GENERATOR, kind=NodeKind.VALUE)
public final class EnvelopeNode extends BasicNode {
    @Override public com.vingame.glswing.node.GLNodeResizeMode resizeMode(){return com.vingame.glswing.node.GLNodeResizeMode.BOTH;}
    public static final PropertyId CURVE=new PropertyId("curve"),OUT=new PropertyId("audio-out"),GATE=new PropertyId("gate"),
            TRIGGER=new PropertyId("trigger"),DURATION=new PropertyId("duration"),DECAY=new PropertyId("decay"),RELEASE=new PropertyId("release"),SUSTAIN=new PropertyId("sustain-position"),HOLD=new PropertyId("hold");
    public static final PropertyId ATTACK=DURATION;
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Envelope").signal(SignalCategory.VALUE);
        n.addEditor(CURVE,"Envelope",new CurveEditor(CurveData.ADSR,true)).keepVisibleOnHide().growVertically();
        n.addInput(GATE,"Gate",Editor.Slider.bar(0,0,1)).signal(SignalCategory.VALUE).tooltip(com.vin.easyknack.i18n.AppText.literal("A rise above 0 starts Attack/Decay. While positive, holds at Sustain. Returning to 0 starts Release."));
        n.addInput(TRIGGER,"Trigger",new Editor.Trigger()).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER).rate(ProcessingRate.EVENT).tooltip(com.vin.easyknack.i18n.AppText.literal("Each trigger restarts the envelope, even while Gate remains high. Unconnected: click to trigger."));
        n.addInput(DURATION,"Attack",Editor.Slider.button(120,0.0,60000.0).step(1).formats(UnitFormats.TIME_MS)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(DECAY,"Decay",Editor.Slider.button(180,0.0,60000.0).step(1).formats(UnitFormats.TIME_MS)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(RELEASE,"Release",Editor.Slider.button(300,0.0,60000.0).step(1).formats(UnitFormats.TIME_MS)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addEditor(HOLD,"Hold",Editor.checkBox(true)).tooltip(com.vin.easyknack.i18n.AppText.text("envelope.hold.tooltip"));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("envelope", "nodes/EnvelopeNode.hpp",
            ParameterContract.builder().source(GATE).value(GATE).source(TRIGGER).value(TRIGGER).source(DURATION).value(DURATION).source(DECAY).value(DECAY).source(RELEASE).value(RELEASE).value(HOLD).curve(CURVE).build()); }
    @Override public void updateEditor(PropertyId id,com.vingame.glswing.component.GLContainer control,NodeState state){
        if(control instanceof com.vingame.glswing.ui.GLCurveEditor curve){var points=curve.getCurve().points();curve.setMarker(points.get(points.size()-2).x());}
    }
}
