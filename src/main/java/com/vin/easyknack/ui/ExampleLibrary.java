package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import com.vingame.glswing.node.document.*;

public final class ExampleLibrary {
    private record ExperimentStamp(java.nio.file.attribute.FileTime modified,long size,boolean experimental){}
    private static final Map<Path,ExperimentStamp> experiments=new HashMap<>();
    static synchronized boolean experimental(Path path)throws IOException{
        var modified=Files.getLastModifiedTime(path);long size=Files.size(path);var cached=experiments.get(path);
        if(cached!=null&&cached.modified().equals(modified)&&cached.size()==size)return cached.experimental();
        boolean experimental=false;
        try(var input=Files.newInputStream(path)){
            var codec=new KnackDocumentCodec();experimental=ExperimentalFeatures.requiresOptIn(codec.read(input,codec.currentSchemaVersion()));
        }catch(IOException|IllegalArgumentException invalid){return false;}
        if(experiments.size()>=256)experiments.clear();experiments.put(path,new ExperimentStamp(modified,size,experimental));return experimental;
    }
    record Entry(String name,Path path,List<Entry> children){boolean directory(){return children!=null;}}
    public static void initialize()throws IOException{
        if(com.vin.easyknack.app.ApplicationSettings.bundledExamples())return;
        installExamples(ApplicationSettings.examples());
    }
    /** Build-time entry point: installed examples are read-only at runtime. */
    public static void main(String[] args)throws IOException{
        if(args.length!=1)throw new IllegalArgumentException("Expected example output directory");
        installExamples(Path.of(args[0]));
    }
    private static void installExamples(Path root)throws IOException{
        Files.createDirectories(root);
        installSampler(root);
        installStochasticBinWidth(root);
        var marker=root.resolve(".easyknack-examples-v1");if(Files.exists(marker))return;
        var examples=new LinkedHashMap<String,java.util.function.Supplier<GLNodeGraphDocument>>();
        examples.put("Harmonizer.knack",HarmonizerExample::create);
        examples.put("Spectral/Processing.knack",SpectralExample::create);
        examples.put("Spectral/Resynthesis.knack",SpectralResynthesisExample::create);
        for(var entry:examples.entrySet()){
            var path=root.resolve(entry.getKey());Files.createDirectories(path.getParent());
            if(Files.exists(path))continue;
            install(path,entry.getValue());
        }
        Files.writeString(marker,"Built-in examples copied once; user files are never replaced.\n",StandardOpenOption.CREATE_NEW);
    }
    static void installStochasticBinWidth(Path root)throws IOException{
        var path=root.resolve("Spectral/Stochastic Bin Width.knack");
        Files.createDirectories(path.getParent());
        install(path,StochasticBinWidthExample::create);
    }
    private static void installSampler(Path root)throws IOException{
        var marker=root.resolve(".easyknack-sampler-v1");if(Files.exists(marker))return;
        var path=root.resolve("Buffer/Sampler.knack");Files.createDirectories(path.getParent());
        install(path,SamplerExample::create);
        Files.writeString(marker,"Sampler example installed; user edits are preserved.\n",StandardOpenOption.CREATE_NEW);
    }
    private static void install(Path path,java.util.function.Supplier<GLNodeGraphDocument> example)throws IOException{
        if(Files.exists(path))return;
        var temporary=Files.createTempFile(path.getParent(),".easyknack-example-",".tmp");
        try{
            try(var out=Files.newOutputStream(temporary)){new KnackDocumentCodec().write(example.get(),out);}
            // Publish only complete files; closing during background preparation must not leave a broken .knack.
            try{Files.move(temporary,path);}catch(FileAlreadyExistsException existing){/* Preserve user/concurrent files. */}
        }finally{Files.deleteIfExists(temporary);}
    }
    static List<Entry> scan(Path folder)throws IOException{return scan(folder,false);}
    static List<Entry> scan(Path folder,boolean experimentalEnabled)throws IOException{return scan(folder,0,experimentalEnabled); }
    private static List<Entry> scan(Path folder,int depth,boolean experimentalEnabled)throws IOException{
        if(depth>12||!Files.isDirectory(folder,LinkOption.NOFOLLOW_LINKS))return List.of();
        var result=new ArrayList<Entry>();
        try(var entries=Files.list(folder)){
            for(var path:entries.sorted(Comparator.comparing(p->p.getFileName().toString(),String.CASE_INSENSITIVE_ORDER)).toList()){
                if(Files.isSymbolicLink(path))continue;
                var name=path.getFileName().toString();
                if(Files.isDirectory(path)){var children=scan(path,depth+1,experimentalEnabled);if(!children.isEmpty())result.add(new Entry(name,path,children));}
                else if(name.toLowerCase(Locale.ROOT).endsWith(".knack")){
                    boolean experimental=experimental(path);
                    if(!experimental||experimentalEnabled){
                        String title=name.substring(0,name.length()-6);
                        result.add(new Entry(experimental?ExperimentalFeatures.label(title):title,path,null));
                    }
                }
            }
        }return List.copyOf(result);
    }
}
