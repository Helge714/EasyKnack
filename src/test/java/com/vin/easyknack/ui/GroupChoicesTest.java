package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.binding.*;

class GroupChoicesTest {
    record Fixture(GLNodeGraphDocument doc,GLGraphNode group){}
    static Fixture fixture(boolean nested){
        var inner=new GLNodeGraph();var scale=add(inner,new ScaleQuantizerNode());
        var d=new GLCompositeNodeDescriptor("test.choices","test.choices.asset",1,List.of(input("In",SignalCategory.VALUE,scale,"frequency"),output("Out",SignalCategory.VALUE,scale,"out")));
        var asset=new GLNodeGraphAsset(d.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),GLNodeEditorViewState.empty(),Map.of(),d);
        var n=inner.snapshot().findNode(scale).orElseThrow();
        for(var prop:List.of("mode","root","scale"))asset=GroupChoices.expose(asset,new GroupChoices.Candidate(scale,prop,prop,GroupChoices.nodeEditor(n,prop)),prop);
        var assets=new LinkedHashMap<String,GLNodeGraphAsset>();assets.put(asset.id(),asset);
        if(nested){
            var g=new GLNodeGraph();var child=new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of());g.edit("Child",e->e.addNode(child));
            var outer=new GLCompositeNodeDescriptor("test.outer.choices","test.outer.choices.asset",1,List.of(input("In",SignalCategory.VALUE,child.id(),"In"),output("Out",SignalCategory.VALUE,child.id(),"Out")),asset.compositeDescriptor().properties(),asset.compositeDescriptor().properties().stream().map(p->new GLCompositePropertyMapping(p.id(),child.id(),p.id())).toList());
            asset=new GLNodeGraphAsset(outer.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,g.snapshot(),GLNodeEditorViewState.empty(),Map.of(),outer);assets.put(asset.id(),asset);
        }
        var root=new GLNodeGraph();var group=new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of("mode",GLNodeValue.of("fixed")));
        root.edit("Group",e->e.addNode(group));return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),assets,List.of()),group);
    }
    @Test void nestedChoicesRoundTripAndExportJavaKeepsTheirLabels()throws Exception{
        for(boolean nested:new boolean[]{false,true}){
            var f=fixture(nested);var registry=new GLCompositeGraphRegistry();registry.registerDocument(f.doc);
            var asset=registry.findAsset(registry.findComposite(f.group.typeId()).orElseThrow().assetId()).orElseThrow();
            assertEquals(3,GroupChoices.choices(asset,registry).size());
            var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(f.doc,bytes);assertEquals(f.doc,codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
            var changed=f.group.withProperty("root",GLNodeValue.of("2"));var g=f.doc.root().graph();
            var doc=new GLNodeGraphDocument(new GLNodeGraphAsset(f.doc.root().id(),1,GLNodeGraphAssetKind.PROJECT,new GLNodeGraphSnapshot(g.graphId(),g.revision(),List.of(changed),g.connections()),f.doc.root().viewState()),f.doc.embeddedAssets(),List.of());
            assertEquals(GroupBakes.key(f.doc,f.group.id()),GroupBakes.key(doc,f.group.id()));
            var export=GroupSourceExporter.create(f.doc,f.group);var d=export.definition();
            assertEquals("fixed",d.defaults().get("choice_0").toJavaValue());
            assertTrue(export.files().get("native/"+export.className()+".hpp").contains("p.choice_1_value"));
            var dir=Files.createTempDirectory(Path.of("target"),"choice-java-");var source=dir.resolve(export.className()+".java");Files.writeString(source,export.files().get("java/com/vin/easyknack/nodes/"+export.className()+".java"));
            var log=new java.io.ByteArrayOutputStream();assertEquals(0,javax.tools.ToolProvider.getSystemJavaCompiler().run(null,log,log,"-proc:none","-classpath",System.getProperty("java.class.path"),"-d",dir.toString(),source.toString()),log.toString());
            try(var loader=new java.net.URLClassLoader(new java.net.URL[]{dir.toUri().toURL()},getClass().getClassLoader())){
                var generated=(BasicNode)loader.loadClass("com.vin.easyknack.nodes."+export.className()).getConstructor().newInstance();
                assertEquals(NativeContractGenerator.fingerprint(d),NativeContractGenerator.fingerprint(generated));
                assertEquals(d.defaults().get("choice_1"),generated.defaults().get("choice_1"));
            }
        }
    }
    @Test void groupViewAndUndoUseComboboxes()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var doc=HarmonizerExample.create();c.openDocument("Example",doc);var group=doc.root().graph().nodes().stream().filter(n->n.typeId().equals("easyknack.group.harmonizer")).findFirst().orElseThrow();
            var view=c.workspace().editor().getNodes().stream().filter(n->n.getTitle().equals("Harmonizer")).findFirst().orElseThrow(()->new IllegalStateException(c.workspace().editor().getNodes().stream().map(n->n.getTitle()).toList().toString()));
            var combos=view.getContents().stream().filter(com.vingame.glswing.ui.GLComboBox.class::isInstance).toList();assertEquals(3,combos.size());
            @SuppressWarnings("unchecked") var mode=(com.vingame.glswing.ui.GLComboBox<GroupChoices.Option>)combos.get(0);
            mode.setSelectedItem(mode.getItems().stream().filter(o->o.id().equals("scale")).findFirst().orElseThrow());
            assertEquals("scale",c.snapshotDocument().root().graph().findNode(group.id()).orElseThrow().properties().get("Mode").toJavaValue());
            c.history().undo();assertEquals("fixed",mode.getSelectedItem().id());
            assertTrue(c.audioGraph().contains("scalequantizer"));
        }});
    }
    @Test void exposedChoicesAreUnifiedInputsWithHiddenSockets()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var f=fixture(false);c.openDocument("Choices",f.doc());
            var asset=c.snapshotDocument().embeddedAssets().get("test.choices.asset");
            for(String id:List.of("mode","root","scale")){
                var port=asset.compositeDescriptor().portMappings().stream().filter(p->GroupInputs.choice(p)&&GroupInputs.property(p).equals(id)).findFirst().orElseThrow();
                assertFalse(port.presentation().socketVisible());
                var view=c.workspace().editor().getNodes().get(0);
                assertTrue(view.getPorts().stream().anyMatch(p->p.getId().equals(port.publicPort().id())&&!p.isSocketVisible()));
            }
            assertTrue(c.workspace().openComposite(asset.id()));c.workspace().inspectorTabs().setSelectedIndex(3);
            assertEquals(4,c.workspace().inspectorTabs().getTabCount());
        }});
    }
    @Test void changingAnOuterChoiceKeepsTheBakeAndUpdatesItsParameter()throws Exception{
        var f=fixture(false);var export=GroupSourceExporter.create(f.doc,f.group,true);var definition=GroupBakeTest.definition(export);
        var document=new java.util.concurrent.atomic.AtomicReference<>(f.doc);
        var manager=new GroupBakes(document::get,(files,progress)->new com.vin.easyknack.plugins.PluginManager.Build(Path.of("target/choice-bake"),List.of(definition),Map.of(),false),()->{});
        try{
            GroupBakeTest.edt(()->manager.bake(f.group.id()));GroupBakeTest.waitFor(manager);
            GroupBakeTest.edt(()->{
                var graph=new GLNodeGraph(document.get().root().graph());graph.edit("Root",e->e.setProperty(f.group.id(),"root",GLNodeValue.of("2")));
                document.set(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty()),f.doc.embeddedAssets(),List.of()));
                manager.validate();assertTrue(manager.enabled(f.group.id()),manager.status(f.group.id()));
                assertEquals("2",manager.apply(document.get().root()).graph().findNode(f.group.id()).orElseThrow().properties().get("choice_1").toJavaValue());
            });
        }finally{GroupBakeTest.edt(manager::close);NodeRegistry.installPlugins(List.of());}
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void exportedChoicesChangeActualDspWithoutRecompiling()throws Exception{
        var f=fixture(true);var export=GroupSourceExporter.create(f.doc,f.group);var d=export.definition();
        var dir=Files.createTempDirectory(Path.of("target"),"choice-native-").toAbsolutePath();Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var defs=new ArrayList<>(NodeRegistry.builtinDefinitions());defs.add(d);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(defs));
        StringBuilder code=new StringBuilder("""
            #include "AudioGraph.hpp"
            #include "%s.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){float input=360,out=0,silence=0;const float* inputs[]{&input};char rates[]{'c'};
            ProcessContext c{inputs,1,&out,1,48000,&silence,256};c.sourceRates=rates;
            """.formatted(export.className()));
        var settings=List.of(List.of("fixed","0","chromatic"),List.of("scale","0","major"),List.of("scale","2","major"),List.of("scale","2","minor"));
        double[] expected={440,349.2282314,369.9944227,349.2282314};
        for(int i=0;i<settings.size();i++){
            var values=new HashMap<>(d.defaults());for(int j=0;j<3;j++)values.put("choice_"+j,GLNodeValue.of(settings.get(i).get(j)));
            var payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(values),p->0);
            code.append("{").append(export.className()).append(" node;std::istringstream p(").append(quote(payload)).append(");node.configure(p);node.prepare(48000,256);node.process(c);assert(std::abs(out-").append(expected[i]).append(")<.01);assert(!node.supportsLiveParameters());}\n");
        }code.append("}");var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
