package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class WideGroupOutputsTest {
    static MultiSpectrumOutputTest.Fixture numericFixture(){
        var asset=MultipleGroups.create();var graph=new GLNodeGraph();var ports=new ArrayList<GLCompositePortMapping>();ports.add(MultipleGroups.countPort("Count"));
        for(int i=0;i<12;i++){
            var combine=add(graph,new InstanceCombineNode());final int value=i+1;graph.edit("Numeric contribution",e->{e.setProperty(combine,"in",GLNodeValue.of(value));e.setProperty(combine,"mode",GLNodeValue.of("sum"));});ports.add(output("out"+i,SignalCategory.AUDIO,combine,"out"));
        }
        var descriptor=new GLCompositeNodeDescriptor(asset.compositeDescriptor().nodeTypeId(),asset.id(),1,ports);var metadata=new HashMap<>(asset.metadata());metadata.put(MultipleGroups.MAXIMUM,GLNodeValue.of("2"));
        asset=new GLNodeGraphAsset(asset.id(),1,asset.kind(),graph.snapshot(),GLNodeEditorViewState.empty(),metadata,descriptor);
        var instance=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of(GroupUnits.key("Count"),GLNodeValue.of(2.)));var root=new GLNodeGraph();root.edit("Root",e->e.addNode(instance));
        return new MultiSpectrumOutputTest.Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),instance);
    }
    static MultiSpectrumOutputTest.Fixture fixture(boolean instances){
        var asset=SpectralGroups.create();var graph=new GLNodeGraph();var context=add(graph,new BinContextNode());var modulo=add(graph,new MathNode());
        graph.edit("Shared selection",e->{e.setProperty(modulo,"operation",GLNodeValue.of("modulo"));e.setProperty(modulo,"b",GLNodeValue.of(12.));e.setProperty(modulo,"clamp",GLNodeValue.of(false));e.addConnection(SpectralGroupsTest.wire(context,"index",modulo,"a"));});
        var ports=new ArrayList<GLCompositePortMapping>();ports.add(input("spectrum",SignalCategory.SPECTRUM,context,"spectrum"));
        for(int branch=0;branch<12;branch++){
            var output=add(graph,new BinOutputNode());var mask=add(graph,new ComparatorNode());final int index=branch;
            graph.edit("Branch mask",e->{e.setProperty(mask,"operation",GLNodeValue.of("equal"));e.setProperty(mask,"b",GLNodeValue.of(index));e.addConnection(SpectralGroupsTest.wire(modulo,"audio-out",mask,"a"));});
            for(String component:List.of("real","imaginary")){
                var multiply=add(graph,new MathNode());graph.edit("Masked component",e->{e.setProperty(multiply,"operation",GLNodeValue.of("multiply"));e.setProperty(multiply,"clamp",GLNodeValue.of(false));e.addConnection(SpectralGroupsTest.wire(context,component,multiply,"a"));e.addConnection(SpectralGroupsTest.wire(mask,"value-out",multiply,"b"));e.addConnection(SpectralGroupsTest.wire(multiply,"audio-out",output,component.equals("real")?"a":"b"));});
            }
            ports.add(output("branch"+branch,SignalCategory.SPECTRUM,output,"spectrum"));
        }
        var descriptor=new GLCompositeNodeDescriptor(asset.compositeDescriptor().nodeTypeId(),asset.id(),1,ports);
        asset=new GLNodeGraphAsset(asset.id(),1,asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),descriptor);
        var assets=new HashMap<String,GLNodeGraphAsset>();assets.put(asset.id(),asset);
        if(instances){
            var internal=new GLNodeGraph();var nested=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());internal.edit("Inner group",e->e.addNode(nested));
            ports=new ArrayList<>();ports.add(MultipleGroups.countPort("Count"));ports.add(input("spectrum",SignalCategory.SPECTRUM,nested.id(),"spectrum"));
            for(int i=0;i<12;i++)ports.add(output("branch"+i,SignalCategory.SPECTRUM,nested.id(),"branch"+i));
            descriptor=new GLCompositeNodeDescriptor("test.wide.instances","instances",1,ports);
            assets.put("instances",new GLNodeGraphAsset("instances",1,GLNodeGraphAssetKind.COMPOSITE,internal.snapshot(),GLNodeEditorViewState.empty(),Map.of(MultipleGroups.MARKER,GLNodeValue.of(true),MultipleGroups.MAXIMUM,GLNodeValue.of("2"),MultipleGroups.MERGE,GLNodeValue.of("mean"),MultipleGroups.EXECUTION,GLNodeValue.of(2.)),descriptor));
        }
        var root=new GLNodeGraph();var fft=add(root,new FFTStreamNode());var instance=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),instances?Map.of(GroupUnits.key("Count"),GLNodeValue.of(2.)):Map.of());
        root.edit("Boundary",e->{e.addNode(instance);e.addConnection(SpectralGroupsTest.wire(fft,"spectrum",instance.id(),"spectrum"));});
        return new MultiSpectrumOutputTest.Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),assets,List.of()),instance);
    }
    @Test void twelveOutputsCompilePersistAndExport()throws Exception{
        for(boolean instances:List.of(false,true)){
            var f=fixture(instances);var node=MultiSpectrumOutputTest.compiled(f);var layout=NodeRegistry.require(node.typeId()).layout(new NodeState(node.properties()));
            assertEquals(12,layout.ports().stream().filter(p->p.direction()==GLPortDirection.OUTPUT).count());
            var exported=GroupSourceExporter.create(f.document(),f.instance());assertEquals(12,exported.definition().layout(new NodeState(exported.definition().defaults())).ports().stream().filter(p->p.direction()==GLPortDirection.OUTPUT).count());
            var codec=new KnackDocumentCodec();var saved=new java.io.ByteArrayOutputStream();codec.write(f.document(),saved);assertEquals(NodeDocuments.comparable(f.document()),NodeDocuments.comparable(codec.read(new java.io.ByteArrayInputStream(saved.toByteArray()),codec.currentSchemaVersion())));
            var encoded=com.vin.easyknack.audio.AudioGraphEncoder.encode(new GLNodeGraphSnapshot(f.document().root().graph().graphId(),0,List.of(node),List.of()));assertTrue(encoded.contains(instances?"multiplegraph":"spectralgraph"));
        }
        var numeric=numericFixture();var compiled=MultiSpectrumOutputTest.compiled(numeric);assertEquals(12,new NodeState(compiled.properties()).number("outputs",0));assertNotNull(GroupSourceExporter.create(numeric.document(),numeric.instance()));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void twelveSpectrumStreamsReconstructInLiveAndBakedGroups()throws Exception{
        var directory=Files.createTempDirectory(Path.of("target"),"wide-groups-").toAbsolutePath();var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());var includes=new StringBuilder();var checks=new StringBuilder();
        for(boolean instances:List.of(false,true)){
            var f=fixture(instances);var node=MultiSpectrumOutputTest.compiled(f);var def=NodeRegistry.require(node.typeId());
            var exported=GroupSourceExporter.create(f.document(),f.instance());definitions.add(exported.definition());Files.writeString(directory.resolve(exported.className()+".hpp"),exported.files().get("native/"+exported.className()+".hpp"));includes.append("#include \"").append(exported.className()).append(".hpp\"\n");
            checks.append("check<").append(instances?"MultipleGraphNode":"SpectralGraphNode").append(">(").append(quote("!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(node.properties()),p->-1))).append(',').append(instances?1:0).append(");\n");
            checks.append("check<").append(exported.className()).append(">(").append(quote("!"+NativeContractGenerator.fingerprint(exported.definition())+" "+exported.definition().encode(new NodeState(exported.definition().defaults()),p->-1))).append(',').append(instances?1:0).append(");\n");
        }
        var numeric=numericFixture();var numericNode=MultiSpectrumOutputTest.compiled(numeric);var numericDef=NodeRegistry.require(numericNode.typeId());
        checks.append("checkNumeric<MultipleGraphNode>(").append(quote("!"+NativeContractGenerator.fingerprint(numericDef)+" "+numericDef.encode(new NodeState(numericNode.properties()),p->-1))).append(");\n");
        var numericExport=GroupSourceExporter.create(numeric.document(),numeric.instance());definitions.add(numericExport.definition());Files.writeString(directory.resolve(numericExport.className()+".hpp"),numericExport.files().get("native/"+numericExport.className()+".hpp"));includes.append("#include \"").append(numericExport.className()).append(".hpp\"\n");
        checks.append("checkNumeric<").append(numericExport.className()).append(">(").append(quote("!"+NativeContractGenerator.fingerprint(numericExport.definition())+" "+numericExport.definition().encode(new NodeState(numericExport.definition().defaults()),p->-1))).append(");\n");
        Files.writeString(directory.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String code="""
            #include "AudioGraph.hpp"
            INCLUDES
            #include <cassert>
            using namespace easyknack;
            template<class T> void check(const char* payload,int resourcePort){
                auto node=std::make_unique<T>();std::istringstream parameters(payload);node->configure(parameters);node->prepareOutputStorage();node->prepare(48000,64);
                SpectrumStream input;input.prepare(64);input.size=input.windowSize=2048;input.sampleRate=48000;input.hop=input.blockFrames=64;input.identity=1;input.count=1;
                for(std::size_t k=0;k<=1024;k++)input.frames[0].bins[k]={double(k+1),k&&k!=1024?.25:0};SpectrumStream::realSymmetry(input.frames[0],2048);
                float count=2,out[64]{},silence[64]{};const float* inputs[17]{};inputs[0]=&count;char rates[17]{};std::fill_n(rates,17,'c');const DspResource* resources[17]{};resources[resourcePort]=&input;
                ProcessContext context{inputs,17,out,64,48000,silence,64};context.sourceRates=rates;context.resourceInputs=resources;
                assert(node->outputCount()==12);
                for(int block=0;block<2;block++){
                    input.blockStart=64*block;input.frames[0].start=std::int64_t(input.blockStart)+64-2048;node->process(context);
                    for(std::size_t k=0;k<2048;k++){
                        std::complex<double> sum{};auto folded=std::min(k,std::size_t(2048)-k);
                        for(std::size_t p=0;p<12;p++){auto* stream=static_cast<SpectrumStream*>(node->resourceOutput(p));assert(stream&&stream->count==1&&stream->frames[0].start==input.frames[0].start);auto value=stream->frames[0].bins[k];sum+=value;if(folded%12!=p)assert(value==std::complex<double>{});}
                        assert(std::abs(sum-input.frames[0].bins[k])<1e-7);
                    }
                }
            }
            template<class T> void checkNumeric(const char* payload){
                auto node=std::make_unique<T>();std::istringstream setup(payload);node->configure(setup);node->prepareOutputStorage();node->prepare(48000,64);assert(node->outputCount()==12);
                float count=2,out[12][64]{},silence[64]{};const float* inputs[9]{};inputs[0]=&count;float* outputs[12]{};for(unsigned i=0;i<12;i++)outputs[i]=out[i];char rates[9]{};std::fill_n(rates,9,'c');
                ProcessContext context{inputs,9,out[0],64,48000,silence,64};context.outputs=outputs;context.outputCount=12;context.sourceRates=rates;node->process(context);
                for(unsigned p=0;p<12;p++)for(float value:out[p])assert(std::abs(value-2.f*(p+1))<1e-6);
            }
            int main(){CHECKS}
            """.replace("INCLUDES",includes).replace("CHECKS",checks);
        var source=directory.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,directory.resolve("test.exe"),directory);
    }
}
