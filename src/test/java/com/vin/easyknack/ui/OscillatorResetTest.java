package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;

class OscillatorResetTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void instanceBakeKeepsInnerManualResetAddressable()throws Exception{
        var asset=MultipleGroups.create();
        var osc=asset.graph().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof OscillatorNode).findFirst().orElseThrow();
        var root=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(instance));
        var doc=new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(NodeDocuments.prepare(doc));
        var flat=SpectralGroups.flatten(GroupCompiler.prepare(doc.root(),registry),new GLCompositeExpansionLimits(16,256,512,10000));
        var compiled=flat.graph().orElseThrow().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof MultipleGraphNode).findFirst().orElseThrow();
        var aliases=new HashMap<String,List<String>>();CompiledManualTargets.addAliases(aliases,compiled);
        assertTrue(aliases.containsKey(osc.id().toString()));
        var export=GroupSourceExporter.create(doc,instance,true);var d=export.definition();
        assertTrue(export.manualTargets().containsKey(osc.id().toString()));
        String target=export.manualTargets().get(osc.id().toString()).get(0);
        var dir=Files.createTempDirectory(Path.of("target"),"instance-reset-").toAbsolutePath();
        Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(d);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->-1);
        String code="""
            #include "AudioGraph.hpp"
            #include "%s.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                %s bank,fresh;
                for(auto* n:{&bank,&fresh}){std::istringstream p(%s);n->configure(p);n->prepare(48000,256);}
                bank.bindIdentity("bank");
                float out[256]{},reference[256]{},silence[256]{};
                ProcessContext c{nullptr,0,out,256,48000,silence,256};
                for(int block=0;block<4;block++){++manualTriggerEpoch;bank.process(c);}
                submitManualTrigger(%s);++manualTriggerEpoch;bank.process(c);
                c.output=reference;fresh.process(c);
                for(int i=0;i<256;i++)assert(std::abs(out[i]-reference[i])<1e-5);
                assert(std::abs(out[0])<1e-6);
            }
            """.formatted(export.className(),export.className(),AdvancedSourceExportTest.quote(payload),AdvancedSourceExportTest.quote("bank/"+target+":reset"));
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void exportedResetPortPreservesAdjacentTriggerEvents()throws Exception{
        for(var oscillator:List.of(new OscillatorNode(),new AdditiveOscillatorNode())){
            var inner=new GLNodeGraph();
            var id=AdvancedSourceExportTest.add(inner,oscillator,Map.of("frequency",GLNodeValue.of(480.),"phase",GLNodeValue.of(90.)));
            var descriptor=new GLCompositeNodeDescriptor("test.reset","reset-group",1,List.of(
                AdvancedSourceExportTest.input("Reset",SignalCategory.TRIGGER,id,"reset"),
                AdvancedSourceExportTest.output("Out",SignalCategory.AUDIO,id,"audio-out")));
            var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
            var root=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(instance));
            var doc=new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
            var export=GroupSourceExporter.create(doc,instance,true);var d=export.definition();
            var dir=Files.createTempDirectory(Path.of("target"),"reset-export-").toAbsolutePath();
            Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
            var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(d);
            Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
            String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->0);
            String code="""
                #include "AudioGraph.hpp"
                #include "%s.hpp"
                #include <cassert>
                using namespace easyknack;
                int main(){
                    %s node;std::istringstream params(%s);node.configure(params);node.prepare(48000,256);
                    float input[256]{},out[256]{},silence[256]{};const float* inputs[]{input};char rates[]{'e'};
                    ValueEventBuffer events;events.clear(256);events.push(7,1,ValueEventKind::trigger);events.push(8,1,ValueEventKind::trigger);
                    const ValueEventBuffer* streams[]{&events};events.expand(input,256);
                    ProcessContext c{inputs,1,out,256,48000,silence,256};c.sourceRates=rates;c.valueEventInputs=streams;
                    node.process(c);
                    assert(std::abs(out[7]-1)<1e-6&&std::abs(out[8]-1)<1e-6);
                    assert(out[9]<out[8]&&out[10]<out[9]);
                }
                """.formatted(export.className(),export.className(),AdvancedSourceExportTest.quote(payload));
            var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
        }
    }
    @Test void bothOscillatorsExposeAnUnconnectedTriggerButton(){
        for(var d:List.of(new OscillatorNode(),new AdditiveOscillatorNode())){
            var row=d.layout(new NodeState(d.defaults())).rows().stream().filter(r->r.id().equals("reset")).findFirst().orElseThrow();
            assertInstanceOf(Editor.Trigger.class,row.editor());assertEquals('e',row.hostInputRate());
            assertEquals("reset",d.processor().inputs().get(d.processor().inputs().size()-1).value());
        }
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void resetIsSampleAccurateAcrossAdjacentEventsChunksStereoAndStateTransfer()throws Exception{
        var code=new StringBuilder("#include \"AudioGraph.hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){\n");
        for(var d:List.of(new OscillatorNode(),new AdditiveOscillatorNode())){
            String type=d instanceof OscillatorNode?"OscillatorNode":"AdditiveOscillatorNode";
            int reset=d.processor().inputs().size()-1;
            var values=new HashMap<>(d.defaults());values.put("frequency",GLNodeValue.of(480.));values.put("phase",GLNodeValue.of(90.));
            if(d instanceof AdditiveOscillatorNode)values.put("harmonics",GLNodeValue.of(3));
            String body=d.encode(new NodeState(values),p->-1),payload="!"+NativeContractGenerator.fingerprint(d)+" "+body;
            String legacy=(d instanceof OscillatorNode?"!e5d6fa7b1b0ad545":"!51e01ae3a60f12dc")+" "+body.substring(0,body.lastIndexOf(' '));
            code.append("""
                {
                    %s block,chunk,old;
                    for(auto* n:{&block,&chunk}){std::istringstream p(%s);n->configure(p);n->prepare(48000,256);}
                    std::istringstream legacy(%s);old.configure(legacy);old.prepare(48000,256);
                    float a[256]{},b[256]{},silence[256]{};
                    const float* inputs[7]{};const ValueEventBuffer* streams[7]{};
                    ValueEventBuffer events;events.clear(256);
                    for(unsigned i:{7u,8u,255u})events.push(i,1,ValueEventKind::trigger);
                    streams[%d]=&events;
                    ProcessContext c{inputs,7,a,256,48000,silence,256};c.valueEventInputs=streams;
                    block.process(c);
                    const float initial=a[0];assert(std::abs(initial)>.1);
                    assert(std::abs(a[7]-initial)<1e-6&&std::abs(a[8]-initial)<1e-6&&std::abs(a[255]-initial)<1e-6);
                    for(int i=0;i<256;i++){c.output=b+i;c.bufferSize=1;c.sampleOffset=i;c.samplewise=true;chunk.processSample(c);}
                    for(int i=0;i<256;i++)assert(std::abs(a[i]-b[i])<1e-6);
                    c={inputs,7,a,256,48000,silence,256};
                    block.bindIdentity("reset-test-%s");chunk.shareChannelState(block);
                    submitManualTrigger("reset-test-%s:reset");++manualTriggerEpoch;
                    block.process(c);c.output=b;chunk.process(c);
                    for(int i=0;i<256;i++)assert(std::abs(a[i]-b[i])<1e-6);
                    assert(std::abs(a[0]-initial)<1e-6);
                    %s replacement;std::istringstream p(%s);replacement.configure(p);replacement.prepare(48000,256);
                    replacement.bindIdentity("reset-test-%s");replacement.adoptState(block);
                    ++manualTriggerEpoch;c.output=a;block.process(c);c.output=b;replacement.process(c);
                    for(int i=0;i<256;i++)assert(std::abs(a[i]-b[i])<1e-6);
                }
                """.formatted(type,AdvancedSourceExportTest.quote(payload),AdvancedSourceExportTest.quote(legacy),reset,type,type,type,AdvancedSourceExportTest.quote(payload),type));
        }
        code.append("}");var dir=Files.createTempDirectory(Path.of("target"),"oscillator-reset-").toAbsolutePath();
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
