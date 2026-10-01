package com.vin.easyknack.ui;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.component.GLContainer;
public final class FileBufferView extends BasicNodeView {
 public FileBufferView(FileBufferNode node,NodeViewContext context,GLGraphNode model){super(node,context,model);}
 @Override protected GLContainer control(NodeBuilder.Row row){var control=super.control(row);if(control instanceof FileWaveformEditor.Plot plot)plot.onEdit((start,value)->write(start?FileBufferNode.START:FileBufferNode.END,GLNodeValue.of(value)));return control;}
}
