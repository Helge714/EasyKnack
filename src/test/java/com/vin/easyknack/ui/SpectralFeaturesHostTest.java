package com.vin.easyknack.ui;

import com.vin.easyknack.audio.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
class SpectralFeaturesHostTest {
    @Test void liveHostTransportsAllEightDescriptorsWithoutAnAudioSink()throws Exception{
        BasicNode[] definitions={new OscillatorNode(),new SpectrumNode(),new SpectralFeaturesNode(),new ValueViewerNode()};
        var ids=java.util.stream.IntStream.range(0,4).mapToObj(i->GLNodeId.random()).toList();var graph=new GLNodeGraph();
        graph.edit("Descriptors",edit->{
            for(int i=0;i<4;i++){
                var values=new HashMap<>(definitions[i].defaults());
                if(i==3)values.put("inputs",GLNodeValue.of(8.));
                edit.addNode(new GLGraphNode(ids.get(i),definitions[i].registration().id(),values));
            }
            edit.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(ids.get(0),"audio-out"),new GLNodeEndpoint(ids.get(1),"audio-in")));
            edit.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(ids.get(1),"analysis"),new GLNodeEndpoint(ids.get(2),"spectrum")));
            for(int p=0;p<8;p++)edit.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(ids.get(2),SpectralFeaturesNode.OUTPUTS.get(p)),new GLNodeEndpoint(ids.get(3),ValueViewerNode.input(p).value())));
        });
        try(var client=new AudioHostClient()){
            client.start(AudioHostClient.defaultExecutable(),true);
            assertEquals("OK",client.command(AudioGraphEncoder.encode(graph.snapshot())));assertEquals("OK",client.command("PLAY"));
            AudioAnalysis.Frame packet=null;
            for(int attempt=0;attempt<30;attempt++){
                Thread.sleep(30);packet=AudioAnalysis.parse(client.command("ANALYSIS")).get(ids.get(3).toString());
                if(packet!=null&&packet.values().length>32){int buckets=(int)packet.values()[2];if(buckets>0&&packet.values()[8+(buckets-1)*24+2]>100)break;}
            }
            assertNotNull(packet);assertEquals(8,packet.frequency());
            var history=new ValueHistory();history.configure(8,10);history.accept(packet);var snapshot=history.snapshot(null,null,false);
            assertTrue(snapshot.size()>0);int last=snapshot.size()-1;
            assertEquals(440,snapshot.value()[0][last],10);assertTrue(snapshot.value()[1][last]>0);
            assertTrue(snapshot.value()[5][last]>=400&&snapshot.value()[5][last]<=500);
            assertTrue(snapshot.value()[7][last]<.1);
            assertEquals("OK",client.command("STOP"));
        }
    }
}
