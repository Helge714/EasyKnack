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

/** Authored example composed entirely of existing nodes; regeneration is opt-in. */
class StochasticBinWidthExampleTest {
    static final String INDEX_CODE="""
        double displacement = 2.0 * random - 1.0;
        if (gaussian > 0.5) {
            // Approximate inverse normal CDF, restricted to +/- 3 sigma.
            double u = 0.001349898 + 0.997300204 * random;
            double x = 2.0 * u - 1.0;
            double l = log(1.0 - x*x);
            double a = 2.0 / (pi * 0.147) + l / 2.0;
            displacement = sqrt(2.0 * max(0.0, sqrt(a*a - l/0.147) - a)) / 3.0;
            if (x < 0.0) displacement = -displacement;
        }
        selected = index;
        if (position > 0.0 && position < 1.0) {
            double last = floor(index / position + 0.5);
            double radius = min(max(width, 0.0), last);
            double lo = max(1.0, ceil(index - radius));
            double hi = min(last - 1.0, floor(index + radius));
            selected = clamp(floor(index + radius*displacement + 0.5), lo, hi);
        }
        """;
    static final String PHASE_CODE="""
        double cycles = (index - selected) * (frequency / max(index, 1.0)) * time;
        cycles = cycles - floor(cycles);
        result = phase + tau * cycles;
        """;
    record Fixture(GLNodeGraphDocument document,GLGraphNode instance,String assetId){}
    private static GLNodeId id(String key){return new GLNodeId(UUID.nameUUIDFromBytes(("stochastic-bin-width/"+key).getBytes(java.nio.charset.StandardCharsets.UTF_8)));}
    private static GLGraphNode node(String name,BasicNode definition){return new GLGraphNode(id(name),definition.registration().id(),definition.defaults());}
    private static GLGraphConnection wire(GLNodeId a,String out,GLNodeId b,String in){return new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a,out),new GLNodeEndpoint(b,in));}
    private static GLCompositePortPresentation slider(double value,String unit,boolean checkbox){
        var options=new HashMap<String,GLNodeValue>();options.put("default",GLNodeValue.of(value));options.put("minimumEnabled",GLNodeValue.of(true));options.put("minimum",GLNodeValue.of(0.));options.put("maximumEnabled",GLNodeValue.of(false));
        options.put(GroupUnits.BASE,GLNodeValue.of(unit));options.put(GroupUnits.ALLOWED,GLNodeValue.of(List.of(unit)));
        if(checkbox)options.put("presentation",GLNodeValue.of("checkbox"));
        return new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,options);
    }
    private static GLNodeGraphAsset formula(String name,String output,List<String> inputs,String code){
        String assetId="example.stochastic."+name.toLowerCase(Locale.ROOT).replace(' ','-');var ports=new ArrayList<GLCompositePortMapping>();
        ports.add(FormulaGroups.normalize(new GLCompositePortMapping(GLNodePortDescriptor.output(output,SignalCategory.VALUE.valueTypeId()),null)));
        for(var input:inputs){String unit=switch(input){case "width"->UnitFormat.BIN.id();case "phase"->UnitFormat.RADIAN.id();case "frequency"->UnitFormat.HZ.id();case "time"->UnitFormat.SECOND.id();default->UnitFormat.FACTOR.id();};ports.add(FormulaGroups.normalize(new GLCompositePortMapping(GLNodePortDescriptor.input(input,SignalCategory.VALUE.valueTypeId(),false,1),null,slider(0,unit,false))));}
        var descriptor=new GLCompositeNodeDescriptor("easyknack.group."+assetId,assetId,1,ports);
        var metadata=Map.of(FormulaGroups.MARKER,GLNodeValue.of(true),FormulaGroups.MARKER+".version",GLNodeValue.of(3),FormulaGroups.RATE,GLNodeValue.of("auto"),"easyknack.title",GLNodeValue.of(name),"easyknack.kind",GLNodeValue.of("VALUE"));
        return FormulaGroups.accept(new GLNodeGraphAsset(assetId,1,GLNodeGraphAssetKind.COMPOSITE,new GLNodeGraph().snapshot(),GLNodeEditorViewState.empty(),metadata,descriptor),code);
    }
    static Fixture fixture(double width,boolean gaussian){
        var index=formula("Kernel position","selected",List.of("index","position","random","width","gaussian"),INDEX_CODE);
        var phase=formula("Phase compensation","result",List.of("phase","index","selected","frequency","time"),PHASE_CODE);
        var context=node("context",new BinContextNode());var random=node("random",new BinRandomNode());var reader=node("reader",new BinReadNode());
        var output=node("bin-output",new BinOutputNode()).withProperty("mode",GLNodeValue.of("polar"));
        var select=new GLGraphNode(id("select"),index.compositeDescriptor().nodeTypeId(),Map.of());var compensate=new GLGraphNode(id("phase"),phase.compositeDescriptor().nodeTypeId(),Map.of());
        var graph=new GLNodeGraph();graph.edit("Stochastic kernel",edit->{
            for(var node:List.of(context,random,reader,output,select,compensate))edit.addNode(node);
            for(String port:List.of("index","position"))edit.addConnection(wire(context.id(),port,select.id(),port));
            edit.addConnection(wire(random.id(),"out",select.id(),"random"));edit.addConnection(wire(select.id(),"selected",reader.id(),"index"));
            edit.addConnection(wire(reader.id(),"magnitude",output.id(),"a"));edit.addConnection(wire(reader.id(),"phase",compensate.id(),"phase"));
            for(String port:List.of("index","frequency","time"))edit.addConnection(wire(context.id(),port,compensate.id(),port));
            edit.addConnection(wire(select.id(),"selected",compensate.id(),"selected"));edit.addConnection(wire(compensate.id(),"result",output.id(),"b"));
        });
        String assetId="example.stochastic-bin-width";var ports=new ArrayList<GLCompositePortMapping>();
        ports.add(new GLCompositePortMapping(GLNodePortDescriptor.output("out",SignalCategory.SPECTRUM.valueTypeId()),new GLNodeEndpoint(output.id(),"spectrum")));
        ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input("spectrum",SignalCategory.SPECTRUM.valueTypeId(),false,1),new GLNodeEndpoint(context.id(),"spectrum")).withEndpoints(List.of(new GLNodeEndpoint(context.id(),"spectrum"),new GLNodeEndpoint(reader.id(),"spectrum"))));
        ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input("Width",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(select.id(),"width"),slider(width,"bin",false)));
        ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input("Gaussian",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(select.id(),"gaussian"),slider(gaussian?1:0,"factor",true)));
        var descriptor=new GLCompositeNodeDescriptor("easyknack.group."+assetId,assetId,1,ports);
        var metadata=Map.of(SpectralGroups.MARKER,GLNodeValue.of(true),SpectralGroups.SKIP_EMPTY,GLNodeValue.of("false"),"easyknack.title",GLNodeValue.of("Stochastic Bin Width"),"easyknack.category",GLNodeValue.of("Spectral"),"easyknack.kind",GLNodeValue.of("SPECTRUM"),GroupRouting.INSERT_IN,GLNodeValue.of("spectrum"),GroupRouting.INSERT_OUT,GLNodeValue.of("out"),GroupRouting.MUTE_IN,GLNodeValue.of("spectrum"),GroupRouting.MUTE_OUT,GLNodeValue.of("out"));
        var views=new LinkedHashMap<GLNodeId,GLNodeViewState>();int at=0;for(var n:List.of(context,random,select,reader,compensate,output))views.put(n.id(),new GLNodeViewState(40+(at/2)*360,60+(at++%2)*360,280,300));
        var asset=new GLNodeGraphAsset(assetId,1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),new GLNodeEditorViewState(views),metadata,descriptor);
        var registry=new GLCompositeGraphRegistry();registry.registerAsset(index);registry.registerAsset(phase);registry.registerAsset(asset);
        for(var entry:Map.of("Seed","seed","Vary by frame","by-frame").entrySet())ports.add(GroupEditorCopies.copy(new GLCompositePortMapping(GLNodePortDescriptor.input(entry.getKey(),SignalCategory.VALUE.valueTypeId(),false,1),null),new GLNodeEndpoint(random.id(),entry.getValue()),asset,registry));
        asset=GroupInputs.materialize(GroupInputs.descriptor(asset,new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),assetId,1,ports)),registry);
        var root=new GLNodeGraph();var instance=new GLGraphNode(id("instance"),descriptor.nodeTypeId(),Map.of());
        var osc=node("oscillator",new OscillatorNode());var fft=node("fft",new SpectrumNode());var ifft=node("ifft",new IFFTNode());var audio=node("audio-output",new OutputNode());
        root.edit("Example",edit->{for(var n:List.of(osc,fft,instance,ifft,audio))edit.addNode(n);edit.addConnection(wire(osc.id(),"audio-out",fft.id(),"audio-in"));edit.addConnection(wire(fft.id(),"analysis",instance.id(),"spectrum"));edit.addConnection(wire(instance.id(),"out",ifft.id(),"spectrum"));edit.addConnection(wire(ifft.id(),"out",audio.id(),"audio-in"));});
        var rootViews=new LinkedHashMap<GLNodeId,GLNodeViewState>();at=0;for(var n:List.of(osc,fft,instance,ifft,audio))rootViews.put(n.id(),new GLNodeViewState(40+at++*340,80,280,420));
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("example.root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),new GLNodeEditorViewState(rootViews)),Map.of(assetId,asset,index.id(),index,phase.id(),phase),List.of()),instance,assetId);
    }
    @Test void existingNodesCompileImportAndPersistTheExample()throws Exception{
        var fixture=fixture(8,true);var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(fixture.document(),bytes);
        var restored=NodeDocuments.prepare(codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(restored);var flat=SpectralGroups.flatten(GroupCompiler.prepare(restored.root(),registry),new GLCompositeExpansionLimits(16,512,1024,100000));
        assertTrue(flat.isValid(),flat.diagnostics().toString());assertDoesNotThrow(()->com.vin.easyknack.audio.AudioGraphEncoder.encode(flat.graph().orElseThrow()));
        assertEquals(3,GroupImport.copyMany(restored,List.of(fixture.assetId()),"Stochastic Bin Width").assets().size());
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){controller.openDocument("Stochastic Bin Width",restored);assertTrue(controller.workspace().openComposite(fixture.assetId()));assertDoesNotThrow(controller::audioGraph);}});
        if(Boolean.getBoolean("easyknack.writeExamples"))try(var out=Files.newOutputStream(Path.of("examples/stochastic-bin-width.knack"))){codec.write(restored,out);}
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeKernelReadsSingleNearbyBinsAndKeepsPhaseAcrossFrames()throws Exception{
        var fixture=fixture(8,true);var registry=new GLCompositeGraphRegistry();registry.registerDocument(fixture.document());
        var result=SpectralGroups.flatten(GroupCompiler.prepare(fixture.document().root(),registry),new GLCompositeExpansionLimits(16,512,1024,100000));assertTrue(result.isValid(),result.diagnostics().toString());
        var graph=result.graph().orElseThrow();var compiled=graph.nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof SpectralGraphNode).findFirst().orElseThrow();
        var definition=NodeRegistry.require(compiled.typeId());var state=new NodeState(compiled.properties());
        String payload="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(state,p->0);
        var values=new double[1+SpectralGraphNode.inputCount(state)];
        for(var wire:graph.connections())if(wire.input().nodeId().equals(compiled.id())&&wire.input().portId().startsWith("external")){
            int slot=1+Integer.parseInt(wire.input().portId().substring(8));var source=graph.findNode(wire.output().nodeId()).orElseThrow();values[slot]=new NodeState(source.properties()).number("value",0);
        }
        var dir=Files.createTempDirectory(Path.of("target"),"stochastic-bin-width-").toAbsolutePath();Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(NodeRegistry.builtinDefinitions()));
        var source=dir.resolve("test.cpp");Files.writeString(source,"""
            #include "nodes/SpectralGraphNode.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                auto node=std::make_unique<SpectralGraphNode>();std::istringstream parameters(%s);node->configure(parameters);node->prepare(48000,256);
                SpectrumStream source;source.prepare(256);source.size=source.windowSize=256;source.hop=64;source.sampleRate=48000;source.count=1;
                float silence[256]{},out[256]{},data[32][256]{};const float* inputs[32]{};char rates[32]{};std::fill_n(rates,32,'c');
                double defaults[]={%s};for(unsigned p=0;p<std::size(defaults);p++){std::fill_n(data[p],256,float(defaults[p]));inputs[p]=data[p];}
                const DspResource* resources[32]{};resources[0]=&source;ProcessContext c{inputs,32,out,256,48000,silence,256};c.sourceRates=rates;c.resourceInputs=resources;
                int previous[129]{};bool changed=false;
                for(unsigned block=0;block<3;block++){
                    source.blockStart=block*64;source.frames[0].start=block*64;
                    for(unsigned k=0;k<=128;k++)source.frames[0].bins[k]=std::polar(100.+k,6.283185307179586*k*block/4.);
                    SpectrumStream::realSymmetry(source.frames[0],256);node->process(c);
                    auto* result=static_cast<SpectrumStream*>(node->resourceOutput(0));assert(result&&result->count==1);
                    for(unsigned k=0;k<=128;k++){
                        auto value=result->frames[0].bins[k];int selected=int(std::round(std::abs(value)-100));
                        assert(selected>=0&&selected<=128&&std::abs(selected-int(k))<=8);assert(std::abs(std::abs(value)-(100+selected))<.003);
                        if(block)assert(selected==previous[k]);previous[k]=selected;if(selected!=int(k))changed=true;
                        auto expected=std::polar(100.+selected,6.283185307179586*k*block/4.);assert(std::abs(value-expected)<.01);
                        if(k>0&&k<128)assert(result->frames[0].bins[256-k]==std::conj(value));
                    }
                }
                assert(changed); // Nonzero Width must actually select neighbouring source bins.
            }
            """.formatted(AdvancedSourceExportTest.quote(payload),java.util.Arrays.stream(values).mapToObj(Double::toString).collect(java.util.stream.Collectors.joining(","))));
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
