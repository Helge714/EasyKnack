package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class BackgroundPreparationTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
    void automaticallyWarmsHelpAndEditorWhileUiKeepsDispatching() throws Exception {
        var preparation=new BackgroundPreparation();
        var field=BackgroundPreparation.class.getDeclaredField("worker");field.setAccessible(true);
        var worker=(ExecutorService)field.get(preparation);
        try{
            SwingUtilities.invokeAndWait(()->{preparation.start();preparation.start();});
            var responsive=new CompletableFuture<Boolean>();
            SwingUtilities.invokeLater(()->responsive.complete(true));
            assertTrue(responsive.get(3,TimeUnit.SECONDS));
            worker.submit(()->{}).get(60,TimeUnit.SECONDS);
            var cache=FormulaEditorPreparation.class.getDeclaredField("prepared");cache.setAccessible(true);
            SwingUtilities.invokeAndWait(()->{try{
                var editor=cache.get(null);assertNotNull(editor,"Editor automatically prepared before first use");
                assertSame(editor,FormulaEditorPreparation.acquire());
                ((com.vingame.glswing.ui.GLCodeEditor)editor).clearDiagnosticProvider();
            }catch(IllegalAccessException failure){throw new AssertionError(failure);}});
            var shared=DocumentationPreparation.class.getDeclaredField("CACHE");shared.setAccessible(true);
            var entries=DocumentationPreparation.Cache.class.getDeclaredField("entries");entries.setAccessible(true);
            var pages=(java.util.Map<?,?>)entries.get(shared.get(null));
            assertTrue(pages.size()>200,"Both help languages automatically parsed before first use");
            assertTrue(pages.containsKey("nodes/de/oscillator.md"));
            assertTrue(pages.containsKey("nodes/en/oscillator.md"));
        } finally {preparation.close();assertTrue(worker.awaitTermination(5,TimeUnit.SECONDS));}
    }
    @Test void closeBeforeStartDoesNotSubmitWork() throws Exception {
        var preparation=new BackgroundPreparation();preparation.close();assertDoesNotThrow(preparation::start);
    }
}
