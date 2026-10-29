package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.*;
final class GroupVectorEditors {
 static final String ID="easyknack.vector-editor";
 static boolean isVector(GLCompositePortMapping p){return p.presentation().inputEditorId().equals(ID);}
 static VectorEditor editor(GLCompositePortMapping p){
  var options=p.presentation().editorOptions();var state=new NodeState(options);var base=UnitFormat.fromId(state.text(GroupUnits.BASE,"metre"));
  var allowed=((List<?>)options.get(GroupUnits.ALLOWED).toJavaValue()).stream().map(v->UnitFormat.fromId(v.toString())).toList();
  var value=(List<?>)options.get("default").toJavaValue();var initial=UnitFormat.fromId(value.get(3).toString());
  return new VectorEditor(((Number)value.get(0)).doubleValue(),((Number)value.get(1)).doubleValue(),((Number)value.get(2)).doubleValue(),new UnitFormats(base,initial,allowed));
 }
}
