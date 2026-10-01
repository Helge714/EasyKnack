package com.vin.easyknack.formula;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Debounced background builds. Stale completions never replace a newer draft. */
public final class FormulaLiveSession implements AutoCloseable {
    public enum Phase { WAITING, COMPILING, READY, ERROR }
    public record Compiled(FormulaCompiler.Program program,FormulaNativeCompiler.Result nativeCode){}
    public record Status(long revision,Phase phase,Compiled active,String error){}
    @FunctionalInterface public interface Builder {FormulaNativeCompiler.Result compile(FormulaCompiler.Program program)throws Exception;}
    private final ScheduledExecutorService worker;
    private final Executor delivery;
    private final Builder builder;
    private final Consumer<Status> listener;
    private ScheduledFuture<?> pending;
    private long revision;
    private boolean closed;
    private Compiled active;
    public FormulaLiveSession(Builder builder,Executor delivery,Consumer<Status> listener){
        this.builder=Objects.requireNonNull(builder);this.delivery=Objects.requireNonNull(delivery);this.listener=Objects.requireNonNull(listener);
        worker=Executors.newSingleThreadScheduledExecutor(task->{var thread=new Thread(task,"easyknack-formula-build");thread.setDaemon(true);return thread;});
    }
    /** For Swing callers, use EventQueue::invokeLater as the delivery executor. */
    public synchronized long submit(String source,List<FormulaCompiler.Port> ports,long debounceMillis){return submit(source,ports,debounceMillis,2);} public synchronized long submit(String source,List<FormulaCompiler.Port> ports,long debounceMillis,int version){
        if(closed)throw new IllegalStateException("Formula session is closed");
        if(debounceMillis<0)throw new IllegalArgumentException("Negative debounce");
        long generation=++revision;if(pending!=null)pending.cancel(false);
        var snapshot=List.copyOf(ports);publish(generation,Phase.WAITING,"");
        pending=worker.schedule(()->build(generation,source,snapshot,version),debounceMillis,TimeUnit.MILLISECONDS);return generation;
    }
    private void build(long generation,String source,List<FormulaCompiler.Port> ports,int version){
        try{
            var program=FormulaCompiler.compile(source,ports,version);
            synchronized(this){if(closed||revision!=generation)return;publish(generation,Phase.COMPILING,"");}
            var compiled=new Compiled(program,builder.compile(program));
            synchronized(this){if(closed||revision!=generation)return;active=compiled;publish(generation,Phase.READY,"");}
        }catch(InterruptedException e){Thread.currentThread().interrupt();}
        catch(Exception e){synchronized(this){if(!closed&&revision==generation)publish(generation,Phase.ERROR,e.getMessage()==null?e.toString():e.getMessage());}}
    }
    private void publish(long generation,Phase phase,String error){
        var status=new Status(generation,phase,active,error);
        delivery.execute(()->{synchronized(this){if(closed||revision!=generation)return;listener.accept(status);}});
    }
    public synchronized Compiled active(){return active;}
    @Override public synchronized void close(){if(closed)return;closed=true;revision++;if(pending!=null)pending.cancel(true);worker.shutdownNow();}
}
