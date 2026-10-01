package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import com.vin.easyknack.nodes.*;
import java.nio.file.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class EnvelopeInputsTest {
    @TempDir Path temp;
    @Test void parametersAreConnectableAndHoldDefaultsOn() {
        var node=new EnvelopeNode();
        assertEquals(true,node.defaults().get("hold").toJavaValue());
        var rows=node.layout(new NodeState(node.defaults())).rows();
        for(var id:List.of(EnvelopeNode.DURATION,EnvelopeNode.RELEASE,EnvelopeNode.SUSTAIN))
            assertEquals(NodeBuilder.RowKind.INPUT,rows.stream().filter(r->r.key().equals(id)).findFirst().orElseThrow().kind());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void holdAndConnectedParametersWorkWithScalarAndAudioInputs() throws Exception {
        var source=temp.resolve("envelope.cpp");
        Files.writeString(source,"""
            #include "AudioGraph.hpp"
            #include "nodes/EnvelopeNode.hpp"
            #include <cassert>
            #include <sstream>
            using namespace easyknack;
            int main(){
                const std::string curve="3 0 0 0 0 0 0 0 .5 1 .5 1 .5 1 0 1 0 1 0 1 0 0";
                auto init=[&](EnvelopeNode& n){std::istringstream in(curve);n.curve.read(in);n.gateValue=1;n.duration=2;n.release=2;};
                float out[16]{},silence[16]{};const float* inputs[5]{};
                ProcessContext c{inputs,5,out,16,1000,silence,16};
                EnvelopeNode held;init(held);held.process(c);assert(held.stage==EnvelopeNode::HOLD&&out[15]==1);
                EnvelopeNode settings;init(settings);settings.hold=false;held.applyParameters(settings);
                held.process(c);assert(held.stage==EnvelopeNode::IDLE&&out[15]==0);
                EnvelopeNode shot;init(shot);shot.hold=false;shot.process(c);assert(out[2]==1&&out[5]==0&&out[15]==0);
                shot.process(c);for(float x:out)assert(x==0); // High gate does not retrigger.
                float duration=4,release=3,sustain=.25;
                inputs[2]=&duration;inputs[3]=&release;inputs[4]=&sustain;c.sourceRates="aaccc";
                EnvelopeNode modulated;init(modulated);modulated.hold=false;modulated.process(c);
                assert(out[4]==.5f&&out[7]>0&&out[8]==0); // All three connected scalars affect timing/level.
                float durations[16]{};inputs[2]=durations;c.sourceRates="aaacc";
                EnvelopeNode audio;init(audio);audio.hold=false;audio.process(c);assert(out[0]==.5f&&out[4]==0);
                EnvelopeNode old;std::istringstream legacy("!a11bbab0677e0e68 -1 1 -1 0 2 2 .5 "+curve);
                old.configure(legacy);assert(old.hold&&old.sources.size()==2);
                EnvelopeNode current;std::istringstream modern("!10ad0836639a66ae -1 1 -1 0 2 2 3 2 4 .5 0 "+curve);
                current.configure(modern);assert(!current.hold&&current.sources[2]==2&&current.sources[4]==4);
            }
            """);
        RateConverterTest.compileAndRun(source,temp.resolve("envelope.exe"),temp);
    }
}
