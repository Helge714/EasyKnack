package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.nio.file.*;import java.util.*;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;import com.vingame.glswing.node.model.*;import com.vingame.glswing.node.document.*;
class InstanceDspCatalogTest {
 static MultipleGroupsTest.Fixture fixture(){
  var base=MultipleGroupsTest.fixture(false,"mean");var asset=base.document().embeddedAssets().values().iterator().next();var graph=new GLNodeGraph(asset.graph());
  var osc=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof OscillatorNode).findFirst().orElseThrow();
  var combine=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof InstanceCombineNode).findFirst().orElseThrow();
  var fft=add(graph,new FFTStreamNode());var blur=add(graph,new SpectrumBlurNode());var prune=add(graph,new SpectrumPruneNode());var shift=add(graph,new SpectrumShiftNode());var ifft=add(graph,new IFFTNode());
  graph.edit("Per-instance spectral processing",e->{
   for(var w:graph.snapshot().connections())if(w.input().nodeId().equals(combine.id()))e.removeConnection(w.id());
   e.setProperty(fft,"size",GLNodeValue.of("256"));e.setProperty(blur,"mode",GLNodeValue.of("bins"));e.setProperty(blur,"width",GLNodeValue.of(2.));e.setProperty(blur,"synthesis",GLNodeValue.of("continuous"));
   e.setProperty(prune,"keep",GLNodeValue.of(.5));e.setProperty(shift,"shift",GLNodeValue.of(1.));
   e.addConnection(SpectralGroupsTest.wire(osc.id(),"audio-out",fft,"in"));e.addConnection(SpectralGroupsTest.wire(fft,"spectrum",blur,"spectrum"));
   e.addConnection(SpectralGroupsTest.wire(blur,"out",prune,"spectrum"));e.addConnection(SpectralGroupsTest.wire(prune,"out",shift,"spectrum"));
   e.addConnection(SpectralGroupsTest.wire(shift,"out",ifft,"spectrum"));e.addConnection(SpectralGroupsTest.wire(ifft,"out",combine.id(),"in"));
  });
  var changed=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),asset.compositeDescriptor());
  return new MultipleGroupsTest.Fixture(new GLNodeGraphDocument(base.document().root(),Map.of(changed.id(),changed),List.of()),base.instance());
 }
 @Test void spectralChainCompilesAndObserversStayOutside(){
  var f=fixture();var n=MultipleGroupsTest.compiled(f);String program=new String(HexFormat.of().parseHex(new NodeState(n.properties()).text("program","")),java.nio.charset.StandardCharsets.UTF_8);
  for(String id:List.of("spectrumblur","spectrumprune","spectrumshift"))assertTrue(program.contains(id));
  assertNotNull(GroupSourceExporter.create(f.document(),f.instance()));
  for(var d:List.of(new ValueViewerNode(),new MeasurementRecorderNode(),new InputNode(),new OutputNode(),new MidiInputNode(),new BinReadNode(),new SpectrumWaveformNode()))assertFalse(MultipleGroups.supports(d),d.title());
 }
 @Test void instanceMenuOffersSpectralEffectsAndNewFormulaGroups()throws Exception{
  javax.swing.SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
   controller.addMultipleGroup(0,0);controller.actions().openSelectedComposite();
   var ids=controller.availableNodes().stream().map(e->e.type().id()).toList();
   assertTrue(ids.contains(new SpectrumBlurNode().registration().id()));assertTrue(ids.contains(FormulaGroups.CREATE));
   assertTrue(ids.contains(new EnvelopeNode().registration().id()));assertFalse(ids.contains(new ValueViewerNode().registration().id()));
  }});
 }
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
 void everyAllowedJavaProcessorHasANativeFactoryAndSpectralExportsRender()throws Exception{
  var dir=Files.createTempDirectory(Path.of("target"),"instance-dsp-").toAbsolutePath();var f=fixture();var n=MultipleGroupsTest.compiled(f);var d=NodeRegistry.require(n.typeId());
  String live="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(n.properties()),p->-1);
  var export=GroupSourceExporter.create(f.document(),f.instance());var def=export.definition();var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(def);
  Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
  String baked="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(def.defaults()),p->-1);
  var checks=new StringBuilder();for(var node:NodeRegistry.builtinDefinitions())if(MultipleGroups.supports(node))checks.append("{auto node=MultipleGraphNode::factory(").append(quote(node.processor().id())).append(");std::istringstream p(").append(quote("!"+NativeContractGenerator.fingerprint(node)+" "+node.encode(new NodeState(node.defaults()),port->-1))).append(");node->configure(p);node->prepare(48000,256);}\n");
  String cpp="""
   #include "nodes/MultipleGraphNode.hpp"
   #include "EXPORT_HEADER"
   #include <cassert>
   using namespace easyknack;
   int main(){
    FACTORY_CHECKS
    MultipleGraphNode live;EXPORT_TYPE baked;std::istringstream a(LIVE),b(BAKED);live.configure(a);baked.configure(b);live.prepare(48000,256);baked.prepare(48000,256);
    std::array<float,256> left{},right{},silence{};const float* inputs[9]{};char rates[9]{};std::fill_n(rates,9,'c');double energy=0;
    ProcessContext x{inputs,9,left.data(),256,48000,silence.data(),256};x.sourceRates=rates;auto y=x;y.output=right.data();
    for(int block=0;block<16;block++)for(int channel=0;channel<2;channel++){
     x.channelIndex=y.channelIndex=channel;live.process(x);baked.process(y);
     for(int i=0;i<256;i++){assert(std::isfinite(left[i])&&std::isfinite(right[i]));assert(std::abs(left[i]-right[i])<1e-5);energy+=left[i]*left[i];}
    }
    assert(energy>1);
    auto& first=live.state->instances[0]->runtime->graph;auto& second=live.state->instances[1]->runtime->graph;
    for(std::size_t i=0;i<first.nodes.size();i++)if(first.nodes[i].processorId=="spectrumblur")assert(first.nodes[i].processor->resourceOutput(0)!=second.nodes[i].processor->resourceOutput(0));
   }
   """.replace("EXPORT_HEADER",export.className()+".hpp").replace("EXPORT_TYPE",export.className()).replace("FACTORY_CHECKS",checks.toString()).replace("LIVE",quote(live)).replace("BAKED",quote(baked));
  var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
 }
}
