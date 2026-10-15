package com.vin.easyknack.nodes;
import com.vingame.glswing.node.model.*;
import com.vin.easyknack.ui.*;

@RegisterNode(id="easyknack.waveform.viewer",title="Wavetable Viewer",category=NodeCategory.ANALYSIS,kind=NodeKind.WAVETABLE)
public final class WavetableViewerNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("waveform"),GRAPH=new PropertyId("graph");
    @Override public com.vingame.glswing.node.GLNodeResizeMode resizeMode(){return com.vingame.glswing.node.GLNodeResizeMode.BOTH;}
    public void define(NodeBuilder n,NodeState s){
        n.addInput(IN,"Waveform").dataType(PortDataType.WAVEFORM).signal(SignalCategory.WAVETABLE).rate(ProcessingRate.CONTROL);
        n.addEditor(GRAPH,"Samples",new WavetableViewerEditor()).growVertically().keepVisibleOnHide().editorCopyable(false);
    }
    public NativeProcessor processor(){return new NativeProcessor("waveformviewer","nodes/WavetableViewerNode.hpp",ParameterContract.builder().source(IN).build());}
    @Override public BasicNodeView createView(NodeViewContext context,GLGraphNode model){
        return new BasicNodeView(this,context,model){
            @Override protected com.vingame.glswing.component.GLContainer control(NodeBuilder.Row row){
                var component=super.control(row);if(component instanceof WavetableViewerEditor.Plot plot)plot.bind(model.id().toString());return component;
            }
        };
    }
}
