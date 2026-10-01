package com.vin.easyknack.ui;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;

class OscillatorPhaseTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void phaseOffsetWorksForScalarAudioFeedbackAndLegacyContracts() throws Exception {
        var d=new OscillatorNode();
        String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->-1);
        var dir=Files.createTempDirectory(Path.of("target"),"osc-phase-").toAbsolutePath();
        var source=dir.resolve("test.cpp");
        Files.writeString(source,"""
            #include "nodes/OscillatorNode.hpp"
            #include <sstream>
            #include <cassert>
            using namespace easyknack;
            int main(){
                OscillatorNode osc;std::istringstream p("%s");osc.configure(p);osc.prepare(48000,256);
                float hz=0,offsets[]{90,180,270,360},output[4]{},silence[4]{};
                const float* inputs[]{&hz,nullptr,offsets};char rates[]{'c','c','a'};
                ProcessContext c{inputs,3,output,4,48000,silence,4};c.sourceRates=rates;
                osc.process(c);assert(std::abs(output[0]-1)<1e-5);assert(std::abs(output[1])<1e-5);assert(std::abs(output[2]+1)<1e-5);assert(std::abs(output[3])<1e-5);
                rates[2]='c';osc.process(c);for(auto x:output)assert(std::abs(x-1)<1e-5);
                c.bufferSize=1;osc.processSample(c);assert(std::abs(output[0]-1)<1e-5);
                osc.processFixedSample(c,osc.prepareSample(0,48000));assert(std::abs(output[0]-1)<1e-5);
                inputs[2]=nullptr;osc.phaseOffset=-90;osc.process(c);assert(std::abs(output[0]+1)<1e-5);
                OscillatorNode old;std::istringstream legacy("!6f1c0f366ccb47fd -1 440 wave sine -1");old.configure(legacy);assert(old.phaseOffset==0&&old.sources.size()==2);
            }
            """.formatted(payload));
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
