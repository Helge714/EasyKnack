package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.spectral.compiled",title="Spectral Group",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class SpectralGraphNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("out"),PROGRAM=new PropertyId("program");
    public static final int DEFAULT_EXTERNAL=16;
    public static int inputCount(NodeState state){return ParameterContract.inputCount(state,"inputs",DEFAULT_EXTERNAL);}
    public static int programToken(NodeState state){return 2+inputCount(state);}
    public static PropertyId spectrumOutput(int i){return i==0?OUT:new PropertyId("spectrum"+i);}
    public static PropertyId external(int i){return new PropertyId("external"+i);}
    public boolean isListedInCatalog(){return false;}
    public void define(NodeBuilder n,NodeState s){
        for(int i=0;i<(int)s.number("spectrumOutputs",1);i++)n.addOutput(spectrumOutput(i),"Spectrum "+(i+1)).dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        for(int i=0;i<(int)s.number("audioOutputs",0);i++)n.addOutput(new PropertyId("audio"+i),"Audio "+(i+1)).signal(SignalCategory.AUDIO).rate(ProcessingRate.AUDIO);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        var triggers=java.util.Set.of(s.text("triggerSlots","").split(","));
        for(int i=0;i<inputCount(s);i++){
            var row=n.addInput(external(i),"Frame value "+(i+1));
            if(triggers.contains(Integer.toString(i)))row.signal(SignalCategory.TRIGGER).dataType(PortDataType.TRIGGER).rate(ProcessingRate.EVENT);
            else row.signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        }
    }
    private static String emptyProgram(){var output=new BinOutputNode();return hex("BIN1 1 binoutput !"+NativeContractGenerator.fingerprint(output)+" "+output.encode(new NodeState(output.defaults()),p->-1));}
    public static String hex(String program){return java.util.HexFormat.of().formatHex(program.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    public NativeProcessor processor(){var contract=ParameterContract.builder().source(IN);contract.sources(new PropertyId("inputs"),s->java.util.stream.IntStream.range(0,inputCount(s)).mapToObj(SpectralGraphNode::external).toList());return new NativeProcessor("spectralgraph","nodes/SpectralGraphNode.hpp",contract.mapped(PROGRAM,s->s.text(PROGRAM,emptyProgram())).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
