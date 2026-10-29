package com.vin.easyknack.nodes;

final class VectorSupport {
    static final PropertyId RATE=new PropertyId("rate");
    static void input(NodeBuilder n,PropertyId id,String label,double x,double y,double z){n.addInput(id,label,new VectorEditor(x,y,z)).dataType(PortDataType.VECTOR).signal(SignalCategory.VECTOR).rates(PortRates.AUDIO_CONTROL);}
    static void output(NodeBuilder n,PropertyId id){n.addOutput(id,"Vector").dataType(PortDataType.VECTOR).signal(SignalCategory.VECTOR).rates(PortRates.AUDIO_CONTROL);}
    static ParameterContract.Builder vector(ParameterContract.Builder b,PropertyId id,double x,double y,double z){
        return vector(b,id,x,y,z,s->true);
    }
    static ParameterContract.Builder vector(ParameterContract.Builder b,PropertyId id,double x,double y,double z,java.util.function.Predicate<NodeState> enabled){
        double[] defaults={x,y,z};b.source(id,enabled);
        for(int i=0;i<3;i++){int axis=i;b.mapped(new PropertyId(id.value()+"-"+"xyz".charAt(i)),s->Double.toString(VectorEditor.component(s,id,axis,defaults[axis])));}
        return b;
    }
}
