package com.vin.easyknack.ui;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.quote;

class InstanceReductionTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void fusedReductionMatchesSchedulerAcrossRatesFadesAndZeroWeight()throws Exception{
        var fixture=MultipleGroupsTest.fixture(false,"mean");
        var registry=new com.vingame.glswing.node.composite.GLCompositeGraphRegistry();registry.registerDocument(fixture.document());
        var n=MultipleGroups.compile(fixture.instance().withoutProperty(MultipleGroups.COUNT_PROPERTY),fixture.document().embeddedAssets().values().iterator().next(),registry,
            new com.vingame.glswing.node.composite.GLCompositeExpansionLimits(16,256,512,10000),java.util.Set.of("frequency"),new java.util.HashMap<>()).nodes().get(0);
        var def=NodeRegistry.require(n.typeId());
        String parameters="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(n.properties()),p->-1);
        var dir=Files.createTempDirectory(Path.of("target"),"instance-reduction-").toAbsolutePath();
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(NodeRegistry.builtinDefinitions()));
        String code="""
            #include "nodes/MultipleGraphNode.hpp"
            #include <cassert>
            #include <iostream>
            using namespace easyknack;
            int main(){
                MultipleGraphNode fast,slow;
                std::istringstream a(%s),b(%s);fast.configure(a);slow.configure(b);
                fast.prepare(48000,256);slow.prepare(48000,256);
                for(auto& instance:slow.state->instances)instance->reductionOsc=-1;
                std::array<float,256> x{},y{},silence{},hz{},right{};
                float count=4;const float* inputs[]{&count,hz.data()};const float* stereo[]{&count,right.data()};char rates[]{'c','c'};
                ProcessContext c{inputs,2,x.data(),256,48000,silence.data(),256};c.sourceRates=rates;
                for(int block=0;block<120;block++){
                    count=block<15?4:block<30?1:block<45?0:4;
                    rates[1]=block>=60&&block<80?'a':'c';
                    c.rightInputs=block>=80&&block<100?stereo:nullptr;
                    for(int i=0;i<256;i++){hz[i]=220+block+(rates[1]=='a'?i*.1f:0);right[i]=hz[i]+100;}
                    for(auto* group:{&fast,&slow})for(auto& instance:group->state->instances){
                        auto& combine=static_cast<InstanceCombineNode&>(*instance->runtime->graph.nodes[instance->runtime->outputs[0]].processor);
                        combine.mode=block<40?"sum":block<80?"mean":"weighted";
                        auto& other=static_cast<InstanceCombineNode&>(*instance->runtime->graph.nodes[instance->runtime->outputs[0]].rightProcessor);
                        combine.weight=other.weight=block>=10&&block<20?0.f:.3f;other.mode=combine.mode;
                    }
                    for(int channel=0;channel<2;channel++){
                        c.channelIndex=channel;c.output=x.data();fast.process(c);c.output=y.data();slow.process(c);
                        for(int i=0;i<256;i++)if(std::abs(x[i]-y[i])>2e-6f){std::cerr<<block<<":"<<i<<" "<<x[i]<<" != "<<y[i]<<"\\n";return 1;}
                    }
                    if(block==0)assert(fast.state->instances[0]->reductionReady());
                    if(block==65)assert(fast.state->instances[0]->reductionReady()); // Mono audio modulation now stays optimized.
                    if(block==85)assert(!fast.state->instances[0]->reductionReady()); // Stereo still uses the scheduler.
                }
                // A silent interval must advance the oscillator exactly as the reference.
                for(int i=0;i<4;i++){
                    auto phase=[](auto& instance){for(auto& node:instance.runtime->graph.nodes)if(auto* osc=dynamic_cast<OscillatorNode*>(node.processor.get()))return osc->phase;return -1.;};
                    assert(std::abs(phase(*fast.state->instances[i])-phase(*slow.state->instances[i]))<1e-10);
                }
            }
            """.formatted(quote(parameters),quote(parameters));
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
