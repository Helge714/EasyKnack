package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.audio.recorder",title="Audio Recorder",category=NodeCategory.OUTPUT,kind=NodeKind.AUDIO)
public final class AudioRecorderNode extends BasicNode {
 public static final PropertyId FILE=new PropertyId("file"),CHANNELS=new PropertyId("channels"),OVERWRITE=new PropertyId("overwrite"),DIGITS=new PropertyId("digits"),AUDIO=new PropertyId("audio-in"),START=new PropertyId("start"),STOP=new PropertyId("stop"),STATUS=new PropertyId("status");
 public void define(NodeBuilder n,NodeState s){
  n.addEditor(FILE,"Recording file",new com.vin.easyknack.ui.RecordingFileEditor()).editorCopyable(false).keepVisibleOnHide();
  n.addEditor(STATUS,"Recording status",new com.vin.easyknack.ui.RecordingStatusEditor()).editorCopyable(false).keepVisibleOnHide();
  n.addEditor(CHANNELS,"Channels",new Editor.ComboBox<String>(java.util.List.of("Mono","Stereo"),"Stereo",v->v));
  n.addEditor(OVERWRITE,"Overwrite",new Editor.CheckBox(false));
  n.addEditor(DIGITS,"Index digits",Editor.Slider.button(4,1.,12.).integers().discrete()).visible(!s.flag(OVERWRITE,false));
  n.addInput(AUDIO,"Audio In");
  n.addInput(START,"Start",new Editor.Trigger()).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER).rate(ProcessingRate.EVENT);
  n.addInput(STOP,"Stop",new Editor.Trigger()).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER).rate(ProcessingRate.EVENT);
 }
 public NativeProcessor processor(){return new NativeProcessor("audiorecorder","nodes/AudioRecorderNode.hpp",ParameterContract.builder()
  .source(AUDIO).source(START).source(STOP)
  .mapped(FILE,s->{var path=s.text(FILE,"");return path.isBlank()?"none":java.util.HexFormat.of().formatHex(path.getBytes(java.nio.charset.StandardCharsets.UTF_8));})
  .mapped(CHANNELS,s->s.text(CHANNELS,"Stereo").equals("Mono")?"1":"2").value(OVERWRITE).integer(DIGITS).build());}
}
