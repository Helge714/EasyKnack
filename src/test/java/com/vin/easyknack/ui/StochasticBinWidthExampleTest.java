package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Authored example composed entirely of existing nodes; regeneration is opt-in. */
class StochasticBinWidthExampleTest {
    @Test void existingNodesCompileImportAndPersistTheExample()throws Exception{
        var fixture=StochasticBinWidthExample.fixture(0.25,true);var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(fixture.document(),bytes);
        var restored=NodeDocuments.prepare(codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(restored);var flat=SpectralGroups.flatten(GroupCompiler.prepare(restored.root(),registry),new GLCompositeExpansionLimits(16,512,1024,100000));
        assertTrue(flat.isValid(),flat.diagnostics().toString());assertDoesNotThrow(()->com.vin.easyknack.audio.AudioGraphEncoder.encode(flat.graph().orElseThrow()));
        assertEquals(3,GroupImport.copyMany(restored,List.of(fixture.assetId()),"Stochastic Bin Width").assets().size());
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){controller.openDocument("Stochastic Bin Width",restored);assertTrue(controller.workspace().openComposite(fixture.assetId()));assertDoesNotThrow(controller::audioGraph);}});
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeKernelReadsSingleNearbyBinsAndKeepsPhaseAcrossFrames()throws Exception{
        var fixture=StochasticBinWidthExample.fixture(0.25,true);var registry=new GLCompositeGraphRegistry();registry.registerDocument(fixture.document());
        var result=SpectralGroups.flatten(GroupCompiler.prepare(fixture.document().root(),registry),new GLCompositeExpansionLimits(16,512,1024,100000));assertTrue(result.isValid(),result.diagnostics().toString());
        var graph=result.graph().orElseThrow();var compiled=graph.nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof SpectralGraphNode).findFirst().orElseThrow();
        var definition=NodeRegistry.require(compiled.typeId());var state=new NodeState(compiled.properties());
        String payload="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(state,p->0);
        var values=new double[1+SpectralGraphNode.inputCount(state)];
        for(var wire:graph.connections())if(wire.input().nodeId().equals(compiled.id())&&wire.input().portId().startsWith("external")){
            int slot=1+Integer.parseInt(wire.input().portId().substring(8));var source=graph.findNode(wire.output().nodeId()).orElseThrow();values[slot]=new NodeState(source.properties()).number("value",0);
        }
        var dir=Files.createTempDirectory(Path.of("target"),"stochastic-bin-width-").toAbsolutePath();Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(NodeRegistry.builtinDefinitions()));
        var source=dir.resolve("test.cpp");Files.writeString(source,"""
            #include "nodes/SpectralGraphNode.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                auto node=std::make_unique<SpectralGraphNode>();std::istringstream parameters(%s);node->configure(parameters);node->prepare(48000,256);
                SpectrumStream source;source.prepare(256);source.size=source.windowSize=256;source.hop=64;source.sampleRate=48000;source.count=1;
                float silence[256]{},out[256]{},data[32][256]{};const float* inputs[32]{};char rates[32]{};std::fill_n(rates,32,'c');
                double defaults[]={%s};for(unsigned p=0;p<std::size(defaults);p++){std::fill_n(data[p],256,float(defaults[p]));inputs[p]=data[p];}
                const DspResource* resources[32]{};resources[0]=&source;ProcessContext c{inputs,32,out,256,48000,silence,256};c.sourceRates=rates;c.resourceInputs=resources;
                int previous[129]{};bool changed=false;
                for(unsigned block=0;block<3;block++){
                    source.blockStart=block*64;source.frames[0].start=block*64;
                    for(unsigned k=0;k<=128;k++)source.frames[0].bins[k]=std::polar(100.+k,6.283185307179586*k*block/4.);
                    SpectrumStream::realSymmetry(source.frames[0],256);node->process(c);
                    auto* result=static_cast<SpectrumStream*>(node->resourceOutput(0));assert(result&&result->count==1);
                    for(unsigned k=0;k<=128;k++){
                        auto value=result->frames[0].bins[k];int selected=int(std::round(std::abs(value)-100));
                        assert(selected>=0&&selected<=128);
                        if(k>0&&k<128){assert(selected>=std::max(1,int(std::ceil(k*std::pow(2.,-.25)))));assert(selected<=std::min(127,int(std::floor(k*std::pow(2.,.25)))));}assert(std::abs(std::abs(value)-(100+selected))<.003);
                        if(block)assert(selected==previous[k]);previous[k]=selected;if(selected!=int(k))changed=true;
                        auto expected=std::polar(100.+selected,6.283185307179586*k*block/4.);assert(std::abs(value-expected)<.01);
                        if(k>0&&k<128)assert(result->frames[0].bins[256-k]==std::conj(value));
                    }
                }
                assert(changed); // Nonzero Width must actually select neighbouring source bins.
            }
            """.formatted(AdvancedSourceExportTest.quote(payload),java.util.Arrays.stream(values).mapToObj(Double::toString).collect(java.util.stream.Collectors.joining(","))));
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
