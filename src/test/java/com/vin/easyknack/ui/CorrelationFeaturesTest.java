package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

class CorrelationFeaturesTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void fftCorrelationDescriptorsReachRecorderThroughTheHost()throws Exception{
        var graph=new GLNodeGraph();
        var oscillator=AdvancedSourceExportTest.add(graph,new OscillatorNode(),Map.of("frequency",GLNodeValue.of(440.)));
        var fft=AdvancedSourceExportTest.add(graph,new SpectrumNode(),Map.of("size",GLNodeValue.of("8192"),"window",GLNodeValue.of("rectangular"),"padding",GLNodeValue.of("double"),"remove-dc",GLNodeValue.of(true)));
        var correlation=AdvancedSourceExportTest.add(graph,new SpectrumCorrelationNode(),Map.of("normalization",GLNodeValue.of("unbiased")));
        var features=AdvancedSourceExportTest.add(graph,new CorrelationFeaturesNode());
        var recorder=AdvancedSourceExportTest.add(graph,new MeasurementRecorderNode(),Map.of("inputs",GLNodeValue.of(5.)));
        AdvancedSourceExportTest.connect(graph,oscillator,"audio-out",fft,"audio-in");AdvancedSourceExportTest.connect(graph,fft,"analysis",correlation,"spectrum");AdvancedSourceExportTest.connect(graph,correlation,"analysis",features,"analysis");
        var ports=List.of("periodicity","period","frequency","hnr","valid");
        for(int i=0;i<ports.size();i++)AdvancedSourceExportTest.connect(graph,features,ports.get(i),recorder,"value-"+i);
        try(var host=new com.vin.easyknack.audio.AudioHostClient()){
            host.start(Path.of(System.getProperty("easyknack.test.host",com.vin.easyknack.audio.AudioHostClient.defaultExecutable().toString())),true);
            host.command(com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot()));host.command("PLAY");
            GLNodeValue table=MeasurementTable.empty();List<GLNodeValue> row=List.of();long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
            while(System.nanoTime()<deadline){
                host.command("TRIGGER "+recorder+":record");Thread.sleep(50);
                table=MeasurementTable.merge(table,com.vin.easyknack.audio.AudioAnalysis.parse(host.command("ANALYSIS")).get(recorder.toString()));
                var rows=MeasurementTable.rows(table);if(rows.isEmpty())continue;
                row=((GLNodeValue.ListValue)rows.get(rows.size()-1)).values();if(((Number)row.get(6).toJavaValue()).doubleValue()>.5)break;
            }
            assertEquals(7,row.size());assertEquals(1.,((Number)row.get(6).toJavaValue()).doubleValue());
            assertEquals(440.,((Number)row.get(4).toJavaValue()).doubleValue(),2.);
            assertTrue(((Number)row.get(2).toJavaValue()).doubleValue()>.98);host.command("STOP");
        }
    }
    @Test void descriptorsUseExistingAnalysisResourcesAndInspectorSettings(){
        var node=new CorrelationFeaturesNode();var layout=node.layout(new NodeState(node.defaults()));
        assertTrue(NodeRegistry.find(node.registration().id()).isPresent());
        var outputs=layout.rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).toList();
        assertEquals(List.of("periodicity","period","frequency","hnr","valid"),outputs.stream().map(NodeBuilder.Row::id).toList());
        assertTrue(outputs.stream().allMatch(r->r.rate()==ProcessingRate.CONTROL));
        assertTrue(layout.rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.EDITOR).allMatch(NodeBuilder.Row::isInspectorOnly));
        assertTrue(layout.rows().stream().anyMatch(r->r.id().equals("analysis")&&r.dataType()==PortDataType.ANALYSIS));
        var graph=new GLNodeGraph();var correlation=new SpectrumCorrelationNode();var source=GLNodeId.random();var id=GLNodeId.random();
        graph.edit("Features",e->{e.addNode(new GLGraphNode(source,correlation.registration().id(),correlation.defaults()));e.addNode(new GLGraphNode(id,node.registration().id(),node.defaults()));e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(source,"analysis"),new GLNodeEndpoint(id,"analysis")));});
        assertTrue(com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot()).contains("correlationfeatures"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void peaksSilenceHnrFrameCadenceAndAverageMetadata()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"correlation-features-native-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/CorrelationFeaturesTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
