package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.GLNode;
import com.vingame.glswing.node.model.*;

class StepSwitchTest {
    @Test void selectionWorksOfflineAndSurvivesSavingAndRuntimeFeedback()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var d=new StepSwitchNode();var id=GLNodeId.random();c.workspace().currentGraph().edit("Switch",e->e.addNode(new GLGraphNode(id,d.registration().id(),d.defaults())));
            var v=(StepSwitchView)view(c,id);v.selectStep(2);assertEquals(2,v.activeStep());
            var state=new NodeState(c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties());long revision=StepSwitchNode.revision(state);
            com.vin.easyknack.audio.AudioAnalysis.publish(Map.of(id.toString(),new com.vin.easyknack.audio.AudioAnalysis.Frame(0,revision-1,0,new float[]{0})));
            assertEquals(2,v.activeStep(),"Ignore feedback from before the manual selection");
            var bytes=new java.io.ByteArrayOutputStream();var codec=new KnackDocumentCodec();codec.write(c.snapshotDocument(),bytes);
            c.openDocument("Restored",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
            assertEquals(2,((StepSwitchView)view(c,id)).activeStep());
            com.vin.easyknack.audio.AudioAnalysis.publish(Map.of(id.toString(),new com.vin.easyknack.audio.AudioAnalysis.Frame(3,revision,0,new float[]{3})));
            assertEquals(3,StepSwitchNode.selected(new NodeState(c.snapshotDocument().root().graph().findNode(id).orElseThrow().properties())));
            assertFalse(c.history().canUndo(),"Runtime steps must not create Undo entries");
            var restoredView=(StepSwitchView)view(c,id);restoredView.selectStep(2);
            long abandoned=StepSwitchNode.revision(new NodeState(c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties()));
            c.history().undo();restoredView=(StepSwitchView)view(c,id);restoredView.selectStep(1);
            assertTrue(StepSwitchNode.revision(new NodeState(c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties()))>abandoned);
            com.vin.easyknack.audio.AudioAnalysis.publish(Map.of(id.toString(),new com.vin.easyknack.audio.AudioAnalysis.Frame(2,abandoned,0,new float[]{2})));
            assertEquals(1,((StepSwitchView)view(c,id)).activeStep());
        }catch(Exception e){throw new AssertionError(e);}finally{com.vin.easyknack.audio.AudioAnalysis.clear();}});
    }
    @Test void namesCanBeEditedInTheRowAndByClickingConnectedLabels()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var d=new StepSwitchNode();var id=GLNodeId.random();c.workspace().currentGraph().edit("Switch",e->e.addNode(new GLGraphNode(id,d.registration().id(),d.defaults())));
            var v=(StepSwitchView)view(c,id);v.renameStep(0);
            var group=v.getContents().stream().filter(com.vingame.glswing.ui.GLLayoutGroup.class::isInstance).findFirst().orElseThrow();
            var editor=(com.vingame.glswing.ui.GLTextEditor)group.getGLChildren().stream().filter(com.vingame.glswing.ui.GLTextEditor.class::isInstance).findFirst().orElseThrow();
            assertTrue(editor.isVisible());editor.setText("Größe A");editor.commit();assertFalse(editor.isVisible());
            assertEquals("Größe A",StepSwitchNode.label(new NodeState(c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties()),0));
            var source=c.workspace().editor().getNodes().stream().filter(n->n.findPort("audio-out")!=null).findFirst().orElseThrow();c.workspace().editor().connect(source.findPort("audio-out"),v.findPort("step-0"));
            var label=group.getGLChildren().stream().filter(NodeTooltipLabel.class::isInstance).map(NodeTooltipLabel.class::cast).findFirst().orElseThrow();assertTrue(label.isVisible());
            var awt=new java.awt.event.MouseEvent(new java.awt.Canvas(),java.awt.event.MouseEvent.MOUSE_PRESSED,0,0,2,2,1,false,java.awt.event.MouseEvent.BUTTON1);
            var event=new com.vingame.glswing.input.GLMouseEvent(awt,new com.vingame.glswing.input.GLPickResult(1,label,2,2,2,2,1,0,0,1,0,0),0,0);
            label.mousePressed(event);assertTrue(editor.isVisible());editor.setText("Ignored");editor.cancel();assertEquals("Größe A",label.getText());
        }});
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeRestoresSelectionAndDoesNotReapplyTelemetryAsAManualRequest()throws Exception{
        var d=new StepSwitchNode();var values=new HashMap<>(d.defaults());values.put(StepSwitchNode.SELECTION.value(),StepSwitchNode.selection(2,4));
        String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(values),p->-1);
        var dir=Files.createTempDirectory(Path.of("target"),"switch-state-");var source=dir.resolve("test.cpp");
        Files.writeString(source,"""
            #include "nodes/StepSwitchNode.hpp"
            #include <cassert>
            #include <sstream>
            using namespace easyknack;
            int main(){StepSwitchNode n;std::istringstream in("%s");n.configure(in);assert(n.index==2&&n.selectionRevision==4);
              StepSwitchNode echo;echo.steps=4;echo.index=1;echo.selectionRevision=4;n.index=3;n.applyParameters(echo);assert(n.index==3);
              echo.selectionRevision=5;n.applyParameters(echo);assert(n.index==1);
              StepSwitchNode rebuilt;rebuilt.steps=4;rebuilt.selectionRevision=5;rebuilt.inheritState(n);assert(rebuilt.index==1);
              rebuilt.index=0;rebuilt.selectionRevision=6;rebuilt.inheritState(n);assert(rebuilt.index==0);
            }
            """.formatted(payload));RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test void indexModeAndNamesPersistWithoutRewiringValueInputs()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var d=new StepSwitchNode();var id=GLNodeId.random();
            c.workspace().currentGraph().edit("Switch",e->e.addNode(new GLGraphNode(id,d.registration().id(),d.defaults())));
            var source=c.workspace().editor().getNodes().stream().filter(n->n.findPort("audio-out")!=null).findFirst().orElseThrow();
            c.workspace().editor().connect(source.findPort("audio-out"),view(c,id).findPort("step-0"));
            c.setNodeProperty(id,StepSwitchNode.name(0),GLNodeValue.of("Größe A"));
            c.setNodeProperty(id,StepSwitchNode.MODE,GLNodeValue.of("index"));
            var model=c.workspace().currentGraph().snapshot().findNode(id).orElseThrow();
            var layout=d.layout(new NodeState(model.properties()));
            assertTrue(StepSwitchNode.indexed(new NodeState(model.properties())));
            assertEquals("Größe A",StepSwitchNode.label(new NodeState(model.properties()),0));
            assertTrue(layout.rows().stream().noneMatch(r->r.key().equals(StepSwitchNode.RESET)));
            assertEquals(1,view(c,id).findPort("step-0").getActiveConnectionCount());
            c.setNodeProperty(id,StepSwitchNode.NEXT,GLNodeValue.of(3));
            c.setNodeProperty(id,StepSwitchNode.STEPS,GLNodeValue.of(2));
            assertDoesNotThrow(c::audioGraph);
            var bytes=new java.io.ByteArrayOutputStream();var codec=new KnackDocumentCodec();codec.write(c.snapshotDocument(),bytes);
            var restored=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
            assertEquals("Größe A",restored.root().graph().findNode(id).orElseThrow().properties().get("step-name-0").toJavaValue());
        }catch(Exception ex){throw new AssertionError(ex);}});
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void indexModeSelectsPerSampleAndClamps()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"index-switch-");var d=new StepSwitchNode();
        var properties=new HashMap<>(d.defaults());properties.put("mode",GLNodeValue.of("index"));
        for(int i=0;i<4;i++)properties.put("step-"+i,GLNodeValue.of(10*(i+1)));
        String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(properties),p->p.equals(StepSwitchNode.NEXT)?0:-1);
        var source=dir.resolve("test.cpp");Files.writeString(source,"""
            #include "nodes/StepSwitchNode.hpp"
            #include <cassert>
            #include <sstream>
            using namespace easyknack;
            int main(){StepSwitchNode node;std::istringstream config("%s");node.configure(config);
              float indices[]={0,1.9f,2,99,-2},output[5]{},shown[5]{};const float* inputs[34]{};inputs[0]=indices;
              float* outputs[]={output,shown};ProcessContext c{};c.bufferSize=5;c.blockFrames=5;c.output=output;c.outputs=outputs;c.outputCount=2;c.inputs=inputs;c.inputCount=34;
              node.process(c);float expected[]={10,20,30,40,10};for(int i=0;i<5;i++)assert(output[i]==expected[i]);
              assert(node.resolveAutoRate("cccccccccccccccccccccccccccccccccc",34)=='c');
              assert(node.resolveAutoRate("accccccccccccccccccccccccccccccccc",34)=='a');
            }
            """.formatted(payload));
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test void navigationRestoresConnectedEditorsAndRates()throws Exception{
        WorkspaceController[] holder=new WorkspaceController[1];var id=GLNodeId.random();
        try{
            SwingUtilities.invokeAndWait(()->{
                var c=holder[0]=new WorkspaceController();var graph=c.workspace().currentGraph();var node=new StepSwitchNode();
                var math=new MathNode();var other=GLNodeId.random();
                graph.edit("Nodes",e->{e.addNode(new GLGraphNode(id,node.registration().id(),node.defaults()));e.addNode(new GLGraphNode(other,math.registration().id(),math.defaults()));});
                var source=c.workspace().editor().getNodes().stream().filter(n->n.findPort("audio-out")!=null).findFirst().orElseThrow();
                c.workspace().editor().connect(source.findPort("audio-out"),view(c,id).findPort("step-0"));
                c.workspace().editor().setSelectedNode(view(c,other));c.actions().extractComposite();
            });
            for(int repeat=0;repeat<2;repeat++){
                SwingUtilities.invokeAndWait(()->holder[0].actions().openSelectedComposite());
                SwingUtilities.invokeAndWait(()->holder[0].workspace().navigateBack());
                SwingUtilities.invokeAndWait(()->{
                    var v=view(holder[0],id);
                    assertEquals(1,v.findPort("step-0").getActiveConnectionCount());
                    assertEquals(com.vingame.glswing.node.GLNodePort.Shape.CIRCLE,v.findPort("step-0").getShape());
                    assertEquals(com.vingame.glswing.node.GLNodePort.Shape.CIRCLE,v.findPort("value").getShape());
                    var row=v.getContents().stream().filter(com.vingame.glswing.ui.GLLayoutGroup.class::isInstance).findFirst().orElseThrow();
                    assertFalse(row.getGLChildren().stream().filter(com.vingame.glswing.ui.GLSlider.class::isInstance).findFirst().orElseThrow().isVisible());
                });
            }
        }finally{SwingUtilities.invokeAndWait(()->{if(holder[0]!=null)holder[0].close();});}
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void hostAcceptsManualButtonsAndPublishesSelection()throws Exception{
        var n=new StepSwitchNode();var state=new HashMap<>(n.defaults());long request=0x123456789ABCL;state.put(StepSwitchNode.SELECTION.value(),StepSwitchNode.selection(0,request));String payload="!"+NativeContractGenerator.fingerprint(n)+" "+n.encode(new NodeState(state),p->-1);
        try(var client=new com.vin.easyknack.audio.AudioHostClient()){
            client.start(com.vin.easyknack.audio.AudioHostClient.defaultExecutable(),true);
            assertEquals("OK",client.command("GRAPH 2 switch stepswitch "+payload+" @ c ee"+"c".repeat(32)+" out out 0 0 @ a c"));
            assertEquals("OK",client.command("PLAY"));var initial=awaitIndex(client,0);
            assertEquals(request,StepSwitchStates.reportedRevision(initial));
            assertEquals("OK",client.command("TRIGGER switch:next"));awaitIndex(client,1);
            assertEquals("OK",client.command("TRIGGER switch:select-3"));awaitIndex(client,3);
            // Repeated unchanged selections are covered with explicit DSP epochs below.
            assertEquals("OK",client.command("TRIGGER switch:next"));awaitIndex(client,0);
            assertEquals("OK",client.command("TRIGGER switch:next"));awaitIndex(client,1);
            assertEquals("OK",client.command("TRIGGER switch:reset"));awaitIndex(client,0);
            assertEquals("OK",client.command("STOP"));
        }
    }
    private static com.vin.easyknack.audio.AudioAnalysis.Frame awaitIndex(com.vin.easyknack.audio.AudioHostClient client,int expected)throws Exception{
        long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(3);
        do{
            var frame=com.vin.easyknack.audio.AudioAnalysis.parse(client.command("ANALYSIS")).get("switch");
            if(frame!=null&&frame.frequency()==expected)return frame;
            Thread.sleep(20);
        }while(System.nanoTime()<deadline);
        return fail("Host did not publish step "+expected);
    }
    private static GLNode view(WorkspaceController c,GLNodeId id){return c.workspace().editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();}
    @Test void groupedEditorsPersistSelectionAndRemainAvailableWhenConnected()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            try(var c=new WorkspaceController()){
                var graph=c.workspace().currentGraph();var node=new StepSwitchNode();var id=GLNodeId.random();
                graph.edit("Switch",e->e.addNode(new GLGraphNode(id,node.registration().id(),node.defaults())));
                var view=(StepSwitchView)view(c,id);
                var groups=view.getContents().stream().filter(com.vingame.glswing.ui.GLLayoutGroup.class::isInstance).map(com.vingame.glswing.ui.GLLayoutGroup.class::cast).toList();
                assertEquals(4,groups.size());
                var button=(com.vingame.glswing.ui.GLButton)groups.get(2).getGLChildren().stream().filter(com.vingame.glswing.ui.GLButton.class::isInstance).findFirst().orElseThrow();
                var sent=new ArrayList<String>();com.vin.easyknack.audio.ManualTriggers.receiver(sent::add);
                var before=graph.snapshot();button.doClick();assertTrue(sent.isEmpty(),"The versioned parameter request must not also emit a duplicate manual trigger");assertEquals(2,StepSwitchNode.selected(new NodeState(graph.snapshot().findNode(id).orElseThrow().properties())));assertNotEquals(before,graph.snapshot());
                com.vin.easyknack.audio.AudioAnalysis.publish(Map.of(id.toString(),new com.vin.easyknack.audio.AudioAnalysis.Frame(2,StepSwitchNode.revision(new NodeState(graph.snapshot().findNode(id).orElseThrow().properties())),0,new float[]{2})));
                assertEquals(2,view.activeStep());assertEquals(button.getGLTheme().buttonStyle().pressedDarkColor(),button.getGLStyle().background());
                var source=c.workspace().editor().getNodes().stream().filter(n->n.findPort("audio-out")!=null).findFirst().orElseThrow();
                c.workspace().editor().connect(source.findPort("audio-out"),view.findPort("step-2"));
                assertFalse(view.findPort("step-2").isContentHiddenWhenConnected());
                assertTrue(button.isVisible());
                assertFalse(groups.get(2).getGLChildren().stream().filter(com.vingame.glswing.ui.GLSlider.class::isInstance).findFirst().orElseThrow().isVisible());
            }finally{com.vin.easyknack.audio.ManualTriggers.receiver(id->{});com.vin.easyknack.audio.AudioAnalysis.clear();}
        });
    }
    @Test void changingStepsRetainsInputsAndUndoRestoresRemovedConnections()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            try(var c=new WorkspaceController()){
                var graph=c.workspace().currentGraph();var node=new StepSwitchNode();var id=GLNodeId.random();
                graph.edit("Step Switch",e->e.addNode(new GLGraphNode(id,node.registration().id(),node.defaults())));
                var source=c.workspace().editor().getNodes().stream().filter(n->n.findPort("audio-out")!=null).findFirst().orElseThrow();
                c.workspace().editor().connect(source.findPort("audio-out"),view(c,id).findPort("step-3"));
                var cable=graph.snapshot().connections().get(graph.snapshot().connections().size()-1);
                view(c,id).setLocation(1200,300);
                c.setNodeProperty(id,StepSwitchNode.input(0),GLNodeValue.of(42.));
                c.setNodeProperty(id,StepSwitchNode.STEPS,GLNodeValue.of(6.));
                assertNotNull(view(c,id).findPort("step-5"));assertTrue(graph.snapshot().connections().contains(cable));
                c.setNodeProperty(id,StepSwitchNode.STEPS,GLNodeValue.of(2.));
                assertNull(view(c,id).findPort("step-3"));assertFalse(graph.snapshot().connections().contains(cable));
                assertDoesNotThrow(c::audioGraph);
                assertTrue(c.history().undo());assertNotNull(view(c,id).findPort("step-5"));assertTrue(graph.snapshot().connections().contains(cable));
                assertEquals(1200f,view(c,id).getX());
                assertEquals(42.,graph.snapshot().findNode(id).orElseThrow().properties().get("step-0").toJavaValue());
            }
        });
    }
    @Test void ratesAndInactiveInputs(){
        var n=new StepSwitchNode();var s=new NodeState(n.defaults());
        assertEquals(ProcessingRate.CONTROL,n.resolveAutomaticRate(s,Map.of()));
        assertEquals(ProcessingRate.AUDIO,n.resolveAutomaticRate(s,Map.of(StepSwitchNode.NEXT,ProcessingRate.EVENT)));
        assertEquals(ProcessingRate.AUDIO,n.resolveAutomaticRate(s,Map.of(StepSwitchNode.input(1),ProcessingRate.AUDIO)));
        assertDoesNotThrow(()->n.encode(s,p->{assertFalse(p.value().equals("step-4"));return -1;}));
        assertTrue(NodeRegistry.find(n.registration().id()).isPresent());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void sampleAccurateSwitchingAndBlockDeferredTriggers()throws Exception{
        var n=new StepSwitchNode();var values=new HashMap<>(n.defaults());values.put("steps",GLNodeValue.of(3.));
        for(int i=0;i<3;i++)values.put("step-"+i,GLNodeValue.of((i+1)*10.));
        String payload="!"+NativeContractGenerator.fingerprint(n)+" "+n.encode(new NodeState(values),p->-1);
        var dir=Files.createTempDirectory(Path.of("target"),"step-switch-").toAbsolutePath();var source=dir.resolve("test.cpp");
        Files.writeString(source,"""
            #include "AudioGraph.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                StepSwitchNode node;std::istringstream config("%s");node.configure(config);
                float output[8]{},indices[8]{},silence[8]{},audio[8]{1,2,3,4,5,6,7,8};
                const float* inputs[34]{};float* outputs[]={output,indices};
                ValueEventBuffer next,reset;next.clear(8);reset.clear(8);
                const ValueEventBuffer* events[34]{};events[0]=&next;events[1]=&reset;
                ProcessContext c{inputs,34,output,8,48000,silence,8};c.outputs=outputs;c.outputCount=2;c.valueEventInputs=events;
                next.push(2,1,ValueEventKind::trigger);next.push(4,1,ValueEventKind::trigger);next.push(4,1,ValueEventKind::trigger);
                next.push(6,1,ValueEventKind::trigger);reset.push(6,1,ValueEventKind::trigger);
                node.process(c);// Coincident triggers coalesce within one stream.
                float expected[]={10,10,20,20,30,30,10,10};for(int i=0;i<8;i++)assert(output[i]==expected[i]);
                next.clear(8);reset.clear(8);next.push(0,1,ValueEventKind::trigger);inputs[3]=audio;
                node.process(c);for(int i=0;i<8;i++)assert(output[i]==audio[i]&&indices[i]==1);
                char rates[34]{};rates[3]='c';c.sourceRates=rates;node.process(c);for(int i=0;i<8;i++)assert(output[i]==30&&indices[i]==2);
                node.index=1;next.clear(8);node.process(c);for(int i=0;i<8;i++)assert(output[i]==1);
                inputs[3]=nullptr;c.bufferSize=1;node.index=0;next.push(2,1,ValueEventKind::trigger);
                node.process(c);assert(output[0]==10&&indices[0]==0&&node.index==1);
                next.clear(8);node.process(c);assert(output[0]==20&&indices[0]==1);
                reset.push(0,1,ValueEventKind::trigger);node.process(c);assert(output[0]==10);
                c.valueEventInputs=nullptr;node.bindIdentity("switch");
                submitManualTrigger("switch:next");submitManualTrigger("switch:next");++manualTriggerEpoch;node.process(c);assert(output[0]==20);
                ++manualTriggerEpoch;node.process(c);assert(output[0]==20);
                submitManualTrigger("switch:select-1");++manualTriggerEpoch;node.process(c);assert(output[0]==20);
                submitManualTrigger("switch:select-1");++manualTriggerEpoch;node.process(c);assert(output[0]==20);
                submitManualTrigger("switch:select-2");submitManualTrigger("switch:reset");++manualTriggerEpoch;node.process(c);assert(output[0]==10);
                node.index=2;
                StepSwitchNode changed;changed.steps=2;node.applyParameters(changed);assert(node.index==0);
                node.index=1;changed.inheritState(node);assert(changed.index==1);
                auto graph=AudioGraph::parse("GRAPH 2 switch stepswitch %s @ c eecccccccccccccccccccccccccccccccc out out 0 0 @ a c");
                float block[256];graph.process(block,256);for(auto v:block)assert(v==10);
                submitManualTrigger("switch:next");++manualTriggerEpoch;graph.process(block,256);for(auto v:block)assert(v==20);
            }
            """.formatted(payload,payload));
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
