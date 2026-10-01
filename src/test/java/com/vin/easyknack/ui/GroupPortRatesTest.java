package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class GroupPortRatesTest {
    @Test void viewsAndBoundaryPortsRefreshAfterRateEditsAndUndo() throws Exception {
        var holder = new java.util.concurrent.atomic.AtomicReference<WorkspaceController>();
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            var doc=GroupUnitsTest.document(false);var original=doc.embeddedAssets().get("test.asset");
            var math=new MathNode();var inner=new GLNodeGraph();
            inner.edit("Math",e->e.addNode(new GLGraphNode(GroupUnitsTest.OSC,math.registration().id(),math.defaults()).withProperty("rate",GLNodeValue.of("control"))));
            var descriptor=new GLCompositeNodeDescriptor("test.group","test.asset",1,List.of(
                    new GLCompositePortMapping(GLNodePortDescriptor.input("Pitch",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(GroupUnitsTest.OSC,"a")),
                    new GLCompositePortMapping(GLNodePortDescriptor.output("Out",SignalCategory.AUDIO.valueTypeId()),new GLNodeEndpoint(GroupUnitsTest.OSC,"audio-out"))));
            var c=new WorkspaceController();holder.set(c);
            c.openDocument("Rates",new GLNodeGraphDocument(doc.root(),Map.of(original.id(),asset(descriptor,inner)),List.of()));
        });
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                var c=holder.get();
                assertEquals(ProcessingRate.CONTROL.shape(),GroupUnitsTest.group(c).findPort("Out").getShape());
                assertEquals(ApplicationTheme.current().signal(SignalCategory.AUDIO),GroupUnitsTest.group(c).findPort("Out").getColor());
                assertTrue(c.workspace().openComposite("test.asset"));
                c.setNodeProperty(GroupUnitsTest.OSC,MathNode.RATE,GLNodeValue.of("event"));
            });
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                var c=holder.get();
                var ports=c.workspace().editor().getNodes().stream().filter(n->!(n instanceof BasicNodeView))
                        .map(n->n.findPort("Out")).filter(Objects::nonNull).toList();
                assertFalse(ports.isEmpty());
                ports.forEach(p->assertEquals(ProcessingRate.AUDIO.shape(),p.getShape()));
                c.history().undo();
            });
            javax.swing.SwingUtilities.invokeAndWait(() -> holder.get().workspace().editor().getNodes().stream()
                    .filter(n->!(n instanceof BasicNodeView)).map(n->n.findPort("Out")).filter(Objects::nonNull)
                    .forEach(p->assertEquals(ProcessingRate.CONTROL.shape(),p.getShape())));
        } finally { javax.swing.SwingUtilities.invokeAndWait(() -> holder.get().close()); }
    }

    @Test void fanoutUsesFastestRateAndTracksDefinitionChanges() {
        var registry = new GLCompositeGraphRegistry();
        var math = new MathNode(); var graph = new GLNodeGraph();
        var a = GLNodeId.random(); var b = GLNodeId.random();
        graph.edit("Nodes", e -> {
            e.addNode(new GLGraphNode(a, math.registration().id(), math.defaults()).withProperty("rate",GLNodeValue.of("control")));
            e.addNode(new GLGraphNode(b, math.registration().id(), math.defaults()).withProperty("rate",GLNodeValue.of("event")));
        });
        var input = new GLCompositePortMapping(GLNodePortDescriptor.input("In",SignalCategory.VALUE.valueTypeId(),false,1),
                new GLNodeEndpoint(a,"a")).withEndpoints(List.of(new GLNodeEndpoint(a,"a"),new GLNodeEndpoint(b,"a")));
        var descriptor = new GLCompositeNodeDescriptor("test.rates","test.rates.asset",1,List.of(input));
        registry.registerAsset(asset(descriptor,graph));
        assertEquals(ProcessingRate.AUDIO,new GroupPortRates(registry,Map.of()).rate(descriptor,input));
        graph.edit("Audio",e -> e.setProperty(b,"rate",GLNodeValue.of("audio")));
        registry.replaceAsset(asset(descriptor,graph));
        assertEquals(ProcessingRate.AUDIO,new GroupPortRates(registry,Map.of()).rate(descriptor,input));
        graph.edit("Auto",e -> e.setProperty(b,"rate",GLNodeValue.of("auto")));
        registry.replaceAsset(asset(descriptor,graph));
        assertEquals(ProcessingRate.CONTROL,new GroupPortRates(registry,Map.of()).rate(descriptor,input));
        assertEquals(ProcessingRate.AUDIO,new GroupPortRates(registry,Map.of(new GroupPortRates.Key(descriptor.assetId(),b,"a"),GLNodeValue.of("audio"))).rate(descriptor,input));
    }

    @Test void nestedGroupsKeepInputAndOutputRatesIndependent() {
        var registry = new GLCompositeGraphRegistry();
        registry.registerDocument(GroupUnitsTest.document(true));
        var descriptor=registry.findComposite("test.outer").orElseThrow();
        var rates=new GroupPortRates(registry,Map.of());
        for(var mapping:descriptor.portMappings())assertEquals(mapping.publicPort().direction()==GLPortDirection.OUTPUT?ProcessingRate.AUDIO:ProcessingRate.CONTROL,rates.rate(descriptor,mapping));
        // The oscillator waveform input is ControlRate even though its output is AudioRate.
        var inner=registry.findComposite("test.group").orElseThrow();
        var wave=new GLCompositePortMapping(GLNodePortDescriptor.input("Wave",SignalCategory.WAVETABLE.valueTypeId(),false,1),new GLNodeEndpoint(GroupUnitsTest.OSC,"waveform"));
        assertEquals(ProcessingRate.CONTROL,rates.rate(inner,wave));
    }

    private static GLNodeGraphAsset asset(GLCompositeNodeDescriptor descriptor,GLNodeGraph graph) {
        return new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
    }
}
