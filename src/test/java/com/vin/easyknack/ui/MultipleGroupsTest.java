package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class MultipleGroupsTest {
    record Fixture(GLNodeGraphDocument document,GLGraphNode instance){}
    static Fixture fixture(boolean stereo,String mode){
        var asset=MultipleGroups.create();var metadata=new HashMap<>(asset.metadata());metadata.put(MultipleGroups.MAXIMUM,GLNodeValue.of("4"));metadata.put(MultipleGroups.MERGE,GLNodeValue.of(mode));
        if(stereo){metadata.remove(MultipleGroups.EXECUTION);
            var graph=new GLNodeGraph(asset.graph());var context=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof InstanceContextNode).findFirst().orElseThrow();
            graph.edit("Fixture",e->{for(var n:graph.snapshot().nodes())if(!n.id().equals(context.id()))e.removeNode(n.id());});
            var channel=add(graph,new ContextInputNode());var math=add(graph,new MathNode());graph.edit("Stereo",e->{e.setProperty(math,"operation",GLNodeValue.of("add"));e.setProperty(math,"clamp",GLNodeValue.of(false));e.addConnection(SpectralGroupsTest.wire(context.id(),"index",math,"a"));e.addConnection(SpectralGroupsTest.wire(channel,"channel",math,"b"));});
            var d=asset.compositeDescriptor();var ports=d.portMappings().stream().filter(p->!p.publicPort().id().equals("frequency")).map(p->p.publicPort().id().equals("out")?p.withEndpoints(List.of(new GLNodeEndpoint(math,"audio-out"))):p).toList();
            asset=new GLNodeGraphAsset(asset.id(),1,asset.kind(),graph.snapshot(),asset.viewState(),metadata,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,ports));
        }else asset=new GLNodeGraphAsset(asset.id(),1,asset.kind(),asset.graph(),asset.viewState(),metadata,asset.compositeDescriptor());
        var graph=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of(MultipleGroups.COUNT_PROPERTY,GLNodeValue.of(4.)));graph.edit("Group",e->e.addNode(instance));
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),instance);
    }
    static GLGraphNode compiled(Fixture f){var registry=new GLCompositeGraphRegistry();registry.registerDocument(f.document());var flat=SpectralGroups.flatten(GroupCompiler.prepare(f.document().root(),registry),new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(flat.isValid());return flat.graph().orElseThrow().nodes().get(0);}
    @Test void creationNavigationPersistenceAndExport()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            assertFalse(c.availableNodes().stream().anyMatch(e->e.type().id().equals(new InstanceContextNode().registration().id())));
            c.addMultipleGroup(300,150);c.actions().openSelectedComposite();assertTrue(c.isMultipleContext());
            assertTrue(c.availableNodes().stream().anyMatch(e->e.type().id().equals(new InstanceContextNode().registration().id())));
            assertTrue(c.availableNodes().stream().anyMatch(e->e.type().id().equals(new DigitalNoiseNode().registration().id())));
            assertTrue(c.availableNodes().stream().anyMatch(e->e.type().id().equals(new SpectrumNode().registration().id())));
            assertDoesNotThrow(()->NodeDocuments.prepare(c.snapshotDocument()));
        }});
        var f=fixture(false,"mean");assertEquals(new MultipleGraphNode().registration().id(),compiled(f).typeId());assertEquals(compiled(f).properties().get("program"),compiled(f).properties().get("program"),"Stable node identities are required for state handover");assertNotNull(GroupSourceExporter.create(f.document(),f.instance()));
        var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(f.document(),bytes);var restored=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());assertEquals(NodeDocuments.comparable(f.document()),NodeDocuments.comparable(restored));
        var additive=new AdditiveOscillatorNode();assertFalse(additive.defaults().containsKey("noise"));
        var registry=new NodeRegistry(new GLNodeTypeRegistry(),new GLNodeCatalog());assertFalse(registry.normalize(new GLGraphNode(GLNodeId.random(),additive.registration().id(),Map.of("noise",GLNodeValue.of(.5)))).properties().containsKey("noise"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void exportedGroupAndSeededNoiseWork()throws Exception{
        var f=fixture(true,"mean");var export=GroupSourceExporter.create(f.document(),f.instance());var def=export.definition();
        String payload="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(def.defaults()),p->0);
        var dir=Files.createTempDirectory(Path.of("target"),"multiple-export-").toAbsolutePath();Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(def);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String code="""
            #include "%s.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                %s group;std::istringstream parameters(%s);group.configure(parameters);group.prepare(48000,256);
                float count=4;std::array<float,256> out{},silence{};const float* inputs[]{&count};char rates[]{'c'};
                ProcessContext c{inputs,1,out.data(),256,48000,silence.data(),256};c.sourceRates=rates;group.process(c);assert(std::abs(out[255]-.5)<1e-5);
                c.channelIndex=1;group.process(c);assert(std::abs(out[255]-2.5)<1e-5);
                DigitalNoiseNode a,b;a.strengthValue=b.strengthValue=1;a.seedValue=b.seedValue=123;
                std::array<float,256> x{},y{};const float* absent[3]{};ProcessContext n{absent,3,x.data(),256,48000,silence.data(),256};
                for(int block=0;block<3;block++){n.output=x.data();a.process(n);n.output=y.data();b.process(n);assert(x==y);}
                b.seedValue=124;n.output=x.data();a.process(n);n.output=y.data();b.process(n);assert(x!=y);
                InstanceContextNode context;float data[4]{};float* outputs[]{&data[0],&data[1],&data[2],&data[3]};
                ProcessContext info{nullptr,0,data,1,48000,silence.data(),1};info.outputs=outputs;info.outputCount=4;
                context.index=3;context.count=2;context.process(info);assert(data[2]==1);
                context.count=1;context.process(info);assert(data[2]==0);

            }
            """.formatted(export.className(),export.className(),quote(payload));
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void stereoMixCountFadesAndIndependentOscillatorState()throws Exception{
        var code=new StringBuilder("#include \"nodes/MultipleGraphNode.hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){\n");
        for(String mode:List.of("sum","mean")){
            var n=compiled(fixture(true,mode));var def=NodeRegistry.require(n.typeId());String payload="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(n.properties()),p->p.equals(MultipleGraphNode.COUNT)?0:-1);
            code.append("{MultipleGraphNode node;std::istringstream params(").append(quote(payload)).append(");node.configure(params);node.prepare(48000,256);float count=4;std::array<float,256> left{},right{},silence{};const float* inputs[9]{&count};char rates[9]{};std::fill_n(rates,9,'c');ProcessContext c{inputs,9,left.data(),256,48000,silence.data(),256};c.sourceRates=rates;auto render=[&](){c.channelIndex=0;c.output=left.data();node.process(c);c.channelIndex=1;c.output=right.data();node.process(c);};render();assert(std::abs(left[255]- ")
                .append(mode.equals("sum")?2:.5).append(")<1e-5);assert(std::abs(right[255]- ").append(mode.equals("sum")?10:2.5).append(")<1e-5);count=2;render();assert(std::abs(left[255]- ").append(mode.equals("sum")?-1:-.5).append(")<1e-5);assert(std::abs(right[255]- ").append(mode.equals("sum")?3:1.5).append(")<1e-5);assert(left[0]!=left[255]);count=0;render();assert(std::abs(left[255])<1e-5&&std::abs(right[255])<1e-5);count=100;render();assert(std::abs(left[255]- ").append(mode.equals("sum")?2:.5).append(")<1e-5);}\n");
        }
        var n=compiled(fixture(false,"mean"));var def=NodeRegistry.require(n.typeId());String payload="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(n.properties()),p->-1);
        code.append("{MultipleGraphNode node;std::istringstream params(").append(quote(payload)).append(");node.configure(params);node.prepare(48000,256);std::array<float,256> out{},silence{};const float* inputs[9]{};char rates[9]{};std::fill_n(rates,9,'c');ProcessContext c{inputs,9,out.data(),256,48000,silence.data(),256};c.sourceRates=rates;node.process(c);auto phase=[](auto& instance){for(auto& n:instance.runtime->graph.nodes)if(auto* osc=dynamic_cast<OscillatorNode*>(n.processor.get()))return osc->phase;return -1.;};double first=phase(*node.state->instances[0]);double second=phase(*node.state->instances[1]);assert(first>=0&&second>=0&&first!=second);MultipleGraphNode replacement;std::istringstream again(").append(quote(payload)).append(");replacement.configure(again);replacement.prepare(48000,256);replacement.adoptState(node);assert(phase(*replacement.state->instances[0])==first);assert(phase(*replacement.state->instances[1])==second);}\n}");
        var dir=Files.createTempDirectory(Path.of("target"),"multiple-").toAbsolutePath();var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    static Fixture combinedFixture(String mode,boolean invalid){
        var base=MultipleGroups.create();var g=new GLNodeGraph();
        var context=add(g,new InstanceContextNode());var a=add(g,new InstanceCombineNode());var b=add(g,new InstanceCombineNode());var mix=add(g,new MathNode());
        g.edit("Shared reductions",e->{
            e.setProperty(a,"mode",GLNodeValue.of("sum"));e.setProperty(b,"mode",GLNodeValue.of(mode));
            e.setProperty(mix,"operation",GLNodeValue.of("add"));e.setProperty(mix,"clamp",GLNodeValue.of(false));
            e.addConnection(SpectralGroupsTest.wire(context,"index",a,"in"));e.addConnection(SpectralGroupsTest.wire(context,"index",b,"in"));
            e.addConnection(SpectralGroupsTest.wire(context,"position",b,"weight"));
            e.addConnection(SpectralGroupsTest.wire(a,"out",mix,"a"));e.addConnection(SpectralGroupsTest.wire(invalid?context:b,invalid?"index":"out",mix,"b"));
        });
        var d=base.compositeDescriptor();var ports=List.of(new GLCompositePortMapping(GLNodePortDescriptor.output("out",SignalCategory.AUDIO.valueTypeId()),new GLNodeEndpoint(mix,"audio-out")));
        var metadata=new HashMap<>(base.metadata());metadata.put(MultipleGroups.MAXIMUM,GLNodeValue.of("4"));
        var asset=new GLNodeGraphAsset(base.id(),1,base.kind(),g.snapshot(),base.viewState(),metadata,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,ports));
        var root=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of(MultipleGroups.COUNT_PROPERTY,GLNodeValue.of(4.)));root.edit("Group",e->e.addNode(instance));
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),instance);
    }
    @Test void sharedAreaIsLoweredOnceAndInvalidCrossingRejected(){
        var f=combinedFixture("mean",false);var registry=new GLCompositeGraphRegistry();registry.registerDocument(f.document());
        var result=SpectralGroups.flatten(GroupCompiler.prepare(f.document().root(),registry),new GLCompositeExpansionLimits(16,256,512,10000)).graph().orElseThrow();
        assertEquals(2,result.nodes().size(),"One instance engine plus one shared Math");
        assertEquals(2,result.connections().size());
        assertEquals(1,result.nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof MathNode).count());
        assertThrows(IllegalArgumentException.class,()->compiled(combinedFixture("mean",true)));
        assertTrue(new InstanceContextNode().layout(new NodeState(Map.of())).ports().stream().noneMatch(p->p.direction()==GLPortDirection.INPUT));
        var asset=MultipleGroups.create();var managed=asset.compositeDescriptor().portMappings().stream().filter(MultipleGroups::countPort).findFirst().orElseThrow();
        assertFalse(managed.isBound());assertTrue(managed.presentation().applicationManaged());
        var groups=new GLCompositeGraphRegistry();groups.registerAsset(asset);var types=new GLNodeTypeRegistry();types.register(asset.compositeDescriptor().nodeType());
        assertThrows(IllegalArgumentException.class,()->GLCompositeInterfaceTransactions.removePort(groups,types,asset.id(),managed.publicPort().id(),new com.vingame.glswing.history.GLHistoryManager(),"Delete Count"));
    }
    @Test void countNamedInputIsOrdinaryAndDeadCombinesArePruned(){
        var f=fixture(false,"mean");var asset=f.document().embeddedAssets().values().iterator().next();var d=asset.compositeDescriptor();
        var ports=d.portMappings().stream().map(p->p.publicPort().id().equals("frequency")?new GLCompositePortMapping(GLNodePortDescriptor.input("count",SignalCategory.VALUE.valueTypeId(),false,1),p.internalEndpoint(),p.presentation()):p).toList();
        var g=new GLNodeGraph(asset.graph());add(g,new InstanceCombineNode());
        var changed=new GLNodeGraphAsset(asset.id(),1,asset.kind(),g.snapshot(),asset.viewState(),asset.metadata(),new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,ports));
        var compiled=compiled(new Fixture(new GLNodeGraphDocument(f.document().root(),Map.of(asset.id(),changed),List.of()),f.instance()));
        assertEquals(4,new NodeState(compiled.properties()).number("count",0));
        assertEquals(1,new NodeState(compiled.properties()).number("outputs",0),"Disconnected Combine must not schedule another reduction");
    }
    @Test void oldUnconnectedCountMigratesWithoutChangingValue(){
        var f=fixture(false,"mean");var asset=f.document().embeddedAssets().values().iterator().next();var d=asset.compositeDescriptor();
        var context=asset.graph().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof InstanceContextNode).findFirst().orElseThrow();
        var ports=new ArrayList<>(d.portMappings().stream().filter(p->!MultipleGroups.countPort(p)).toList());ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input("count",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(context.id(),"count"),new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,Map.of("default",GLNodeValue.of(8.)))));
        var old=new GLNodeGraphAsset(asset.id(),1,asset.kind(),asset.graph(),asset.viewState(),asset.metadata(),new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,ports));
        var root=new GLNodeGraph(f.document().root().graph());var driver=add(root,new ValueNode());root.edit("Old count",e->{e.setProperty(f.instance().id(),GroupUnits.key("count"),GLNodeValue.of(3.));e.addConnection(SpectralGroupsTest.wire(driver,"value-out",f.instance().id(),"count"));});
        var oldRoot=new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty());
        var migrated=NodeDocuments.prepare(new GLNodeGraphDocument(oldRoot,Map.of(old.id(),old),List.of()));
        assertEquals(root.snapshot().connections(),migrated.root().graph().connections());
        assertEquals(3,new NodeState(migrated.root().graph().findNode(f.instance().id()).orElseThrow().properties()).number(GroupUnits.key("count"),0));
        assertTrue(migrated.embeddedAssets().get(old.id()).compositeDescriptor().portMappings().stream().filter(p->p.publicPort().id().equals("count")).allMatch(p->MultipleGroups.countPort(p)&&!p.isBound()));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void multipleCombinesAndSharedProcessingExport()throws Exception{
        for(String mode:List.of("sum","mean","weighted")){
            var f=combinedFixture(mode,false);var export=GroupSourceExporter.create(f.document(),f.instance());var def=export.definition();
            String payload="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(def.defaults()),p->-1);
            var dir=Files.createTempDirectory(Path.of("target"),"combine-export-").toAbsolutePath();Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
            var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(def);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
            double expected=6+(mode.equals("sum")?14.0/3:mode.equals("mean")?14.0/12:7.0/3);
            String code="""
                #include "%s.hpp"
                #include <cassert>
                using namespace easyknack;
                int main(){%s node;std::istringstream params(%s);node.configure(params);node.prepare(48000,256);
                    std::array<float,256> out{},silence{};ProcessContext c{nullptr,0,out.data(),256,48000,silence.data(),256};
                    for(int block=0;block<3;block++){c.channelIndex=0;node.process(c);assert(std::abs(out[255]-%s)<1e-5);c.channelIndex=1;node.process(c);assert(std::abs(out[255]-%s)<1e-5);}
                }
                """.formatted(export.className(),export.className(),quote(payload),expected,expected);
            var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
        }
    }

}
