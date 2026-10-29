package com.vin.easyknack.ui;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
class AdditiveSpreadTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void hostAcceptsThePowerModelContract()throws Exception{
        var definition=new com.vin.easyknack.nodes.AdditiveOscillatorNode();
        try(var host=new com.vin.easyknack.audio.AudioHostClient()){
            host.start(com.vin.easyknack.audio.AudioHostClient.defaultExecutable(),true);
            for(double exponent:new double[]{0,.5,1,1.4,2,4}){
                var values=new java.util.HashMap<>(definition.defaults());values.put("spread",com.vingame.glswing.node.model.GLNodeValue.of(exponent));values.put("harmonics",com.vingame.glswing.node.model.GLNodeValue.of(4));
                String payload="!"+com.vin.easyknack.nodes.NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new com.vin.easyknack.nodes.NodeState(values),p->-1);
                org.junit.jupiter.api.Assertions.assertEquals("OK",host.command("GRAPH 2 osc additive "+payload+" @ a cccccce out out 0 0 @ a a"));
            }
        }
    }

    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void powerStretchPreservesFundamentalAndCachesAllPartials()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"spread-test-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/AdditiveSpreadTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
