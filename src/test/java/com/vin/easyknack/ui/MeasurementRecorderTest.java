package com.vin.easyknack.ui;

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
    @Test void csvQuotesHeadersAndLeavesInvalidOrMissingCellsEmpty()throws Exception{
        var f=frame(2,1,7,"0");f.values()[16]=1; // only the first numeric input is valid
        var table=MeasurementTable.merge(MeasurementTable.empty(),f);var csv=new StringWriter();
        MeasurementTable.csv(table,new String[]{"Centroid, Hz","\"Spread\"","Missing"},csv,false);
        assertTrue(csv.toString().contains("\"Centroid, Hz\",\"\"\"Spread\"\"\",\"Missing\""));
        assertTrue(csv.toString().endsWith(",0.0,,\r\n"));
        var excel=new StringWriter();MeasurementTable.csv(table,new String[]{"Value"},excel,true);
        assertTrue(excel.toString().startsWith("\uFEFFmeasurement;session;time_s"));assertTrue(excel.toString().contains("0,0025625"));
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
