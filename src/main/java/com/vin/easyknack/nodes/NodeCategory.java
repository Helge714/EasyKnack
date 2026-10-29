package com.vin.easyknack.nodes;

/** Built-in palette categories are checked by javac, not looked up by name. */
public enum NodeCategory {
    INPUT("Inputs"), OUTPUT("Outputs"), GENERATOR("Generators"), EFFECT("Effects"),
    ANALYSIS("Analysis"), SPECTRAL("Spectrum"), MIDI("MIDI"), TRIGGER("Trigger"),
    ROUTING("Routing"), MATH("Math"), VECTOR("Vector"), BUFFER("Buffers"),
    /** Source-plugin aliases; excluded from category selectors. */
    @Deprecated IO("Inputs"), @Deprecated CONVERTER("Math"), @Deprecated MODULATION("Trigger");
    private final String path;
    NodeCategory(String path) { this.path = path; }
    public NodeCategory canonical(){return switch(this){case IO->INPUT;case CONVERTER->MATH;case MODULATION->TRIGGER;default->this;};}
    public static NodeCategory[] choices(){return java.util.Arrays.stream(values()).filter(c->c.canonical()==c).toArray(NodeCategory[]::new);}
    public static java.util.List<String> menuOrder(){return java.util.List.of(INPUT,GENERATOR,EFFECT,ROUTING,MATH,VECTOR,TRIGGER,MIDI,BUFFER,SPECTRAL,ANALYSIS,OUTPUT).stream().map(NodeCategory::path).toList();}
    public String path() { return NodeText.text("category." + canonical().name(), path); }
    public boolean matches(String value) {
        return path.equals(value) || path().equals(value) || (this == MATH && "Math".equals(value))
                || (this == ROUTING && "Mixing & Routing".equals(value))
                || (this == MATH && ("Math & Mapping".equals(value)||"Converters".equals(value)))
                || (this == INPUT && "Input / Output".equals(value))
                || (this == TRIGGER && "Modulation".equals(value))
                || (this == SPECTRAL && "Spectral".equals(value));
    }
    public static String catalogPath(String value) {
        for (var category : values()) if (category.matches(value)) return category.path();
        return value;
    }
}


