package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class HarmonizerTest {
    @Test void readoutUsesInstanceAndPluginPathsAndClearsOnStop()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{
            AudioAnalysis.clear();AudioAnalysis.aliases(Map.of("first/monitor",List.of("flat-one"),"second/monitor",List.of("flat-two"),"plugin",List.of("flat-plugin")));
            var first=new PitchReadoutEditor.Readout("monitor");first.bind("first");
            var second=new PitchReadoutEditor.Readout("monitor");second.bind("second");
            var plugin=new PitchReadoutEditor.Readout("node3");plugin.bind("plugin");
            AudioAnalysis.publish(Map.of("flat-one",new AudioAnalysis.Frame(660,.9,0,new float[]{440}),"flat-two",new AudioAnalysis.Frame(220,.5,0,new float[]{330}),"flat-plugin/node3",new AudioAnalysis.Frame(440,1,0,new float[]{880})));
            assertTrue(first.detectedText().contains("660.0 Hz"));assertTrue(first.targetText().contains("A4 / 440.0 Hz"));assertEquals("Confidence: 90 %",first.confidenceText());
            assertTrue(second.detectedText().contains("220.0 Hz"));assertTrue(plugin.targetText().contains("880.0 Hz"));
            AudioAnalysis.clear();assertEquals("Detected: --",first.detectedText());assertEquals("Target: --",plugin.targetText());AudioAnalysis.aliases(Map.of());
        });
    }
    @Test void hysteresisUnitsAndExportedReadoutAreRetained()throws Exception{
        var node=new ScaleQuantizerNode();assertEquals(.1,((Number)node.defaults().get("hysteresis").toJavaValue()).doubleValue());
        assertEquals(10,UnitFormat.SEMITONE.convert(.1,UnitFormat.CENT),1e-9);
        var doc=HarmonizerExample.create();var asset=doc.embeddedAssets().values().iterator().next();
        var mapping=asset.compositeDescriptor().portMappings().stream().filter(p->p.publicPort().id().equals("Hysteresis")).findFirst().orElseThrow();
        assertEquals(List.of(UnitFormat.SEMITONE,UnitFormat.CENT),GroupUnits.allowed(mapping,UnitFormat.SEMITONE));
        var group=doc.root().graph().nodes().stream().filter(n->n.typeId().equals("easyknack.group.harmonizer")).findFirst().orElseThrow();
        var exported=GroupSourceExporter.create(doc,group);
        assertTrue(exported.files().get("java/com/vin/easyknack/nodes/"+exported.className()+".java").contains("new PitchReadoutEditor(\"node"));
        assertTrue(exported.manualTargets().containsKey(asset.metadata().get(PitchReadoutEditor.GROUP_MONITOR).toJavaValue().toString()));
        var compiled=GroupBakeTest.definition(exported);
        assertTrue(compiled.layout(new NodeState(compiled.defaults())).rows().stream().anyMatch(r->r.editor() instanceof PitchReadoutEditor));
    }
    static String patch(GLNodeGraphDocument document)throws Exception{
        var result=new java.util.concurrent.atomic.AtomicReference<String>();
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Harmonizer",document);result.set(c.audioGraph());
        }});return result.get();
    }
    @Test void exampleRoundTripsAndExposesControlsWithCompatibleUnits()throws Exception{
        var doc=NodeDocuments.prepare(HarmonizerExample.create());
        var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(doc,bytes);
        var reloaded=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
        assertEquals(NodeDocuments.comparable(doc),NodeDocuments.comparable(reloaded));
        var asset=doc.embeddedAssets().values().iterator().next();
        assertEquals(List.of("Out","In","Target","Strength","Glide","Confidence","Mix","Hysteresis","Mode","Root","Scale"),asset.compositeDescriptor().portMappings().stream().map(p->p.publicPort().id()).toList());
        String command=patch(reloaded);for(String processor:List.of("pitchhold","scalequantizer","slew","positionreader"))assertTrue(command.contains(" "+processor+" "),command);
        assertEquals("audio",new SlewNode().defaults().get("rate").toJavaValue());
    }
    static boolean experimentalAudio(){return Boolean.getBoolean("easyknack.test.audio")&&Boolean.getBoolean("easyknack.test.experimental");}
    @Test @org.junit.jupiter.api.condition.EnabledIf("experimentalAudio")
    void exampleAndExportRunWithAnalysisAndStatefulNodes()throws Exception{
        var doc=HarmonizerExample.create();String command=patch(doc);
        var group=doc.root().graph().nodes().stream().filter(n->n.typeId().equals("easyknack.group.harmonizer")).findFirst().orElseThrow();
        var export=GroupSourceExporter.create(doc,group);
        var dir=Files.createTempDirectory(Path.of("target"),"harmonizer-export-").toAbsolutePath();
        Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var defs=new ArrayList<>(NodeRegistry.builtinDefinitions());defs.add(export.definition());Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(defs));
        var definition=export.definition();String payload="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(definition.defaults()),p->-1);
        String cpp="""
            #include "AudioGraph.hpp"
            #include "%s.hpp"
            #include <cassert>
            #include <thread>
            using namespace easyknack;
            int main(){
                for(double sourceFrequency : {660.,1600.,3000.,4400.}){
                auto graph=AudioGraph::parse(%s);
                for(auto& node:graph.nodes)if(auto* osc=dynamic_cast<OscillatorNode*>(node.processor.get()))osc->frequency=sourceFrequency;
                %s baked;std::istringstream params(%s);baked.configure(params);baked.bindIdentity("baked-harmonizer");baked.prepare(48000,256);
                std::array<float,256> input{},out{},flat{},silence{};const float* inputs[]{input.data(),nullptr,nullptr,nullptr,nullptr,nullptr,nullptr};
                char rates[]{'a','c','c','c','c','c','c'};ProcessContext c{inputs,7,out.data(),256,48000,silence.data(),256};c.sourceRates=rates;
                double flatPower=0,bakedPower=0;int count=0,flatCrossings=0,bakedCrossings=0;float previousFlat=0,previousBaked=0;
                for(int block=0;block<800;block++){
                    for(int i=0;i<256;i++)input[i]=std::sin(6.283185307179586*sourceFrequency*(block*256+i)/48000);
                    graph.process(flat.data(),256);baked.process(c);
                    for(int i=0;i<256;i++){assert(std::isfinite(out[i])&&std::abs(out[i])<=1.01);assert(std::isfinite(flat[i]));if(block>400){flatPower+=flat[i]*flat[i];bakedPower+=out[i]*out[i];count++;flatCrossings+=previousFlat<=0&&flat[i]>0;bakedCrossings+=previousBaked<=0&&out[i]>0;}previousFlat=flat[i];previousBaked=out[i];}
                    std::this_thread::sleep_for(std::chrono::milliseconds(1));
                }
                assert(flatPower/count>.01&&bakedPower/count>.01);
                assert(std::abs(flatCrossings*48000.0/count-440)<10);assert(std::abs(bakedCrossings*48000.0/count-440)<10);
                bool found=false;for(auto& node:graph.nodes)if(auto* hold=dynamic_cast<PitchHoldNode*>(node.processor.get())){assert(hold->accepted&&std::abs(hold->held-sourceFrequency)<sourceFrequency*.01);found=true;}assert(found);
                bool snapshot=false;for(auto& node:baked.runtime->graph.nodes)if(auto* monitor=dynamic_cast<PitchMonitorNode*>(node.processor.get())){
                    AnalysisFrame frame;assert(monitor->mailbox&&monitor->mailbox->latest(frame));assert(frame.valueCount==1&&frame.values[0]==440);snapshot=true;
                }assert(snapshot);
                }
            }
            """.formatted(export.className(),AdvancedSourceExportTest.quote(command),export.className(),AdvancedSourceExportTest.quote(payload));
        var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
        try(var host=new AudioHostClient()){host.start(AudioHostClient.defaultExecutable(),true);assertEquals("OK",host.command(command));assertEquals("OK",host.command("PLAY"));assertTrue(host.command("PING").startsWith("OK "));assertEquals("OK",host.command("STOP"));}
    }
}
