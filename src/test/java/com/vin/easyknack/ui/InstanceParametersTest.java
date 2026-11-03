package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.nio.file.*;import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.binding.*;
class InstanceParametersTest {
 static MultipleGroupsTest.Fixture nested(){
  var child=InstanceWavetableTest.fixture(false,false);var base=MultipleGroups.create();var graph=new GLNodeGraph();
  var merge=add(graph,new InstanceCombineNode());graph.edit("Nested wavetable",e->{e.addNode(child.instance());e.addConnection(SpectralGroupsTest.wire(child.instance().id(),"out",merge,"in"));});
  var asset=NestedInstanceGroupsTest.asset(base,graph,List.of(new GLCompositePortMapping(GLNodePortDescriptor.output("out",SignalCategory.AUDIO.valueTypeId()),new GLNodeEndpoint(merge,"out")),MultipleGroups.countPort("Count")),2);
  var root=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of(MultipleGroups.COUNT_PROPERTY,GLNodeValue.of(2.)));root.edit("Root",e->e.addNode(instance));
  var assets=new HashMap<>(child.document().embeddedAssets());assets.put(asset.id(),asset);
  return new MultipleGroupsTest.Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),assets,List.of()),instance);
 }
 static MultipleGroupsTest.Fixture change(MultipleGroupsTest.Fixture f,boolean resource,boolean structural){
  var assets=new HashMap<String,GLNodeGraphAsset>();
  for(var a:f.document().embeddedAssets().values()){
   var graph=new GLNodeGraph(a.graph());graph.edit("Parameters",e->{for(var n:a.graph().nodes()){
    if(NodeRegistry.find(n.typeId()).orElse(null) instanceof OscillatorNode)e.setProperty(n.id(),"frequency",GLNodeValue.of(660.));
    if(resource&&NodeRegistry.find(n.typeId()).orElse(null) instanceof WaveformNode){float[] samples=new float[WaveformSamples.SIZE];Arrays.fill(samples,.5f);e.setProperty(n.id(),"curve",GLNodeValue.of(WaveformSamples.PREFIX+WaveformSamples.encode(samples)));}
   }});
   var metadata=new HashMap<>(a.metadata());if(structural)metadata.put(MultipleGroups.MAXIMUM,GLNodeValue.of("3"));
   assets.put(a.id(),new GLNodeGraphAsset(a.id(),a.version(),a.kind(),graph.snapshot(),a.viewState(),metadata,a.compositeDescriptor()));
  }
  return new MultipleGroupsTest.Fixture(new GLNodeGraphDocument(f.document().root(),assets,List.of()),f.instance());
 }
 static String command(MultipleGroupsTest.Fixture f){var n=MultipleGroupsTest.compiled(f);var d=NodeRegistry.require(n.typeId());return "GRAPH 1 instance multiplegraph !"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(n.properties()),p->-1)+" @ a "+"c".repeat(d.processor().inputs(new NodeState(n.properties())).size());}
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
 void exportedDefaultsUpdateWithoutReplacingCompiledInstances()throws Exception{
  var f=MultipleGroupsTest.fixture(false,"sum");var export=GroupSourceExporter.create(f.document(),f.instance());var def=export.definition();
  String code=export.files().get("native/"+export.className()+".hpp");assertTrue(code.contains(":MultipleGraphNode"));
  var dir=Files.createTempDirectory(Path.of("target"),"export-parameters-").toAbsolutePath();Files.writeString(dir.resolve(export.className()+".hpp"),code);
  var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(def);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
  var values=new HashMap<>(def.defaults());var frequency=def.processor().inputs().get(def.processor().inputs().size()-1);values.put(frequency.value(),GLNodeValue.of(880.));
  String initial="GRAPH 1 compiled "+def.processor().id()+" !"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(def.defaults()),p->-1)+" @ a cc";
  String changed="GRAPH 1 compiled "+def.processor().id()+" !"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(values),p->-1)+" @ a cc";
  String cpp="""
   #include "EXPORT_HEADER"
   #include <cassert>
   using namespace easyknack;
   int main(){
    auto factory=[](const std::string& id)->std::unique_ptr<NodeProcessor>{return std::make_unique<EXPORT_TYPE>();};
    AudioEngine engine;auto graph=AudioGraph::compile(INITIAL,factory);auto* node=static_cast<EXPORT_TYPE*>(graph.nodes[0].processor.get());auto* state=node->state.get();
    engine.publish(std::move(graph));std::array<float,512> out{};engine.render(out.data(),256);
    assert(engine.publishParameters(AudioGraph::compile(CHANGED,factory,true)));engine.render(out.data(),256);
    assert(node->state.get()==state);assert(node->defaults[1]==880.f);
    assert(engine.publishParameters(AudioGraph::compile(INITIAL,factory,true)));engine.render(out.data(),256);assert(node->defaults[1]==220.f);
   }
   """.replace("EXPORT_HEADER",export.className()+".hpp").replace("EXPORT_TYPE",export.className()).replace("INITIAL",quote(initial)).replace("CHANGED",quote(changed));
  var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
 }
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
 void recursiveUpdatesKeepInstancesPhasesResourcesAndRejectStructuralChanges()throws Exception{
  var dir=Files.createTempDirectory(Path.of("target"),"instance-parameters-").toAbsolutePath();var calls=new StringBuilder();
  for(var f:List.of(InstanceWavetableTest.fixture(false,false),nested()))calls.append("check(").append(quote(command(f))).append(',').append(quote(command(change(f,false,false)))).append(',').append(quote(command(change(f,true,false)))).append(',').append(quote(command(change(f,false,true)))).append(");\n");
  String cpp=Files.readString(Path.of("native/audio-host/tests/InstanceParametersTest.cpp")).replace("// CHECKS",calls.toString());var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),Path.of("target/native/generated").toAbsolutePath());
 }
}
