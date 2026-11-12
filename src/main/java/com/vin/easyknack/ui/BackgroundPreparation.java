package com.vin.easyknack.ui;

import java.awt.EventQueue;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.jogamp.opengl.*;
import com.vin.easyknack.formula.FormulaLanguage;

/** Optional caches warm after the workspace appears, using a separate GL context. */
final class BackgroundPreparation implements AutoCloseable {
    private final AtomicBoolean started=new AtomicBoolean();
    private volatile boolean closed;
    private final ExecutorService worker=Executors.newSingleThreadExecutor(task->{
        var thread=new Thread(task,"EasyKnack background preparation");
        thread.setDaemon(true);thread.setPriority(Thread.MIN_PRIORITY);return thread;
    });
    void start(){
        if(!closed&&started.compareAndSet(false,true))worker.execute(this::prepare);
    }
    private void prepare(){
        GLOffscreenAutoDrawable drawable=null;
        boolean current=false;
        try{
            new FormulaLanguage(List.of(),3).diagnose("double seconds=(double)samplePosition/sampleRate;");
            if(closed)return;
            try{
                var profile=GLProfile.get(GLProfile.GL3);var capabilities=new GLCapabilities(profile);
                capabilities.setOnscreen(false);capabilities.setFBO(true);
                drawable=GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,capabilities,null,32,32);
                drawable.display();
                current=drawable.getContext().makeCurrent()!=GLContext.CONTEXT_NOT_CURRENT;
                if(!current)throw new IllegalStateException("Background GL context unavailable");
                FormulaEditorPreparation.prepareFont(drawable.getGL().getGL3());
            }catch(RuntimeException unavailable){
                System.err.println("Background font preparation unavailable: "+unavailable.getMessage());
            }
            if(closed)return;
            EventQueue.invokeLater(()->{if(!closed)FormulaEditorPreparation.prepare();});
            DocumentationPreparation.prepare(title->{},(done,total)->{});
            if(!closed&&current)DocumentationPreparation.prepareFonts(drawable.getGL().getGL3(),(done,total)->{});
        }catch(RuntimeException failure){
            if(!closed)System.err.println("Background preparation unavailable: "+failure.getMessage());
        }finally{
            if(drawable!=null){
                try{if(current)drawable.getContext().release();}
                finally{drawable.destroy();}
            }
        }
    }
    @Override public void close(){closed=true;worker.shutdownNow();}
}
