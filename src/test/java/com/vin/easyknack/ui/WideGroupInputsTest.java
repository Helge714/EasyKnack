package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;import java.nio.file.*;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;import com.vingame.glswing.node.composite.*;import com.vingame.glswing.node.document.*;import com.vingame.glswing.node.model.*;
class WideGroupInputsTest {
    static MultiSpectrumOutputTest.Fixture fixture(int mode){
        boolean spectral=mode==2,resources=mode==1;int count=spectral?24:resources?12:20;
        var base=spectral?SpectralGroups.create():MultipleGroups.create();var graph=new GLNodeGraph();var ports=new ArrayList<GLCompositePortMapping>();
        if(!spectral)ports.add(MultipleGroups.countPort("Count"));
        GLNodeId previous=null;
        for(int i=0;i<count;i++){
            if(resources){var relay=add(graph,new SpectrumRelayNode());ports.add(input("in"+i,SignalCategory.SPECTRUM,relay,"in"));ports.add(output("out"+i,SignalCategory.SPECTRUM,relay,"out"));continue;}
            if(spectral&&i==count-1){var hold=add(graph,new BinHoldNode());connect(graph,previous,"audio-out",hold,"in");ports.add(input("capture",SignalCategory.TRIGGER,hold,"trigger"));var bin=add(graph,new BinOutputNode());connect(graph,hold,"out",bin,"a");ports.add(output("out",SignalCategory.SPECTRUM,bin,"spectrum"));break;}
            var sum=add(graph,new MathNode());graph.edit("Sum",e->{e.setProperty(sum,"operation",GLNodeValue.of("add"));e.setProperty(sum,"clamp",GLNodeValue.of(false));});
            if(previous!=null)connect(graph,previous,"audio-out",sum,"a");ports.add(input("value"+i,SignalCategory.VALUE,sum,"b"));previous=sum;
        }
        if(spectral){var context=add(graph,new BinContextNode());ports.add(0,input("spectrum",SignalCategory.SPECTRUM,context,"spectrum"));}
        else if(!resources){var merge=add(graph,new InstanceCombineNode());connect(graph,previous,"audio-out",merge,"in");graph.edit("Sum voices",e->e.setProperty(merge,"mode",GLNodeValue.of("sum")));ports.add(output("out",SignalCategory.AUDIO,merge,"out"));}
        var d=new GLCompositeNodeDescriptor(base.compositeDescriptor().nodeTypeId(),base.id(),1,ports);var meta=new HashMap<>(base.metadata());if(!spectral){meta.put(MultipleGroups.MAXIMUM,GLNodeValue.of(resources?"1":"2"));meta.put(MultipleGroups.MERGE,GLNodeValue.of("mean"));}
        var asset=new GLNodeGraphAsset(base.id(),1,base.kind(),graph.snapshot(),GLNodeEditorViewState.empty(),meta,d);var root=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of(GroupUnits.key("Count"),GLNodeValue.of(resources?1.:2.)));root.edit("Root",e->e.addNode(instance));
        for(var port:ports)if(port.publicPort().direction()==GLPortDirection.INPUT&&!MultipleGroups.countPort(port)){
            String type=port.publicPort().valueTypeId();BasicNode source=type.equals(SignalCategory.SPECTRUM.valueTypeId())?new FFTStreamNode():type.equals(SignalCategory.TRIGGER.valueTypeId())?new TriggerInputNode():new ValueNode();var id=add(root,source);connect(root,id,source instanceof FFTStreamNode?"spectrum":source instanceof TriggerInputNode?"trigger-out":"value-out",instance.id(),port.publicPort().id());
        }
        return new MultiSpectrumOutputTest.Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),instance);
    }
    static void connect(GLNodeGraph g,GLNodeId a,String out,GLNodeId b,String in){g.edit("Connect",e->e.addConnection(SpectralGroupsTest.wire(a,out,b,in)));}
    @Test void variableSourceListsCompilePersistAndExport()throws Exception{
        for(int mode=0;mode<3;mode++){
            var f=fixture(mode);var n=MultiSpectrumOutputTest.compiled(f);var state=new NodeState(n.properties());var def=NodeRegistry.require(n.typeId());int expected=mode==0?21:mode==1?13:25;
            assertEquals(expected,def.processor().inputs(state).size());assertNotNull(GroupSourceExporter.create(f.document(),f.instance()));
            var codec=new KnackDocumentCodec();var saved=new java.io.ByteArrayOutputStream();codec.write(f.document(),saved);assertEquals(NodeDocuments.comparable(f.document()),NodeDocuments.comparable(codec.read(new java.io.ByteArrayInputStream(saved.toByteArray()),codec.currentSchemaVersion())));
        }
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void numericResourceAndFrameInputsWorkInLiveAndExportedGroups()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"wide-inputs-").toAbsolutePath();var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());var includes=new StringBuilder();var checks=new StringBuilder();
        for(int mode=0;mode<3;mode++){
            var f=fixture(mode);var n=MultiSpectrumOutputTest.compiled(f);var def=NodeRegistry.require(n.typeId());var exported=GroupSourceExporter.create(f.document(),f.instance());definitions.add(exported.definition());Files.writeString(dir.resolve(exported.className()+".hpp"),exported.files().get("native/"+exported.className()+".hpp"));includes.append("#include \"").append(exported.className()).append(".hpp\"\n");
            checks.append("check<").append(mode==2?"SpectralGraphNode":"MultipleGraphNode").append(">(").append(quote("!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(n.properties()),p->-1))).append(',').append(mode).append(");\n");
            checks.append("check<").append(exported.className()).append(">(").append(quote("!"+NativeContractGenerator.fingerprint(exported.definition())+" "+exported.definition().encode(new NodeState(exported.definition().defaults()),p->-1))).append(',').append(mode).append(");\n");
        }
        for(boolean spectral:List.of(false,true)){
            BasicNode def=spectral?new SpectralGraphNode():new MultipleGraphNode();var tokens=new ArrayList<>(List.of(def.encode(new NodeState(def.defaults()),p->-1).split(" ")));tokens.remove(spectral?1:4);
            checks.append("{ ").append(spectral?"SpectralGraphNode":"MultipleGraphNode").append(" n;std::istringstream p(").append(quote("!"+(spectral?"779c8958a30145d1":"be6c5b3b1b5d2183")+" "+String.join(" ",tokens))).append(");n.configure(p);assert(n.sources.size()==").append(spectral?17:9).append(");}\n");
        }
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String code="""
            #include "AudioGraph.hpp"
            INCLUDES
            #include <cassert>
            using namespace easyknack;
            template<class T> void check(const char* payload,int mode){
                auto node=std::make_unique<T>();std::istringstream setup(payload);node->configure(setup);node->preparePortStorage();node->prepare(48000,64);
                float values[25]{},out[64]{},silence[64]{};const float* inputs[25]{};char rates[25]{};std::fill_n(rates,25,'c');const DspResource* resources[25]{};const ValueEventBuffer* events[25]{};
                std::vector<std::unique_ptr<SpectrumStream>> streams;
                for(int p=0;p<25;p++){values[p]=float(p);inputs[p]=&values[p];}values[0]=2;
                if(mode!=0){int count=mode==1?12:1;for(int i=0;i<count;i++){auto s=std::make_unique<SpectrumStream>();s->size=s->windowSize=64;s->sampleRate=48000;s->hop=s->blockFrames=64;s->count=1;s->identity=i+1;s->frames.resize(1);s->frames[0].bins.resize(64);s->frames[0].bins[1]={double(i+1),2};SpectrumStream::realSymmetry(s->frames[0],64);resources[mode==1?i+1:0]=s.get();streams.push_back(std::move(s));}}
                ValueEventBuffer trigger;trigger.clear(64);trigger.push(32,1,ValueEventKind::trigger);events[24]=&trigger;inputs[24]=nullptr;rates[24]='e';
                ProcessContext c{inputs,std::size_t(mode==0?21:mode==1?13:25),out,64,48000,silence,64};c.sourceRates=rates;c.resourceInputs=resources;c.valueEventInputs=events;
                node->process(c);
                if(mode==0){for(float value:out)assert(std::abs(value-420)<1e-5);}
                else if(mode==1){for(int p=0;p<12;p++){auto* stream=static_cast<SpectrumStream*>(node->resourceOutput(p));assert(stream&&stream->count==1&&stream->frames[0].bins[1]==std::complex<double>(p+1,2));}}
                else {
                    auto verify=[&](double expected){auto* stream=static_cast<SpectrumStream*>(node->resourceOutput(0));assert(stream&&stream->count==1);assert(std::abs(stream->frames[0].bins[1].real()-expected)<1e-5);};verify(276);
                    auto& source=*streams[0];source.blockStart=64;source.frames[0].start=64;trigger.clear(64);for(int p=1;p<24;p++)values[p]+=100;node->process(c);verify(276);
                    source.blockStart=128;source.frames[0].start=128;trigger.push(1,1,ValueEventKind::trigger);node->process(c);verify(2576);
                }
            }
            int main(){CHECKS}
            """.replace("INCLUDES",includes).replace("CHECKS",checks);
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
