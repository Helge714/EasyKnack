package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioGraphEncoder;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.ui.GLDriverMapping;
import java.util.*;
import java.nio.file.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

class ControlInterpolationTest {
    static Editor.Slider slider(BasicNode node,String property){var editor=node.layout(new NodeState(node.defaults())).rows().stream().filter(r->r.id().equals(property)).findFirst().orElseThrow().editor();return editor instanceof UnitSlider u?u.spec():(Editor.Slider)editor;}
    static GLNodeGraphSnapshot graph(BasicNode node,Map<String,GLNodeValue> values){var graph=new GLNodeGraph();var merged=new HashMap<>(node.defaults());merged.putAll(values);graph.edit("Fixture",e->e.addNode(new GLGraphNode(GLNodeId.random(),node.registration().id(),merged)));return graph.snapshot();}
    @Test void bufferingAndBoundarySmoothingPersistIndependently(){
        var defaults=ControlInterpolationSettings.DEFAULT;assertTrue(defaults.buffered());assertTrue(defaults.smoothing());assertEquals(50,defaults.bufferMs());assertEquals(5,defaults.smoothingMs());
        for(boolean buffered:List.of(false,true))for(boolean smoothing:List.of(false,true)){
            var settings=new ControlInterpolationSettings(true,5,50,buffered,35,smoothing,3);
            assertEquals(settings,ControlInterpolationSettings.read(settings.value()));
            var driver=new MidiDriverBinding("1","Device",0,7,true,new GLDriverMapping(0,1,false,false,false),false,64,settings);
            assertEquals(driver,MidiDriverBinding.read(driver.value()));
        }
        var legacy=ControlInterpolationSettings.read(GLNodeValue.of(Map.of("enabled",true,"minimumMs",5,"maximumMs",50)));
        assertTrue(legacy.buffered());assertTrue(legacy.smoothing());
        assertThrows(IllegalArgumentException.class,()->new ControlInterpolationSettings(true,5,50,true,1001,true,5));
        var definition=new ValueNode();try(var scope=ControlMetadata.use(defaults)){
            assertTrue(AudioGraphEncoder.encode(graph(definition,Map.of())).contains(" B 1 50.0 1 5.0 -"));
        }
    }
    @Test void continuousDefaultIsIndependentOfIntegerPresentationAndConfigurationIsDiscrete(){
        assertTrue(Editor.Slider.button(0).continuousChanges());
        assertTrue(Editor.Slider.button(0).integers().continuousChanges());
        assertFalse(Editor.Slider.button(0).discrete().step(.1).integers().formats(UnitFormats.PROPORTION).continuousChanges());
        assertTrue(slider(new OscillatorNode(),"frequency").continuousChanges());
        assertTrue(slider(new ValueNode(),"value").continuousChanges());
        assertFalse(slider(new NoiseNode(),"seed").continuousChanges());
        assertFalse(slider(new StepSwitchNode(),"steps").continuousChanges());
        assertFalse(slider(new ValueNode(),"minimum").continuousChanges());
    }
    @Test void metadataIsScopedDoesNotAddNodesAndOverridesPersist(){
        var definition=new OscillatorNode();
        var assignment=new MidiDriverBinding("1","Device",2,7,true,new GLDriverMapping(100,1000,false,true,false),false,64,new ControlInterpolationSettings(true,8,35));
        assertEquals(assignment,MidiDriverBinding.read(assignment.value()));
        var source=graph(definition,Map.of(MidiDriverBinding.key("frequency"),assignment.value()));
        assertFalse(AudioGraphEncoder.encode(source).contains(" ~ "));
        try(var scope=ControlMetadata.use(new ControlInterpolationSettings(false,5,50))){
            String encoded=AudioGraphEncoder.encode(source);assertTrue(encoded.startsWith("GRAPH 1 "));assertTrue(encoded.contains("0 1 8.0 35.0"));assertTrue(encoded.contains(" 1 2 7 100.0 1000.0 0 1 0 0"));
        }
        assertFalse(AudioGraphEncoder.encode(source).contains(" ~ "));
        var original=new MidiDriverBinding("1","Device",0,7,true,new GLDriverMapping(0,1,false,false,false),false,64);
        assertNull(MidiDriverBinding.read(original.value()).interpolation());
        assertThrows(IllegalArgumentException.class,()->new ControlInterpolationSettings(true,10,5));
    }
    @Test void connectedPortsHaveNoManualMetadataButAssignmentsRemainInTheDocument(){
        var definition=new OscillatorNode();var source=new ValueNode();var oscillator=GLNodeId.random();var value=GLNodeId.random();var graph=new GLNodeGraph();
        var properties=new HashMap<>(definition.defaults());properties.put(MidiDriverBinding.key("frequency"),new MidiDriverBinding("1","Device",0,7,true,new GLDriverMapping(100,1000,false,false,false),false,64).value());
        graph.edit("Fixture",e->{e.addNode(new GLGraphNode(oscillator,definition.registration().id(),properties));e.addNode(new GLGraphNode(value,source.registration().id(),source.defaults()));e.addConnection(SpectralGroupsTest.wire(value,"value-out",oscillator,"frequency"));});
        var node=graph.snapshot().findNode(oscillator).orElseThrow();
        try(var scope=ControlMetadata.use(ControlInterpolationSettings.DEFAULT)){
            String metadata=ControlMetadata.encode(node,definition,definition.layout(new NodeState(node.properties())),Set.of("frequency"));assertTrue(metadata.startsWith(" ~ 1 2 "),metadata);
        }
        assertTrue(node.properties().containsKey(MidiDriverBinding.key("frequency")));
    }
    @Test void documentSettingsAndDriverAssignmentsSurviveSaving()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.controlInterpolation=new ControlInterpolationSettings(true,12,60);
            var saved=c.snapshotDocument();
            assertEquals(c.controlInterpolation,ControlInterpolationSettings.read(saved.root().metadata().get(ControlInterpolationSettings.KEY)));
            c.openDocument("Restored",saved);assertEquals(new ControlInterpolationSettings(true,12,60),c.controlInterpolation);
        }});
    }
    @Test void groupDefaultsAndDriverMappingsReachTheirExistingBoundaryAdapters(){
        var fixture=MultipleGroupsTest.fixture(false,"sum");
        var instance=fixture.instance();var properties=new HashMap<>(instance.properties());
        properties.put(MidiDriverBinding.key(GroupUnits.key("frequency")),new MidiDriverBinding("1","Device",0,7,true,new GLDriverMapping(200,800,false,false,false),false,64).value());
        var graph=new GLNodeGraph(fixture.document().root().graph());graph.edit("Assign",e->e.setProperty(instance.id(),MidiDriverBinding.key(GroupUnits.key("frequency")),properties.get(MidiDriverBinding.key(GroupUnits.key("frequency")))));
        graph.edit("Display unit",e->e.setProperty(instance.id(),GroupUnits.unitKey("frequency"),GLNodeValue.of("percent")));
        var root=fixture.document().root();root=new GLNodeGraphAsset(root.id(),root.version(),root.kind(),graph.snapshot(),root.viewState(),root.metadata(),root.compositeDescriptor());
        var registry=new com.vingame.glswing.node.composite.GLCompositeGraphRegistry();fixture.document().embeddedAssets().values().forEach(registry::registerAsset);
        try(var scope=ControlMetadata.use(ControlInterpolationSettings.DEFAULT)){
            var flattened=SpectralGroups.flatten(GroupCompiler.prepare(root,registry),new com.vingame.glswing.node.composite.GLCompositeExpansionLimits(16,256,512,10000));
            assertTrue(flattened.isValid(),flattened.diagnostics().toString());
            String program=(String)flattened.graph().orElseThrow().nodes().get(0).properties().get("program").toJavaValue();String decoded=new String(HexFormat.of().parseHex(program),java.nio.charset.StandardCharsets.UTF_8);assertTrue(decoded.contains(" 1 0 7 200.0 800.0"),decoded);assertTrue(decoded.contains(" LUT 20000.0 "),decoded);
        }
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeRampsBypassConnectedSignalsAndDoNotAllocateInTheRenderCallback()throws Exception{
        var directory=Files.createTempDirectory(Path.of("target"),"control-interpolation-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/ControlInterpolationTest.cpp").toAbsolutePath(),directory.resolve("test.exe"),directory);
    }
}
