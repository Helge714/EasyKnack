package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.multiple.compiled",title="Instance Group",category=NodeCategory.ROUTING,kind=NodeKind.AUDIO)
public final class MultipleGraphNode extends BasicNode {
    public static final int DEFAULT_INPUTS=8;
    public static int inputCount(NodeState state){return ParameterContract.inputCount(state,"inputs",DEFAULT_INPUTS);}
    public static final PropertyId COUNT=new PropertyId("count"),MAXIMUM=new PropertyId("maximum"),MODE=new PropertyId("mode"),PROGRAM=new PropertyId("program");
    public static PropertyId input(int i){return new PropertyId("input"+i);}
    public static PropertyId output(int i){return new PropertyId("output"+i);}
    public boolean isListedInCatalog(){return false;}
    public void define(NodeBuilder n,NodeState s){
        var spectrumOutputs=java.util.Set.of(s.text("spectrumOutputs","").split(","));
        for(int i=0;i<(int)s.number("outputs",1);i++){var row=n.addOutput(output(i),"Out "+(i+1));if(spectrumOutputs.contains(Integer.toString(i)))row.dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);}
        n.addInput(COUNT,"Count",Editor.Slider.button(8,0.0,256.0).integers().discrete()).rate(ProcessingRate.CONTROL).signal(SignalCategory.VALUE);
        var spectrum=java.util.Set.of(s.text("spectrumInputs","").split(","));
        var histories=java.util.Set.of(s.text("historyInputs","").split(","));
        var waveforms=java.util.Set.of(s.text("waveformInputs","").split(","));
        var vectors=java.util.Set.of(s.text("vectorInputs","").split(","));
        for(int i=0;i<inputCount(s);i++){
            var row=n.addInput(input(i),"In "+(i+1));
            if(histories.contains(Integer.toString(i)))row.dataType(PortDataType.HISTORY).signal(SignalCategory.HISTORY).rate(ProcessingRate.CONTROL);
            else if(waveforms.contains(Integer.toString(i)))row.dataType(PortDataType.WAVEFORM).signal(SignalCategory.WAVETABLE).rate(ProcessingRate.CONTROL);
            else if(spectrum.contains(Integer.toString(i)))row.dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
            else if(vectors.contains(Integer.toString(i)))row.dataType(PortDataType.VECTOR).signal(SignalCategory.VECTOR).rates(PortRates.AUDIO_CONTROL);
            else row.rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        }
        n.addEditor(MAXIMUM,"Maximum instances",Editor.Slider.button(16,1.0,256.0).integers().discrete());
        n.addEditor(MODE,"Merge",Editor.comboBox(java.util.List.of("sum","mean"),"sum",v->v));
    }
    public NativeProcessor processor(){var b=ParameterContract.builder().source(COUNT).integer(COUNT).integer(MAXIMUM).value(MODE);b.sources(new PropertyId("inputs"),s->java.util.stream.IntStream.range(0,inputCount(s)).mapToObj(MultipleGraphNode::input).toList());
        return new NativeProcessor("multiplegraph","nodes/MultipleGraphNode.hpp",b.mapped(PROGRAM,s->s.text(PROGRAM,emptyProgram())).build());}
    private static String emptyProgram(){
        var value=new ValueNode();var output=new MultipleOutputNode();
        return SpectralGraphNode.hex("MULT1 0 1 1 GRAPH 2 silence value !"+NativeContractGenerator.fingerprint(value)+" "+value.encode(new NodeState(value.defaults()),p->-1)
            +" @ c - out multipleoutput !"+NativeContractGenerator.fingerprint(output)+" "+output.encode(new NodeState(output.defaults()),p->0)+" @ c c");
    }
}
