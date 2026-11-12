package com.vin.easyknack.audio;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class AudioHostShutdownTest {
    @Test void forcedShutdownWaitsForTerminationBeforeReturning() throws Exception {
        class Child extends Process {
            boolean alive=true,killed;int waits;
            public OutputStream getOutputStream(){return OutputStream.nullOutputStream();}
            public InputStream getInputStream(){return InputStream.nullInputStream();}
            public InputStream getErrorStream(){return InputStream.nullInputStream();}
            public int waitFor(){alive=false;return 0;}
            public boolean waitFor(long timeout,TimeUnit unit){waits++;if(killed)alive=false;return !alive;}
            public int exitValue(){if(alive)throw new IllegalThreadStateException();return 0;}
            public void destroy(){killed=true;}
            public Process destroyForcibly(){killed=true;return this;}
            public boolean isAlive(){return alive;}
        }
        var child=new Child();var client=new AudioHostClient();
        var field=AudioHostClient.class.getDeclaredField("process");field.setAccessible(true);field.set(client,child);
        client.close();assertTrue(child.killed);assertFalse(child.isAlive());assertEquals(2,child.waits);
        client.close();assertEquals(2,child.waits,"Repeated close remains harmless");
    }
}
