package com.vin.easyknack.plugins;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** The shipped SDK contains the exact host sources; GLSwing remains an external JAR. */
public final class PluginSdk {
    public static final String RESOURCE = "/easyknack/plugin-sdk.zip";
    private PluginSdk() { }
    public static byte[] bytes() throws IOException {
        // Eclipse may clean generated resources without running Maven's process-classes.
        // Anchor development sources to our class directory, never the working directory.
        try {
            var location = PluginSdk.class.getProtectionDomain().getCodeSource();
            if (location != null) {
                var source = developmentSource(Path.of(location.getLocation().toURI()));
                if (source != null) return archive(source);
            }
        } catch (java.net.URISyntaxException e) {
            throw new IOException(com.vin.easyknack.ui.AppText.literal("Invalid plugin SDK class location"), e);
        }
        try (var in = PluginSdk.class.getResourceAsStream(RESOURCE)) {
            if (in == null) throw new IOException(com.vin.easyknack.ui.AppText.literal("Plugin SDK missing from this installation; rebuild with Maven package or reinstall EasyKnack"));
            return in.readAllBytes();
        }
    }
    static Path developmentSource(Path classes) {
        classes = classes.toAbsolutePath().normalize();
        if (!Files.isDirectory(classes) || !classes.endsWith(Path.of("target", "classes"))) return null;
        var root = classes.getParent().getParent();
        var source = root.resolve("native/audio-host");
        return Files.isRegularFile(root.resolve("pom.xml"))
                && Files.isRegularFile(root.resolve("src/main/java/com/vin/easyknack/plugins/PluginSdk.java"))
                && Files.isRegularFile(source.resolve("src/NodeProcessor.hpp"))
                ? source : null;
    }
    public static void packageSdk(Path source, Path output) throws IOException {
        Files.createDirectories(output.getParent());
        var bytes = archive(source);
        if (!Files.exists(output) || !Arrays.equals(Files.readAllBytes(output), bytes))
            Files.write(output, bytes);
    }
    private static byte[] archive(Path source) throws IOException {
        var bytes = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(bytes); var paths = Files.walk(source)) {
            for (var path : paths.filter(Files::isRegularFile).sorted().toList()) {
                String name = source.relativize(path).toString().replace('\\', '/');
                if (!(name.startsWith("src/") || name.startsWith("third_party/"))) continue;
                var entry = new ZipEntry(name); entry.setTime(0); zip.putNextEntry(entry);
                Files.copy(path, zip); zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }
    /** Reject traversal, duplicate entries, excessive expansion and platform-specific path tricks. */
    public static void unpack(InputStream input, Path destination) throws IOException {
        Files.createDirectories(destination); destination = destination.toAbsolutePath().normalize();
        long total = 0; int entries = 0; var names = new HashSet<String>();
        try (var zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (++entries > 4096 || name.contains("\\") || name.contains(":") || name.startsWith("/")
                        || Arrays.stream(name.split("/")).anyMatch(p -> p.equals("..") || p.endsWith(".") || p.endsWith(" ")))
                    throw new IOException(com.vin.easyknack.ui.AppText.literal("Invalid archive path: ") + name);
                var target = destination.resolve(name).normalize();
                if (!target.startsWith(destination) || !names.add(target.toString().toLowerCase(Locale.ROOT)))
                    throw new IOException(com.vin.easyknack.ui.AppText.literal("Duplicate or invalid archive entry: ") + name);
                if (entry.isDirectory()) { Files.createDirectories(target); continue; }
                Files.createDirectories(target.getParent());
                try (var out = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
                    byte[] buffer = new byte[16384]; int n;
                    while ((n = zip.read(buffer)) != -1) {
                        total += n; if (total > 64L * 1024 * 1024) throw new IOException(com.vin.easyknack.ui.AppText.literal("Plugin archive exceeds 64 MiB"));
                        out.write(buffer, 0, n);
                    }
                }
            }
        }
    }
}
