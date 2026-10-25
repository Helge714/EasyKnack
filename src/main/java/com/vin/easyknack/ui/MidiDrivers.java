package com.vin.easyknack.ui;

import com.vin.easyknack.audio.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.component.*;
import com.vingame.glswing.history.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;
import java.util.*;

/** Document-scoped MIDI remote control. All model writes occur on the EDT. */
public final class MidiDrivers implements AutoCloseable {
    static final String ROOT="";
    private static final Map<GLHistoryManager,MidiDrivers> owners=new WeakHashMap<>();
    record Address(String asset,GLNodeId node,String property){}
    record Spec(Editor editor,String port,Runnable action){}
    private record Runtime(MidiDriverBinding binding,GLDriverMapping.Pickup pickup,boolean[] high){}
    private final WorkspaceController owner;
    private final Map<Address,Runtime> runtime=new HashMap<>();
    private final Map<Address,GLNodeValue> external=new LinkedHashMap<>();
    private Set<Address> suspended=Set.of();
    private final List<java.lang.ref.WeakReference<GLControl>> controls=new ArrayList<>();
    private final Map<GLControl,Address> addresses=new WeakHashMap<>();
    private final ArrayDeque<MidiDriverInput.Event> pending=new ArrayDeque<>();
    private final javax.swing.Timer timer=new javax.swing.Timer(16,e->flush());
    private final AutoCloseable subscription;
    private final GLHistoryManagerListener historyListener=h->{restoreExternal();refresh();};
    private Address learning;private GLDriverTarget.Kind learningKind;
    private GLDialog learnDialog;private boolean restoring,closed;
    MidiDrivers(WorkspaceController owner){
        this.owner=owner;owners.put(owner.history(),this);owner.history().addListener(historyListener);
        subscription=MidiDriverInput.listen(events->{if(!closed){pending.addAll(events);if(!timer.isRunning())timer.start();}});
        timer.setRepeats(false);
    }
    static MidiDrivers of(WorkspaceController owner){return owners.get(owner.history());}
    public static void attach(GLHistoryManager history,GLNodeId node,String property,GLContainer component){
        var owner=owners.get(history);if(owner!=null)owner.attach(node,property,component);
    }
    public static void attachInspector(GLNodeId node,String property,GLContainer component){
        java.awt.EventQueue.invokeLater(()->{
            for(var owner:List.copyOf(owners.values()))if(!owner.closed&&inside(component,owner.owner.workspace())&&owner.owner.workspace().currentGraph().snapshot().findNode(node).isPresent()){
                owner.attach(node,property,component);break;
            }
        });
    }
    private static boolean inside(GLContainer component,GLContainer root){for(var current=component;current!=null;current=current.getGLParent() instanceof GLContainer parent?parent:null)if(current==root)return true;return false;}
    private void attach(GLNodeId node,String property,GLContainer component){
        if(!(component instanceof GLControl control))return;
        var address=new Address(owner.driverCurrentAsset(),node,property);var target=target(address);
        if(target==null)return;
        boolean fresh=!addresses.containsKey(control);addresses.put(control,address);control.setDriverTarget(target);
        if(!fresh){control.setDriverIndicator(binding(address)!=null,"easyknack.signal.MIDI");return;}
        if(fresh)controls.add(new java.lang.ref.WeakReference<>(control));
        GLPopupMenu menu=control instanceof GLSlider slider?slider.getContextMenu():new GLPopupMenu();
        var actions=new GLPopupMenu();menu.addSubMenu(AppText.text("driver.title"),actions);
        actions.addItem(AppText.text("driver.learn"),e->learn(address,control));
        var configure=actions.addItem(AppText.text("driver.configure"),e->showSettings(address,control));
        var remove=actions.addItem(AppText.text("driver.remove"),e->assign(address,null));
        control.setDriverContextMenu(()->{boolean assigned=binding(address)!=null;configure.setEnabled(assigned);remove.setEnabled(assigned);return menu;});
        control.setDriverIndicator(binding(address)!=null,"easyknack.signal.MIDI");
    }
    GLGraphNode node(Address a){return owner.driverGraphs().getOrDefault(a.asset(),empty()).findNode(a.node()).orElse(null);}
    private static GLNodeGraphSnapshot empty(){return new GLNodeGraph().snapshot();}
    MidiDriverBinding binding(Address a){var node=node(a);var value=node==null?null:node.properties().get(MidiDriverBinding.key(a.property()));return value==null?null:MidiDriverBinding.read(value);}
    Spec spec(Address a){
        var node=node(a);if(node==null)return null;
        var definition=NodeRegistry.find(node.typeId()).orElse(null);
        if(definition!=null){var row=definition.layout(new NodeState(node.properties())).rows().stream().filter(r->r.id().equals(a.property())).findFirst().orElse(null);
            return row==null||!row.isDriverAssignable()?null:new Spec(row.editor(),row.kind()==NodeBuilder.RowKind.INPUT?row.id():null,()->ManualTriggers.fire(a.node().toString(),a.property()));}
        return owner.driverGroupSpec(node,a.property());
    }
    GLDriverTarget target(Address a){
        var spec=spec(a);if(spec==null)return null;final Editor original=spec.editor();Editor editor=original;
        java.util.function.Supplier<GLNodeValue> read=()->{var node=node(a);return node==null?original.defaultValue():node.properties().getOrDefault(a.property(),original.defaultValue());};
        java.util.function.Consumer<GLNodeValue> write=v->{external.put(a,v);owner.driverWrite(a.asset(),a.node(),a.property(),v);};
        if(editor instanceof UnitSlider u)editor=u.spec();
        if(editor instanceof Editor.Slider slider)return GLDriverTarget.number(()->((Number)read.get().toJavaValue()).doubleValue(),v->write.accept(GLNodeValue.of(v)),slider.min()==null?-Double.MAX_VALUE:slider.min(),slider.max()==null?Double.MAX_VALUE:slider.max(),slider.integral());
        if(editor instanceof Editor.CheckBox)return GLDriverTarget.toggle(()->Boolean.TRUE.equals(read.get().toJavaValue())?1:0,v->write.accept(GLNodeValue.of(v!=0)));
        if(editor instanceof Editor.Toggle)return GLDriverTarget.toggle(()->((Number)read.get().toJavaValue()).doubleValue(),v->write.accept(GLNodeValue.of(v)));
        if(editor instanceof Editor.Trigger)return GLDriverTarget.action(spec.action());
        if(editor instanceof Editor.ComboBox<?> combo)return choice(combo,read,write);
        return null;
    }
    private static <T> GLDriverTarget choice(Editor.ComboBox<T> combo,java.util.function.Supplier<GLNodeValue> read,java.util.function.Consumer<GLNodeValue> write){
        return GLDriverTarget.choice(()->{String id=String.valueOf(read.get().toJavaValue());for(int i=0;i<combo.items().size();i++)if(combo.id().apply(combo.items().get(i)).equals(id))return i;return 0;},
            value->write.accept(GLNodeValue.of(combo.id().apply(combo.items().get((int)value)))),()->combo.items().size());
    }
    private boolean blocked(Address a){
        var spec=spec(a);var graph=owner.driverGraphs().get(a.asset());
        return spec==null||graph==null||owner.driverControlled(a.asset(),a.node(),a.property())||spec.port()!=null&&graph.connections().stream().anyMatch(c->c.input().equals(new GLNodeEndpoint(a.node(),spec.port())));
    }
    void learn(Address address,GLParent parent){
        if(!beginLearn(address))return;
        var panel=new GLContainer(){
            final GLLabel text=new GLLabel(AppText.text("driver.learnHint"));final GLButton cancel=new GLButton(AppText.text("cancel"));
            {addGL(text);addGL(cancel);cancel.addActionListener(e->cancelLearn());}
            protected void resized(float w,float h){text.setBounds(16,12,Math.max(0,w-32),60);cancel.setBounds(Math.max(0,w-130),h-40,114,28);}
            protected void record(com.vingame.glswing.rendering.GLContext2D c){}
        };
        learnDialog=new GLDialog(AppText.text("driver.learn"),panel);var opened=learnDialog;
        opened.setDecoration(GLDialog.Decoration.THEMED);opened.setSize(580,140);opened.closedFuture().thenRun(()->{if(learnDialog==opened){learnDialog=null;learning=null;refresh();}});opened.show(parent);
    }
    boolean beginLearn(Address address){var target=target(address);if(target==null)return false;cancelLearn();learning=address;learningKind=target.kind();refresh();return true;}
    private void cancelLearn(){learning=null;if(learnDialog!=null){var dialog=learnDialog;learnDialog=null;dialog.close();}refresh();}
    void assign(Address a,MidiDriverBinding binding){
        owner.driverConfigure(a.asset(),a.node(),MidiDriverBinding.key(a.property()),binding==null?null:binding.value());
        runtime.remove(a);refresh();
    }
    private void showSettings(Address a,GLParent parent){
        owner.workspace().setInspectorVisible(true);
        var panel=new MidiDriverSettingsPanel(binding(a),target(a),value->assign(a,MidiDriverBinding.read(value)));
        var dialog=new GLDialog(AppText.text("driver.configure"),panel);dialog.setDecoration(GLDialog.Decoration.THEMED);dialog.setSize(580,440);dialog.show(parent);
    }
    void flush(){
        if(closed){pending.clear();return;}
        var events=new ArrayList<MidiDriverInput.Event>(pending);pending.clear();
        // Coalesce only adjacent CC events for the same controller; note ordering is preserved.
        var compact=new ArrayList<MidiDriverInput.Event>();for(var event:events){
            if(learning==null&&event.type()==0xb0&&!compact.isEmpty()&&coalescible(event)){var previous=compact.get(compact.size()-1);if(previous.type()==0xb0&&previous.device().equals(event.device())&&previous.channel()==event.channel()&&previous.number()==event.number()){compact.set(compact.size()-1,event);continue;}}
            compact.add(event);
        }
        for(var event:compact){
            if(learning!=null&&(event.type()==0xb0||event.on()&&learningKind!=GLDriverTarget.Kind.NUMBER)){
                var a=learning;var spec=spec(a);if(spec==null){cancelLearn();continue;}Editor editor=spec.editor();if(editor instanceof UnitSlider u)editor=u.spec();
                double current=target(a).read();
                double min=editor instanceof Editor.Slider s&&s.min()!=null?s.min():Math.min(0,current),max=editor instanceof Editor.Slider s&&s.max()!=null?s.max():Math.max(1,current);
                if(min>max){if(editor instanceof Editor.Slider s&&s.min()!=null)max=min;else min=max;}
                String name=MidiDevices.current().stream().filter(d->d.id().equals(event.device())).map(MidiDevices.Device::name).findFirst().orElse("MIDI");
                assign(a,new MidiDriverBinding(event.device(),name,event.channel(),event.number(),event.type()==0xb0,new GLDriverMapping(min,max,false,false,false),false,64));cancelLearn();
                // The learning gesture assigns only; it does not unexpectedly change a parameter.
                continue;
            }
            for(var entry:bindings().entrySet()){
                var a=entry.getKey();var b=entry.getValue();if(!b.device().equals(event.device()))continue;
                var rt=runtime.compute(a,(key,old)->old!=null&&old.binding.equals(b)?old:new Runtime(b,new GLDriverMapping.Pickup(),new boolean[1]));
                var target=target(a);if(target==null)continue;
                if(event.message()==255){rt.pickup.reset();rt.high[0]=false;if(b.hold()&&target.kind()==GLDriverTarget.Kind.TOGGLE&&!blocked(a))target.set(0);continue;}
                if(event.channel()!=b.channel()||event.number()!=b.number())continue;
                if(blocked(a)){rt.pickup.reset();rt.high[0]=false;continue;}
                if(b.cc()&&event.type()==0xb0){
                    double t=event.value()/127.;
                    if(target.kind()==GLDriverTarget.Kind.NUMBER){if(rt.pickup.accepts(b.mapping(),t,target.read())){target.set(b.mapping().map(t));rt.pickup.output(target.read());}}
                    else if(target.kind()==GLDriverTarget.Kind.CHOICE)target.normalized(t);
                    else {boolean high=event.value()>=b.threshold();if(target.kind()==GLDriverTarget.Kind.TOGGLE)target.set(high?1:0);else if(high&&!rt.high[0])target.pulse();rt.high[0]=high;}
                }else if(!b.cc()){
                    if(event.on()){if(b.hold()&&target.kind()==GLDriverTarget.Kind.TOGGLE)target.set(1);else target.pulse();}
                    else if(event.off()&&b.hold()&&target.kind()==GLDriverTarget.Kind.TOGGLE)target.set(0);
                }
            }
        }
        refreshIndicators();
    }
    private boolean coalescible(MidiDriverInput.Event event){
        for(var entry:bindings().entrySet()){var b=entry.getValue();if(!b.cc()||!b.device().equals(event.device())||b.channel()!=event.channel()||b.number()!=event.number())continue;
            var target=target(entry.getKey());if(b.mapping().pickup()||target==null||target.kind()!=GLDriverTarget.Kind.NUMBER&&target.kind()!=GLDriverTarget.Kind.CHOICE)return false;
        }return true;
    }
    Map<Address,MidiDriverBinding> bindings(){
        var result=new LinkedHashMap<Address,MidiDriverBinding>();
        owner.driverGraphs().forEach((asset,graph)->graph.nodes().forEach(node->node.properties().forEach((key,value)->{
            if(key.startsWith(GLDriverTarget.PROPERTY_PREFIX))result.put(new Address(asset,node.id(),MidiDriverBinding.property(key)),MidiDriverBinding.read(value));
        })));return result;
    }
    void refresh(){
        if(closed)return;
        var bindings=bindings();runtime.keySet().retainAll(bindings.keySet());
        var nextSuspended=bindings.keySet().stream().filter(this::blocked).collect(java.util.stream.Collectors.toSet());
        for(var entry:runtime.entrySet())if(nextSuspended.contains(entry.getKey())||suspended.contains(entry.getKey())){entry.getValue().pickup.reset();entry.getValue().high[0]=false;}
        suspended=Set.copyOf(nextSuspended);
        MidiDriverInput.request(this,bindings.values().stream().map(MidiDriverBinding::device).collect(java.util.stream.Collectors.toSet()),learning!=null);refreshIndicators();
    }
    private void refreshIndicators(){controls.removeIf(ref->{var c=ref.get();if(c==null)return true;var a=addresses.get(c);c.setDriverIndicator(a!=null&&binding(a)!=null,"easyknack.signal.MIDI");return false;});}
    void manualEdit(String asset,GLNodeId node,String property){external.remove(new Address(asset,node,property));}
    void manualGraphChange(GLNodeGraphSnapshot before,GLNodeGraphSnapshot after){
        var graphs=owner.driverGraphs();external.keySet().removeIf(a->{
            var graph=graphs.get(a.asset());if(graph==null||!graph.graphId().equals(after.graphId()))return false;
            var old=before.findNode(a.node()).orElse(null);var next=after.findNode(a.node()).orElse(null);
            return old!=null&&next!=null&&(!Objects.equals(old.properties().get(a.property()),next.properties().get(a.property()))||old.isPropertyAutomatic(a.property())!=next.isPropertyAutomatic(a.property()));
        });
    }
    private void restoreExternal(){
        if(restoring||closed)return;restoring=true;
        try{for(var entry:List.copyOf(external.entrySet()))if(spec(entry.getKey())!=null&&node(entry.getKey())!=null){var a=entry.getKey();owner.driverWrite(a.asset(),a.node(),a.property(),entry.getValue());}}
        finally{restoring=false;}
    }
    void documentChanged(){external.clear();runtime.clear();pending.clear();cancelLearn();refresh();}
    @Override public void close(){closed=true;timer.stop();cancelLearn();owner.history().removeListener(historyListener);owners.remove(owner.history());MidiDriverInput.release(this);try{subscription.close();}catch(Exception ignored){}}
}
