package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioGraphEncoder;
import com.vingame.glswing.node.model.*;

class RateSeparationTest {
    @Test @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void exportedGeneratorRetainsAdaptiveInputsAndSampleAccurateTriggers()throws Exception{
        var inner=new GLNodeGraph();var noise=add(inner,new DigitalNoiseNode());
        var descriptor=new com.vingame.glswing.node.composite.GLCompositeNodeDescriptor("test.noise","test.noise.asset",1,
            List.of(input("Frequency",SignalCategory.VALUE,noise,"frequency"),output("Trigger",SignalCategory.TRIGGER,noise,"trigger")));
        var asset=new com.vingame.glswing.node.document.GLNodeGraphAsset(descriptor.assetId(),1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var root=new GLNodeGraph();var group=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(group));
        var doc=new com.vingame.glswing.node.document.GLNodeGraphDocument(new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,root.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
        var export=GroupSourceExporter.create(doc,group,true);var d=export.definition();
        var dir=java.nio.file.Files.createTempDirectory(java.nio.file.Path.of("target"),"adaptive-noise-").toAbsolutePath();
        java.nio.file.Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(d);
        java.nio.file.Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->0);
        String code="#include \"AudioGraph.hpp\"\n#include \""+export.className()+".hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){"+export.className()+" baked;std::istringstream setup("+quote(payload)+");baked.configure(setup);baked.prepare(48000,256);"+"""
            DigitalNoiseNode reference;std::array<float,256> input{},a{},b{},factor{},silence{};
            for(int i=0;i<256;i++)input[i]=i<80?100.f:12000.f;
            const float* ins[]{input.data(),nullptr};char rates[]{'a','c'};
            float* referenceOutputs[]{factor.data(),b.data()};
            ValueEventBuffer actual,expected;ValueEventBuffer* ae[]{&actual};ValueEventBuffer* re[]{nullptr,&expected};
            ProcessContext c{ins,1,a.data(),256,48000,silence.data(),256};c.sourceRates=rates;c.valueEventOutputs=ae;
            ProcessContext r{ins,2,factor.data(),256,48000,silence.data(),256};r.sourceRates=rates;r.outputs=referenceOutputs;r.outputCount=2;r.valueEventOutputs=re;
            for(int block=0;block<6;block++){
                rates[0]=block%2?'c':'a';actual.clear(256);expected.clear(256);baked.process(c);reference.process(r);
                assert(actual.count==expected.count);
                for(unsigned i=0;i<actual.count;i++)assert(actual.events[i].sampleOffset==expected.events[i].sampleOffset);
            }
            }
            """;
        var source=dir.resolve("test.cpp");java.nio.file.Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test void generatorInputsFollowSourcesWithoutANodeRateSelector(){
        var graph=new GLNodeGraph();var audio=add(graph,new OscillatorNode());var value=add(graph,new ValueNode());var noise=add(graph,new DigitalNoiseNode());
        connect(graph,audio,"audio-out",noise,"frequency");connect(graph,value,"value-out",noise,"strength");
        var model=AutomaticRates.resolve(graph.snapshot()).findNode(noise).orElseThrow();
        var layout=new DigitalNoiseNode().layout(new NodeState(model.properties()));
        assertFalse(model.properties().containsKey("rate"));
        assertEquals(ProcessingRate.AUDIO,layout.rows().stream().filter(r->r.id().equals("frequency")).findFirst().orElseThrow().rate());
        assertEquals(ProcessingRate.CONTROL,layout.rows().stream().filter(r->r.id().equals("strength")).findFirst().orElseThrow().rate());
        assertTrue(AudioGraphEncoder.encode(graph.snapshot()).contains("@ aea ac"));
        graph.edit("Disconnect",e->{for(var wire:graph.snapshot().connections())e.removeConnection(wire.id());});
        assertTrue(AudioGraphEncoder.encode(graph.snapshot()).contains("@ aea cc"));
    }
    @Test void fixedAndFlexiblePortsResolveCentrallyWithoutCustomRateCode(){
        BasicNode node=new BasicNode(){
            public String textPrefix(){return "test.portRates";}
            public NativeProcessor processor(){return new MathNode().processor();}
            public void define(NodeBuilder n,NodeState s){
                n.addInput(MathNode.A,"Fixed").rates(PortRates.CONTROL);
                n.addInput(MathNode.B,"Flexible").rates(PortRates.AUDIO_CONTROL);
                n.addOutput(MathNode.OUT,"Out").rates(PortRates.AUDIO_CONTROL);
            }
        };
        var incoming=Map.of(MathNode.A,ProcessingRate.AUDIO,MathNode.B,ProcessingRate.CONTROL);
        var resolved=AutomaticRates.resolveProperties(node,Map.of(),incoming);
        assertEquals("control",resolved.get(ProcessingRate.EFFECTIVE).toJavaValue());
        var rows=node.layout(new NodeState(resolved)).rows();
        assertEquals('c',rows.get(0).hostInputRate());assertEquals('a',rows.get(1).hostInputRate());
        var audio=AutomaticRates.resolveProperties(node,Map.of(),Map.of(MathNode.B,ProcessingRate.AUDIO));
        assertEquals(ProcessingRate.AUDIO,node.layout(new NodeState(audio)).rows().get(2).rate());
        var manual=AutomaticRates.resolveProperties(node,Map.of("rate",GLNodeValue.of("control")),Map.of(MathNode.B,ProcessingRate.AUDIO));
        assertTrue(node.layout(new NodeState(manual)).rows().stream().allMatch(r->r.rate()==ProcessingRate.CONTROL));
    }
    @Test void numericSelectorsDoNotOfferTriggerAndShareTheirExplanation(){
        for(BasicNode node:List.of(new MathNode(),new MapRangeNode(),new MixNode(),new ClampNode(),new CurveNode(),new BitcrusherNode(),new ClockNode())){
            var row=node.layout(new NodeState(node.defaults())).rows().stream().filter(r->r.id().equals("rate")).findFirst().orElseThrow();
            assertEquals(ProcessingRate.NUMERIC,((Editor.ComboBox<?>)row.editor()).items());
            assertEquals(ProcessingRate.editorHint(),row.tooltip());
        }
    }
    @Test void autoKeepsEachInputRateAndMidiNumbersAreAudio(){
        var graph=new GLNodeGraph();var midi=add(graph,new MidiNoteNode());var value=add(graph,new ValueNode());var math=add(graph,new MathNode());
        connect(graph,midi,"frequency",math,"a");connect(graph,value,"value-out",math,"b");
        var resolved=AutomaticRates.resolve(graph.snapshot()).findNode(math).orElseThrow();
        var layout=new MathNode().layout(new NodeState(resolved.properties()));
        assertEquals(ProcessingRate.AUDIO,layout.rows().stream().filter(r->r.id().equals("a")).findFirst().orElseThrow().rate());
        assertEquals(ProcessingRate.CONTROL,layout.rows().stream().filter(r->r.id().equals("b")).findFirst().orElseThrow().rate());
        String encoded=AudioGraphEncoder.encode(graph.snapshot());assertTrue(encoded.contains("@ aaeaa e"));assertTrue(encoded.contains("@ a ac"));
    }
    @Test void oldCrossTypeWiresGetExplicitConvertersAndRemainConnected(){
        var graph=new GLNodeGraph();var clock=add(graph,new ClockNode());var math=add(graph,new MathNode());var value=add(graph,new ValueNode());
        connect(graph,clock,"value-out",math,"a");connect(graph,value,"value-out",clock,"sync");
        assertThrows(IllegalArgumentException.class,()->AudioGraphEncoder.encode(graph.snapshot()));
        var types=new GLNodeTypeRegistry();var registry=new NodeRegistry(types,new GLNodeCatalog());
        var migrated=registry.normalize(graph.snapshot());
        assertEquals(5,migrated.nodes().size());assertEquals(4,migrated.connections().size());
        assertTrue(migrated.nodes().stream().anyMatch(n->NodeRegistry.require(n.typeId()) instanceof TriggerPulseNode));
        assertTrue(migrated.nodes().stream().anyMatch(n->NodeRegistry.require(n.typeId()) instanceof EdgeDetectorNode));
        assertDoesNotThrow(()->AudioGraphEncoder.encode(migrated));
        assertEquals(migrated,registry.normalize(migrated));
        assertFalse(types.compatibleValueTypes(PortDataType.TRIGGER.id(),PortDataType.SIGNAL.id()));
    }
}
