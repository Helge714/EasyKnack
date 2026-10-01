package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.plugins.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;

class GroupBakeTest {
    @Test void forcedBakeRegeneratesEvenEnabledGroupWithUniqueBuildIdentity()throws Exception{
        var p=ReferenceProjects.make("osc-gain-filter",0);
        var def=definition(GroupSourceExporter.create(p.document(),p.group(),true));
        var requests=new CopyOnWriteArrayList<Map<String,String>>();
        var manager=new GroupBakes(p::document,(files,progress)->{
            requests.add(files);
            return new PluginManager.Build(Path.of("target/forced-bake-"+requests.size()),List.of(def),Map.of(),false);
        },()->{});
        try{
            edt(()->manager.bake(p.group().id()));waitFor(manager);
            edt(()->manager.bake(p.group().id()));assertEquals(1,requests.size());
            edt(()->manager.bake(p.group().id(),true));waitFor(manager);
            edt(()->manager.bake(p.group().id(),true));waitFor(manager);
            assertEquals(3,requests.size());
            assertFalse(requests.get(0).containsKey("bake-request.txt"));
            assertNotEquals(requests.get(1).get("bake-request.txt"),requests.get(2).get("bake-request.txt"));
            assertEquals(requests.get(0).get("plugin.properties"),requests.get(2).get("plugin.properties"));
            edt(()->{assertTrue(manager.enabled(p.group().id()));assertEquals(Path.of("target/forced-bake-3/easyknack-audio-host.exe"),manager.host());});
        }finally{edt(manager::close);NodeRegistry.installPlugins(List.of());}
    }
    static void edt(Runnable action)throws Exception{SwingUtilities.invokeAndWait(action);}
    static void waitFor(GroupBakes bakes)throws Exception{
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(20);var busy=new AtomicBoolean(true);
        while(busy.get()&&System.nanoTime()<deadline){edt(()->busy.set(bakes.busy()));if(busy.get())Thread.sleep(10);}
        assertFalse(busy.get(),"Bake did not finish");
    }
    static BasicNode definition(GroupSourceExporter.Export source)throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"bake-java-").toAbsolutePath();
        var file=dir.resolve(source.className()+".java");Files.writeString(file,source.files().get("java/com/vin/easyknack/nodes/"+source.className()+".java"));
        assertEquals(0,javax.tools.ToolProvider.getSystemJavaCompiler().run(null,null,null,"-proc:none","-classpath",System.getProperty("java.class.path"),"-d",dir.toString(),file.toString()));
        try(var loader=new java.net.URLClassLoader(new java.net.URL[]{dir.toUri().toURL()},GroupBakeTest.class.getClassLoader())){
            var definition=(BasicNode)loader.loadClass("com.vin.easyknack.nodes."+source.className()).getConstructor().newInstance();definition.defaults();return definition;
        }
    }
    static GLNodeGraphDocument changed(GLNodeGraphDocument doc){
        var assets=new LinkedHashMap<>(doc.embeddedAssets());
        var original=assets.values().iterator().next();var graph=new GLNodeGraph(original.graph());
        var node=graph.snapshot().nodes().stream().filter(n->NodeRegistry.find(n.typeId()).orElse(null) instanceof OscillatorNode).findFirst().orElseThrow();
        graph.edit("Frequency",e->e.setProperty(node.id(),"frequency",GLNodeValue.of(880)));
        assets.put(original.id(),new GLNodeGraphAsset(original.id(),original.version(),original.kind(),graph.snapshot(),original.viewState(),original.metadata(),original.compositeDescriptor()));
        return new GLNodeGraphDocument(doc.root(),assets,doc.externalAssets());
    }
    @Test void internalMuteAndUnmuteRegenerateTheOriginalSource()throws Exception{
        var p=MultipleGroupsTest.fixture(false,"mean");
        var original=p.document();var assets=new LinkedHashMap<>(original.embeddedAssets());
        var asset=assets.values().iterator().next();var graph=new GLNodeGraph(asset.graph());
        var gain=graph.snapshot().nodes().stream().filter(n->NodeRegistry.find(n.typeId()).orElse(null) instanceof MathNode).findFirst().orElseThrow();
        graph.edit("Mute",e->e.setProperty(gain.id(),GLNodeState.MUTED,GLNodeValue.of(true)));
        assets.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),asset.compositeDescriptor()));
        var muted=new GLNodeGraphDocument(original.root(),assets,original.externalAssets());
        var doc=new AtomicReference<>(original);var requests=new CopyOnWriteArrayList<Map<String,String>>();
        var def=definition(GroupSourceExporter.create(original,p.instance(),true));
        var manager=new GroupBakes(doc::get,(files,progress)->{requests.add(files);return new PluginManager.Build(Path.of("target/mute-bake-"+requests.size()),List.of(def),Map.of(),false);},()->{});
        try{
            edt(()->manager.bake(p.instance().id()));waitFor(manager);
            edt(()->{doc.set(muted);manager.validate();assertFalse(manager.enabled(p.instance().id()));manager.bake(p.instance().id());});waitFor(manager);
            edt(()->assertTrue(manager.enabled(p.instance().id()),manager.status(p.instance().id())));
            edt(()->{doc.set(original);manager.validate();assertFalse(manager.enabled(p.instance().id()));manager.bake(p.instance().id());});waitFor(manager);
            assertEquals(3,requests.size());assertNotEquals(requests.get(0),requests.get(1));assertEquals(requests.get(0),requests.get(2));
        }finally{edt(manager::close);NodeRegistry.installPlugins(List.of());}
    }
    @Test void localBakeReplacesOnlyExecutionAndInvalidatesOnEdit()throws Exception{
        var p=ReferenceProjects.make("osc-gain-filter",0);var export=GroupSourceExporter.create(p.document(),p.group(),true);var def=definition(export);
        var doc=new AtomicReference<>(p.document());var calls=new AtomicInteger();
        var manager=new GroupBakes(doc::get,(files,progress)->{
            calls.incrementAndGet();assertTrue(files.containsKey("plugin.properties"));progress.accept("Compiling test");
            return new PluginManager.Build(Path.of("target/fake-bake"),List.of(def),Map.of(),false);
        },()->{});
        try{
            edt(()->manager.bake(p.group().id()));waitFor(manager);
            edt(()->{
                assertTrue(manager.enabled(p.group().id()),manager.status(p.group().id()));
                var execution=manager.apply(doc.get().root());
                assertEquals(def.registration().id(),execution.graph().findNode(p.group().id()).orElseThrow().typeId());
                assertEquals(p.group().typeId(),doc.get().root().graph().findNode(p.group().id()).orElseThrow().typeId());
                assertTrue(execution.graph().connections().stream().anyMatch(c->c.output().portId().equals("output_0")));
                manager.disable(p.group().id());assertSame(doc.get().root(),manager.apply(doc.get().root()));
            });
            edt(()->manager.bake(p.group().id()));waitFor(manager);
            edt(()->{doc.set(changed(doc.get()));manager.validate();assertFalse(manager.enabled(p.group().id()));assertTrue(manager.status(p.group().id()).startsWith("Changed"));});
            assertEquals(2,calls.get());
        }finally{edt(manager::close);NodeRegistry.installPlugins(List.of());}
    }
    @Test void editDuringCompilationCannotActivateStaleResult()throws Exception{
        var p=ReferenceProjects.make("osc-gain-filter",0);var def=definition(GroupSourceExporter.create(p.document(),p.group(),true));
        var doc=new AtomicReference<>(p.document());var entered=new CountDownLatch(1);var release=new CountDownLatch(1);
        var manager=new GroupBakes(doc::get,(files,progress)->{entered.countDown();assertTrue(release.await(10,TimeUnit.SECONDS));return new PluginManager.Build(Path.of("target/stale-bake"),List.of(def),Map.of(),false);},()->{});
        try{
            edt(()->manager.bake(p.group().id()));assertTrue(entered.await(10,TimeUnit.SECONDS));
            edt(()->{doc.set(changed(doc.get()));manager.validate();});release.countDown();waitFor(manager);
            edt(()->{assertFalse(manager.enabled(p.group().id()));assertNull(manager.host());});
        }finally{release.countDown();edt(manager::close);NodeRegistry.installPlugins(List.of());}
    }
    @Test void compilerFailureRetainsEditableGroup()throws Exception{
        var p=ReferenceProjects.make("osc-gain-filter",0);
        var manager=new GroupBakes(p::document,(files,progress)->{throw new java.io.IOException("test compiler error");},()->{});
        try{edt(()->manager.bake(p.group().id()));waitFor(manager);edt(()->{
            assertTrue(manager.status(p.group().id()).contains("test compiler error"));assertFalse(manager.enabled(p.group().id()));assertSame(p.document().root(),manager.apply(p.document().root()));
        });}finally{edt(manager::close);}
    }
    @Test void controllerExecutesBakeAndReturnsToOriginalGraph()throws Exception{
        var p=ReferenceProjects.make("osc-gain-filter",0);
        var def=definition(GroupSourceExporter.create(p.document(),p.group(),true));
        var ref=new AtomicReference<WorkspaceController>();var original=new AtomicReference<String>();
        edt(()->{
            var controller=new WorkspaceController((files,progress)->new PluginManager.Build(Path.of("target/controller-bake"),List.of(def),Map.of(),false));
            ref.set(controller);controller.openDocument("Bake integration",p.document());original.set(controller.audioGraph());
            controller.bakes().bake(p.group().id());
        });
        try{
            waitFor(ref.get().bakes());
            edt(()->{
                var controller=ref.get();assertTrue(controller.bakes().enabled(p.group().id()));
                assertTrue(controller.audioGraph().contains(def.processor().id()),controller.audioGraph());
                assertEquals(p.group().typeId(),controller.snapshotDocument().root().graph().findNode(p.group().id()).orElseThrow().typeId());
                controller.bakes().disable(p.group().id());assertEquals(original.get(),controller.audioGraph());
            });
        }finally{edt(()->ref.get().close());NodeRegistry.installPlugins(List.of());}
    }
    @Test void inspectorOffersBakeWithoutPersistingCompiledNodes()throws Exception{
        edt(()->{try(var controller=new WorkspaceController()){
            var p=ReferenceProjects.make("osc-gain-filter",0);controller.openDocument("Bake test",p.document());
            var ws=controller.workspace();var view=ws.editor().getNodes().stream().filter(n->n instanceof GroupViewFactory.GroupView).findFirst().orElseThrow();
            ws.editor().setSelectedNode(view);
            var panel=ws.propertyInspector().getGLChildren().stream().filter(n->n instanceof GroupBakes.Panel).map(n->(GroupBakes.Panel)n).findFirst().orElseThrow();
            assertTrue(panel.bake.isEnabled());assertFalse(panel.disable.isEnabled());
            assertFalse(controller.snapshotDocument().root().graph().findNode(p.group().id()).orElseThrow().properties().containsKey(GroupBakes.PROPERTY));
            assertTrue(controller.audioGraph().contains("osc"));
        }});
    }
    @Test void outerSliderUpdatesBakedParameterWithoutRebuilding()throws Exception{
        var original=GroupUnitsTest.document(false);var graph=new GLNodeGraph(original.root().graph());
        graph.edit("Unit",e->e.setProperty(GroupUnitsTest.GROUP,GroupUnits.unitKey("Pitch"),GLNodeValue.of("khz")));
        var root=original.root();var doc=new AtomicReference<>(new GLNodeGraphDocument(new GLNodeGraphAsset(root.id(),root.version(),root.kind(),graph.snapshot(),root.viewState(),root.metadata(),root.compositeDescriptor()),original.embeddedAssets(),original.externalAssets()));
        var instance=graph.snapshot().findNode(GroupUnitsTest.GROUP).orElseThrow();
        var def=definition(GroupSourceExporter.create(doc.get(),instance,true));var builds=new AtomicInteger();
        var manager=new GroupBakes(doc::get,(files,progress)->{builds.incrementAndGet();return new PluginManager.Build(Path.of("target/boundary-bake"),List.of(def),Map.of(),true);},()->{});
        try{
            edt(()->manager.bake(instance.id()));waitFor(manager);
            edt(()->{
                graph.edit("Outer slider",e->e.setExplicitProperty(instance.id(),GroupUnits.key("Pitch"),GLNodeValue.of(880)));
                doc.set(new GLNodeGraphDocument(new GLNodeGraphAsset(root.id(),root.version(),root.kind(),graph.snapshot(),root.viewState(),root.metadata(),root.compositeDescriptor()),original.embeddedAssets(),original.externalAssets()));
                manager.validate();assertTrue(manager.enabled(instance.id()));
                var baked=manager.apply(doc.get().root()).graph().findNode(instance.id()).orElseThrow();
                assertEquals(.88,((Number)baked.properties().get("input_0").toJavaValue()).doubleValue(),1e-9);
                manager.bake(instance.id());assertEquals(1,builds.get());
            });
        }finally{edt(manager::close);NodeRegistry.installPlugins(List.of());}
    }
    @Test void savedBakeRestoresAutomatically()throws Exception{
        var p=ReferenceProjects.make("osc-gain-filter",0);
        var def=definition(GroupSourceExporter.create(p.document(),p.group(),true));
        var ref=new AtomicReference<WorkspaceController>();var saved=new AtomicReference<GLNodeGraphDocument>();
        GroupBakes.Builder builder=(files,progress)->new PluginManager.Build(Path.of("target/restored-bake"),List.of(def),Map.of(),true);
        edt(()->{ref.set(new WorkspaceController(builder));ref.get().openDocument("Bake",p.document());ref.get().bakes().bake(p.group().id());});
        try{
            waitFor(ref.get().bakes());
            edt(()->{saved.set(ref.get().snapshotDocument());assertTrue(saved.get().root().metadata().containsKey(GroupBakes.SAVED));ref.get().close();});
            var file=Files.createTempFile(Path.of("target"),"saved-bake-",".knack");
            try(var documents=new NodeDocuments()){
                documents.write(file,saved.get()).toCompletableFuture().get();
                var loaded=documents.read(file).toCompletableFuture().get();
                edt(()->{ref.set(new WorkspaceController(builder));ref.get().openDocument("Restored",loaded);});
            }
            var enabled=new AtomicBoolean();long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
            while(!enabled.get()&&System.nanoTime()<deadline){Thread.sleep(30);edt(()->enabled.set(ref.get().bakes().enabled(p.group().id())));}
            assertTrue(enabled.get());
            edt(()->{ref.get().bakes().disable(p.group().id());assertFalse(ref.get().snapshotDocument().root().metadata().containsKey(GroupBakes.SAVED));});
        }finally{edt(()->ref.get().close());NodeRegistry.installPlugins(List.of());}
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void transientBuildUsesCacheWithoutInstallingPluginState()throws Exception{
        var p=ReferenceProjects.make("osc-gain-filter",0);var export=GroupSourceExporter.create(p.document(),p.group(),true);
        var home=Files.createTempDirectory(Path.of("target"),"local-bakes-");var manager=new PluginManager(home);
        var files=new TreeMap<String,String>();export.files().forEach((k,v)->{if(k.startsWith("java/")||k.startsWith("native/"))files.put(k,v);});
        files.put("plugin.properties",new PluginManifest("easyknack_local_bakes","1",List.of("com.vin.easyknack.nodes."+export.className())).text());
        var build=manager.buildEphemeral(files,s->{});var cached=manager.buildEphemeral(files,s->{});
        assertTrue(cached.cached());assertEquals(build.directory(),cached.directory());
        files.put("bake-request.txt",UUID.randomUUID().toString());
        var forced=manager.buildEphemeral(files,s->{});
        assertFalse(forced.cached());assertNotEquals(build.host(),forced.host());
        assertTrue(Files.isRegularFile(build.host()),"Previous host must remain usable");
        assertFalse(Files.exists(home.resolve("active.properties")));assertFalse(Files.exists(home.resolve("sources")));
        try(var host=new com.vin.easyknack.audio.AudioHostClient()){
            host.start(build.host(),true);host.command(ReferenceProjects.baked(p,build.definitions().get(0)));host.command("PLAY");host.command("STOP");
        }
    }
}
