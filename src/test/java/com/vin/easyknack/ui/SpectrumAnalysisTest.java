package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.*;
import com.vingame.glswing.node.model.*;

class SpectrumAnalysisTest {
    @Test void largeWindowsAreSelectableAndAnalysisAcceptsEveryPaddedBin(){
        for(BasicNode node:List.of(new SpectrumNode(),new FFTStreamNode())){
            var row=node.layout(new NodeState(node.defaults())).rows().stream().filter(r->r.id().equals("size")).findFirst().orElseThrow();
            var sizes=(Editor.ComboBox<?>)row.editor();
            assertTrue(sizes.items().containsAll(List.of("16384","32768","65536")));
            for(String size:List.of("16384","32768","65536")){
                var state=new HashMap<>(node.defaults());state.put("size",GLNodeValue.of(size));
                assertTrue(node.encode(new NodeState(state),p->-1).contains(" "+size+" "));
            }
        }
        var message=new StringBuilder("OK 2 fft 0 0 24000 0 65537");
        for(int i=0;i<65537;i++)message.append(i==65536?" 0.75":" 0");
        var frame=AudioAnalysis.parse(message.toString()).get("fft");
        assertEquals(65537,frame.values().length);assertEquals(.75f,frame.values()[65536]);
        assertThrows(IllegalArgumentException.class,()->AudioAnalysis.parse("OK 2 fft 0 0 24000 0 65538"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void largestPaddedWindowReachesTheViewerThroughTheHostProtocol()throws Exception{
        var graph=new GLNodeGraph();var osc=add(graph,new OscillatorNode());
        var fft=add(graph,new SpectrumNode(),Map.of("size",GLNodeValue.of("65536"),"padding",GLNodeValue.of("double"),"overlap",GLNodeValue.of(0.0)));
        var viewer=add(graph,new SpectrumViewerNode());connect(graph,osc,"audio-out",fft,"audio-in");connect(graph,fft,"analysis",viewer,"analysis");
        try(var host=new AudioHostClient()){
            host.start(AudioHostClient.defaultExecutable(),true);assertEquals("OK",host.command(AudioGraphEncoder.encode(graph.snapshot())));host.command("PLAY");
            var frame=awaitFrame(host,viewer,65537);assertEquals(24000,frame.span(),1e-6);
            assertTrue(host.command("PING").startsWith("OK"));host.command("STOP");
        }
    }
    @Test void analysisTitlesHaveASeparateYellowThemePaletteAndFftCanMix(){
        for(var node:NodeRegistry.builtinDefinitions())if(node.registration().category()==NodeCategory.ANALYSIS)assertEquals(node instanceof WavetableViewerNode?NodeKind.WAVETABLE:NodeKind.ANALYSIS,node.registration().kind());
        var colors=ApplicationTheme.current().title(NodeKind.ANALYSIS);
        assertEquals(ApplicationTheme.current().signal(SignalCategory.ANALYSIS),colors.light());assertNotEquals(colors.light(),colors.dark());
        var fft=new SpectrumNode();var values=new HashMap<>(fft.defaults());values.put("channel",GLNodeValue.of("mix"));
        assertTrue(fft.encode(new NodeState(values),p->-1).contains(" mix "));
    }
    @Test void correlationStagesUseAnalysisResourcesAndLegacyRemainsLoadable(){
        var correlation=new SpectrumCorrelationNode();var pitch=new PitchDetectionNode();
        assertTrue(correlation.isListedInCatalog());assertTrue(pitch.isListedInCatalog());
        assertFalse(new AutocorrelationNode().isListedInCatalog());
        assertTrue(NodeRegistry.find("easyknack.analysis.autocorrelation").isPresent());
        assertTrue(NodeRegistry.find(correlation.registration().id()).isPresent());
        assertTrue(NodeRegistry.find(pitch.registration().id()).isPresent());
        assertEquals("none",new SpectrumNode().defaults().get("padding").toJavaValue());
        assertTrue(correlation.layout(new NodeState(correlation.defaults())).rows().stream().filter(r->r.id().equals("spectrum")||r.id().equals("analysis")).allMatch(r->r.dataType()==(r.id().equals("spectrum")?PortDataType.SPECTRUM:PortDataType.ANALYSIS)));
        assertEquals("biased",correlation.defaults().get("normalization").toJavaValue());
        assertEquals(1.0,((Number)correlation.defaults().get("smoothing").toJavaValue()).doubleValue());
        assertEquals(false,new SpectrumNode().defaults().get("remove-dc").toJavaValue());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void fftCorrelationPipelineSupportsPaddingChangesAndDisconnects()throws Exception{
        var graph=new GLNodeGraph();var osc=add(graph,new OscillatorNode(),Map.of("frequency",GLNodeValue.of(440.0)));
        var fft=add(graph,new SpectrumNode(),Map.of("size",GLNodeValue.of("8192"),"padding",GLNodeValue.of("double"),"window",GLNodeValue.of("rectangular")));
        var correlation=add(graph,new SpectrumCorrelationNode());var viewer=add(graph,new SpectrumViewerNode());
        connect(graph,osc,"audio-out",fft,"audio-in");connect(graph,fft,"analysis",correlation,"spectrum");connect(graph,correlation,"analysis",viewer,"analysis");
        try(var host=new AudioHostClient()){
            host.start(AudioHostClient.defaultExecutable(),true);assertEquals("OK",host.command(AudioGraphEncoder.encode(graph.snapshot())));host.command("PLAY");
            var frame=awaitFrame(host,viewer,8192);assertTrue(frame.symmetric());assertEquals(1,frame.values()[0],1e-6);
            assertEquals(8191.0/48000,frame.span(),1e-6);assertTrue(frame.values()[109]>.95);
            graph.edit("Change FFT",e->{e.setProperty(fft,"size",GLNodeValue.of("256"));e.setProperty(fft,"padding",GLNodeValue.of("none"));});
            assertEquals("OK",host.command(AudioGraphEncoder.encode(graph.snapshot())));assertTrue(awaitFrame(host,viewer,256).symmetric());
            assertEquals("OK",host.command("GRAPH 0"));assertEquals("OK",host.command("STOP"));
        }
    }
    @Test void overlapSliderMigratesOldSelectionsAndKeepsArbitraryValues(){
        var node=new SpectrumNode();var old=new HashMap<>(node.defaults());old.put("overlap",GLNodeValue.of("half"));
        var state=new NodeState(old);assertDoesNotThrow(()->node.layout(state));
        assertTrue(node.encode(state,p->-1).contains(" 0.5 "));
        var types=new GLNodeTypeRegistry();var registry=new NodeRegistry(types,new GLNodeCatalog());
        var normalized=registry.normalize(new GLGraphNode(GLNodeId.random(),node.registration().id(),old));
        assertEquals(.5,normalized.properties().get("overlap").toJavaValue());
        var row=node.layout(new NodeState(node.defaults())).rows().stream().filter(r->r.key().equals(SpectrumNode.OVERLAP)).findFirst().orElseThrow();
        assertInstanceOf(UnitSlider.class,row.editor());assertTrue(((UnitSlider)row.editor()).spec().bar());
        old.put("overlap",GLNodeValue.of(.637));assertTrue(node.encode(new NodeState(old),p->-1).contains(" 0.637 "));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void analysisViewerAutomaticallyDisplaysTheSharedAutocorrelation()throws Exception{
        var graph=new GLNodeGraph();var osc=add(graph,new OscillatorNode());var correlation=add(graph,new AutocorrelationNode());var viewer=add(graph,new SpectrumViewerNode());
        connect(graph,osc,"audio-out",correlation,"audio-in");connect(graph,correlation,"analysis",viewer,"analysis");
        try(var host=new AudioHostClient()){
            host.start(AudioHostClient.defaultExecutable(),true);assertEquals("OK",host.command(AudioGraphEncoder.encode(graph.snapshot())));host.command("PLAY");
            var frame=awaitFrame(host,viewer,8192);assertTrue(frame.symmetric());assertEquals(440,frame.frequency(),2);assertTrue(frame.confidence()>.9);
            assertEquals(8191./48000,frame.span(),1e-6);
            assertEquals("OK",host.command("STOP"));
        }
    }
    @Test void directAudioAndConsumerSelectionReplaceTheLegacyCatalogChain(){
        var fft=new SpectrumNode();var layout=fft.layout(new NodeState(fft.defaults()));
        var input=layout.rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.INPUT).findFirst().orElseThrow();
        assertEquals(PortDataType.SIGNAL,input.dataType());assertEquals(PortRates.AUDIO_CONTROL,input.rates());
        assertFalse(fft.defaults().containsKey("minimum"));assertFalse(fft.defaults().containsKey("maximum"));
        for(var key:List.of("size","window","overlap","channel"))assertTrue(fft.defaults().containsKey(key));
        for(BasicNode consumer:List.of(new SpectrumViewerNode(),new SpectrumStatisticsNode())){
            var view=consumer.layout(new NodeState(consumer.defaults())).rows().stream().filter(r->r.id().equals("view")).findFirst().orElseThrow();
            assertEquals(consumer instanceof SpectrumViewerNode?List.of("Auto","Magnitude","Phase","Power","Correlation"):List.of(SpectrumView.values()),((Editor.ComboBox<?>)view.editor()).items());
        }
        for(BasicNode old:List.of(new FFTAnalysisNode(),new AnalysisViewNode(),new AnalysisViewerNode(),new AnalysisStatisticsNode())){
            assertFalse(old.isListedInCatalog());assertTrue(NodeRegistry.find(old.registration().id()).isPresent());
        }
        // Old source plugins must retain byte-identical contract fingerprints.
        assertEquals("9d0952fca297181f",NativeContractGenerator.fingerprint(new AnalysisViewerNode()));
        assertEquals("502dd8eefa16b9dc",NativeContractGenerator.fingerprint(new AnalysisStatisticsNode()));
    }
    @Test void settingsAndDirectConnectionsSurviveDocumentRoundTrip()throws Exception{
        var graph=new GLNodeGraph();var source=add(graph,new OscillatorNode());var fft=add(graph,new SpectrumNode(),Map.of("size",GLNodeValue.of("4096"),"window",GLNodeValue.of("blackman")));
        var viewer=add(graph,new SpectrumViewerNode(),Map.of("view",GLNodeValue.of("phase")));var stats=add(graph,new SpectrumStatisticsNode(),Map.of("view",GLNodeValue.of("power")));
        connect(graph,source,"audio-out",fft,"audio-in");connect(graph,fft,"analysis",viewer,"analysis");connect(graph,fft,"analysis",stats,"analysis");
        var doc=new com.vingame.glswing.node.document.GLNodeGraphDocument(new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,graph.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty()),Map.of(),List.of());
        var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(doc,bytes);
        var restored=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
        assertEquals(AudioGraphEncoder.encode(graph.snapshot()),AudioGraphEncoder.encode(restored.root().graph()));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void exportedFftRetainsComplexResourceAndRunsTheWholeAudioBlock()throws Exception{
        var inner=new GLNodeGraph();var fft=add(inner,new SpectrumNode(),Map.of("size",GLNodeValue.of("256"),"window",GLNodeValue.of("rectangular"),"overlap",GLNodeValue.of("none"),"padding",GLNodeValue.of("double"),"synchronous",GLNodeValue.of(false)));
        var correlation=add(inner,new SpectrumCorrelationNode());var pitch=add(inner,new PitchDetectionNode());
        connect(inner,fft,"analysis",correlation,"spectrum");connect(inner,correlation,"analysis",pitch,"analysis");
        var descriptor=new com.vingame.glswing.node.composite.GLCompositeNodeDescriptor("test.spectrum","test.spectrum.asset",1,List.of(input("In",SignalCategory.AUDIO,fft,"audio-in"),output("Out",SignalCategory.SPECTRUM,fft,"analysis"),output("Correlation",SignalCategory.ANALYSIS,correlation,"analysis"),output("Frequency",SignalCategory.VALUE,pitch,"frequency"),output("Confidence",SignalCategory.VALUE,pitch,"confidence")));
        var asset=new com.vingame.glswing.node.document.GLNodeGraphAsset(descriptor.assetId(),1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var root=new GLNodeGraph();var group=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(group));
        var doc=new com.vingame.glswing.node.document.GLNodeGraphDocument(new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,root.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
        var export=GroupSourceExporter.create(doc,group,true);var d=export.definition();
        var dir=java.nio.file.Files.createTempDirectory(java.nio.file.Path.of("target"),"spectrum-export-").toAbsolutePath();
        java.nio.file.Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(d);
        java.nio.file.Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->0);
        String code="#include \"AudioGraph.hpp\"\n#include \""+export.className()+".hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){"+export.className()+" baked;std::istringstream setup("+quote(payload)+");baked.configure(setup);baked.prepare(48000,256);"+"""
            SpectrumNode* fft=nullptr;SpectrumCorrelationNode* correlation=nullptr;
            for(auto& n:baked.runtime->graph.nodes){if(auto* found=dynamic_cast<SpectrumNode*>(n.processor.get()))fft=found;if(auto* found=dynamic_cast<SpectrumCorrelationNode*>(n.processor.get()))correlation=found;}
            assert(fft&&correlation);fft->job=std::make_shared<AnalysisJob>();correlation->job=std::make_shared<AnalysisJob>(); // Deterministic worker scheduling for this test.
            std::array<float,256> input{},out{},silence{};input.fill(.25f);const float* inputs[]{input.data()};char rate='c';
            ProcessContext c{inputs,1,out.data(),1,48000,silence.data(),256};c.sourceRates=&rate;
            baked.process(c);assert(fft->end==256);fft->job->work();baked.process(c);
            auto* resource=static_cast<AnalysisResource*>(baked.resourceOutput(0));assert(resource&&resource->result);
            const auto& frame=resource->result->spectrum;
            assert(frame.fftFrames==512&&frame.windowFrames==256&&frame.hopFrames==256&&frame.endSample==256);
            assert(std::abs(frame.bins[0].real()-64)<1e-7&&std::abs(resource->result->views[0][0]-.25)<1e-7);
            correlation->job->work();baked.process(c);
            auto* curve=static_cast<AnalysisResource*>(baked.resourceOutput(1));assert(curve&&curve->result&&curve->result->correlationCount==256);
            assert(std::abs(curve->result->views[3][0]-1)<1e-7&&std::abs(curve->result->views[3][255]-1.f/256)<1e-7);
            }
            """;
        var source=dir.resolve("test.cpp");java.nio.file.Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void directFftPublishesNyquistAndSurvivesLiveWindowAndViewChanges()throws Exception{
        var graph=new GLNodeGraph();var osc=add(graph,new OscillatorNode(),Map.of("frequency",GLNodeValue.of(750.0)));
        var fft=add(graph,new SpectrumNode(),Map.of("size",GLNodeValue.of("1024"),"channel",GLNodeValue.of("mix")));
        var viewer=add(graph,new SpectrumViewerNode());
        connect(graph,osc,"audio-out",fft,"audio-in");connect(graph,fft,"analysis",viewer,"analysis");
        try(var host=new AudioHostClient()){
            host.start(AudioHostClient.defaultExecutable(),true);assertEquals("OK",host.command(AudioGraphEncoder.encode(graph.snapshot())));host.command("PLAY");
            AudioAnalysis.Frame frame=awaitFrame(host,viewer,513);
            assertFalse(frame.symmetric());assertEquals(24000,frame.span(),1e-6);assertEquals(0,frame.frequency());
            int peak=0;for(int i=1;i<frame.values().length;i++)if(frame.values()[i]>frame.values()[peak])peak=i;
            assertEquals(16,peak);assertEquals(1,frame.values()[peak],.03);
            for(int i=0;i<12;i++){
                final String size=i%2==0?"256":"2048",view=i%2==0?"phase":"power";
                graph.edit("FFT settings",e->{e.setProperty(fft,"size",GLNodeValue.of(size));e.setProperty(viewer,"view",GLNodeValue.of(view));});
                assertEquals("OK",host.command(AudioGraphEncoder.encode(graph.snapshot())));
                assertNotNull(awaitFrame(host,viewer,Integer.parseInt(size)/2+1));assertTrue(host.command("PING").startsWith("OK"));
            }
            assertEquals("OK",host.command("GRAPH 0"));assertEquals("OK",host.command("STOP"));
        }
    }
    private static AudioAnalysis.Frame awaitFrame(AudioHostClient host,GLNodeId viewer,int count)throws Exception{
        long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(4);
        while(System.nanoTime()<deadline){
            Thread.sleep(30);var frame=AudioAnalysis.parse(host.command("ANALYSIS")).get(viewer.toString());
            if(frame!=null&&frame.values().length==count)return frame;
        }
        fail("No FFT frame with "+count+" bins");return null;
    }
}
