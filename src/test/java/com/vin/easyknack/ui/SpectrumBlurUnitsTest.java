package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

class SpectrumBlurUnitsTest {
    static NodeBuilder.Row width(NodeState state){return new SpectrumBlurNode().layout(state).rows().stream().filter(r->r.key().equals(SpectrumBlurNode.WIDTH)).findFirst().orElseThrow();}
    @Test void widthUsesRealUnitsAndStableSourceOrder(){
        var node=new SpectrumBlurNode();var values=new HashMap<>(node.defaults());values.put("width",GLNodeValue.of(1.));
        assertEquals(List.of(SpectrumBlurNode.IN,SpectrumBlurNode.WIDTH),node.processor().inputs());
        assertFalse(node.layout(new NodeState(values)).rows().stream().anyMatch(r->Set.of("mode","factor","octaves").contains(r.id())));
        for(var unit:SpectrumBlurNode.UNITS){
            values.put("width.unit",GLNodeValue.of(unit.id()));var state=new NodeState(values);var row=width(state);var editor=(UnitSlider)row.editor();
            assertEquals(unit,row.unit());assertEquals(SpectrumBlurNode.UNITS,row.unitChoices());
            assertEquals(unit==UnitFormat.CENT?1200:unit==UnitFormat.SEMITONE?12:1,editor.display(1),1e-9);
            assertEquals(1,editor.stored(editor.display(1)),1e-9);
            assertTrue(node.encode(state,p->p.equals(SpectrumBlurNode.WIDTH)?7:-1).startsWith("-1 7 1.0 "+(unit==UnitFormat.BIN?"bin":"octave")));
        }
        assertFalse(UnitFormat.BIN.compatible(UnitFormat.OCTAVE));
        assertEquals(List.of(UnitFormat.BIN),UnitConverterNode.destinations(UnitFormat.BIN));
        assertThrows(IllegalArgumentException.class,()->node.layout(new NodeState(Map.of("mode",GLNodeValue.of("factor")))));
    }
    @Test void connectedPitchInputsGetExplicitAdaptersAndBinsStayRaw(){
        for(var unit:SpectrumBlurNode.UNITS){
            var source=PortUnitsTest.node(new ValueNode());var blur=PortUnitsTest.node(new SpectrumBlurNode()).withProperty("width.unit",GLNodeValue.of(unit.id()));
            var graph=new GLNodeGraph();graph.edit("Width",e->{e.addNode(source);e.addNode(blur);e.addConnection(PortUnitsTest.wire(source,"value-out",blur,"width"));});
            var prepared=PortUnitConversions.materialize(graph.snapshot());
            var adapters=prepared.nodes().stream().filter(n->n.typeId().equals("easyknack.math.unitconverter")).toList();
            assertEquals(unit==UnitFormat.CENT||unit==UnitFormat.SEMITONE?1:0,adapters.size());
            if(!adapters.isEmpty()){var state=new NodeState(adapters.get(0).properties());assertEquals(unit.id(),state.text("from",""));assertEquals("octave",state.text("to",""));}
        }
    }
    @Test void nodeAndInspectorMenusSwitchFamiliesAndUndoWithoutLosingConnections()throws Exception{
        var controller=new WorkspaceController[1];
        try{
            SwingUtilities.invokeAndWait(()->{
                var c=controller[0]=new WorkspaceController();var graph=c.workspace().currentGraph();
                var blur=PortUnitsTest.node(new SpectrumBlurNode()).withProperty("width",GLNodeValue.of(1.));
                graph.edit("Width",e->{for(var old:graph.snapshot().nodes())e.removeNode(old.id());e.addNode(blur);});
                c.workspace().editor().setSelectedNode(PortUnitsTest.view(c,blur.id()));
                PortUnitsTest.choose(PortUnitsTest.view(c,blur.id()).findPort("width"),UnitFormat.CENT);
                var slider=(GLUnitSlider<?>)PortUnitsTest.view(c,blur.id()).getContents().stream().filter(GLUnitSlider.class::isInstance).findFirst().orElseThrow();
                assertEquals(1200,slider.getValue(),1e-6);
                var menu=slider.getContextMenu().getItems().stream().filter(i->i.getSubMenu()!=null).findFirst().orElseThrow().getSubMenu();
                assertEquals(4,menu.getItems().size());menu.getItems().stream().filter(i->i.getText().equals(UnitFormat.BIN.toString())).findFirst().orElseThrow().doClick();
                assertEquals("bin",new NodeState(graph.snapshot().findNode(blur.id()).orElseThrow().properties()).text("width.unit",""));
                c.history().undo();assertEquals("cent",new NodeState(graph.snapshot().findNode(blur.id()).orElseThrow().properties()).text("width.unit",""));
            });
            SwingUtilities.invokeAndWait(()->{
                var inspector=controller[0].workspace().propertyInspector();
                var slider=(GLUnitSlider<?>)inspector.getGLChildren().stream().filter(GLUnitSlider.class::isInstance).findFirst().orElseThrow();
                assertEquals(UnitFormat.CENT,slider.getDisplayUnit());
                var menu=slider.getContextMenu().getItems().stream().filter(i->i.getSubMenu()!=null).findFirst().orElseThrow().getSubMenu();
                menu.getItems().stream().filter(i->i.getText().equals(UnitFormat.BIN.toString())).findFirst().orElseThrow().doClick();
            });
            SwingUtilities.invokeAndWait(()->{
                var inspector=controller[0].workspace().propertyInspector();
                var slider=(GLUnitSlider<?>)inspector.getGLChildren().stream().filter(GLUnitSlider.class::isInstance).findFirst().orElseThrow();
                assertEquals(UnitFormat.BIN,slider.getDisplayUnit());assertEquals(1,slider.getValue(),1e-6);
                var c=controller[0];var graph=c.workspace().currentGraph();var blur=graph.snapshot().nodes().get(0);var value=PortUnitsTest.node(new ValueNode());
                graph.edit("Connect width",e->{e.addNode(value);e.addConnection(PortUnitsTest.wire(value,"value-out",blur,"width"));});
                var port=PortUnitsTest.view(c,blur.id()).findPort("width");assertTrue(port.isContentHiddenByConnection());
                PortUnitsTest.choose(port,UnitFormat.OCTAVE);assertEquals(1,graph.snapshot().connections().size());
                assertTrue(PortUnitsTest.view(c,blur.id()).findPort("width").isContentHiddenByConnection());
            });
        }finally{if(controller[0]!=null)SwingUtilities.invokeAndWait(controller[0]::close);}
    }
    @Test void oldOctaveDocumentsRetainTheirValueWiresAndGroupBindings(){
        var source=PortUnitsTest.node(new ValueNode());
        var blur=new GLGraphNode(GLNodeId.random(),"easyknack.spectral.blur",Map.of("mode",GLNodeValue.of("octaves"),"width",GLNodeValue.of(0.),"octaves",GLNodeValue.of(1.5)));
        var graph=new GLNodeGraph();graph.edit("Old blur",e->{e.addNode(source);e.addNode(blur);e.addConnection(PortUnitsTest.wire(source,"value-out",blur,"octaves"));});
        var mapping=new com.vingame.glswing.node.composite.GLCompositePortMapping(GLNodePortDescriptor.input("Radius",PortDataType.SIGNAL.id(),false,1),new GLNodeEndpoint(blur.id(),"octaves"));
        var descriptor=new com.vingame.glswing.node.composite.GLCompositeNodeDescriptor("test.blur","test.blur.asset",1,List.of(mapping));
        var asset=new com.vingame.glswing.node.document.GLNodeGraphAsset("test.blur.asset",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var root=new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,new GLNodeGraph().snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty());
        var document=new com.vingame.glswing.node.document.GLNodeGraphDocument(root,Map.of(asset.id(),asset),List.of());
        var migrated=SpectrumBlurMigration.migrate(document).embeddedAssets().get(asset.id());
        var state=new NodeState(migrated.graph().findNode(blur.id()).orElseThrow().properties());
        assertEquals(1.5,state.number("width",0));assertEquals("octave",state.text("width.unit",""));assertFalse(state.values().containsKey("mode"));
        assertEquals("width",migrated.graph().connections().get(0).input().portId());
        assertEquals("width",migrated.compositeDescriptor().portMappings().get(0).internalEndpoint().portId());
        assertEquals("octave",new NodeState(migrated.compositeDescriptor().portMappings().get(0).presentation().editorOptions()).text(GroupUnits.BASE,""));
        assertEquals(migrated,SpectrumBlurMigration.migrate(SpectrumBlurMigration.migrate(document)).embeddedAssets().get(asset.id()));
        assertEquals(UnitFormat.BIN,width(new NodeState(Map.of("mode",GLNodeValue.of("bins"),"width",GLNodeValue.of(3.)))).unit());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeWidthAndUnitConversionsAgreeForPitchAndBins()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"blur-units-").toAbsolutePath();
        var definition=new SpectrumBlurNode();var values=new HashMap<>(definition.defaults());values.put("kernel",GLNodeValue.of("maximum"));values.put("synthesis",GLNodeValue.of("preserve"));
        String pitch="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(values),p->-1);
        values.put("width.unit",GLNodeValue.of("bin"));String bins="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(values),p->-1);
        Path cpp=dir.resolve("test.cpp");Files.writeString(cpp,"""
            #include "nodes/SpectrumBlurNode.hpp"
            #include "nodes/UnitConverterNode.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){SpectrumBlurNode logarithmic,linear;std::istringstream a("%s"),b("%s");logarithmic.configure(a);linear.configure(b);logarithmic.prepare(48000,256);linear.prepare(48000,256);
              SpectrumStream input;input.prepare(256);input.size=input.windowSize=256;input.sampleRate=48000;input.count=1;input.hop=64;input.frames[0].bins[32]={2,0};SpectrumStream::realSymmetry(input.frames[0],256);
              const DspResource* resources[]{&input};float amount=1,out=0,silence=0;const float* inputs[]{nullptr,&amount};ProcessContext c{inputs,2,&out,1,48000,&silence};c.resourceInputs=resources;
              for(auto unit:{UnitFormat::octave,UnitFormat::cent,UnitFormat::semitone}){float value=unit==UnitFormat::cent?1200:unit==UnitFormat::semitone?12:1;amount=UnitConverterNode::evaluate(value,unit,UnitFormat::octave);assert(std::abs(amount-1)<1e-6);logarithmic.process(c);for(int k=1;k<=128;k++)assert(std::abs(logarithmic.stream.frames[0].bins[k])==(k>=16&&k<=64?2:0));}
              amount=1;linear.process(c);for(int k=0;k<=128;k++)assert(std::abs(linear.stream.frames[0].bins[k])==(k>=31&&k<=33?2:0));
              assert(unitDimension(UnitFormat::bin)!=unitDimension(UnitFormat::octave));assert(!unitsConvertible(UnitFormat::bin,UnitFormat::octave));
              amount=0;for(auto* node:{&logarithmic,&linear}){node->process(c);for(int k=0;k<256;k++)assert(node->stream.frames[0].bins[k]==input.frames[0].bins[k]);}
            }
            """.formatted(pitch,bins));
        RateConverterTest.compileAndRun(cpp,dir.resolve("test.exe"),dir);
    }
}
