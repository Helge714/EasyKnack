package com.vin.easyknack.nodes;

/** Built-in palette categories are checked by javac, not looked up by name. */
public enum NodeCategory {
    ANALYSIS("Analysis"), SPECTRAL("Spectral"), GENERATOR("Generators"), EFFECT("Effects"), MATH("Math & Mapping"), IO("Input / Output"), INPUT("Inputs"), MIDI("MIDI"), CONVERTER("Converters"), ROUTING("Mixing & Routing"), MODULATION("Modulation"), VECTOR("Vector"), BUFFER("Buffers");
    private final String path;
    NodeCategory(String path) { this.path = path; }
    public String path() { return NodeText.text("category." + name(), path); }
    public boolean matches(String value) {
        return path.equals(value) || path().equals(value) || (this == MATH && "Math".equals(value))
                || (this == ROUTING && "Routing".equals(value));
    }
    public static String catalogPath(String value) {
        for (var category : values()) if (category.matches(value)) return category.path();
        return value;
    }
}


