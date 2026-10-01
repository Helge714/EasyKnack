package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;
@RegisterNode(id="easyknack.math.unitconverter",title="Unit Converter",category=NodeCategory.CONVERTER, kind=NodeKind.VALUE)
public class UnitConverterNode extends BasicNode {
    @Override public boolean supportsSpectralBins() { return true; }
    @Override public boolean supportsSampleFeedback(){return true;}
    public static final PropertyId IN=new PropertyId("value"),OUT=new PropertyId("out"),FROM=new PropertyId("from"),TO=new PropertyId("to"),RATE=new PropertyId("rate");
    @Override public boolean isListedInCatalog(){return getClass()!=UnitConverterNode.class;}
    protected List<UnitFormat> pool(){return List.of(UnitFormat.values());}
    protected UnitFormat initialFrom(){return UnitFormat.FACTOR;}
    protected UnitFormat initialTo(){return UnitFormat.PERCENT;}
    private UnitFormat from(NodeState s){return UnitFormat.fromId(s.text(FROM,initialFrom().id()));}
    private UnitFormat to(NodeState s){return UnitFormat.fromId(s.text(TO,initialTo().id()));}
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Out ["+to(s).suffix()+"]").rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addEditor(FROM,"From",Editor.comboBox(pool(),initialFrom(),UnitFormat::id));
        n.addEditor(TO,"To",Editor.comboBox(pool(),initialTo(),UnitFormat::id));
        n.addInput(IN,"Value ["+from(s).suffix()+"]",Editor.Slider.button(0)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        ProcessingRate.addEditor(n,RATE);
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("unitconvert", "nodes/UnitConverterNode.hpp",
            ParameterContract.builder().source(IN).value(IN).mapped(FROM,s -> from(s).id()).mapped(TO,s -> {if(!from(s).compatible(to(s)))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Incompatible unit dimensions"));return to(s).id();}).build()); }
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
