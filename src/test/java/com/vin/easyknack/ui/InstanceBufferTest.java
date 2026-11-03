package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;

class InstanceBufferTest {
    static MultipleGroupsTest.Fixture fixture(boolean external, boolean shared, boolean position) {
        var base=MultipleGroups.create();var graph=new GLNodeGraph();
        var reader=add(graph,position?new PositionReaderNode():new HistoryReaderNode());var merge=add(graph,new InstanceCombineNode());
        graph.edit("Buffer instances",e->{
            e.setProperty(merge,"mode",GLNodeValue.of("mean"));
            e.addConnection(SpectralGroupsTest.wire(reader,"audio-out",merge,"in"));
        });
        var ports=new ArrayList<>(List.of(new GLCompositePortMapping(GLNodePortDescriptor.output("out",SignalCategory.AUDIO.valueTypeId()),new GLNodeEndpoint(merge,"out")),MultipleGroups.countPort("Count")));
        if(external){
            var endpoints=new ArrayList<>(List.of(new GLNodeEndpoint(reader,"history")));
            if(shared){
                var second=add(graph,position?new PositionReaderNode():new HistoryReaderNode());var sum=add(graph,new MathNode());
                graph.edit("Shared buffer",e->{e.setProperty(sum,"operation",GLNodeValue.of("add"));e.setProperty(sum,"clamp",GLNodeValue.of(false));e.addConnection(SpectralGroupsTest.wire(merge,"out",sum,"a"));e.addConnection(SpectralGroupsTest.wire(second,"audio-out",sum,"b"));});
                ports.set(0,ports.get(0).withEndpoints(List.of(new GLNodeEndpoint(sum,"audio-out"))));
                endpoints.add(new GLNodeEndpoint(second,"history"));
            }
            ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input("buffer",SignalCategory.HISTORY.valueTypeId(),false,1),endpoints.get(0)).withEndpoints(endpoints));
        }else{
            var wave=add(graph,new HistoryBufferNode());var value=add(graph,new ValueNode());
            graph.edit("Internal buffer",e->{e.setProperty(value,"value",GLNodeValue.of(.25));e.addConnection(SpectralGroupsTest.wire(value,"value-out",wave,"audio-in"));e.addConnection(SpectralGroupsTest.wire(wave,"history",reader,"history"));});
        }
        var metadata=new HashMap<>(base.metadata());metadata.put(MultipleGroups.MAXIMUM,GLNodeValue.of("2"));
        var asset=new GLNodeGraphAsset(base.id(),1,base.kind(),graph.snapshot(),base.viewState(),metadata,new GLCompositeNodeDescriptor(base.compositeDescriptor().nodeTypeId(),base.id(),1,ports));
        var root=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of(MultipleGroups.COUNT_PROPERTY,GLNodeValue.of(2.)));
        root.edit("Instance",e->e.addNode(instance));
        if(external){var wave=add(root,new HistoryBufferNode());root.edit("External buffer",e->e.addConnection(SpectralGroupsTest.wire(wave,"history",instance.id(),"buffer")));}
        return new MultipleGroupsTest.Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),instance);
    }
    @Test void internalAndExternalBuffersCompileAndExport(){
        assertTrue(MultipleGroups.supports(new HistoryBufferNode()));
        assertTrue(MultipleGroups.supports(new HistoryReaderNode()));
        assertTrue(MultipleGroups.supports(new PositionReaderNode()));
        for(boolean position:List.of(false,true))for(boolean external:List.of(false,true))for(boolean shared:List.of(false,true)){
            if(!external&&shared)continue;
            var f=fixture(external,shared,position);var node=MultipleGroupsTest.compiled(f);
            if(external)assertTrue(NodeRegistry.require(node.typeId()).layout(new NodeState(node.properties())).rows().stream().anyMatch(r->r.kind()==NodeBuilder.RowKind.INPUT&&r.dataType()==PortDataType.HISTORY));
            assertNotNull(GroupSourceExporter.create(f.document(),f.instance()));
        }
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void liveAndExportedBuffersReachBothChannels()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"instance-buffer-").toAbsolutePath();
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());var includes=new StringBuilder();var checks=new StringBuilder();
        for(boolean position:List.of(false,true))for(int mode=1;mode<3;mode++){
            boolean external=mode>0,shared=mode==2;var f=fixture(external,shared,position);
            var node=MultipleGroupsTest.compiled(fixture(external,false,position));var d=NodeRegistry.require(node.typeId());
            String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(node.properties()),p->-1);
            var export=GroupSourceExporter.create(f.document(),f.instance());var def=export.definition();definitions.add(def);
            Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
            includes.append("#include \"").append(export.className()).append(".hpp\"\n");
            String exported="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(def.defaults()),p->external?0:-1);
            checks.append("check<MultipleGraphNode>(").append(quote(payload)).append(',').append(external).append(",false);\n");
            checks.append("check<").append(export.className()).append(">(").append(quote(exported)).append(',').append(external).append(',').append(shared).append(");\n");
        }
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String cpp="""
            #include "nodes/MultipleGraphNode.hpp"
            EXPORT_HEADERS
            #include <cassert>
            using namespace easyknack;
            template<class T>void check(const char* payload,bool external,bool shared){
                T node;std::istringstream params(payload);node.configure(params);node.prepare(48000,256);
                HistoryResource left,right;left.prepare(48000,1,256);right.prepare(48000,1,256);
                std::fill(left.samples.begin(),left.samples.end(),.25f);std::fill(right.samples.begin(),right.samples.end(),.5f);left.immutable=right.immutable=true;left.end=left.samples.size();right.end=right.samples.size();
                std::array<float,256> out{},silence{};const float* inputs[9]{};char rates[9]{};std::fill_n(rates,9,'c');
                const DspResource* resources[9]{};const DspResource* rightResources[9]{};
                if(external){std::fill_n(resources,9,&left);std::fill_n(rightResources,9,&right);}
                ProcessContext c{inputs,9,out.data(),256,48000,silence.data(),256};c.sourceRates=rates;c.resourceInputs=resources;c.rightResourceInputs=rightResources;
                for(int block=0;block<3;block++){
                    c.channelIndex=0;node.process(c);for(float value:out)assert(std::abs(value-.25f*(shared?2:1))<1e-5);
                    c.channelIndex=1;node.process(c);for(float value:out)assert(std::abs(value-(external?.5f:.25f)*(shared?2:1))<1e-5);
                }
                std::fill_n(resources,9,nullptr);std::fill_n(rightResources,9,nullptr);
                for(int channel=0;channel<2;channel++){c.channelIndex=channel;node.process(c);for(float value:out)assert(value==0);}
            }
            int main(){CHECKS}
            """.replace("EXPORT_HEADERS",includes.toString()).replace("CHECKS",checks.toString());
        var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
