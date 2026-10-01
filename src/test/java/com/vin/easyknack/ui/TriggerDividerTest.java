package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import com.vin.easyknack.nodes.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
class TriggerDividerTest {
    @TempDir Path temp;
    @Test void bpmUsesHzStorageAndDividerRegisters(){
        assertEquals(2,UnitFormat.BPM.convert(120,UnitFormat.HZ),1e-12);
        assertEquals(120,UnitFormat.HZ.convert(2,UnitFormat.BPM),1e-12);
        assertEquals(UnitFormat.HZ,UnitFormats.CLOCK.stored());
        assertTrue(UnitFormats.CLOCK.choices().contains(UnitFormat.BPM));
        var node=new TriggerDividerNode();assertEquals(2.0,((Number)node.defaults().get("divisor").toJavaValue()).doubleValue());
        assertTrue(GeneratedNodes.create().stream().anyMatch(TriggerDividerNode.class::isInstance));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void dividesSampleAccurateEventsAcrossBlocksAndResets()throws Exception{
        var source=temp.resolve("divider.cpp");Files.writeString(source,"""
        #include "AudioGraph.hpp"
        #include "nodes/TriggerDividerNode.hpp"
        #include "nodes/UnitConverterNode.hpp"
        #include <cassert>
        using namespace easyknack;
        int main(){
            ValueEventBuffer in,rst,out;in.clear(16);rst.clear(16);out.clear(16);
            const ValueEventBuffer* streams[]{&in,nullptr,&rst};ValueEventBuffer* outputs[]{&out};
            float audio[16]{},silence[16]{},division=2;const float* inputs[]{nullptr,&division,nullptr};
            ProcessContext c{inputs,3,audio,16,48000,silence,16};c.sourceRates="ece";c.valueEventInputs=streams;c.valueEventOutputs=outputs;
            TriggerDividerNode node;
            in.push(2,1,ValueEventKind::trigger);in.push(5,1,ValueEventKind::trigger);in.push(9,1,ValueEventKind::trigger);
            node.processEventBlock(c);assert(out.count==1&&out.events[0].sampleOffset==5&&node.count==1);
            in.clear(16);out.clear(16);in.push(1,1,ValueEventKind::trigger);
            node.processEventBlock(c);assert(out.count==1&&out.events[0].sampleOffset==1);
            // Reset precedes triggers at the same sample. Duplicate triggers at one sample collapse.
            in.clear(16);out.clear(16);rst.push(5,1,ValueEventKind::trigger);
            in.push(2,1,ValueEventKind::trigger);in.push(5,1,ValueEventKind::trigger);in.push(5,1,ValueEventKind::trigger);
            node.processEventBlock(c);assert(out.count==0&&node.count==1);
            division=1;rst.clear(16);out.clear(16);node.processEventBlock(c);assert(out.count==2);
            TriggerDividerNode inherited;inherited.inheritState(node);assert(inherited.count==node.count);
            // Manual buttons use the same counter and reset semantics.
            streams[0]=nullptr;streams[2]=nullptr;division=2;node.bindIdentity("divider");out.clear(16);
            submitManualTrigger("divider:trigger");++manualTriggerEpoch;node.processEventBlock(c);assert(out.count==0);
            submitManualTrigger("divider:trigger");++manualTriggerEpoch;node.processEventBlock(c);assert(out.count==1);
            out.clear(16);submitManualTrigger("divider:reset");submitManualTrigger("divider:trigger");++manualTriggerEpoch;node.processEventBlock(c);assert(out.count==0&&node.count==1);
            assert(unitDimension(UnitFormat::bpm)==unitDimension(UnitFormat::hz));
            assert(unitToBase(120,UnitFormat::bpm)==2&&unitFromBase(2,UnitFormat::bpm)==120);
            std::istringstream name("bpm");assert(readUnit(name)==UnitFormat::bpm);
        }
        """);RateConverterTest.compileAndRun(source,temp.resolve("divider.exe"),temp);
    }
}
