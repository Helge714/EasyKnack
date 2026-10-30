package com.vin.easyknack.ui;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
class DynamicOutputsTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void hostRoutesWideAudioControlEventsAndResourcesWithoutCallbackAllocation()throws Exception{
        var directory=Files.createTempDirectory(Path.of("target"),"dynamic-outputs-").toAbsolutePath();
        var source=directory.resolve("test.cpp");
        Files.writeString(source,Files.readString(Path.of("native/audio-host/tests/DynamicOutputsTest.cpp")).replace("TEST_RELAY_FINGERPRINT",com.vin.easyknack.nodes.NativeContractGenerator.fingerprint(new com.vin.easyknack.nodes.SpectrumRelayNode())));
        RateConverterTest.compileAndRun(source,directory.resolve("test.exe"),directory);
    }
}
