package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;
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

class SpectralGroupsTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void binReadRequiresItsExplicitSpectrumBinding()throws Exception{
        var def=new BinReadNode();
        var dir=Files.createTempDirectory(Path.of("target"),"binread-binding-").toAbsolutePath();
        var code=new StringBuilder("#include \"AudioGraph.hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){\n");
        for(boolean connected:List.of(false,true)){
            var state=new NodeState(def.defaults(),connected?Set.of(BinReadNode.SPECTRUM):Set.of());
            String payload="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(state,p->-1);
            code.append("{BinReadNode node;std::istringstream params(").append(quote(payload)).append(");node.configure(params);")
                .append("StreamSpectrumFrame frame;frame.bins.resize(5);frame.bins[0]={.75,0};node.frame=&frame;node.size=8;float out=99,silence=0;ProcessContext c{nullptr,0,&out,1,48000,&silence,1};node.process(c);assert(out==")
                .append(connected?".75f":"0").append(");}\n");
        }
        code.append("}");var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test void spectralExampleCompilesAndNewGroupIsSaved()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Spectrum",SpectralExample.create());assertTrue(c.audioGraph().contains(" spectralgraph "));
            var before=c.snapshotDocument();c.addSpectralGroup();var after=c.snapshotDocument();
            assertEquals(before.embeddedAssets().size()+1,after.embeddedAssets().size());
            assertEquals(before.root().graph().nodes().size()+1,after.root().graph().nodes().size());
            c.history().undo();assertEquals(before.root().graph().nodes().size(),c.snapshotDocument().root().graph().nodes().size());
        }});
    }
    record Fixture(GLNodeGraphDocument document,GLGraphNode instance){}
    static Fixture fixture(){return fixture(false);}
    static Fixture fixture(boolean muted){
        var bins=SpectralGroups.create();var graph=new GLNodeGraph(bins.graph());
        var context=graph.snapshot().nodes().stream().filter(n->n.typeId().endsWith("bincontext")).findFirst().orElseThrow();
        var output=graph.snapshot().nodes().stream().filter(n->n.typeId().endsWith("binoutput")).findFirst().orElseThrow();
        var multiply=add(graph,new MathNode());var second=add(graph,new MathNode());graph.edit("Bin gain",e->{
            e.setProperty(multiply,"operation",GLNodeValue.of("multiply"));e.setProperty(multiply,"clamp",GLNodeValue.of(false));e.setProperty(multiply,"b",GLNodeValue.of(.5));e.setProperty(output.id(),"mode",GLNodeValue.of("polar"));
            e.setProperty(multiply,GLNodeState.MUTED,GLNodeValue.of(muted));e.setProperty(second,GLNodeState.MUTED,GLNodeValue.of(muted));
            e.setProperty(second,"clamp",GLNodeValue.of(false));e.setProperty(second,"operation",GLNodeValue.of("multiply"));
            for(var w:graph.snapshot().connections())e.removeConnection(w.id());
            e.addConnection(wire(context.id(),"magnitude",multiply,"a"));e.addConnection(wire(multiply,"audio-out",second,"a"));e.addConnection(wire(second,"audio-out",output.id(),"a"));e.addConnection(wire(context.id(),"phase",output.id(),"b"));
        });
        bins=new GLNodeGraphAsset(bins.id(),1,bins.kind(),graph.snapshot(),bins.viewState(),bins.metadata(),bins.compositeDescriptor());
        var low=new GroupChoices.Option("low","Quarter");var high=new GroupChoices.Option("high","Three quarters");
        bins=GroupChoices.store(bins,new GroupChoices.Choice("amount","Gain",Editor.comboBox(List.of(low,high),low,GroupChoices.Option::id)),List.of(new GLCompositePropertyMapping("amount",multiply,"b",Map.of("low",GLNodeValue.of(.25),"high",GLNodeValue.of(.75)))));
        bins=GroupChoices.store(bins,new GroupChoices.Choice("trim","Trim",Editor.comboBox(List.of(low,high),high,GroupChoices.Option::id)),List.of(new GLCompositePropertyMapping("trim",second,"b",Map.of("low",GLNodeValue.of(.5),"high",GLNodeValue.of(1.)))));
        var audio=new GLNodeGraph();var fft=add(audio,new FFTStreamNode());var inverse=add(audio,new IFFTNode());
        var spectral=new GLGraphNode(GLNodeId.random(),bins.compositeDescriptor().nodeTypeId(),Map.of());audio.edit("Spectral",e->{e.addNode(spectral);e.addConnection(wire(fft,"spectrum",spectral.id(),"spectrum"));e.addConnection(wire(spectral.id(),"out",inverse,"spectrum"));});
        var props=bins.compositeDescriptor().properties();var d=new GLCompositeNodeDescriptor("test.spectral.effect","spectral-effect",1,List.of(input("In",SignalCategory.AUDIO,fft,"in"),output("Out",SignalCategory.AUDIO,inverse,"out")),props,List.of(new GLCompositePropertyMapping("amount",spectral.id(),"amount"),new GLCompositePropertyMapping("trim",spectral.id(),"trim")));
        var effect=new GLNodeGraphAsset(d.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,audio.snapshot(),GLNodeEditorViewState.empty(),Map.of(),d);
        var root=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of());root.edit("Root",e->e.addNode(instance));
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(bins.id(),bins,effect.id(),effect),List.of()),instance);
    }
    static GLGraphConnection wire(GLNodeId a,String out,GLNodeId b,String in){return new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a,out),new GLNodeEndpoint(b,in));}
    @Test void spectralGraphsCompileThroughNestedGroupsAndKeepEditableDocument()throws Exception{
        for(boolean muted:List.of(false,true)){
        var f=fixture(muted);var doc=NodeDocuments.prepare(f.document());var registry=new GLCompositeGraphRegistry();registry.registerDocument(doc);
        var flat=SpectralGroups.flatten(GroupCompiler.prepare(doc.root(),registry),new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(flat.isValid());
        assertTrue(flat.graph().orElseThrow().nodes().stream().anyMatch(n->n.typeId().equals("easyknack.spectral.compiled")));
        var export=GroupSourceExporter.create(doc,f.instance());assertNotNull(GroupBakeTest.definition(export));
        assertTrue(export.files().get("native/"+export.className()+".hpp").contains("SpectralGraphNode"));
        var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(doc,bytes);assertEquals(NodeDocuments.comparable(doc),NodeDocuments.comparable(codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion())));
        }
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void exportedSpectralEffectReconstructsBothMappedGainChoices()throws Exception{
        for(boolean muted:List.of(false,true)){
        var f=fixture(muted);var export=GroupSourceExporter.create(f.document(),f.instance());var d=export.definition();
        var dir=Files.createTempDirectory(Path.of("target"),"spectral-export-").toAbsolutePath();Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var defs=new ArrayList<>(NodeRegistry.builtinDefinitions());defs.add(d);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(defs));
        var code=new StringBuilder("#include \"AudioGraph.hpp\"\n#include \""+export.className()+".hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){\n");
        for(var choice:List.of("low","high"))for(var trim:List.of("low","high")){
            var values=new HashMap<>(d.defaults());values.put("choice_0",GLNodeValue.of(choice));values.put("choice_1",GLNodeValue.of(trim));String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(values),p->0);
            code.append("{").append(export.className()).append(" node;std::istringstream params(").append(quote(payload)).append(");node.configure(params);node.prepare(48000,256);").append("std::array<float,256> input{},out{},silence{};const float* inputs[]{input.data()};char rates[]{'a'};ProcessContext c{inputs,1,out.data(),256,48000,silence.data(),256};c.sourceRates=rates;")
                .append("for(int base=0;base<8192;base+=256){for(int i=0;i<256;i++)input[i]=std::sin((base+i)*.03);node.process(c);for(int i=0;i<256;i++){double expected=base+i>=2048?").append(muted?1:(choice.equals("low")?.25:.75)*(trim.equals("low")?.5:1)).append("*std::sin((base+i-2048)*.03):0;assert(std::abs(out[i]-expected)<2e-5);}}}\n");
        }code.append("}");var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
        }
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void standaloneSpectrumExportSupportsBinLookupAndControlScheduledResourcePorts()throws Exception{
        var asset=SpectralGroups.create();var graph=new GLNodeGraph(asset.graph());
        var context=graph.snapshot().nodes().stream().filter(n->n.typeId().endsWith("bincontext")).findFirst().orElseThrow();
        var output=graph.snapshot().nodes().stream().filter(n->n.typeId().endsWith("binoutput")).findFirst().orElseThrow();
        var lookup=add(graph,new BinReadNode());graph.edit("Lookup",e->{
            for(var w:graph.snapshot().connections())e.removeConnection(w.id());
            e.addConnection(wire(context.id(),"index",lookup,"index"));e.addConnection(wire(lookup,"real",output.id(),"a"));e.addConnection(wire(lookup,"imaginary",output.id(),"b"));
        });
        var descriptor=asset.compositeDescriptor();
        var mappings=descriptor.portMappings().stream().map(p->{
            if(!p.publicPort().id().equals("spectrum"))return p;
            var endpoints=new ArrayList<>(p.internalEndpoints());endpoints.add(new GLNodeEndpoint(lookup,"spectrum"));return p.withEndpoints(endpoints);
        }).toList();
        descriptor=new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),1,mappings,descriptor.properties(),descriptor.propertyMappings());
        asset=new GLNodeGraphAsset(asset.id(),1,asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),descriptor);
        var root=new GLNodeGraph();var node=new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of());root.edit("Root",e->e.addNode(node));
        var document=new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
        var export=GroupSourceExporter.create(document,node);assertNotNull(GroupBakeTest.definition(export));var def=export.definition();
        var dir=Files.createTempDirectory(Path.of("target"),"spectrum-boundary-").toAbsolutePath();Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var defs=new ArrayList<>(NodeRegistry.builtinDefinitions());defs.add(def);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(defs));
        String payload="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(def.defaults()),p->0);
        String code="""
            #include "AudioGraph.hpp"
            #include "%s.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                %s node;std::istringstream params(%s);node.configure(params);node.prepare(48000,256);
                FFTStreamNode fft;fft.prepare(48000,256);IFFTNode inverse;inverse.prepare(48000,256);
                std::array<float,256> input{},out{},silence{};const float* inputs[]{input.data()};const DspResource* resources[]{&fft.stream};char rates[]{'c'};
                for(int base=0;base<8192;base+=256){
                    for(int i=0;i<256;i++)input[i]=std::sin((base+i)*.07);
                    ProcessContext c{inputs,1,out.data(),256,48000,silence.data(),256};fft.process(c);
                    c.resourceInputs=resources;c.sourceRates=rates;c.bufferSize=1;node.process(c);
                    auto* stream=static_cast<SpectrumStream*>(node.resourceOutput(0));assert(stream&&stream->blockStart==static_cast<unsigned>(base));
                    const DspResource* result[]{stream};c.resourceInputs=result;c.bufferSize=256;inverse.process(c);
                    for(int i=0;i<256;i++){double expected=base+i>=2048?std::sin((base+i-2048)*.07):0;assert(std::abs(out[i]-expected)<2e-5);}
                }
            }
            """.formatted(export.className(),export.className(),quote(payload));
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void hostSchedulerKeepsSpectralStereoIndependent()throws Exception{
        for(BasicNode fft:List.of(new FFTStreamNode(),new SpectrumNode())){
        var inverse=new IFFTNode();
        String graph="GRAPH 4 src input 61 copy stereo @ a - fft "+fft.processor().id()+" !"+NativeContractGenerator.fingerprint(fft)+" "+fft.encode(new NodeState(fft.defaults()),p->0)+" @ c a inv ifft !"+NativeContractGenerator.fingerprint(inverse)+" "+inverse.encode(new NodeState(inverse.defaults()),p->1)+" @ a c out out 2 0 @ a a";
        var dir=Files.createTempDirectory(Path.of("target"),"spectrum-stereo-").toAbsolutePath();
        String code="""
            #include "AudioGraph.hpp"
            #include <cassert>
            using namespace easyknack;
            struct Input:AudioIO{
                int base=0;
                void beginBlock(std::size_t)noexcept override{}
                float input(const std::string&,std::size_t channel,std::size_t offset)const noexcept override{return std::sin((base+offset)*(channel?.08:.03))*(channel?.25:.5);}
                void output(const std::string&,std::size_t,const float*,std::size_t)noexcept override{}
                void endBlock(const float*,std::size_t)noexcept override{}
            };
            int main(){auto graph=AudioGraph::parse(%s);graph.prepare(48000,256);Input input;std::array<float,256> out{};
                for(input.base=0;input.base<8192;input.base+=256)for(int channel=0;channel<2;channel++){
                    graph.process(out.data(),256,channel,&input);
                    for(int i=0;i<256;i++){double expected=input.base+i>=2048?std::sin((input.base+i-2048)*(channel?.08:.03))*(channel?.25:.5):0;assert(std::abs(out[i]-expected)<2e-5);}
                }
            }
            """.formatted(quote(graph));
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
        }
    }
}
