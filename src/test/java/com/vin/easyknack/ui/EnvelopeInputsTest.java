package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import com.vin.easyknack.nodes.*;
import java.nio.file.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class EnvelopeInputsTest {
    @TempDir Path temp;
    @Test void parametersAreConnectableAndHoldDefaultsOn() {
        var node=new EnvelopeNode();
        assertEquals(true,node.defaults().get("hold").toJavaValue());
        var rows=node.layout(new NodeState(node.defaults())).rows();
        for(var id:List.of(EnvelopeNode.DURATION,EnvelopeNode.DECAY,EnvelopeNode.RELEASE))
            assertEquals(NodeBuilder.RowKind.INPUT,rows.stream().filter(r->r.key().equals(id)).findFirst().orElseThrow().kind());
    }
    @Test void envelopeEditorLocksPointPositions()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{
            var rows=new EnvelopeNode().layout(new NodeState(new EnvelopeNode().defaults())).rows();
            var curve=(CurveEditor)rows.stream().filter(r->r.key().equals(EnvelopeNode.CURVE)).findFirst().orElseThrow().editor();
            assertTrue(((com.vingame.glswing.ui.GLCurveEditor)curve.create("Envelope",v->{}).component()).hasFixedPointPositions());
            assertTrue(rows.stream().noneMatch(r->r.key().equals(EnvelopeNode.SUSTAIN)));
        });
    }
    @Test void oldCombinedTimingIsSplitAndRetiredConnectionsAreRemoved(){
        var envelope=new EnvelopeNode();var id=com.vingame.glswing.node.model.GLNodeId.random();var sourceId=com.vingame.glswing.node.model.GLNodeId.random();
        var values=new java.util.HashMap<>(envelope.defaults());values.remove("decay");values.put("duration",com.vingame.glswing.node.model.GLNodeValue.of(500));values.put("sustain-position",com.vingame.glswing.node.model.GLNodeValue.of(.5));
        var graph=new com.vingame.glswing.node.model.GLNodeGraph();var value=new ValueNode();
        graph.edit("Old envelope",e->{e.addNode(new com.vingame.glswing.node.model.GLGraphNode(id,envelope.registration().id(),values));e.addNode(new com.vingame.glswing.node.model.GLGraphNode(sourceId,value.registration().id(),value.defaults()));
            e.addConnection(new com.vingame.glswing.node.model.GLGraphConnection(com.vingame.glswing.node.model.GLNodeConnectionId.random(),new com.vingame.glswing.node.model.GLNodeEndpoint(sourceId,"value-out"),new com.vingame.glswing.node.model.GLNodeEndpoint(id,"sustain-position")));});
        var document=new com.vingame.glswing.node.document.GLNodeGraphDocument(new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,graph.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty()));
        var migrated=EnvelopeMigration.migrate(document);var state=new NodeState(migrated.root().graph().findNode(id).orElseThrow().properties());
        assertEquals(200,state.number("duration",0),1e-9);assertEquals(300,state.number("decay",0),1e-9);assertTrue(migrated.root().graph().connections().isEmpty());
        assertEquals(migrated,EnvelopeMigration.migrate(migrated));
        var endpoint=new com.vingame.glswing.node.model.GLNodeEndpoint(id,"sustain-position");
        var presentation=new com.vingame.glswing.node.composite.GLCompositePortPresentation(com.vingame.glswing.node.composite.GLNodeInputEditorRegistry.SLIDER,true,java.util.Map.of(GroupEditorCopies.TARGETS,GroupEditorCopies.targetsValue(List.of(endpoint))));
        var descriptor=new com.vingame.glswing.node.composite.GLCompositeNodeDescriptor("test.envelope","envelope-group",1,List.of(new com.vingame.glswing.node.composite.GLCompositePortMapping(com.vingame.glswing.node.model.GLNodePortDescriptor.input("Legacy",SignalCategory.VALUE.valueTypeId(),false,1),endpoint,presentation)));
        var asset=new com.vingame.glswing.node.document.GLNodeGraphAsset("envelope-group",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty(),java.util.Map.of(),descriptor);
        var nested=EnvelopeMigration.migrate(new com.vingame.glswing.node.document.GLNodeGraphDocument(document.root(),java.util.Map.of(asset.id(),asset),List.of()));
        var port=nested.embeddedAssets().get(asset.id()).compositeDescriptor().portMappings().get(0);
        assertFalse(port.isBound());assertTrue(GroupEditorCopies.targets(port).isEmpty());assertEquals(com.vingame.glswing.node.composite.GLNodeInputEditorRegistry.NONE,port.presentation().inputEditorId());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void holdAndConnectedParametersWorkWithScalarAndAudioInputs() throws Exception {
        var source=temp.resolve("envelope.cpp");
        Files.writeString(source,"""
            #include "AudioGraph.hpp"
            #include "nodes/EnvelopeNode.hpp"
            #include <cassert>
            #include <sstream>
            using namespace easyknack;
            int main(){
                const std::string curve="3 0 0 0 0 0 0 0 .5 1 .5 1 .5 1 0 1 0 1 0 1 0 0";
                auto init=[&](EnvelopeNode& n){std::istringstream in(curve);n.curve.read(in);n.gateValue=1;n.duration=2;n.release=2;};
                float out[16]{},silence[16]{};const float* inputs[5]{};
                ProcessContext c{inputs,5,out,16,1000,silence,16};
                EnvelopeNode held;init(held);held.process(c);assert(held.stage==EnvelopeNode::HOLD&&out[15]==1);
                EnvelopeNode settings;init(settings);settings.hold=false;held.applyParameters(settings);
                held.process(c);assert(held.stage==EnvelopeNode::IDLE&&out[15]==0);
                EnvelopeNode shot;init(shot);shot.hold=false;shot.process(c);assert(out[2]==1&&out[5]==0&&out[15]==0);
                shot.process(c);for(float x:out)assert(x==0); // High gate does not retrigger.
                float duration=4,release=3,sustain=.25;
                inputs[2]=&duration;inputs[3]=&release;inputs[4]=&sustain;c.sourceRates="aaccc";
                EnvelopeNode modulated;init(modulated);modulated.hold=false;modulated.process(c);
                assert(out[4]==.5f&&out[7]>0&&out[8]==0); // All three connected scalars affect timing/level.
                float durations[16]{};inputs[2]=durations;c.sourceRates="aaacc";
                EnvelopeNode audio;init(audio);audio.hold=false;audio.process(c);assert(out[0]==.5f&&out[4]==0);
                auto phaseInit=[](EnvelopeNode& n){
                    std::istringstream shape("4 0 0 0 0 0 0 0 .2 1 .2 1 .2 1 0 .5 .6 .5 .6 .5 .6 0 1 0 1 0 1 0 0");
                    n.curve.read(shape);n.legacy=false;n.duration=2;n.decay=2;n.release=3;n.gateValue=1;
                };
                const float* phaseInputs[5]{};ProcessContext phases{phaseInputs,5,out,16,1000,silence,16};
                EnvelopeNode separate;phaseInit(separate);separate.process(phases);
                assert(out[0]==0&&std::abs(out[1]-.5f)<1e-6&&out[2]==1&&std::abs(out[3]-.8f)<1e-6&&std::abs(out[4]-.6f)<1e-6&&separate.stage==EnvelopeNode::HOLD);
                separate.gateValue=0;separate.process(phases);assert(std::abs(out[0]-.6f)<1e-6&&std::abs(out[1]-.4f)<1e-6&&std::abs(out[2]-.2f)<1e-6&&out[3]==0);
                EnvelopeNode phaseShot;phaseInit(phaseShot);phaseShot.hold=false;phaseShot.process(phases);assert(phaseShot.stage==EnvelopeNode::IDLE&&out[7]==0);
                EnvelopeNode immediate;phaseInit(immediate);immediate.duration=immediate.decay=immediate.release=0;immediate.process(phases);assert(immediate.stage==EnvelopeNode::HOLD&&std::abs(out[0]-.6f)<1e-6);
                immediate.gateValue=0;immediate.process(phases);assert(immediate.stage==EnvelopeNode::IDLE&&out[0]==0);
                float attackMs=4,decayMs=3,releaseMs=2;phaseInputs[2]=&attackMs;phaseInputs[3]=&decayMs;phaseInputs[4]=&releaseMs;phases.sourceRates="aaccc";
                EnvelopeNode scalars;phaseInit(scalars);scalars.process(phases);assert(out[4]==1&&std::abs(out[7]-.6f)<1e-6);
                float audioAttack[16]{};phaseInputs[2]=audioAttack;phases.sourceRates="aaacc";EnvelopeNode audioPhases;phaseInit(audioPhases);audioPhases.process(phases);assert(out[0]==1&&std::abs(out[3]-.6f)<1e-6);
                EnvelopeNode latest;std::istringstream latestParameters("ENVELOPE_NEW_PARAMETERS");latest.configure(latestParameters);assert(!latest.legacy&&latest.duration==120&&latest.decay==180&&latest.sources.size()==5);
                EnvelopeNode old;std::istringstream legacy("!a11bbab0677e0e68 -1 1 -1 0 2 2 .5 "+curve);
                old.configure(legacy);assert(old.hold&&old.sources.size()==2);
                EnvelopeNode current;std::istringstream modern("!10ad0836639a66ae -1 1 -1 0 2 2 3 2 4 .5 0 "+curve);
                current.configure(modern);assert(!current.hold&&current.sources[2]==2&&current.sources[4]==4);
            }
            """.replace("ENVELOPE_NEW_PARAMETERS",RateConverterTest.payload(new EnvelopeNode(),"audio",-1)));
        RateConverterTest.compileAndRun(source,temp.resolve("envelope.exe"),temp);
    }
}
