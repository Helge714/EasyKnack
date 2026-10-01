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

class AdaptiveFusionTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void controlPowerAndAudioMultiplyShareLoopAndMatchReference()throws Exception{
        var graph=new GLNodeGraph();var power=add(graph,new MathNode());var multiply=add(graph,new MathNode());
        graph.edit("Fixture",e->{
            e.setProperty(power,"operation",GLNodeValue.of("power"));e.setProperty(power,"b",GLNodeValue.of(2.));
            e.setProperty(power,"clamp",GLNodeValue.of(false));e.setProperty(multiply,"clamp",GLNodeValue.of(false));
            e.addConnection(SpectralGroupsTest.wire(power,"audio-out",multiply,"a"));
        });
        var ports=List.of(
            new GLCompositePortMapping(GLNodePortDescriptor.input("Control",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(power,"a")),
            new GLCompositePortMapping(GLNodePortDescriptor.input("Audio",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(multiply,"b")),
            new GLCompositePortMapping(GLNodePortDescriptor.output("Out",SignalCategory.VALUE.valueTypeId()),new GLNodeEndpoint(multiply,"audio-out")));
        var descriptor=new GLCompositeNodeDescriptor("test.adaptive","adaptive",1,ports);
        var asset=new GLNodeGraphAsset("adaptive",1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var node=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());
        var root=new GLNodeGraphSnapshot(GLNodeGraphId.random(),0,List.of(node),List.of());
        var doc=new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root,GLNodeEditorViewState.empty()),Map.of("adaptive",asset),List.of());
        var export=GroupSourceExporter.create(doc,node);var name=export.className();var definition=export.definition();
        String source=export.files().get("native/"+name+".hpp");assertTrue(source.contains("renderFusedMath"));
        var dir=Files.createTempDirectory(Path.of("target"),"adaptive-fusion-").toAbsolutePath();
        Files.writeString(dir.resolve(name+".hpp"),source);
        Files.writeString(dir.resolve("Reference.hpp"),source.replace("struct "+name+":","struct Reference:").replace("if(renderFusedMath","if(false && renderFusedMath"));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(definition);
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String payload="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(definition.defaults()),p->0);
        String cpp="""
            #include "%s.hpp"
            #include "Reference.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                %s fused;Reference plain;std::istringstream f(%s),p(%s);fused.configure(f);plain.configure(p);
                std::array<float,256> control{},audio{},right{},out{},ref{},silence{};
                const float* inputs[]{control.data(),audio.data()};const float* rights[]{control.data(),right.data()};char rates[]{'c','a'};
                ProcessContext a{inputs,2,out.data(),256,48000,silence.data(),256};a.sourceRates=rates;a.rightInputs=rights;
                auto b=a;b.output=ref.data();
                for(int block=0;block<90;block++){
                    rates[0]=block<30?'c':'a';rates[1]=block<60?'a':'c';
                    for(int i=0;i<256;i++){control[i]=.2f+i*.001f+block*.01f;audio[i]=std::sin(i*.03f);right[i]=-audio[i]*.7f;}
                    for(int channel=0;channel<2;channel++){
                        a.channelIndex=b.channelIndex=channel;fused.process(a);plain.process(b);
                        for(int i=0;i<256;i++)assert(std::abs(out[i]-ref[i])<1e-6f);
                    }
                    assert(fused.renderFusedMath(256,nullptr,nullptr));
                }
            }
            """.formatted(name,name,quote(payload),quote(payload));
        var file=dir.resolve("test.cpp");Files.writeString(file,cpp);RateConverterTest.compileAndRun(file,dir.resolve("test.exe"),dir);
    }
}
