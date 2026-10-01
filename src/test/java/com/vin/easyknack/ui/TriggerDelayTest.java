package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.*;
import com.vingame.glswing.node.model.*;
import java.nio.file.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class TriggerDelayTest {
    @TempDir Path temp;
    record Fixture(GLNodeGraph graph,GLNodeId seed,GLNodeId delay,GLNodeId reset){}
    Fixture fixture(int samples,boolean finite){
        var g=new GLNodeGraph();var seed=add(g,new TriggerInputNode());var reset=add(g,new TriggerInputNode());
        var merge=add(g,new TriggerMergeNode());var delay=add(g,new TriggerDelayNode(),Map.of("time",GLNodeValue.of(samples/48000.0)));
        connect(g,seed,"trigger-out",merge,"input-0");connect(g,merge,"trigger-out",delay,"trigger");connect(g,reset,"trigger-out",delay,"reset");
        if(finite){
            var counter=add(g,new TriggerCounterNode());var cmp=add(g,new ComparatorNode(),Map.of("b",GLNodeValue.of(2)));
            var filter=add(g,new TriggerFilterNode());var divider=add(g,new TriggerDividerNode(),Map.of("divisor",GLNodeValue.of(1)));
            connect(g,delay,"trigger-out",counter,"trigger");connect(g,counter,"index",cmp,"a");
            connect(g,counter,"trigger-out",filter,"trigger");connect(g,cmp,"value-out",filter,"condition");
            connect(g,filter,"trigger-out",divider,"trigger");connect(g,divider,"trigger-out",merge,"input-1");
        }else connect(g,delay,"trigger-out",merge,"input-1");
        var pulse=add(g,new TriggerPulseNode());var out=add(g,new OutputNode());
        connect(g,delay,"trigger-out",pulse,"trigger");connect(g,pulse,"audio-out",out,"audio-in");
        return new Fixture(g,seed,delay,reset);
    }
    @Test void feedbackValidationAndDynamicMerge(){
        for(boolean finite:new boolean[]{false,true}){
            var f=fixture(1,finite);assertTrue(SampleFeedback.diagnostics(f.graph.snapshot()).isEmpty());assertTrue(AudioGraphEncoder.encode(f.graph.snapshot()).contains("triggerdelay"));
        }
        var merge=new TriggerMergeNode();var values=new java.util.HashMap<>(merge.defaults());values.put("inputs",GLNodeValue.of(16));
        assertEquals(16,merge.layout(new NodeState(values)).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.INPUT).count());
        var f=fixture(1,false);var counter=add(f.graph,new TriggerCounterNode());connect(f.graph,f.delay,"trigger-out",counter,"trigger");connect(f.graph,counter,"index",f.delay,"time");
        assertTrue(SampleFeedback.diagnostics(f.graph.snapshot()).stream().anyMatch(d->d.code().equals("feedback.block-input")));
        assertFalse(new TriggerDelayNode().defaults().containsKey("start"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void schedulingFeedbackResetAndMerge()throws Exception{
        var code=new StringBuilder("""
        #include "AudioGraph.hpp"
        #include "nodes/TriggerDelayNode.hpp"
        #include "nodes/TriggerMergeNode.hpp"
        #include <cassert>
        using namespace easyknack;
        int main(){
            ValueEventBuffer in,rst,out;in.clear(8);rst.clear(8);out.clear(8);
            float samples[8]{},silence[8]{},time=10.f/48000;
            const float* inputs[]{nullptr,&time,nullptr};const ValueEventBuffer* events[]{&in,nullptr,&rst};ValueEventBuffer* outputs[]{&out};
            ProcessContext c{inputs,3,samples,8,48000,silence,8};c.sourceRates="ece";c.valueEventInputs=events;c.valueEventOutputs=outputs;
            TriggerDelayNode d;d.prepare(48000,8);in.push(0,1,ValueEventKind::trigger);d.process(c);assert(out.count==0);
            // Changing time does not move pending triggers. A new trigger can share their due sample.
            time=2.f/48000;in.clear(8);in.push(0,1,ValueEventKind::trigger);out.clear(8);d.process(c);
            assert(out.count==1&&out.events[0].sampleOffset==2);
            // A reset at the due sample suppresses an already pending trigger.
            time=10.f/48000;in.clear(8);in.push(0,1,ValueEventKind::trigger);out.clear(8);d.process(c);
            in.clear(8);rst.push(2,1,ValueEventKind::trigger);out.clear(8);d.process(c);assert(out.count==0);
            // Pending state survives graph publication.
            rst.clear(8);in.push(0,1,ValueEventKind::trigger);out.clear(8);d.process(c);
            TriggerDelayNode next;next.prepare(48000,8);next.adoptState(d);in.clear(8);out.clear(8);next.process(c);assert(out.count==1&&out.events[0].sampleOffset==2);
            // Zero externally supplied time still means one sample.
            time=0;in.push(0,1,ValueEventKind::trigger);in.push(1,1,ValueEventKind::trigger);out.clear(8);next.process(c);
            assert(out.count==2&&out.events[0].sampleOffset==1&&out.events[1].sampleOffset==2);
        """);
        for(int length:new int[]{1,4,17,256,480}){
            var f=fixture(length,false);
            code.append("{auto g=AudioGraph::parse(").append(quote(AudioGraphEncoder.encode(f.graph.snapshot()))).append(");float result[256]{};submitManualTrigger(").append(quote(f.seed+":trigger")).append(");++manualTriggerEpoch;int elapsed=0;");
            code.append("for(int frames:{256,13,256,7,256}){g.process(result,frames,0,nullptr);assert(g.lastFeedbackChunk==std::min(frames,").append(length).append("));for(int i=0;i<frames;i++){int t=elapsed+i;assert(result[i]==(t>0&&t%").append(length).append("==0?1.f:0.f));}elapsed+=frames;++manualTriggerEpoch;}");
            code.append("submitManualTrigger(").append(quote(f.reset+":trigger")).append(");++manualTriggerEpoch;g.process(result,256,0,nullptr);for(float v:result)assert(v==0);g.process(result,256,0,nullptr);for(float v:result)assert(v==0);}");
        }
        var finite=fixture(1,true);
        code.append("{auto g=AudioGraph::parse(").append(quote(AudioGraphEncoder.encode(finite.graph.snapshot()))).append(");float result[256]{};submitManualTrigger(").append(quote(finite.seed+":trigger")).append(");++manualTriggerEpoch;g.process(result,256,0,nullptr);for(int i=0;i<256;i++)assert(result[i]==(i>=1&&i<=3?1.f:0.f));}");
        code.append("}");var source=temp.resolve("delay.cpp");Files.writeString(source,code);
        RateConverterTest.compileAndRun(source,temp.resolve("delay.exe"),temp);
    }
}
