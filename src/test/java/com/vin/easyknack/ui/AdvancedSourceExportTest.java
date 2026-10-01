package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioGraphEncoder;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class AdvancedSourceExportTest {
    @Test void bakedAutoRateFollowsInternalDependenciesInsteadOfAllInputs() {
        var math=new MathNode();
        var plan=new BakedRatePlan(1,List.of(new BakedRatePlan.Step(math,math.defaults(),List.of(0,-1))),List.of(1));
        for(var rate:List.of(ProcessingRate.CONTROL,ProcessingRate.AUDIO))
            assertEquals(rate,plan.resolve(Map.of(new PropertyId("input_0"),rate)));
        var oscillator=new OscillatorNode();
        var generator=new BakedRatePlan(1,List.of(new BakedRatePlan.Step(oscillator,oscillator.defaults(),List.of(0,-1))),List.of(1));
        assertEquals(ProcessingRate.AUDIO,generator.resolve(Map.of()));
        var converter=new RateConverterNode();
        var reducer=new BakedRatePlan(1,List.of(new BakedRatePlan.Step(converter,converter.defaults(),List.of(0))),List.of(1));
        assertEquals(ProcessingRate.CONTROL,reducer.resolve(Map.of(new PropertyId("input_0"),ProcessingRate.AUDIO)));
    }
    record Fixture(GLNodeGraphDocument document,GLGraphNode group,String tail,boolean midi) { }
    static GLNodeId add(GLNodeGraph graph,BasicNode definition,Map<String,GLNodeValue> changes){
        var id=GLNodeId.random();var values=new HashMap<>(definition.defaults());values.putAll(changes);
        graph.edit("Node",e->e.addNode(new GLGraphNode(id,definition.registration().id(),values)));return id;
    }
    static GLNodeId add(GLNodeGraph graph,BasicNode definition){return add(graph,definition,Map.of());}
    static void connect(GLNodeGraph graph,GLNodeId from,String out,GLNodeId to,String in){graph.edit("Wire",e->e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(from,out),new GLNodeEndpoint(to,in))));}
    static GLCompositePortMapping input(String name,SignalCategory signal,GLNodeId node,String port){return new GLCompositePortMapping(GLNodePortDescriptor.input(name,signal.valueTypeId(),false,1),new GLNodeEndpoint(node,port));}
    static GLCompositePortMapping output(String name,SignalCategory signal,GLNodeId node,String port){return new GLCompositePortMapping(GLNodePortDescriptor.output(name,signal.valueTypeId()),new GLNodeEndpoint(node,port));}
    static Fixture fixture(int kind){
        var inner=new GLNodeGraph();var ports=new ArrayList<GLCompositePortMapping>();
        if(kind==0){
            var split=add(inner,new ChannelSeparateNode());var combine=add(inner,new ChannelCombineNode());
            connect(inner,split,"right",combine,"left");connect(inner,split,"left",combine,"right");
            ports.add(input("In",SignalCategory.AUDIO,split,"audio-in"));ports.add(output("Out",SignalCategory.AUDIO,combine,"audio-out"));
        }else if(kind==1){
            var transpose=add(inner,new MidiTransposeNode(),Map.of("transpose",GLNodeValue.of(7.5)));
            var delay=add(inner,new MidiDelayNode(),Map.of("delay",GLNodeValue.of(3.0)));
            var note=add(inner,new MidiNoteNode(),Map.of("voices",GLNodeValue.of(3)));
            var osc=add(inner,new OscillatorNode());var gate=add(inner,new MathNode());
            var pan=add(inner,new PanNode(),Map.of("pan",GLNodeValue.of(-.4)));
            var merge=add(inner,new VoiceMergeNode());var split=add(inner,new ChannelSeparateNode());var combine=add(inner,new ChannelCombineNode());
            connect(inner,transpose,"midi-out",delay,"midi-in");connect(inner,delay,"midi-out",note,"midi-in");
            connect(inner,note,"frequency",osc,"frequency");connect(inner,osc,"audio-out",gate,"a");connect(inner,note,"gate",gate,"b");
            connect(inner,gate,"audio-out",pan,"audio-in");connect(inner,pan,"audio-out",merge,"audio-in");connect(inner,merge,"audio-out",split,"audio-in");
            connect(inner,split,"right",combine,"left");connect(inner,split,"left",combine,"right");
            ports.add(input("In",SignalCategory.MIDI,transpose,"midi-in"));ports.add(output("Out",SignalCategory.AUDIO,combine,"audio-out"));
        }else if(kind==2){
            var transpose=add(inner,new MidiTransposeNode(),Map.of("transpose",GLNodeValue.of(7.25)));
            var delay=add(inner,new MidiDelayNode(),Map.of("delay",GLNodeValue.of(3.0)));
            connect(inner,transpose,"midi-out",delay,"midi-in");ports.add(input("In",SignalCategory.MIDI,transpose,"midi-in"));
            ports.add(output("First",SignalCategory.MIDI,transpose,"midi-out"));ports.add(output("Delayed",SignalCategory.MIDI,delay,"midi-out"));
        }else if(kind==3){
            var note=add(inner,new MidiNoteNode(),Map.of("voices",GLNodeValue.of(3)));
            ports.add(input("In",SignalCategory.MIDI,note,"midi-in"));ports.add(output("Frequency",SignalCategory.VALUE,note,"frequency"));ports.add(output("Gate",SignalCategory.VALUE,note,"gate"));
        }else if(kind==4){
            var transpose=add(inner,new MidiTransposeNode(),Map.of("transpose",GLNodeValue.of(7.25)));
            var note=add(inner,new MidiNoteNode());connect(inner,transpose,"midi-out",note,"midi-in");
            ports.add(input("In",SignalCategory.MIDI,transpose,"midi-in"));ports.add(output("Gate",SignalCategory.VALUE,note,"gate"));ports.add(output("MIDI",SignalCategory.MIDI,transpose,"midi-out"));
        }else if(kind==5){
            var osc=add(inner,new OscillatorNode());var gate=add(inner,new MathNode());connect(inner,osc,"audio-out",gate,"a");
            ports.add(input("In",SignalCategory.VALUE,osc,"frequency"));ports.add(input("Gate",SignalCategory.VALUE,gate,"b"));ports.add(output("Out",SignalCategory.AUDIO,gate,"audio-out"));
        }else{
            var device=add(inner,new MidiInputNode());var note=add(inner,new MidiNoteNode());connect(inner,device,"midi-out",note,"midi-in");
            ports.add(output("Out",SignalCategory.VALUE,note,"gate"));
        }
        var descriptor=new GLCompositeNodeDescriptor("test.export"+kind,"test.export.asset"+kind,1,ports);
        var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var root=new GLNodeGraph();var source=add(root,kind==0?new ContextInputNode():new MidiInputNode());
        var group=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(group));
        if(kind!=5&&kind!=6)connect(root,source,kind==0?"channel":"midi-out",group.id(),"In");
        String tail;
        if(kind<2){var sink=add(root,new OutputNode());connect(root,group.id(),"Out",sink,"audio-in");tail=" out out 1 0 @ a a";}
        else if(kind==2){
            var first=add(root,new MidiNoteNode());var delayed=add(root,new MidiNoteNode());var math=add(root,new MathNode(),Map.of("operation",GLNodeValue.of("add")));var sink=add(root,new OutputNode());
            connect(root,group.id(),"First",first,"midi-in");connect(root,group.id(),"Delayed",delayed,"midi-in");
            connect(root,first,"frequency",math,"a");connect(root,delayed,"frequency",math,"b");connect(root,math,"audio-out",sink,"audio-in");
            tail=" first midinote 1 0 2 @ e e delayed midinote 257 0 2 @ e e math math 2 0 3 0 add 0 @ e ee out out 4 0 @ a a";
        }else if(kind==3){
            var osc=add(root,new OscillatorNode());var gate=add(root,new MathNode());var merge=add(root,new VoiceMergeNode());var sink=add(root,new OutputNode());
            connect(root,group.id(),"Frequency",osc,"frequency");connect(root,osc,"audio-out",gate,"a");connect(root,group.id(),"Gate",gate,"b");connect(root,gate,"audio-out",merge,"audio-in");connect(root,merge,"audio-out",sink,"audio-in");
            tail=" osc osc 1 440 wave sine -1 @ a ca gate math 2 0 257 1 multiply 0 @ a aa merge voicemerge 3 combine sum @ a a out out 4 0 @ a a";
            // Oscillator source order is frequency, waveform; both input metadata match its declaration.
            tail=tail.replace("@ a ca","@ a ac");
        }else if(kind==4){
            var note=add(root,new MidiNoteNode());var gate=add(root,new MathNode());var sink=add(root,new OutputNode());
            connect(root,group.id(),"MIDI",note,"midi-in");connect(root,group.id(),"Gate",gate,"b");connect(root,note,"frequency",gate,"a");connect(root,gate,"audio-out",sink,"audio-in");
            tail=" note midinote 257 0 2 @ e e gate math 2 0 1 1 multiply 0 @ e ee out out 3 0 @ a a";
        }else if(kind==5){
            var note=add(root,new MidiNoteNode(),Map.of("voices",GLNodeValue.of(3)));var merge=add(root,new VoiceMergeNode());var sink=add(root,new OutputNode());
            connect(root,source,"midi-out",note,"midi-in");connect(root,note,"frequency",group.id(),"In");connect(root,note,"gate",group.id(),"Gate");connect(root,group.id(),"Out",merge,"audio-in");connect(root,merge,"audio-out",sink,"audio-in");
            tail=" merge voicemerge 2 combine sum @ a a out out 3 0 @ a a";
        }else{
            var sink=add(root,new OutputNode());connect(root,group.id(),"Out",sink,"audio-in");tail=" out out 1 0 @ a a";
        }
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),group,tail,kind!=0);
    }
    static String reference(Fixture fixture){
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(fixture.document);
        var prepared=GroupCompiler.prepare(fixture.document.root(),registry);
        var flat=GLCompositeGraphFlattener.flatten(prepared.root(),prepared.registry(),new GLCompositeExpansionLimits(16,256,512,10000));
        assertTrue(flat.isValid(),flat.diagnostics().toString());return AudioGraphEncoder.encode(flat.graph().orElseThrow());
    }
    static String quote(String value){return "\""+value.replace("\\","\\\\").replace("\"","\\\"")+"\"";}

    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void exportedChannelsMidiDelayInternalAndExternalVoicesMatchTheirOriginalGraphs()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"advanced-export-").toAbsolutePath();
        var definitions=new ArrayList<BasicNode>(NodeRegistry.definitionsList());var fixtures=new ArrayList<Fixture>();var exports=new ArrayList<GroupSourceExporter.Export>();
        var cpp=new StringBuilder("#include \"AudioGraph.hpp\"\n#include <cassert>\n#include <iostream>\n");
        for(int kind=0;kind<7;kind++){
            var f=fixture(kind);fixtures.add(f);var export=GroupSourceExporter.create(f.document,f.group);exports.add(export);definitions.add(export.definition());
            Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));cpp.append("#include ").append(quote(export.className()+".hpp")).append('\n');
            var java=dir.resolve(export.className()+".java");Files.writeString(java,export.files().get("java/com/vin/easyknack/nodes/"+export.className()+".java"));
            var diagnostics=new java.io.ByteArrayOutputStream();
            assertEquals(0,javax.tools.ToolProvider.getSystemJavaCompiler().run(null,diagnostics,diagnostics,"-proc:none","-classpath",System.getProperty("java.class.path"),"-d",dir.toString(),java.toString()),diagnostics.toString());
            try(var loader=new java.net.URLClassLoader(new java.net.URL[]{dir.toUri().toURL()},getClass().getClassLoader())){
                var loaded=(BasicNode)loader.loadClass("com.vin.easyknack.nodes."+export.className()).getConstructor().newInstance();
                assertEquals(NativeContractGenerator.fingerprint(export.definition()),NativeContractGenerator.fingerprint(loaded));
                assertEquals(export.definition().resolveAutomaticRate(new NodeState(Map.of()),Map.of()),loaded.resolveAutomaticRate(new NodeState(Map.of()),Map.of()));
            }
        }
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        cpp.append("""
                using namespace easyknack;
                struct Source:MidiSource{MidiBlock block;Source(){block.source=1;block.generation=1;}void beginBlock(std::size_t,double)noexcept override{}const MidiBlock* events(std::uint64_t)const noexcept override{return &block;}};
                int main(){
                auto factory=[](const std::string& id)->std::unique_ptr<NodeProcessor>{
                """);
        for(var export:exports)cpp.append("if(id==").append(quote(export.definition().processor().id())).append(")return std::make_unique<").append(export.className()).append(">();\n");
        cpp.append("return createNodeProcessor(id);};\n");
        for(int kind=0;kind<7;kind++){
            var export=exports.get(kind);var f=fixtures.get(kind);var d=export.definition();
            String rate=d.layout(new NodeState(d.defaults())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).map(r->String.valueOf(r.rate().code())).findFirst().orElseThrow();
            final int scenario=kind;
            String prefix=kind==5?" source midiin 1 @ e - note midinote 0 0 2 poly 3 @ e e":f.midi?" source midiin 1 @ e -":" source context @ c -";
            String patch="GRAPH "+(kind<2||kind==6?3:kind==4||kind==5?5:6)+prefix+" baked "+d.processor().id()+" !"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->scenario==5?(p.value().equals("input_0")?1:257):0)+" @ "+rate+(kind==5?" aa":kind==6?" -":f.midi?" e":" a")+f.tail;
            cpp.append("{const std::string patch=").append(quote(patch)).append(";const std::string reference=").append(quote(reference(f))).append(";auto baked=AudioGraph::compile(patch,factory);auto original=AudioGraph::parse(reference);Source source;std::array<float,256> a{},b{};\n");
            if(kind==3)cpp.append("assert(baked.nodes[1].voiceCount()==3);assert(static_cast<BakedGraphProcessor&>(*baked.nodes[1].processor).runtime==static_cast<BakedGraphProcessor&>(*baked.nodes[1].voice(2).processor).runtime);\n");
            if(kind==5)cpp.append("assert(baked.nodes[2].voiceCount()==3);assert(static_cast<BakedGraphProcessor&>(*baked.nodes[2].processor).runtime!=static_cast<BakedGraphProcessor&>(*baked.nodes[2].voice(2).processor).runtime);\n");
            if(kind==6)cpp.append("assert(baked.midiDevices()==std::vector<std::uint64_t>{1});\n");
            cpp.append("""
                    for(int block=0;block<48;block++){
                        int frames=block%%3==0?17:256;source.block.clear();
                        if(block==0){source.block.append(2,144|(60<<8)|(100<<16));source.block.append(8,144|(67<<8)|(110<<16));}
                        if(block==4)source.block.append(7,128|(60<<8));
                        if(block==7)source.block.append(5,144|(72<<8)|(127<<16));
                        if(block==12){source.block.panic=true;source.block.generation++;}
                        if(block==20)source.block.append(3,144|(69<<8)|(100<<16));
                        if(block==25){auto next=AudioGraph::compile(patch,factory);next.inheritState(baked);baked=std::move(next);auto ref=AudioGraph::parse(reference);ref.inheritState(original);original=std::move(ref);}
                        for(int channel=0;channel<2;channel++){
                            baked.process(a.data(),frames,channel,nullptr,&source);original.process(b.data(),frames,channel,nullptr,&source);
                            for(int i=0;i<frames;i++)if(std::abs(a[i]-b[i])>1e-5){std::cerr<<"case %d block "<<block<<" channel "<<channel<<" sample "<<i<<": "<<a[i]<<" != "<<b[i]<<'\\n';return 2;}
                        }
                    }}
                    """.formatted(kind));
        }
        cpp.append("std::cout<<\"Advanced export equivalence passed\\n\";}\n");
        var file=dir.resolve("test.cpp");Files.writeString(file,cpp);RateConverterTest.compileAndRun(file,dir.resolve("test.exe"),dir);
    }
}
