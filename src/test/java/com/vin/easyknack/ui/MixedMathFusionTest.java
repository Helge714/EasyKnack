package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class MixedMathFusionTest {
    private static GLNodeId add(GLNodeGraph graph,BasicNode definition,Map<String,GLNodeValue> values){
        var id=GLNodeId.random();var properties=new HashMap<>(definition.defaults());properties.putAll(values);
        graph.edit("Add",e->e.addNode(new GLGraphNode(id,definition.registration().id(),properties)));return id;
    }
    private static void wire(GLNodeGraph graph,GLNodeId from,String output,GLNodeId to,String input){
        graph.edit("Wire",e->e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(from,output),new GLNodeEndpoint(to,input))));
    }
    private static GLCompositePortMapping out(String name,GLNodeId id,String port){return new GLCompositePortMapping(GLNodePortDescriptor.output(name,SignalCategory.AUDIO.valueTypeId()),new GLNodeEndpoint(id,port));}
    static GroupSourceExporter.Export fixture(){
        // Nested boundary falls inside the first Math chain. Its tap escapes to another branch.
        var inner=new GLNodeGraph();var m1=add(inner,new MathNode(),Map.of("b",GLNodeValue.of(.8)));
        var descriptor=new GLCompositeNodeDescriptor("test.inner","test.inner.asset",1,List.of(
                new GLCompositePortMapping(GLNodePortDescriptor.input("In",SignalCategory.AUDIO.valueTypeId(),false,1),new GLNodeEndpoint(m1,"a")),out("Out",m1,"audio-out")));
        var innerAsset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var graph=new GLNodeGraph();var osc=add(graph,new OscillatorNode(),Map.of("frequency",GLNodeValue.of(220)));
        var group=GLNodeId.random();graph.edit("Subgroup",e->e.addNode(new GLGraphNode(group,descriptor.nodeTypeId(),Map.of())));
        var m2=add(graph,new MathNode(),Map.of("operation",GLNodeValue.of("add"),"b",GLNodeValue.of(.1)));
        var filter=add(graph,new FilterNode(),Map.of());var otherOsc=add(graph,new OscillatorNode(),Map.of("frequency",GLNodeValue.of(330)));
        var combine=add(graph,new ChannelCombineNode(),Map.of());var separate=add(graph,new ChannelSeparateNode(),Map.of());
        var m3=add(graph,new MathNode(),Map.of("b",GLNodeValue.of(.6)));var m4=add(graph,new MathNode(),Map.of("operation",GLNodeValue.of("negate")));
        var gain=add(graph,new GainNode(),Map.of());
        wire(graph,osc,"audio-out",group,"In");wire(graph,group,"Out",m2,"a");wire(graph,m2,"audio-out",filter,"audio-in");
        wire(graph,filter,"audio-out",combine,"left");wire(graph,otherOsc,"audio-out",combine,"right");wire(graph,combine,"audio-out",separate,"audio-in");
        wire(graph,separate,"right",m3,"a");wire(graph,m3,"audio-out",m4,"a");wire(graph,group,"Out",gain,"audio-in");
        var outer=new GLCompositeNodeDescriptor("test.outer","test.outer.asset",1,List.of(out("Out",m4,"audio-out"),out("Tap",gain,"audio-out"),out("Filtered",separate,"left")));
        var outerAsset=new GLNodeGraphAsset(outer.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of("easyknack.title",GLNodeValue.of("Nested filter")),outer);
        var root=new GLNodeGraph();var selected=new GLGraphNode(GLNodeId.random(),outer.nodeTypeId(),Map.of());root.edit("Root",e->e.addNode(selected));
        var doc=new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(innerAsset.id(),innerAsset,outerAsset.id(),outerAsset),List.of());
        return GroupSourceExporter.create(doc,selected);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nestedMixedGroupsFuseRegionsAndPreserveFanoutAndSecondSourceOutputs()throws Exception{
        var export=fixture();var name=export.className();var d=export.definition();String source=export.files().get("native/"+name+".hpp");
        assertTrue(source.contains("bool renderSampleRegion0("));assertTrue(source.contains("bool renderSampleRegion1("));
        var dir=Files.createTempDirectory(Path.of("target"),"mixed-fusion-").toAbsolutePath();
        Files.writeString(dir.resolve(name+".hpp"),source);
        Files.writeString(dir.resolve("Reference.hpp"),source.replace("struct "+name+":","struct Reference:").replace("if(renderFusedMath","if(false && renderFusedMath").replace("if(renderSampleRegion","if(false && renderSampleRegion"));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(d);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->-1);
        var code="""
            #include "AudioGraph.hpp"
            #include "%s.hpp"
            #include "Reference.hpp"
            #include <cassert>
            #include <iostream>
            #include <chrono>
            using namespace easyknack;
            int main(){
                %s fused;Reference plain;std::istringstream f("%s"),p("%s");fused.configure(f);plain.configure(p);
                fused.prepare(48000,256);plain.prepare(48000,256);
                std::array<std::array<float,256>,3> a{},b{};std::array<float,256> silence{};
                float* ap[]={a[0].data(),a[1].data(),a[2].data()};float* bp[]={b[0].data(),b[1].data(),b[2].data()};
                ProcessContext ca{nullptr,0,ap[0],256,48000,silence.data(),256,nullptr,0,ap,3};auto cb=ca;cb.output=bp[0];cb.outputs=bp;
                bool signal=false,distinct=false;
                for(int block=0;block<100;block++){
                    int frames=block%%4==0?1:block%%3==0?17:256;
                    ca.bufferSize=cb.bufferSize=ca.blockFrames=cb.blockFrames=frames;
                    for(int channel=0;channel<2;channel++){
                        ca.channelIndex=cb.channelIndex=channel;fused.process(ca);plain.process(cb);
                        for(int port=0;port<3;port++)for(int i=0;i<frames;i++){
                            if(std::abs(a[port][i]-b[port][i])>1e-6f){std::cerr<<"Mismatch block "<<block<<" channel "<<channel<<" port "<<port<<" sample "<<i<<"\\n";return 1;}
                            signal=signal||std::abs(a[port][i])>.01f;distinct=distinct||std::abs(a[0][i]-a[2][i])>.01f;
                        }
                    }
                    if(block==60){%s next;Reference ref;std::istringstream nf("%s"),np("%s");next.configure(nf);ref.configure(np);next.prepare(48000,256);ref.prepare(48000,256);next.inheritState(fused);ref.inheritState(plain);fused.runtime=next.runtime;plain.runtime=ref.runtime;}
                }
                assert(signal&&distinct);
                // Prove both regions qualify in this mixed, mono/stereo/channel-splitting graph.
                assert(fused.renderSampleRegion0(256,nullptr,nullptr));assert(fused.renderSampleRegion1(256,nullptr,nullptr));
                std::cout<<"Nested mixed Math regions match their scheduled reference\\n";
                ca.bufferSize=cb.bufferSize=ca.blockFrames=cb.blockFrames=256;
                auto measure=[&](auto& processor,auto& c){auto start=std::chrono::steady_clock::now();for(int block=0;block<2000;block++)for(int ch=0;ch<2;ch++){c.channelIndex=ch;processor.process(c);}return std::chrono::duration<double,std::milli>(std::chrono::steady_clock::now()-start).count();};
                auto scheduled=measure(plain,cb),compiled=measure(fused,ca);std::cout<<"Sample fusion: scheduled="<<scheduled<<" ms, compiled="<<compiled<<" ms\\n";
            }
            """.formatted(name,name,payload,payload,name,payload,payload);
        var file=dir.resolve("test.cpp");Files.writeString(file,code);RateConverterTest.compileAndRun(file,dir.resolve("test.exe"),dir);
    }
}
