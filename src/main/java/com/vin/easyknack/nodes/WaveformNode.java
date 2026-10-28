package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.waveform",title="Waveform",category=NodeCategory.GENERATOR,kind=NodeKind.WAVETABLE)
public final class WaveformNode extends BasicNode {
    public static final PropertyId CURVE=new PropertyId("curve"),OUT=new PropertyId("waveform");
    @Override public com.vingame.glswing.node.GLNodeResizeMode resizeMode(){return com.vingame.glswing.node.GLNodeResizeMode.BOTH;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Waveform").dataType(PortDataType.WAVEFORM).signal(SignalCategory.WAVETABLE).rate(ProcessingRate.CONTROL);
        n.addEditor(CURVE,"Waveform",new WaveformEditor()).keepVisibleOnHide().growVertically().editorCopyable(false);
    }
    @Override public NodeState normalizeState(NodeState state){
        if(!state.values().containsKey(CURVE.value())||state.text(CURVE,"").startsWith(WaveformSamples.PREFIX))return state;
        var values=new java.util.HashMap<>(state.values());values.put(CURVE.value(),com.vingame.glswing.node.model.GLNodeValue.of(CurveData.encode(WaveformEditor.periodic(CurveData.decode(state.text(CURVE,""))))));return new NodeState(values,state.connectedInputs());
    }
    public NativeProcessor processor(){return new NativeProcessor("waveform","nodes/WaveformNode.hpp",ParameterContract.builder().mapped(CURVE,s->WaveformSamples.encodedValue(s.text(CURVE,""))).build());}
}
