package com.vin.easyknack.formula;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class FormulaNativeCompilerTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void isolatedBuildCacheAndRealNativeResults()throws Exception{
        Path root=Files.createTempDirectory(Path.of("target"),"formula-native-").toAbsolutePath();
        Path compiler=Path.of(System.getProperty("easyknack.test.cxx"));
        var ports=List.of(FormulaCompilerTest.number("signal",false),FormulaCompilerTest.number("gain",false),FormulaCompilerTest.trigger("clock",false),FormulaCompilerTest.number("result",true),FormulaCompilerTest.trigger("fire",true));
        var program=FormulaCompiler.compile("result = signal * gain; if(clock && result > 0) fire=true;",ports);
        var builder=new FormulaNativeCompiler(compiler,root);var result=builder.compile(program);assertFalse(result.cached());assertTrue(builder.compile(program).cached());
        Files.writeString(root.resolve("timing.txt"),"Isolated C++ build: "+result.milliseconds()+" ms\n");
        String library=result.library().toString().replace("\\","\\\\");
        var platform=com.vin.easyknack.NativePlatform.current();
        Path source=root.resolve("check.cpp"),exe=root.resolve(platform.executable("check")),log=root.resolve("check.log");
        Files.writeString(source,"""
            #include "platform/SharedLibrary.hpp"
            #include <cassert>
            #include "FormulaProgram.hpp"
            int main(){
                easyknack::platform::SharedLibrary dll;assert(dll.open("%s"));
                auto abi=dll.symbol<unsigned(*)()>("easyknack_formula_abi");assert(abi&&abi()==6);
                auto sample=dll.symbol<void(*)(const float*,float*,const easyknack::FormulaContext*)>("easyknack_formula_sample");assert(sample);
                easyknack::FormulaContext context;float input[]{-2,3,1},output[]{99,99};sample(input,output,&context);assert(output[0]==-6&&output[1]==0);
                input[0]=2;sample(input,output,&context);assert(output[0]==6&&output[1]==1);
                input[2]=0;sample(input,output,&context);assert(output[1]==0);
                auto block=dll.symbol<void(*)(const float* const*,const unsigned*,float* const*,unsigned,const easyknack::FormulaContext*)>("easyknack_formula_block");assert(block);
                float audio[]{-1,2,3,-4},gain[]{2},trigger[]{1,1,0,1},a[4]{},b[4]{};
                const float* inputs[]{audio,gain,trigger};float* outputs[]{a,b};unsigned strides[]{1,0,1};block(inputs,strides,outputs,4,&context);
                assert(a[0]==-2&&a[1]==4&&a[2]==6&&a[3]==-8);assert(b[0]==0&&b[1]==1&&b[2]==0&&b[3]==0);
                easyknack::FormulaProgram preview;preview.read("%s",3,2);
                for(int i=0;i<1000;i++){float in[]{float(i-500)*.01f,float(i %% 7),float(i %% 2)},vm[2]{},native[2]{};preview.evaluate(in,vm);sample(in,native,&context);assert(vm[0]==native[0]&&vm[1]==native[1]);}
                bool rejected=false;try{preview.read("FORM1 1 1 jump 0 0 0 0 0",3,2);}catch(const std::invalid_argument&){rejected=true;}assert(rejected);
                dll.close();
            }
            """.formatted(library,program.bytecode()));
        var command=new ArrayList<>(List.of(compiler.toString(),"-std=c++17","-O2","-I",Path.of("native/audio-host/src").toAbsolutePath().toString(),source.toString(),"-o",exe.toString()));
        if(platform.windows())command.add("-static");
        else if(platform.os()==com.vin.easyknack.NativePlatform.OperatingSystem.LINUX)command.add("-ldl");
        run(command,log);
        run(List.of(exe.toString()),log);
    }
    private static void run(List<String> command,Path log)throws Exception{
        var p=new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try{assertTrue(p.waitFor(60,TimeUnit.SECONDS));assertEquals(0,p.exitValue(),Files.readString(log));}finally{if(p.isAlive())p.destroyForcibly();}
    }
}
