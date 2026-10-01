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

class FeedbackExportTest {
    static Fixture fixture(int samples,boolean nested){
        var inner=new GLNodeGraph();
        var osc=add(inner,new OscillatorNode());
        var context=add(inner,new ContextInputNode());
        var pan=add(inner,new PanNode());connect(inner,osc,"audio-out",pan,"audio-in");connect(inner,context,"channel",pan,"pan");
        BasicNode delayDefinition=samples==-1?new SampleDelayNode():samples==-256?new BlockDelayNode():new DelayNode();
        var delay=add(inner,delayDefinition,samples>0?Map.of("time",GLNodeValue.of(samples/48000.)):Map.of());
        String delayIn=samples>0?"audio-in":"in",delayOut=samples>0?"audio-out":"out";
        var gain=add(inner,new MathNode(),Map.of("b",GLNodeValue.of(.3)));
        var sum=add(inner,new MathNode(),Map.of("operation",GLNodeValue.of("add")));
        connect(inner,delay,delayOut,gain,"a");connect(inner,gain,"audio-out",sum,"b");
        connect(inner,pan,"audio-out",sum,"a");connect(inner,sum,"audio-out",delay,delayIn);
        var ports=new ArrayList<GLCompositePortMapping>();ports.add(output("Out",SignalCategory.AUDIO,delay,delayOut));
        if(samples==4)ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input("Time",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(delay,"time"),new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,Map.of("default",GLNodeValue.of(4./48000),GroupInputSettings.RATE,GLNodeValue.of("control"),GroupUnits.BASE,GLNodeValue.of(UnitFormat.SECOND.id()),GroupUnits.ALLOWED,GLNodeValue.of(List.of(UnitFormat.SECOND.id()))))));
        var d=new GLCompositeNodeDescriptor("test.feedback","test.feedback.asset",1,ports);
        var asset=new GLNodeGraphAsset(d.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),GLNodeEditorViewState.empty(),Map.of(),d);
        var assets=new HashMap<String,GLNodeGraphAsset>();assets.put(asset.id(),asset);
        if(nested){
            var wrapper=new GLNodeGraph();var child=new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of());wrapper.edit("Child",e->e.addNode(child));
            var wrapperPorts=new ArrayList<GLCompositePortMapping>();wrapperPorts.add(output("Out",SignalCategory.AUDIO,child.id(),"Out"));
            if(samples==4)wrapperPorts.add(new GLCompositePortMapping(ports.get(1).publicPort(),new GLNodeEndpoint(child.id(),"Time"),ports.get(1).presentation()));
            d=new GLCompositeNodeDescriptor("test.wrapper","test.wrapper.asset",1,wrapperPorts);
            asset=new GLNodeGraphAsset(d.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,wrapper.snapshot(),GLNodeEditorViewState.empty(),Map.of(),d);assets.put(asset.id(),asset);
        }
        var root=new GLNodeGraph();var group=new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(group));
        var out=add(root,new OutputNode());connect(root,group.id(),"Out",out,"audio-in");
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),assets,List.of()),group,"",false);
    }
    @Test void nestedFeedbackExportsAndResolvesRates(){
        var f=fixture(1,true);var export=GroupSourceExporter.create(f.document(),f.group());
        assertEquals(ProcessingRate.AUDIO,export.definition().resolveAutomaticRate(new NodeState(Map.of()),Map.of()));
        assertTrue(export.files().get("native/"+export.className()+".hpp").contains("renderFeedbackGraph"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void feedbackMatchesLiveGraphAcrossBlocksChannelsAndStateTransfer()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"feedback-export-").toAbsolutePath();
        var defs=new ArrayList<BasicNode>(NodeRegistry.definitionsList());var exports=new ArrayList<GroupSourceExporter.Export>();var fixtures=new ArrayList<Fixture>();
        var cpp=new StringBuilder("#include \"AudioGraph.hpp\"\n#include <iostream>\n");
        for(int samples:List.of(1,4,256,480,-1,-256)){
            var f=fixture(samples,true);var export=GroupSourceExporter.create(f.document(),f.group());fixtures.add(f);exports.add(export);defs.add(export.definition());
            Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));cpp.append("#include ").append(quote(export.className()+".hpp")).append('\n');
        }
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(defs));
        cpp.append("using namespace easyknack;int main(){auto factory=[](const std::string& id)->std::unique_ptr<NodeProcessor>{");
        for(var export:exports)cpp.append("if(id==").append(quote(export.definition().processor().id())).append(")return std::make_unique<").append(export.className()).append(">();\n");
        cpp.append("return createNodeProcessor(id);};\n");
        for(int j=0;j<exports.size();j++){
            var d=exports.get(j).definition();String patch="GRAPH 2 baked "+d.processor().id()+" !"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->-1)+" @ a "+(d.processor().inputs().isEmpty()?"-":"c")+" out out 0 0 @ a a";
            cpp.append("{std::string patch=").append(quote(patch)).append(",ref=").append(quote(reference(fixtures.get(j)))).append(";auto baked=AudioGraph::compile(patch,factory);auto live=AudioGraph::parse(ref);std::array<float,256>a{},b{};");
            cpp.append("for(int block=0;block<80;block++){int frames=block%3==0?17:256;if(block==30){auto next=AudioGraph::compile(patch,factory);next.inheritState(baked);baked=std::move(next);auto n=AudioGraph::parse(ref);n.inheritState(live);live=std::move(n);}for(int channel=0;channel<2;channel++){baked.process(a.data(),frames,channel,nullptr);live.process(b.data(),frames,channel,nullptr);for(int i=0;i<frames;i++)if(!std::isfinite(a[i])||std::abs(a[i]-b[i])>1e-5){std::cerr<<\"feedback case "+j+" block \"<<block<<\" sample \"<<i<<\" actual \"<<a[i]<<\" expected \"<<b[i];return 2;}}}}\n");
        }
        cpp.append("std::cout<<\"Feedback export equivalence passed\\n\";}");var file=dir.resolve("test.cpp");Files.writeString(file,cpp);RateConverterTest.compileAndRun(file,dir.resolve("test.exe"),dir);
    }
}
