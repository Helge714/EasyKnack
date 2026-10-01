package com.vin.easyknack.ui;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
class OptimizationTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void simdSpectrumAndBufferReuseMatchReferences()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"optimization-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/OptimizationTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
