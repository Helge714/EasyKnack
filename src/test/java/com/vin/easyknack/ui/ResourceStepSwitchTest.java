package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;

class ResourceStepSwitchTest {
    static final List<StepSwitchNode.Type> RESOURCES=Arrays.stream(StepSwitchNode.Type.values()).filter(t->t!=StepSwitchNode.Type.VALUE).toList();
    static MultiSpectrumOutputTest.Fixture fixture(StepSwitchNode.Type type,boolean instances){
        var base=MultipleGroups.create();var graph=new GLNodeGraph();var step=add(graph,new StepSwitchNode());
        graph.edit("Typed switch",e->{e.setProperty(step,"type",GLNodeValue.of(type.id()));e.setProperty(step,"mode",GLNodeValue.of("index"));e.setProperty(step,"steps",GLNodeValue.of(2.));e.setProperty(step,"next",GLNodeValue.of(1.));});
        BasicNode source=switch(type){case VECTOR->new VectorCombineNode();case WAVETABLE->new WaveformNode();case BUFFER->new HistoryBufferNode();default->new FFTStreamNode();};
        var sourceId=add(graph,source);String sourcePort=switch(type){case VECTOR->"vector";case WAVETABLE->"waveform";case BUFFER->"history";default->"spectrum";};
        if(source instanceof FFTStreamNode||source instanceof HistoryBufferNode){var osc=add(graph,new OscillatorNode());if(source instanceof FFTStreamNode)graph.edit("Small FFT",e->e.setProperty(sourceId,"size",GLNodeValue.of("256")));WideGroupInputsTest.connect(graph,osc,"audio-out",sourceId,source instanceof FFTStreamNode?"in":"audio-in");}
        for(int i=0;i<2;i++)WideGroupInputsTest.connect(graph,sourceId,sourcePort,step,"step-"+i);
        var ports=new ArrayList<GLCompositePortMapping>();
        if(instances){var context=add(graph,new InstanceContextNode());var merge=add(graph,new InstanceCombineNode());graph.edit("Sum indices",e->e.setProperty(merge,"mode",GLNodeValue.of("sum")));WideGroupInputsTest.connect(graph,context,"index",step,"next");WideGroupInputsTest.connect(graph,step,"index",merge,"in");ports.add(MultipleGroups.countPort("Count"));ports.add(output("out",SignalCategory.AUDIO,merge,"out"));}
        else ports.add(output("out",type.signal,step,"value"));
        var descriptor=new GLCompositeNodeDescriptor(base.compositeDescriptor().nodeTypeId(),base.id(),1,ports);
        Map<String,GLNodeValue> metadata=instances?Map.of(MultipleGroups.MARKER,GLNodeValue.of(true),MultipleGroups.MAXIMUM,GLNodeValue.of("2"),MultipleGroups.EXECUTION,GLNodeValue.of(2.)):Map.of();
        var asset=new GLNodeGraphAsset(base.id(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),base.viewState(),metadata,descriptor);
        var root=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of(GroupUnits.key("Count"),GLNodeValue.of(2.)));root.edit("Root",e->e.addNode(instance));
        return new MultiSpectrumOutputTest.Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),base.viewState()),Map.of(asset.id(),asset),List.of()),instance);
    }
    @Test void typedPortsViewsSelectionAndPersistence()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var d=new StepSwitchNode();var id=GLNodeId.random();c.workspace().currentGraph().edit("Switch",e->e.addNode(new GLGraphNode(id,d.registration().id(),d.defaults())));
            for(var type:StepSwitchNode.Type.values()){
                c.setNodeProperty(id,StepSwitchNode.TYPE,GLNodeValue.of(type.id()));
                var model=c.workspace().currentGraph().snapshot().findNode(id).orElseThrow();var rows=d.layout(new NodeState(model.properties())).rows();
                var input=rows.stream().filter(r->r.key().equals(StepSwitchNode.input(0))).findFirst().orElseThrow();assertEquals(type.dataType,input.dataType());assertEquals(type==StepSwitchNode.Type.VALUE,input.editor()!=null);
                var view=(StepSwitchView)c.workspace().editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();view.selectStep(2);assertEquals(2,view.activeStep());
                assertFalse(view.findPort("step-0").isContentHiddenWhenConnected());
            }
            var bytes=new java.io.ByteArrayOutputStream();var codec=new KnackDocumentCodec();codec.write(c.snapshotDocument(),bytes);var doc=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
            assertEquals("analysis",doc.root().graph().findNode(id).orElseThrow().properties().get("type").toJavaValue());
        }catch(Exception e){throw new AssertionError(e);}});
    }
    @Test void resourceGroupsCompileAndExport(){for(var type:RESOURCES)for(boolean instances:List.of(false,true)){var f=fixture(type,instances);if(instances)assertNotNull(MultiSpectrumOutputTest.compiled(f));assertNotNull(GroupSourceExporter.create(f.document(),f.instance()));}}

    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void routesResourcesWithoutCopyingAndDefersMidBlockSelection()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"resource-switch-").toAbsolutePath();var d=new StepSwitchNode();var checks=new StringBuilder();
        for(var type:RESOURCES){var values=new HashMap<>(d.defaults());values.put("type",GLNodeValue.of(type.id()));values.put("steps",GLNodeValue.of(2.));values.put("mode",GLNodeValue.of("index"));checks.append("check(").append(quote("!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(values),p->p.equals(StepSwitchNode.NEXT)?-1:2))).append(");\n");}
        String numeric="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->-1);String legacy="!13e990f788f8b055 "+numeric.substring(numeric.indexOf(' ')+1,numeric.lastIndexOf(' '));
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(NodeRegistry.builtinDefinitions()));
        String cpp="""
            #include "nodes/StepSwitchNode.hpp"
            #include <cassert>
            #include <sstream>
            using namespace easyknack;
            struct Resource:DspResource {ResourceKind k;Resource(ResourceKind k):k(k){} ResourceKind kind()const noexcept override{return k;}};
            void check(const char* payload){
                StepSwitchNode n;std::istringstream p(payload);n.configure(p);n.bindIdentity(payload);
                Resource a(n.resourceKind),b(n.resourceKind),wrong(ResourceKind::none);
                const DspResource* resources[34]{};resources[2]=&a;resources[3]=&b;
                const float* inputs[34]{};char rates[34];std::fill_n(rates,34,'c');float output[4]{},index[4]{},silence[4]{};float* outputs[]{output,index};
                ProcessContext c{inputs,34,output,4,48000,silence,4};c.outputs=outputs;c.outputCount=2;c.sourceRates=rates;c.resourceInputs=resources;
                n.process(c);assert(n.resourceOutput(0)==&a&&n.resourceOutput(1)==nullptr&&index[0]==0);
                n.selectedIndex=1;n.process(c);assert(n.resourceOutput(0)==&b&&index[0]==1);
                resources[3]=nullptr;n.process(c);assert(n.resourceOutput(0)==nullptr);
                resources[3]=&wrong;n.process(c);assert(n.resourceOutput(0)==nullptr);resources[3]=&b;
                n.indexed=false;n.index=0;ValueEventBuffer next;next.clear(4);next.push(2,1,ValueEventKind::trigger);const ValueEventBuffer* events[34]{};events[0]=&next;c.valueEventInputs=events;
                n.process(c);assert(n.resourceOutput(0)==&a&&n.index==1);for(float v:index)assert(v==0);
                next.clear(4);n.process(c);assert(n.resourceOutput(0)==&b&&index[0]==1);
                n.index=0;submitManualTrigger(std::string(payload)+":select-1");++manualTriggerEpoch;n.process(c);assert(n.resourceOutput(0)==&b);
                // Control evaluations still consume later triggers, but expose the selected block-start resource.
                n.index=0;next.push(3,1,ValueEventKind::trigger);c.bufferSize=1;n.process(c);assert(n.resourceOutput(0)==&a&&n.index==1);next.clear(4);n.process(c);assert(n.resourceOutput(0)==&b);
            }
            int main(){CHECKS StepSwitchNode old;std::istringstream legacy(LEGACY);old.configure(legacy);assert(old.resourceKind==ResourceKind::none&&old.steps==4);}
            """.replace("CHECKS",checks).replace("LEGACY",quote(legacy));
        var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeInstanceAndExportedResourcesRemainTyped()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"resource-switch-export-").toAbsolutePath();var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());var includes=new StringBuilder();var checks=new StringBuilder();
        for(var type:RESOURCES)for(boolean instances:List.of(false,true)){
            var f=fixture(type,instances);var export=GroupSourceExporter.create(f.document(),f.instance());var def=export.definition();definitions.add(def);Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));includes.append("#include \"").append(export.className()).append(".hpp\"\n");
            checks.append("check<").append(export.className()).append(">(").append(quote("!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(def.defaults()),p->-1))).append(',').append(instances).append(");\n");
            if(instances){var node=MultiSpectrumOutputTest.compiled(f);var owner=NodeRegistry.require(node.typeId());checks.append("check<MultipleGraphNode>(").append(quote("!"+NativeContractGenerator.fingerprint(owner)+" "+owner.encode(new NodeState(node.properties()),p->-1))).append(",true);\n");}
        }
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String cpp="""
            #include "nodes/MultipleGraphNode.hpp"
            INCLUDES
            #include <cassert>
            using namespace easyknack;
            void collect(NodeProcessor& n,std::vector<StepSwitchNode*>& out){
                if(auto* s=dynamic_cast<StepSwitchNode*>(&n))out.push_back(s);
                if(auto* g=dynamic_cast<MultipleGraphNode*>(&n))for(auto& i:g->state->instances)for(auto& child:i->runtime->graph.nodes)collect(*child.processor,out);
                else if(auto* g=dynamic_cast<BakedGraphProcessor*>(&n))for(auto& child:g->runtime->graph.nodes)collect(*child.processor,out);
            }
            template<class T>void check(const char* payload,bool instances){
                auto node=std::make_unique<T>();std::istringstream p(payload);node->configure(p);node->prepare(48000,64);node->bindIdentity(payload);
                float out[64]{},silence[64]{},count=2;const float* inputs[]{&count};char rates[]{'c'};ProcessContext c{inputs,1,out,64,48000,silence,64};c.sourceRates=rates;
                for(int block=0;block<8;block++)node->process(c);
                std::vector<StepSwitchNode*> steps;collect(*node,steps);assert(steps.size()==(instances?2:1));
                for(unsigned i=0;i<steps.size();i++){assert(steps[i]->resource);assert(steps[i]->index==(instances?i:1));}
                if(instances)for(float v:out)assert(v==1);else assert(node->resourceOutput(0)==steps[0]->resource);
            }
            int main(){CHECKS}
            """.replace("INCLUDES",includes).replace("CHECKS",checks);
        var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
