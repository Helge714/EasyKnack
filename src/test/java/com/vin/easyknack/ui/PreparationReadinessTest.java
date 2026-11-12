package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.node.composite.GLCompositeGraphRegistry;
import com.vingame.glswing.ui.GLLabel;

class PreparationReadinessTest {
    @Test void helpKeepsLoadingUntilReadyAndThenShowsTheLatestRequest() throws Exception {
        var ready=new CompletableFuture<Void>();var panel=new DocumentationViewer[1];
        var requests=new CompletableFuture<?>[2];
        SwingUtilities.invokeAndWait(()->{
            panel[0]=new DocumentationViewer(()->{},ready);
            requests[0]=panel[0].openPage("nodes/en/oscillator.md");
            requests[1]=panel[0].openPage("nodes/en/additive-oscillator.md");
            assertEquals(AppText.text("help.loading"),panel[0].status.getText());
            assertTrue(panel[0].content.document().blocks().isEmpty());
            assertFalse(requests[1].isDone());
        });
        ready.complete(null);
        requests[0].get(10,TimeUnit.SECONDS);requests[1].get(10,TimeUnit.SECONDS);
        SwingUtilities.invokeAndWait(()->{
            assertTrue(panel[0].position().page().endsWith("/additive-oscillator.md"));
            assertFalse(panel[0].content.document().blocks().isEmpty());
            assertFalse(panel[0].status.getText().equals(AppText.text("help.loading")));
        });
    }
    @Test void formulaWaitsWithoutCreatingAnEditorAndUsesTheLatestSavedDraft() throws Exception {
        var ready=new CompletableFuture<Void>();
        SwingUtilities.invokeAndWait(()->{
            var fixture=FormulaGroupsTest.fixture();var groups=new GLCompositeGraphRegistry();
            groups.registerAsset(fixture.asset());
            try(var runtime=new FormulaRuntime(()->{})){
                var panel=new FormulaEditorPanel(fixture.asset().id(),groups,new GLHistoryManager(),runtime,()->{},ready);
                assertNull(panel.code);
                assertEquals(AppText.text("formula.preparing"),((GLLabel)panel.getGLChildren().get(0)).getText());
                panel.apply();panel.refresh();assertNull(panel.code);
                groups.replaceAsset(FormulaGroups.accept(fixture.asset(),"result = signal * 4;"));
                ready.complete(null);
                assertNotNull(panel.code);assertEquals("result = signal * 4;",panel.code.getText());
                panel.close();
            }
        });
    }
    @Test void closingWhileLoadingDoesNotCreateAnEditorLater() throws Exception {
        var ready=new CompletableFuture<Void>();
        SwingUtilities.invokeAndWait(()->{
            var fixture=FormulaGroupsTest.fixture();var groups=new GLCompositeGraphRegistry();groups.registerAsset(fixture.asset());
            try(var runtime=new FormulaRuntime(()->{})){
                var panel=new FormulaEditorPanel(fixture.asset().id(),groups,new GLHistoryManager(),runtime,()->{},ready);
                panel.close();ready.complete(null);assertNull(panel.code);
            }
        });
    }
    @Test void preparationFailureEndsTheLoadingState() throws Exception {
        var ready=new CompletableFuture<Void>();var help=new DocumentationViewer[1];var request=new CompletableFuture<?>[1];
        SwingUtilities.invokeAndWait(()->{
            help[0]=new DocumentationViewer(()->{},ready);request[0]=help[0].openPage("nodes/en/oscillator.md");
            var fixture=FormulaGroupsTest.fixture();var groups=new GLCompositeGraphRegistry();groups.registerAsset(fixture.asset());
            try(var runtime=new FormulaRuntime(()->{})){
                var panel=new FormulaEditorPanel(fixture.asset().id(),groups,new GLHistoryManager(),runtime,()->{},ready);
                ready.completeExceptionally(new IllegalStateException("Preparation unavailable"));
                assertNull(panel.code);
                assertEquals(AppText.text("preparation.failed"),((GLLabel)panel.getGLChildren().get(0)).getText());
                panel.close();
            }
        });
        assertThrows(ExecutionException.class,()->request[0].get(5,TimeUnit.SECONDS));
        SwingUtilities.invokeAndWait(()->assertFalse(help[0].status.getText().equals(AppText.text("help.loading"))));
    }
}
