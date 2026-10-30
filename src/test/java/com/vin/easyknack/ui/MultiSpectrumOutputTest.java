package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class MultiSpectrumOutputTest {
    record Fixture(GLNodeGraphDocument document,GLGraphNode instance){}
    static Fixture fixture(boolean instances,String mode){
        var base=SpectralGroups.create();var graph=new GLNodeGraph(base.graph());
        var context=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinContextNode).findFirst().orElseThrow();
        var first=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinOutputNode).findFirst().orElseThrow();
        var second=add(graph,new BinOutputNode());var low=add(graph,new ComparatorNode());var high=add(graph,new ComparatorNode());
        graph.edit("Complementary masks",e->{
            for(var w:graph.snapshot().connections())e.removeConnection(w.id());
            e.setProperty(low,"operation",GLNodeValue.of("less"));e.setProperty(low,"b",GLNodeValue.of(1000.));
            e.setProperty(high,"operation",GLNodeValue.of("greater_equal"));e.setProperty(high,"b",GLNodeValue.of(1000.));
            e.addConnection(SpectralGroupsTest.wire(context.id(),"index",low,"a"));e.addConnection(SpectralGroupsTest.wire(context.id(),"index",high,"a"));
        });
        for(var pair:List.of(List.of(first.id(),low),List.of(second,high)))for(String component:List.of("real","imaginary")){
            var multiply=add(graph,new MathNode());graph.edit("Mask component",e->{
                e.setProperty(multiply,"operation",GLNodeValue.of("multiply"));e.setProperty(multiply,"clamp",GLNodeValue.of(false));
                e.addConnection(SpectralGroupsTest.wire(context.id(),component,multiply,"a"));e.addConnection(SpectralGroupsTest.wire(pair.get(1),"value-out",multiply,"b"));
                e.addConnection(SpectralGroupsTest.wire(multiply,"audio-out",pair.get(0),component.equals("real")?"a":"b"));
            });
        }
        var mappings=new ArrayList<>(base.compositeDescriptor().portMappings());mappings.add(new GLCompositePortMapping(GLNodePortDescriptor.output("high",SignalCategory.SPECTRUM.valueTypeId()),new GLNodeEndpoint(second,"spectrum")));
        var descriptor=new GLCompositeNodeDescriptor(base.compositeDescriptor().nodeTypeId(),base.id(),1,mappings);
        var spectral=new GLNodeGraphAsset(base.id(),1,base.kind(),graph.snapshot(),base.viewState(),base.metadata(),descriptor);
        var assets=new HashMap<String,GLNodeGraphAsset>();assets.put(spectral.id(),spectral);
        if(instances){
            var inner=new GLNodeGraph();var nested=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());inner.edit("Nested split",e->e.addNode(nested));
            var ports=List.of(MultipleGroups.countPort("Count"),input("spectrum",SignalCategory.SPECTRUM,nested.id(),"spectrum"),output("low",SignalCategory.SPECTRUM,nested.id(),"out"),output("high",SignalCategory.SPECTRUM,nested.id(),"high"));
            descriptor=new GLCompositeNodeDescriptor("test.instance.spectra","instances",1,ports);
            var metadata=Map.of(MultipleGroups.MARKER,GLNodeValue.of(true),MultipleGroups.MAXIMUM,GLNodeValue.of("2"),MultipleGroups.MERGE,GLNodeValue.of(mode),MultipleGroups.EXECUTION,GLNodeValue.of(2.));
            assets.put("instances",new GLNodeGraphAsset("instances",1,GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),GLNodeEditorViewState.empty(),metadata,descriptor));
        }
        var root=new GLNodeGraph();var source=add(root,new FFTStreamNode());var instance=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),instances?Map.of(GroupUnits.key("Count"),GLNodeValue.of(2.)):Map.of());root.edit("Root",e->{e.addNode(instance);e.addConnection(SpectralGroupsTest.wire(source,"spectrum",instance.id(),"spectrum"));});
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),assets,List.of()),instance);
    }
    static GLGraphNode compiled(Fixture fixture){
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(fixture.document());
        var result=SpectralGroups.flatten(GroupCompiler.prepare(fixture.document().root(),registry),new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(result.isValid());
        return result.graph().orElseThrow().nodes().stream().filter(n->n.id().equals(fixture.instance().id())).findFirst().orElseThrow();
    }
    static Fixture mixed(){
        var f=fixture(true,"sum");var asset=f.document().embeddedAssets().get("instances");var graph=new GLNodeGraph(asset.graph());var merge=add(graph,new InstanceCombineNode());
        graph.edit("Numeric output",e->{e.setProperty(merge,"in",GLNodeValue.of(3.));e.setProperty(merge,"mode",GLNodeValue.of("sum"));});
        var ports=new ArrayList<>(asset.compositeDescriptor().portMappings());ports.add(output("audio",SignalCategory.AUDIO,merge,"out"));
        var d=new GLCompositeNodeDescriptor(asset.compositeDescriptor().nodeTypeId(),asset.id(),1,ports);
        return new Fixture(f.document().withEmbeddedAsset(new GLNodeGraphAsset(asset.id(),1,asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),d)),f.instance());
    }
    @Test void mixedSpectrumAndNumericBoundariesRetainTheirTypes(){
        var f=mixed();var node=compiled(f);var state=new NodeState(node.properties());assertEquals(3,state.number("outputs",0));assertEquals("1,2",state.text("spectrumOutputs",""));assertNotNull(GroupSourceExporter.create(f.document(),f.instance()));
    }
    @Test void splitKeepsOneSharedBinPlanAndTypedInstanceOutputs(){
        for(boolean instances:List.of(false,true)){
            var f=fixture(instances,"mean");var node=compiled(f);var state=new NodeState(node.properties());
            var layout=NodeRegistry.require(node.typeId()).layout(state);
            assertEquals(2,layout.ports().stream().filter(p->p.direction()==GLPortDirection.OUTPUT&&p.valueTypeId().equals(SignalCategory.SPECTRUM.valueTypeId())).count());
            var exported=GroupSourceExporter.create(f.document(),f.instance());assertNotNull(exported);
            if(!instances){String plan=new String(HexFormat.of().parseHex(state.text("program","")),java.nio.charset.StandardCharsets.UTF_8);assertEquals(1,plan.split("bincontext",-1).length-1);assertEquals(2,plan.split("binoutput",-1).length-1);assertTrue(exported.files().get("native/"+exported.className()+".hpp").contains("writeBin(frame,k)"));}
        }
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void complementaryComplexOutputsReconstructInLiveAndExportedKernels()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"multi-spectrum-").toAbsolutePath();var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());
        var includes=new StringBuilder();var checks=new StringBuilder();
        for(boolean instances:List.of(false,true))for(String mode:instances?List.of("sum","mean"):List.of("mean")){
            var f=fixture(instances,mode);var node=compiled(f);var def=NodeRegistry.require(node.typeId());
            var export=GroupSourceExporter.create(f.document(),f.instance());definitions.add(export.definition());
            Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));includes.append("#include \"").append(export.className()).append(".hpp\"\n");
            String direct="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(node.properties()),p->p.value().equals("spectrum")?0:p.value().equals("input0")?1:-1);
            String baked="!"+NativeContractGenerator.fingerprint(export.definition())+" "+export.definition().encode(new NodeState(export.definition().defaults()),p->p.value().equals(instances?"input_1":"input_0")?1:-1);
            checks.append("check<").append(instances?"MultipleGraphNode":"SpectralGraphNode").append(">(").append(quote(direct)).append(',').append(instances?1:0).append(',').append(mode.equals("sum")?2:1).append(");\n");
            checks.append("check<").append(export.className()).append(">(").append(quote(baked)).append(',').append(instances?1:0).append(',').append(mode.equals("sum")?2:1).append(");\n");
            var isolated=new GLNodeGraph();isolated.edit("Compiled boundary",e->e.addNode(node));
            checks.append("{auto graph=AudioGraph::parse(").append(quote(com.vin.easyknack.audio.AudioGraphEncoder.encode(isolated.snapshot()))).append(");assert(graph.nodes.size()==1);}\n");
        }
        var mixed=mixed();var mixedNode=compiled(mixed);var md=NodeRegistry.require(mixedNode.typeId());
        checks.append("checkMixed<MultipleGraphNode>(").append(quote("!"+NativeContractGenerator.fingerprint(md)+" "+md.encode(new NodeState(mixedNode.properties()),p->-1))).append(",0,1,2);\n");
        var export=GroupSourceExporter.create(mixed.document(),mixed.instance());definitions.add(export.definition());Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));includes.append("#include \"").append(export.className()).append(".hpp\"\n");
        checks.append("checkMixed<").append(export.className()).append(">(").append(quote("!"+NativeContractGenerator.fingerprint(export.definition())+" "+export.definition().encode(new NodeState(export.definition().defaults()),p->-1))).append(",2,0,1);\n");
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String code="""
            #include "AudioGraph.hpp"
            INCLUDES
            #include <cassert>
            using namespace easyknack;
            template<class T> void check(const char* payload,int resourcePort,int gain){
                auto node=std::make_unique<T>();auto right=std::make_unique<T>();std::istringstream params(payload),rp(payload);node->configure(params);right->configure(rp);right->shareChannelState(*node);node->prepare(48000,256);right->prepare(48000,256);
                SpectrumStream source;source.prepare(256);source.size=source.windowSize=4096;source.hop=256;source.count=1;source.blockFrames=256;source.sampleRate=48000;source.identity=8;
                for(std::size_t k=0;k<=source.size/2;k++)source.frames[0].bins[k]={double(k+1),k&&k!=source.size/2?.125:0};SpectrumStream::realSymmetry(source.frames[0],source.size);
                SpectrumStream other;other.prepare(256);other.copy(source);for(std::size_t k=0;k<source.size;k++)other.frames[0].bins[k]*=2;
                float silence[256]{},out[256]{},count=2;const float* inputs[17]{};inputs[0]=&count;char rates[17]{};std::fill_n(rates,17,'c');const DspResource* resources[17]{};resources[resourcePort]=&source;
                const DspResource* rightResources[17]{};rightResources[resourcePort]=&other;
                ProcessContext c{inputs,17,out,256,48000,silence,256};c.resourceInputs=resources;c.rightResourceInputs=rightResources;c.sourceRates=rates;
                assert(node->outputCount()==2);assert(node->outputResourceKind(0)==ResourceKind::spectrum&&node->outputResourceKind(1)==ResourceKind::spectrum);
                for(int block=0;block<3;block++){
                    source.blockStart=other.blockStart=256*block;source.frames[0].start=other.frames[0].start=std::int64_t(source.blockStart)+256-4096;node->process(c);
                    auto* a=static_cast<SpectrumStream*>(node->resourceOutput(0));auto* b=static_cast<SpectrumStream*>(node->resourceOutput(1));assert(a&&b&&a!=b&&a->count==1&&b->count==1);assert(a->frames[0].start==b->frames[0].start);
                    for(std::size_t k=0;k<source.size;k++){
                        auto original=source.frames[0].bins[k]*double(gain);assert(std::abs(a->frames[0].bins[k]+b->frames[0].bins[k]-original)<1e-7);
                        auto folded=std::min(k,source.size-k);assert((folded<1000?b:a)->frames[0].bins[k]==std::complex<double>{});
                    }
                    auto rc=c;rc.channelIndex=1;rc.resourceInputs=rightResources;right->process(rc);auto* ra=static_cast<SpectrumStream*>(right->resourceOutput(0));auto* rb=static_cast<SpectrumStream*>(right->resourceOutput(1));assert(ra&&rb&&ra->count==1&&rb->count==1);
                    for(std::size_t k=0;k<source.size;k++)assert(std::abs(ra->frames[0].bins[k]+rb->frames[0].bins[k]-other.frames[0].bins[k]*double(gain))<1e-7);
                }
                if constexpr(std::is_base_of_v<MultipleGraphNode,T>){auto mismatch=source.hop;source.hop++;node->mergeSpectrum(0,0,&source);assert(node->state->invalidSpectra[0][0]);source.hop=mismatch;}
                source.count=other.count=0;source.blockStart+=256;other.blockStart=source.blockStart;node->process(c);assert(static_cast<SpectrumStream*>(node->resourceOutput(0))->count==0&&static_cast<SpectrumStream*>(node->resourceOutput(1))->count==0);
            }
            template<class T> void checkMixed(const char* payload,int audio,int low,int high){
                auto node=std::make_unique<T>();std::istringstream params(payload);node->configure(params);node->prepare(48000,256);
                SpectrumStream source;source.prepare(256);source.size=source.windowSize=8;source.hop=256;source.count=1;source.blockFrames=256;source.sampleRate=48000;source.identity=8;source.frames[0].bins[1]={4,2};SpectrumStream::realSymmetry(source.frames[0],8);
                float count=2,silence[256]{},data[3][256]{};float* outputs[]={data[0],data[1],data[2]};const float* inputs[17]{};inputs[0]=&count;char rates[17]{};std::fill_n(rates,17,'c');const DspResource* resources[17]{};resources[1]=&source;
                ProcessContext c{inputs,17,data[0],256,48000,silence,256};c.outputs=outputs;c.outputCount=3;c.resourceInputs=resources;c.sourceRates=rates;node->process(c);
                assert(node->outputCount()==3&&std::abs(data[audio][255]-6)<1e-6);
                auto* a=static_cast<SpectrumStream*>(node->resourceOutput(low));auto* b=static_cast<SpectrumStream*>(node->resourceOutput(high));assert(a&&b&&a->count==1&&b->count==1);assert(std::abs(a->frames[0].bins[1]-std::complex<double>(8,4))<1e-7);assert(b->frames[0].bins[1]==std::complex<double>{});
            }
            int main(){CHECKS}
            """.replace("INCLUDES",includes).replace("CHECKS",checks);
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
