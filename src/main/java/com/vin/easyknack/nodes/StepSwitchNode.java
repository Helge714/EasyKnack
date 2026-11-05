package com.vin.easyknack.nodes;

import java.util.*;
import com.vingame.glswing.node.model.GLNodeValue;

@RegisterNode(id="easyknack.routing.stepswitch",title="Step Switch",category=NodeCategory.ROUTING,kind=NodeKind.VALUE)
public final class StepSwitchNode extends BasicNode {
    public static final int MAX_STEPS=32;
    public static final PropertyId STEPS=new PropertyId("steps"),NEXT=new PropertyId("next"),RESET=new PropertyId("reset"),
        OUT=new PropertyId("value"),INDEX=new PropertyId("index"),RATE=new PropertyId("rate"),MODE=new PropertyId("mode"),TYPE=new PropertyId("type"),SELECTION=new PropertyId("easyknack.stepswitch.selection");
    public enum Type {
        VALUE(PortDataType.SIGNAL,SignalCategory.VALUE), VECTOR(PortDataType.VECTOR,SignalCategory.VECTOR),
        WAVETABLE(PortDataType.WAVEFORM,SignalCategory.WAVETABLE), BUFFER(PortDataType.HISTORY,SignalCategory.HISTORY),
        SPECTRUM(PortDataType.SPECTRUM,SignalCategory.SPECTRUM), ANALYSIS(PortDataType.ANALYSIS,SignalCategory.ANALYSIS);
        public final PortDataType dataType;public final SignalCategory signal;
        Type(PortDataType dataType,SignalCategory signal){this.dataType=dataType;this.signal=signal;}
        public String id(){return name().toLowerCase(Locale.ROOT);}
        @Override public String toString(){return NodeText.text("stepswitch.type."+id(),name());}
    }
    public static Type type(NodeState s){return Type.valueOf(s.text(TYPE,"value").toUpperCase(Locale.ROOT));}
    public static int selected(NodeState s){int index=(int)selectionNumber(s,"index");return index>=0&&index<count(s)?index:0;}
    public static long revision(NodeState s){return Math.min(9007199254740991L,Math.max(0,(long)selectionNumber(s,"revision")));}
    private static long lastRequest;
    public static synchronized long nextRevision(NodeState state){lastRequest=Math.max(lastRequest,revision(state));if(lastRequest>=9007199254740991L)lastRequest=0;return ++lastRequest;}
    private static double selectionNumber(NodeState s,String key){var value=s.values().get(SELECTION.value());if(value!=null&&value.toJavaValue() instanceof Map<?,?> map&&map.get(key) instanceof Number number&&Double.isFinite(number.doubleValue()))return number.doubleValue();return 0;}
    public static GLNodeValue selection(int index,long revision){return GLNodeValue.of(Map.of("index",index,"revision",revision));}
    public enum Mode { TRIGGER, INDEX; public String id(){return name().toLowerCase(Locale.ROOT);} @Override public String toString(){return NodeText.text("easyknack.routing.stepswitch.mode."+id(),name());} }
    public static boolean indexed(NodeState s){return s.text(MODE,"trigger").equals("index");}
    public static PropertyId name(int index){return new PropertyId("step-name-"+index);}
    public static String label(NodeState s,int index){return s.text(name(index),"").isBlank()?NodeText.text("easyknack.routing.stepswitch.input","Input")+" "+(index+1):s.text(name(index),"");}
    public static PropertyId input(int index){return new PropertyId("step-"+index);}
    public static final PropertyId ORDER=new PropertyId("step-order");
    public static List<Integer> order(NodeState state){
        var result=new ArrayList<Integer>();var saved=state.values().get(ORDER.value());
        if(saved instanceof GLNodeValue.ListValue list)for(var item:list.values())if(item.toJavaValue() instanceof Number n&&n.doubleValue()==n.intValue()&&n.intValue()>=0&&n.intValue()<MAX_STEPS&&!result.contains(n.intValue()))result.add(n.intValue());
        for(int i=0;i<MAX_STEPS;i++)if(!result.contains(i))result.add(i);
        return result.stream().filter(i->i<count(state)).toList();
    }
    public static int count(NodeState s){return Math.max(1,Math.min(MAX_STEPS,(int)s.number(STEPS,4)));}
    @Override public void define(NodeBuilder n,NodeState s){
        var type=type(s);boolean numeric=type==Type.VALUE;
        var output=n.addOutput(OUT,"Value").dataType(type.dataType).signal(type.signal);
        if(!numeric)output.textKey("stepswitch.selected");
        if(numeric||type==Type.VECTOR)output.rates(PortRates.AUDIO_CONTROL);else output.rate(ProcessingRate.CONTROL);
        var indexOutput=n.addOutput(INDEX,"Index").signal(SignalCategory.VALUE).tooltip(com.vin.easyknack.ui.AppText.text("stepswitch.index.help"));
        if(numeric)indexOutput.rates(PortRates.AUDIO_CONTROL);else indexOutput.rate(ProcessingRate.CONTROL);
        n.addEditor(TYPE,"Data type",Editor.comboBox(List.of(Type.values()),Type.VALUE,Type::id)).inspectorOnly()
            .tooltip(com.vin.easyknack.ui.AppText.text("stepswitch.resource.help"));
        n.addEditor(STEPS,"Steps",Editor.Slider.button(4,1.,(double)MAX_STEPS).integers().discrete()).editorCopyable(false).keepVisibleOnHide()
            .tooltip("Number of inputs, 1–32. Removing the active step returns to the first input. Undo restores removed connections.");
        n.addEditor(MODE,"Selection",Editor.comboBox(List.of(Mode.values()),Mode.TRIGGER,Mode::id)).inspectorOnly();
        if(indexed(s))n.addInput(NEXT,"Selected index",Editor.Slider.button(0,0.,(double)MAX_STEPS-1).integers().discrete()).textKey("easyknack.routing.stepswitch.selectedIndex").rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE)
            .tooltip("Zero-based selection. Values are rounded down and clamped to the available steps.");
        else n.addInput(NEXT,"Next",new Editor.Trigger()).dataType(PortDataType.TRIGGER).rate(ProcessingRate.EVENT).signal(SignalCategory.TRIGGER)
            .tooltip("Each trigger advances one step and wraps at the end. Unconnected: click to advance at the next block.");
        if(!indexed(s))n.addInput(RESET,"Reset",new Editor.Trigger()).dataType(PortDataType.TRIGGER).rate(ProcessingRate.EVENT).signal(SignalCategory.TRIGGER)
            .tooltip("Select the first input. Reset wins over Next at the same sample.");
        for(int i:order(s)){
            var row=numeric?n.addInput(input(i),label(s,i),Editor.Slider.button(0)):n.addInput(input(i),label(s,i));
            row.editorCopyable(false).dataType(type.dataType).signal(type.signal).optional();
            if(numeric||type==Type.VECTOR)row.rates(PortRates.AUDIO_CONTROL);else row.rate(ProcessingRate.CONTROL);
        }
        for(int i=0;i<count(s);i++)n.addEditor(name(i),NodeText.text("easyknack.routing.stepswitch.stepName","Step name")+" "+(i+1),new Editor.Text("")).inspectorOnly();
        n.addEditor(ORDER,NodeText.text("stepswitch.order","Input order"),new StepOrderEditor(s)).inspectorOnly().editorCopyable(false);
        if(numeric||type==Type.VECTOR)ProcessingRate.addEditor(n,RATE).tooltip(numeric?"Auto uses AudioRate when a trigger or audio input is connected, otherwise ControlRate. ControlRate samples at block start and defers later triggers to the next block. Audio switching is immediate and can click.":com.vin.easyknack.ui.AppText.text("stepswitch.resource.help"));
    }
    @Override public Map<String,GLNodeValue> defaults(){
        var values=new HashMap<>(super.defaults());for(int i=0;i<MAX_STEPS;i++)values.putIfAbsent(input(i).value(),GLNodeValue.of(0.));values.put(NEXT.value(),GLNodeValue.of(0.));values.put(SELECTION.value(),selection(0,0));return Map.copyOf(values);
    }
    private static final ParameterContract CONTRACT=contract();
    private static ParameterContract contract(){
        var b=ParameterContract.builder().integer(STEPS).mapped(new PropertyId("initial-step"),s->Integer.toString(selected(s))).mapped(new PropertyId("selection-revision"),s->Long.toString(revision(s))).value(MODE).value(NEXT).source(NEXT).source(RESET,s->!indexed(s));
        for(int i=0;i<MAX_STEPS;i++){final int index=i;b.source(input(i),s->index<count(s)).value(input(i));}return b.mapped(TYPE,s->type(s).id()).build();
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("stepswitch","nodes/StepSwitchNode.hpp",CONTRACT);}
    @Override public ProcessingRate resolveAutomaticRate(NodeState s,Map<PropertyId,ProcessingRate> incoming){
        return !indexed(s)&&(incoming.containsKey(NEXT)||incoming.containsKey(RESET))?ProcessingRate.AUDIO:super.resolveAutomaticRate(s,incoming);
    }
    @Override public com.vin.easyknack.ui.BasicNodeView createView(com.vin.easyknack.ui.NodeViewContext context,com.vingame.glswing.node.model.GLGraphNode model){
        return new com.vin.easyknack.ui.StepSwitchView(this,context,model);
    }
    @Override public void updateEditor(PropertyId key,com.vingame.glswing.component.GLContainer control,NodeState state){if(key.equals(ORDER)&&control instanceof StepOrderEditor.Panel panel)panel.configure(state);}
    @Override public void mute(MuteRouting r,NodeState s){r.connect(input(0),OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(input(0),OUT);}
}
