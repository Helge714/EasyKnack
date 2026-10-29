package com.vin.easyknack.nodes;

import java.util.List;

@RegisterNode(id="easyknack.analysis.level",title="Level Detector",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class LevelAnalysisNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("audio-in"),PEAK=new PropertyId("peak"),RMS=new PropertyId("rms"),
        CHANNEL=new PropertyId("channel"),WINDOW=new PropertyId("window"),SMOOTH=new PropertyId("smooth"),
        ATTACK=new PropertyId("attack"),RELEASE=new PropertyId("release");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(PEAK,"Peak").formats(new UnitFormats(UnitFormat.FACTOR,UnitFormat.FACTOR,UnitFormats.GAIN_DB.choices())).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip("Absolute peak of the current block. Linear amplitude: 0 = silence, 1 = full scale; values above 1 are allowed. Optional smoothing follows the block peaks.");
        n.addOutput(RMS,"RMS").formats(new UnitFormats(UnitFormat.FACTOR,UnitFormat.FACTOR,UnitFormats.GAIN_DB.choices())).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip("Root mean square over the trailing window. Linear amplitude, updated once per block. A full-scale sine has RMS about 0.707. No frequency weighting.");
        n.addInput(IN,"Audio In").tooltip("Polyphonic voices are summed before measuring. Unconnected input is silence.");
        n.addEditor(CHANNEL,"Channel",Editor.comboBox(List.of("Left","Right","Mix"),"Left",v->v.toLowerCase(java.util.Locale.ROOT)))
            .tooltip("Mix measures (Left + Right) / 2. Mono keeps its level; opposite-phase channels cancel.");
        n.addEditor(WINDOW,"RMS window",Editor.Slider.button(50,.1,2000.).formats(UnitFormats.TIME_MS))
            .tooltip("Trailing RMS window. At startup only the samples received so far are measured. Changing the window or channel resets the measurement history.");
        n.addEditor(SMOOTH,"Smoothing",Editor.checkBox(false));
        n.addEditor(ATTACK,"Attack",Editor.Slider.button(10,0.,2000.).formats(UnitFormats.TIME_MS)).enabled(s.flag(SMOOTH,false))
            .tooltip("Response time for increasing levels. 0 means immediate. One time constant reaches about 63% of a level step.");
        n.addEditor(RELEASE,"Release",Editor.Slider.button(100,0.,2000.).formats(UnitFormats.TIME_MS)).enabled(s.flag(SMOOTH,false))
            .tooltip("Response time for decreasing levels. 0 means immediate. Applied independently to Peak and RMS after block measurement.");
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("levelanalysis","nodes/LevelAnalysisNode.hpp",
        ParameterContract.builder().source(IN).value(CHANNEL).value(WINDOW).value(SMOOTH).value(ATTACK).value(RELEASE).build());}
}
