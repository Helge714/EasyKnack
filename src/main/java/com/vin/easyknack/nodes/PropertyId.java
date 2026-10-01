package com.vin.easyknack.nodes;

/** Stable key for an input, output or editor. Its role is defined by NodeBuilder. */
public record PropertyId(String value) {
    public PropertyId {
        if (value == null || !value.matches("[A-Za-z0-9][A-Za-z0-9._-]*"))
            throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid property ID: ") + value);
    }
}

