package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.NodeState;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.GLCheckBox;
/** Numeric checkbox bindings shared by group node and inspector state. */
final class GroupCheckboxes {
 private static final Map<GLNodePropertySchemaRegistry,Map<String,List<GLCompositePortMapping>>> installed=new WeakHashMap<>();
 private static final class Control extends GLCheckBox {
  boolean updating;
  Control(String label,java.util.function.Consumer<GLNodeValue> changed){super(label);addActionListener(e->{if(!updating)changed.accept(GLNodeValue.of(isSelected()?1.0:0.0));});}
  void sync(GLNodeValue value){updating=true;try{setSelected(((Number)value.toJavaValue()).doubleValue()!=0);}finally{updating=false;}}
 }
 static void register(GLNodePropertySchemaRegistry schemas,GLCompositeGraphRegistry registry){
  var previous=installed.computeIfAbsent(schemas,k->new HashMap<>());
  for(var d:registry.composites()){
   var ports=d.portMappings().stream().filter(GroupInputSettings::checkbox).toList();
   var old=previous.get(d.nodeTypeId());if(ports.equals(old))continue;
   var properties=new ArrayList<>(schemas.properties(d.nodeTypeId()));
   if(old!=null)properties.removeIf(p->old.stream().anyMatch(port->GroupUnits.key(port.publicPort().id()).equals(p.id())));
   for(var p:ports){String id=GroupUnits.key(p.publicPort().id());var fallback=p.presentation().editorOptions().getOrDefault("default",GLNodeValue.of(0.0));
    properties.removeIf(v->v.id().equals(id));properties.add(new GLCustomNodeProperty(id,p.publicPort().id(),(node,changed)->{var c=new Control(p.publicPort().id(),changed);c.sync(node.properties().getOrDefault(id,fallback));return c;},(c,node)->((Control)c).sync(node.properties().getOrDefault(id,fallback))));
   }
   schemas.register(d.nodeTypeId(),properties);previous.put(d.nodeTypeId(),ports);
  }
 }
}
