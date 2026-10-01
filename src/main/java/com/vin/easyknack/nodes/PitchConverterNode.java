package com.vin.easyknack.nodes;
import java.util.List;
@RegisterNode(id="easyknack.math.pitchconverter",title="Pitch Converter",category=NodeCategory.CONVERTER, kind=NodeKind.VALUE)
public final class PitchConverterNode extends UnitConverterNode {
    @Override protected List<UnitFormat> pool(){return List.of(UnitFormat.CENT,UnitFormat.SEMITONE,UnitFormat.OCTAVE,UnitFormat.FACTOR);}
    @Override protected UnitFormat initialFrom(){return UnitFormat.CENT;}
    @Override protected UnitFormat initialTo(){return UnitFormat.FACTOR;}
}
