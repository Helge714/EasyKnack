package com.vin.easyknack.audio;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
class AudioDiagnosticsTest {
    static String response(long epoch){return "OK 1 "+epoch+" 100 25 140 1250 7000 2 256 48000 0 1 2 3 4 5 6 434c4f434b 42554646455253";}
    @Test void versionUnitsAndInvalidData()throws Exception{
        var d=AudioDiagnostics.parse(response(1));assertEquals(140,d.peak());assertEquals(5333.333,d.budgetUs(),.001);assertEquals(5,d.driverOverload());assertEquals(3,d.playbackUnder());
        for(String bad:new String[]{"OK",response(1).replace("OK 1","OK 2"),response(1).replace(" 25 "," NaN "),response(1).replace(" 25 "," -1 "),response(1).replace(" 100 "," 1.5 ")})assertThrows(IOException.class,()->AudioDiagnostics.parse(bad));
    }
    @Test void historyIsBoundedResetsEpochAndReportsUnits()throws Exception{
        var h=new AudioDiagnosticHistory();for(int i=0;i<1000;i++)h.add(i*250_000_000L,AudioDiagnostics.parse(response(1)));
        assertTrue(h.points().size()<=241);assertTrue(h.report("RUNNING").contains("p99_us"));
        h.add(251_000_000_000L,AudioDiagnostics.parse(response(2)));assertEquals(1,h.points().size());h.clear();assertTrue(h.points().isEmpty());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeProtocolResetKeepsAudioAlive()throws Exception{
        try(var host=new AudioHostClient()){
            host.start(AudioHostClient.defaultExecutable(),true);host.command("GRAPH 2 osc osc -1 440 @ a a out out 0 0 @ a a");host.command("PLAY");
            var first=AudioDiagnostics.parse(host.command("DIAGNOSTICS"));AudioDiagnostics next=first;
            for(int i=0;i<40&&next.blocks()==0;i++){Thread.sleep(25);next=AudioDiagnostics.parse(host.command("DIAGNOSTICS"));}
            assertTrue(next.blocks()>0);assertTrue(next.frames()>0);assertEquals("OK",host.command("DIAGNOSTICS_RESET"));
            var reset=AudioDiagnostics.parse(host.command("DIAGNOSTICS"));assertTrue(reset.epoch()>first.epoch());assertTrue(host.command("PING").startsWith("OK "));
        }
    }
}
