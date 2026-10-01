package com.vin.easyknack.model;

public enum ClampMode {

    CLIP("clip"),
    FOLDBACK("foldback"),
    WRAP("wrap"),
    SOFT_CLIP("soft_clip");

    private final String id;

    ClampMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static ClampMode fromId(String id) {
        for (var mode : values()) {
            if (mode.id.equals(id))
                return mode;
        }

        return CLIP;
    }

    @Override
    public String toString() {
        return switch (this) {
            case CLIP -> "Clip";
            case FOLDBACK -> "Foldback";
            case WRAP -> "Wrap";
            case SOFT_CLIP -> "Soft Clip";
        };
    }
}
