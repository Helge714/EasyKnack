package com.vin.easyknack.nodes;
import java.util.List;
@RegisterNode(id="easyknack.math.frequencyconverter",title="Frequency Converter",category=NodeCategory.CONVERTER, kind=NodeKind.VALUE)
public final class FrequencyConverterNode extends UnitConverterNode {
    @Override protected List<UnitFormat> pool(){return List.of(UnitFormat.HZ,UnitFormat.KHZ,UnitFormat.BPM);}
    @Override protected UnitFormat initialFrom(){return UnitFormat.HZ;}
    @Override protected UnitFormat initialTo(){return UnitFormat.KHZ;}
}
