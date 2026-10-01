package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.*;
import com.vingame.glswing.node.model.*;

class ReaderTriggerTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void positionFollowerGraphIsAcceptedByHost()throws Exception{
        var graph=new GLNodeGraph();var oscillator=add(graph,new OscillatorNode());var buffer=add(graph,new HistoryBufferNode());
        var clock=add(graph,new HistoryReaderNode());var follower=add(graph,new PositionReaderNode(),Map.of("offset",GLNodeValue.of(.5)));var out=add(graph,new OutputNode());
        connect(graph,oscillator,"audio-out",buffer,"audio-in");connect(graph,buffer,"history",clock,"history");connect(graph,buffer,"history",follower,"history");
        connect(graph,clock,"read-position",follower,"position");connect(graph,follower,"audio-out",out,"audio-in");
        try(var host=new AudioHostClient()){
            host.start(AudioHostClient.defaultExecutable(),true);assertEquals("OK",host.command(AudioGraphEncoder.encode(graph.snapshot())));
            assertEquals("OK",host.command("PLAY"));assertTrue(host.command("PING").startsWith("OK "));assertEquals("OK",host.command("STOP"));
        }
    }
    @Test void obsoleteCrossingWiresAreRemovedWithoutChangingAudioConnections(){
        var graph=new GLNodeGraph();var first=add(graph,new HistoryReaderNode());var second=add(graph,new HistoryReaderNode());var out=add(graph,new OutputNode());
        connect(graph,first,"write-head-crossing",second,"reset");connect(graph,second,"audio-out",out,"audio-in");
        var asset=new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,graph.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty());
        var document=new com.vingame.glswing.node.document.GLNodeGraphDocument(asset,Map.of(),List.of());
        var migrated=ReaderPositionMigration.migrate(document);
        assertEquals(2,document.root().graph().connections().size());
        assertEquals(1,migrated.root().graph().connections().size());
        assertEquals("audio-out",migrated.root().graph().connections().get(0).output().portId());
        assertEquals(graph.snapshot().nodes(),migrated.root().graph().nodes());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void bakedTriggerButtonsKeepTheirIdentityAndDoNotTriggerOtherInstances() throws Exception {
        var inner=new GLNodeGraph();var trigger=add(inner,new TriggerInputNode());
        var descriptor=new com.vingame.glswing.node.composite.GLCompositeNodeDescriptor("test.trigger","test.trigger.asset",1,List.of(output("Trigger",SignalCategory.TRIGGER,trigger,"trigger-out")));
        var asset=new com.vingame.glswing.node.document.GLNodeGraphAsset(descriptor.assetId(),1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var root=new GLNodeGraph();var group=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(group));
        var doc=new com.vingame.glswing.node.document.GLNodeGraphDocument(new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,root.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
        var export=GroupSourceExporter.create(doc,group,true);assertEquals(List.of("node0"),export.manualTargets().get(trigger.toString()));
        var dir=Files.createTempDirectory(Path.of("target"),"baked-trigger-").toAbsolutePath();var d=export.definition();
        Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(d);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->-1);
        String code="#include \"AudioGraph.hpp\"\n#include \""+export.className()+".hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){"+export.className()+" a,b;std::istringstream ca("+quote(payload)+"),cb("+quote(payload)+");a.configure(ca);b.configure(cb);a.bindIdentity(\"a\");b.bindIdentity(\"b\");a.prepare(48000,256);b.prepare(48000,256);std::array<float,256> audio{},silence{};ValueEventBuffer events;events.clear(256);ValueEventBuffer* outputs[]{&events};ProcessContext c{nullptr,0,audio.data(),256,48000,silence.data(),256};c.valueEventOutputs=outputs;submitManualTrigger(\"a/node0:trigger\");submitManualTrigger(\"a/node0:trigger\");++manualTriggerEpoch;a.process(c);assert(events.count==1&&events.events[0].sampleOffset==0);events.clear(256);b.process(c);assert(events.count==0);}";
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
    void repeatedButtonClicksAreTransientAndNeverWriteProperties() throws Exception {
        javax.swing.SwingUtilities.invokeAndWait(()->{
            var received=new ArrayList<String>();ManualTriggers.aliases(Map.of("source",List.of("flattened")));ManualTriggers.receiver(received::add);
            try{
                var editor=new Editor.Trigger().create("Reset",value->fail("Trigger must not write model properties"));
                var button=(Editor.TriggerButton)editor.component();button.bind("source","reset");
                button.doClick();button.doClick();editor.update().accept(GLNodeValue.of(false));
                assertEquals(List.of("flattened:reset","flattened:reset"),received);
            }finally{ManualTriggers.receiver(id->{});ManualTriggers.aliases(Map.of());}
        });
    }
    @Test void readerExposesAudioRatePositionInsteadOfCrossing(){
        var reader=new HistoryReaderNode();var layout=reader.layout(new NodeState(reader.defaults()));
        assertEquals("a",AudioGraphEncoder.outputRateMetadata(layout));
        assertEquals(List.of("audio-out","distance","read-position"),layout.rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).map(NodeBuilder.Row::id).toList());
        assertTrue(layout.rows().stream().anyMatch(r->r.key().equals(HistoryReaderNode.RESET)&&r.editor() instanceof Editor.Trigger&&r.rate()==ProcessingRate.EVENT));
        assertTrue(NodeRegistry.find("easyknack.input.trigger").isPresent());
    }
    @Test void positionReaderHasOnlyPositionControlsAndConverterProducesTriggers(){
        var reader=new PositionReaderNode();var layout=reader.layout(new NodeState(reader.defaults()));
        assertEquals(List.of("history","position","offset"),layout.rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.INPUT).map(NodeBuilder.Row::id).toList());
        assertEquals("a",AudioGraphEncoder.outputRateMetadata(layout));
        var converter=new EdgeDetectorNode();var triggerLayout=converter.layout(new NodeState(converter.defaults()));
        assertTrue(triggerLayout.rows().stream().anyMatch(r->r.key().equals(EdgeDetectorNode.OUT)&&r.rate()==ProcessingRate.EVENT));
        assertTrue(NodeRegistry.find("easyknack.resource.positionreader").isPresent());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void sharedManualTriggerResetsBothReadersWithoutAParameterUpdate() throws Exception {
        var graph=new GLNodeGraph();var context=add(graph,new ContextInputNode());
        var buffer=add(graph,new HistoryBufferNode(),Map.of("seconds",GLNodeValue.of(512./48000)));
        var trigger=add(graph,new TriggerInputNode());
        var first=add(graph,new HistoryReaderNode(),Map.of("speed",GLNodeValue.of(0)));
        var second=add(graph,new HistoryReaderNode(),Map.of("speed",GLNodeValue.of(0),"offset",GLNodeValue.of(.5)));
        var mix=add(graph,new MixNode());var out=add(graph,new OutputNode());
        connect(graph,context,"channel",buffer,"audio-in");
        for(var reader:List.of(first,second)){connect(graph,buffer,"history",reader,"history");connect(graph,trigger,"trigger-out",reader,"reset");}
        connect(graph,first,"audio-out",mix,"a");connect(graph,second,"audio-out",mix,"b");connect(graph,mix,"audio-out",out,"audio-in");
        String patch=AudioGraphEncoder.encode(graph.snapshot());
        var dir=Files.createTempDirectory(Path.of("target"),"reader-trigger-").toAbsolutePath();
        String code="#include \"AudioGraph.hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){auto graph=AudioGraph::parse("+quote(patch)+");std::array<float,256> audio{};auto block=[&]{++manualTriggerEpoch;graph.process(audio.data(),256,0,nullptr);graph.process(audio.data(),256,1,nullptr);};block();submitManualTrigger("+quote(trigger+":trigger")+");submitManualTrigger("+quote(trigger+":trigger")+");block();int readers=0;for(auto& node:graph.nodes)if(auto* reader=dynamic_cast<HistoryReaderNode*>(node.processor.get())){assert(reader->cursor==256);auto* right=static_cast<HistoryReaderNode*>(node.rightProcessor.get());assert(right->cursor==256);++readers;}assert(readers==2);block();for(auto& node:graph.nodes)if(auto* reader=dynamic_cast<HistoryReaderNode*>(node.processor.get()))assert(reader->cursor==256);}";
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
        try(var host=new AudioHostClient()){
            host.start(AudioHostClient.defaultExecutable(),true);assertEquals("OK",host.command(patch));assertEquals("OK",host.command("PLAY"));
            assertEquals("OK",host.command("TRIGGER "+trigger+":trigger"));assertEquals("OK",host.command("TRIGGER "+trigger+":trigger"));
            assertTrue(host.command("PING").startsWith("OK "));assertEquals("OK",host.command("STOP"));
        }
    }
}
