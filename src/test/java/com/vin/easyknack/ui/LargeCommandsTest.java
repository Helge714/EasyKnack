package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.audio.AudioHostClient;
import com.vin.easyknack.nodes.*;
@EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
class LargeCommandsTest {
    @Test void bufferedFramingPreservesCommandsAndRecoversAfterRejection()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"command-reader-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/CommandReaderTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),Path.of("target/native/generated").toAbsolutePath());
    }
    @Test void hostAcceptsLargeWavetableGraphsAndKeepsConnectionAfterErrors()throws Exception{
        var wave=new WaveformNode();String params="!"+NativeContractGenerator.fingerprint(wave)+" "+wave.encode(new NodeState(wave.defaults()),p->-1);
        var graph=new StringBuilder("GRAPH 8");for(int i=0;i<8;i++)graph.append(" w").append(i).append(" waveform ").append(params).append(" @ c -");
        assertTrue(graph.length()>65536);
        try(var client=new AudioHostClient()){
            client.start(AudioHostClient.defaultExecutable(),true);
            assertEquals("OK",client.command(graph.toString()));
            assertThrows(AudioHostClient.CommandRejectedException.class,()->client.command("UNKNOWN "+"x".repeat(200000)));
            assertTrue(client.command("PING").startsWith("OK "));
            // A genuine transport overflow must also leave the stream synchronized.
            assertThrows(AudioHostClient.CommandRejectedException.class,()->client.command("x".repeat(64*1024*1024+1)));
            assertTrue(client.command("PING").startsWith("OK "));
            assertEquals("OK",client.command("GRAPH 0"));
        }
    }
}
