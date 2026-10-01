package com.vin.easyknack.formula;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Isolated formula build, deliberately independent of the host and its headers. */
public final class FormulaNativeCompiler {
    public record Result(Path library, boolean cached, long milliseconds) {}
    private final Path compiler,cache;
    public FormulaNativeCompiler(Path compiler,Path cache){this.compiler=compiler.toAbsolutePath();this.cache=cache.toAbsolutePath();}
    private static final class Abi {
        static final String HEADER=read();
        private static String read(){try(var zip=new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(com.vin.easyknack.plugins.PluginSdk.bytes()))){
            String context=null,numeric=null;java.util.zip.ZipEntry entry;
            while((entry=zip.getNextEntry())!=null){if(entry.getName().equals("src/FormulaContext.hpp"))context=new String(zip.readAllBytes(),StandardCharsets.UTF_8);else if(entry.getName().equals("src/FormulaNumeric.hpp"))numeric=new String(zip.readAllBytes(),StandardCharsets.UTF_8);}
            if(context==null||numeric==null)throw new IOException("Formula ABI missing from plugin SDK");
            return numeric+"\n"+context.replace("#include \"FormulaNumeric.hpp\"","");
        }catch(IOException e){throw new java.io.UncheckedIOException(e);}}
    }
    public static String translationUnit(FormulaCompiler.Program program){
        int inputs=(int)program.ports().stream().filter(p->!p.output()).count();
        int outputs=program.ports().size()-inputs;
        StringBuilder block=new StringBuilder("EK_EXPORT void easyknack_formula_block(const float* const* inputs, const unsigned* strides, float* const* outputs, unsigned frames,const easyknack::FormulaContext* supplied) noexcept {\nauto context=*supplied;for(unsigned frame=0;frame<frames;++frame){context.sampleIndex=supplied->sampleIndex+frame;context.samplePosition=supplied->samplePosition+frame;\n");
        block.append("float input[").append(Math.max(1,inputs)).append("]{}, output[").append(Math.max(1,outputs)).append("]{};\n");
        for(int i=0;i<inputs;i++)block.append("input[").append(i).append("] = inputs[").append(i).append("][frame * strides[").append(i).append("]];\n");
        block.append(program.cpp());
        for(int i=0;i<outputs;i++)block.append("outputs[").append(i).append("][frame] = output[").append(i).append("];\n");
        block.append("}\n}\n");
        return Abi.HEADER+"\n#include <cmath>\n#include <algorithm>\n#include <cstddef>\n"
            +"#ifdef _WIN32\n#define EK_EXPORT extern \"C\" __declspec(dllexport)\n#else\n#define EK_EXPORT extern \"C\" __attribute__((visibility(\"default\")))\n#endif\n"
            +"EK_EXPORT unsigned easyknack_formula_abi() noexcept { return 6; }\n"
            +"EK_EXPORT void easyknack_formula_sample(const float* input, float* output,const easyknack::FormulaContext* supplied) noexcept {const auto& context=*supplied;\n"+program.cpp()+"}\n"+block;
    }
    /** Caller owns the background executor; never invoke from the UI or audio thread. */
    public synchronized Result compile(FormulaCompiler.Program program)throws IOException,InterruptedException{
        long start=System.nanoTime();String source=translationUnit(program);
        String identity=compiler+":"+Files.size(compiler)+":"+Files.getLastModifiedTime(compiler)+":-O3:-ffp-contract=off:abi6:"+source;
        String key;
        try{key=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException e){throw new AssertionError(e);}
        Files.createDirectories(cache);boolean windows=System.getProperty("os.name").startsWith("Windows");
        Path library=cache.resolve(key+(windows?".dll":".so"));
        if(Files.isRegularFile(library)&&Files.size(library)>0)return new Result(library,true,elapsed(start));
        Path work=Files.createTempDirectory(cache,"compile-");Path cpp=work.resolve("formula.cpp"),log=work.resolve("build.log"),built=work.resolve(library.getFileName());
        Files.writeString(cpp,source,StandardCharsets.UTF_8);
        var command=new ArrayList<>(List.of(compiler.toString(),"-std=c++17","-O3","-ffp-contract=off","-shared"));
        if(windows)command.add("-static");else command.add("-fPIC");
        command.addAll(List.of(cpp.toString(),"-o",built.toString()));
        Process process=new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try{
            if(!process.waitFor(60,TimeUnit.SECONDS)){process.destroyForcibly().waitFor();throw new IOException("Formula compilation timed out; log: "+log);}
            if(process.exitValue()!=0)throw new IOException("Formula compilation failed; log: "+log+"\n"+Files.readString(log));
            try{Files.move(built,library,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(built,library,StandardCopyOption.REPLACE_EXISTING);}
            return new Result(library,false,elapsed(start));
        }catch(InterruptedException e){process.destroyForcibly();Thread.currentThread().interrupt();throw e;}
    }
    private static long elapsed(long start){return TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start);}
}
