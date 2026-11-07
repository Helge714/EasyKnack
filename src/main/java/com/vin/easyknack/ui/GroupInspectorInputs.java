package com.vin.easyknack.ui;

import java.util.*;
import java.util.function.Consumer;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

/** Inspector controls mirror public group editors, including inherited defaults. */
final class GroupInspectorInputs {
 private record Registration(Object signature,List<GLCustomNodeProperty> properties){}
 private static final Map<GLNodePropertySchemaRegistry,Map<String,Registration>> installed=new WeakHashMap<>();
 static void register(GLNodePropertySchemaRegistry schemas,GLCompositeGraphRegistry groups,GLHistoryManager history,NodeViewContext.PropertyWriter writer){
  var previous=installed.computeIfAbsent(schemas,k->new HashMap<>());
  for(var d:groups.composites()){
   var asset=groups.findAsset(d.assetId()).orElseThrow();
   var ports=d.portMappings().stream().filter(p->p.publicPort().direction()==GLPortDirection.INPUT&&!MultipleGroups.countPort(p)&&(GroupVectorEditors.isVector(p)||GroupInputs.choice(p)||p.presentation().inputEditorId().equals(GroupInputSettings.BUTTON)||p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER))).toList();
   var signature=List.of(ports,GroupChoices.choices(asset,groups).stream().map(c->List.of(c.id(),c.label(),c.editor().items(),c.editor().defaultValue())).toList());var old=previous.get(d.nodeTypeId());
   var current=new ArrayList<>(schemas.properties(d.nodeTypeId()));
   if(old!=null&&old.signature().equals(signature)&&old.properties().stream().allMatch(p->current.stream().anyMatch(v->v==p)))continue;
   var ids=new HashSet<String>();if(old!=null)old.properties().forEach(p->ids.add(p.id()));ports.forEach(p->ids.add(key(p)));
   current.removeIf(p->ids.contains(p.id()));var own=new ArrayList<GLCustomNodeProperty>();
   for(var p:ports)own.add(new GLCustomNodeProperty(key(p),p.publicPort().id(),
    (node,changed)->new Control(groups,d,p,node,changed,history,writer),
    (control,node)->((Control)control).update(node)));
   current.addAll(own);schemas.register(d.nodeTypeId(),current);previous.put(d.nodeTypeId(),new Registration(signature,List.copyOf(own)));
  }
 }
 private static String key(GLCompositePortMapping port){return GroupInputs.choice(port)?GroupInputs.property(port):GroupUnits.key(port.publicPort().id());}
 private static Editor editor(GLCompositeGraphRegistry groups,GLCompositeNodeDescriptor d,GLCompositePortMapping p,GLGraphNode node){
  if(GroupVectorEditors.isVector(p))return GroupVectorEditors.editor(p);
  if(GroupInputs.choice(p))return GroupChoices.choices(groups.findAsset(d.assetId()).orElseThrow(),groups).stream().filter(c->c.id().equals(GroupInputs.property(p))).map(c->(Editor)c.editor()).findFirst().orElse(null);
  if(p.presentation().inputEditorId().equals(GroupInputSettings.BUTTON))return new Editor.Trigger();
  if(GroupInputSettings.checkbox(p))return new Editor.Toggle(new NodeState(p.presentation().editorOptions()).number("default",0)!=0);
  if(!p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER))return null;
  var options=new NodeState(p.presentation().editorOptions());var base=GroupUnits.base(groups,d,p);
  var spec=new Editor.Slider(options.number("default",0),options.flag("minimumEnabled",true)?options.number("minimum",0):null,options.flag("maximumEnabled",true)?options.number("maximum",1):null,options.number("step",.01),options.text("numericType","float").equals("integer"),options.text("presentation","bar").equals("bar"));
  if(!options.flag("continuous",true))spec=spec.discrete();
  return new UnitSlider(spec,new UnitFormats(base,GroupUnits.initial(p,base),GroupUnits.allowed(p,base)),GroupUnits.selected(node,p,base));
 }
 static final class Control extends GLContainer {
  private final GLCompositeGraphRegistry groups;private final GLCompositeNodeDescriptor descriptor;private final GLCompositePortMapping port;
  private final Editor.Control binding;private final GLLabel label;private boolean updating;
  Control(GLCompositeGraphRegistry groups,GLCompositeNodeDescriptor d,GLCompositePortMapping port,GLGraphNode node,Consumer<GLNodeValue> changed,GLHistoryManager history,NodeViewContext.PropertyWriter writer){
   this.groups=groups;descriptor=d;this.port=port;setName(key(port));var definition=editor(groups,d,port,node);
   if(definition instanceof Editor.Trigger){var button=new GLButton(port.publicPort().id());button.addActionListener(e->GroupInputTriggers.fire(groups,node.id().toString(),d,port));binding=new Editor.Control(button,v->{});}
   else binding=definition.create(port.publicPort().id(),value->{if(!updating)writer.set(node.id(),new PropertyId(key(port)),value);});
   label=definition instanceof Editor.ComboBox<?>?new GLLabel(port.publicPort().id()):null;if(label!=null)addGL(label);addGL(binding.component());
   if(definition instanceof UnitSlider units){UnitSlider.bindHistory((GLSlider)binding.component(),()->history);units.bindUnitMenu((GLSlider)binding.component(),unit->writer.set(node.id(),new PropertyId(GroupUnits.unitKey(port.publicPort().id())),GLNodeValue.of(unit.id())));}
   if(binding.component() instanceof GLSliderGroup vector)for(var slider:vector.getSliders())UnitSlider.bindHistory(slider,()->history);
   if(binding.component() instanceof GLControl control){control.setName(port.publicPort().id());MidiDrivers.attachInspector(node.id(),key(port),control);}
   float height=binding.component().getPreferredGLSize().height();setPreferredGLSize(220,Math.max(28,height)+(label==null?0:24));update(node);
  }
  @SuppressWarnings("unchecked") void update(GLGraphNode node){
   updating=true;try{
    var definition=editor(groups,descriptor,port,node);var value=node.properties().getOrDefault(key(port),definition.defaultValue());
    if(GroupInputs.choice(port)){
     var asset=groups.findAsset(descriptor.assetId()).orElseThrow();var choice=GroupChoices.choices(asset,groups).stream().filter(c->c.id().equals(key(port))).findFirst().orElseThrow();value=GroupChoices.selection(asset,choice,value);
    }
    if(definition instanceof UnitSlider unit&&binding.component() instanceof GLUnitSlider<?> slider)((GLUnitSlider<UnitFormat>)slider).setDisplayUnit(unit.selected());
    binding.update().accept(value);
   }finally{updating=false;}
  }
  @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
  @Override protected void resized(float w,float h){float top=label==null?0:24;if(label!=null)label.setBounds(0,0,w,24);binding.component().setBounds(0,top,w,Math.max(0,h-top));}
 }
}
