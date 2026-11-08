package com.vin.easyknack.ui;
import com.vin.easyknack.compiler.CompiledManualTargets;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;import java.nio.file.*;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;import com.vingame.glswing.node.composite.*;import com.vingame.glswing.node.document.*;import com.vingame.glswing.node.model.*;
class InstanceStepSwitchTest {
    record Fixture(MultiSpectrumOutputTest.Fixture group,GLNodeId step){}
    static Fixture fixture(boolean indexed,boolean nested){
        var asset=MultipleGroups.create();var graph=new GLNodeGraph();var context=add(graph,new InstanceContextNode());var step=add(graph,new StepSwitchNode());var merge=add(graph,new InstanceCombineNode());
        graph.edit("Switch",e->{e.setProperty(step,"steps",GLNodeValue.of(2.));e.setProperty(step,"step-0",GLNodeValue.of(10.));e.setProperty(step,"step-1",GLNodeValue.of(20.));e.setProperty(step,"mode",GLNodeValue.of(indexed?"index":"trigger"));e.setProperty(merge,"mode",GLNodeValue.of("sum"));e.addConnection(SpectralGroupsTest.wire(step,"value",merge,"in"));});
        if(indexed)WideGroupInputsTest.connect(graph,context,"index",step,"next");
        else {var add=add(graph,new MathNode());var multiply=add(graph,new MathNode());var clock=add(graph,new ClockNode());graph.edit("Independent clocks",e->{e.setProperty(add,"operation",GLNodeValue.of("add"));e.setProperty(add,"b",GLNodeValue.of(2.));e.setProperty(add,"clamp",GLNodeValue.of(false));e.setProperty(multiply,"operation",GLNodeValue.of("multiply"));e.setProperty(multiply,"b",GLNodeValue.of(750.));e.setProperty(multiply,"clamp",GLNodeValue.of(false));});WideGroupInputsTest.connect(graph,context,"index",add,"a");WideGroupInputsTest.connect(graph,add,"audio-out",multiply,"a");WideGroupInputsTest.connect(graph,multiply,"audio-out",clock,"frequency");WideGroupInputsTest.connect(graph,clock,"value-out",step,"next");}
        var descriptor=new GLCompositeNodeDescriptor(asset.compositeDescriptor().nodeTypeId(),asset.id(),1,List.of(MultipleGroups.countPort("Count"),output("out",SignalCategory.AUDIO,merge,"out")));
        var meta=new HashMap<>(asset.metadata());meta.put(MultipleGroups.MAXIMUM,GLNodeValue.of("2"));asset=new GLNodeGraphAsset(asset.id(),1,asset.kind(),graph.snapshot(),GLNodeEditorViewState.empty(),meta,descriptor);
        var assets=new HashMap<String,GLNodeGraphAsset>();assets.put(asset.id(),asset);
        if(nested){var outer=MultipleGroups.create();var body=new GLNodeGraph();var child=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of(GroupUnits.key("Count"),GLNodeValue.of(2.)));body.edit("Child",e->e.addNode(child));var combine=add(body,new InstanceCombineNode());body.edit("Sum",e->e.setProperty(combine,"mode",GLNodeValue.of("sum")));WideGroupInputsTest.connect(body,child.id(),"out",combine,"in");descriptor=new GLCompositeNodeDescriptor(outer.compositeDescriptor().nodeTypeId(),outer.id(),1,List.of(MultipleGroups.countPort("Count"),output("out",SignalCategory.AUDIO,combine,"out")));assets.put(outer.id(),new GLNodeGraphAsset(outer.id(),1,outer.kind(),body.snapshot(),GLNodeEditorViewState.empty(),meta,descriptor));}
        var root=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of(GroupUnits.key("Count"),GLNodeValue.of(2.)));root.edit("Root",e->e.addNode(instance));
        return new Fixture(new MultiSpectrumOutputTest.Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),assets,List.of()),instance),step);
    }
    @Test void catalogCompilationAndNestedAliasesIncludeBothModes()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){c.addMultipleGroup(0,0);c.actions().openSelectedComposite();assertTrue(c.availableNodes().stream().anyMatch(e->e.type().id().equals(new StepSwitchNode().registration().id())));}});
        for(boolean indexed:List.of(false,true))for(boolean nested:List.of(false,true)){
            var f=fixture(indexed,nested);var node=MultiSpectrumOutputTest.compiled(f.group());var aliases=CompiledManualTargets.read(node);assertTrue(aliases.containsKey(f.step().toString()));assertEquals(1,aliases.get(f.step().toString()).size());assertNotNull(GroupSourceExporter.create(f.group().document(),f.group().instance()));
        }
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void independentSelectionsTriggersRepresentativeAndExports()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"instance-switch-").toAbsolutePath();var defs=new ArrayList<>(NodeRegistry.builtinDefinitions());var includes=new StringBuilder();var calls=new StringBuilder();
        for(boolean indexed:List.of(false,true))for(boolean nested:List.of(false,true)){
            var f=fixture(indexed,nested);var node=MultiSpectrumOutputTest.compiled(f.group());var def=NodeRegistry.require(node.typeId());var export=GroupSourceExporter.create(f.group().document(),f.group().instance());defs.add(export.definition());Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));includes.append("#include \"").append(export.className()).append(".hpp\"\n");
            String target=CompiledManualTargets.read(node).get(f.step().toString()).get(0);
            calls.append("check<MultipleGraphNode>(").append(quote("!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(node.properties()),p->-1))).append(',').append(quote(target)).append(',').append(indexed).append(',').append(nested?4:2).append(");\n");
            calls.append("check<").append(export.className()).append(">(").append(quote("!"+NativeContractGenerator.fingerprint(export.definition())+" "+export.definition().encode(new NodeState(export.definition().defaults()),p->-1))).append(',').append(quote(target)).append(',').append(indexed).append(',').append(nested?4:2).append(");\n");
        }
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(defs));
        String cpp="""
            #include "AudioGraph.hpp"
            INCLUDES
            #include <cassert>
            using namespace easyknack;
            void collect(NodeProcessor& node,std::vector<StepSwitchNode*>& out){
                if(auto* step=dynamic_cast<StepSwitchNode*>(&node))out.push_back(step);
                if(auto* group=dynamic_cast<MultipleGraphNode*>(&node))for(auto& instance:group->state->instances)for(auto& child:instance->runtime->graph.nodes)collect(*child.processor,out);
                else if(auto* baked=dynamic_cast<BakedGraphProcessor*>(&node))for(auto& child:baked->runtime->graph.nodes)collect(*child.processor,out);
            }
            template<class T> void check(const char* payload,const char* target,bool indexed,unsigned replicas){
                auto node=std::make_unique<T>();std::istringstream p(payload);node->configure(p);node->prepare(48000,64);node->bindIdentity("switch-test");
                std::vector<StepSwitchNode*> steps;collect(*node,steps);assert(steps.size()==replicas);
                for(unsigned i=0;i<replicas;i++){assert(bool(steps[i]->mailbox)==(i==0));assert(bool(steps[i]->frame)==(i==0));}
                float count=2,out[64]{},silence[64]{};const float* inputs[]={&count};char rates[]{'c'};ProcessContext c{inputs,1,out,64,48000,silence,64};c.sourceRates=rates;
                ++manualTriggerEpoch;node->process(c);for(unsigned i=0;i<replicas;i++)assert(steps[i]->index==i%2);assert(std::abs(out[63]-15.f*replicas)<1e-6);
                AnalysisFrame report;assert(steps[0]->mailbox->latest(report));assert(report.frequency==steps[0]->index);
                if(!indexed){submitManualTrigger(std::string("switch-test/")+target+":select-1");++manualTriggerEpoch;node->process(c);assert(std::abs(out[0]-20.f*replicas)<1e-6);}
                // Routine model feedback must not reset the other replicas.
                StepSwitchNode incoming;incoming.steps=2;incoming.index=0;incoming.indexed=indexed;incoming.values[0]=10;incoming.values[1]=20;
                for(auto* step:steps){auto before=step->index;step->applyParameters(incoming);assert(step->index==before);}
                incoming.selectionRevision=1;incoming.index=1;for(auto* step:steps){step->applyParameters(incoming);assert(step->index==1);}
            }
            int main(){CALLS}
            """.replace("INCLUDES",includes).replace("CALLS",calls);
        var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
