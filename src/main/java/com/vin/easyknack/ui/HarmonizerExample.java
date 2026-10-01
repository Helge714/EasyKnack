package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;

/** Editable reference patch; the group can be copied into any document or exported. */
final class HarmonizerExample {
    private final GLNodeGraph graph=new GLNodeGraph();
    private final Map<GLNodeId,GLNodeViewState> views=new LinkedHashMap<>();
    private GLNodeId add(BasicNode node,int x,int y,Object... properties){
        var values=new HashMap<>(node.defaults());
        for(int i=0;i<properties.length;i+=2)values.put((String)properties[i],value(properties[i+1]));
        var id=GLNodeId.random();graph.edit("Example",e->e.addNode(new GLGraphNode(id,node.registration().id(),values)));
        views.put(id,new GLNodeViewState(x,y));return id;
    }
    private static GLNodeValue value(Object v){return v instanceof String s?GLNodeValue.of(s):v instanceof Boolean b?GLNodeValue.of(b):GLNodeValue.of(((Number)v).doubleValue());}
    private void wire(GLNodeId from,String output,GLNodeId to,String input){graph.edit("Example",e->e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(from,output),new GLNodeEndpoint(to,input))));}
    private static GLNodeEndpoint endpoint(GLNodeId node,String port){return new GLNodeEndpoint(node,port);}
    private static GLCompositePortMapping slider(String name,double initial,double min,double max,UnitFormat unit,GLNodeEndpoint... targets){
        var options=new HashMap<String,GLNodeValue>();
        options.put("default",GLNodeValue.of(initial));options.put("minimum",GLNodeValue.of(min));options.put("maximum",GLNodeValue.of(max));
        options.put("presentation",GLNodeValue.of(max==1?"bar":"button"));
        if(name.equals("Scale"))options.put("numericType",GLNodeValue.of("integer"));
        options.put(GroupUnits.BASE,GLNodeValue.of(unit.id()));
        if(name.equals("Hysteresis"))options.put(GroupUnits.ALLOWED,GLNodeValue.of(List.of(UnitFormat.SEMITONE.id(),UnitFormat.CENT.id())));
        var presentation=new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,options);
        return new GLCompositePortMapping(GLNodePortDescriptor.input(name,SignalCategory.VALUE.valueTypeId(),false,1),targets[0],presentation,List.of(targets).subList(1,targets.length));
    }
    static GLNodeGraphDocument create(){return new HarmonizerExample().build();}
    private GLNodeGraphDocument build(){
        var fft=add(new SpectrumNode(),0,0,"size","4096","window","rectangular","padding","double","remove-dc",true,"overlap",.75,"channel","mix");
        var correlation=add(new SpectrumCorrelationNode(),320,0,"normalization","unbiased");
        var pitch=add(new PitchDetectionNode(),640,0,"minimum",60,"maximum",5000);
        var hold=add(new PitchHoldNode(),960,0);
        var scale=add(new ScaleQuantizerNode(),1280,0,"mode","fixed");
        var monitor=add(new PitchMonitorNode(),1600,0);
        
        
        var ratio=add(new MathNode(),1920,0,"operation","divide");
        var strength=add(new MathNode(),2240,0,"operation","power","b",1);
        var slew=add(new SlewNode(),2560,0);
        var buffer=add(new HistoryBufferNode(),0,600,"seconds",8192.0/48000);
        var reader=add(new HistoryReaderNode(),2880,0,"offset",.25);
        var follower=add(new PositionReaderNode(),3200,0,"offset",.5);
        var crossfade=add(new MixNode(),3520,0);
        var dryWet=add(new MixNode(),3840,0,"factor",1);
        wire(fft,"analysis",correlation,"spectrum");wire(correlation,"analysis",pitch,"analysis");
        wire(pitch,"frequency",hold,"frequency");wire(pitch,"confidence",hold,"confidence");
        wire(hold,"out",scale,"frequency");wire(scale,"out",monitor,"target");wire(monitor,"out",ratio,"a");wire(hold,"out",ratio,"b");
        wire(pitch,"frequency",monitor,"frequency");wire(pitch,"confidence",monitor,"confidence");
        wire(ratio,"audio-out",strength,"a");wire(strength,"audio-out",slew,"in");
        wire(slew,"out",reader,"speed");wire(buffer,"history",reader,"history");wire(buffer,"history",follower,"history");
        wire(reader,"read-position",follower,"position");
        wire(reader,"audio-out",crossfade,"a");wire(follower,"audio-out",crossfade,"b");wire(follower,"distance",crossfade,"factor");
        wire(crossfade,"audio-out",dryWet,"b");
        var in=new GLCompositePortMapping(GLNodePortDescriptor.input("In",SignalCategory.AUDIO.valueTypeId(),false,1),endpoint(buffer,"audio-in"))
            .withEndpoints(List.of(endpoint(buffer,"audio-in"),endpoint(fft,"audio-in"),endpoint(dryWet,"a")));
        var ports=List.of(new GLCompositePortMapping(GLNodePortDescriptor.output("Out",SignalCategory.AUDIO.valueTypeId()),endpoint(dryWet,"audio-out")),in,
            slider("Target",440,20,20000,UnitFormat.HZ,endpoint(scale,"target")),
            slider("Strength",1,0,1,UnitFormat.FACTOR,endpoint(strength,"b")),
            slider("Glide",.03,0,2,UnitFormat.SECOND,endpoint(slew,"rise"),endpoint(slew,"fall")),
            slider("Confidence",.8,0,1,UnitFormat.FACTOR,endpoint(hold,"threshold")),
            slider("Mix",1,0,1,UnitFormat.FACTOR,endpoint(dryWet,"factor")),
            slider("Hysteresis",.1,0,12,UnitFormat.SEMITONE,endpoint(scale,"hysteresis")));
        var descriptor=new GLCompositeNodeDescriptor("easyknack.group.harmonizer","easyknack.group.harmonizer.asset",1,ports);
        var metadata=Map.of(PitchReadoutEditor.GROUP_MONITOR,GLNodeValue.of(monitor.toString()),"easyknack.title",GLNodeValue.of("Harmonizer"),"easyknack.category",GLNodeValue.of(NodeCategory.EFFECT.path()),"easyknack.kind",GLNodeValue.of("AUDIO"),
            GroupRouting.INSERT_IN,GLNodeValue.of("In"),GroupRouting.INSERT_OUT,GLNodeValue.of("Out"),GroupRouting.MUTE_IN,GLNodeValue.of("In"),GroupRouting.MUTE_OUT,GLNodeValue.of("Out"));
        var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),new GLNodeEditorViewState(views),metadata,descriptor);
        var scaleNode=graph.snapshot().findNode(scale).orElseThrow();
        for(var pair:List.of(new String[]{"mode","Mode"},new String[]{"root","Root"},new String[]{"scale","Scale"}))
            asset=GroupChoices.expose(asset,new GroupChoices.Candidate(scale,pair[0],pair[1],GroupChoices.nodeEditor(scaleNode,pair[0])),pair[1]);
        var root=new HarmonizerExample();var source=root.add(new OscillatorNode(),50,100,"frequency",660);
        var group=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());root.graph.edit("Example",e->e.addNode(group));root.views.put(group.id(),new GLNodeViewState(380,100));
        var out=root.add(new OutputNode(),720,100);
        root.wire(source,"audio-out",group.id(),"In");root.wire(group.id(),"Out",out,"audio-in");
        return new GLNodeGraphDocument(new GLNodeGraphAsset("harmonizer-example",1,GLNodeGraphAssetKind.PROJECT,root.graph.snapshot(),new GLNodeEditorViewState(root.views)),Map.of(asset.id(),asset),List.of());
    }
}
