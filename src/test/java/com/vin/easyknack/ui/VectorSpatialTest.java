package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class VectorSpatialTest {
    @Test void vectorRatesTypesUnitsAndStereoControls(){
        var combine=new VectorCombineNode();var state=AutomaticRates.resolveProperties(combine,combine.defaults(),Map.of(VectorCombineNode.X,ProcessingRate.AUDIO));
        var layout=combine.layout(new NodeState(state));
        assertEquals(ProcessingRate.AUDIO,layout.rows().get(0).rate());
        assertEquals(PortDataType.VECTOR,layout.rows().get(0).dataType());
        assertEquals(1,UnitFormat.CENTIMETRE.convert(100,UnitFormat.METRE));
        assertEquals(1000,UnitFormat.METRE.convert(1,UnitFormat.MILLIMETRE));
        assertFalse(UnitFormat.METRE.compatible(UnitFormat.SECOND));
        var receiver=new SpatialReceiverNode();var mono=new HashMap<>(receiver.defaults());mono.put("mode",GLNodeValue.of("mono"));
        assertFalse(receiver.layout(new NodeState(mono)).rows().stream().anyMatch(r->r.key().equals(SpatialReceiverNode.SPACING)));
        var resolved=AutomaticRates.resolveProperties(receiver,receiver.defaults(),Map.of(SpatialReceiverNode.IN,ProcessingRate.AUDIO));
        assertEquals(ProcessingRate.CONTROL,receiver.layout(new NodeState(resolved)).rows().get(1).rate());
        resolved=AutomaticRates.resolveProperties(receiver,receiver.defaults(),Map.of(SpatialReceiverNode.SOURCE,ProcessingRate.AUDIO));
        assertEquals(ProcessingRate.AUDIO,receiver.layout(new NodeState(resolved)).rows().get(1).rate());
    }
    @Test void vectorEditorUsesCoreGroupAndPersistsUnit(){
        var editor=new VectorEditor(1,2,3);var changed=new ArrayList<GLNodeValue>();var control=editor.create("Position",changed::add);
        var group=assertInstanceOf(com.vingame.glswing.ui.GLSliderGroup.class,control.component());assertEquals(3,group.getSliders().size());
        var value=VectorEditor.value(4,5,6,UnitFormat.CENTIMETRE);control.update().accept(value);
        assertEquals(400,group.getSliders().get(0).getValue());assertTrue(changed.isEmpty());
        group.getSliders().get(0).setValue(500);assertEquals(5,VectorEditor.component(new NodeState(Map.of("p",changed.get(changed.size()-1))),new PropertyId("p"),0,0));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeVectorsStereoAndPropagation()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"vector-native-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/VectorSpatialTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void encodedGraphSchedulesVectorResourcesAndReceiverChannels()throws Exception{
        var vector=new VectorNode();var properties=new HashMap<>(vector.defaults());properties.put("position",VectorEditor.value(1,2,3,UnitFormat.METRE));
        String vectorPayload="!"+NativeContractGenerator.fingerprint(vector)+" "+vector.encode(new NodeState(properties),p->-1);
        var separate=new VectorSeparateNode();String split=RateConverterTest.payload(separate,"auto",0);
        var receiver=new SpatialReceiverNode();var r=new HashMap<>(receiver.defaults());r.put("source",VectorEditor.value(1,0,0,UnitFormat.METRE));r.put("attenuation",GLNodeValue.of(false));
        String receive="!"+NativeContractGenerator.fingerprint(receiver)+" "+receiver.encode(new NodeState(r),p->p.equals(SpatialReceiverNode.IN)?0:-1);
        var dir=Files.createTempDirectory(Path.of("target"),"vector-graph-").toAbsolutePath();var cpp=dir.resolve("test.cpp");
        Files.writeString(cpp,"""
            #include "AudioGraph.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                auto g=AudioGraph::parse("GRAPH 3 v vector %s @ c - s vectorseparate %s @ c c out out 257 0 @ a a");
                float left[256]{},right[256]{};
                for(int frames:{1,17,256}){g.process(left,frames,0,nullptr);g.process(right,frames,1,nullptr);for(int i=0;i<frames;i++){assert(left[i]==2);assert(right[i]==2);}}
                auto r=AudioGraph::parse("GRAPH 3 v value 1 @ c - r spatialreceiver %s @ ac acccc out out 1 0 @ a a");
                for(int frames:{1,17,256}){r.process(left,frames,0,nullptr);r.process(right,frames,1,nullptr);for(int i=0;i<frames;i++){assert(std::abs(left[i])<1e-6);assert(std::abs(right[i]-1)<1e-6);}}
            }
            """.formatted(vectorPayload,split,receive));
        RateConverterTest.compileAndRun(cpp,dir.resolve("test.exe"),dir);
    }
    private static GroupSourceExporter.Export vectorExport(){
        var math=new VectorMathNode();var inner=new GLNodeGraph();var id=GLNodeId.random();
        var props=new HashMap<>(math.defaults());props.put("b",VectorEditor.value(1,2,3,UnitFormat.METRE));
        inner.edit("Vector",e->e.addNode(new GLGraphNode(id,math.registration().id(),props)));
        var descriptor=new GLCompositeNodeDescriptor("test.vector.group","vector.asset",1,List.of(
            new GLCompositePortMapping(GLNodePortDescriptor.input("Position",PortDataType.VECTOR.id(),false,1),new GLNodeEndpoint(id,"a")),
            new GLCompositePortMapping(GLNodePortDescriptor.output("Vector",PortDataType.VECTOR.id()),new GLNodeEndpoint(id,"vector"))));
        var asset=new GLNodeGraphAsset("vector.asset",1,GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var root=new GLNodeGraph();var group=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(group));
        var document=new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
        return GroupSourceExporter.create(document,group);
    }
    @Test void vectorGroupExportsTypedPorts(){
        var exported=vectorExport();
        assertTrue(exported.definition().layout(new NodeState(exported.definition().defaults())).ports().stream().allMatch(p->p.valueTypeId().equals(PortDataType.VECTOR.id())));
        assertTrue(exported.files().get("native/"+exported.className()+".hpp").contains("baked_input 0 6"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void bakedVectorBoundaryPreservesEverySample()throws Exception{
        var export=vectorExport();var dir=Files.createTempDirectory(Path.of("target"),"vector-bake-").toAbsolutePath();
        var definitions=new ArrayList<BasicNode>(NodeRegistry.definitionsList());definitions.add(export.definition());
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var d=export.definition();String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->0);
        var cpp=dir.resolve("test.cpp");Files.writeString(cpp,"""
            #include "%s.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                %s baked;std::istringstream config("%s");baked.configure(config);baked.prepare(48000,256);
                VectorResource source;source.count=4;source.samples={{1,0,0},{2,0,0},{3,0,0},{4,0,0}};
                const DspResource* resources[]{&source};float zero[256]{},output[256]{};const float* inputs[]{zero};char rates[]{'a'};
                ProcessContext c{inputs,1,output,4,48000,zero,4};c.resourceInputs=resources;c.rightResourceInputs=resources;c.sourceRates=rates;
                baked.resolveAutoRate(rates,1);baked.process(c);
                auto* result=static_cast<VectorResource*>(baked.resourceOutput(0));assert(result);assert(result->at(3)[0]==5&&result->at(3)[1]==2);
                rates[0]='c';baked.resolveAutoRate(rates,1);baked.process(c);assert(result->at(3)[0]==2);
            }
            """.formatted(export.className(),export.className(),payload));
        RateConverterTest.compileAndRun(cpp,dir.resolve("test.exe"),dir);
    }
}
