package com.vin.easyknack;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BuildInfoTest {
    @Test void dirtyAndUnknownSourcesAreExplicit() {
        assertEquals("0.2.0 — Build local-test (01234567, dirty)",
                new BuildInfo("0.2.0", "local-test", "0123456789", "true").display());
        assertTrue(new BuildInfo("unknown", "unavailable", "unknown", "unknown").display().contains("worktree unknown"));
    }
    @Test void classDirectoryDoesNotAdvertiseStalePackageIdentity() {
        assertEquals("IDE / unpackaged", BuildInfo.application().build());
    }
}
