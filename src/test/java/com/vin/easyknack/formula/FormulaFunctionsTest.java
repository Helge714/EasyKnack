package com.vin.easyknack.formula;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class FormulaFunctionsTest {
    static final List<FormulaCompiler.Port> PORTS=List.of(FormulaCompilerTest.number("signal",false),FormulaCompilerTest.number("result",true));
    static FormulaCompiler.Program compile(String source){return FormulaCompiler.compile(source,PORTS,3);}
    @Test void finalAndConstHaveDistinctDependenciesAndCannotBeReassigned(){
        for(String code:List.of(
            "final float gain=signal*2;result=gain;",
            "const float lo=0.5;const float hi=lo*4;result=hi;",
            "const double angle=sin(pi/2);result=angle;",
            "float f(final float x){final float y=x*2;return y;}final float x=1;result=f(x);",
            "result=0;if(signal>0){const float x=2;result=x;}float x=3;x+=1;result+=x;",
            "result=0;for(int i=0;i<3;i++){final float x=i;result+=x;}float x=1;x=2;result+=x;"))assertDoesNotThrow(()->compile(code),code);
        for(String code:List.of(
            "final float x=1;x=2;result=x;", "const float x=1;x+=2;result=x;",
            "const float x=signal;result=x;", "final float x=1;const float y=x;result=y;",
            "float x=1;const float y=x;result=y;", "const double x=sampleRate;result=x;",
            "const float x=rand(0,1);result=x;", "const float x=sineWave(0);result=x;",
            "state final float x=0;result=x;", "const final float x=1;result=x;", "final const float x=1;result=x;",
            "float f(final float x){x+=1;return x;}result=f(signal);",
            "float f(const float x){return x;}result=f(signal);",
            "float f(float x){const float y=x;return y;}result=f(signal);"))assertThrows(FormulaCompiler.Failure.class,()->compile(code),code);
        assertTrue(FormulaCompiler.compile("const float x=2;result=x;",PORTS,2).bytecode().startsWith("FORM3"));
        assertTrue(compile("final float gain=signal;result=gain;").usedInputs().contains(PORTS.get(0).id()));
    }
    @Test void functionsSupportForwardCallsBranchesAndLexicalParameters(){
        for(String source:List.of(
            "float square(float x){return x*x;} result=square(signal);",
            "result=first(signal); float first(float x){return second(x);} float second(float y){if(y>0) return y; else return -y;}",
            "float sum(int n){float result=0; for(int i=0;i<4;i++) result+=i; return result+n;} result=sum(2);",
            "float f(float signal){return signal*2.0;} result=f(3);",
            "float f(float x){for(int i=0;i<0;i++)return 3;return x;} result=f(signal);"))
            assertDoesNotThrow(()->compile(source),source);
        assertTrue(FormulaCompiler.compile("float f(float x){return x;} result=f(signal);",PORTS,2).bytecode().startsWith("FORM3"));
    }
    @Test void invalidFunctionsDoNotHideInsideUnusedDefinitions(){
        for(String source:List.of(
            "float f(float x){if(x>0)return x;} result=0;",
            "float f(float x){return f(x);} result=0;",
            "float f(float x){return g(x);} float g(float y){return f(y);} result=0;",
            "float f(float x){result=x;return x;} result=0;",
            "float f(float x){return missing;} result=0;",
            "float f(float x){return x;} result=f();"))assertThrows(FormulaCompiler.Failure.class,()->compile(source),source);
    }
    @Test void loopsHaveFixedBoundsScopesAndHardExpansionLimits(){
        for(String source:List.of(
            "result=0;for(int i=0;i<4;i++) result+=i;",
            "result=0;for(int i=4;i>=0;--i){result+=i;}",
            "result=0;for(int i=-2;i<=4;i+=2){for(int j=2;j>0;j-=1)result+=i*j;}",
            "result=0;for(int i=0;i<0;i++)result=1;"))assertDoesNotThrow(()->compile(source),source);
        assertDoesNotThrow(()->compile("result=0;for(int i=0;i<1024;i++)result+=i;"));
        for(String source:List.of(
            "for(int i=0;i<0;i++)result=1;",
            "result=0;for(int i=0;i<1025;i++)result+=i;",
            "result=0;for(int i=0;i<4;i--)result+=i;",
            "result=0;for(int i=0;i<signal;i++)result+=i;",
            "result=0;for(int i=0;i<4;i++){i=3;}",
            "result=0;for(int i=0;i<4;i++)result+=i;result=i;",
            "result=0;while(true)result+=1;"))assertThrows(FormulaCompiler.Failure.class,()->compile(source),source);
    }
    @Test void floatAssignmentsFollowCppStyleWithoutRepeatedCasts(){
        assertDoesNotThrow(()->compile("float x=0.5;x=x*2.0;x/=sampleRate;result=x;"));
        assertThrows(FormulaCompiler.Failure.class,()->compile("float x=1.5;int y=x;result=y;"));
    }
    @Test void unusedDefinitionsAndZeroLoopsDoNotChangeRandomSitesOrExecutionContext(){
        var base=compile("result=rand(0,1);");
        for(String prefix:List.of("float unused(float x){return rand(0,x);}","for(int i=0;i<0;i++)result=rand(0,signal);")){
            var program=compile(prefix+"result=rand(0,1);");assertEquals(base.bytecode(),program.bytecode());assertEquals(base.cpp(),program.cpp());assertEquals(base.usedInputs(),program.usedInputs());
        }
        assertFalse(compile("for(int i=0;i<0;i++)result=rand(0,1);result=1;").sampleContext());
    }
    @Test void diagnosticsUnderlineTheActualSymbolAcrossLinesAndCrlf(){
        String source="float good=1;\r\nresult = nonexistent + good;";
        var failure=assertThrows(FormulaCompiler.Failure.class,()->compile(source));
        assertEquals(source.indexOf("nonexistent"),failure.start());assertEquals("nonexistent",source.substring(failure.start(),failure.end()));
        var diagnostic=new FormulaLanguage(PORTS,3).diagnose(source).get(0);assertEquals(failure.start(),diagnostic.start());
        String bad="double x=2;\nint target=x;\nresult=target;";failure=assertThrows(FormulaCompiler.Failure.class,()->compile(bad));assertEquals(bad.indexOf("target"),failure.start());
    }
    @Test void indentationUnderstandsBracelessAndNestedStatements(){
        assertEquals(1,FormulaIndentation.next("if(signal>0)"));
        assertEquals(0,FormulaIndentation.next("if(signal>0)\n    result=1;"));
        assertEquals(2,FormulaIndentation.next("if(signal>0)\n    if(signal<2)"));
        assertEquals(0,FormulaIndentation.next("if(signal>0)\n    if(signal<2)\n        result=1;"));
        assertEquals(1,FormulaIndentation.next("if(signal>0){\n    result=1;"));
        String source="if(signal>0)\nresult=1;\nelse\nresult=0;\nresult+=2;";
        assertEquals("if (signal>0)\n    result=1;\nelse\n    result=0;\nresult+=2;",FormulaIndentation.format(source,4));
    }
    @Test void formatUsesCppCallAndDefinitionSpacingWithoutChangingCommentsOrMeaning(){
        String source="float shape  ( float x ,float amount )  {\nreturn tanh ( x*amount ); // keep name () {}\n}\nif(signal>0){\nresult=shape ( signal,2.0 );\n}else {\nresult=shape( 0 , 1 );\n}";
        String formatted=FormulaIndentation.format(source,4);
        assertTrue(formatted.contains("float shape(float x, float amount) {"));
        assertTrue(formatted.contains("if (signal>0) {"));assertTrue(formatted.contains("} else {"));
        assertTrue(formatted.contains("shape(signal, 2.0)"));assertTrue(formatted.contains("// keep name () {}"));
        assertEquals(compile(source).bytecode(),compile(formatted).bytecode());assertEquals(formatted,FormulaIndentation.format(formatted,4));
        assertEquals("float f() {}",FormulaIndentation.format("float f () { }",4));
    }
}
