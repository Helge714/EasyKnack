package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.composite.*;

class GraphPresentationCacheTest {
    @Test void scalarEditsReevaluateOneSignatureAndKeepTheSemanticKey(){
        var graph=new GLNodeGraph();var changed=GLNodeId.random();var calls=new AtomicInteger();
        graph.edit("Scene",e->{e.addNode(new GLGraphNode(changed,"easyknack.math",Map.of()));
            for(int i=1;i<1000;i++)e.addNode(new GLGraphNode(GLNodeId.random(),"easyknack.math",Map.of()));});
        var cache=new WorkspaceGraphKey(n->{calls.incrementAndGet();return GraphDiagnostics.nodeKey(n);});
        var snapshot=graph.snapshot();var first=cache.update(snapshot,snapshot,List.of());assertEquals(1000,calls.get());
        for(int i=0;i<100;i++){
            final int value=i;graph.edit("Value",e->e.setProperty(changed,"a",GLNodeValue.of(value)));
            snapshot=graph.snapshot();assertSame(first,cache.update(snapshot,snapshot,List.of()));
        }
        assertEquals(1100,calls.get(),"Only the edited node needs another semantic signature");
        graph.edit("Mute",e->e.setProperty(changed,GLNodeState.MUTED,GLNodeValue.of(true)));
        snapshot=graph.snapshot();var muted=cache.update(snapshot,snapshot,List.of());assertNotSame(first,muted);
        graph.edit("Rate",e->e.setProperty(changed,"rate",GLNodeValue.of("control")));
        snapshot=graph.snapshot();assertNotSame(muted,cache.update(snapshot,snapshot,List.of()));
    }
    @Test void wiringDelayLengthNestedAssetsAndMetadataInvalidate(){
        var graph=new GLNodeGraph();var a=GLNodeId.random();var b=GLNodeId.random();
        graph.edit("Nodes",e->{e.addNode(new GLGraphNode(a,"easyknack.audio.delay",Map.of("time",GLNodeValue.of(.1))));e.addNode(new GLGraphNode(b,"easyknack.math",Map.of()));});
        var cache=new WorkspaceGraphKey(GraphDiagnostics::nodeKey);var s=graph.snapshot();var key=cache.update(s,s,List.of());
        graph.edit("Delay",e->e.setProperty(a,"time",GLNodeValue.of(.00001)));s=graph.snapshot();var next=cache.update(s,s,List.of());assertNotSame(key,next);key=next;
        graph.edit("Wire",e->e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a,"audio-out"),new GLNodeEndpoint(b,"a"))));
        s=graph.snapshot();next=cache.update(s,s,List.of());assertNotSame(key,next);
        var child=new GLNodeGraphAsset("child",1,GLNodeGraphAssetKind.COMPOSITE,new GLNodeGraph().snapshot(),GLNodeEditorViewState.empty());
        key=cache.update(s,s,List.of(child));assertNotSame(next,key);
        var updated=new GLNodeGraphAsset(child.id(),child.version(),child.kind(),child.graph(),child.viewState(),Map.of("routing",GLNodeValue.of("new")),child.compositeDescriptor());
        next=cache.update(s,s,List.of(updated));assertNotSame(key,next);assertNotSame(next,cache.update(s,s,List.of()));
    }
    @Test void scalarChangesSkipDiagnosticCollectionButMuteAndModeChangesDoNot()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
            var graph=controller.workspace().currentGraph();var definition=new ValueNode();var id=GLNodeId.random();
            graph.edit("Value",e->e.addNode(new GLGraphNode(id,definition.registration().id(),definition.defaults())));
            var diagnostics=new WorkspaceGraphDiagnostics();var composites=new GLCompositeGraphRegistry();var types=new GLNodeTypeRegistry();
            new NodeRegistry(types,new GLNodeCatalog());var calls=new AtomicInteger();
            java.util.function.Supplier<GLCompositeFlattenResult> flattened=()->{calls.incrementAndGet();return new GLCompositeFlattenResult(Optional.of(graph.snapshot()),Map.of(),List.of());};
            diagnostics.refresh(controller.workspace(),graph,composites,types,flattened,false,false,Map.of());assertEquals(1,calls.get());
            graph.edit("Parameter",e->e.setProperty(id,"value",GLNodeValue.of(.75)));
            diagnostics.refresh(controller.workspace(),graph,composites,types,flattened,false,false,Map.of());assertEquals(1,calls.get());
            graph.edit("Mute",e->e.setProperty(id,GLNodeState.MUTED,GLNodeValue.of(true)));
            diagnostics.refresh(controller.workspace(),graph,composites,types,flattened,false,false,Map.of());assertEquals(2,calls.get());
            diagnostics.refresh(controller.workspace(),graph,composites,types,flattened,true,false,Map.of());assertEquals(3,calls.get());
        }});
    }
}
