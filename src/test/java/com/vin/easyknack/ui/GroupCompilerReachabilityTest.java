package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.*;

class GroupCompilerReachabilityTest {
    @Test void preparesNestedDependenciesWithoutProcessingUnusedGroups(){
        var fixture=InstanceParametersTest.nested();
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(fixture.document());
        var unused=MultipleGroups.create();
        // Invalid unused source must not enter the execution compiler at all.
        var metadata=new HashMap<>(unused.metadata());
        metadata.put(FormulaGroups.MARKER,GLNodeValue.of(true));
        metadata.put(FormulaGroups.SOURCE,GLNodeValue.of("this is not a formula"));
        unused=FormulaGroups.metadata(unused,metadata);registry.registerAsset(unused);
        var original=List.copyOf(registry.assets());
        var prepared=GroupCompiler.prepare(fixture.document().root(),registry);
        assertEquals(fixture.document().embeddedAssets().keySet(),prepared.registry().assets().stream()
            .map(a->a.id()).collect(java.util.stream.Collectors.toSet()));
        var flat=SpectralGroups.flatten(prepared,new GLCompositeExpansionLimits(16,256,512,10000));
        assertTrue(flat.isValid(),flat.diagnostics().toString());
        assertEquals(original,List.copyOf(registry.assets()),"Execution must preserve unused document assets");
    }
}
