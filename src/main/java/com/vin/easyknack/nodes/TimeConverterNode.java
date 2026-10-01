package com.vin.easyknack.nodes;
import java.util.List;
@RegisterNode(id="easyknack.math.timeconverter",title="Time Converter",category=NodeCategory.CONVERTER, kind=NodeKind.VALUE)
public final class TimeConverterNode extends UnitConverterNode {
    @Override protected List<UnitFormat> pool(){return List.of(UnitFormat.MS,UnitFormat.SECOND,UnitFormat.SAMPLE);}
    @Override protected UnitFormat initialFrom(){return UnitFormat.MS;}
    @Override protected UnitFormat initialTo(){return UnitFormat.SECOND;}
}
