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

class TriggerCounterTest {
    @TempDir Path temp;
    private String patch(){
        var g=new GLNodeGraph();
        var clock=add(g,new ClockNode(),Map.of("frequency",GLNodeValue.of(12000)));
        var counter=add(g,new TriggerCounterNode());
        var compare=add(g,new ComparatorNode(),Map.of("operation",GLNodeValue.of("equal"),"b",GLNodeValue.of(1)));
        var filter=add(g,new TriggerFilterNode());var pulse=add(g,new TriggerPulseNode());var out=add(g,new OutputNode());
        connect(g,clock,"value-out",counter,"trigger");
        connect(g,counter,"index",compare,"a");connect(g,counter,"trigger-out",filter,"trigger");
        connect(g,compare,"value-out",filter,"condition");connect(g,filter,"trigger-out",pulse,"trigger");
        connect(g,pulse,"audio-out",out,"audio-in");
        return AudioGraphEncoder.encode(g.snapshot());
    }
    @Test void graphExposesIndependentIndexAndTriggerRates(){
        var n=new TriggerCounterNode();assertEquals("ae",AudioGraphEncoder.outputRateMetadata(n.layout(new NodeState(n.defaults()))));
        assertTrue(patch().contains("triggerfilter"));
        assertTrue(GeneratedNodes.create().stream().anyMatch(TriggerCounterNode.class::isInstance));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void countersFiltersAndGlobalCoalescing()throws Exception{
        var source=temp.resolve("counter.cpp");
        Files.writeString(source,"""
            #include "AudioGraph.hpp"
            #include "nodes/TriggerCounterNode.hpp"
            #include "nodes/TriggerFilterNode.hpp"
            #include "nodes/MidiNoteNode.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                ValueEventBuffer input,reset,output;input.clear(16);reset.clear(16);output.clear(16);
                input.push(2,1,ValueEventKind::trigger);input.push(2,1,ValueEventKind::trigger);
                input.push(2,3);input.push(2,1,ValueEventKind::trigger);
                assert(input.count==2&&input.dropped==0&&input.valueAt(2)==3);
                // Coalescing does not consume capacity or lose numeric updates.
                ValueEventBuffer full;full.clear(512);for(int i=0;i<512;i++)full.push(i,1,ValueEventKind::trigger);
                assert(full.push(511,1,ValueEventKind::trigger)&&full.dropped==0);
                input.clear(16);for(unsigned i:{2,5,9}){input.push(i,1,ValueEventKind::trigger);input.push(i,1,ValueEventKind::trigger);}
                float index[16]{},pulses[16]{},silence[16]{},count=2;
                const float* inputs[]{nullptr,nullptr,&count};float* outputs[]{index,pulses};
                const ValueEventBuffer* incoming[]{&input,&reset,nullptr};ValueEventBuffer* outgoing[]{nullptr,&output};
                ProcessContext c{inputs,3,index,16,48000,silence,16};c.outputs=outputs;c.outputCount=2;c.sourceRates="eec";c.valueEventInputs=incoming;c.valueEventOutputs=outgoing;
                TriggerCounterNode counter;counter.process(c);
                assert(output.count==3&&index[2]==0&&index[4]==0&&index[5]==1&&index[9]==2&&index[15]==2);
                output.clear(16);input.clear(16);input.push(0,1,ValueEventKind::trigger);counter.process(c);assert(index[0]==3);
                reset.push(0,1,ValueEventKind::trigger);output.clear(16);counter.process(c);assert(index[0]==0&&output.count==1);
                counter.wrapping=true;input.clear(16);for(unsigned i:{0,1,2,3})input.push(i,1,ValueEventKind::trigger);output.clear(16);counter.process(c);
                assert(index[0]==0&&index[1]==1&&index[2]==0&&index[3]==1);
                TriggerCounterNode inherited;inherited.inheritState(counter);assert(inherited.next==counter.next&&inherited.index==counter.index);
                // Filter samples the condition at each event, including adjacent samples.
                float condition[16]{};condition[1]=1;condition[2]=-1;condition[3]=std::numeric_limits<float>::quiet_NaN();
                const float* filterInputs[]{nullptr,condition};const ValueEventBuffer* filterEvents[]{&output,nullptr};ValueEventBuffer accepted;accepted.clear(16);ValueEventBuffer* filterOut[]{&accepted};
                ProcessContext f{filterInputs,2,pulses,16,48000,silence,16};f.sourceRates="ea";f.valueEventInputs=filterEvents;f.valueEventOutputs=filterOut;
                TriggerFilterNode filter;filter.processEventBlock(f);assert(accepted.count==1&&accepted.events[0].sampleOffset==1);
                // Separate MIDI voices retain simultaneous note triggers.
                MidiBlock midi;midi.source=1;midi.append(4,144|(60<<8)|(100<<16));midi.append(4,144|(64<<8)|(100<<16));assert(midi.count==2);
                for(unsigned voice=0;voice<2;voice++){
                    MidiNoteNode note;note.voiceCount=2;note.selectVoice(voice);float a[16]{},b[16]{},d[16]{},e[16]{};float* noteOutputs[]{a,b,d,e};
                    ValueEventBuffer events;events.clear(16);ValueEventBuffer* noteEvents[]{nullptr,nullptr,&events,nullptr};const MidiBlock* midiInputs[]{&midi};const float* nullInput[]{nullptr};
                    ProcessContext m{nullInput,1,a,16,48000,silence,16};m.outputs=noteOutputs;m.outputCount=4;m.valueEventOutputs=noteEvents;m.midiInputs=midiInputs;
                    note.process(m);assert(events.count==1&&events.events[0].sampleOffset==4);
                }
            """+"auto graph=AudioGraph::parse("+quote(patch())+");"+"""
                float result[16]{};graph.process(result,16,0,nullptr);
                for(int i=0;i<16;i++)assert(result[i]==(i==7?1.f:0.f));
                graph.process(result,16,0,nullptr);for(float v:result)assert(v==0);
            }
            """);
        RateConverterTest.compileAndRun(source,temp.resolve("counter.exe"),temp);
    }
}
