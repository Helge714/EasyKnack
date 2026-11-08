package com.vin.easyknack.plugins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Compiler policy shared with developer, CMake and installer builds. */
final class NativeBuildSettings {
    private final Properties values = new Properties();

    NativeBuildSettings(Path sdk) throws IOException {
        try (var input = Files.newInputStream(sdk.resolve("build.properties"))) {
            values.load(input);
        }
    }

    List<String> arguments(String key) {
        String value = values.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing native build setting: " + key);
        }
        return List.of(value.strip().split("\\s+"));
    }

    List<String> compile() {
        var result = new ArrayList<String>();
        result.add("-std=c++" + values.getProperty("cpp.standard"));
        result.addAll(arguments("plugin.compile.flags"));
        return result;
    }

    List<String> libraries() {
        return arguments("windows.libraries").stream().map(name -> "-l" + name).toList();
    }
}
