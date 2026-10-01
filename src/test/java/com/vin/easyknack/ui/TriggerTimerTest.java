package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioGraphEncoder;
import com.vingame.glswing.node.model.*;
import java.nio.file.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class TriggerTimerTest {
    @TempDir Path temp;
    String patch(){
        var g=new GLNodeGraph();var clock=add(g,new ClockNode(),Map.of("frequency",GLNodeValue.of(12000)));
        var timer=add(g,new TriggerTimerNode());var out=add(g,new OutputNode());
        connect(g,clock,"value-out",timer,"trigger");connect(g,timer,"interval",out,"audio-in");
        return AudioGraphEncoder.encode(g.snapshot());
    }
    @Test void twoAudioOutputsAndConnectableSmoothing(){
        var node=new TriggerTimerNode();var rows=node.layout(new NodeState(node.defaults())).rows();
        assertEquals(2,rows.stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT&&r.rate()==ProcessingRate.AUDIO).count());
        assertTrue(rows.stream().anyMatch(r->r.key().equals(TriggerTimerNode.SMOOTHING)&&r.kind()==NodeBuilder.RowKind.INPUT));
        assertEquals("second",node.defaults().get("unit").toJavaValue());assertTrue(patch().contains("triggertimer"));
        assertTrue(node.layout(new NodeState(Map.of("unit",GLNodeValue.of("sample")))).rows().get(0).label().contains("smp"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void timingSmoothingUnitsAndFeedbackChunks()throws Exception{
        var source=temp.resolve("timer.cpp");Files.writeString(source,"""
        #include "AudioGraph.hpp"
        #include "nodes/TriggerTimerNode.hpp"
        #include <cassert>
        using namespace easyknack;
        int main(){
            ValueEventBuffer events;events.clear(8);float elapsed[8]{},interval[8]{},silence[8]{},smooth=.9f;
            const float* inputs[]{nullptr,&smooth};float* outputs[]{elapsed,interval};const ValueEventBuffer* incoming[]{&events,nullptr};
            ProcessContext c{inputs,2,elapsed,8,1000,silence,8};c.outputs=outputs;c.outputCount=2;c.sourceRates="ec";c.valueEventInputs=incoming;
            TriggerTimerNode timer;timer.unit=TriggerTimerNode::Unit::sample;
            timer.process(c);for(int i=0;i<8;i++)assert(elapsed[i]==0&&interval[i]==0);
            events.push(2,1,ValueEventKind::trigger);events.push(6,1,ValueEventKind::trigger);events.push(6,1,ValueEventKind::trigger);
            timer.process(c);assert(elapsed[2]==0&&elapsed[5]==3&&elapsed[6]==0&&elapsed[7]==1);
            assert(interval[5]==0&&interval[6]==4&&interval[7]==4); // First interval is not faded from zero.
            events.clear(8);events.push(6,1,ValueEventKind::trigger);timer.process(c);assert(std::abs(interval[6]-4.4f)<1e-6&&elapsed[6]==0);
            smooth=1;timer.process(c);assert(std::abs(interval[6]-4.4f)<1e-6);
            smooth=0;timer.process(c);assert(interval[6]==8);
            TriggerTimerNode settings;settings.unit=TriggerTimerNode::Unit::second;timer.applyParameters(settings);events.clear(8);timer.process(c);
            assert(std::abs(interval[0]-.008f)<1e-7&&std::abs(elapsed[0]-.002f)<1e-7);
            settings.unit=TriggerTimerNode::Unit::ms;timer.applyParameters(settings);timer.process(c);assert(interval[0]==8&&elapsed[0]==10);
            TriggerTimerNode copy;copy.unit=timer.unit;copy.inheritState(timer);timer.process(c);float expected=elapsed[7];copy.process(c);assert(elapsed[7]==expected&&interval[7]==8);
            // Full blocks and chunks must agree, including the smoothing value at each trigger.
            events.clear(8);for(unsigned i:{0,1,4,7})events.push(i,1,ValueEventKind::trigger);
            float modulation[]{1,1,1,1,.5f,0,0,0},a[8]{},b[8]{},x[8]{},y[8]{};
            inputs[1]=modulation;c.sourceRates="ea";TriggerTimerNode full,chunked;full.unit=chunked.unit=TriggerTimerNode::Unit::sample;
            outputs[0]=a;outputs[1]=b;c.output=a;full.process(c);
            for(int offset=0;offset<8;offset+=2){inputs[1]=modulation+offset;outputs[0]=x+offset;outputs[1]=y+offset;c.output=x+offset;c.bufferSize=2;c.sampleOffset=offset;c.samplewise=true;chunked.processSample(c);}
            for(int i=0;i<8;i++){assert(a[i]==x[i]);assert(b[i]==y[i]);}
            assert(b[1]==1&&b[4]==2&&b[7]==3);
            // Manual triggering and coalescing use the same interval semantics.
            TriggerTimerNode manual;manual.unit=TriggerTimerNode::Unit::sample;manual.bindIdentity("timer");
            inputs[0]=inputs[1]=nullptr;c.valueEventInputs=nullptr;c.sampleOffset=0;c.bufferSize=8;c.output=elapsed;outputs[0]=elapsed;outputs[1]=interval;
            submitManualTrigger("timer:trigger");submitManualTrigger("timer:trigger");++manualTriggerEpoch;manual.process(c);assert(interval[0]==0&&elapsed[7]==7);
            submitManualTrigger("timer:trigger");++manualTriggerEpoch;manual.process(c);assert(interval[0]==8&&elapsed[0]==0);
        """+"auto graph=AudioGraph::parse("+quote(patch())+");"+"""
            float out[16]{};graph.process(out,16,0,nullptr);for(int i=0;i<16;i++)assert(std::abs(out[i]-(i<7?0.f:4.f/48000))<1e-9);
        }
        """);RateConverterTest.compileAndRun(source,temp.resolve("timer.exe"),temp);
    }
}
