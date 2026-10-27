package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.node.GLNode;
import com.vingame.glswing.node.binding.GLNodeViewFactory;
import com.vingame.glswing.node.model.*;

/** Core adapter only. Definitions own view creation and customization. */
final class AudioNodeFactory implements GLNodeViewFactory {
    @FunctionalInterface interface PropertyWriter extends NodeViewContext.PropertyWriter { }
    private final NodeViewContext context;
    private final Runnable observe;
    private final java.util.function.Function<String,BasicNode> definitions;
    AudioNodeFactory(PropertyWriter writer,GLHistoryManager history,Runnable observe) {
        this(writer,history,observe,NodeRegistry::require);
    }
    AudioNodeFactory(PropertyWriter writer,GLHistoryManager history,Runnable observe,java.util.function.Function<String,BasicNode> definitions) {
        context=new NodeViewContext(writer,history); this.observe=observe; this.definitions=definitions;
    }
    @Override public GLNode createNode(GLGraphNode model,GLNodeTypeDescriptor type) {
        observe.run();return definitions.apply(model.typeId()).createView(context,model);
    }
    @Override public void transferViewState(GLNode previous,GLNode view,GLGraphNode model,GLNodeTypeDescriptor type){
        for(var control:view.getContents())for(var old:previous.getContents()){
            if(control instanceof ValueViewerPanel panel&&old instanceof ValueViewerPanel prior)panel.adoptViewState(prior,new NodeState(model.properties()));
            if(control instanceof MeasurementRecorderPanel panel&&old instanceof MeasurementRecorderPanel prior)panel.adoptViewState(prior,new NodeState(model.properties()));
        }
    }
    @Override public void updateNode(GLNode view,GLGraphNode model,GLNodeTypeDescriptor type) { ((BasicNodeView)view).update(model); }
    @Override public boolean isViewCurrent(GLNode view,GLGraphNode model,GLNodeTypeDescriptor type) {
        return ((BasicNodeView)view).isCurrent(model,type);
    }
    static Optional<GLNodeId> idOf(GLNode view) { return view instanceof BasicNodeView basic?Optional.of(basic.nodeId()):Optional.empty(); }

}

