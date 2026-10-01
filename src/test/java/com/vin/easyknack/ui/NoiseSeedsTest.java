package com.vin.easyknack.ui;

import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.quote;

class NoiseSeedsTest {
    @Test void seedIsAControlInput(){
        var noise=new NoiseNode();assertTrue(noise.processor().inputs().contains(NoiseNode.SEED));
        assertEquals(-1.,((Number)noise.defaults().get("seed").toJavaValue()).doubleValue());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void deterministicCopiesDistinctSeedsAndSharedInstances()throws Exception{
        var f=MultipleGroupsTest.fixture(false,"mean");var asset=f.document().embeddedAssets().values().iterator().next();
        var osc=asset.graph().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof OscillatorNode).findFirst().orElseThrow();
        var noise=new NoiseNode();
        var nodes=asset.graph().nodes().stream().map(n->n.id().equals(osc.id())?new GLGraphNode(n.id(),noise.registration().id(),noise.defaults()):n).toList();
        var graph=new GLNodeGraphSnapshot(asset.graph().graphId(),asset.graph().revision(),nodes,asset.graph().connections().stream().filter(c->!c.input().nodeId().equals(osc.id())).toList());
        var modified=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),graph,asset.viewState(),asset.metadata(),asset.compositeDescriptor());
        var doc=new GLNodeGraphDocument(f.document().root(),Map.of(modified.id(),modified),List.of());
        var group=MultipleGroupsTest.compiled(new MultipleGroupsTest.Fixture(doc,f.instance()));
        var definition=NodeRegistry.require(group.typeId());
        String payload="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(group.properties()),p->-1);
        var dir=Files.createTempDirectory(Path.of("target"),"noise-seeds-").toAbsolutePath();
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(NodeRegistry.builtinDefinitions()));
        String code="""
            #include "nodes/MultipleGraphNode.hpp"
            #include "nodes/OutputNode.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                std::array<float,256> a{},b{},triggerA{},triggerB{},hzA{},hzB{},silence{},frequency{};
                float* outputsA[]{a.data(),triggerA.data(),hzA.data()};float* outputsB[]{b.data(),triggerB.data(),hzB.data()};
                float seed=123;const float* inputs[]{&seed};char control[]{'c'};
                ProcessContext x{inputs,1,a.data(),256,48000,silence.data(),256};x.sourceRates=control;
                auto y=x;y.output=b.data();
                for(auto mode:{NoiseMode::white,NoiseMode::pink,NoiseMode::brown,NoiseMode::blue}){
                    NoiseNode first,second;first.mode=second.mode=mode;first.bindIdentity("logical");second.bindIdentity("logical");
                    first.instanceCache=second.instanceCache=std::make_shared<NoiseBlockCache>();
                    seed=-1;first.process(x);second.process(y);assert(a==b);assert(first.instanceCache->renders==1&&first.instanceCache->hits==1);
                    first.process(x);second.process(y);assert(a==b);assert(first.instanceCache->renders==2);
                    // A different history must not be collapsed, even with the same seed.
                    first.process(x);first.process(x);second.process(y);assert(first.randomState!=second.randomState);
                    seed=4;first.process(x);second.process(y);assert(a==b);
                    seed=5;second.process(y);assert(a!=b);
                    assert(first.canShareChannels());
                }
                DigitalNoiseNode first,second;first.strengthValue=second.strengthValue=1;
                first.bindIdentity("digital");second.bindIdentity("digital");
                first.instanceCache=second.instanceCache=std::make_shared<NoiseBlockCache>();
                frequency.fill(1000);const float* digital[]{frequency.data(),nullptr,nullptr};char rates[]{'a','c','c'};
                x.inputs=y.inputs=digital;x.inputCount=y.inputCount=3;x.sourceRates=y.sourceRates=rates;
                x.outputs=outputsA;y.outputs=outputsB;x.outputCount=y.outputCount=3;
                ValueEventBuffer eventsA,eventsB;ValueEventBuffer* ea[]{nullptr,&eventsA,nullptr};ValueEventBuffer* eb[]{nullptr,&eventsB,nullptr};
                x.valueEventOutputs=ea;y.valueEventOutputs=eb;
                for(int block=0;block<4;block++){
                    eventsA.clear(256);eventsB.clear(256);first.process(x);second.process(y);
                    assert(a==b&&triggerA==triggerB&&hzA==hzB&&eventsA.count==eventsB.count);
                    for(int i=0;i<256;i++)assert(hzA[i]==frequency[i]*a[i]);
                    for(unsigned i=0;i<eventsA.count;i++)assert(eventsA.events[i].sampleOffset==eventsB.events[i].sampleOffset);
                }
                assert(first.instanceCache->renders==4&&first.instanceCache->hits==4);
                frequency[100]=2000;first.process(x);frequency[100]=1000;second.process(y);
                assert(first.instanceCache->renders==6); // Different audio input: no reuse.
                MultipleGraphNode group;std::istringstream p(%s);group.configure(p);group.bindIdentity("group");group.prepare(48000,256);
                ProcessContext c{nullptr,0,a.data(),256,48000,silence.data(),256};group.process(c);
                NoiseNode* n=nullptr;for(auto& node:group.state->instances[0]->runtime->graph.nodes)if(auto* candidate=dynamic_cast<NoiseNode*>(node.processor.get()))n=candidate;
                assert(n&&n->instanceCache&&n->instanceCache->renders==1&&n->instanceCache->hits==3);
                assert(group.canShareChannels());
                auto factory=[](const std::string& type)->std::unique_ptr<NodeProcessor>{
                    if(type=="noise")return std::make_unique<NoiseNode>();
                    if(type=="context")return std::make_unique<ContextInputNode>();
                    return std::make_unique<OutputNode>();
                };
                auto mono=AudioGraph::compile("GRAPH 2 n noise white @ a - o out 0 0 @ a a",factory);
                assert(!mono.nodes[0].stereo&&!mono.nodes[1].stereo);
                mono.process(a.data(),256,0,nullptr);mono.process(b.data(),256,1,nullptr);assert(a==b);
                auto stereo=AudioGraph::compile(%s,factory);
                assert(stereo.nodes[1].stereo);stereo.process(a.data(),256,0,nullptr);stereo.process(b.data(),256,1,nullptr);assert(a!=b);
            }
            """.formatted(quote(payload),quote("GRAPH 3 c context @ ccc - n noise !"+NativeContractGenerator.fingerprint(noise)+" "+noise.encode(new NodeState(noise.defaults()),p->0)+" @ a c o out 1 0 @ a a"));
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
