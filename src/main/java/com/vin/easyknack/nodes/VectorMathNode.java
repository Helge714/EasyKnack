package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.vector.math",title="Vector Math",category=NodeCategory.VECTOR,kind=NodeKind.VECTOR)
public final class VectorMathNode extends GenericVectorNode {
    @Override public void insert(InsertionRouting routing,NodeState state){routing.connect(A,OUT);}
    public static final PropertyId A=new PropertyId("a"),B=new PropertyId("b"),FACTOR=new PropertyId("factor"),OP=new PropertyId("operation"),OUT=new PropertyId("vector");
    public enum Operation {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, SCALE, NORMALIZE, NEGATE, ABSOLUTE, MINIMUM, MAXIMUM, CROSS;
        public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
        public String toString(){return NodeText.text("vector.operation."+id(),id());}
    }
    private static boolean binary(NodeState s){return !java.util.Set.of("scale","normalize","negate","absolute").contains(s.text(OP,"add"));}
    public void define(NodeBuilder n,NodeState s){
        VectorSupport.output(n,OUT);
        n.addEditor(OP,"Operation",Editor.comboBox(java.util.List.of(Operation.values()),Operation.ADD,Operation::id));
        VectorQuantity.input(n,A,"A",s);
        if(binary(s))VectorQuantity.input(n,B,"B",s);
        if(s.text(OP,"add").equals("scale"))n.addInput(FACTOR,"Factor",Editor.Slider.button(1)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        VectorQuantity.addEditor(n);ProcessingRate.addEditor(n,VectorSupport.RATE);
    }
    @Override public java.util.Map<String,com.vingame.glswing.node.model.GLNodeValue> defaults(){var values=new java.util.HashMap<>(super.defaults());values.putIfAbsent(FACTOR.value(),com.vingame.glswing.node.model.GLNodeValue.of(1));return values;}
    public NativeProcessor processor(){return new NativeProcessor("vectormath","nodes/VectorMathNode.hpp",VectorSupport.vector(VectorSupport.vector(ParameterContract.builder(),A,0,0,0),B,0,0,0,VectorMathNode::binary).source(FACTOR,s->s.text(OP,"add").equals("scale")).value(FACTOR).value(OP).build());}
}
