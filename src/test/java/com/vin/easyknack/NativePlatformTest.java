package com.vin.easyknack;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NativePlatformTest {
    @Test void nativeArtifactsFollowTheTargetPlatform() {
        var windows = NativePlatform.detect("Windows 11", "amd64");
        var linux = NativePlatform.detect("Linux", "x86_64");
        var mac = NativePlatform.detect("Mac OS X", "aarch64");
        assertEquals("easyknack-audio-host.exe", windows.hostExecutable());
        assertEquals("easyknack-audio-host", linux.hostExecutable());
        assertEquals(".so", linux.libraryExtension());
        assertEquals(".dylib", mac.libraryExtension());
        assertEquals("macos-arm64", mac.cacheKey());
        assertTrue(mac.supportsHostBuild());
        assertNotEquals(windows.cacheKey(), linux.cacheKey());
        assertTrue(linux.sharedLibraryFlags().contains("-fPIC"));
        assertTrue(mac.sharedLibraryFlags().contains("-dynamiclib"));
        assertFalse(NativePlatform.detect("Windows 11", "arm64").supportsHostBuild());
        assertThrows(IllegalArgumentException.class, () -> NativePlatform.detect("Unknown", "amd64"));
    }
}
