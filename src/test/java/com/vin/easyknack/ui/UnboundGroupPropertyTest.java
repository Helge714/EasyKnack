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
 @Test void removedInputDefaultsAreIgnoredButUnknownUserPropertiesStillFail(){
  var asset=FormulaGroups.create();var key=GroupUnits.key("chroma");
  var node=new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of(key,GLNodeValue.of(7.),key+".unit",GLNodeValue.of("factor"),GLGraphNode.AUTOMATIC_PREFIX+key,GLNodeValue.of(false)));
  for(boolean unknown:List.of(false,true)){
   var current=unknown?node.withProperty("unrecognized",GLNodeValue.of(1)):node;var graph=new GLNodeGraph();graph.edit("Instance",e->e.addNode(current));
   var root=new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty());var registry=new GLCompositeGraphRegistry();registry.registerAsset(asset);
   var prepared=GroupCompiler.prepare(root,registry);var values=prepared.root().graph().nodes().get(0).properties();assertFalse(values.containsKey(key));assertFalse(values.containsKey(key+".unit"));assertFalse(values.containsKey(GLGraphNode.AUTOMATIC_PREFIX+key));
   var flat=GLCompositeGraphFlattener.flatten(prepared.root(),prepared.registry(),new GLCompositeExpansionLimits(16,256,512,10000));assertEquals(!unknown,flat.isValid(),flat.diagnostics().toString());
   assertTrue(current.properties().containsKey(key));
  }
 }
 @Test void formulaPhysicalUnitOverridesGenericKernelDefaults(){
  var definition=new FormulaNode();var values=new HashMap<>(definition.defaults());values.put("unit.in0",GLNodeValue.of("hz"));values.put("in0.unit",GLNodeValue.of("factor"));
  var row=definition.layout(new NodeState(values)).rows().stream().filter(r->r.id().equals("in0")).findFirst().orElseThrow();assertEquals(UnitFormat.HZ,row.unit());
 }}
