package com.vin.easyknack;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;

class BuildInfoTest {
    @Test void dirtyAndUnknownSourcesAreExplicit() {
        assertEquals("0.2.0 — Build local-test (01234567, dirty)",
                new BuildInfo("0.2.0", "local-test", "0123456789", "true").display());
        assertTrue(new BuildInfo("unknown", "unavailable", "unknown", "unknown").display().contains("worktree unknown"));
    }
    @Test void classDirectoryDoesNotAdvertiseStalePackageIdentity() {
        assertEquals("IDE / unpackaged", BuildInfo.application().build());
        assertNotEquals("development", BuildInfo.application().version());
        assertNotEquals("unknown", BuildInfo.application().version());
        assertEquals(BuildInfo.application().version() + " — IDE / unpackaged", BuildInfo.application().display());
    }
    @Test void ideVersionUsesOwningPomAndIgnoresStalePackagedMetadata(@TempDir Path root) throws Exception {
        Files.writeString(root.resolve("pom.xml"), "<project><parent><version>8.0</version></parent><version>0.9.3-SNAPSHOT</version></project>");
        Path classes = Files.createDirectories(root.resolve("target/validation/classes"));
        Path metadata = classes.resolve("META-INF/easyknack/build.properties");
        Files.createDirectories(metadata.getParent());
        Files.writeString(metadata, "version=0.1.0\nbuild.id=stale");
        assertEquals("0.9.3-SNAPSHOT", BuildInfo.developmentVersion(classes));
        Files.writeString(root.resolve("pom.xml"), "<project><version>0.9.4-SNAPSHOT</version></project>");
        assertEquals("0.9.4-SNAPSHOT", BuildInfo.developmentVersion(classes));
    }
    @Test void missingIdePomDoesNotInventVersion(@TempDir Path root) {
        assertEquals("unknown", BuildInfo.developmentVersion(root));
    }
}
