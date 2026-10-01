package com.vin.easyknack.formula;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.ProcessingRate;

class FormulaLiveSessionTest {
    final List<FormulaCompiler.Port> ports=List.of(FormulaCompilerTest.number("signal",false),FormulaCompilerTest.number("result",true));
    @Test void schedulingRetainsIndividualRates(){
        var program=FormulaCompiler.compile("result=signal;",ports);
        assertEquals(ProcessingRate.CONTROL,FormulaRatePlan.resolve(program,ProcessingRate.AUTO,Map.of()).execution());
        assertEquals(ProcessingRate.AUDIO,FormulaRatePlan.resolve(program,ProcessingRate.AUTO,Map.of("signal",ProcessingRate.AUDIO)).execution());
        assertThrows(IllegalArgumentException.class,()->FormulaRatePlan.resolve(program,ProcessingRate.CONTROL,Map.of("signal",ProcessingRate.AUDIO)));
        var list=List.of(FormulaCompilerTest.number("signal",false),FormulaCompilerTest.trigger("clock",false),FormulaCompilerTest.number("result",true));
        var triggered=FormulaCompiler.compile("if(clock) result=signal;else result=0;",list);
        var rates=FormulaRatePlan.resolve(triggered,ProcessingRate.AUTO,Map.of());assertEquals(ProcessingRate.AUDIO,rates.execution());assertEquals(ProcessingRate.CONTROL,rates.inputs().get("signal"));
    }
    @Test void outdatedBuildAndInvalidDraftCannotReplaceValidCode()throws Exception{
        var firstStarted=new CountDownLatch(1);var unblock=new CountDownLatch(1);var statuses=new LinkedBlockingQueue<FormulaLiveSession.Status>();
        try(var session=new FormulaLiveSession(program->{if(program.cpp().contains("1.0f")){firstStarted.countDown();assertTrue(unblock.await(5,TimeUnit.SECONDS));}return new FormulaNativeCompiler.Result(Path.of("test.dll"),false,1);},Runnable::run,statuses::add)){
            session.submit("result=1;",ports,0);assertTrue(firstStarted.await(5,TimeUnit.SECONDS));
            long newest=session.submit("result=2;",ports,0);unblock.countDown();
            var ready=await(statuses,FormulaLiveSession.Phase.READY);assertEquals(newest,ready.revision());assertTrue(ready.active().program().cpp().contains("2.0f"));
            session.submit("result=unknown;",ports,0);var failed=await(statuses,FormulaLiveSession.Phase.ERROR);
            assertSame(ready.active(),failed.active());assertSame(ready.active(),session.active());
        }finally{unblock.countDown();}
    }
    private static FormulaLiveSession.Status await(BlockingQueue<FormulaLiveSession.Status> queue,FormulaLiveSession.Phase phase)throws Exception{
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
        while(System.nanoTime()<deadline){var status=queue.poll(100,TimeUnit.MILLISECONDS);if(status!=null&&status.phase()==phase)return status;}
        throw new AssertionError("Missing status "+phase);
    }
}
