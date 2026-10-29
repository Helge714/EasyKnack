package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.nio.file.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;

class PortUnitsTest {
    static GLGraphNode node(BasicNode def){return new GLGraphNode(GLNodeId.random(),def.registration().id(),def.defaults());}
    static GLGraphConnection wire(GLGraphNode a,String out,GLGraphNode b,String in){return new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a.id(),out),new GLNodeEndpoint(b.id(),in));}
    static GLNode view(WorkspaceController c,GLNodeId id){return c.workspace().editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();}
    static void choose(GLNodePort port,UnitFormat unit){
        port.getContextMenu().getItems().get(0).getSubMenu().getItems().stream().filter(i->i.getText().equals(unit.toString())).findFirst().orElseThrow().doClick();
    }
    @Test void outputAndConnectedInputMenusPersistAndUndoWithoutAutoConversion()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController();var reopened=new WorkspaceController()){
            var time=node(new ContextInputNode());var osc=node(new OscillatorNode());var graph=c.workspace().currentGraph();
            graph.edit("Units",e->{for(var old:graph.snapshot().nodes())e.removeNode(old.id());e.addNode(time);e.addNode(osc);e.addConnection(wire(time,"time",osc,"frequency"));});
            choose(view(c,time.id()).findPort("time"),UnitFormat.MS);
            assertEquals("ms",new NodeState(graph.snapshot().findNode(time.id()).orElseThrow().properties()).text("time.unit",""));
            assertTrue(view(c,time.id()).getContents().stream().anyMatch(v->v instanceof com.vingame.glswing.ui.GLLabel l&&l.getText().equals("Time [ms]")));
            assertEquals("hz",new NodeState(graph.snapshot().findNode(osc.id()).orElseThrow().properties()).text("frequency.unit",""));
            var input=view(c,osc.id()).findPort("frequency");assertTrue(input.isContentHiddenByConnection());choose(input,UnitFormat.KHZ);
            assertTrue(view(c,osc.id()).getContents().stream().anyMatch(v->v.getName().equals("Frequency [kHz]")));
            c.history().undo();assertEquals("hz",new NodeState(graph.snapshot().findNode(osc.id()).orElseThrow().properties()).text("frequency.unit",""));c.history().redo();
            var bytes=new java.io.ByteArrayOutputStream();var codec=new KnackDocumentCodec();codec.write(c.snapshotDocument(),bytes);
            reopened.openDocument("Units",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
            assertEquals("khz",new NodeState(reopened.workspace().currentGraph().snapshot().findNode(osc.id()).orElseThrow().properties()).text("frequency.unit",""));
        }catch(java.io.IOException ex){throw new AssertionError(ex);}});
    }
    static GLNodeGraphSnapshot timeGraph(boolean milliseconds){
        var time=node(new ContextInputNode());if(milliseconds)time=time.withProperty("time.unit",GLNodeValue.of("ms"));
        var converter=node(new UnitConverterNode()).withProperty("from",GLNodeValue.of("ms")).withProperty("to",GLNodeValue.of("second"));
        var output=node(new OutputNode());
        return new GLNodeGraphSnapshot(new GLNodeGraph().snapshot().graphId(),0,List.of(time,converter,output),List.of(wire(time,"time",converter,"value"),wire(converter,"out",output,"audio-in")));
    }
    @Test void outputConversionIsExplicitAndMaterializationIsIdempotent(){
        var graph=timeGraph(true);var adapted=PortUnitConversions.materialize(graph);
        assertEquals(4,adapted.nodes().size());assertEquals(adapted,PortUnitConversions.materialize(adapted));
        assertEquals(3,PortUnitConversions.materialize(timeGraph(false)).nodes().size());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void secondsAreInterpretedAsMillisecondsUnlessOutputIsConverted()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"port-units-").toAbsolutePath();var cpp=dir.resolve("test.cpp");
        String seconds=com.vin.easyknack.audio.AudioGraphEncoder.encode(timeGraph(false));
        String milliseconds=com.vin.easyknack.audio.AudioGraphEncoder.encode(timeGraph(true));
        Files.writeString(cpp,"""
            #include "AudioGraph.hpp"
            #include <cassert>
            int main(){
                auto seconds=easyknack::AudioGraph::parse("%s");auto milliseconds=easyknack::AudioGraph::parse("%s");
                float a[256]{},b[256]{};
                seconds.process(a,256,0,nullptr);milliseconds.process(b,256,0,nullptr);
                assert(std::abs(a[240]-0.000005f)<1e-9);assert(std::abs(b[240]-0.005f)<1e-7);
            }
            """.formatted(seconds,milliseconds));
        RateConverterTest.compileAndRun(cpp,dir.resolve("test.exe"),dir);
    }
    @Test void groupOutputUnitsReachExportAndRuntime() throws Exception {
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var inner=new GLNodeGraph();var time=node(new ContextInputNode());inner.edit("Time",e->e.addNode(time));
            var mapping=new com.vingame.glswing.node.composite.GLCompositePortMapping(GLNodePortDescriptor.output("Time",SignalCategory.VALUE.valueTypeId()),new GLNodeEndpoint(time.id(),"time"));
            var descriptor=new com.vingame.glswing.node.composite.GLCompositeNodeDescriptor("test.time","test.time.asset",1,List.of(mapping));
            var asset=new com.vingame.glswing.node.document.GLNodeGraphAsset("test.time.asset",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty(),Map.of(),descriptor);
            var group=new GLGraphNode(GLNodeId.random(),"test.time",Map.of(GroupUnits.unitKey("Time"),GLNodeValue.of("ms")));var output=node(new OutputNode());var root=new GLNodeGraph();
            root.edit("Group",e->{e.addNode(group);e.addNode(output);e.addConnection(wire(group,"Time",output,"audio-in"));});
            var doc=new com.vingame.glswing.node.document.GLNodeGraphDocument(new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,root.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
            c.openDocument("Time",doc);assertTrue(c.audioGraph().contains("second ms"),c.audioGraph());
            var exported=GroupSourceExporter.create(doc,group);var definition=exported.definition();
            var row=definition.layout(new NodeState(definition.defaults())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).findFirst().orElseThrow();
            assertEquals(UnitFormat.MS,row.unit());assertEquals(UnitFormat.MS,row.formats().stored());
        }});
    }    @Test void timerPortMenusUseTheExistingSharedUnitContract(){
        var timer=node(new TriggerTimerNode()).withProperty("unit",GLNodeValue.of("ms"));
        var rows=new TriggerTimerNode().layout(new NodeState(timer.properties())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).toList();
        assertTrue(rows.stream().allMatch(r->r.unit()==UnitFormat.MS && r.unitProperty().equals(TriggerTimerNode.UNIT)));
    }}
