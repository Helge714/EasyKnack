package com.vin.easyknack.ui;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
class AdditiveSpreadTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void allOvertonesReceiveTheSameDetuning()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"spread-test-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/AdditiveSpreadTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
