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
class InstanceSpectrumInputTest {
 static MultipleGroupsTest.Fixture fixture(){return fixture(false);}
 static MultipleGroupsTest.Fixture fixture(boolean shared){
  var base=NestedSpectralInstancesTest.fixture();var assets=new HashMap<>(base.document().embeddedAssets());
  var asset=assets.values().stream().filter(MultipleGroups::multiple).findFirst().orElseThrow();
  var spectral=assets.values().stream().filter(SpectralGroups::spectral).findFirst().orElseThrow();
  var graph=new GLNodeGraph(asset.graph());var inner=graph.snapshot().nodes().stream().filter(n->n.typeId().equals(spectral.compositeDescriptor().nodeTypeId())).findFirst().orElseThrow();
  graph.edit("External FFT",e->{for(var n:graph.snapshot().nodes())if(NodeRegistry.find(n.typeId()).orElse(null) instanceof FFTStreamNode)e.removeNode(n.id());});
  var ports=new ArrayList<>(asset.compositeDescriptor().portMappings());
  ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input("spectrum",SignalCategory.SPECTRUM.valueTypeId(),false,1),new GLNodeEndpoint(inner.id(),"spectrum")));
  if(shared){
   var ifft=add(graph,new IFFTNode());var mix=add(graph,new MathNode());
   var output=ports.stream().filter(p->p.publicPort().direction()==GLPortDirection.OUTPUT).findFirst().orElseThrow();
   graph.edit("Shared spectrum branch",e->{e.setProperty(mix,"clamp",GLNodeValue.of(false));e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),output.internalEndpoint(),new GLNodeEndpoint(mix,"a")));e.addConnection(SpectralGroupsTest.wire(ifft,"out",mix,"b"));});
   ports.set(ports.indexOf(output),output.withEndpoints(List.of(new GLNodeEndpoint(mix,"audio-out"))));
   var input=ports.get(ports.size()-1);ports.set(ports.size()-1,input.withEndpoints(List.of(new GLNodeEndpoint(inner.id(),"spectrum"),new GLNodeEndpoint(ifft,"spectrum"))));
  }
  var descriptor=new GLCompositeNodeDescriptor(asset.compositeDescriptor().nodeTypeId(),asset.id(),1,ports);
  assets.put(asset.id(),new GLNodeGraphAsset(asset.id(),1,asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),descriptor));
  var root=new GLNodeGraph(base.document().root().graph());var fft=add(root,new FFTStreamNode());
  root.edit("Supply spectrum",e->e.addConnection(SpectralGroupsTest.wire(fft,"spectrum",base.instance().id(),"spectrum")));
  var r=base.document().root();return new MultipleGroupsTest.Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset(r.id(),r.version(),r.kind(),root.snapshot(),r.viewState()),assets,List.of()),base.instance());
 }
 @Test void externalSpectrumIsTypedAndExportable()throws Exception{
  var f=fixture();var n=MultipleGroupsTest.compiled(f);var d=NodeRegistry.require(n.typeId());
  assertTrue(d.layout(new NodeState(n.properties())).rows().stream().anyMatch(r->r.kind()==NodeBuilder.RowKind.INPUT&&r.dataType()==PortDataType.SPECTRUM));
  String program=new String(HexFormat.of().parseHex(new NodeState(n.properties()).text("program","")),java.nio.charset.StandardCharsets.UTF_8);
  assertFalse(program.contains("fftstream"),"FFT must stay outside the instance engine");
  assertTrue(program.contains("spectralgraph"));assertNotNull(GroupSourceExporter.create(f.document(),f.instance()));
 }
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
 void sharedFramesReachLiveAndExportedInstances()throws Exception{
  var f=fixture();var node=MultipleGroupsTest.compiled(f);var d=NodeRegistry.require(node.typeId());
  String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(node.properties()),p->-1);
  var shared=fixture(true);var export=GroupSourceExporter.create(shared.document(),shared.instance());var def=export.definition();
  String exported="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(def.defaults()),p->-1);
  var fft=new FFTStreamNode();var values=new HashMap<>(fft.defaults());values.put("size",GLNodeValue.of("256"));
  String fftPayload="!"+NativeContractGenerator.fingerprint(fft)+" "+fft.encode(new NodeState(values),p->-1);
  var dir=Files.createTempDirectory(Path.of("target"),"instance-spectrum-").toAbsolutePath();
  Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
  var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(def);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
  String cpp="""
   #include "nodes/MultipleGraphNode.hpp"
   #include "EXPORT_HEADER"
   #include <cassert>
   using namespace easyknack;
   template<class T>void check(const char* payload){
    T node;std::istringstream params(payload);node.configure(params);node.prepare(48000,256);
    FFTStreamNode fft;std::istringstream fp(FFT_PAYLOAD);fft.configure(fp);fft.prepare(48000,256);
    std::array<float,256> signal{},out{},silence{};const float* audio[]={signal.data()};
    ProcessContext fc{audio,1,out.data(),256,48000,silence.data(),256};
    const float* inputs[9]{};char rates[9]{};std::fill_n(rates,9,'c');const DspResource* resources[9]{};
    ProcessContext c{inputs,9,out.data(),256,48000,silence.data(),256};c.sourceRates=rates;c.resourceInputs=resources;
    double energy=0;for(int b=0;b<24;b++){
     for(int i=0;i<256;i++)signal[i]=float(std::sin((b*256+i)*440.*6.283185307179586/48000));
     fft.process(fc);std::fill_n(resources,9,fft.resourceOutput(0));node.process(c);
     for(float x:out){assert(std::isfinite(x));energy+=x*x;}
    }assert(energy>10);
   }
   int main(){check<MultipleGraphNode>(LIVE_PAYLOAD);check<EXPORT_TYPE>(EXPORTED_PAYLOAD);}
   """.replace("EXPORT_HEADER",export.className()+".hpp").replace("EXPORT_TYPE",export.className()).replace("FFT_PAYLOAD",quote(fftPayload)).replace("LIVE_PAYLOAD",quote(payload)).replace("EXPORTED_PAYLOAD",quote(exported));
  var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
 }
}
