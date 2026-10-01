package com.vin.easyknack.audio;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AudioHostServiceTest {
    @Test void closingClearsLiveAnalysisOnTheUiThread()throws Exception{
        var service=new AudioHostService(false);
        javax.swing.SwingUtilities.invokeAndWait(()->AudioAnalysis.publish(java.util.Map.of("monitor",new AudioAnalysis.Frame(440,1,0,new float[]{440}))));
        service.close();
        javax.swing.SwingUtilities.invokeAndWait(()->assertNull(AudioAnalysis.frame("monitor")));
    }
    @Test @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void executableAndGraphSwitchTogetherAndKeepRequestedPlayState()throws Exception{
        var directory=java.nio.file.Files.createTempDirectory(java.nio.file.Path.of("target"),"bake-host-switch-");
        var copy=directory.resolve("host.exe").toAbsolutePath();java.nio.file.Files.copy(AudioHostClient.defaultExecutable(),copy);
        try(var service=new AudioHostService(false,true)){
            service.update("GRAPH 1 out out -1 0 @ a a");service.start();service.tick();
            var field=AudioHostService.class.getDeclaredField("client");field.setAccessible(true);var previous=field.get(service);
            service.update("GRAPH 1 out out -1 0 @ a a",copy);service.tick();
            assertEquals(AudioHostService.State.RUNNING,service.state());assertNotSame(previous,field.get(service));
            var baked=field.get(service);service.invalidGraph("Editing");service.tick();
            assertSame(baked,field.get(service));assertEquals(AudioHostService.State.PAUSED,service.state());
            service.update("GRAPH 0");service.tick();assertNotSame(baked,field.get(service));
            assertEquals(AudioHostService.State.RUNNING,service.state());
        }
    }
    @Test @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void invalidGraphsPauseAndResumeWithoutRestartingTheProcess() throws Exception {
        try(var service=new AudioHostService(false,true)) {
            service.update("GRAPH 1 out out -1 0 @ a a"); service.start(); service.tick();
            assertEquals(AudioHostService.State.RUNNING,service.state());
            var field=AudioHostService.class.getDeclaredField("client");field.setAccessible(true);
            var client=(AudioHostClient)field.get(service);
            service.update("GRAPH 1 bad unknown @ a -"); service.tick();
            assertEquals(AudioHostService.State.PAUSED,service.state());
            assertSame(client,field.get(service)); assertTrue(client.command("PING").startsWith("OK "));
            service.tick(); assertEquals(AudioHostService.State.PAUSED,service.state());
            service.update("GRAPH 1 out out -1 0 @ a a"); service.tick();
            assertEquals(AudioHostService.State.RUNNING,service.state());
            service.invalidGraph("Invalid connection"); service.tick();
            assertEquals(AudioHostService.State.PAUSED,service.state());
            service.stop();service.tick();
            service.update("GRAPH 0");service.tick();
            assertEquals(AudioHostService.State.STOPPED,service.state());
            assertSame(client,field.get(service));
        }
    }
    @Test void startAndStopAreGuardedWhileTransitioning() {
        var service=new AudioHostService(false);
        assertEquals(AudioHostService.State.STOPPED,service.state());
        service.start(); service.start();
        assertEquals(AudioHostService.State.STARTING,service.state());
        service.stop(); service.start(); service.stop();
        assertEquals(AudioHostService.State.STOPPING,service.state());
        service.close(); service.start();
        assertEquals(AudioHostService.State.CLOSED,service.state());
    }
}

