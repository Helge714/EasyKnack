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

class MathFusionTest {
    static GroupSourceExporter.Export fixture(){
        var graph=new GLNodeGraph();var definition=new MathNode();var ids=new ArrayList<GLNodeId>();
        String[] operations={"multiply","add","subtract","divide","minimum","maximum","absolute","negate"};
        for(int i=0;i<operations.length;i++){
            var id=GLNodeId.random();ids.add(id);var properties=new HashMap<>(definition.defaults());
            properties.put("operation",GLNodeValue.of(operations[i]));properties.put("b",GLNodeValue.of(i==3?0:0.3));
            properties.put("clamp",GLNodeValue.of(i==operations.length-1));
            graph.edit("Node",e->e.addNode(new GLGraphNode(id,definition.registration().id(),properties)));
            if(i>0){var previous=ids.get(i-1);graph.edit("Wire",e->e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(previous,"audio-out"),new GLNodeEndpoint(id,"a"))));}
        }
        var ports=List.of(
            new GLCompositePortMapping(GLNodePortDescriptor.input("In",SignalCategory.AUDIO.valueTypeId(),false,1),new GLNodeEndpoint(ids.get(0),"a")),
            new GLCompositePortMapping(GLNodePortDescriptor.output("Out",SignalCategory.VALUE.valueTypeId()),new GLNodeEndpoint(ids.get(ids.size()-1),"audio-out")),
            new GLCompositePortMapping(GLNodePortDescriptor.output("Intermediate",SignalCategory.VALUE.valueTypeId()),new GLNodeEndpoint(ids.get(2),"audio-out")));
        var descriptor=new GLCompositeNodeDescriptor("test.fused","test.fused.asset",1,ports);
        var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var root=new GLNodeGraph();var group=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(group));
        var doc=new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
        return GroupSourceExporter.create(doc,group);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void fusedKernelMatchesSchedulerAcrossRatesChannelsShortBlocksAndStateChanges() throws Exception {
        var export=fixture();var d=export.definition();String name=export.className();
        String source=export.files().get("native/"+name+".hpp");assertTrue(source.contains("renderFusedMath"));
        var dir=Files.createTempDirectory(Path.of("target"),"fusion-test-").toAbsolutePath();
        Files.writeString(dir.resolve(name+".hpp"),source);
        Files.writeString(dir.resolve("Reference.hpp"),source.replace("struct "+name+":","struct Reference:").replace("if(renderFusedMath(","if(false && renderFusedMath("));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(d);
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->0);
        String cpp="""
            #include "AudioGraph.hpp"
            #include "%s.hpp"
            #include "Reference.hpp"
            #include <cassert>
            #include <chrono>
            #include <iostream>
            using namespace easyknack;
            int main(){
                %s fused;Reference plain;
                std::istringstream fa("%s"),pa("%s");fused.configure(fa);plain.configure(pa);
                std::array<float,256> left{},right{},a{},b{},ax{},bx{},silence{};
                const float* inputs[]={left.data()};const float* rights[]={right.data()};
                float* outputsA[]={a.data(),ax.data()};float* outputsB[]={b.data(),bx.data()};
                char rate='a';
                ProcessContext ca{inputs,1,a.data(),256,48000,silence.data(),256,nullptr,0,outputsA,2,nullptr,nullptr,nullptr,rights,nullptr,&rate};
                auto cb=ca;cb.output=b.data();cb.outputs=outputsB;
                for(int block=0;block<80;block++){
                    int frames=block%%5==0?1:block%%3==0?17:256;
                    rate=block<20?'a':block<35?'c':block<50?'e':'a';
                    for(int i=0;i<256;i++){left[i]=std::sin((i+block)*.031)*3;right[i]=-left[i]*.7f;}
                    if(block==4){left[7]=std::numeric_limits<float>::infinity();left[9]=std::numeric_limits<float>::quiet_NaN();}
                    ca.bufferSize=cb.bufferSize=ca.blockFrames=cb.blockFrames=frames;
                    for(int channel=0;channel<2;channel++){
                        ca.channelIndex=cb.channelIndex=channel;fused.process(ca);plain.process(cb);
                        for(int i=0;i<frames;i++){assert(std::abs(a[i]-b[i])<1e-6f);assert(std::abs(ax[i]-bx[i])<1e-6f);}
                    }
                    if(block==61){
                        // Intermediate node 1 has no public consumer and is never written by fusion.
                        assert(fused.runtime->graph.nodes[1].stereo);
                        auto& cache=fused.runtime->graph.nodes[1].channelCache[0].outputs[0];cache.fill(12345);
                        ca.channelIndex=0;fused.process(ca);assert(cache[0]==12345);
                    }
                    if(block==65){%s next;Reference ref;std::istringstream nfa("%s"),npa("%s");next.configure(nfa);ref.configure(npa);next.inheritState(fused);ref.inheritState(plain);fused.runtime=next.runtime;plain.runtime=ref.runtime;}
                }
                // Informational benchmark: no machine-dependent speed assertion.
                rate='a';ca.bufferSize=cb.bufferSize=ca.blockFrames=cb.blockFrames=256;ca.channelIndex=cb.channelIndex=0;
                auto measure=[&](auto& processor,auto& context){auto start=std::chrono::steady_clock::now();for(int i=0;i<20000;i++)processor.process(context);return std::chrono::duration<double,std::milli>(std::chrono::steady_clock::now()-start).count();};
                auto ordinary=measure(plain,cb),optimized=measure(fused,ca);
                std::cout<<"Math fusion: scheduler="<<ordinary<<" ms, fused="<<optimized<<" ms, ratio="<<ordinary/optimized<<"\\n";
            }
            """.formatted(name,name,payload,payload,name,payload,payload);
        var file=dir.resolve("test.cpp");Files.writeString(file,cpp);RateConverterTest.compileAndRun(file,dir.resolve("test.exe"),dir);
    }
}
