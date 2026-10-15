package com.vin.easyknack.ui;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;
class WaveformTest {
    @Test void waveformsAreResourcesWithPersistentCurves(){
        var d=new WaveformNode();var state=new NodeState(d.defaults());var layout=d.layout(state);
        var port=layout.rows().get(0);assertEquals(PortDataType.WAVEFORM,port.dataType());assertEquals(ProcessingRate.CONTROL,port.rate());
        var curve=CurveData.decode(state.text(WaveformNode.CURVE,""));assertEquals(curve.evaluate(0),curve.evaluate(1));
        assertEquals(CurveData.encode(curve),CurveData.encode(CurveData.decode(CurveData.encode(curve))));
        assertTrue(NodeRegistry.find(d.registration().id()).isPresent());assertTrue(NodeRegistry.find(new SpectrumWaveformNode().registration().id()).isPresent());
        for(var osc:List.of(new OscillatorNode(),new AdditiveOscillatorNode()))assertEquals(PortDataType.WAVEFORM,osc.layout(new NodeState(osc.defaults())).rows().stream().filter(r->r.id().equals("waveform")).findFirst().orElseThrow().dataType());
    }
    @Test void captureSnapshotPersistsAndDoesNotOverwriteUndo()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{
            var definition=new SpectrumWaveformNode();var id=GLNodeId.random();var writes=new HashMap<String,GLNodeValue>();
            var model=new GLGraphNode(id,definition.registration().id(),definition.defaults());
            var view=new WaveformCaptureView(definition,new NodeViewContext((node,key,value)->writes.put(key.value(),value),new com.vingame.glswing.history.GLHistoryManager()),model);
            float[] samples=new float[2048];for(int i=0;i<samples.length;i++)samples[i]=(float)Math.sin(2*Math.PI*i/samples.length);
            var frame=new com.vin.easyknack.audio.AudioAnalysis.Frame(0,0,1,samples);
            com.vin.easyknack.audio.AudioAnalysis.publish(Map.of(id.toString(),frame));
            String captured=(String)writes.get("saved").toJavaValue();assertEquals(16384,captured.length());
            var restored=new HashMap<>(definition.defaults());restored.put("saved",GLNodeValue.of(captured));
            assertTrue(definition.encode(new NodeState(restored),port->-1).endsWith(captured));
            view.update(model);writes.clear();com.vin.easyknack.audio.AudioAnalysis.publish(Map.of(id.toString(),frame));assertTrue(writes.isEmpty());
            com.vin.easyknack.audio.AudioAnalysis.clear();
        });
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeTablesCaptureAndBothOscillators()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"waveform-test-").toAbsolutePath();
        var waveform=new WaveformNode();var osc=new OscillatorNode();
        String payload="GRAPH 3 w waveform !"+NativeContractGenerator.fingerprint(waveform)+" "+waveform.encode(new NodeState(waveform.defaults()),id->-1)+" @ c - o osc !"+NativeContractGenerator.fingerprint(osc)+" "+osc.encode(new NodeState(osc.defaults()),id->id.equals(OscillatorNode.WAVEFORM)?0:-1)+" @ a acae sink out 1 0 @ a a";
        var capture=new SpectrumWaveformNode();String captureGraph="GRAPH 1 c spectrumwaveform !"+NativeContractGenerator.fingerprint(capture)+" "+capture.encode(new NodeState(capture.defaults()),id->-1)+" @ c cce";
        String integration="auto captureGraph=AudioGraph::parse("+AdvancedSourceExportTest.quote(captureGraph)+");captureGraph.process(output.data(),256);auto graph=AudioGraph::parse("+AdvancedSourceExportTest.quote(payload)+");graph.process(output.data(),256);double energy=0;for(float v:output){assert(std::isfinite(v));energy+=v*v;}assert(energy>50);";
        var source=dir.resolve("test.cpp");Files.writeString(source,Files.readString(Path.of("native/audio-host/tests/WaveformTest.cpp")).replace("// GRAPH_TEST",integration));
        RateConverterTest.run(List.of("powershell.exe","-NoProfile","-ExecutionPolicy","Bypass","-File",Path.of("scripts/generate-native-nodes.ps1").toAbsolutePath().toString(),"-OutputDirectory",dir.toString()));
        var executable=dir.resolve("test.exe");
        RateConverterTest.run(List.of(System.getProperty("easyknack.test.cxx"),"-std=c++17","-O0","-static","-I",dir.toString(),"-I",Path.of("native/audio-host/src").toAbsolutePath().toString(),"-I",Path.of("target/native/generated").toAbsolutePath().toString(),source.toString(),"-o",executable.toString()));
        RateConverterTest.run(List.of(executable.toString()));
    }
}
