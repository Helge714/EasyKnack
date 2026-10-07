package com.vin.easyknack.plugins;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PluginSdkTest {
    @TempDir Path directory;

    @Test void developmentSdkMatchesPackagedSources() throws Exception {
        var source = PluginSdk.developmentSource(Path.of("target/classes"));
        assertNotNull(source);
        var zip = directory.resolve("sdk.zip");
        PluginSdk.packageSdk(source, zip);
        assertArrayEquals(Files.readAllBytes(zip), PluginSdk.bytes());
        PluginSdk.unpack(new java.io.ByteArrayInputStream(PluginSdk.bytes()), directory.resolve("unpacked"));
        assertTrue(Files.isRegularFile(directory.resolve("unpacked/src/NodeProcessor.hpp")));
        assertTrue(Files.readString(directory.resolve("unpacked/LICENSE")).contains("GNU GENERAL PUBLIC LICENSE"));
        assertTrue(Files.readString(directory.resolve("unpacked/LICENSING.md")).contains("GPL-3.0-only"));
    }

    @Test void onlyAnActualDevelopmentClassDirectoryEnablesSourceLoading() throws Exception {
        assertNull(PluginSdk.developmentSource(Path.of("target/easy-knack-0.0.1-SNAPSHOT.jar")));
        var classes = Files.createDirectories(directory.resolve("target/classes"));
        assertNull(PluginSdk.developmentSource(classes));
        assertNull(PluginSdk.developmentSource(Path.of(".")));
    }

    @Test void ideStartupWorksWithoutGeneratedSdkResource() throws Exception {
        var classes = Files.createDirectories(directory.resolve("target/classes"));
        var classFile = classes.resolve("com/vin/easyknack/plugins/PluginSdk.class");
        Files.createDirectories(classFile.getParent());
        try (var input = PluginSdk.class.getResourceAsStream("PluginSdk.class")) {
            Files.copy(input, classFile);
        }
        try (var input = PluginLicensing.class.getResourceAsStream("PluginLicensing.class")) {
            Files.copy(input, classFile.resolveSibling("PluginLicensing.class"));
        }
        var license = classes.resolve("META-INF/easyknack/legal/LICENSE");
        Files.createDirectories(license.getParent());
        try (var input = PluginLicensing.class.getResourceAsStream("/META-INF/easyknack/legal/LICENSE")) {
            Files.copy(input, license);
        }
        Files.writeString(directory.resolve("pom.xml"), "<project/>");
        var javaSource = directory.resolve("src/main/java/com/vin/easyknack/plugins/PluginSdk.java");
        Files.createDirectories(javaSource.getParent());Files.writeString(javaSource, "// fixture");
        var header = directory.resolve("native/audio-host/src/NodeProcessor.hpp");
        Files.createDirectories(header.getParent());Files.writeString(header, "// current native source");
        try (var loader = new java.net.URLClassLoader(new java.net.URL[]{classes.toUri().toURL()}, ClassLoader.getPlatformClassLoader())) {
            assertNull(loader.getResource("easyknack/plugin-sdk.zip"));
            var bytes = (byte[]) loader.loadClass(PluginSdk.class.getName()).getMethod("bytes").invoke(null);
            PluginSdk.unpack(new java.io.ByteArrayInputStream(bytes), directory.resolve("restored"));
            assertEquals(Files.readString(header), Files.readString(directory.resolve("restored/src/NodeProcessor.hpp")));
        }
    }
}
