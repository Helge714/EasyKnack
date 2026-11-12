package com.vin.easyknack;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

/** Package identity; IDE launches retain the Maven version without a package identity. */
public record BuildInfo(String version, String build, String commit, String dirty) {
    public static BuildInfo application() { return read("/META-INF/easyknack/build.properties", EasyKnack.class); }
    public static BuildInfo core() { return read("/META-INF/glswing/build.properties", com.vingame.glswing.application.GLApplication.class); }
    private static BuildInfo read(String resource, Class<?> owner) {
        if (owner.getProtectionDomain().getCodeSource() != null
                && owner.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith("/"))
            return new BuildInfo(developmentVersion(owner), "IDE / unpackaged", "unknown", "unknown");
        var p = new Properties();
        try (var input = owner.getResourceAsStream(resource)) {
            if (input != null) p.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (java.io.IOException ignored) { }
        return new BuildInfo(p.getProperty("version", "unknown"), p.getProperty("build.id", "unavailable"),
                p.getProperty("git.commit", "unknown"), p.getProperty("git.dirty", "unknown"));
    }
    private static String developmentVersion(Class<?> owner) {
        try {
            return developmentVersion(Path.of(owner.getProtectionDomain().getCodeSource().getLocation().toURI()));
        } catch (Exception unavailable) { return "unknown"; }
    }
    static String developmentVersion(Path classes) {
        // Anchor to the loaded classes, never the process working directory or stale package metadata.
        for (Path root = classes.toAbsolutePath().normalize(); root != null; root = root.getParent()) {
            Path pom = root.resolve("pom.xml");
            if (!Files.isRegularFile(pom)) continue;
            try (var input = Files.newInputStream(pom)) {
                var factory = DocumentBuilderFactory.newInstance();
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                var project = factory.newDocumentBuilder().parse(input).getDocumentElement();
                for (var node = project.getFirstChild(); node != null; node = node.getNextSibling()) {
                    if (node.getNodeName().equals("version")) {
                        String version = node.getTextContent().strip();
                        return version.isEmpty() || version.contains("${") ? "unknown" : version;
                    }
                }
            } catch (Exception unavailable) { return "unknown"; }
            return "unknown";
        }
        return "unknown";
    }
    public String display() {
        if (build.equals("IDE / unpackaged")) return version + " — " + build;
        return version + " — Build " + build + " (" + commit.substring(0, Math.min(8, commit.length()))
                + (dirty.equals("true") ? ", dirty" : dirty.equals("unknown") ? ", worktree unknown" : "") + ")";
    }
}
