package com.vin.easyknack.nodes;

@RegisterNode(id = "easyknack.audio.value", title="Value Input", category=NodeCategory.INPUT, kind=NodeKind.VALUE)
public final class ValueNode extends BasicNode {
    public enum SliderStyle {
        BUTTONS, BAR;
        public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
        @Override public String toString(){return NodeText.text("value.slider."+id(),this==BAR?"Bar":"Buttons");}
    }
    @Override public java.util.Map<PropertyId,Integer> manualControlPorts(){return java.util.Map.of(VALUE,0);}
    @Override public boolean supportsSpectralBins() { return true; }
    @Override public boolean supportsSampleFeedback(){return true;}

    public static final PropertyId VALUE = new PropertyId("value"), OUT = new PropertyId("value-out");
    public static final PropertyId STYLE=new PropertyId("slider-style"),MIN=new PropertyId("minimum"),MAX=new PropertyId("maximum"),
        USE_MIN=new PropertyId("use-minimum"),USE_MAX=new PropertyId("use-maximum");
    private static boolean bar(NodeState s){return s.text(STYLE,"buttons").equals("bar");}
    @Override public NodeState normalizeState(NodeState state){
        var values=new java.util.HashMap<>(state.values());
        if(bar(state)){values.put(USE_MIN.value(),com.vingame.glswing.node.model.GLNodeValue.of(true));values.put(USE_MAX.value(),com.vingame.glswing.node.model.GLNodeValue.of(true));}
        boolean lower=bar(state)||state.flag(USE_MIN,false),upper=bar(state)||state.flag(USE_MAX,false);
        double min=state.number(MIN,0),max=state.number(MAX,1);
        if(lower&&upper&&max<=min){max=min+Math.max(1,Math.abs(min)*1e-6);values.put(MAX.value(),com.vingame.glswing.node.model.GLNodeValue.of(max));}
        double value=state.number(VALUE,0);
        if(lower)value=Math.max(min,value);if(upper)value=Math.min(max,value);
        if(values.containsKey(VALUE.value()))values.put(VALUE.value(),com.vingame.glswing.node.model.GLNodeValue.of(value));
        return new NodeState(values,state.connectedInputs());
    }
    @Override public void define(NodeBuilder node, NodeState state) {
        node.addOutput(OUT, "Value Out").rate(ProcessingRate.CONTROL).signal(SignalCategory.VALUE);
        boolean bar=bar(state),lower=bar||state.flag(USE_MIN,false),upper=bar||state.flag(USE_MAX,false);
        double min=state.number(MIN,0),max=state.number(MAX,1),initial=0;
        if(lower)initial=Math.max(min,initial);if(upper)initial=Math.min(max,initial);
        node.addEditor(VALUE, "Value",bar?Editor.Slider.bar(initial,min,max):Editor.Slider.button(initial,lower?min:null,upper?max:null));
        node.addEditor(STYLE,"Slider type",Editor.comboBox(java.util.List.of(SliderStyle.values()),SliderStyle.BUTTONS,SliderStyle::id)).inspectorOnly();
        node.addEditor(USE_MIN,"Enable minimum",new Editor.CheckBox(bar)).enabled(!bar).inspectorOnly();
        node.addEditor(MIN,"Minimum",Editor.Slider.button(0)).enabled(lower).inspectorOnly();
        node.addEditor(USE_MAX,"Enable maximum",new Editor.CheckBox(bar)).enabled(!bar).inspectorOnly();
        node.addEditor(MAX,"Maximum",Editor.Slider.button(1)).enabled(upper).inspectorOnly();
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("value", "nodes/ValueNode.hpp",
            ParameterContract.builder().value(VALUE).build()); }
}

