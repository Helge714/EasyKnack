package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;

class SpecializedChainTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void specializedChainMatchesOriginalPreviousExportAndHandwritten() throws Exception {
        var p=ReferenceProjects.make("osc-gain-filter",0);
        var export=GroupSourceExporter.create(p.document(),p.group());
        var dir=Path.of("target/specialized-chain").toAbsolutePath();Files.createDirectories(dir);
        var name=export.className();var header=export.files().get("native/"+name+".hpp");
        assertTrue(header.contains("if(renderSpecializedChain(frames))"));
        for(String fixture:List.of("math","stereo","synth-8","rates")){
            var other=ReferenceProjects.make(fixture,fixture.equals("synth-8")?8:0);
            var e=GroupSourceExporter.create(other.document(),other.group());
            assertFalse(e.files().get("native/"+e.className()+".hpp").contains("if(renderSpecializedChain(frames))"));
        }
        Files.writeString(dir.resolve("Export.hpp"),header);
        Files.writeString(dir.resolve("Legacy.hpp"),header.replace("struct "+name+":","struct LegacyChain:").replace("EASYKNACK_NODE("+name+",","EASYKNACK_NODE(LegacyChain,").replace("if(renderSpecializedChain(","if(false && renderSpecializedChain("));
        Files.writeString(dir.resolve("Blocks.hpp"),header.replace("struct "+name+":","struct BlockChain:").replace("EASYKNACK_NODE("+name+",","EASYKNACK_NODE(BlockChain,").replace("renderOscGainFilter<true>","renderOscGainFilter<false>"));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(export.definition());
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        Files.copy(Path.of("native/audio-host/benchmarks/SpecializedChainHarness.hpp"),dir.resolve("SpecializedChainHarness.hpp"),StandardCopyOption.REPLACE_EXISTING);
        var java=dir.resolve(name+".java");Files.writeString(java,export.files().get("java/com/vin/easyknack/nodes/"+name+".java"));
        assertEquals(0,javax.tools.ToolProvider.getSystemJavaCompiler().run(null,null,null,"-proc:none","-classpath",System.getProperty("java.class.path"),"-d",dir.toString(),java.toString()));
        String baked;
        try(var loader=new java.net.URLClassLoader(new java.net.URL[]{dir.toUri().toURL()},getClass().getClassLoader())){
            baked=ReferenceProjects.baked(p,(BasicNode)loader.loadClass("com.vin.easyknack.nodes."+name).getConstructor().newInstance());
        }
        var cpp="#include \"AudioGraph.hpp\"\n#include \"Export.hpp\"\n#include \"Legacy.hpp\"\n#include \"Blocks.hpp\"\nnamespace easyknack {using ExportChain="+name+";}\n#include \"SpecializedChainHarness.hpp\"\nint main(int argc,char** argv){try{easyknack::compareSpecializedChains("+
            AdvancedSourceExportTest.quote(ReferenceProjects.original(p))+","+AdvancedSourceExportTest.quote(baked)+","+
            AdvancedSourceExportTest.quote(export.definition().processor().id())+",argc>1?argv[1]:\"results.csv\");}catch(const std::exception& e){std::cerr<<e.what();return 1;}}";
        Files.writeString(dir.resolve("comparison.cpp"),cpp);
        RateConverterTest.run(List.of(System.getProperty("easyknack.test.cxx"),"-std=c++17","-O3","-ffp-contract=off","-static","-I",dir.toString(),"-I",Path.of("native/audio-host/src").toAbsolutePath().toString(),"-I",Path.of("target/native/generated").toAbsolutePath().toString(),dir.resolve("comparison.cpp").toString(),"-o",dir.resolve("comparison.exe").toString()));
        RateConverterTest.run(List.of(dir.resolve("comparison.exe").toString(),dir.resolve("results.csv").toString()));
    }
}
