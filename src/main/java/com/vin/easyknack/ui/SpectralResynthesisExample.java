package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Editable bin selection and additive synthesis; deliberately distinct from exact IFFT. */
final class SpectralResynthesisExample {
    private static GLGraphNode node(BasicNode definition){return new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults());}
    private static GLGraphConnection wire(GLGraphNode a,String out,GLGraphNode b,String in){return new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a.id(),out),new GLNodeEndpoint(b.id(),in));}
    static GLNodeGraphDocument create(){
        var asset=SpectralGroups.create();var inner=new GLNodeGraph(asset.graph());
        var context=asset.graph().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinContextNode).findFirst().orElseThrow();
        var spectrumOut=asset.graph().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinOutputNode).findFirst().orElseThrow();
        var read=node(new BinReadNode());var osc=node(new OscillatorNode());var compare=node(new ComparatorNode()).withProperty("b",GLNodeValue.of(.002));
        var discard=node(new BinDiscardNode());var combine=node(new InstanceCombineNode()).withProperty("mode",GLNodeValue.of("sum"));
        inner.edit("Additive bins",e->{
            for(var n:List.of(read,osc,compare,discard,combine))e.addNode(n);
            e.addConnection(wire(context,"index",read,"index"));e.addConnection(wire(read,"frequency",osc,"frequency"));
            e.addConnection(wire(read,"amplitude",compare,"a"));e.addConnection(wire(compare,"value-out",discard,"discard"));
            e.addConnection(wire(read,"amplitude",combine,"weight"));e.addConnection(wire(osc,"audio-out",combine,"in"));
        });
        var descriptor=asset.compositeDescriptor();var ports=new ArrayList<GLCompositePortMapping>();
        for(var p:descriptor.portMappings())ports.add(p.publicPort().direction()==GLPortDirection.INPUT
            ?new GLCompositePortMapping(p.publicPort(),p.internalEndpoint(),p.presentation(),List.of(new GLNodeEndpoint(read.id(),"spectrum"))):p);
        ports.add(new GLCompositePortMapping(GLNodePortDescriptor.output("audio",SignalCategory.AUDIO.valueTypeId()),new GLNodeEndpoint(combine.id(),"out")));
        var metadata=new HashMap<>(asset.metadata());metadata.put("easyknack.title",GLNodeValue.of("Spectral Resynthesis"));
        var positions=new LinkedHashMap<GLNodeId,GLNodeViewState>();
        positions.put(context.id(),new GLNodeViewState(160,100));positions.put(read.id(),new GLNodeViewState(460,100));positions.put(osc.id(),new GLNodeViewState(790,100));positions.put(combine.id(),new GLNodeViewState(1110,100));
        positions.put(compare.id(),new GLNodeViewState(790,450));positions.put(discard.id(),new GLNodeViewState(1110,450));positions.put(spectrumOut.id(),new GLNodeViewState(1430,450));
        asset=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),inner.snapshot(),new GLNodeEditorViewState(positions),metadata,new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),asset.id(),1,ports));
        var root=new GLNodeGraph();var source=node(new OscillatorNode());var fft=node(new FFTStreamNode());var group=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());var gain=node(new GainNode());var out=node(new OutputNode());
        var nodes=List.of(source,fft,group,gain,out);var views=new LinkedHashMap<GLNodeId,GLNodeViewState>();
        root.edit("Spectral resynthesis",e->{for(int i=0;i<nodes.size();i++){e.addNode(nodes.get(i));views.put(nodes.get(i).id(),new GLNodeViewState(40+i*310,120));}
            e.addConnection(wire(source,"audio-out",fft,"in"));e.addConnection(wire(fft,"spectrum",group,"spectrum"));e.addConnection(wire(group,"audio",gain,"audio-in"));e.addConnection(wire(gain,"audio-out",out,"audio-in"));});
        return new GLNodeGraphDocument(new GLNodeGraphAsset("spectral-resynthesis",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),new GLNodeEditorViewState(views)),Map.of(asset.id(),asset),List.of());
    }
}
