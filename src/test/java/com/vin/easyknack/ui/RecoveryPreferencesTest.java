package com.vin.easyknack.ui;
import com.vin.easyknack.document.RecoveryStore;

import java.nio.file.*;
import java.time.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RecoveryPreferencesTest {
    @TempDir Path directory;
    @Test void customRetentionIsUsedInsteadOfFixedSevenDays()throws Exception{
        try(var writer=new RecoveryStore(directory)){writer.write(StarterDocument.create(),null);}
        try(var reader=new RecoveryStore(directory)){
            assertEquals(1,reader.scan(Instant.now().plus(Duration.ofDays(8)),Duration.ofDays(14)).size());
            assertTrue(reader.scan(Instant.now().plus(Duration.ofDays(8)),Duration.ofDays(2)).isEmpty());
        }
    }
    @Test void backgroundPreparationUsesDetachedGraphAndPreservesNestedGroups()throws Exception{
        var captured=new com.vingame.glswing.node.document.GLNodeGraphDocument[1];
        var expected=new com.vingame.glswing.node.document.GLNodeGraphDocument[1];
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Snapshot",GroupUnitsTest.document(true));
            expected[0]=c.snapshotDocument();captured[0]=c.captureDocument();
            c.workspace().currentGraph().edit("Remove group",e->e.removeNode(GroupUnitsTest.GROUP));
        }});
        var result=CompletableFuture.supplyAsync(()->WorkspaceController.prepareDocumentSnapshot(captured[0])).get(10,TimeUnit.SECONDS);
        assertEquals(expected[0],result);assertEquals(2,result.embeddedAssets().size());
        assertTrue(result.root().graph().findNode(GroupUnitsTest.GROUP).isPresent());
    }
    @Test void curvatureIncludesStraightLinesAndRestoresThemeHandles()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            var editor=new com.vingame.glswing.node.GLNodeEditor();var style=editor.getGLStyle();
            ConnectionAppearance.apply(editor,0);assertEquals(0,editor.getConnectionHandleFactor());assertEquals(0,editor.getMinimumConnectionHandleLength());
            ConnectionAppearance.apply(editor,.5);assertEquals(style.connectionHandleFactor()*.5,editor.getConnectionHandleFactor(),1e-6);assertEquals(style.minimumConnectionHandleLength()*.5,editor.getMinimumConnectionHandleLength(),1e-6);
            ConnectionAppearance.apply(editor,1);assertEquals(style.connectionHandleFactor(),editor.getConnectionHandleFactor());assertEquals(style.minimumConnectionHandleLength(),editor.getMinimumConnectionHandleLength());
            assertThrows(IllegalArgumentException.class,()->ConnectionAppearance.apply(editor,Double.NaN));
        });
    }
    @Test void invalidPreferencesDoNotWriteSettings(){
        assertThrows(IllegalArgumentException.class,()->ApplicationSettings.save("en","",0,7,1));
        assertThrows(IllegalArgumentException.class,()->ApplicationSettings.save("en","",30,0,1));
        assertThrows(IllegalArgumentException.class,()->ApplicationSettings.save("en","",30,7,Double.NaN));
    }
}
