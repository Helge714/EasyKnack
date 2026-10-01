package com.vin.easyknack.nodes;
import java.util.List;
@RegisterNode(id="easyknack.math.phaseconverter",title="Phase Converter",category=NodeCategory.CONVERTER, kind=NodeKind.VALUE)
public final class PhaseConverterNode extends UnitConverterNode {
    @Override protected List<UnitFormat> pool(){return List.of(UnitFormat.TURN,UnitFormat.DEGREE,UnitFormat.RADIAN);}
    @Override protected UnitFormat initialFrom(){return UnitFormat.TURN;}
    @Override protected UnitFormat initialTo(){return UnitFormat.DEGREE;}
}
