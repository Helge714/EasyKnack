package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class ExportEditorParityTest {
    static Fixture fixture(){
        var graph=new GLNodeGraph();var osc=add(graph,new OscillatorNode());var math=add(graph,new MathNode());var pulse=add(graph,new TriggerPulseNode());
        connect(graph,osc,"audio-out",math,"a");
        var pitch=new GLCompositePortMapping(GLNodePortDescriptor.input("Pitch",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(osc,"frequency"),new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,false,Map.of("default",GLNodeValue.of(330.),"minimum",GLNodeValue.of(20.),"maximum",GLNodeValue.of(20000.),"presentation",GLNodeValue.of("buttons"),GroupInputSettings.RATE,GLNodeValue.of("control"),GroupUnits.BASE,GLNodeValue.of("hz"),GroupUnits.ALLOWED,GLNodeValue.of(List.of("hz")))));
        var enabled=new GLCompositePortMapping(GLNodePortDescriptor.input("Enabled",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(math,"b"),new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,Map.of("presentation",GLNodeValue.of("checkbox"),"default",GLNodeValue.of(1.))));
        var d=new GLCompositeNodeDescriptor("test.editors","test.editors.asset",1,List.of(pitch,input("Wave",SignalCategory.WAVETABLE,osc,"waveform"),enabled,input("Reset",SignalCategory.TRIGGER,pulse,"trigger"),output("Out",SignalCategory.AUDIO,math,"audio-out"),output("Pulse",SignalCategory.AUDIO,pulse,"audio-out")));
        var asset=new GLNodeGraphAsset(d.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),d);
        var registry=new GLCompositeGraphRegistry();registry.registerAsset(asset);
        var template=new EditorTemplates.Template("waves","Waves",List.of(new EditorTemplates.Entry("one","Sine",GLNodeValue.of(0.)),new EditorTemplates.Entry("two","Triangle",GLNodeValue.of(1.))),"one");
        asset=GroupInputs.applyTemplate(asset,d.portMappings().get(1),template,registry);
        var group=new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of());var root=new GLNodeGraph();root.edit("Group",e->e.addNode(group));
        var out=add(root,new OutputNode());connect(root,group.id(),"Out",out,"audio-in");
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),group,"",false);
    }
    @Test void generatedClassRetainsOrderedEditorsAndSockets()throws Exception{
        var f=fixture();var export=GroupSourceExporter.create(f.document(),f.group());
        var dir=Files.createTempDirectory(Path.of("target"),"editor-export-").toAbsolutePath();var source=dir.resolve(export.className()+".java");Files.writeString(source,export.files().get("java/com/vin/easyknack/nodes/"+export.className()+".java"));
        var diagnostics=new java.io.ByteArrayOutputStream();assertEquals(0,javax.tools.ToolProvider.getSystemJavaCompiler().run(null,diagnostics,diagnostics,"-proc:none","-classpath",System.getProperty("java.class.path"),"-d",dir.toString(),source.toString()),diagnostics.toString());
        try(var loader=new java.net.URLClassLoader(new java.net.URL[]{dir.toUri().toURL()},getClass().getClassLoader())){
            var loaded=(BasicNode)loader.loadClass("com.vin.easyknack.nodes."+export.className()).getConstructor().newInstance();assertEquals(NativeContractGenerator.fingerprint(export.definition()),NativeContractGenerator.fingerprint(loaded));
            var rows=loaded.layout(new NodeState(loaded.defaults())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.INPUT).toList();
            assertEquals(List.of("Pitch","Wave","Enabled","Reset"),rows.stream().map(NodeBuilder.Row::label).toList());
            assertFalse(rows.get(0).socketVisible());assertEquals(PortRates.CONTROL,rows.get(0).rates());assertInstanceOf(UnitSlider.class,rows.get(0).editor());assertInstanceOf(Editor.ComboBox.class,rows.get(1).editor());assertInstanceOf(Editor.Toggle.class,rows.get(2).editor());assertInstanceOf(Editor.Trigger.class,rows.get(3).editor());
        }
    }
    @Test void hiddenComboKeepsItsPositionBetweenInputs(){
        var f=fixture();var a=f.document().embeddedAssets().get("test.editors.asset");var d=a.compositeDescriptor();
        var ports=new ArrayList<>(d.portMappings());var wave=ports.get(1);
        ports.set(1,new GLCompositePortMapping(wave.publicPort(),wave.internalEndpoint(),new GLCompositePortPresentation(wave.presentation().inputEditorId(),false,wave.presentation().editorOptions()),wave.additionalEndpoints()));
        a=GroupInputs.descriptor(a,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,ports,d.properties(),d.propertyMappings()));
        var export=GroupSourceExporter.create(new GLNodeGraphDocument(f.document().root(),Map.of(a.id(),a),List.of()),f.group());
        var rows=export.definition().layout(new NodeState(export.definition().defaults())).rows().stream().filter(r->r.kind()!=NodeBuilder.RowKind.OUTPUT&&!r.isInspectorOnly()).toList();
        assertEquals(List.of("Pitch","Wave","Enabled","Reset"),rows.stream().map(NodeBuilder.Row::label).toList());
        assertInstanceOf(Editor.ComboBox.class,rows.get(1).editor());assertEquals(NodeBuilder.RowKind.EDITOR,rows.get(1).kind());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void exportedComboFallbackAndManualTriggerWork()throws Exception{
        var f=fixture();var export=GroupSourceExporter.create(f.document(),f.group());var d=export.definition();
        var dir=Files.createTempDirectory(Path.of("target"),"editor-export-native-").toAbsolutePath();Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var defs=new ArrayList<BasicNode>(NodeRegistry.definitionsList());defs.add(d);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(defs));
        String patch="GRAPH 2 baked "+d.processor().id()+" !"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->-1)+" @ aa ccce out out 0 0 @ a a";
        var cpp=new StringBuilder("#include \"AudioGraph.hpp\"\n#include \"").append(export.className()).append(".hpp\"\n#include <cassert>\n#include <iostream>\nusing namespace easyknack;int main(){auto factory=[](const std::string& id)->std::unique_ptr<NodeProcessor>{if(id==").append(quote(d.processor().id())).append(")return std::make_unique<").append(export.className()).append(">();return createNodeProcessor(id);};auto baked=AudioGraph::compile(").append(quote(patch)).append(",factory);auto live=AudioGraph::parse(").append(quote(reference(f))).append(");std::array<float,256>a{},b{};for(int block=0;block<8;block++)for(int ch=0;ch<2;ch++){baked.process(a.data(),256,ch,nullptr);live.process(b.data(),256,ch,nullptr);for(int i=0;i<256;i++)assert(std::abs(a[i]-b[i])<1e-5);}");
        for(String selection:List.of("one","two")){
            var values=new HashMap<>(d.defaults());values.put("input_1",GLNodeValue.of(selection));
            var asset=f.document().embeddedAssets().get("test.editors.asset");var port=asset.compositeDescriptor().portMappings().get(1);
            var group=f.group().withProperty(GroupInputs.property(port),GLNodeValue.of(selection));
            var root=f.document().root();var nodes=root.graph().nodes().stream().map(n->n.id().equals(group.id())?group:n).toList();
            var doc=new GLNodeGraphDocument(new GLNodeGraphAsset(root.id(),root.version(),root.kind(),new GLNodeGraphSnapshot(root.graph().graphId(),0,nodes,root.graph().connections()),root.viewState()),f.document().embeddedAssets(),List.of());
            String selectedPatch="GRAPH 2 baked "+d.processor().id()+" !"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(values),p->-1)+" @ aa ccce out out 0 0 @ a a";
            cpp.append("{auto selected=AudioGraph::compile(").append(quote(selectedPatch)).append(",factory);auto expected=AudioGraph::parse(").append(quote(reference(new Fixture(doc,group,"",false)))).append(");for(int block=0;block<8;block++){selected.process(a.data(),256);expected.process(b.data(),256);for(int i=0;i<256;i++)assert(std::abs(a[i]-b[i])<1e-5);}}\n");
        }
        cpp.append("auto& processor=static_cast<BakedGraphProcessor&>(*baked.nodes[0].processor);submitManualTrigger(\"baked:input_3\");++manualTriggerEpoch;baked.process(a.data(),256,0,nullptr);auto& input=processor.runtime->graph.nodes[processor.runtime->inputs[3]];assert(input.valueEvents[0][0].count>0);assert(processor.runtime->graph.nodes[processor.runtime->outputs[1]].channelCache[0].outputs[0][0]==1.f);std::cout<<\"Editor export passed\\n\";}");
        var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
