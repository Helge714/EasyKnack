package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;import java.nio.file.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
class SpectrumArithmeticTest {
    @Test void borrowedResourcesUseReferenceCables(){
        for(var type:List.of(PortDataType.HISTORY,PortDataType.ANALYSIS,PortDataType.SPECTRUM,PortDataType.WAVEFORM,PortDataType.PARTIALS)){
            assertTrue(type.isReference());assertTrue(PortDataType.isReference(type.id()));
        }
        assertFalse(PortDataType.SIGNAL.isReference());assertFalse(PortDataType.VECTOR.isReference());
    }
    @Test void oldWeightedMixesMigrateWithoutChangingConnections(){
        var doc=PartialsTest.document(false);var root=doc.root();
        var id=root.graph().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof SpectrumMathNode).findFirst().orElseThrow().id();
        var graph=new GLNodeGraph(root.graph());graph.edit("Legacy mix",e->{e.removeNode(id);e.addNode(new GLGraphNode(id,new SpectrumMixNode().registration().id(),Map.of("gain-a",GLNodeValue.of(2.),"gain-b",GLNodeValue.of(3.))));});
        // Replacing the node in the editor removes wires; restore the original endpoints to emulate a saved file.
        var legacy=new GLNodeGraphAsset(root.id(),root.version(),root.kind(),new GLNodeGraphSnapshot(root.graph().graphId(),0,graph.snapshot().nodes(),root.graph().connections()),root.viewState(),root.metadata(),root.compositeDescriptor());
        var migrated=NodeDocuments.prepare(new GLNodeGraphDocument(legacy,doc.embeddedAssets(),List.of()));var n=migrated.root().graph().findNode(id).orElseThrow();
        assertEquals(new SpectrumMathNode().registration().id(),n.typeId());assertEquals(2,new NodeState(n.properties()).number("gain-a",0));assertEquals("add",new NodeState(n.properties()).text("operation",""));assertEquals(root.graph().connections(),migrated.root().graph().connections());
        assertEquals(migrated,SpectrumMixMigration.migrate(migrated));
        var mix=new SpectrumMixNode();assertFalse(mix.defaults().containsKey("gain-a"));assertEquals(.5,new NodeState(mix.defaults()).number("factor",0));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true") void nativeArithmetic()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"spectrum-arithmetic-").toAbsolutePath();var math=new SpectrumMathNode();var mix=new SpectrumMixNode();
        Files.writeString(dir.resolve("SpectrumArithmeticParameters.hpp"),"#pragma once\nstatic const char* mathParameters=R\"p(!"+NativeContractGenerator.fingerprint(math)+" "+math.encode(new NodeState(math.defaults()),p->-1)+")p\";\nstatic const char* mixParameters=R\"p(!"+NativeContractGenerator.fingerprint(mix)+" "+mix.encode(new NodeState(mix.defaults()),p->-1)+")p\";\n");
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/SpectrumArithmeticTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
