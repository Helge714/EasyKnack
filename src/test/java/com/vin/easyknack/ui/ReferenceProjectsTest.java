package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;

class ReferenceProjectsTest {
    @Test void versionedDocumentsRemainValidAndDeterministic() throws Exception {
        try(var files=new NodeDocuments()){
            var routing=Path.of("src/test/resources/reference-projects/routing.knack");
            if(Boolean.getBoolean("easyknack.references.generate")){
                Files.createDirectories(routing.getParent());files.write(routing,ReferenceProjects.routing()).toCompletableFuture().get();
            }
            assertEquals(6,files.read(routing).toCompletableFuture().get().root().graph().nodes().size());
            for(var p:ReferenceProjects.all()){
                var file=Path.of("src/test/resources/reference-projects",p.name()+".knack");
                if(Boolean.getBoolean("easyknack.references.generate")){
                    Files.createDirectories(file.getParent());files.write(file,p.document()).toCompletableFuture().get();
                }
                assertTrue(Files.exists(file),"Generate reference documents explicitly: -Deasyknack.references.generate=true");
                var loaded=files.read(file).toCompletableFuture().get();
                assertEquals(ReferenceProjects.original(p),ReferenceProjects.original(new ReferenceProjects.Project(p.name(),loaded,loaded.root().graph().findNode(p.group().id()).orElseThrow(),p.midi())));
                assertEquals(ReferenceProjects.original(p),ReferenceProjects.original(ReferenceProjects.make(p.name(),p.name().startsWith("synth-")?Integer.parseInt(p.name().substring(6)):0)));
                for(var asset:NodeDocuments.assets(p.document()))assertEquals(asset.graph().nodes().size(),asset.viewState().nodes().size());
            }
        }
    }

    @Test @EnabledIfSystemProperty(named="easyknack.test.references",matches="true")
    void compareAndMeasureOriginalExportAndScheduledExport() throws Exception {
        var dir=Path.of("target/reference-v1").toAbsolutePath();Files.createDirectories(dir);
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());var exports=new ArrayList<GroupSourceExporter.Export>();
        var cpp=new StringBuilder("#include \"AudioGraph.hpp\"\n#include \"ReferenceHarness.hpp\"\n");
        var scenarios=ReferenceProjects.all();var loadedDefinitions=new ArrayList<BasicNode>();
        for(var p:scenarios){
            var export=GroupSourceExporter.create(p.document(),p.group());exports.add(export);definitions.add(export.definition());
            String header=export.files().get("native/"+export.className()+".hpp");
            Files.writeString(dir.resolve(export.className()+".hpp"),header);
            Files.writeString(dir.resolve(export.className()+"Plain.hpp"),header.replace("struct "+export.className()+":","struct "+export.className()+"Plain:").replace("if(renderFusedMath","if(false && renderFusedMath").replace(",true>(",",false>(").replace("if(renderSpecializedChain(","if(false && renderSpecializedChain("));
            cpp.append("#include \"").append(export.className()).append(".hpp\"\n#include \"").append(export.className()).append("Plain.hpp\"\n");
            export.write(dir.resolve(p.name()+".knackplugin"));
            Files.writeString(dir.resolve(p.name()+".original.txt"),ReferenceProjects.original(p));

            var java=dir.resolve(export.className()+".java");Files.writeString(java,export.files().get("java/com/vin/easyknack/nodes/"+export.className()+".java"));
            var diagnostics=new java.io.ByteArrayOutputStream();
            assertEquals(0,javax.tools.ToolProvider.getSystemJavaCompiler().run(null,diagnostics,diagnostics,"-proc:none","-classpath",System.getProperty("java.class.path"),"-d",dir.toString(),java.toString()),diagnostics.toString());
            try(var loader=new java.net.URLClassLoader(new java.net.URL[]{dir.toUri().toURL()},getClass().getClassLoader())){
                var loaded=(BasicNode)loader.loadClass("com.vin.easyknack.nodes."+export.className()).getConstructor().newInstance();
                assertEquals(NativeContractGenerator.fingerprint(export.definition()),NativeContractGenerator.fingerprint(loaded));
                loadedDefinitions.add(loaded);Files.writeString(dir.resolve(p.name()+".plugin.txt"),ReferenceProjects.baked(p,loaded));
            }
        }
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        cpp.append("using namespace easyknack;\nint main(int argc,char** argv){try{\n");
        cpp.append("auto factory=[](const std::string& id,bool plain)->std::unique_ptr<NodeProcessor>{\n");
        for(var e:exports)cpp.append("if(id==\"").append(e.definition().processor().id()).append("\"){if(plain)return std::make_unique<").append(e.className()).append("Plain>();return std::make_unique<").append(e.className()).append(">();}\n");
        cpp.append("return createNodeProcessor(id);};\nReferenceHarness harness(argc>1?argv[1]:\"target/reference-v1\");\n");
        for(int i=0;i<scenarios.size();i++){
            var p=scenarios.get(i);
            cpp.append("harness.run(\"").append(p.name()).append("\",").append(AdvancedSourceExportTest.quote(ReferenceProjects.original(p))).append(',')
                    .append(AdvancedSourceExportTest.quote(ReferenceProjects.baked(p,loadedDefinitions.get(i)))).append(",{");
            for(boolean mute:new boolean[]{false,true}){
                var changed=ReferenceProjects.changed(p,mute);
                cpp.append('{').append(AdvancedSourceExportTest.quote(ReferenceProjects.original(changed))).append(',').append(AdvancedSourceExportTest.quote(ReferenceProjects.baked(changed,loadedDefinitions.get(i)))).append("},");
            }
            cpp.append("},factory);\n");
        }
        cpp.append("}catch(const std::exception& e){std::cerr<<e.what()<<'\\n';return 1;}}\n");
        var source=dir.resolve("references.cpp");Files.writeString(source,cpp);
        Files.copy(Path.of("native/audio-host/benchmarks/ReferenceHarness.hpp"),dir.resolve("ReferenceHarness.hpp"),StandardCopyOption.REPLACE_EXISTING);
        // Compilation is separate from the standalone measurement; no other build runs during it.
        RateConverterTest.run(List.of(System.getProperty("easyknack.test.cxx"),"-std=c++17","-O3","-ffp-contract=off","-static","-I",dir.toString(),"-I",Path.of("native/audio-host/src").toAbsolutePath().toString(),"-I",Path.of("target/native/generated").toAbsolutePath().toString(),source.toString(),Path.of("native/audio-host/src/nodes/LorisLibrary.cpp").toAbsolutePath().toString(),"-o",dir.resolve("references.exe").toString()));
        var process=new ProcessBuilder(dir.resolve("references.exe").toString(),dir.toString()).redirectErrorStream(true).redirectOutput(dir.resolve("run.log").toFile()).start();
        try{
            assertTrue(process.waitFor(10,java.util.concurrent.TimeUnit.MINUTES),"Reference matrix timed out");
            assertEquals(0,process.exitValue(),Files.readString(dir.resolve("run.log")));
        }finally{if(process.isAlive()){process.destroyForcibly();process.waitFor();}}
    }

    @Test @EnabledIfSystemProperty(named="easyknack.test.references",matches="true")
    void referenceExportsInstallTogetherAndLoadFromCache() throws Exception {
        var dir=Path.of("target/reference-v1").toAbsolutePath();Files.createDirectories(dir);
        var projects=ReferenceProjects.all();var entries=new LinkedHashMap<String,String>();var names=new ArrayList<String>();
        for(var p:projects){
            var e=GroupSourceExporter.create(p.document(),p.group());
            names.add("com.vin.easyknack.nodes."+e.className());
            e.files().forEach((name,value)->{if(name.startsWith("java/")||name.startsWith("native/"))entries.put(name,value);});
        }
        entries.put("plugin.properties",new com.vin.easyknack.plugins.PluginManifest("reference_v1","1.0.0",names).text());
        var archive=dir.resolve("all-references.knackplugin");
        try(var zip=new java.util.zip.ZipOutputStream(Files.newOutputStream(archive))){
            for(var e:entries.entrySet()){zip.putNextEntry(new java.util.zip.ZipEntry(e.getKey()));zip.write(e.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8));zip.closeEntry();}
        }
        // Isolated installation: no activation in the user's plugin directory.
        var manager=new com.vin.easyknack.plugins.PluginManager(dir.resolve("installed"));
        var build=manager.install(archive,System.out::println).build();
        assertEquals(projects.size(),build.definitions().size());
        var cached=new com.vin.easyknack.plugins.PluginManager(dir.resolve("installed")).startup(System.out::println);
        assertTrue(cached.cached());assertEquals(build.directory(),cached.directory());
        Files.writeString(dir.resolve("installed-host.txt"),build.host().toString());
        try(var host=new com.vin.easyknack.audio.AudioHostClient()){
            host.start(build.host(),true);
            for(var p:projects){
                var exported=GroupSourceExporter.create(p.document(),p.group());
                var definition=build.definitions().stream().filter(d->d.processor().id().equals(exported.definition().processor().id())).findFirst().orElseThrow();
                assertEquals(NativeContractGenerator.fingerprint(exported.definition()),NativeContractGenerator.fingerprint(definition));
                var command=ReferenceProjects.baked(p,definition);host.command(command);host.command("PLAY");
                if(p.midi())host.command("MIDI_KEYBOARD 2 "+(144|(60<<8)|(100<<16))+" "+(144|(67<<8)|(100<<16)));
                Thread.sleep(100);host.command(command);host.command("MIDI_PANIC");host.command("STOP");
            }
            host.command("QUIT");
        }
    }
}
