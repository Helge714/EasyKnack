import com.vin.easyknack.formula.*;
import com.vin.easyknack.plugins.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Target-side acceptance of the shipped compiler, SDK, Formula and Bake builder.
 * Source-file launch keeps this diagnostic outside application startup/UI flows. */
class NativeCompilationCheck {
    static void check(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    static void run(List<String> command,Path log)throws Exception{
        var process=new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try{check(process.waitFor(300,TimeUnit.SECONDS),"Compiler timed out: "+log);check(process.exitValue()==0,Files.readString(log));}
        finally{if(process.isAlive())process.destroyForcibly();}
    }
    static void perform(Path compiler,Path root)throws Exception{
        System.setProperty("easyknack.plugin.cxx",compiler.toString());
        var ports=List.of(new FormulaCompiler.Port("signal","signal",FormulaCompiler.Type.NUMBER,false),new FormulaCompiler.Port("result","result",FormulaCompiler.Type.NUMBER,true));
        var program=FormulaCompiler.compile("result=signal*2;",ports);
        var builder=new FormulaNativeCompiler(compiler,root.resolve("formula"));
        var formula=builder.compile(program);check(!formula.cached()&&builder.compile(program).cached(),"Formula cache was not reused");
        var sdk=root.resolve("sdk");PluginSdk.unpack(new java.io.ByteArrayInputStream(PluginSdk.bytes()),sdk);
        var source=root.resolve("formula-check.cpp");var platform=com.vin.easyknack.NativePlatform.current();var executable=root.resolve(platform.executable("formula-check"));
        var library=formula.library().toString().replace("\\","\\\\").replace("\"","\\\"");
        Files.writeString(source,"""
            #include "platform/SharedLibrary.hpp"
            #include "FormulaContext.hpp"
            #include <cassert>
            int main(){easyknack::platform::SharedLibrary library;assert(library.open("%s"));
              auto sample=library.symbol<void(*)(const float*,float*,const easyknack::FormulaContext*)>("easyknack_formula_sample");assert(sample);
              easyknack::FormulaContext context;float input[]{3},output[]{0};sample(input,output,&context);assert(output[0]==6);}
            """.formatted(library));
        var command=new ArrayList<>(List.of(compiler.toString(),"-std=c++17","-O2","-I",sdk.resolve("src").toString(),source.toString(),"-o",executable.toString()));
        command.addAll(platform.compilerArchitectureFlags());
        if(platform.windows())command.add("-static");else if(platform.os()==com.vin.easyknack.NativePlatform.OperatingSystem.LINUX)command.add("-ldl");
        run(command,root.resolve("formula-check-build.log"));run(List.of(executable.toString()),root.resolve("formula-check-run.log"));
        System.out.println("PASS: Formula native build, module loading, result and cache");
        var files=Map.of("plugin.properties","format=1\nid=acceptance_check\nversion=1\nnodes=acceptance.LinuxAcceptanceNode\n",
          "java/acceptance/LinuxAcceptanceNode.java","""
            package acceptance;
            import com.vin.easyknack.nodes.*;
            @RegisterNode(id="easyknack.acceptance.check",title="Acceptance",category=NodeCategory.INPUT,kind=NodeKind.AUDIO)
            public final class LinuxAcceptanceNode extends BasicNode {
              public void define(NodeBuilder node,NodeState state){node.addOutput(new PropertyId("out"),"Out");}
              public NativeProcessor processor(){return new NativeProcessor("acceptance_check","nodes/LinuxAcceptanceNode.hpp");}
            }
            """,
          "native/LinuxAcceptanceNode.hpp","""
            #pragma once
            #include "ParameterContracts.hpp"
            #include "../NodeProcessor.hpp"
            namespace easyknack {
            struct LinuxAcceptanceNode:NodeProcessor {
              void configure(std::istream& input)override{parameters::LinuxAcceptanceNodeParameters::read(input);}
              void process(ProcessContext& context)noexcept override{std::fill_n(context.output,context.bufferSize,.125f);}
            };
            EASYKNACK_NODE(LinuxAcceptanceNode,"acceptance_check")
            }
            """);
        var manager=new PluginManager(root.resolve("plugins"));
        var built=manager.buildEphemeral(files,System.out::println);
        check(Files.isExecutable(built.host())&&!built.cached(),"Native host was not built");
        check(manager.buildEphemeral(files,System.out::println).cached(),"Bake/plugin build cache was not reused");
        System.out.println("PASS: javac, source-plugin/Bake compiler pipeline, native host handshake and cache");
    }
    public static void main(String[] args){
        Path root=null;
        try{
            check(args.length==1,"Expected C++ compiler path");root=Files.createTempDirectory("easyknack-compiler-acceptance-");
            System.out.println("Compiler test work directory: "+root);perform(Path.of(args[0]).toAbsolutePath(),root);
            try(var files=Files.walk(root)){for(var path:files.sorted(Comparator.reverseOrder()).toList())Files.delete(path);}
            catch(java.io.IOException cleanup){System.out.println("Tests passed; temporary files retained at "+root+": "+cleanup.getMessage());}
            System.exit(0);
        }catch(Exception error){error.printStackTrace();System.err.println("Compiler test files retained at "+root);System.exit(1);}
    }
}
