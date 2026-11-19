package com.vin.easyknack;

import java.nio.file.Path;
import java.util.*;

/** Native artifact/toolchain policy, kept outside GUI widgets and DSP contracts. */
public record NativePlatform(OperatingSystem os, String architecture) {
    public enum OperatingSystem { WINDOWS, LINUX, MACOS }
    public static NativePlatform current() { return detect(System.getProperty("os.name", ""), System.getProperty("os.arch", "")); }
    public static NativePlatform detect(String name, String arch) {
        String lower = name.toLowerCase(Locale.ROOT);
        var os = lower.startsWith("windows") ? OperatingSystem.WINDOWS : lower.contains("linux") ? OperatingSystem.LINUX
                : lower.contains("mac") || lower.contains("darwin") ? OperatingSystem.MACOS : null;
        if (os == null) throw new IllegalArgumentException("Unsupported native platform: " + name);
        String normalized = switch (arch.toLowerCase(Locale.ROOT)) { case "amd64", "x86_64" -> "x86_64"; case "aarch64", "arm64" -> "arm64"; default -> arch.toLowerCase(Locale.ROOT); };
        return new NativePlatform(os, normalized);
    }
    public boolean windows() { return os == OperatingSystem.WINDOWS; }
    public String policyKey() { return os.name().toLowerCase(Locale.ROOT); }
    public String cacheKey() { return policyKey() + "-" + architecture; }
    public String executable(String name) { return name + (windows() ? ".exe" : ""); }
    public String hostExecutable() { return executable("easyknack-audio-host"); }
    public String libraryExtension() { return windows() ? ".dll" : os == OperatingSystem.MACOS ? ".dylib" : ".so"; }
    public List<String> sharedLibraryFlags() { return windows() ? List.of("-shared", "-static") : os == OperatingSystem.MACOS ? List.of("-dynamiclib", "-fPIC") : List.of("-shared", "-fPIC"); }
    public List<String> compilerNames() { return windows() ? List.of("clang++.exe") : List.of("clang++", "g++"); }
    public List<Path> systemCompilers() { return windows() ? List.of() : List.of(Path.of("/usr/bin/clang++"), Path.of("/usr/bin/g++"), Path.of("/usr/local/bin/clang++")); }
    public boolean supportsHostBuild() { return architecture.equals("x86_64") || !windows() && architecture.equals("arm64"); }
}
