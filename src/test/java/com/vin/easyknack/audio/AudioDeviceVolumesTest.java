package com.vin.easyknack.audio;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class AudioDeviceVolumesTest {
    public static java.util.Map<String,Double> drainRequests() { return AudioDeviceVolumes.drain(); }
    @Test void snapshotsValidateAndRequestsCoalesce() {
        var values=AudioDeviceVolumes.parse("OK 1 64656661756c74 0.4 1 0.25");
        assertEquals(new AudioDeviceVolumes.Value(.4,true,.25),values.get("default"));
        for(String bad:new String[]{"OK 1 61 NaN 0 0","OK 1 61 2 0 0","OK 0 61 0.5 2 0","OK 0 61 0.5 0 -1"})
            assertThrows(IllegalArgumentException.class,()->AudioDeviceVolumes.parse(bad));
        AudioDeviceVolumes.request("default",.2); AudioDeviceVolumes.request("default",.3);
        assertEquals(java.util.Map.of("default",.3),AudioDeviceVolumes.drain());
        assertTrue(AudioDeviceVolumes.drain().isEmpty());
    }
}

