package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.audio.file",title="Audio File",category=NodeCategory.IO,kind=NodeKind.AUDIO)
public final class AudioFileNode extends BasicNode {
    public static final PropertyId SPEED=new PropertyId("speed");
    @Override public com.vin.easyknack.ui.BasicNodeView createView(com.vin.easyknack.ui.NodeViewContext context,com.vingame.glswing.node.model.GLGraphNode model){return new com.vin.easyknack.ui.AudioFileView(this,context,model);}
    public static final PropertyId FILE=new PropertyId("file"),PLAY=new PropertyId("play"),RESET=new PropertyId("reset"),POSITION=new PropertyId("position"),LOOP=new PropertyId("loop"),START=new PropertyId("loop-start"),END=new PropertyId("loop-end");
    @Override public void define(NodeBuilder node,NodeState state){
        node.addOutput(new PropertyId("audio-out"),"Audio Out");
        node.addOutput(new PropertyId("read-position"),"Position").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        node.addOutput(new PropertyId("duration"),"Duration (s)").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        node.addOutput(new PropertyId("underruns"),"Buffer underruns").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        node.addEditor(FILE,"File",new com.vin.easyknack.ui.AudioFileEditor()).keepVisibleOnHide();
        node.addInput(PLAY,"Play / Pause",new Editor.Toggle(false)).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        node.addInput(RESET,"Reset",new Editor.Trigger()).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER).rate(ProcessingRate.EVENT);
        node.addInput(POSITION,"Seek position",FilePositionEditor.of(0,state.text(FILE,""))).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        node.addInput(SPEED,"Speed",Editor.Slider.button(1,0.0,4.0).step(.01)).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        node.addEditor(LOOP,"Loop",new Editor.CheckBox(false));
        node.addEditor(START,"Loop start",FilePositionEditor.of(0,state.text(FILE,"")));
        node.addEditor(END,"Loop end",FilePositionEditor.of(1,state.text(FILE,"")));
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("audiofile","nodes/AudioFileNode.hpp",ParameterContract.builder()
        .mapped(FILE,s->com.vin.easyknack.audio.AudioFileDecoder.nativePath(s.text(FILE,"")))
        .source(PLAY).value(PLAY).source(RESET).source(POSITION).value(POSITION).source(SPEED).value(SPEED).value(LOOP).value(START).value(END).build());}
}
