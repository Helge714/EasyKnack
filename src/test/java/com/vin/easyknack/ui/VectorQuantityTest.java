package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
class VectorQuantityTest {
 @Test void allGenericVectorNodesOfferConsistentQuantities(){
  for(var node:List.of(new VectorNode(),new VectorCombineNode(),new VectorSeparateNode(),new VectorMathNode(),new VectorDistanceNode(),new VectorLengthNode(),new VectorDotNode()))
   for(var quantity:VectorQuantity.values()){
    var values=new HashMap<>(node.defaults());values.put("vectorQuantity",GLNodeValue.of(quantity.id()));
    var state=node.normalizeState(new NodeState(values));var layout=node.layout(state);
    assertTrue(layout.rows().stream().anyMatch(r->r.id().equals("vectorQuantity")));
    for(var row:layout.rows())if(row.editor() instanceof VectorEditor vector){
     assertEquals(quantity.formats(),vector.formats());
     if(state.values().containsKey(row.id()))assertDoesNotThrow(()->vector.validate(state.values().get(row.id())));
    }
   }
 }
 @Test void quantityChangePreservesComponentsAndResetsIncompatibleDisplayUnit(){
  var n=new VectorNode();var values=new HashMap<>(n.defaults());
  values.put("vectorQuantity",GLNodeValue.of("angle"));
  var vectorRow=n.layout(new NodeState(n.defaults())).rows().stream().filter(r->r.editor() instanceof VectorEditor).findFirst().orElseThrow();
  values.put(vectorRow.id(),VectorEditor.value(10,20,90,UnitFormat.METRE));
  var normalized=n.normalizeState(new NodeState(values));
  assertEquals(VectorEditor.value(10,20,90,UnitFormat.DEGREE),normalized.values().get(vectorRow.id()));
  var combine=new VectorCombineNode();var a=combine.layout(new NodeState(combine.defaults())).rows().stream().filter(r->r.id().equals("x")).findFirst().orElseThrow();
  values=new HashMap<>(combine.defaults());values.put("vectorQuantity",GLNodeValue.of("angle"));values.put(a.unitProperty().value(),GLNodeValue.of("metre"));
  var state=combine.normalizeState(new NodeState(values));assertEquals("degree",state.text(a.unitProperty(),""));
  var b=combine.layout(state).rows().stream().filter(r->r.id().equals("x")).findFirst().orElseThrow();
  assertNotEquals(a.editor().structuralKey(),b.editor().structuralKey());
 }
 @Test void groupFallbackRetainsItsExplicitUnitFamily(){
  assertEquals(UnitFormat.DEGREE,VectorQuantity.formats(new NodeState(Map.of("vectorQuantity",GLNodeValue.of("length"),"vectorUnit",GLNodeValue.of("degree")))).stored());
 }
 @Test void radiansAtCombineBecomeCanonicalDegreesAndSeparateCanEmitRadians(){
  var g=new GLNodeGraph();var combine=new VectorCombineNode();var separate=new VectorSeparateNode();
  var a=new GLGraphNode(GLNodeId.random(),combine.registration().id(),new HashMap<>(combine.defaults()));
  var b=new GLGraphNode(GLNodeId.random(),separate.registration().id(),new HashMap<>(separate.defaults()));
  var source=new GLGraphNode(GLNodeId.random(),new ValueNode().registration().id(),new ValueNode().defaults());
  var sink=new GLGraphNode(GLNodeId.random(),new MathNode().registration().id(),new MathNode().defaults());
  g.edit("Angular vectors",e->{for(var n:List.of(a,b,source,sink))e.addNode(n);
   e.setProperty(a.id(),"vectorQuantity",GLNodeValue.of("angle"));e.setProperty(b.id(),"vectorQuantity",GLNodeValue.of("angle"));
   e.setProperty(a.id(),"z.unit",GLNodeValue.of("radian"));e.setProperty(b.id(),"z.unit",GLNodeValue.of("radian"));
   e.addConnection(SpectralGroupsTest.wire(source.id(),"value-out",a.id(),"z"));
   e.addConnection(SpectralGroupsTest.wire(a.id(),"vector",b.id(),"vector"));
   e.addConnection(SpectralGroupsTest.wire(b.id(),"z",sink.id(),"a"));
  });
  var converted=PortUnitConversions.materialize(g.snapshot());
  var adapters=converted.nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof UnitConverterNode).toList();
  assertEquals(2,adapters.size());
  assertTrue(adapters.stream().anyMatch(n->new NodeState(n.properties()).text("from","").equals("radian")&&new NodeState(n.properties()).text("to","").equals("degree")));
  assertTrue(adapters.stream().anyMatch(n->new NodeState(n.properties()).text("from","").equals("degree")&&new NodeState(n.properties()).text("to","").equals("radian")));
 }

}
