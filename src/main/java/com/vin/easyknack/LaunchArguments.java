package com.vin.easyknack;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/** The Windows native Java launcher can lose characters outside its ANSI code page. */
public final class LaunchArguments {
    private LaunchArguments() { }

    public static String[] decode(String[] arguments) {
        String encoded = System.getProperty("easyknack.launch.arguments");
        if (encoded == null) return arguments;
        return Arrays.stream(encoded.split(":", -1))
                .map(value -> new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8))
                .toArray(String[]::new);
    }
}
