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

class SpectralUniformTest {
    record Fixture(GLNodeGraphDocument document,GLGraphNode instance){}
    static Fixture fixture(){return fixture(false);}
    static Fixture fixture(boolean memory){
        var asset=SpectralGroups.create();var graph=new GLNodeGraph(asset.graph());
        var output=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinOutputNode).findFirst().orElseThrow();
        graph.edit("External value",e->{for(var w:graph.snapshot().connections())e.removeConnection(w.id());});
        GLNodeId destination=output.id();
        if(memory){
            var id=add(graph,new BinMemoryNode());destination=id;
            var context=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinContextNode).findFirst().orElseThrow();
            graph.edit("Memory",e->{e.addConnection(SpectralGroupsTest.wire(context.id(),"real",id,"in"));e.addConnection(SpectralGroupsTest.wire(id,"out",output.id(),"a"));});
        }
        var descriptor=asset.compositeDescriptor();var ports=new ArrayList<>(descriptor.portMappings());
        ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input("amount",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(destination,memory?"update":"a")));
        descriptor=new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),1,ports);
        asset=new GLNodeGraphAsset(asset.id(),1,asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),descriptor);
        var root=new GLNodeGraph();var value=add(root,new ValueNode());var instance=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());
        root.edit("Connect",e->{e.addNode(instance);e.addConnection(SpectralGroupsTest.wire(value,"value-out",instance.id(),"amount"));});
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),instance);
    }
    static GLGraphNode compiled(Fixture f){
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(f.document());
        var flat=SpectralGroups.flatten(GroupCompiler.prepare(f.document().root(),registry),new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(flat.isValid());
        return flat.graph().orElseThrow().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof SpectralGraphNode).findFirst().orElseThrow();
    }
    @Test void numericBoundaryCompilesAndExports(){
        var f=fixture();var node=compiled(f);
        String program=new String(java.util.HexFormat.of().parseHex(new NodeState(node.properties()).text("program","")),java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(program.contains("binuniform"));assertNotNull(GroupSourceExporter.create(f.document(),f.instance()));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void delayedFramesUseTheirHistoricalAudioOrControlValueAlsoInExports()throws Exception{
        var f=fixture();var node=compiled(f);var def=NodeRegistry.require(node.typeId());
        String direct="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(node.properties()),p->p.equals(SpectralGraphNode.IN)?0:p.equals(SpectralGraphNode.external(0))?1:-1);
        var export=GroupSourceExporter.create(f.document(),f.instance());var exported=export.definition();
        String exportedPayload="!"+NativeContractGenerator.fingerprint(exported)+" "+exported.encode(new NodeState(exported.defaults()),p->p.value().equals("input_0")?0:1);
        var dir=Files.createTempDirectory(Path.of("target"),"spectral-uniform-").toAbsolutePath();Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var defs=new ArrayList<>(NodeRegistry.builtinDefinitions());defs.add(exported);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(defs));
        String code="""
            #include "AudioGraph.hpp"
            #include "%s.hpp"
            #include <cassert>
            using namespace easyknack;
            template<class T> void check(const char* payload,char rate){
                T node;std::istringstream params(payload);node.configure(params);node.prepare(48000,4);
                SpectrumStream input;input.prepare(4);input.size=input.windowSize=8;input.sampleRate=48000;input.identity=17;input.blockFrames=4;
                float values[4]{},out[4]{},silence[4]{};const float* inputs[17]{};inputs[1]=values;char rates[17]{};std::fill_n(rates,17,'c');rates[1]=rate;
                const DspResource* resources[17]{};resources[0]=&input;ProcessContext c{inputs,17,out,4,48000,silence,4};c.sourceRates=rates;c.resourceInputs=resources;
                auto run=[&](unsigned start){input.blockStart=start;for(int i=0;i<4;i++)values[i]=start+i;node.process(c);return static_cast<SpectrumStream*>(node.resourceOutput(0));};
                auto verify=[](SpectrumStream* result,float expected){assert(result->count==1);for(int k=0;k<=4;k++)assert(result->frames[0].bins[k].real()==expected);};
                input.count=0;run(0);run(4);
                // A late asynchronous window ending at sample 4 must not use block 8's values.
                input.count=1;input.frames[0].start=-4;verify(run(8),rate=='a'?3:0);
                input.frames[0].start=0;verify(run(12),rate=='a'?7:4);
                // Synchronous window: last sample in this block, held across all bins.
                input.frames[0].start=12;verify(run(16),rate=='a'?19:16);
                input.frames[0].start=-100;assert(run(20)->count==0);
                // New FFT identity invalidates the old timeline.
                input.identity=18;input.frames[0].start=0;assert(run(24)->count==0);
                // Missing audio blocks invalidate history too.
                input.frames[0].start=20;assert(run(100)->count==0);
                input.count=0;for(unsigned start=104;start<132000;start+=4)run(start);
                input.count=1;input.frames[0].start=92;assert(run(132000)->count==0);
                input.frames[0].start=131992;verify(run(132004),rate=='a'?131999:131996);
            }
            int main(){for(char rate:{'a','c'}){check<SpectralGraphNode>(%s,rate);check<%s>(%s,rate);}}
            """.formatted(export.className(),quote(direct),export.className(),quote(exportedPayload));
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
