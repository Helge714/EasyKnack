package com.vin.easyknack.ui;

import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

class RecoveryTest {
    @TempDir Path directory;
    @Test void snapshotsAreAtomicRetainOriginalAndIgnoreActiveSessions()throws Exception{
        var document=GroupUnitsTest.document(true);var original=directory.resolve("original.knack");Files.writeString(original,"untouched");
        try(var writer=new RecoveryStore(directory);var reader=new RecoveryStore(directory)){
            writer.write(document,original);assertTrue(reader.scan(Instant.now()).isEmpty());
            assertThrows(IllegalArgumentException.class,()->writer.write(null,original));
            assertEquals("untouched",Files.readString(original));
        }
        try(var reader=new RecoveryStore(directory)){
            var entries=reader.scan(Instant.now());assertEquals(1,entries.size());var entry=entries.get(0);
            assertEquals(original,entry.original());assertEquals(document,entry.document());
            assertEquals(2,entry.document().embeddedAssets().size());
            reader.delete(entry);assertTrue(reader.scan(Instant.now()).isEmpty());
        }
    }
    @Test void untitledRecoveryExpiresAfterSevenDaysButActiveFilesDoNot()throws Exception{
        try(var writer=new RecoveryStore(directory);var reader=new RecoveryStore(directory)){
            writer.write(StarterDocument.create(),null);
            assertTrue(reader.scan(Instant.now().plus(Duration.ofDays(8))).isEmpty());
            try(var files=Files.list(directory)){assertEquals(1,files.filter(p->p.toString().endsWith(".recovery")).count());}
        }
        try(var reader=new RecoveryStore(directory)){
            assertNull(reader.scan(Instant.now()).get(0).original());
            assertTrue(reader.scan(Instant.now().plus(Duration.ofDays(8))).isEmpty());
            try(var files=Files.list(directory)){assertEquals(0,files.filter(p->p.toString().endsWith(".recovery")).count());}
        }
    }
    @Test void restoredDocumentSavesToOriginalAndUntitledStillNeedsSaveAs()throws Exception{
        org.junit.jupiter.api.Assumptions.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        var original=directory.resolve("project.knack");Files.writeString(original,"old content");
        var controller=new WorkspaceController[1];var actions=new DocumentActions[1];var frame=new JFrame[1];
        try{
            SwingUtilities.invokeAndWait(()->{
                controller[0]=new WorkspaceController();frame[0]=new JFrame();actions[0]=new DocumentActions(controller[0],new com.vingame.glswing.component.GLContainer(){protected void record(com.vingame.glswing.rendering.GLContext2D c){}},frame[0],s->{},directory);
                actions[0].restoreSnapshot(new RecoveryStore.Entry(directory.resolve("unused.recovery"),null,Instant.now(),StarterDocument.create()));
                assertNull(actions[0].currentPath());assertTrue(frame[0].getTitle().startsWith("* "));
                actions[0].restoreSnapshot(new RecoveryStore.Entry(directory.resolve("unused.recovery"),original,Instant.now(),StarterDocument.create()));
                assertEquals(original,actions[0].currentPath());
            });
            var saving=new CompletableFuture<Boolean>();SwingUtilities.invokeAndWait(()->actions[0].save(false).whenComplete((v,e)->{if(e!=null)saving.completeExceptionally(e);else saving.complete(v);}));
            assertTrue(saving.get(10,TimeUnit.SECONDS));
            try(var in=Files.newInputStream(original)){assertNotNull(new KnackDocumentCodec().read(in,new KnackDocumentCodec().currentSchemaVersion()));}
        }finally{SwingUtilities.invokeAndWait(()->{if(actions[0]!=null)actions[0].close();if(controller[0]!=null)controller[0].close();if(frame[0]!=null)frame[0].dispose();});}
    }
    @Test void mutedUnusedInputsFadeButBypassInputsRemainActive()throws Exception{
        var graph=new GLNodeGraph();var source=add(graph,new MathNode());var muted=add(graph,new MathNode());var target=add(graph,new MathNode());
        connect(graph,source,"audio-out",muted,"a");connect(graph,source,"audio-out",muted,"b");connect(graph,muted,"audio-out",target,"a");
        graph.edit("Mute",e->e.setProperty(muted,GLNodeState.MUTED,GLNodeValue.of(true)));
        var types=new GLNodeTypeRegistry();new NodeRegistry(types,new GLNodeCatalog());
        var diagnostics=MutedConnections.diagnostics(graph.snapshot(),types);assertEquals(1,diagnostics.size());
        var edge=graph.snapshot().findConnection(diagnostics.get(0).connectionIds().iterator().next()).orElseThrow();assertEquals("b",edge.input().portId());
    }
}
