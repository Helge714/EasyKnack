package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.plugins.*;

class PluginIntegrationTest {
    @TempDir Path directory;
    @Test void deletedPluginIsRemovedFromStartupState() throws Exception {
        var home=directory.resolve("removed");Files.createDirectories(home);
        Files.writeString(home.resolve("active.properties"),"plugins=deleted_plugin\nbuild=missing\n");
        assertNull(new PluginManager(home).startup(s->{}));
        assertFalse(Files.exists(home.resolve("active.properties")));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void sourceExportBuildsOnceLoadsFromCacheAndRetainsGoodBuildOnFailure() throws Exception {
        var document=GroupUnitsTest.document(false);
        var exported=GroupSourceExporter.create(document,document.root().graph().findNode(GroupUnitsTest.GROUP).orElseThrow());
        var archive=directory.resolve("osc.knackplugin");exported.write(archive);
        var manager=new PluginManager(directory.resolve("plugins"));
        Files.createDirectories(directory.resolve("plugins"));
        Files.writeString(directory.resolve("plugins/active.properties"),"plugins=deleted_plugin\nbuild=missing\n");
        var messages=new java.util.ArrayList<String>();
        var installation=manager.install(archive,s->{messages.add(s);System.out.println(s);});var built=installation.build();
        assertTrue(messages.stream().anyMatch(s->s.contains("C++ 1/")&&s.contains("main.cpp")&&s.contains(" s")));
        assertTrue(messages.stream().anyMatch(s->s.contains("Linking C++ audio host")));
        assertFalse(built.cached());assertFalse(installation.restartRequired());assertEquals(1,built.definitions().size());
        assertTrue(Files.isExecutable(built.host()));
        var definition=built.definitions().get(0);
        // The loaded Java definition participates in normal model/view, persistence and native playback.
        com.vin.easyknack.nodes.NodeRegistry.installPlugins(built.definitions());
        try {
            var command=new java.util.concurrent.atomic.AtomicReference<String>();
            javax.swing.SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController();var reopened=new WorkspaceController()){
                var graph=controller.workspace().currentGraph();var id=com.vingame.glswing.node.model.GLNodeId.random();
                var sink=graph.snapshot().nodes().stream().filter(n->com.vin.easyknack.nodes.NodeRegistry.require(n.typeId()) instanceof com.vin.easyknack.nodes.OutputNode).findFirst().orElseThrow();
                graph.edit("Use plugin",e->{
                    for(var wire:graph.snapshot().connections())e.removeConnection(wire.id());
                    e.addNode(new com.vingame.glswing.node.model.GLGraphNode(id,definition.registration().id(),definition.defaults()));
                    e.addConnection(new com.vingame.glswing.node.model.GLGraphConnection(com.vingame.glswing.node.model.GLNodeConnectionId.random(),new com.vingame.glswing.node.model.GLNodeEndpoint(id,"output_0"),new com.vingame.glswing.node.model.GLNodeEndpoint(sink.id(),"audio-in")));
                });
                assertTrue(controller.workspace().editor().getNodes().stream().anyMatch(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()));
                command.set(controller.audioGraph());reopened.openDocument("Plugin",controller.snapshotDocument());assertEquals(command.get(),reopened.audioGraph());
            }});
            try(var host=new com.vin.easyknack.audio.AudioHostClient()){
                host.start(built.host(),true);assertEquals("OK",host.command(command.get()));host.command("PLAY");
                Thread.sleep(150);assertTrue(host.command("STATS").startsWith("OK "));
                assertEquals("OK",host.command(command.get()));host.command("STOP");
            }
        } finally { com.vin.easyknack.nodes.NodeRegistry.installPlugins(java.util.List.of()); }
        var startup=new PluginManager(directory.resolve("plugins")).startup(System.out::println);
        assertTrue(startup.cached());assertEquals(built.directory(),startup.directory());
        assertEquals(built.definitions().get(0).processor().id(),startup.definitions().get(0).processor().id());
        // A local bake of an already installed export must have its own native/Java identity.
        var local=GroupSourceExporter.create(document,document.root().graph().findNode(GroupUnitsTest.GROUP).orElseThrow(),true);
        var combined=manager.buildEphemeral(local.files(),s->{});
        assertEquals(2,combined.definitions().size());
        assertTrue(new PluginManager(directory.resolve("plugins")).startup(s->{}).cached());
        // Manual edits with errors neither publish a partial build nor remove the last good executable.
        var manifest=PluginManifest.read(built.directory().resolve("sources").resolve(built.sources().keySet().iterator().next()));
        var source=manager.sourcesDirectory().resolve(manifest.id()).resolve("java/com/vin/easyknack/nodes/"+exported.className()+".java");
        Files.writeString(source,"\nnot java",StandardOpenOption.APPEND);
        assertThrows(java.io.IOException.class,()->manager.rebuild(s->{}));
        var recovered=new PluginManager(directory.resolve("plugins")).startup(System.out::println);
        assertEquals(built.directory(),recovered.directory());assertTrue(Files.isRegularFile(built.host()));
        var info=built.directory().resolve("build.properties");var text=Files.readString(info);
        Files.writeString(info,text.replaceFirst("(?m)^sdk=.*$","sdk=incompatible"));
        assertThrows(java.io.IOException.class,()->new PluginManager(directory.resolve("plugins")).startup(s->{}),"An incompatible old build must not be a fallback");
    }
    @Test void archivesCannotEscapeOrOverwriteThroughDuplicatePaths() throws Exception {
        for(String[] names:new String[][]{{"../outside"},{"a", "A"},{"C:/outside"},{"a/../../outside"}}){
            var bytes=new java.io.ByteArrayOutputStream();
            try(var zip=new java.util.zip.ZipOutputStream(bytes)){for(String name:names){zip.putNextEntry(new java.util.zip.ZipEntry(name));zip.write(1);zip.closeEntry();}}
            var output=directory.resolve(java.util.UUID.randomUUID().toString());
            assertThrows(java.io.IOException.class,()->PluginSdk.unpack(new java.io.ByteArrayInputStream(bytes.toByteArray()),output));
        }
        assertFalse(Files.exists(directory.resolve("outside")));
    }
}
