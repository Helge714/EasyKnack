package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;

class GroupInputFallbackTest {
    @Test void socketFallbackUsesStoredInternalValueButExplicitPublicDefaultWins(){
        var graph=new GLNodeGraph();var node=AdvancedSourceExportTest.add(graph,new MathNode(),Map.of("a",GLNodeValue.of(.4)));
        var port=new GLCompositePortMapping(GLNodePortDescriptor.input("In",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(node,"a"));
        var descriptor=new GLCompositeNodeDescriptor("test.fallback","test.fallback.asset",1,List.of(port));
        var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var registry=new GLCompositeGraphRegistry();registry.registerAsset(asset);
        assertEquals(.4,GroupUnits.inputDefault(registry,descriptor,port));
        var explicit=new GLCompositePortMapping(port.publicPort(),port.internalEndpoint(),new GLCompositePortPresentation("slider",true,Map.of("default",GLNodeValue.of(.7))));
        assertEquals(.7,GroupUnits.inputDefault(registry,descriptor,explicit));
    }
}
