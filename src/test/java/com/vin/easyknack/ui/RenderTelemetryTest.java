package com.vin.easyknack.ui;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
class RenderTelemetryTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeTelemetryAndPriming()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"telemetry-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/RenderTelemetryTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
