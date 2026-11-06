package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.partials.compiled",title="Partial Group",category=NodeCategory.SPECTRAL,kind=NodeKind.PARTIALS)
public final class PartialGraphNode extends BasicNode {
    public boolean isListedInCatalog(){return false;}
    public static PropertyId external(int i){return new PropertyId("external"+i);}
    public void define(NodeBuilder n,NodeState s){n.addOutput(new PropertyId("out"),"Partials").dataType(PortDataType.PARTIALS).signal(SignalCategory.PARTIALS).rate(ProcessingRate.CONTROL);n.addInput(new PropertyId("partials"),"Partials").dataType(PortDataType.PARTIALS).signal(SignalCategory.PARTIALS).rate(ProcessingRate.CONTROL);for(int i=0;i<(int)s.number("inputs",0);i++)n.addInput(external(i),"Frame value "+(i+1)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);}
    private static String empty(){var c=new PartialContextNode();var o=new PartialOutputNode();return SpectralGraphNode.hex("PAR1 2 partialcontext !"+NativeContractGenerator.fingerprint(c)+" "+c.encode(new NodeState(c.defaults()),p->-1)+" partialoutput !"+NativeContractGenerator.fingerprint(o)+" "+o.encode(new NodeState(o.defaults()),p->p.value().equals("frequency")?0:p.value().equals("amplitude")?256:512));}
    public NativeProcessor processor(){return new NativeProcessor("partialgraph","nodes/PartialGraphNode.hpp",ParameterContract.builder().source(new PropertyId("partials")).sources(new PropertyId("inputs"),s->java.util.stream.IntStream.range(0,(int)s.number("inputs",0)).mapToObj(PartialGraphNode::external).toList()).mapped(new PropertyId("program"),s->s.text("program",empty())).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(new PropertyId("partials"),new PropertyId("out"));}
    public void insert(InsertionRouting r,NodeState s){r.connect(new PropertyId("partials"),new PropertyId("out"));}
}
