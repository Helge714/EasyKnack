package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.audio.freeverb",title="Freeverb",category=NodeCategory.EFFECT,kind=NodeKind.AUDIO)
public final class FreeverbNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("audio-in"),OUT=new PropertyId("audio-out"),
        ROOM=new PropertyId("roomSize"),DAMP=new PropertyId("damp"),WIDTH=new PropertyId("width"),
        WET=new PropertyId("wet"),DRY=new PropertyId("dry");
    @Override public void define(NodeBuilder node,NodeState state) {
        node.addOutput(OUT,"Audio Out");node.addInput(IN,"Audio In");
        node.addInput(ROOM,"Room Size",Editor.Slider.bar(.5,0,1).formats(UnitFormats.PROPORTION)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        node.addInput(DAMP,"Damping",Editor.Slider.bar(.5,0,1).formats(UnitFormats.PROPORTION)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        node.addInput(WIDTH,"Width",Editor.Slider.bar(1,0,1).formats(UnitFormats.PROPORTION)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        node.addInput(WET,"Wet",Editor.Slider.bar(1.0/3,0,1).formats(UnitFormats.PROPORTION)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        node.addInput(DRY,"Dry",Editor.Slider.bar(.5,0,1).formats(UnitFormats.PROPORTION)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
    }
    @Override public NativeProcessor processor() {
        return new NativeProcessor("freeverb","nodes/FreeverbNode.hpp",ParameterContract.builder().source(IN)
            .source(ROOM).value(ROOM).source(DAMP).value(DAMP).source(WIDTH).value(WIDTH)
            .source(WET).value(WET).source(DRY).value(DRY).build());
    }
    @Override public void mute(MuteRouting routing,NodeState state){routing.connect(IN,OUT);}
    @Override public void insert(InsertionRouting routing,NodeState state){routing.connect(IN,OUT);}
}
