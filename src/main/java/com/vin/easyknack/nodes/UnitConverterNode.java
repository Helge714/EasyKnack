package com.vin.easyknack.nodes;
import java.util.*;
import com.vingame.glswing.node.model.GLNodeValue;
@RegisterNode(id="easyknack.math.unitconverter",title="Unit Converter",category=NodeCategory.MATH, kind=NodeKind.VALUE)
public final class UnitConverterNode extends BasicNode {
    @Override public boolean supportsSpectralBins(){return true;}
    @Override public boolean supportsSampleFeedback(){return true;}
    public static final PropertyId IN=new PropertyId("value"),OUT=new PropertyId("out"),FROM=new PropertyId("from"),TO=new PropertyId("to"),RATE=new PropertyId("rate");
    public static boolean canConvert(UnitFormat from,UnitFormat to){return from.compatible(to)||isPeriodic(from)&&isPeriodic(to);}
    private static boolean isPeriodic(UnitFormat unit){return unit.dimension().equals("time")||unit.dimension().equals("frequency");}
    public static List<UnitFormat> destinations(UnitFormat from){return Arrays.stream(UnitFormat.values()).filter(to->canConvert(from,to)).toList();}
    private UnitFormat from(NodeState s){return UnitFormat.fromId(s.text(FROM,"factor"));}
    private UnitFormat to(NodeState s){return UnitFormat.fromId(s.text(TO,"percent"));}
    @Override public NodeState normalizeState(NodeState s){
        var from=from(s);if(canConvert(from,to(s)))return s;
        var values=new HashMap<>(s.values());values.put(TO.value(),GLNodeValue.of(from.id()));return new NodeState(values,s.connectedInputs());
    }
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Out ["+to(s).suffix()+"]").formats(new UnitFormats(to(s),to(s),to(s).compatible())).unitProperty(TO).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addEditor(FROM,"From",Editor.comboBox(List.of(UnitFormat.values()),UnitFormat.FACTOR,UnitFormat::id).grouped(u->com.vin.easyknack.i18n.AppText.text("unit.group."+u.dimension())));
        n.addEditor(TO,"To",Editor.comboBox(destinations(from(s)),to(s),UnitFormat::id));
        n.addInput(IN,"Value ["+from(s).suffix()+"]",Editor.Slider.button(0)).formats(new UnitFormats(from(s),from(s),from(s).compatible())).unitProperty(FROM).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.i18n.AppText.text("unit.converter.period"));
        ProcessingRate.addEditor(n,RATE);
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("unitconvert","nodes/UnitConverterNode.hpp",
        ParameterContract.builder().source(IN).value(IN).mapped(FROM,s->from(s).id()).mapped(TO,s->{if(!canConvert(from(s),to(s)))throw new IllegalArgumentException("Incompatible unit dimensions");return to(s).id();}).build());}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
