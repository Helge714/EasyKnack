package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.binding.*;
class UnboundGroupPropertyTest {
 @Test void unusedSliderAndPendingFormulaInputDoNotRejectTheirSavedValues(){
  for(boolean formula:List.of(false,true)){
   var asset=formula?FormulaGroups.create():SpectralGroups.create();var d=asset.compositeDescriptor();var ports=new ArrayList<>(d.portMappings());
   var p=new GLCompositePortMapping(GLNodePortDescriptor.input("chroma",SignalCategory.VALUE.valueTypeId(),false,1),null,new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,Map.of("default",GLNodeValue.of(0.))));
   ports.add(formula?FormulaGroups.normalize(p):p);
   asset=GroupInputs.descriptor(asset,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports,d.properties(),d.propertyMappings()));
   var node=new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of(GroupUnits.key("chroma"),GLNodeValue.of(7.),GroupUnits.unitKey("chroma"),GLNodeValue.of("factor")));
   var graph=new GLNodeGraph();graph.edit("Instance",e->e.addNode(node));
   var root=new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty());var registry=new GLCompositeGraphRegistry();registry.registerAsset(asset);
   var prepared=GroupCompiler.prepare(root,registry);
   var flat=GLCompositeGraphFlattener.flatten(prepared.root(),prepared.registry(),new GLCompositeExpansionLimits(16,256,512,10000));
   assertTrue(flat.isValid(),flat.diagnostics().toString());
  }
 }
}
