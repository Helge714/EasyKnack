package com.vin.easyknack.plugins;

import com.vin.easyknack.NativePlatform;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NativeBuildSettingsTest {
    @Test void systemLibrariesAndLinkFlagsDoNotLeakAcrossPlatforms() throws Exception {
        var sdk = Path.of("native/audio-host");
        var windows = new NativeBuildSettings(sdk, NativePlatform.detect("Windows", "amd64"));
        var linux = new NativeBuildSettings(sdk, NativePlatform.detect("Linux", "amd64"));
        var mac = new NativeBuildSettings(sdk, NativePlatform.detect("Darwin", "arm64"));
        assertTrue(windows.libraries().contains("-lws2_32"));
        assertTrue(windows.link().contains("-static"));
        assertEquals(java.util.List.of("-ldl", "-lpthread", "-lm"), linux.libraries());
        assertFalse(linux.link().contains("-static"));
        assertTrue(mac.libraries().contains("CoreAudio"));
        assertFalse(mac.libraries().contains("-ldl"));
        assertEquals(windows.compile(), linux.compile());
    }
}
