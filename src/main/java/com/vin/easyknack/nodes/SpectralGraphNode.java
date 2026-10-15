package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.spectral.compiled",title="Spectral Group",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class SpectralGraphNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("out"),PROGRAM=new PropertyId("program");
    public static final int MAX_EXTERNAL=16, PROGRAM_TOKEN=MAX_EXTERNAL+1;
    public static PropertyId external(int i){return new PropertyId("external"+i);}
    public boolean isListedInCatalog(){return false;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        for(int i=0;i<(int)s.number("audioOutputs",0);i++)n.addOutput(new PropertyId("audio"+i),"Audio "+(i+1)).signal(SignalCategory.AUDIO).rate(ProcessingRate.AUDIO);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        for(int i=0;i<MAX_EXTERNAL;i++)n.addInput(external(i),"Frame value "+(i+1)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
    }
    private static String emptyProgram(){var output=new BinOutputNode();return hex("BIN1 1 binoutput !"+NativeContractGenerator.fingerprint(output)+" "+output.encode(new NodeState(output.defaults()),p->-1));}
    public static String hex(String program){return java.util.HexFormat.of().formatHex(program.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    public NativeProcessor processor(){var contract=ParameterContract.builder().source(IN);for(int i=0;i<MAX_EXTERNAL;i++)contract.source(external(i));return new NativeProcessor("spectralgraph","nodes/SpectralGraphNode.hpp",contract.mapped(PROGRAM,s->s.text(PROGRAM,emptyProgram())).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
