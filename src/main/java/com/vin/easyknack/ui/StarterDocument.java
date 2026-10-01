package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.document.*;
final class StarterDocument {
    static GLNodeGraphDocument create(){
        var osc=new OscillatorNode();var out=new OutputNode();var a=GLNodeId.random();var b=GLNodeId.random();
        var graph=new GLNodeGraph();graph.edit("New",e->{
            e.addNode(new GLGraphNode(a,osc.registration().id(),osc.defaults()));
            e.addNode(new GLGraphNode(b,out.registration().id(),out.defaults()));
            e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a,"audio-out"),new GLNodeEndpoint(b,"audio-in")));
        });
        return new GLNodeGraphDocument(new GLNodeGraphAsset("easyknack.root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),new GLNodeEditorViewState(Map.of(a,new GLNodeViewState(70,140),b,new GLNodeViewState(430,140)))));
    }
}
