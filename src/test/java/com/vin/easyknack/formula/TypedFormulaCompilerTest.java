package com.vin.easyknack.formula;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class TypedFormulaCompilerTest {
    private static final List<FormulaCompiler.Port> PORTS=List.of(FormulaCompilerTest.number("signal",false),FormulaCompilerTest.trigger("clock",false),FormulaCompilerTest.number("result",true));
    private static FormulaCompiler.Program compile(String code){return FormulaCompiler.compile(code,PORTS,3);}
    @Test void declarationsCastsPromotionsAndDiagnostics(){
        for(String code:List.of("double seconds=(double)samplePosition/sampleRate;result=(float)sin(tau*440.0*seconds);", "unsigned long p=samplePosition;uint low=(uint)p;result=low;", "state double phase=0;phase+=signal/sampleRate;phase-=floor(phase);result=sin(tau*phase);", "bool active=(bool)signal;int count=active+clock;result=count;", "state long x=-9223372036854775808;state ulong y=18446744073709551615;result=(float)x;", "float a=1.5f;double b=(double)a;result=(float)a/(double)b;", "uint a=1u;ulong b=1ul;result=a+b;")){
            var p=assertDoesNotThrow(()->compile(code),code);assertTrue(p.bytecode().startsWith("FORM3 "));assertTrue(new FormulaLanguage(PORTS,3).diagnose(code).isEmpty());
        }
        for(String code:List.of("long x=1;int y=x;result=y;", "uint x=1;result=x;", "int x=1;uint y=(uint)2;result=x+y;", "bool x=1;result=x;", "if(signal)result=0;else result=1;", "state uint x=-1;result=x;", "state ulong x=18446744073709551616;result=0;", "state double x=1e999;result=0;", "result+=1;", "signal*=2;result=0;"))assertThrows(FormulaCompiler.Failure.class,()->compile(code),code);
        assertTrue(FormulaCompiler.compile("double x=1;result=x;",PORTS,2).bytecode().startsWith("FORM3 "));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void typedPreviewAndCompiledResultsAgreeAtNumericBoundaries()throws Exception{
        var cases=new LinkedHashMap<String,Float>();
                cases.put("int x=5/2;result=x;",2f);
        cases.put("final float x=signal*2;result=x;",6f);
        cases.put("const float lo=0.5;const float hi=lo*4;result=hi;",2f);
        cases.put("float f(final float x){const float n=2;final float y=x*n;return y;}result=f(signal);",6f);
        cases.put("float square(float x){return x*x;} result=square(signal)+square(2);",13f);
        cases.put("float choose(float x){if(x>0)return x;return -x;} result=choose(-3);",3f);
        cases.put("float a(float x){return b(x)+1;} float b(float y){return y*2;} result=a(3);",7f);
        cases.put("result=0;for(int i=0;i<4;i++)result+=i;",6f);
        cases.put("result=0;for(int i=4;i>=0;i-=2)result+=i;",6f);
        cases.put("result=0;for(int i=0;i<3;i++){for(int j=0;j<2;j++)result+=i+j;}",9f);
        cases.put("float sum(float x){float a=0;for(int i=0;i<4;i++){if(i==2)return a+x;a+=i;}return -1;}result=sum(3);",4f);
        cases.put("float f(float x){for(int i=0;i<0;i++)return 3;return x;}result=f(7);",7f);
        cases.put("float x=1.5;x*=2.0;result=x;",3f);
        cases.put("bool positive(float x){return x>0;} if(positive(signal))result=1;else result=0;",1f);
        cases.put("state float x=2;float square(float a){return a*a;}result=square(x);x+=1;",4f);

        cases.put("result=sineWave(0.25);",1f);
        cases.put("result=sineWave(-0.25);",-1f);
        cases.put("result=sineWave(1000000.25);",1f);
        cases.put("result=sineWave(1);",0f);
        cases.put("result=sineWave(1.0/0.0);",0f);
        cases.put("result=sineWave(sqrt(-1.0));",0f);
        cases.put("int x=-2147483648;result=x==(-2147483647-1);",1f);
        cases.put("long x=-9223372036854775808;result=x/(long)-1==x;",1f);
        cases.put("uint x=(uint)3;result=x*true;",3f);
        cases.put("result=((bool)signal && clock)+true;",2f);
        cases.put("result=(false && (signal>0))+true;",1f);
        cases.put("state float x=1.5f;state ulong n=1UL;result=x+(float)n;",2.5f);
        cases.put("double x=(double)5/2;result=x;",2.5f);
        cases.put("result=true+true+false;",2f);
        cases.put("result=signal*clock;",3f);
        cases.put("ulong x=samplePosition;result=(float)(x%(ulong)256);",123f);
        cases.put("long x=9007199254740993;result=x-9007199254740992;",1f);
        cases.put("uint x=(uint)0;x-=(uint)1;result=x==(uint)4294967295;",1f);
        cases.put("int x=2147483647;x+=1;result=x==(-2147483647-1);",1f);
        cases.put("long x=(long)-9223372036854775808;result=x/(long)-1==x;",1f);
        cases.put("int x=8;result=x/0;",0f);
        cases.put("result=(int)-2.9;",-2f);
        cases.put("result=(uint)-2.9;",0f);
        cases.put("result=(int)1e300==2147483647;",1f);
        cases.put("result=(int)sqrt(-1.0);",0f);
        cases.put("double x=16777217.0;result=(float)((double)(float)x-x);",-1f);
        cases.put("double x=16777217.0;result=(float)(x-16777216.0);",1f);
        cases.put("bool x=(bool)signal;result=x+clock;",2f);
        cases.put("uint x=(uint)7;result=min(x,(uint)3);",3f);
        cases.put("float x=2.0f;x+=3;x*=2;x-=4;x/=3;result=x;",2f);
        cases.put("double time=(double)samplePosition/sampleRate;result=sin(tau*440.0*time);",null);
        Path root=Files.createTempDirectory(Path.of("target"),"formula-types-").toAbsolutePath();Path cxx=Path.of(System.getProperty("easyknack.test.cxx"));
        StringBuilder source=new StringBuilder("#include <cassert>\n#include <windows.h>\n#include \"FormulaProgram.hpp\"\n#include \"Wavetables.hpp\"\n");
        int n=0;var programs=new ArrayList<FormulaCompiler.Program>();for(var entry:cases.entrySet()){var p=compile(entry.getKey());programs.add(p);source.append("void test").append(n++).append("(const float* input,float* output,const easyknack::FormulaContext& context){\n").append(p.cpp()).append("}\n");}
        String persistent="state ulong count=9007199254740993;state double phase=0;state bool active=false;active=!active;count+=(ulong)1;phase+=440.0/sampleRate;phase-=floor(phase);result=sineWave(phase)+(float)(count%(ulong)2)*active;";
        var program=compile(persistent);var dll=new FormulaNativeCompiler(cxx,root).compile(program);
        source.append("int main(){using namespace easyknack;FormulaContext context;context.sineTable=Wavetables::builtinTables().waves[0][0].data();context.samplePosition=UINT64_C(9007199254741115);float input[]{3,1},native[1]{},vm[1]{};\n");
        n=0;for(var expected:cases.values()){source.append("{FormulaProgram p;p.read(\"").append(programs.get(n).bytecode()).append("\",2,1);auto bank=p.typed.values;auto nativeContext=context;nativeContext.typedState=bank.data();p.evaluate(input,vm,context);test").append(n++).append("(input,native,nativeContext);assert(vm[0]==native[0]);assert(bank==p.typed.values);");if(expected!=null)source.append("assert(vm[0]==").append(expected).append("f);");source.append("}\n");}
        source.append("auto dll=LoadLibraryA(\"").append(dll.library().toString().replace("\\","\\\\")).append("\");assert(dll);auto sample=reinterpret_cast<void(*)(const float*,float*,const FormulaContext*)>(GetProcAddress(dll,\"easyknack_formula_sample\"));assert(sample);\n");
        source.append("FormulaProgram p;p.read(\"").append(program.bytecode()).append("\",2,1);auto nativeState=p.typed.values;context.typedState=nativeState.data();for(int i=0;i<5000;i++){sample(input,native,&context);auto preview=context;preview.typedState=nullptr;p.evaluate(input,vm,preview);assert(vm[0]==native[0]);assert(nativeState==p.typed.values);context.samplePosition++;}\n");
        var live=compile("const float scale=2;final float value=signal*scale;result=value;");
        source.append("{FormulaProgram live;live.read(\"").append(live.bytecode()).append("\",2,1);for(float value:{1.f,4.f,-2.f}){input[0]=value;live.evaluate(input,vm,context);assert(vm[0]==value*2);}input[0]=3;}\n");
        var reordered=compile("state bool active=false;state double phase=0;state ulong count=9007199254740993;result=phase;");
        source.append("FormulaProgram next;next.read(\"").append(reordered.bytecode()).append("\",2,1);next.inheritState(p);assert(next.typed.values[0]==p.typed.values[2]);assert(next.typed.values[1]==p.typed.values[1]);assert(next.typed.values[2]==p.typed.values[0]);\n");
        var legacy=FormulaCompiler.compile("result=sineWave(signal);",PORTS,2);
        source.append("{FormulaProgram legacy;legacy.read(\"").append(legacy.bytecode()).append("\",2,1);for(int i=-100000;i<=100000;i++){double phase=i/10000.;input[0]=float(phase);legacy.evaluate(input,vm,context);assert(vm[0]==context.sineWave(input[0]));double expected=std::sin(Wavetables::tau*phase);assert(std::abs(context.sineWave(phase)-expected)<1.3e-6);assert(context.sineWave(phase)==float(Wavetables::lookup(context.sineTable,2048,phase)));}}\n");
        source.append("FreeLibrary(dll);}\n");Path cpp=root.resolve("check.cpp"),exe=root.resolve("check.exe"),log=root.resolve("check.log");Files.writeString(cpp,source);
        run(List.of(cxx.toString(),"-std=c++17","-O2","-ffp-contract=off","-static","-I",Path.of("native/audio-host/src").toAbsolutePath().toString(),cpp.toString(),"-o",exe.toString()),log);run(List.of(exe.toString()),log);
    }
    private static void run(List<String> command,Path log)throws Exception{var process=new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();try{assertTrue(process.waitFor(90,TimeUnit.SECONDS));assertEquals(0,process.exitValue(),Files.readString(log));}finally{if(process.isAlive())process.destroyForcibly();}}
}
