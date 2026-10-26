package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.valueaverage",title="Value Average",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class ValueAverageNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("in"),OUT=new PropertyId("out");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Mean").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO);
        AverageMeasurement.outputs(n);
        n.addInput(IN,"Value",Editor.Slider.button(0)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        AverageMeasurement.settings(n,s);
    }
    public NativeProcessor processor(){return new NativeProcessor("valueaverage","nodes/ValueAverageNode.hpp",AverageMeasurement.contract(IN,true));}
    @Override public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
