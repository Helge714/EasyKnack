package com.vin.easyknack.ui;

import java.awt.EventQueue;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.jogamp.opengl.*;
import com.vin.easyknack.formula.FormulaLanguage;

/** Optional caches warm after the workspace appears, using a separate GL context. */
final class BackgroundPreparation implements AutoCloseable {
    private static volatile BackgroundPreparation active;
    private final CompletableFuture<Void> formulaReady=new CompletableFuture<>();
    private final CompletableFuture<Void> documentationReady=new CompletableFuture<>();
    private final AtomicBoolean started=new AtomicBoolean();
    BackgroundPreparation(){active=this;}
    static CompletionStage<Void> formulaReady(){var current=active;return current==null?CompletableFuture.completedFuture(null):current.formulaReady;}
    static CompletionStage<Void> documentationReady(){var current=active;return current==null?CompletableFuture.completedFuture(null):current.documentationReady;}
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
        boolean current=false, formulaScheduled=false;
        try{
            new FormulaLanguage(List.of(),3).diagnose("double seconds=(double)samplePosition/sampleRate;");
            if(closed)return;
            var profile=GLProfile.get(GLProfile.GL3);var capabilities=new GLCapabilities(profile);
            capabilities.setOnscreen(false);capabilities.setFBO(true);
            drawable=GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,capabilities,null,32,32);
            drawable.display();
            current=drawable.getContext().makeCurrent()!=GLContext.CONTEXT_NOT_CURRENT;
            if(!current)throw new IllegalStateException("Background GL context unavailable");
            FormulaEditorPreparation.prepareFont(drawable.getGL().getGL3());
            if(closed)return;
            EventQueue.invokeLater(()->{
                if(closed)return;
                try{FormulaEditorPreparation.prepare();formulaReady.complete(null);}
                catch(RuntimeException failure){formulaReady.completeExceptionally(failure);}
            });
            formulaScheduled=true;
            DocumentationPreparation.prepare(title->{},(done,total)->{});
            if(!closed){
                DocumentationPreparation.prepareFonts(drawable.getGL().getGL3(),(done,total)->{});
                documentationReady.complete(null);
            }
        }catch(RuntimeException | LinkageError failure){
            if(!formulaScheduled)formulaReady.completeExceptionally(failure);
            documentationReady.completeExceptionally(failure);
            if(!closed)System.err.println("Background preparation unavailable: "+failure.getMessage());
        }finally{
            if(drawable!=null){
                try{if(current)drawable.getContext().release();}
                finally{drawable.destroy();}
            }
        }
    }
    @Override public void close(){
        closed=true;worker.shutdownNow();
        var cancelled=new CancellationException("Workspace closed");
        formulaReady.completeExceptionally(cancelled);documentationReady.completeExceptionally(cancelled);
        if(active==this)active=null;
    }
}
