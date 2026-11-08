package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;
import com.vin.easyknack.compiler.CompiledManualTargets;

import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;

class SpectralInstancesTest {
    record Fixture(GLNodeGraphDocument document,GLGraphNode instance){}
    static Fixture fixture(boolean shared,boolean invalid){
        var asset=SpectralGroups.create();var graph=new GLNodeGraph(asset.graph());
        var context=asset.graph().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinContextNode).findFirst().orElseThrow();
        var osc=add(graph,new OscillatorNode());var combine=add(graph,new InstanceCombineNode());var compare=add(graph,new ComparatorNode());var discard=add(graph,new BinDiscardNode());
        connect(graph,context.id(),"frequency",osc,"frequency");connect(graph,osc,"audio-out",combine,"in");connect(graph,context.id(),"magnitude",combine,"weight");
        connect(graph,invalid?osc:context.id(),invalid?"audio-out":"magnitude",compare,"a");connect(graph,compare,ComparatorNode.OUT.value(),discard,"discard");
        graph.edit("Bin selection",e->{e.setProperty(compare,"operation",GLNodeValue.of("less"));e.setProperty(compare,"b",GLNodeValue.of(.1));e.setProperty(combine,"mode",GLNodeValue.of("mean"));});
        GLNodeId output=combine;String outputPort="out";
        if(shared){output=add(graph,new MathNode());connect(graph,combine,"out",output,"a");var id=output;graph.edit("Shared gain",e->e.setProperty(id,"b",GLNodeValue.of(.5)));outputPort="audio-out";}
        var descriptor=asset.compositeDescriptor();var ports=new ArrayList<>(descriptor.portMappings());ports.add(output("audio",SignalCategory.AUDIO,output,outputPort));
        asset=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),asset.id(),1,ports));
        var instance=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());var root=new GLNodeGraph();root.edit("Group",e->e.addNode(instance));
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),instance);
    }
    static GLNodeGraphSnapshot flatten(Fixture f){var registry=new GLCompositeGraphRegistry();registry.registerDocument(f.document());return SpectralGroups.flatten(GroupCompiler.prepare(f.document().root(),registry),new GLCompositeExpansionLimits(16,256,512,10000)).graph().orElseThrow();}
    @Test void compilerRetainsDiscardAndLowersSharedProcessing(){
        var graph=flatten(fixture(true,false));var node=graph.nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof SpectralGraphNode).findFirst().orElseThrow();
        assertEquals(1,new NodeState(node.properties()).number("audioOutputs",0));
        assertFalse(CompiledManualTargets.read(node).isEmpty());
        var program=new String(HexFormat.of().parseHex(new NodeState(node.properties()).text("program","")),java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(program.contains("bindiscard"));assertTrue(program.contains("osc"));assertTrue(program.indexOf("bindiscard")<program.indexOf("osc"));
        assertTrue(graph.nodes().stream().anyMatch(n->NodeRegistry.require(n.typeId()) instanceof MathNode));
        assertThrows(IllegalArgumentException.class,()->flatten(fixture(false,true)));
    }
    @Test void exampleIsEditableSerializableAndCompiles()throws Exception{
        var document=SpectralResynthesisExample.create();var registry=new GLCompositeGraphRegistry();registry.registerDocument(document);
        var flat=SpectralGroups.flatten(GroupCompiler.prepare(document.root(),registry),new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(flat.isValid());
        assertTrue(com.vin.easyknack.audio.AudioGraphEncoder.encode(flat.graph().orElseThrow()).contains("spectralgraph"));
        var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(document,bytes);
        assertEquals(NodeDocuments.comparable(document),NodeDocuments.comparable(codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion())));
        for(var asset:document.embeddedAssets().values())assertTrue(asset.compositeDescriptor().portMappings().stream().noneMatch(MultipleGroups::countPort));
    }
    @ParameterizedTest @ValueSource(booleans={false,true}) @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void continuousVoicesDiscardNormalizationTimingAndExport(boolean shared)throws Exception{
        var f=fixture(shared,false);var graph=flatten(f);var node=graph.nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof SpectralGraphNode).findFirst().orElseThrow();var definition=NodeRegistry.require(node.typeId());
        String payload="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(node.properties()),p->-1);
        var export=GroupSourceExporter.create(f.document(),f.instance());var defs=new ArrayList<>(NodeRegistry.builtinDefinitions());defs.add(export.definition());
        var dir=Files.createTempDirectory(Path.of("target"),"spectral-instances-").toAbsolutePath();
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(defs));Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        String exported="!"+NativeContractGenerator.fingerprint(export.definition())+" "+export.definition().encode(new NodeState(export.definition().defaults()),p->-1);
        String code="""
            #include "%s.hpp"
            #include <cassert>
            #include <iostream>
            using namespace easyknack;
            int main(){
                SpectralGraphNode node;std::istringstream p(%s);node.configure(p);node.prepare(48000,256);
                %s baked;std::istringstream b(%s);baked.configure(b);baked.prepare(48000,256);
                SpectrumStream input;input.prepare(256);input.size=input.windowSize=16;input.hop=64;input.sampleRate=48000;input.window=SpectrumWindow::rectangular;input.identity=123;
                std::array<float,256> dummy{},out{},other{},silence{};float* outputs[]{dummy.data(),out.data()};float* bakedOutputs[]{dummy.data(),other.data()};const DspResource* resources[]{&input};
                const float* numericInputs[]{nullptr};ProcessContext c{numericInputs,1,dummy.data(),256,48000,silence.data(),256};c.outputs=outputs;c.outputCount=2;c.resourceInputs=resources;
                auto d=c;d.outputs=bakedOutputs;
                std::uint64_t time=0;
                for(int block=0;block<36;block++){
                    input.blockStart=time;input.count=block%%3==0?1:0;input.frames[0].start=std::int64_t(time)-16;
                    input.frames[0].bins[1]={1,0};
                    node.process(c);baked.process(d);assert(node.activeBins==9);
                    for(int i=0;i<256;i++){assert(std::abs(out[i]*%s-other[i])<1e-5f);if(block>20)assert(std::abs(out[i]-std::sin(6.283185307179586*(time+i)/16))<1e-4);}
                    assert(node.activeVoices.size()==1);time+=256;
                }
                assert(node.discardedBinFrames>0&&node.renderedBinSamples==time);
                // Discard: bounded fade then no further oscillator work; Mean does not include silent bins.
                input.blockStart=time;input.count=1;input.frames[0].start=std::int64_t(time)-16;input.frames[0].bins[1]={};node.process(c);time+=256;
                assert(node.activeVoices.empty());assert(node.stream.frames[0].bins[1]==std::complex<double>{});auto work=node.renderedBinSamples;
                input.blockStart=time;input.count=0;node.process(c);assert(node.renderedBinSamples==work);for(float v:out)assert(v==0);time+=256;
                // A new frame halfway through a block must not become audible before its timestamp.
                input.blockStart=time;input.count=1;input.frames[0].start=std::int64_t(time)+128-16;input.frames[0].bins[1]={1,0};node.process(c);
                for(int i=0;i<128;i++)assert(out[i]==0);assert(node.renderedBinSamples==work+128);assert(out[130]!=0);
                // Changing FFT resolution changes Count without callback allocation.
                input.blockStart=time+256;input.size=input.windowSize=32;input.frames[0].start=std::int64_t(input.blockStart)-32;node.process(c);assert(node.activeBins==17);
                SpectralGraphNode next;std::istringstream again(%s);next.configure(again);next.prepare(48000,256);next.adoptState(node);assert(next.activeBins==17&&next.activeVoices.size()==1);
                // Stereo state stays independent despite sharing the logical node identity.
                SpectralGraphNode right;std::istringstream rp(%s);right.configure(rp);right.shareChannelState(next);right.prepare(48000,256);
                input.frames[0].bins[1]={};input.frames[0].bins[2]={1,0};right.process(c);
                assert(right.activeVoices.size()==1&&right.activeVoices[0]==2);assert(next.activeVoices[0]==1);
                // Losing and restoring the spectrum must not retain stale active bins.
                resources[0]=nullptr;right.process(c);assert(right.activeBins==0&&right.activeVoices.empty());for(float v:out)assert(v==0);
                resources[0]=&input;right.process(c);assert(right.activeBins==17&&right.activeVoices[0]==2);
                // Bake binds the host identity after inner preparation as well.
                right.bindIdentity("spectral-reset");
                for(std::size_t s=0;s<right.steps.size();s++)if(right.steps[s].type=="osc"){
                    auto& osc=static_cast<OscillatorNode&>(*right.voices[2].processors[s]);
                    auto target="spectral-reset/"+std::to_string(s)+":reset";
                    assert(osc.reset.mailbox==bindManualTrigger(target));
                    submitManualTrigger(target);++manualTriggerEpoch;right.process(c);
                    assert(osc.reset.seen==osc.reset.mailbox->submitted.load());
                }
                // Bin Read keeps raw coefficients and provides normalized amplitude separately.
                BinReadNode reader;reader.frame=&input.frames[0];reader.size=32;reader.rate=48000;reader.windowSum=32;reader.index=1;
                input.frames[0].bins[1]={16,0};float values[6]{};float* ports[]{&values[0],&values[1],&values[2],&values[3],&values[4],&values[5]};
                ProcessContext read{nullptr,0,values,1,48000,silence.data()};read.outputs=ports;read.outputCount=6;reader.process(read);
                assert(values[2]==16&&values[4]==1&&values[5]==1500);
                std::cout<<"Spectral voices, retained frames, discard, normalization, frame timing, resolution and export passed\\n";
            }
            """.formatted(export.className(),quote(payload),export.className(),quote(exported),shared?".5f":"1.f",quote(payload),quote(payload));
        var path=dir.resolve("test.cpp");Files.writeString(path,code);RateConverterTest.compileAndRun(path,dir.resolve("test.exe"),dir);
    }
}
