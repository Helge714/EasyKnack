package com.vin.easyknack.model;

public enum NoiseMode {

    WHITE("white"),
    PINK("pink"),
    BROWN("brown"),
    BLUE("blue");

    private final String id;

    NoiseMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static NoiseMode fromId(String id) {
        for (var mode : values()) {
            if (mode.id.equals(id))
                return mode;
        }

        return WHITE;
    }

    @Override
    public String toString() {
        return switch (this) {
            case WHITE -> "White";
            case PINK -> "Pink";
            case BROWN -> "Brown";
            case BLUE -> "Blue";
        };
    }
}
