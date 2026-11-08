package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import com.vin.easyknack.audio.AudioAnalysis;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.node.document.*;
import java.util.*;
import java.io.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MeasurementRecorderTest {
    @Test void everyInputCountCapturesAndRetainsRowsAcrossUndo()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            for(int count=1;count<=8;count++){
                var graph=new GLNodeGraph();var id=GLNodeId.random();var definition=new MeasurementRecorderNode();
                var properties=new HashMap<>(definition.defaults());properties.put("inputs",GLNodeValue.of(count));
                graph.edit("Recorder",e->e.addNode(new GLGraphNode(id,definition.registration().id(),properties)));
                c.openDocument("Recorder "+count,new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty())));
                assertTrue(NodeRegistry.find(c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().typeId()).orElseThrow() instanceof MeasurementRecorderNode);
                c.setNodeProperty(id,MeasurementRecorderNode.input(0),GLNodeValue.of(20.));
                AudioAnalysis.publish(Map.of(id.toString(),frame(count,1,7,"0")));
                var table=c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("table");
                assertEquals(1,MeasurementTable.rows(table).size(),"Capture with "+count+" inputs");
                assertEquals(count+2,((GLNodeValue.ListValue)MeasurementTable.rows(table).get(0)).values().size());
                c.history().undo();
                assertEquals(table,c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("table"),"Retain with "+count+" inputs");
            }
        }finally{AudioAnalysis.clear();}});
    }
    @Test @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeCaptureAndUnconnectedManualRecorder()throws Exception{
        var directory=java.nio.file.Files.createTempDirectory(java.nio.file.Path.of("target"),"recorder-native-").toAbsolutePath();
        RateConverterTest.compileAndRun(java.nio.file.Path.of("native/audio-host/tests/MeasurementRecorderTest.cpp").toAbsolutePath(),directory.resolve("test.exe"),directory);
        var graph=new GLNodeGraph();var id=GLNodeId.random();var definition=new MeasurementRecorderNode();var props=new HashMap<>(definition.defaults());props.put("value-0",GLNodeValue.of(7.75));
        graph.edit("Recorder",e->e.addNode(new GLGraphNode(id,definition.registration().id(),props)));
        try(var host=new com.vin.easyknack.audio.AudioHostClient()){
            host.start(java.nio.file.Path.of(System.getProperty("easyknack.test.host")),true);
            host.command(com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot()));host.command("PLAY");host.command("TRIGGER "+id+":record");
            GLNodeValue table=MeasurementTable.empty();long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
            while(MeasurementTable.rows(table).isEmpty()&&System.nanoTime()<deadline){Thread.sleep(30);table=MeasurementTable.merge(table,AudioAnalysis.parse(host.command("ANALYSIS")).get(id.toString()));}
            assertEquals(1,MeasurementTable.rows(table).size());
            assertEquals(7.75,((GLNodeValue.ListValue)MeasurementTable.rows(table).get(0)).values().get(2).toJavaValue());host.command("STOP");
        }
    }
    static AudioAnalysis.Frame frame(int count,int rows,long session,String generation){
        var v=new float[12+rows*(5+count)];v[0]=MeasurementTable.MAGIC;v[1]=1;v[2]=count;v[3]=rows;
        words(v,4,Long.parseUnsignedLong(generation,16));words(v,8,session);
        for(int i=0;i<rows;i++){int p=12+i*(5+count);words(v,p,48000L*i+123);v[p+4]=(1<<count)-1;for(int j=0;j<count;j++)v[p+5+j]=i*10+j;}
        return new AudioAnalysis.Frame(0,48000,0,v);
    }
    static void words(float[] v,int at,long value){for(int i=0;i<4;i++)v[at+i]=(value>>>(16*i))&65535;}
    @Test void packetsDeduplicateRetainMissingPollsAndRespectClearAndCapacity(){
        var table=MeasurementTable.merge(MeasurementTable.empty(),frame(2,1,7,"0"));
        assertEquals(1,MeasurementTable.rows(table).size());assertSame(table,MeasurementTable.merge(table,frame(2,1,7,"0")));
        table=MeasurementTable.merge(table,frame(2,4,7,"0"));assertEquals(4,MeasurementTable.rows(table).size());
        table=MeasurementTable.merge(table,frame(1,1,8,"0"));assertEquals(5,MeasurementTable.rows(table).size());
        assertEquals(2,MeasurementTable.columns(table,1));
        var clear=MeasurementTable.clear();assertSame(clear,MeasurementTable.merge(clear,frame(2,4,7,"0")));
        clear=MeasurementTable.merge(clear,frame(1,1,9,MeasurementTable.generation(clear)));assertEquals(1,MeasurementTable.rows(clear).size());
        table=MeasurementTable.merge(table,frame(2,4096,10,"0"));assertEquals(4096,MeasurementTable.rows(table).size());
        assertSame(table,MeasurementTable.merge(table,frame(2,1,11,"0")));
    }
    @Test void labelsRemainAttachedToRowsWhenCaptureAppendsAndCsvEscapesThem()throws Exception{
        var table=MeasurementTable.merge(MeasurementTable.empty(),frame(2,1,7,"0"));
        String name="FFT; \"breit\", Größe\nA";
        table=MeasurementTable.withLabel(table,0,name);assertSame(table,MeasurementTable.withLabel(table,0,name));
        table=MeasurementTable.merge(table,frame(2,3,7,"0"));
        assertEquals(name,MeasurementTable.label(table,0));assertEquals("",MeasurementTable.label(table,1));
        assertEquals(2,MeasurementTable.columns(table,1));assertEquals(4,((GLNodeValue.ListValue)MeasurementTable.rows(table).get(0)).values().size());
        var csv=new StringWriter();MeasurementTable.csv(table,new String[]{"B","A"},new int[]{1,0},csv,false);
        assertTrue(csv.toString().startsWith("measurement,test,session,time_s"));
        assertTrue(csv.toString().contains("\"FFT; \"\"breit\"\", Größe\nA\""));
        assertTrue(csv.toString().contains(",1.0,0.0\r\n"));
        var excel=new StringWriter();MeasurementTable.csv(table,new String[]{"A"},excel,true);
        assertTrue(excel.toString().startsWith("\uFEFFmeasurement;test;session;time_s"));assertTrue(excel.toString().contains("Größe\nA"));
        assertEquals("",MeasurementTable.label(MeasurementTable.clear(),0));
        assertThrows(IndexOutOfBoundsException.class,()->MeasurementTable.withLabel(MeasurementTable.empty(),0,"Missing"));
    }
    @Test void labelUndoPreservesNewAutomaticRowsAndRoundTripsTheDocument()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var graph=new GLNodeGraph();var id=GLNodeId.random();var definition=new MeasurementRecorderNode();
            graph.edit("Recorder",e->e.addNode(new GLGraphNode(id,definition.registration().id(),definition.defaults())));
            c.openDocument("Labels",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty())));
            java.util.function.Supplier<GLNodeValue> table=()->c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("table");
            AudioAnalysis.publish(Map.of(id.toString(),frame(1,1,7,"0")));
            c.setNodeProperty(id,MeasurementRecorderNode.TABLE,MeasurementTable.withLabel(table.get(),0,"Sinus Ä"));
            AudioAnalysis.publish(Map.of(id.toString(),frame(1,2,7,"0")));
            c.setNodeProperty(id,MeasurementRecorderNode.TABLE,MeasurementTable.withLabel(table.get(),0,"Noise"));
            AudioAnalysis.publish(Map.of(id.toString(),frame(1,3,7,"0")));
            c.history().undo();assertEquals(3,MeasurementTable.rows(table.get()).size());assertEquals("Sinus Ä",MeasurementTable.label(table.get(),0));
            c.history().undo();assertEquals(3,MeasurementTable.rows(table.get()).size());assertEquals("",MeasurementTable.label(table.get(),0));
            c.history().redo();c.history().redo();assertEquals("Noise",MeasurementTable.label(table.get(),0));
            var codec=new KnackDocumentCodec();var bytes=new ByteArrayOutputStream();
            try{codec.write(c.snapshotDocument(),bytes);c.openDocument("Reload",codec.read(new ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));}
            catch(IOException ex){throw new java.io.UncheckedIOException(ex);}
            assertEquals(3,MeasurementTable.rows(table.get()).size());assertEquals("Noise",MeasurementTable.label(table.get(),0));
        }finally{AudioAnalysis.clear();}});
    }
    @Test void testCellsEditDirectlyAndKeepDraftsWhileCaptureContinues()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            var model=new java.util.concurrent.atomic.AtomicReference<GLNodeValue>(MeasurementTable.merge(MeasurementTable.empty(),frame(1,1,7,"0")));
            var panels=new MeasurementRecorderPanel[1];var panel=new MeasurementRecorderPanel(v->{model.set(v);panels[0].update(v);},true);panels[0]=panel;
            panel.setBounds(0,0,1000,650);panel.update(model.get());
            var grid=panelGrid(panel);var editor=(com.vingame.glswing.ui.GLTextEditor)grid.getGLChildren().get(0);
            java.util.function.Consumer<Integer> click=row->{
                var original=new java.awt.event.MouseEvent(new java.awt.Canvas(),java.awt.event.MouseEvent.MOUSE_PRESSED,0,0,300,42+row*28,1,false,java.awt.event.MouseEvent.BUTTON1);
                grid.mousePressed(new com.vingame.glswing.input.GLMouseEvent(original,new com.vingame.glswing.input.GLPickResult(1,grid,300,42+row*28,300,42+row*28,1,0,0,1,0,0),0,0));
            };
            click.accept(0);assertTrue(editor.isVisible());editor.setText("Test N");
            model.set(MeasurementTable.merge(model.get(),frame(1,2,7,"0")));panel.update(model.get());assertEquals("Test N",editor.getText());
            editor.commit();assertEquals("Test N",MeasurementTable.label(model.get(),0));assertEquals(2,MeasurementTable.rows(model.get()).size());
            click.accept(1);editor.setText("Discard");editor.cancel();assertEquals("",MeasurementTable.label(model.get(),1));assertFalse(editor.isVisible());
            click.accept(0);editor.setText("Stale");panel.update(MeasurementTable.clear());assertFalse(editor.isVisible());assertEquals("Test N",MeasurementTable.label(model.get(),0));
        });
    }
    private static com.vingame.glswing.component.GLContainer panelGrid(com.vingame.glswing.component.GLContainer parent){
        for(var container:parent.getGLChildren()){if("recorder-grid".equals(container.getName()))return container;try{return panelGrid(container);}catch(java.util.NoSuchElementException ignored){}}
        throw new java.util.NoSuchElementException("Recorder grid");
    }
    @Test void csvQuotesHeadersAndLeavesInvalidOrMissingCellsEmpty()throws Exception{
        var f=frame(2,1,7,"0");f.values()[16]=1; // only the first numeric input is valid
        var table=MeasurementTable.merge(MeasurementTable.empty(),f);var csv=new StringWriter();
        MeasurementTable.csv(table,new String[]{"Centroid, Hz","\"Spread\"","Missing"},csv,false);
        assertTrue(csv.toString().contains("\"Centroid, Hz\",\"\"\"Spread\"\"\",\"Missing\""));
        assertTrue(csv.toString().endsWith(",0.0,,\r\n"));
        var excel=new StringWriter();MeasurementTable.csv(table,new String[]{"Value"},excel,true);
        assertTrue(excel.toString().startsWith("\uFEFFmeasurement;test;session;time_s"));assertTrue(excel.toString().contains("0,0025625"));
    }
    @Test void captureSurvivesDocumentRoundTripAndUnrelatedUndoWithoutAddingHistory()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var graph=new GLNodeGraph();var id=GLNodeId.random();var d=new MeasurementRecorderNode();
            graph.edit("Recorder",e->e.addNode(new GLGraphNode(id,d.registration().id(),d.defaults())));
            c.openDocument("Measurements",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty())));
            assertEquals(1,c.workspace().editor().getNodes().size(),"Recorder view must be created even during zero-width initial layout");
            c.setNodeProperty(id,MeasurementRecorderNode.input(0),GLNodeValue.of(20.));
            AudioAnalysis.publish(Map.of(id.toString(),frame(1,2,7,"0")));
            assertTrue(c.history().isModified());assertEquals(2,MeasurementTable.rows(c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("table")).size());
            c.history().undo();assertEquals(2,MeasurementTable.rows(c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("table")).size());
            assertFalse(c.history().canUndo(),"Automatic rows must not create Undo commands");
            var saved=c.snapshotDocument();var codec=new KnackDocumentCodec();var bytes=new ByteArrayOutputStream();
            try{codec.write(saved,bytes);var loaded=codec.read(new ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
                c.openDocument("Reload",loaded);assertEquals(2,MeasurementTable.rows(c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("table")).size());
                var old=MeasurementTable.rows(c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("table")).size();
                AudioAnalysis.publish(Map.of(id.toString(),frame(1,2,7,"0")));assertEquals(old,MeasurementTable.rows(c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties().get("table")).size());
            }catch(IOException e){throw new java.io.UncheckedIOException(e);}
        }finally{AudioAnalysis.clear();}});
    }
}
