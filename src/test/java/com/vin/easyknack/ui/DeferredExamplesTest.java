package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.concurrent.*;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.ui.GLPopupMenu;

class DeferredExamplesTest {
    @TempDir Path directory;
    @Test void slowExamplesDoNotBlockUiAndRefreshPublishesAfterLoading() throws Exception {
        String previous=System.getProperty("easyknack.examples");
        System.setProperty("easyknack.examples",directory.resolve("examples").toString());
        var controller=new WorkspaceController[1];var documents=new DocumentActions[1];
        var frame=new JFrame[1];var menu=new GLPopupMenu[1];
        var release=new CountDownLatch(1);var started=new CountDownLatch(1);
        ExecutorService worker=null;
        try {
            SwingUtilities.invokeAndWait(()->{
                controller[0]=new WorkspaceController();frame[0]=new JFrame();
                var owner=new GLContainer(){protected void record(com.vingame.glswing.rendering.GLContext2D c){}};
                documents[0]=new DocumentActions(controller[0],owner,frame[0],s->{},directory.resolve("recovery"));
                menu[0]=new GLPopupMenu();
            });
            var field=DocumentActions.class.getDeclaredField("examplesWorker");field.setAccessible(true);
            worker=(ExecutorService)field.get(documents[0]);
            worker.submit(()->{started.countDown();try{release.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}});
            assertTrue(started.await(5,TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(()->{
                documents[0].populateExamples(menu[0],()->{});
                assertEquals(1,menu[0].getItems().size());
                assertEquals(AppText.text("examples.loading"),menu[0].getItems().get(0).getText());
                assertFalse(menu[0].getItems().get(0).isEnabled());
                assertFalse(Files.exists(directory.resolve("examples")),"No file generation on the UI thread");
            });
            release.countDown();worker.submit(()->{}).get(30,TimeUnit.SECONDS);
            SwingUtilities.invokeAndWait(()->{
                assertTrue(menu[0].getItems().stream().anyMatch(item->item.getText().equals("Spectral")));
                assertTrue(menu[0].getItems().stream().anyMatch(item->item.getText().equals(AppText.text("examples.refresh"))));
                assertTrue(Files.exists(directory.resolve("examples/Spectral/Stochastic Bin Width.knack")));
            });
        } finally {
            release.countDown();
            SwingUtilities.invokeAndWait(()->{if(documents[0]!=null)documents[0].close();if(controller[0]!=null)controller[0].close();if(frame[0]!=null)frame[0].dispose();});
            if(previous==null)System.clearProperty("easyknack.examples");else System.setProperty("easyknack.examples",previous);
        }
    }
}
