package com.vin.easyknack.plugins;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** One source package, with one or more annotated BasicNode entry points. */
public record PluginManifest(String id, String version, List<String> nodes) {
    public static final String FILE = "plugin.properties";
    public PluginManifest {
        if (!id.matches("[a-z][a-z0-9_-]{0,79}")) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid plugin ID: ") + id);
        if (!version.matches("[0-9A-Za-z][0-9A-Za-z._+\\-]{0,79}")) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid plugin version"));
        nodes = List.copyOf(nodes);
        if (nodes.isEmpty() || nodes.size() > 64 || new HashSet<>(nodes).size() != nodes.size()
                || nodes.stream().anyMatch(n -> !n.matches("[A-Za-z_$][\\w$]*(\\.[A-Za-z_$][\\w$]*)+")))
            throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid plugin entry points"));
    }
    public static PluginManifest read(Path directory) throws IOException {
        Properties p = new Properties();
        try (var reader = Files.newBufferedReader(directory.resolve(FILE))) { p.load(reader); }
        if (!"1".equals(p.getProperty("format"))) throw new IOException(com.vin.easyknack.i18n.AppText.literal("Unsupported plugin source format"));
        return new PluginManifest(p.getProperty("id", ""), p.getProperty("version", ""),
                Arrays.stream(p.getProperty("nodes", "").split(",")).map(String::trim).toList());
    }
    public String text() {
        return "format=1\nid=" + id + "\nversion=" + version + "\nnodes=" + String.join(",", nodes) + "\n";
    }
}
