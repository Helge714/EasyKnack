package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.vector.value",title="Vector",category=NodeCategory.VECTOR,kind=NodeKind.VECTOR)
public final class VectorNode extends BasicNode {
    public static final PropertyId VALUE=new PropertyId("position"),OUT=new PropertyId("vector");
    public void define(NodeBuilder n,NodeState s){VectorSupport.output(n,OUT);n.addEditor(VALUE,"Position",new VectorEditor(0,0,0)).editorCopyable(false);ProcessingRate.addEditor(n,VectorSupport.RATE);}
    public NativeProcessor processor(){return new NativeProcessor("vector","nodes/VectorNode.hpp",ParameterContract.builder()
        .mapped(new PropertyId("x"),s->Double.toString(VectorEditor.component(s,VALUE,0,0)))
        .mapped(new PropertyId("y"),s->Double.toString(VectorEditor.component(s,VALUE,1,0)))
        .mapped(new PropertyId("z"),s->Double.toString(VectorEditor.component(s,VALUE,2,0))).build());}
}
