package com.vin.easyknack.ui;

import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.quote;

class InstanceSourceSpecializationTest {
    private static MultipleGroupsTest.Fixture fixture(){
        var f=MultipleGroupsTest.fixture(false,"mean");var a=f.document().embeddedAssets().values().iterator().next();var d=a.compositeDescriptor();
        // Public Count need not be the first port. The inner execution order is independent.
        var ports=new ArrayList<>(d.portMappings());Collections.reverse(ports);
        var asset=new GLNodeGraphAsset(a.id(),a.version(),a.kind(),a.graph(),a.viewState(),a.metadata(),new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports));
        return new MultipleGroupsTest.Fixture(new GLNodeGraphDocument(f.document().root(),Map.of(a.id(),asset),List.of()),f.instance());
    }
    private static MultipleGroupsTest.Fixture noiseFixture(boolean direct){
        var f=fixture();var a=f.document().embeddedAssets().values().iterator().next();var graph=new GLNodeGraph(a.graph());
        var osc=a.graph().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof OscillatorNode).findFirst().orElseThrow();
        var wire=a.graph().connections().stream().filter(c->c.input().equals(new GLNodeEndpoint(osc.id(),"frequency"))).findFirst().orElseThrow();
        var noise=AdvancedSourceExportTest.add(graph,new DigitalNoiseNode());var math=AdvancedSourceExportTest.add(graph,new MathNode());
        graph.edit("Noise frequency modulation",e->{
            e.removeConnection(wire.id());e.setProperty(noise,"strength",GLNodeValue.of(.7));e.setProperty(noise,"seed",GLNodeValue.of(123.));
            e.addConnection(SpectralGroupsTest.wire(wire.output().nodeId(),wire.output().portId(),noise,"frequency"));
            e.addConnection(SpectralGroupsTest.wire(wire.output().nodeId(),wire.output().portId(),math,"a"));
            e.addConnection(SpectralGroupsTest.wire(noise,"factor",math,"b"));
            e.addConnection(SpectralGroupsTest.wire(direct?noise:math,direct?"frequency-out":"audio-out",osc.id(),"frequency"));
        });
        var asset=new GLNodeGraphAsset(a.id(),a.version(),a.kind(),graph.snapshot(),a.viewState(),a.metadata(),a.compositeDescriptor());
        return new MultipleGroupsTest.Fixture(new GLNodeGraphDocument(f.document().root(),Map.of(asset.id(),asset),List.of()),f.instance());
    }
    @Test void bakeAndExportUseTheSameSpecialization(){
        var f=fixture();var export=GroupSourceExporter.create(f.document(),f.instance());var bake=GroupSourceExporter.create(f.document(),f.instance(),true);
        String code=export.files().get("native/"+export.className()+".hpp");
        String local=bake.files().get("native/"+bake.className()+".hpp").replace(bake.className(),export.className()).replace("local_baked_","baked_");
        assertEquals(code,local);
        assertTrue(code.contains("struct CompiledInstance"));assertTrue(code.contains("MathNode::evaluate("));
        assertTrue(code.contains("processInstances<CompiledInstance>"));assertFalse(code.contains(":BakedGraphProcessor"));
        assertTrue(code.contains("audioKernel.process<CompiledInstance>(*this,inner)"));
    }
    @ParameterizedTest @ValueSource(ints={0,1,2}) @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void generatedKernelPreservesRatesChannelsDefaultsAndState(int variant)throws Exception{
        var f=variant>0?noiseFixture(variant==2):fixture();var export=GroupSourceExporter.create(f.document(),f.instance());var definition=export.definition();
        if(variant>0)assertTrue(export.files().get("native/"+export.className()+".hpp").contains("DigitalNoiseNode::process(c)"));
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(f.document());
        var node=MultipleGroups.compile(f.instance().withoutProperty(MultipleGroups.COUNT_PROPERTY),f.document().embeddedAssets().values().iterator().next(),registry,
            new GLCompositeExpansionLimits(16,256,512,10000),Set.of("frequency"),new HashMap<>()).nodes().get(0);
        var liveDef=NodeRegistry.require(node.typeId());
        String live="!"+NativeContractGenerator.fingerprint(liveDef)+" "+liveDef.encode(new NodeState(node.properties()),p->-1);
        String compiled="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(definition.defaults()),p->-1);
        var dir=Files.createTempDirectory(Path.of("target"),"instance-source-").toAbsolutePath();
        Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(definition);
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String code="""
            #include "%s.hpp"
            #include <cassert>
            #include <iostream>
            using namespace easyknack;
            using Compiled=%s;
            int main(){
                MultipleGraphNode live,reference;Compiled baked;
                std::istringstream l(%s),b(%s);live.configure(l);baked.configure(b);
                std::istringstream refParams(l.str());reference.configure(refParams);
                for(auto& instance:reference.state->instances)instance->reductionOsc=-1;
                assert(live.canShareChannels()&&baked.canShareChannels());
                CompiledScalarMemo memo;int evaluations=0;
                auto evaluate=[&]{++evaluations;return 8.f;};
                assert(memo.evaluate(0,2,3,48000,evaluate)==8);
                memo.evaluate(0,2,3,48000,evaluate);assert(evaluations==1);
                memo.evaluate(0,2,4,48000,evaluate);assert(evaluations==2);
                memo.evaluate(0,2,4,44100,evaluate);assert(evaluations==3);
                live.prepare(48000,256);baked.prepare(48000,256);reference.prepare(48000,256);
                std::array<float,256> x{},y{},z{},silence{},frequency{},right{};
                float count=4;const float* inputs[]{&count,frequency.data()};const float* inputRight[]{&count,right.data()};
                const float* reordered[]{frequency.data(),&count};const float* reorderedRight[]{right.data(),&count};
                char rates[]{'c','c'},reorderedRates[]{'c','c'};
                ProcessContext a{inputs,2,x.data(),256,48000,silence.data(),256};a.sourceRates=rates;
                auto c=a;c.inputs=reordered;c.output=y.data();c.sourceRates=reorderedRates;
                for(int block=0;block<150;block++){
                    int frames=block%%3==0?17:256;a.bufferSize=a.blockFrames=c.bufferSize=c.blockFrames=frames;
                    count=block<20?4:block<40?1:block<55?0:4;
                    rates[1]=reorderedRates[0]=block>=60&&block<80?'a':'c';
                    a.rightInputs=block>=80&&block<100?inputRight:nullptr;
                    c.rightInputs=block>=80&&block<100?reorderedRight:nullptr;
                    for(int i=0;i<256;i++){frequency[i]=220+block+(rates[1]=='a'?i*.1f:0);right[i]=frequency[i]+90;}
                    // Simulate inherited, not-yet-settled internal manual values.
                    if(block==110)for(MultipleGraphNode* group:{&live,&reference,static_cast<MultipleGraphNode*>(&baked)})
                        for(auto& instance:group->state->instances)for(auto& node:instance->runtime->graph.nodes)
                            if(auto* math=dynamic_cast<MathNode*>(node.processor.get()))if(math->sources[1]<0)math->currentB=.25f;
                    for(int channel=0;channel<2;channel++){
                        a.channelIndex=c.channelIndex=channel;live.process(a);baked.process(c);
                        auto r=a;r.output=z.data();reference.process(r);
                        for(int i=0;i<frames;i++)if(std::abs(x[i]-z[i])>3e-6f){std::cerr<<"reference "<<block<<":"<<i<<" "<<x[i]<<" != "<<z[i]<<"\\n";return 1;}
                        for(int i=0;i<frames;i++)if(std::abs(x[i]-y[i])>3e-6f){std::cerr<<block<<":"<<i<<" "<<x[i]<<" != "<<y[i]<<"\\n";return 1;}
                    }
                }
                // Missing external input uses the exported slider value; live changes retain phase.
                reordered[0]=nullptr;c.rightInputs=nullptr;c.channelIndex=0;c.bufferSize=c.blockFrames=256;
                Compiled parameters;std::istringstream p(%s);parameters.configure(p);parameters.defaults[0]=550;baked.applyParameters(parameters);
                std::fill(frequency.begin(),frequency.end(),550);a.rightInputs=nullptr;a.channelIndex=0;a.bufferSize=a.blockFrames=256;
                for(int block=0;block<10;block++){live.process(a);baked.process(c);for(int i=0;i<256;i++)assert(std::abs(x[i]-y[i])<3e-6f);}
                Compiled replacement;std::istringstream r(%s);replacement.configure(r);replacement.prepare(48000,256);replacement.applyParameters(parameters);replacement.adoptState(baked);
                live.process(a);replacement.process(c);for(int i=0;i<256;i++)assert(std::abs(x[i]-y[i])<3e-6f);
                // Prove the audio kernel is exercised, not merely its fallback.
                CompiledInstanceAudio probe;auto direct=a;direct.output=y.data();
                live.process(a);assert(probe.process<Compiled::CompiledInstance>(replacement,direct));
                for(int i=0;i<256;i++)assert(std::abs(x[i]-y[i])<3e-6f);
                direct.rightInputs=inputRight;
                assert(!probe.process<Compiled::CompiledInstance>(replacement,direct));
            }
            """.formatted(export.className(),export.className(),quote(live),quote(compiled),quote(compiled),quote(compiled));
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
