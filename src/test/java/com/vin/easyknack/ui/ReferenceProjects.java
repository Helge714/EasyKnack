package com.vin.easyknack.ui;

import java.nio.charset.StandardCharsets;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Versioned, deterministic benchmark documents. Never reads or modifies a user document. */
final class ReferenceProjects {
    record Project(String name, GLNodeGraphDocument document, GLGraphNode group, boolean midi) { }
    static UUID uuid(String key) { return UUID.nameUUIDFromBytes(("easyknack.reference.v1/"+key).getBytes(StandardCharsets.UTF_8)); }
    static final class Graph {
        final String name;
        final GLNodeGraph graph;
        final Map<GLNodeId,GLNodeViewState> positions=new LinkedHashMap<>();
        Graph(String name){this.name=name.replace('/','.');graph=new GLNodeGraph(new GLNodeGraphId(uuid(this.name)));}
        GLNodeId node(String key,BasicNode definition,Object... pairs){
            var values=new LinkedHashMap<>(definition.defaults());
            for(int i=0;i<pairs.length;i+=2){Object v=pairs[i+1];values.put((String)pairs[i],v instanceof String s?GLNodeValue.of(s):v instanceof Boolean b?GLNodeValue.of(b):GLNodeValue.of(((Number)v).doubleValue()));}
            return node(key,definition.registration().id(),values);
        }
        GLNodeId node(String key,String type,Map<String,GLNodeValue> properties){
            var id=new GLNodeId(uuid(name+"/"+key));int index=positions.size();
            positions.put(id,new GLNodeViewState(60+(index%4)*300,60+(index/4)*380));
            graph.edit("Reference node",e->e.addNode(new GLGraphNode(id,type,properties)));return id;
        }
        void wire(GLNodeId from,String out,GLNodeId to,String in){
            var id=new GLNodeConnectionId(uuid(name+"/"+from+"/"+out+"/"+to+"/"+in));
            graph.edit("Reference wire",e->e.addConnection(new GLGraphConnection(id,new GLNodeEndpoint(from,out),new GLNodeEndpoint(to,in))));
        }
        GLNodeGraphAsset asset(GLCompositeNodeDescriptor descriptor){
            return new GLNodeGraphAsset(name,1,descriptor==null?GLNodeGraphAssetKind.PROJECT:GLNodeGraphAssetKind.COMPOSITE,
                    graph.snapshot(),new GLNodeEditorViewState(positions),Map.of("easyknack.title",GLNodeValue.of(name)),descriptor);
        }
    }
    static GLCompositePortMapping in(GLNodeId id,String port){return new GLCompositePortMapping(GLNodePortDescriptor.input("In",SignalCategory.MIDI.valueTypeId(),false,1),new GLNodeEndpoint(id,port));}
    static GLCompositePortMapping out(GLNodeId id,String port){return new GLCompositePortMapping(GLNodePortDescriptor.output("Out",SignalCategory.AUDIO.valueTypeId()),new GLNodeEndpoint(id,port));}
    static List<Project> all(){return List.of(make("math",0),make("synth-1",1),make("synth-8",8),make("synth-32",32),make("stereo",0),make("rates",0),make("analysis",0),make("osc-gain-filter",0));}
    static GLNodeGraphDocument routing(){
        var root=new Graph("routing.root");
        var endpoint=new com.vin.easyknack.audio.AudioEndpoint("reference-select-capture","Choose virtual cable capture",
                com.vin.easyknack.audio.AudioEndpoint.Type.HARDWARE_INPUT,true,false,false);
        var input=root.node("capture",new InputNode(),"endpoint",endpoint.reference());
        var scale=root.node("scale",new MathNode(),"b",.5);
        var scope=root.node("scope",new OscilloscopeNode());var correlation=root.node("correlation",new AutocorrelationNode());
        var quiet=root.node("quiet",new MathNode(),"b",0);var output=root.node("output",new OutputNode());
        root.wire(input,"audio-out",scale,"a");root.wire(scale,"audio-out",scope,"audio-in");
        root.wire(scale,"audio-out",correlation,"audio-in");root.wire(scale,"audio-out",quiet,"a");root.wire(quiet,"audio-out",output,"audio-in");
        return new GLNodeGraphDocument(root.asset(null),Map.of(),List.of());
    }
    static Project make(String name,int voices){
        var g=new Graph(name+"/processing");var assets=new LinkedHashMap<String,GLNodeGraphAsset>();
        var ports=new ArrayList<GLCompositePortMapping>();GLNodeId last;String lastPort="audio-out";
        boolean midi=voices>0||name.equals("rates");
        if(voices>0){
            var note=g.node("notes",new MidiNoteNode(),"voices",voices);
            var env=g.node("envelope",new EnvelopeNode(),"duration",30,"release",60);
            var osc=g.node("oscillator",new AdditiveOscillatorNode(),"harmonics",16,"falloff",1.5);
            var multiply=g.node("amplitude",new MathNode());
            var filter=g.node("filter",new FilterNode(),"cutoff",1800);
            last=g.node("merge",new VoiceMergeNode(),"mode","mean");
            ports.add(in(note,"midi-in"));g.wire(note,"frequency",osc,"frequency");
            g.wire(note,"gate",env,"gate");g.wire(note,"trigger",env,"trigger");
            g.wire(osc,"audio-out",multiply,"a");g.wire(env,"audio-out",multiply,"b");
            g.wire(multiply,"audio-out",filter,"audio-in");g.wire(filter,"audio-out",last,"audio-in");
        }else if(name.equals("osc-gain-filter")){
            var osc=g.node("oscillator",new OscillatorNode(),"frequency",440);
            var gain=g.node("gain",new MathNode(),"b",.3);
            last=g.node("filter",new FilterNode(),"cutoff",1000);
            g.wire(osc,"audio-out",gain,"a");g.wire(gain,"audio-out",last,"audio-in");
        }else if(name.equals("math")){
            var nested=new Graph(name+"/nested");var scale=nested.node("scale",new MathNode(),"b",.8);
            var nestedDescriptor=new GLCompositeNodeDescriptor("reference.v1.nested",nested.name,1,List.of(
                    new GLCompositePortMapping(GLNodePortDescriptor.input("In",SignalCategory.AUDIO.valueTypeId(),false,1),new GLNodeEndpoint(scale,"a")),out(scale,"audio-out")));
            assets.put(nested.name,nested.asset(nestedDescriptor));
            var osc=g.node("oscillator",new OscillatorNode(),"frequency",220);
            var sub=g.node("nested",nestedDescriptor.nodeTypeId(),Map.of());
            var add=g.node("offset",new MathNode(),"operation","add","b",.1);
            var mix=g.node("fanout",new MixNode(),"factor",.3);
            var map=g.node("map",new MapRangeNode(),"from-min",-1,"from-max",1,"to-min",0,"to-max",1);
            var units=g.node("percent",new UnitConverterNode(),"from","factor","to","percent");
            last=g.node("factor",new UnitConverterNode(),"from","percent","to","factor");
            g.wire(osc,"audio-out",sub,"In");g.wire(sub,"Out",add,"a");g.wire(sub,"Out",mix,"a");g.wire(add,"audio-out",mix,"b");
            g.wire(mix,"audio-out",map,"audio-in");g.wire(map,"audio-out",units,"value");g.wire(units,"out",last,"value");lastPort="out";
        }else if(name.equals("stereo")){
            var context=g.node("context",new ContextInputNode());var map=g.node("frequency",new MapRangeNode(),"from-min",-1,"from-max",1,"to-min",220,"to-max",330);
            var osc=g.node("oscillator",new OscillatorNode());var pan=g.node("pan",new PanNode(),"pan",.2);
            var split=g.node("split",new ChannelSeparateNode());last=g.node("swap",new ChannelCombineNode());
            g.wire(context,"channel",map,"audio-in");g.wire(map,"audio-out",osc,"frequency");g.wire(osc,"audio-out",pan,"audio-in");
            g.wire(pan,"audio-out",split,"audio-in");g.wire(split,"right",last,"left");g.wire(split,"left",last,"right");
        }else if(name.equals("rates")){
            var delay=g.node("delay",new MidiDelayNode(),"delay",3.25);var note=g.node("notes",new MidiNoteNode());
            var math=g.node("event",new MathNode(),"b",1.01,"rate","event");var osc=g.node("oscillator",new OscillatorNode());
            var control=g.node("rms",new RateConverterNode(),"rate","control","reduction","rms");
            last=g.node("smoothed",new MathNode(),"rate","audio");ports.add(in(delay,"midi-in"));
            g.wire(delay,"midi-out",note,"midi-in");g.wire(note,"frequency",math,"a");g.wire(math,"audio-out",osc,"frequency");
            g.wire(osc,"audio-out",control,"value");g.wire(osc,"audio-out",last,"a");g.wire(control,"out",last,"b");
        }else{
            last=g.node("oscillator",new OscillatorNode(),"frequency",440);
        }
        ports.add(out(last,lastPort));
        var descriptor=new GLCompositeNodeDescriptor("reference.v1."+name,g.name,1,ports);assets.put(g.name,g.asset(descriptor));
        var root=new Graph(name+"/root");GLNodeId input=midi?root.node("keyboard",new MidiInputNode()):null;
        var groupId=root.node("group",descriptor.nodeTypeId(),Map.of());
        if(midi)root.wire(input,"midi-out",groupId,"In");
        var gain=root.node("monitor-level",new MathNode(),"b",.1);root.wire(groupId,"Out",gain,"a");
        if(name.equals("analysis")){
            var scope=root.node("scope",new OscilloscopeNode());var correlation=root.node("correlation",new AutocorrelationNode());
            root.wire(gain,"audio-out",scope,"audio-in");root.wire(gain,"audio-out",correlation,"audio-in");
        }
        var sink=root.node("output",new OutputNode());root.wire(gain,"audio-out",sink,"audio-in");
        return new Project(name,new GLNodeGraphDocument(root.asset(null),assets,List.of()),root.graph.snapshot().findNode(groupId).orElseThrow(),midi);
    }
    static String original(Project p){return AdvancedSourceExportTest.reference(new AdvancedSourceExportTest.Fixture(p.document,p.group,"",p.midi));}
    static Project changed(Project p,boolean muted){
        var root=p.document.root();var graph=new GLNodeGraph(root.graph());
        var id=new GLNodeId(uuid(p.name+".root/monitor-level"));
        graph.edit("Reference live change",e->{e.setProperty(id,"b",GLNodeValue.of(.23));e.setProperty(id,GLNodeState.MUTED,GLNodeValue.of(muted));});
        var asset=new GLNodeGraphAsset(root.id(),root.version(),root.kind(),graph.snapshot(),root.viewState(),root.metadata(),root.compositeDescriptor());
        return new Project(p.name,new GLNodeGraphDocument(asset,p.document.embeddedAssets(),p.document.externalAssets()),p.group,p.midi);
    }
    static String baked(Project p,BasicNode definition){
        var graph=new GLNodeGraph(p.document.root().graph());
        // Reconnect exactly the existing root graph, including its analysis observers.
        graph.edit("Bake reference",e->{
            e.removeNode(p.group.id());e.addNode(new GLGraphNode(p.group.id(),definition.registration().id(),definition.defaults()));
            for(var wire:p.document.root().graph().connections())if(wire.input().nodeId().equals(p.group.id())||wire.output().nodeId().equals(p.group.id()))
                e.addConnection(new GLGraphConnection(wire.id(),wire.output().nodeId().equals(p.group.id())?new GLNodeEndpoint(p.group.id(),"output_0"):wire.output(),wire.input().nodeId().equals(p.group.id())?new GLNodeEndpoint(p.group.id(),"input_0"):wire.input()));
        });
        NodeRegistry.installPlugins(List.of(definition));
        try{return com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot());}finally{NodeRegistry.installPlugins(List.of());}
    }
}
