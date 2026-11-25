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
        if(args.length==2&&args[0].equals("--verify")){
            verifyBundle(Path.of(args[1]));
            System.out.println("Bundled examples verified: "+LearningExamples.entries().size()+" projects and both language guides.");
            return;
        }
        if(args.length!=1)throw new IllegalArgumentException("Expected example output directory, or --verify <directory>");
        installExamples(Path.of(args[0]));
    }
    /** Package gate: validate the installed content, never regenerate missing files. */
    private static void verifyBundle(Path root)throws IOException{
        var expected=new TreeSet<String>();var actual=new TreeSet<String>();
        var codec=new KnackDocumentCodec();
        for(var entry:LearningExamples.entries()){
            expected.add(entry.path());var path=root.resolve(entry.path());
            if(!Files.isRegularFile(path,LinkOption.NOFOLLOW_LINKS))throw new IOException("Missing bundled example: "+entry.path());
            try(var input=Files.newInputStream(path)){
                var document=NodeDocuments.prepare(codec.read(input,codec.currentSchemaVersion()));
                if(!document.externalAssets().isEmpty())throw new IOException("External assets in bundled example: "+entry.path());
                for(var asset:NodeDocuments.assets(document))
                    if(asset.viewState().camera()==null)throw new IOException("Missing saved camera in bundled example: "+entry.path());
            }catch(IllegalArgumentException invalid){throw new IOException("Invalid bundled example: "+entry.path(),invalid);}
        }
        try(var paths=Files.walk(root)){
            for(var path:paths.filter(p->Files.isRegularFile(p,LinkOption.NOFOLLOW_LINKS)&&p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".knack")).toList()){
                var relative=root.relativize(path);boolean hidden=false;
                for(var part:relative)hidden|=part.toString().startsWith(".");
                if(!hidden)actual.add(relative.toString().replace('\\','/'));
            }
        }
        if(!expected.equals(actual))throw new IOException("Unexpected bundled example files: "+actual.stream().filter(p->!expected.contains(p)).toList());
        for(String language:List.of("de","en")){
            var guide=root.resolve("README-"+language+".md");
            if(!Files.isRegularFile(guide,LinkOption.NOFOLLOW_LINKS)||Files.size(guide)==0)throw new IOException("Missing bundled example guide: "+guide.getFileName());
        }
    }
    private static void installExamples(Path root)throws IOException{
        Files.createDirectories(root);
        archivePreviousExamples(root);
        for(var entry:LearningExamples.entries()){
            var path=root.resolve(entry.path());Files.createDirectories(path.getParent());
            install(path,entry.document());
        }
        for(String language:List.of("de","en")){
            var guide=root.resolve("README-"+language+".md");
            if(!Files.exists(guide))Files.writeString(guide,NodeDocumentation.read("user/"+language+"/examples.md"),StandardOpenOption.CREATE_NEW);
        }
        var marker=root.resolve(".easyknack-examples-v2");
        if(!Files.exists(marker))Files.writeString(marker,"Numbered learning examples; existing documents are never replaced.\n",StandardOpenOption.CREATE_NEW);
    }
    private static void archivePreviousExamples(Path root)throws IOException{
        if(Files.exists(root.resolve(".easyknack-examples-v2")))return;
        // Only the former generated library is moved. Keep edited copies in a hidden backup.
        if(!Files.exists(root.resolve(".easyknack-examples-v1"))&&!Files.exists(root.resolve(".easyknack-sampler-v1")))return;
        for(String relative:List.of("Harmonizer.knack","Buffer/Sampler.knack","Spectral/Processing.knack","Spectral/Resynthesis.knack","Spectral/Stochastic Bin Width.knack")){
            var old=root.resolve(relative);if(!Files.isRegularFile(old,LinkOption.NOFOLLOW_LINKS))continue;
            var backup=root.resolve(".previous-examples").resolve(relative);Files.createDirectories(backup.getParent());
            if(Files.exists(backup))backup=backup.resolveSibling(UUID.randomUUID()+"-"+backup.getFileName());
            Files.move(old,backup);
        }
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
                if(Files.isSymbolicLink(path)||path.getFileName().toString().startsWith("."))continue;
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
