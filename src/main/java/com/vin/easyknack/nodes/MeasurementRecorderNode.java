package com.vin.easyknack.nodes;

import com.vin.easyknack.ui.MeasurementTable;
import com.vin.easyknack.ui.MeasurementRecorderPanel;
import com.vingame.glswing.node.model.GLNodeValue;

@RegisterNode(id="easyknack.analysis.recorder",title="Measurement Recorder",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class MeasurementRecorderNode extends BasicNode {
    public static final PropertyId COUNT=new PropertyId("inputs"),RECORD=new PropertyId("record"),TABLE=new PropertyId("table");
    public static PropertyId input(int i){return new PropertyId("value-"+i);}
    public static int count(NodeState s){return Math.max(1,Math.min(8,(int)s.number(COUNT,1)));}
    public void define(NodeBuilder n,NodeState s){
        n.addEditor(TABLE,"Measurements",new TableEditor()).keepVisibleOnHide().editorCopyable(false);
        n.addInput(RECORD,"Record",new Editor.Trigger()).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER).rate(ProcessingRate.EVENT)
            .tooltip("Append one row with all current input values at the trigger's sample position. Measurements are saved in this document.");
        n.addEditor(COUNT,"Inputs",Editor.Slider.button(1,1.,8.).integers()).editorCopyable(false);
        for(int i=0;i<count(s);i++){
            String name=s.text("name-"+i,NodeText.text("easyknack.analysis.recorder.value","Value")+" "+(i+1));
            n.addInput(input(i),name,Editor.Slider.button(0)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
            n.addEditor(new PropertyId("name-"+i),name,new ViewerNameEditor(name)).inspectorOnly();
        }
    }
    @Override public void updateEditor(PropertyId id,com.vingame.glswing.component.GLContainer control,NodeState state){
        if(control instanceof MeasurementRecorderPanel panel)panel.configure(state);
    }
    private static final ParameterContract CONTRACT=contract();
    private static ParameterContract contract(){
        var b=ParameterContract.builder().source(RECORD).integer(COUNT)
            .mapped(new PropertyId("generation"),s->MeasurementTable.generation(s.values().get(TABLE.value())));
        for(int i=0;i<8;i++){final int p=i;b.source(input(i),s->p<count(s)).value(input(i));}
        return b.build();
    }
    // Inactive numeric inputs still need fallbacks in the fixed native contract.
    @Override public java.util.Map<String,GLNodeValue> defaults(){
        var defaults=new java.util.HashMap<>(super.defaults());
        for(int i=0;i<8;i++)defaults.putIfAbsent(input(i).value(),GLNodeValue.of(0.));return java.util.Map.copyOf(defaults);
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("measurementrecorder","nodes/MeasurementRecorderNode.hpp",CONTRACT);}
    private record TableEditor() implements Editor {
        public GLNodeValue defaultValue(){return MeasurementTable.empty();}
        public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
            var panel=new MeasurementRecorderPanel(changed);return new Control(panel,panel::update);
        }
    }
}
