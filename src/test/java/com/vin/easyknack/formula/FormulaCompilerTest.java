package com.vin.easyknack.formula;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class FormulaCompilerTest {
    @Test void compoundAssignmentsMatchExplicitArithmetic(){
        String shorthand="state float phase=0; float value=signal; value+=gain*2; value-=1; value*=value+2; value/=3; phase+=value/sampleRate; result=phase; result*=gain;";
        String expanded="state float phase=0; float value=signal; value=value+gain*2; value=value-1; value=value*(value+2); value=value/3; phase=phase+value/sampleRate; result=phase; result=result*gain;";
        var actual=FormulaCompiler.compile(shorthand,ports);var expected=FormulaCompiler.compile(expanded,ports);
        assertEquals(expected.bytecode(),actual.bytecode());assertEquals(expected.cpp(),actual.cpp());assertEquals(expected.usedInputs(),actual.usedInputs());
        assertTrue(new FormulaLanguage(ports).diagnose(shorthand).isEmpty());
        for(String code:List.of("result+=1;","if(clock)result=1;result*=2;","signal+=1;result=0;","float x+=1;result=x;","state bool b=true;b*=false;result=0;","float x=0;x+=clock;result=x;"))assertThrows(FormulaCompiler.Failure.class,()->FormulaCompiler.compile(code,ports),code);
    }
    @Test void persistentVariablesRequireBoundedTopLevelLiteralDeclarations(){
        var program=FormulaCompiler.compile("state float phase=0; state bool enabled=true; if(clock) enabled=false; result=sin(tau*phase); phase=phase+signal/sampleRate;phase=phase-floor(phase);",ports);
        assertEquals(2,program.states().size());assertTrue(program.sampleContext());assertTrue(program.bytecode().startsWith("FORM2 "));
        assertTrue(program.cpp().contains("context.state[0]"));
        for(String code:List.of("state float phase=signal; result=0;","if(clock){state float phase=0;}result=0;","result=0;state float phase=0;","state float phase=1e999;result=0;","state bool enabled=1;result=0;","state float phase=0;state float phase=1;result=0;"))assertThrows(FormulaCompiler.Failure.class,()->FormulaCompiler.compile(code,ports),code);
        assertThrows(FormulaCompiler.Failure.class,()->FormulaCompiler.compile(java.util.stream.IntStream.range(0,65).mapToObj(i->"state float s"+i+"=0;").collect(java.util.stream.Collectors.joining())+"result=0;",ports));
    }
    @Test void typedMembersAndVersionedBufferReads(){
        var p=List.of(new FormulaCompiler.Port("b","buffer",FormulaCompiler.Type.BUFFER,false),number("signal",false),trigger("clock",false),number("result",true));
        String code="result=buffer.read(buffer.end-1,Clamp,Linear)+buffer.begin+buffer.writePosition+buffer.sampleRate+signal.value; if(clock.active && signal.rate==Audio) result=0;";
        assertDoesNotThrow(()->FormulaCompiler.compile(code,p));
        assertTrue(FormulaCompiler.compile("result=buffer.read(0);",p,1).cpp().contains("context.read("));
        assertTrue(FormulaCompiler.compile("result=buffer.read(0);",p,2).cpp().contains("context.readTimeline("));
        for(String bad:List.of("result=signal.read(0);","result=clock.value;","result=signal.active;","result=buffer.read(0,Wrong,Linear);","result=buffer.read(0,Clamp,Wrong);"))assertThrows(FormulaCompiler.Failure.class,()->FormulaCompiler.compile(bad,p));
    }
    @Test void incompleteTypingProducesValidDiagnosticsIncludingEmptyAndEndOfFile(){
        var language=new FormulaLanguage(ports);
        String complete="if(clock){result=sqrt(abs(signal));}else{result=gain;}";
        for(int i=0;i<=complete.length();i++){
            String draft=complete.substring(0,i);
            var diagnostics=assertDoesNotThrow(()->language.diagnose(draft),draft);
            for(var d:diagnostics){assertTrue(d.start()>=0);assertTrue(d.end()>d.start());assertTrue(d.end()<=Math.max(1,draft.length()));}
        }
        assertFalse(language.diagnose("").isEmpty());
        assertFalse(language.diagnose("result=signal").isEmpty());
        assertTrue(language.diagnose(complete).isEmpty());
    }
    @Test void contextAndBufferSyntaxAreTypedAndRateAware(){
        var ports=List.of(new FormulaCompiler.Port("buffer","buffer",FormulaCompiler.Type.BUFFER,false),number("result",true));
        var p=FormulaCompiler.compile("result=buffer.read(sampleIndex-1,true,false)+buffer.size+rand(-1,1,42)+samplePosition/sampleRate+blockSize;",ports);
        assertTrue(p.sampleContext());assertEquals(Set.of("buffer"),p.usedInputs());
        assertEquals(com.vin.easyknack.nodes.ProcessingRate.AUDIO,FormulaRatePlan.resolve(p,com.vin.easyknack.nodes.ProcessingRate.AUTO,Map.of()).execution());
        assertEquals(com.vin.easyknack.nodes.ProcessingRate.CONTROL,FormulaRatePlan.resolve(p,com.vin.easyknack.nodes.ProcessingRate.CONTROL,Map.of()).execution());
        for(String code:List.of("result=buffer;","result=buffer.read(0,1);","result=buffer.read(true);","result=buffer.nope;","sampleIndex=1;result=0;","float blockSize=1;result=0;","result=rand(1);"))assertThrows(FormulaCompiler.Failure.class,()->FormulaCompiler.compile(code,ports),code);
    }
    static FormulaCompiler.Port number(String name,boolean out){return new FormulaCompiler.Port(name,name,FormulaCompiler.Type.NUMBER,out);}
    static FormulaCompiler.Port trigger(String name,boolean out){return new FormulaCompiler.Port(name,name,FormulaCompiler.Type.BOOLEAN,out);}
    final List<FormulaCompiler.Port> ports=List.of(number("signal",false),number("gain",false),trigger("clock",false),number("result",true),trigger("fire",true));
    @Test void compilesBranchesMathAndTriggers(){
        var p=FormulaCompiler.compile("float value = signal * gain; if (clock && value > 0) { result = sqrt(abs(value)); fire = true; } else { result = -value; }",ports);
        assertEquals(Set.of("signal","gain","clock"),p.usedInputs());assertTrue(p.cpp().contains("std::sqrt"));assertTrue(p.cpp().startsWith("output[1] = 0.0f;"));assertFalse(p.cpp().contains("signal"));
    }
    @Test void rejectsUndefinedValuesTypesAndUnsafeSyntax(){
        for(String code:List.of("if (clock) result = signal;","result = missing;","signal = 0; result = 0;","result = clock;","fire = signal; result=0;","while (true) {}","result = std::sqrt(signal);","float x = x; result=x;","if(clock) float x=0; result=x;","result = 1e999;","result = sqrt(1,2);","result = 0; #include <x>"))assertThrows(FormulaCompiler.Failure.class,()->FormulaCompiler.compile(code,ports),code);
    }
    @Test void branchIntersectionAndShadowing(){
        assertDoesNotThrow(()->FormulaCompiler.compile("float x=0; if(clock) x=1; else x=2; result=x;",ports));
        assertThrows(FormulaCompiler.Failure.class,()->FormulaCompiler.compile("float signal=2;result=signal;",ports));
        assertDoesNotThrow(()->FormulaCompiler.compile("if(clock){result=1;}else if(!clock){result=2;}else{result=3;}",ports));
    }
    @Test void precedenceAndLimits(){
        var p=FormulaCompiler.compile("result = signal + gain * 2;",ports);assertTrue(p.cpp().contains("(input[0] + (input[1] * 2.0f))"));assertEquals(Set.of("signal","gain"),p.usedInputs());
        assertThrows(FormulaCompiler.Failure.class,()->FormulaCompiler.compile("result="+"(".repeat(200)+"0"+")".repeat(200)+";",ports));
    }
    @Test void codeEditorDiagnosticsUseCompiler(){
        var language=new FormulaLanguage(ports);assertTrue(language.diagnose("result=signal;").isEmpty());assertFalse(language.diagnose("result=clock;").isEmpty());assertFalse(language.tokenize("// test\nfloat x=2;").isEmpty());
    }
}
