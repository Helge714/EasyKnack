package com.vin.easyknack.graph;
import com.vin.easyknack.nodes.NodeRegistry;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.execution.GLMutedGraphResolver;
import com.vingame.glswing.node.composite.GLCompositeGraphRegistry;
/** Audio-specific mute and feedback preparation, independent of views and controllers. */
public final class GraphExecutionPolicy {
    private GraphExecutionPolicy(){}
    /** Keep leaf nodes until public group ports have been expanded onto their endpoints. */
    public static GLNodeGraphSnapshot compositeMutes(GLNodeGraphSnapshot snapshot,GLCompositeGraphRegistry composites,GLNodeTypeRegistry types) {
        var leafMutes=new java.util.HashSet<GLNodeId>();
        var masked=new GLNodeGraphSnapshot(snapshot.graphId(),snapshot.revision(),snapshot.nodes().stream().map(n->{
            var clean=n.withoutProperty(GLNodeState.HIDDEN);
            if(composites.findComposite(n.typeId()).isEmpty()){
                if(GLNodeState.isMuted(n))leafMutes.add(n.id());
                return clean.withoutProperty(GLNodeState.MUTED);
            }
            return GLNodeState.isMuted(n)?clean:clean.withoutProperty(GLNodeState.MUTED);
        }).toList(),snapshot.connections());
        var resolved=GLMutedGraphResolver.resolve(masked,types);
        return new GLNodeGraphSnapshot(resolved.graphId(),resolved.revision(),resolved.nodes().stream()
            .map(n->leafMutes.contains(n.id())?n.withProperty(GLNodeState.MUTED,com.vingame.glswing.node.model.GLNodeValue.of(true)):n).toList(),resolved.connections());
    }

    public static GLNodeGraphSnapshot executable(GLNodeGraphSnapshot snapshot,GLNodeTypeRegistry types) {
        var muteable = new GLNodeGraphSnapshot(snapshot.graphId(), snapshot.revision(), snapshot.nodes().stream()
                // Keep an explicit zero source connected. Disconnecting would activate downstream slider defaults.
                .map(n -> GLNodeState.isMuted(n) && NodeRegistry.find(n.typeId()).orElse(null) instanceof com.vin.easyknack.nodes.InputNode
                        ? n.withType(new com.vin.easyknack.nodes.MathNode().registration().id(),n.typeVersion())
                            .withoutProperty(GLNodeState.MUTED)
                            .withProperty(com.vin.easyknack.nodes.MathNode.A.value(),com.vingame.glswing.node.model.GLNodeValue.of(0.0))
                            .withProperty(com.vin.easyknack.nodes.MathNode.B.value(),com.vingame.glswing.node.model.GLNodeValue.of(0.0)) : n)
                .map(n -> NodeRegistry.find(n.typeId()).map(def -> def.isMuteable() ? n : n.withoutProperty(GLNodeState.MUTED)).orElse(n))
                .toList(), snapshot.connections());
        var resolved = com.vin.easyknack.audio.FeedbackConnections.markDelayBypasses(muteable, GLMutedGraphResolver.resolve(muteable, types), types);
        return new GLNodeGraphSnapshot(resolved.graphId(), resolved.revision(), resolved.nodes().stream()
                .map(node -> node.withoutProperty(GLNodeState.HIDDEN).withoutProperty(GLNodeState.MUTED)).toList(),
                resolved.connections());
    }

}
