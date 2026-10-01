package com.vin.easyknack.nodes;

/** Presentation of a shared spectrum; selecting a view never computes another FFT. */
public enum SpectrumView {
    MAGNITUDE("Magnitude"), PHASE("Phase"), POWER("Power");
    private final String label;
    SpectrumView(String label){this.label=label;}
    public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
    @Override public String toString(){return NodeText.text("spectrum.view."+id(),label);}
    public static void addEditor(NodeBuilder node,PropertyId id){
        node.addEditor(id,"Data",Editor.comboBox(java.util.List.of(values()),MAGNITUDE,SpectrumView::id))
            .tooltip(com.vin.easyknack.ui.AppText.literal("Magnitude: one-sided amplitude. Phase: radians relative to the start of the window. Power: squared amplitude, not power spectral density."));
    }
}
