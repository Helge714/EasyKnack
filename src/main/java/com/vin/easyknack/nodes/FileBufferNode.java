package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.resource.filebuffer",title="File Buffer",category=NodeCategory.BUFFER,kind=NodeKind.AUDIO)
public final class FileBufferNode extends BasicNode {
 @Override public com.vin.easyknack.ui.BasicNodeView createView(com.vin.easyknack.ui.NodeViewContext context,com.vingame.glswing.node.model.GLGraphNode model){return new com.vin.easyknack.ui.FileBufferView(this,context,model);}
 public static final PropertyId FILE=new PropertyId("file"),START=new PropertyId("start"),END=new PropertyId("end");
 private static final double SAMPLE=1.0/48000;
 static double duration(NodeState s){return com.vin.easyknack.nodes.FilePositionEditor.of(0,s.text(FILE,"")).seconds();}
 private static boolean hasFile(NodeState s){return !com.vin.easyknack.audio.AudioFileDecoder.nativePath(s.text(FILE,"")).equals("none");}
 @Override public NodeState normalizeState(NodeState s){
  double length=duration(s),last=s.number(END,0);last=last==0?length:Math.max(SAMPLE,Math.min(length,last));
  double first=Math.max(0,Math.min(s.number(START,0),last-SAMPLE));
  var values=new java.util.HashMap<>(s.values());values.put(START.value(),com.vingame.glswing.node.model.GLNodeValue.of(first));values.put(END.value(),com.vingame.glswing.node.model.GLNodeValue.of(last));return new NodeState(values,s.connectedInputs());
 }
 @Override public String encode(NodeState s,java.util.function.ToIntFunction<PropertyId> input){return super.encode(normalizeState(s),input);}
 public void define(NodeBuilder n,NodeState s){
  n.addOutput(new PropertyId("history"),"Buffer").dataType(PortDataType.HISTORY).signal(SignalCategory.HISTORY).rate(ProcessingRate.CONTROL);
  n.addOutput(new PropertyId("duration"),"Duration (s)").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
  n.addOutput(new PropertyId("samples"),"Samples").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
  n.addEditor(FILE,"File",new com.vin.easyknack.ui.AudioFileEditor()).keepVisibleOnHide();
  n.addEditor(new PropertyId("snap-zero"),"Snap to zero",new Editor.CheckBox(false));
  n.addEditor(new PropertyId("waveform"),"Waveform",new com.vin.easyknack.ui.FileWaveformEditor());
  double length=duration(s),last=s.number(END,length),first=s.number(START,0);
  n.addEditor(START,"Start",(hasFile(s)?Editor.Slider.bar(0,0,Math.max(SAMPLE,last-SAMPLE)):Editor.Slider.button(0,0.0,null)).step(SAMPLE).formats(UnitFormats.TIME_SECONDS)).enabled(hasFile(s)&&last>SAMPLE);
  n.addEditor(END,"End",(hasFile(s)?Editor.Slider.bar(length,Math.min(length-SAMPLE,first+SAMPLE),length):Editor.Slider.button(0,0.0,null)).step(SAMPLE).formats(UnitFormats.TIME_SECONDS)).enabled(hasFile(s)&&first+SAMPLE<length);
  n.addEditor(new PropertyId("memory"),"Memory",new com.vin.easyknack.ui.FileBufferMemoryEditor(s.text(FILE,""),s.number(START,0),s.number(END,0)));
 }
 @Override public void updateEditor(PropertyId id,com.vingame.glswing.component.GLContainer control,NodeState state){state=normalizeState(state);if(control instanceof com.vin.easyknack.ui.FileWaveformEditor.Plot plot){plot.setSnapping(state.flag("snap-zero",false));plot.configure(state.text(FILE,""),state.number(START,0),state.number(END,0),duration(state));}if((id.equals(START)||id.equals(END))&&control instanceof com.vingame.glswing.ui.GLUnitSlider<?> slider)slider.setStoredValue(state.number(id,0));if(id.equals(new PropertyId("memory"))&&control instanceof com.vingame.glswing.ui.GLLabel label)label.setText(new com.vin.easyknack.ui.FileBufferMemoryEditor(state.text(FILE,""),state.number(START,0),state.number(END,0)).text(NodeText.text("easyknack.resource.filebuffer.memory","Memory")));}
 @Override public java.util.Map<String,com.vingame.glswing.node.model.GLNodeValue> defaults(){var values=new java.util.HashMap<>(super.defaults());values.put(END.value(),com.vingame.glswing.node.model.GLNodeValue.of(0.0));return java.util.Map.copyOf(values);}
 public NativeProcessor processor(){return new NativeProcessor("filebuffer","nodes/FileBufferNode.hpp",ParameterContract.builder().mapped(FILE,s->com.vin.easyknack.audio.AudioFileDecoder.nativePath(s.text(FILE,""))).value(START).value(END).build());}
}
