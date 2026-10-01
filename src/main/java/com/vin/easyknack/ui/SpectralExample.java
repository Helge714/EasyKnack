package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Audible, editable starting point: spectrum processing with identity bin mapping. */
final class SpectralExample {
    static GLNodeGraphDocument create(){
        var asset=SpectralGroups.create();var graph=new GLNodeGraph();var views=new LinkedHashMap<GLNodeId,GLNodeViewState>();
        var definitions=List.of(new OscillatorNode(),new FFTStreamNode(),new SpectrumShiftNode(),new IFFTNode(),new OutputNode());
        var nodes=new ArrayList<GLGraphNode>();
        for(var definition:definitions)nodes.add(new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults()));
        nodes.add(3,new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of()));
        String[] outputs={"audio-out","spectrum","out","out","out"};String[] inputs={"in","spectrum","spectrum","spectrum","audio-in"};
        graph.edit("Spectral example",e->{
            for(int i=0;i<nodes.size();i++){var node=nodes.get(i);e.addNode(node);views.put(node.id(),new GLNodeViewState(40+i*280,100));}
            for(int i=0;i<nodes.size()-1;i++)e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(nodes.get(i).id(),outputs[i]),new GLNodeEndpoint(nodes.get(i+1).id(),inputs[i])));
        });
        return new GLNodeGraphDocument(new GLNodeGraphAsset("spectral-example",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),new GLNodeEditorViewState(views)),Map.of(asset.id(),asset),List.of());
    }
}
