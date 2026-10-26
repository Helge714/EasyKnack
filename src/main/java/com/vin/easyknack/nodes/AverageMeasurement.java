package com.vin.easyknack.nodes;

import java.util.List;

/** Shared acquisition settings; configuration is latched by the next Start. */
final class AverageMeasurement {
    static final PropertyId START=new PropertyId("start"),DURATION=new PropertyId("duration"),
        COUNT=new PropertyId("measurements"),MODE=new PropertyId("mode"),FINISHED=new PropertyId("finished"),
        SAMPLES=new PropertyId("count");
    static void outputs(NodeBuilder n){
        n.addOutput(SAMPLES,"Count").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip("Number of accepted measurements in the current acquisition.");
        n.addOutput(FINISHED,"Finished").signal(SignalCategory.TRIGGER).dataType(PortDataType.TRIGGER).rate(ProcessingRate.EVENT);
    }
    static void settings(NodeBuilder n,NodeState s){
        n.addInput(START,"Start",new Editor.Trigger()).signal(SignalCategory.TRIGGER).dataType(PortDataType.TRIGGER).rate(ProcessingRate.EVENT).tooltip("Start or restart one measurement. The final mean is held until the next Start.");
        n.addEditor(MODE,"Measure by",Editor.comboBox(List.of("Time","Measurements"),"Time",v->v.toLowerCase(java.util.Locale.ROOT)));
        n.addEditor(DURATION,"Duration",Editor.Slider.button(1,1.0/com.vin.easyknack.audio.AudioProcessing.SAMPLE_RATE,86400.0).formats(UnitFormats.TIME_SECONDS)).visible(s.text(MODE,"time").equals("time")).tooltip("Audio-clock duration. Settings take effect on the next Start.");
        n.addEditor(COUNT,"Measurements",Editor.Slider.button(16,1.0,16777216.0).integers()).visible(s.text(MODE,"time").equals("measurements"));
    }
    static ParameterContract contract(PropertyId input){return contract(input,false);}
    static ParameterContract contract(PropertyId input,boolean numeric){
        var contract=ParameterContract.builder().source(input);if(numeric)contract.value(input);
        return contract.source(START).value(MODE).value(DURATION).integer(COUNT).build();
    }
}
