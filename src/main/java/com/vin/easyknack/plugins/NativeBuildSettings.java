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
    private final com.vin.easyknack.NativePlatform platform;

    NativeBuildSettings(Path sdk) throws IOException {
        this(sdk, com.vin.easyknack.NativePlatform.current());
    }

    NativeBuildSettings(Path sdk, com.vin.easyknack.NativePlatform platform) throws IOException {
        this.platform = platform;
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
        result.addAll(platform.compilerArchitectureFlags());
        if(platform.os()==com.vin.easyknack.NativePlatform.OperatingSystem.MACOS)
            result.addAll(arguments("macos.plugin.compile.flags"));
        return result;
    }

    List<String> link() {
        var result=new ArrayList<>(arguments(platform.windows()?"plugin.link.flags":platform.policyKey()+".plugin.link.flags"));
        result.addAll(platform.compilerArchitectureFlags());return result;
    }
    List<String> sourceArguments(Path source) {
        return platform.os()==com.vin.easyknack.NativePlatform.OperatingSystem.MACOS
                && source.getFileName().toString().equals("MacPlatform.mm")
                ? List.of("-x", "objective-c++", "-fobjc-arc") : List.of();
    }
    List<String> libraries() {
        var result=new ArrayList<>(arguments(platform.policyKey()+".libraries").stream().map(name -> "-l" + name).toList());
        if(platform.os()==com.vin.easyknack.NativePlatform.OperatingSystem.MACOS)
            for(String framework:arguments("macos.frameworks")){result.add("-framework");result.add(framework);}
        return result;
    }
}
