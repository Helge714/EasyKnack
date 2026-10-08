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
class NestedInstanceGroupsTest {
 static GLNodeGraphAsset asset(GLNodeGraphAsset base,GLNodeGraph g,List<GLCompositePortMapping> ports,int capacity){
  var metadata=new HashMap<>(base.metadata());metadata.put(MultipleGroups.MAXIMUM,GLNodeValue.of(Integer.toString(capacity)));
  return new GLNodeGraphAsset(base.id(),1,base.kind(),g.snapshot(),base.viewState(),metadata,new GLCompositeNodeDescriptor(base.compositeDescriptor().nodeTypeId(),base.id(),1,ports));
 }
 static MultipleGroupsTest.Fixture fixture(int capacity){
  var innerBase=MultipleGroups.create();var inner=new GLNodeGraph();var index=add(inner,new InstanceContextNode());var add=add(inner,new MathNode());var merge=add(inner,new InstanceCombineNode());
  inner.edit("Inner instances",e->{e.setProperty(add,"operation",GLNodeValue.of("add"));e.setProperty(add,"clamp",GLNodeValue.of(false));e.setProperty(merge,"mode",GLNodeValue.of("sum"));e.addConnection(SpectralGroupsTest.wire(index,"index",add,"a"));e.addConnection(SpectralGroupsTest.wire(add,"audio-out",merge,"in"));});
  var child=asset(innerBase,inner,List.of(new GLCompositePortMapping(GLNodePortDescriptor.output("out",SignalCategory.VALUE.valueTypeId()),new GLNodeEndpoint(merge,"out")),MultipleGroups.countPort("Count"),new GLCompositePortMapping(GLNodePortDescriptor.input("offset",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(add,"b"))),capacity);
  var outerBase=MultipleGroups.create();var outer=new GLNodeGraph();var outerIndex=add(outer,new InstanceContextNode());var count=add(outer,new MathNode());var outerMerge=add(outer,new InstanceCombineNode());
  var childInstance=new GLGraphNode(GLNodeId.random(),child.compositeDescriptor().nodeTypeId(),Map.of());
  outer.edit("Nested instances",e->{e.addNode(childInstance);e.setProperty(count,"operation",GLNodeValue.of("add"));e.setProperty(count,"b",GLNodeValue.of(1.));e.setProperty(count,GLGraphNode.AUTOMATIC_PREFIX+"b",GLNodeValue.of(false));e.setProperty(count,"clamp",GLNodeValue.of(false));e.setProperty(outerMerge,"mode",GLNodeValue.of("sum"));
   e.addConnection(SpectralGroupsTest.wire(outerIndex,"index",count,"a"));e.addConnection(SpectralGroupsTest.wire(count,"audio-out",childInstance.id(),"Count"));
   e.addConnection(SpectralGroupsTest.wire(outerIndex,"index",childInstance.id(),"offset"));e.addConnection(SpectralGroupsTest.wire(childInstance.id(),"out",outerMerge,"in"));});
  var parent=asset(outerBase,outer,List.of(new GLCompositePortMapping(GLNodePortDescriptor.output("out",SignalCategory.VALUE.valueTypeId()),new GLNodeEndpoint(outerMerge,"out")),MultipleGroups.countPort("Count")),capacity);
  var root=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),parent.compositeDescriptor().nodeTypeId(),Map.of(MultipleGroups.COUNT_PROPERTY,GLNodeValue.of(3.)));root.edit("Root",e->e.addNode(instance));
  return new MultipleGroupsTest.Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(parent.id(),parent,child.id(),child),List.of()),instance);
 }
 @Test void nestedGroupsCompileExportAndRespectTotalCapacity()throws Exception{
  var f=fixture(3);assertNotNull(MultipleGroupsTest.compiled(f));assertNotNull(GroupSourceExporter.create(f.document(),f.instance()));
  assertTrue(assertThrows(IllegalArgumentException.class,()->MultipleGroupsTest.compiled(fixture(256))).getMessage().contains("4096"));
  javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
   c.addMultipleGroup(0,0);c.actions().openSelectedComposite();assertTrue(c.availableNodes().stream().anyMatch(n->n.type().id().equals(MultipleGroups.CREATE_TYPE)));
   c.addMultipleGroup(100,100);c.actions().openSelectedComposite();assertTrue(c.isMultipleContext());
  }});
 }
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
 void countsAndContextAreScopedInLiveAndExportedGroups()throws Exception{
  var f=fixture(3);var n=MultipleGroupsTest.compiled(f);var d=NodeRegistry.require(n.typeId());
  String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(n.properties()),p->-1);
  var export=GroupSourceExporter.create(f.document(),f.instance());var def=export.definition();
  String exported="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(def.defaults()),p->-1);
  var dir=Files.createTempDirectory(Path.of("target"),"nested-instances-").toAbsolutePath();Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
  var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(def);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
  var source=dir.resolve("test.cpp");Files.writeString(source,"""
   #include "nodes/MultipleGraphNode.hpp"
   #include "EXPORT_HEADER"
   #include <cassert>
   using namespace easyknack;
   template<class T>void check(const char* text){T node;std::istringstream p(text);node.configure(p);node.prepare(48000,256);
    std::array<float,256> out{},silence{};float count=3;const float* inputs[9]{&count};char rates[9]{};std::fill_n(rates,9,'c');
    ProcessContext c{inputs,9,out.data(),256,48000,silence.data(),256};c.sourceRates=rates;
    node.process(c);for(float x:out)assert(std::abs(x-12.f)<1e-5);
    assert(node.state->instances.size()==3);
    for(int i=0;i<3;i++){int children=0;for(auto& n:node.state->instances[i]->runtime->graph.nodes)if(auto* child=dynamic_cast<MultipleGraphNode*>(n.processor.get())){
     ++children;assert(child->state->instances.size()==3);
     for(int j=0;j<=i;j++)for(auto* context:child->state->instances[j]->contexts){assert(context->index==j);assert(context->count==i+1);}
    }assert(children==1);}
    count=0;for(int b=0;b<4;b++)node.process(c);for(float x:out)assert(std::abs(x)<1e-5);
   }
   int main(){check<MultipleGraphNode>(LIVE);check<EXPORT_TYPE>(EXPORTED);}
   """.replace("EXPORT_HEADER",export.className()+".hpp").replace("EXPORT_TYPE",export.className()).replace("EXPORTED",quote(exported)).replace("LIVE",quote(payload)));
  RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
 }
}
