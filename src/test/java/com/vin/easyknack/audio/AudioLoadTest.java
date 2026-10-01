package com.vin.easyknack.audio;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class AudioLoadTest {
    @Test void parsesPercentageUnitsWithoutClampingOverloads() throws Exception {
        var load = AudioLoad.parse("OK 1250 15000 3");
        assertEquals(12.5, load.averagePercent()); assertEquals(150, load.peakPercent());
        assertEquals(3, load.overBudget()); assertTrue(load.label().contains("12,5 %"));
        for (var invalid : new String[] {"OK", "ERR 1 2 3", "OK -1 2 3", "OK NaN 2 3", "OK 1 2 -3"})
            assertThrows(IOException.class, () -> AudioLoad.parse(invalid));
    }
}

