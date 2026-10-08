package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;

class NestedSpectralInstancesTest {
    static MultipleGroupsTest.Fixture fixture(){
        var base=MultipleGroupsTest.fixture(false,"mean");
        var asset=base.document().embeddedAssets().values().iterator().next();
        var spectral=SpectralGroups.create();var g=new GLNodeGraph(asset.graph());
        var osc=g.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof OscillatorNode).findFirst().orElseThrow();
        var combine=g.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof InstanceCombineNode).findFirst().orElseThrow();
        var fft=add(g,new FFTStreamNode());var ifft=add(g,new IFFTNode());
        var inner=new GLGraphNode(GLNodeId.random(),spectral.compositeDescriptor().nodeTypeId(),Map.of());
        g.edit("Nested spectral processor",e->{
            for(var w:g.snapshot().connections())if(w.input().nodeId().equals(combine.id()))e.removeConnection(w.id());
            e.addNode(inner);e.setProperty(fft,"size",GLNodeValue.of("256"));
            e.addConnection(SpectralGroupsTest.wire(osc.id(),"audio-out",fft,"in"));
            e.addConnection(SpectralGroupsTest.wire(fft,"spectrum",inner.id(),"spectrum"));
            e.addConnection(SpectralGroupsTest.wire(inner.id(),"out",ifft,"spectrum"));
            e.addConnection(SpectralGroupsTest.wire(ifft,"out",combine.id(),"in"));
        });
        var changed=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),g.snapshot(),asset.viewState(),asset.metadata(),asset.compositeDescriptor());
        return new MultipleGroupsTest.Fixture(new GLNodeGraphDocument(base.document().root(),Map.of(changed.id(),changed,spectral.id(),spectral),List.of()),base.instance());
    }
    @Test void compilesAndExportsNestedSpectralBoundary()throws Exception{
        var f=fixture();var compiled=MultipleGroupsTest.compiled(f);
        String program=new String(java.util.HexFormat.of().parseHex(new NodeState(compiled.properties()).text("program","")),java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(program.contains("spectralgraph"));assertTrue(program.contains("fftstream"));assertTrue(program.contains("ifft"));
        assertNotNull(GroupSourceExporter.create(f.document(),f.instance()));
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.addMultipleGroup(0,0);c.actions().openSelectedComposite();
            assertTrue(c.availableNodes().stream().anyMatch(n->n.type().id().equals(SpectralGroups.CREATE_TYPE)));
            c.addSpectralGroup();c.actions().openSelectedComposite();
            assertTrue(c.availableNodes().stream().anyMatch(n->n.type().id().equals(new BinReadNode().registration().id())));
        }});
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeInstancesKeepIndependentSpectraAndProduceAudio()throws Exception{
        var n=MultipleGroupsTest.compiled(fixture());var d=NodeRegistry.require(n.typeId());
        String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(n.properties()),p->-1);
        var dir=Files.createTempDirectory(Path.of("target"),"nested-spectral-").toAbsolutePath();
        var source=dir.resolve("test.cpp");
        Files.writeString(source,"""
            #include "nodes/MultipleGraphNode.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){MultipleGraphNode node;std::istringstream params(PAYLOAD);node.configure(params);node.prepare(48000,256);
              std::array<float,256> out{},silence{};const float* inputs[9]{};char rates[9]{};std::fill_n(rates,9,'c');
              ProcessContext c{inputs,9,out.data(),256,48000,silence.data(),256};c.sourceRates=rates;
              double energy=0;for(int b=0;b<20;b++){node.process(c);for(float x:out){assert(std::isfinite(x));energy+=x*x;}}
              assert(energy>1);std::set<DspResource*> resources;
              for(auto& instance:node.state->instances)for(auto& n:instance->runtime->graph.nodes)
                if(n.processorId=="spectralgraph")resources.insert(n.processor->resourceOutput(0));
              assert(resources.size()==4);assert(!resources.count(nullptr));
            }
            """.replace("PAYLOAD",quote(payload)).replace("#include <cassert>","#include <cassert>\n#include <set>"));
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
