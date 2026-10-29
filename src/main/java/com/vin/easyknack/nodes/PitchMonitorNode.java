package com.vin.easyknack.nodes;

/** Inline telemetry: target is passed through unchanged, so a baked graph retains the monitor. */
@RegisterNode(id="easyknack.analysis.pitchmonitor",title="Pitch Monitor",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class PitchMonitorNode extends BasicNode {
    public static final PropertyId FREQUENCY=new PropertyId("frequency"),TARGET=new PropertyId("target"),CONFIDENCE=new PropertyId("confidence"),OUT=new PropertyId("out");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Target [Hz]").formats(UnitFormats.CLOCK).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addEditor(new PropertyId("readout"),"Pitch",new PitchReadoutEditor("")).keepVisibleOnHide();
        n.addInput(FREQUENCY,"Detected [Hz]").signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addInput(TARGET,"Target [Hz]").signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addInput(CONFIDENCE,"Confidence").signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
    }
    public NativeProcessor processor(){return new NativeProcessor("pitchmonitor","nodes/PitchMonitorNode.hpp",ParameterContract.builder().source(FREQUENCY).source(TARGET).source(CONFIDENCE).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(TARGET,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(TARGET,OUT);}
}
