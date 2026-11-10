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
class PureFusionTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void mixedPureOperationsConstantsAndCommonExpressionsMatchScheduler()throws Exception{
        var graph=new GLNodeGraph();var ids=new ArrayList<GLNodeId>();
        BasicNode[] definitions={new MathNode(),new MathNode(),new MathNode(),new MixNode(),new MapRangeNode(),new UnitConverterNode(),new MathNode()};
        for(int i=0;i<definitions.length;i++){
            var d=definitions[i];var p=new HashMap<>(d.defaults());p.put("rate",GLNodeValue.of("audio"));
            if(i==0){p.put("a",GLNodeValue.of(2));p.put("b",GLNodeValue.of(3));}
            if(i==1||i==2)p.put("b",GLNodeValue.of(.3));
            if(i==3)p.put("factor",GLNodeValue.of(.25));
            if(i==4){p.put("from-min",GLNodeValue.of(-1));p.put("to-min",GLNodeValue.of(-2));p.put("to-max",GLNodeValue.of(2));}
            if(i==6)p.put("operation",GLNodeValue.of("add"));
            var id=GLNodeId.random();ids.add(id);graph.edit("Node",e->e.addNode(new GLGraphNode(id,d.registration().id(),p)));
        }
        String[][] wires={{"1","audio-out","3","a"},{"2","audio-out","3","b"},{"3","audio-out","4","audio-in"},{"0","audio-out","4","from-max"},{"4","audio-out","5","value"},{"5","out","6","a"},{"0","audio-out","6","b"}};
        for(var w:wires)graph.edit("Wire",e->e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(ids.get(Integer.parseInt(w[0])),w[1]),new GLNodeEndpoint(ids.get(Integer.parseInt(w[2])),w[3]))));
        var ports=List.of(new GLCompositePortMapping(GLNodePortDescriptor.input("In",SignalCategory.AUDIO.valueTypeId(),false,1),new GLNodeEndpoint(ids.get(1),"a")).withEndpoints(List.of(new GLNodeEndpoint(ids.get(1),"a"),new GLNodeEndpoint(ids.get(2),"a"))),
            new GLCompositePortMapping(GLNodePortDescriptor.output("Out",SignalCategory.VALUE.valueTypeId()),new GLNodeEndpoint(ids.get(6),"audio-out")));
        var descriptor=new GLCompositeNodeDescriptor("test.pure","test.pure.asset",1,ports);
        var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var root=new GLNodeGraph();var group=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(group));
        var doc=new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
        var export=GroupSourceExporter.create(doc,group);String name=export.className(),source=export.files().get("native/"+name+".hpp");
        var optimizations=java.util.regex.Pattern.compile("folded=(\\d+), reused=(\\d+)").matcher(source);
        assertTrue(optimizations.find(),source);
        assertTrue(Integer.parseInt(optimizations.group(1))>=1,"Expected constant folding");
        assertTrue(Integer.parseInt(optimizations.group(2))>=1,"Expected common-expression reuse");
        var dir=Files.createTempDirectory(Path.of("target"),"pure-fusion-").toAbsolutePath();
        Files.writeString(dir.resolve(name+".hpp"),source);
        Files.writeString(dir.resolve("Reference.hpp"),source.replace("struct "+name+":","struct Reference:").replace("if(renderFusedMath","if(false && renderFusedMath"));
        var all=new ArrayList<>(NodeRegistry.builtinDefinitions());all.add(export.definition());Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(all));
        var d=export.definition();String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->0);
        String cpp="""
            #include "AudioGraph.hpp"
            #include "%s.hpp"
            #include "Reference.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){%s fused;Reference plain;std::istringstream f("%s"),p("%s");fused.configure(f);plain.configure(p);
                std::array<float,256> left{},right{},a{},b{},silence{};const float* ins[]={left.data()};const float* rights[]={right.data()};char rate='a';
                ProcessContext ca{ins,1,a.data(),256,48000,silence.data(),256,nullptr,0,nullptr,0,nullptr,nullptr,nullptr,rights,nullptr,&rate};auto cb=ca;cb.output=b.data();
                for(int block=0;block<100;block++){
                    int frames=block%%3==0?1:block%%3==1?17:256;rate=block<25?'a':block<50?'c':block<75?'e':'a';
                    for(int i=0;i<256;i++){left[i]=std::sin(i*.02+block);right[i]=-left[i]*.7f;}
                    ca.bufferSize=cb.bufferSize=ca.blockFrames=cb.blockFrames=frames;
                    for(int ch=0;ch<2;ch++){ca.channelIndex=cb.channelIndex=ch;fused.process(ca);plain.process(cb);for(int i=0;i<frames;i++)assert(std::abs(a[i]-b[i])<1e-5);}
                }
                assert(fused.renderFusedMath(256,nullptr,nullptr));
            }
            """.formatted(name,name,payload,payload);
        var file=dir.resolve("test.cpp");Files.writeString(file,cpp);RateConverterTest.compileAndRun(file,dir.resolve("test.exe"),dir);
    }
}
