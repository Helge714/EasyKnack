package com.vin.easyknack.nodes;
import java.util.*;
import com.vingame.glswing.node.model.GLNodeValue;
/** Keeps numbers intact when the user explicitly changes a vector's physical interpretation. */
public abstract class GenericVectorNode extends BasicNode {
 @Override public NodeState normalizeState(NodeState state){
  var builder=new NodeBuilder();define(builder,state);var values=new HashMap<>(state.values());
  for(var row:builder.rows()){
   if(row.editor() instanceof VectorEditor editor){
    var old=values.get(row.id());
    if(old!=null && old.toJavaValue() instanceof List<?> a && a.size()==4 && !editor.formats().choices().contains(UnitFormat.fromId(a.get(3).toString())))
     values.put(row.id(),GLNodeValue.of(List.of(a.get(0),a.get(1),a.get(2),editor.formats().initial().id())));
   }
   var formats=row.formats()!=null?row.formats():row.editor() instanceof Editor.Slider slider?slider.formats():null;
   if(formats!=null){var key=row.unitProperty().value();var unit=values.get(key);if(unit!=null&&!formats.choices().contains(UnitFormat.fromId(unit.toJavaValue().toString())))values.put(key,GLNodeValue.of(formats.initial().id()));}
  }
  return new NodeState(values,state.connectedInputs());
 }
}
