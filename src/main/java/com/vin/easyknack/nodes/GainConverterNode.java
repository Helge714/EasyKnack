package com.vin.easyknack.nodes;
import java.util.List;
@RegisterNode(id="easyknack.math.gainconverter",title="Gain Converter",category=NodeCategory.CONVERTER, kind=NodeKind.VALUE)
public final class GainConverterNode extends UnitConverterNode {
    @Override protected List<UnitFormat> pool(){return List.of(UnitFormat.FACTOR,UnitFormat.PERCENT,UnitFormat.DB);}
    @Override protected UnitFormat initialFrom(){return UnitFormat.FACTOR;}
    @Override protected UnitFormat initialTo(){return UnitFormat.PERCENT;}
}
