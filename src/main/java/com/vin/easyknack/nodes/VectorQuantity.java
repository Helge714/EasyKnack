package com.vin.easyknack.nodes;
import java.util.*;
/** Explicit interpretation of generic XYZ values; connections never select this automatically. */
public enum VectorQuantity {
 LENGTH(UnitFormats.LENGTH),
 ANGLE(new UnitFormats(UnitFormat.DEGREE,UnitFormat.DEGREE,List.of(UnitFormat.DEGREE,UnitFormat.RADIAN,UnitFormat.TURN))),
 SCALE(UnitFormats.PROPORTION), UNITLESS(UnitFormats.fixed(UnitFormat.FACTOR));
 public static final PropertyId PROPERTY=new PropertyId("vectorQuantity");
 private final UnitFormats formats;
 VectorQuantity(UnitFormats formats){this.formats=formats;}
 public UnitFormats formats(){return formats;}
 public String id(){return name().toLowerCase(Locale.ROOT);}
 public String toString(){return NodeText.text("vector.quantity."+id(),name());}
 public static UnitFormats formats(NodeState s){
  // Private group adapters may carry a canonical unit without a public quantity setting.
  if(s.values().containsKey("vectorUnit")){
   var unit=UnitFormat.fromId(s.text("vectorUnit","metre"));return new UnitFormats(unit,unit,unit.compatible());
  }
  return valueOf(s.text(PROPERTY,"length").toUpperCase(Locale.ROOT)).formats;
 }
 public static void addEditor(NodeBuilder n){n.addEditor(PROPERTY,NodeText.text("vector.quantity.label","Quantity"),Editor.comboBox(List.of(values()),LENGTH,VectorQuantity::id)).inspectorOnly().editorCopyable(false);}
 public static void input(NodeBuilder n,PropertyId id,String label,NodeState s){n.addInput(id,label,new VectorEditor(0,0,0,formats(s))).dataType(PortDataType.VECTOR).signal(SignalCategory.VECTOR).rates(PortRates.AUDIO_CONTROL);}
}
