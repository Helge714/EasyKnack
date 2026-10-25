package com.vin.easyknack.ui;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import com.vin.easyknack.audio.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import java.util.Map;

class AudioRenderAheadTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void deviceReadsNeverWaitForSpectralWork()throws Exception {
        var directory=Files.createTempDirectory(Path.of("target"),"render-ahead-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/AudioRenderAheadTest.cpp").toAbsolutePath(),directory.resolve("test.exe"),directory);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void hostAutomaticallyBuffersFftAndRestartsItsProducer()throws Exception {
        var graph=new GLNodeGraph();var oscillator=add(graph,new OscillatorNode());
        var fft=add(graph,new SpectrumNode(),Map.of("size",GLNodeValue.of("4096"),"overlap",GLNodeValue.of(.75)));
        var inverse=add(graph,new IFFTNode());var output=add(graph,new OutputNode());
        connect(graph,oscillator,"audio-out",fft,"audio-in");connect(graph,fft,"analysis",inverse,"spectrum");connect(graph,inverse,"out",output,"audio-in");
        try(var host=new AudioHostClient()){
            host.start(AudioHostClient.defaultExecutable(),true);
            assertEquals("OK",host.command(AudioGraphEncoder.encode(graph.snapshot())));
            for(int restart=0;restart<2;restart++){
                assertEquals("OK",host.command("PLAY"));
                AudioDiagnostics diagnostics=null;long until=System.nanoTime()+5_000_000_000L;
                while(System.nanoTime()<until){
                    diagnostics=AudioDiagnostics.parse(host.command("DIAGNOSTICS"));
                    if(diagnostics.clock().contains("render-reserve 1536")&&diagnostics.blocks()>0)break;
                    Thread.sleep(10);
                }
                assertNotNull(diagnostics);assertTrue(diagnostics.clock().contains("render-reserve 1536"),diagnostics.toString());
                assertTrue(diagnostics.blocks()>0);assertEquals("OK",host.command("STOP"));assertTrue(host.command("PING").startsWith("OK "));
            }
        }
    }
}
