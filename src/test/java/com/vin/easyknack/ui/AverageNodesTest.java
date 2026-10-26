package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.*;
import com.vingame.glswing.node.model.*;
import java.util.Map;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

class AverageNodesTest {
    @Test void registeredContractsExposeTriggerAndHeldResults(){
        for(var node:new BasicNode[]{new ValueAverageNode(),new AnalysisAverageNode()}){
            assertTrue(NodeRegistry.find(node.registration().id()).isPresent());
            var layout=node.layout(new NodeState(node.defaults()));
            assertEquals(node instanceof ValueAverageNode?6:3,layout.rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).count());
            assertTrue(layout.rows().stream().anyMatch(r->r.id().equals("finished")&&r.dataType()==PortDataType.TRIGGER));
            assertFalse(node.encode(new NodeState(node.defaults()),p->-1).isBlank());
            var state=new NodeState(node.defaults());var mute=new MuteRouting();var insert=new InsertionRouting();
            node.mute(mute,state);node.insert(insert,state);
            assertTrue(node.isMuteable());assertEquals(1,mute.bypasses().size());assertNotNull(insert.ports());
            assertEquals(insert.ports().inputPortId(),mute.bypasses().get(0).inputPortId());
            assertEquals("out",insert.ports().outputPortId());
        }
        var node=new SpectralFeaturesNode();
        assertTrue(node.layout(new NodeState(node.defaults())).rows().stream().anyMatch(r->r.id().equals("spectrum")&&r.dataType()==PortDataType.ANALYSIS));
        var value=new ValueAverageNode();
        assertEquals(java.util.List.of("out","count","finished","deviation","minimum","maximum"),
            value.layout(new NodeState(value.defaults())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).map(NodeBuilder.Row::id).toList());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void finiteMeasurementTimingPowerCorrelationAndHold()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"averages-native-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/AverageNodesTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void synchronousAndAsynchronousFftReachTheViewerThroughTheHost()throws Exception{
        for(boolean sync:new boolean[]{true,false}){
            var graph=new GLNodeGraph();var source=add(graph,new OscillatorNode());
            var fft=add(graph,new SpectrumNode(),Map.of("size",GLNodeValue.of("256"),"synchronous",GLNodeValue.of(sync)));
            var average=add(graph,new AnalysisAverageNode(),Map.of("mode",GLNodeValue.of("measurements"),"measurements",GLNodeValue.of(4.)));
            var viewer=add(graph,new SpectrumViewerNode());
            connect(graph,source,"audio-out",fft,"audio-in");connect(graph,fft,"analysis",average,"analysis");connect(graph,average,"out",viewer,"analysis");
            try(var host=new AudioHostClient()){
                host.start(Path.of(System.getProperty("easyknack.test.host",AudioHostClient.defaultExecutable().toString())),true);
                assertEquals("OK",host.command(AudioGraphEncoder.encode(graph.snapshot())));host.command("PLAY");
                Thread.sleep(150);host.command("TRIGGER "+average+":start");
                AudioAnalysis.Frame frame=null;long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
                while(System.nanoTime()<deadline){
                    Thread.sleep(30);frame=AudioAnalysis.parse(host.command("ANALYSIS")).get(viewer.toString());
                    if(frame!=null&&frame.values().length==129)break;
                }
                assertNotNull(frame,"No averaged FFT: synchronous="+sync);assertEquals(129,frame.values().length);
                boolean nonzero=false;for(float value:frame.values())nonzero|=value>0;
                assertTrue(nonzero,"Averaged FFT must contain the oscillator signal");host.command("STOP");
            }
        }
    }
}
