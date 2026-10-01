package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.spectrumstatistics",title="Spectrum Statistics",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class SpectrumStatisticsNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("analysis"),OUT=new PropertyId("value"),VIEW=new PropertyId("view"),STATISTIC=new PropertyId("statistic");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Value").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        SpectrumView.addEditor(n,VIEW);
        n.addEditor(STATISTIC,"Statistic",Editor.comboBox(java.util.List.of("Peak","Mean","RMS","Integral"),"Peak",v->v.toLowerCase(java.util.Locale.ROOT)))
            .tooltip(com.vin.easyknack.ui.AppText.literal("Reduces the selected spectrum bins to one value. Peak is the largest absolute value; RMS is spectral-bin RMS, not audio RMS. Integral is the bin sum multiplied by frequency spacing in Hz. Held until the next analysis frame."));
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrumstatistics","nodes/SpectrumStatisticsNode.hpp",ParameterContract.builder().source(IN).value(VIEW).value(STATISTIC).build());}
}
