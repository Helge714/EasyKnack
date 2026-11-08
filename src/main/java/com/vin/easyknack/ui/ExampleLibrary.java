package com.vin.easyknack.ui;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import com.vingame.glswing.node.document.*;

public final class ExampleLibrary {
    record Entry(String name,Path path,List<Entry> children){boolean directory(){return children!=null;}}
    public static void initialize()throws IOException{
        var root=ApplicationSettings.examples();Files.createDirectories(root);
        installSampler(root);
        installStochasticBinWidth(root);
        var marker=root.resolve(".easyknack-examples-v1");if(Files.exists(marker))return;
        var examples=new LinkedHashMap<String,java.util.function.Supplier<GLNodeGraphDocument>>();
        examples.put("Harmonizer.knack",HarmonizerExample::create);
        examples.put("Spectral/Processing.knack",SpectralExample::create);
        examples.put("Spectral/Resynthesis.knack",SpectralResynthesisExample::create);
        var codec=new KnackDocumentCodec();
        for(var entry:examples.entrySet()){
            var path=root.resolve(entry.getKey());Files.createDirectories(path.getParent());
            if(Files.exists(path))continue;
            try(var out=Files.newOutputStream(path,StandardOpenOption.CREATE_NEW)){codec.write(entry.getValue().get(),out);}
        }
        Files.writeString(marker,"Built-in examples copied once; user files are never replaced.\n",StandardOpenOption.CREATE_NEW);
    }
    static void installStochasticBinWidth(Path root)throws IOException{
        var path=root.resolve("Spectral/Stochastic Bin Width.knack");
        Files.createDirectories(path.getParent());
        if(!Files.exists(path))try(var out=Files.newOutputStream(path,StandardOpenOption.CREATE_NEW)){
            new KnackDocumentCodec().write(StochasticBinWidthExample.create(),out);
        }
    }
    private static void installSampler(Path root)throws IOException{
        var marker=root.resolve(".easyknack-sampler-v1");if(Files.exists(marker))return;
        var path=root.resolve("Buffer/Sampler.knack");Files.createDirectories(path.getParent());
        if(!Files.exists(path))try(var out=Files.newOutputStream(path,StandardOpenOption.CREATE_NEW)){new KnackDocumentCodec().write(SamplerExample.create(),out);}
        Files.writeString(marker,"Sampler example installed; user edits are preserved.\n",StandardOpenOption.CREATE_NEW);
    }    static List<Entry> scan(Path folder)throws IOException{return scan(folder,0);}
    private static List<Entry> scan(Path folder,int depth)throws IOException{
        if(depth>12||!Files.isDirectory(folder,LinkOption.NOFOLLOW_LINKS))return List.of();
        var result=new ArrayList<Entry>();
        try(var entries=Files.list(folder)){
            for(var path:entries.sorted(Comparator.comparing(p->p.getFileName().toString(),String.CASE_INSENSITIVE_ORDER)).toList()){
                if(Files.isSymbolicLink(path))continue;
                var name=path.getFileName().toString();
                if(Files.isDirectory(path)){var children=scan(path,depth+1);if(!children.isEmpty())result.add(new Entry(name,path,children));}
                else if(name.toLowerCase(Locale.ROOT).endsWith(".knack"))result.add(new Entry(name.substring(0,name.length()-6),path,null));
            }
        }return List.copyOf(result);
    }
}
