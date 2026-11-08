package com.vin.easyknack.nodes;

/** Stable key for an input, output or editor. Its role is defined by NodeBuilder. */
public record PropertyId(String value) {
    private static final java.util.regex.Pattern VALID=java.util.regex.Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");
    public PropertyId {
        if (value == null || !VALID.matcher(value).matches())
            throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid property ID: ") + value);
    }
}

