package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.binding.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

class SpectralFormulaTest {
    @Test void documentedGaussianCompiles()throws Exception{
        var ports=new ArrayList<com.vin.easyknack.formula.FormulaCompiler.Port>();
        for(String name:List.of("frequency","magnitude","center","factor","result"))ports.add(new com.vin.easyknack.formula.FormulaCompiler.Port(name,name,com.vin.easyknack.formula.FormulaCompiler.Type.NUMBER,name.equals("result")));
        String code=Files.readString(Path.of("docs/development/features/SPECTRAL-FORMULA.md")).split("```cpp\\R")[1].split("```")[0];
        assertNotNull(com.vin.easyknack.formula.FormulaCompiler.compile(code,ports,3));
    }
    static SpectralUniformTest.Fixture fixture(String code){
        var formula=FormulaGroups.accept(FormulaGroups.create(),code);
        var asset=SpectralGroups.create();var graph=new GLNodeGraph(asset.graph());
        var context=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinContextNode).findFirst().orElseThrow();
        var output=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinOutputNode).findFirst().orElseThrow();
        var node=new GLGraphNode(GLNodeId.random(),formula.compositeDescriptor().nodeTypeId(),Map.of());
        graph.edit("Formula",e->{for(var w:graph.snapshot().connections())e.removeConnection(w.id());e.addNode(node);e.addConnection(SpectralGroupsTest.wire(context.id(),"real",node.id(),"signal"));e.addConnection(SpectralGroupsTest.wire(node.id(),"result",output.id(),"a"));});
        asset=new GLNodeGraphAsset(asset.id(),1,asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),asset.compositeDescriptor());
        var instance=new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of());var root=new GLNodeGraph();root.edit("Instance",e->e.addNode(instance));
        return new SpectralUniformTest.Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset,formula.id(),formula),List.of()),instance);
    }
    @Test void compilesAndExportsNestedFormula(){
        var f=fixture("result = exp(-0.5 * signal * signal);");
        assertTrue(SpectralGroups.supports(new FormulaNode()));
        var n=SpectralUniformTest.compiled(f);
        assertTrue(new String(HexFormat.of().parseHex(new NodeState(n.properties()).text("program",""))).contains("formula"));
        var export=GroupSourceExporter.create(f.document(),f.instance());
        assertTrue(export.files().get("native/"+export.className()+".hpp").contains("BinFormulaNode"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void perBinStateAndGaussianWorkInRuntimeAndExport()throws Exception{
        for(boolean stateful:List.of(false,true)){
            var f=fixture(stateful?"state double total = 0; total += signal; result = total;":"if (signal < 0) { result = 0; } else { result = exp(-0.5 * signal * signal); }");
            var node=SpectralUniformTest.compiled(f);var def=NodeRegistry.require(node.typeId());
            String direct="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(node.properties()),p->p.equals(SpectralGraphNode.IN)?0:-1);
            var export=GroupSourceExporter.create(f.document(),f.instance());var d=export.definition();
            String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->0);
            var dir=Files.createTempDirectory(Path.of("target"),"spectral-formula-").toAbsolutePath();
            Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
            var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(d);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
            String code="""
                #include "AudioGraph.hpp"
                #include "%s.hpp"
                #include <cassert>
                using namespace easyknack;
                template<class T> void check(const char* payload){
                    T node;std::istringstream in(payload);node.configure(in);node.prepare(48000,256);
                    SpectrumStream input;input.prepare(256);input.size=input.windowSize=1024;input.hop=256;input.identity=7;input.count=1;
                    float out[256]{},silence[256]{};const float* inputs[17]{};const DspResource* resources[17]{};resources[0]=&input;
                    ProcessContext c{inputs,17,out,256,48000,silence,256};c.resourceInputs=resources;
                    for(int f=0;f<4;f++){
                        input.blockStart=f*256;input.frames[0].start=f*256-768;
                        for(int k=0;k<=512;k++)input.frames[0].bins[k]={double(k)/256,0};
                        node.process(c);auto* result=static_cast<SpectrumStream*>(node.resourceOutput(0));assert(result->count==1);
                        for(int k=0;k<=512;k++){double x=double(k)/256;double expected=%s;assert(std::abs(result->frames[0].bins[k].real()-expected)<1e-5);}
                    }
                    T next;std::istringstream again(payload);next.configure(again);next.prepare(48000,256);next.adoptState(node);
                    input.blockStart=1024;input.frames[0].start=256;next.process(c);
                    auto* result=static_cast<SpectrumStream*>(next.resourceOutput(0));double x=1;assert(std::abs(result->frames[0].bins[256].real()-(%s))<1e-5);
                }
                int main(){check<SpectralGraphNode>(%s);check<%s>(%s);}
                """.formatted(export.className(),stateful?"x*(f+1)":"std::exp(-.5*x*x)",stateful?"5":"std::exp(-.5*x*x)",AdvancedSourceExportTest.quote(direct),export.className(),AdvancedSourceExportTest.quote(payload));
            var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
        }
    }
}
