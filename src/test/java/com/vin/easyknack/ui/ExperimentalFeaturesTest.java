package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.vin.easyknack.nodes.*;

class ExperimentalFeaturesTest {
    @TempDir Path directory;
    @Test void optInControlsCreationWithoutRemovingExistingGraphs()throws Exception{
        String previous=System.getProperty("easyknack.experimental");
        try{
            System.setProperty("easyknack.experimental","false");
            SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
                String pitch=new PitchHoldNode().registration().id();
                assertTrue(new PitchHoldNode().isExperimental());assertFalse(new MathNode().isExperimental());
                assertTrue(new PitchHoldNode().title().contains(AppText.text("experimental")));
                assertFalse(c.availableNodes().stream().anyMatch(e->e.type().id().equals(pitch)));
                System.setProperty("easyknack.experimental","true");
                var entry=c.availableNodes().stream().filter(e->e.type().id().equals(pitch)).findFirst().orElseThrow();
                System.setProperty("easyknack.experimental","false");
                int count=c.workspace().currentGraph().snapshot().nodes().size();
                c.workspace().createNode(entry,100,100);assertEquals(count,c.workspace().currentGraph().snapshot().nodes().size());
                var document=HarmonizerExample.create();assertTrue(ExperimentalFeatures.requiresOptIn(document));

                c.openDocument("Existing experiment",document);
                assertEquals(document.embeddedAssets().keySet(),c.snapshotDocument().embeddedAssets().keySet());
            }});
        }finally{restore(previous);}
    }
    @Test void exampleDiscoveryLabelsAndHidesExperimentalDependencies()throws Exception{
        String previous=System.getProperty("easyknack.experimental");
        try{
            var file=directory.resolve("Renamed experiment.knack");
            try(var out=Files.newOutputStream(file)){new com.vin.easyknack.document.KnackDocumentCodec().write(HarmonizerExample.create(),out);}
            System.setProperty("easyknack.experimental","false");assertTrue(ExampleLibrary.scan(directory).isEmpty());
            System.setProperty("easyknack.experimental","true");
            var entries=ExampleLibrary.scan(directory);assertEquals(1,entries.size());assertTrue(entries.get(0).name().contains(AppText.text("experimental")));
            // The cache must observe edited files as well as the live preference.
            try(var out=Files.newOutputStream(file)){new com.vin.easyknack.document.KnackDocumentCodec().write(StarterDocument.create(),out);}
            System.setProperty("easyknack.experimental","false");assertEquals(1,ExampleLibrary.scan(directory).size());
        }finally{restore(previous);}
    }
    @Test void exportedPluginRetainsExperimentalMetadata()throws Exception{
        var document=HarmonizerExample.create();
        var group=document.root().graph().nodes().stream().filter(n->n.typeId().equals("easyknack.group.harmonizer")).findFirst().orElseThrow();
        var export=GroupSourceExporter.create(document,group);assertTrue(export.definition().isExperimental());
        var source=directory.resolve(export.className()+".java");Files.writeString(source,export.files().get("java/com/vin/easyknack/nodes/"+export.className()+".java"));
        var log=new java.io.ByteArrayOutputStream();
        assertEquals(0,javax.tools.ToolProvider.getSystemJavaCompiler().run(null,log,log,"-proc:none","-classpath",System.getProperty("java.class.path"),"-d",directory.toString(),source.toString()),log.toString());
        try(var loader=new java.net.URLClassLoader(new java.net.URL[]{directory.toUri().toURL()},getClass().getClassLoader())){
            var compiled=(BasicNode)loader.loadClass("com.vin.easyknack.nodes."+export.className()).getConstructor().newInstance();
            assertTrue(compiled.isExperimental());
        }
    }
    private static void restore(String previous){if(previous==null)System.clearProperty("easyknack.experimental");else System.setProperty("easyknack.experimental",previous);}
}
