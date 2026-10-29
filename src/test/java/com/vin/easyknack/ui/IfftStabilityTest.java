package com.vin.easyknack.ui;
import com.vin.easyknack.nodes.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

class IfftStabilityTest {
    @Test void stabilityDefaultsToBypass(){
        var node=new IFFTNode();
        assertEquals("-1 0.0",node.encode(new NodeState(node.defaults()),p->-1));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void stabilizesNarrowNoiseAfterOverlap()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"ifft-stability-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/IfftStabilityTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
