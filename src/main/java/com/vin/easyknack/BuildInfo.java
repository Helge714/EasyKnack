package com.vin.easyknack;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Package identity. An IDE class directory is deliberately labelled as development. */
public record BuildInfo(String version, String build, String commit, String dirty) {
    public static BuildInfo application() { return read("/META-INF/easyknack/build.properties", EasyKnack.class); }
    public static BuildInfo core() { return read("/META-INF/glswing/build.properties", com.vingame.glswing.application.GLApplication.class); }
    private static BuildInfo read(String resource, Class<?> owner) {
        if (owner.getProtectionDomain().getCodeSource() != null
                && owner.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith("/"))
            return new BuildInfo("development", "IDE / unpackaged", "unknown", "unknown");
        var p = new Properties();
        try (var input = owner.getResourceAsStream(resource)) {
            if (input != null) p.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (java.io.IOException ignored) { }
        return new BuildInfo(p.getProperty("version", "unknown"), p.getProperty("build.id", "unavailable"),
                p.getProperty("git.commit", "unknown"), p.getProperty("git.dirty", "unknown"));
    }
    public String display() {
        return version + " — Build " + build + " (" + commit.substring(0, Math.min(8, commit.length()))
                + (dirty.equals("true") ? ", dirty" : dirty.equals("unknown") ? ", worktree unknown" : "") + ")";
    }
}
