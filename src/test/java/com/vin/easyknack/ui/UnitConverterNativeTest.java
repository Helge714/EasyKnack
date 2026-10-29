package com.vin.easyknack.ui;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
class UnitConverterNativeTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void reciprocalUnitsAndMixedRates()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"unit-converter-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/UnitConverterTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
