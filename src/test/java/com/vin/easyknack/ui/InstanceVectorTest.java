package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.binding.*;
class InstanceVectorTest {
 static MultipleGroupsTest.Fixture fixture(){return fixture(false);}
 static MultipleGroupsTest.Fixture fixture(boolean shared){
  var base=MultipleGroups.create();var g=new GLNodeGraph();
  var context=add(g,new InstanceContextNode());var combine=add(g,new VectorCombineNode());var math=add(g,new VectorMathNode());
  var separate=add(g,new VectorSeparateNode());var merge=add(g,new InstanceCombineNode());
  g.edit("Vectors per instance",e->{e.setProperty(merge,"mode",GLNodeValue.of("mean"));
   e.addConnection(SpectralGroupsTest.wire(context,"index",combine,"x"));
   e.addConnection(SpectralGroupsTest.wire(combine,"vector",math,"a"));
   e.addConnection(SpectralGroupsTest.wire(math,"vector",separate,"vector"));
   e.addConnection(SpectralGroupsTest.wire(separate,"x",merge,"in"));});
  var ports=new ArrayList<>(List.of(new GLCompositePortMapping(GLNodePortDescriptor.output("out",SignalCategory.AUDIO.valueTypeId()),new GLNodeEndpoint(merge,"out")),MultipleGroups.countPort("Count"),new GLCompositePortMapping(GLNodePortDescriptor.input("offset",SignalCategory.VECTOR.valueTypeId(),false,1),new GLNodeEndpoint(math,"b"))));
  if(shared){
   var sharedSeparate=add(g,new VectorSeparateNode());var sum=add(g,new MathNode());
   g.edit("Shared vector branch",e->{e.setProperty(sum,"clamp",GLNodeValue.of(false));e.setProperty(sum,"operation",GLNodeValue.of("add"));e.addConnection(SpectralGroupsTest.wire(merge,"out",sum,"a"));e.addConnection(SpectralGroupsTest.wire(sharedSeparate,"x",sum,"b"));});
   ports.set(0,ports.get(0).withEndpoints(List.of(new GLNodeEndpoint(sum,"audio-out"))));
   ports.set(2,ports.get(2).withEndpoints(List.of(new GLNodeEndpoint(math,"b"),new GLNodeEndpoint(sharedSeparate,"vector"))));
  }
  var metadata=new HashMap<>(base.metadata());metadata.put(MultipleGroups.MAXIMUM,GLNodeValue.of("4"));
  var asset=new GLNodeGraphAsset(base.id(),1,base.kind(),g.snapshot(),base.viewState(),metadata,new GLCompositeNodeDescriptor(base.compositeDescriptor().nodeTypeId(),base.id(),1,ports));
  var root=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of(MultipleGroups.COUNT_PROPERTY,GLNodeValue.of(4.)));
  root.edit("Group",e->e.addNode(instance));var vector=add(root,new VectorNode());root.edit("Connect vector",e->e.addConnection(SpectralGroupsTest.wire(vector,"vector",instance.id(),"offset")));
  return new MultipleGroupsTest.Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),instance);
 }
 @Test void vectorFamilyIsAvailableAndVectorBoundaryExports(){
  for(var d:List.of(new VectorNode(),new VectorCombineNode(),new VectorSeparateNode(),new VectorMathNode(),new VectorRotateNode(),new VectorScaleNode(),new VectorLengthNode(),new VectorDistanceNode(),new VectorDotNode(),new SpatialReceiverNode()))assertTrue(MultipleGroups.supports(d),d.title());
  var f=fixture();var n=MultipleGroupsTest.compiled(f);
  assertTrue(NodeRegistry.require(n.typeId()).layout(new NodeState(n.properties())).rows().stream().anyMatch(r->r.kind()==NodeBuilder.RowKind.INPUT&&r.dataType()==PortDataType.VECTOR));
  assertNotNull(GroupSourceExporter.create(f.document(),f.instance()));
 }
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
 void liveAndExportedVectorsPreserveSamplesAndChannels()throws Exception{
  var f=fixture();var n=MultipleGroupsTest.compiled(f);var d=NodeRegistry.require(n.typeId());
  String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(n.properties()),p->-1);
  var shared=fixture(true);var export=GroupSourceExporter.create(shared.document(),shared.instance());var def=export.definition();
  String exported="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(def.defaults()),p->-1);
  var dir=Files.createTempDirectory(Path.of("target"),"instance-vector-").toAbsolutePath();
  Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
  var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(def);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
  String cpp="""
   #include "nodes/MultipleGraphNode.hpp"
   #include "EXPORT_HEADER"
   #include <cassert>
   using namespace easyknack;
   template<class T,bool Shared=false>void check(const char* payload){
    T node;std::istringstream params(payload);node.configure(params);node.prepare(48000,256);
    VectorResource left,right;left.samples.resize(256);right.samples.resize(256);left.count=right.count=256;
    for(int i=0;i<256;i++){left.samples[i]={2.+i/256.,0,0};right.samples[i]={7.+i/256.,0,0};}
    std::array<float,256> out{},silence{};const float* inputs[9]{};char rates[9]{};std::fill_n(rates,9,'a');rates[0]='c';
    const DspResource* resources[9]{};const DspResource* rightResources[9]{};std::fill_n(resources,9,&left);std::fill_n(rightResources,9,&right);
    ProcessContext c{inputs,9,out.data(),256,48000,silence.data(),256};c.sourceRates=rates;c.resourceInputs=resources;c.rightResourceInputs=rightResources;
    node.process(c);for(int i=0;i<256;i++)assert(std::abs(out[i]-(1.5+(Shared?2:1)*(2.+i/256.)))<1e-5);
    c.channelIndex=1;node.process(c);for(int i=0;i<256;i++)assert(std::abs(out[i]-(1.5+(Shared?2:1)*(7.+i/256.)))<1e-5);
   }
   int main(){check<MultipleGraphNode>(LIVE_PAYLOAD);check<EXPORT_TYPE,true>(EXPORTED_PAYLOAD);}
   """.replace("EXPORT_HEADER",export.className()+".hpp").replace("EXPORT_TYPE",export.className()).replace("LIVE_PAYLOAD",quote(payload)).replace("EXPORTED_PAYLOAD",quote(exported));
  var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
 }
}
