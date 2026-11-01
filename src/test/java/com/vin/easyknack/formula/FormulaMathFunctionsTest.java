package com.vin.easyknack.formula;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class FormulaMathFunctionsTest {
    private static final List<String> FUNCTIONS=List.of("sinh","cosh","tanh","asinh","acosh","atanh","expm1","log1p");
    private static final List<FormulaCompiler.Port> PORTS=List.of(FormulaCompilerTest.number("signal",false),FormulaCompilerTest.number("result",true));

    @Test void functionsCompileInLegacyAndTypedLanguagesWithCorrectArityAndHelp(){
        for(String name:FUNCTIONS){
            assertEquals(1,FormulaCompiler.FUNCTIONS.get(name));
            for(int version:List.of(2,3)){
                var program=FormulaCompiler.compile("result="+name+"(signal);",PORTS,version);
                assertTrue(program.bytecode().contains(" "+name+" "));
                assertThrows(FormulaCompiler.Failure.class,()->FormulaCompiler.compile("result="+name+"();",PORTS,version));
                assertThrows(FormulaCompiler.Failure.class,()->FormulaCompiler.compile("result="+name+"(signal,1);",PORTS,version));
            }
            var help=FormulaEditorHelp.function(name,true);
            assertEquals(List.of("value"),help.parameters());assertNotEquals("Numeric function.",help.help());
            assertTrue(help.signature(0).label().startsWith(name+"("));
        }
        assertTrue(FormulaCompiler.compile("result=tanh(1);",PORTS,3).cpp().contains("formulaCalc<double>"));
        assertTrue(FormulaEditorHelp.function("tanh",true).help().contains("saturation"));
        assertTrue(FormulaEditorHelp.function("acosh",true).help().contains(">= 1"));
        assertTrue(FormulaEditorHelp.function("log1p",true).help().contains("NaN"));
    }

    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void interpretersAndGeneratedKernelsMatchStandardMathIncludingDomainBoundaries()throws Exception{
        Path root=Files.createTempDirectory(Path.of("target"),"formula-hyperbolic-").toAbsolutePath();
        Path cxx=Path.of(System.getProperty("easyknack.test.cxx"));
        var source=new StringBuilder("#include <cassert>\n#include <limits>\n#include <windows.h>\n#include \"FormulaProgram.hpp\"\n");
        var checks=new StringBuilder();int index=0;
        for(int version:List.of(2,3))for(String name:FUNCTIONS){
            String expression=version==3?"double value=signal;result="+name+"(value);":"result="+name+"(signal);";
            var program=FormulaCompiler.compile(expression,PORTS,version);
            source.append("void kernel").append(index).append("(const float* input,float* output,const easyknack::FormulaContext& context){\n").append(program.cpp()).append("}\n");
            checks.append("{easyknack::FormulaProgram p;p.read(\"").append(program.bytecode()).append("\",1,1);\n")
                .append("for(float value:values){float input[]{value},vm[]{0},compiled[]{0};p.evaluate(input,vm,context);kernel").append(index++)
                .append("(input,compiled,context);float expected=std::").append(name).append(version==3?"(double(value));":"(value);")
                .append("check(vm[0],expected);check(compiled[0],expected);}}\n");
        }
        // Exercise the real standalone compiler/SDK and loaded DLL too, not just C++ snippets.
        var builder=new FormulaNativeCompiler(cxx,root);
        for(int version:List.of(2,3)){
            String expression=String.join("+",FUNCTIONS.stream().map(n->n.equals("acosh")?"acosh(1+abs(signal))":n+"(signal)").toList());
            var program=FormulaCompiler.compile("result="+expression+";",PORTS,version);
            var library=builder.compile(program);assertTrue(builder.compile(program).cached());
            checks.append("{auto library=LoadLibraryA(\"").append(library.library().toString().replace("\\","\\\\")).append("\");assert(library);")
                .append("auto sample=reinterpret_cast<void(*)(const float*,float*,const easyknack::FormulaContext*)>(GetProcAddress(library,\"easyknack_formula_sample\"));assert(sample);")
                .append("easyknack::FormulaProgram p;p.read(\"").append(program.bytecode()).append("\",1,1);")
                .append("for(float value:{-.75f,-1e-8f,0.0f,1e-8f,.75f}){float input[]{value},vm[1]{},native[1]{};p.evaluate(input,vm,context);sample(input,native,&context);assert(std::isfinite(vm[0]));check(vm[0],native[0]);}FreeLibrary(library);}\n");
        }
        source.append("void check(float actual,float expected){if(std::isnan(expected)){assert(std::isnan(actual));}else if(std::isinf(expected)){assert(actual==expected);}else assert(std::abs(actual-expected)<=2e-6f*std::max(1.0f,std::abs(expected)));}\n")
            .append("int main(){easyknack::FormulaContext context;const float values[]{-100,-2,-1,-.75f,-1e-8f,0,1e-8f,.75f,1,2,100,std::numeric_limits<float>::infinity(),-std::numeric_limits<float>::infinity(),std::numeric_limits<float>::quiet_NaN()};\n")
            .append(checks).append("}\n");
        Path cpp=root.resolve("check.cpp"),exe=root.resolve("check.exe"),log=root.resolve("check.log");Files.writeString(cpp,source);
        run(List.of(cxx.toString(),"-std=c++17","-O2","-ffp-contract=off","-static","-I",Path.of("native/audio-host/src").toAbsolutePath().toString(),cpp.toString(),"-o",exe.toString()),log);
        run(List.of(exe.toString()),log);
    }
    private static void run(List<String> command,Path log)throws Exception{
        var process=new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try{assertTrue(process.waitFor(90,TimeUnit.SECONDS));assertEquals(0,process.exitValue(),Files.readString(log));}
        finally{if(process.isAlive())process.destroyForcibly();}
    }
}
