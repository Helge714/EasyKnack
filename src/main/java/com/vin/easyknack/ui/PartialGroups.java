package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Per-track frame programs. Ordinary nested groups and Formula Groups flatten before encoding. */
final class PartialGroups {
    static final String MARKER="easyknack.partials",CREATE="easyknack.action.partialgroup";
    static boolean partial(GLNodeGraphAsset a){return new NodeState(a.metadata()).flag(MARKER,false);}
    static boolean supports(BasicNode n){return n!=null&&Set.of("partialcontext","partialoutput","partialuniform","math","maprange","clamp","comparator","mix","curve","unitconvert","value","noise","digitalnoise","formula").contains(n.processor().id());}
    static GLNodeGraphAsset create(){
        String id="partials-"+UUID.randomUUID();var context=new PartialContextNode();var output=new PartialOutputNode();
        var a=new GLGraphNode(GLNodeId.random(),context.registration().id(),context.defaults());var b=new GLGraphNode(GLNodeId.random(),output.registration().id(),output.defaults());
        var graph=new GLNodeGraph();graph.edit("Partial group",e->{e.addNode(a);e.addNode(b);for(String p:List.of("frequency","amplitude","phase"))e.addConnection(wire(a.id(),p,b.id(),p));});
        var ports=List.of(new GLCompositePortMapping(GLNodePortDescriptor.output("out",SignalCategory.PARTIALS.valueTypeId()),new GLNodeEndpoint(b.id(),"partials")),new GLCompositePortMapping(GLNodePortDescriptor.input("partials",SignalCategory.PARTIALS.valueTypeId(),false,1),new GLNodeEndpoint(a.id(),"partials")));
        var d=new GLCompositeNodeDescriptor("easyknack.group."+id,id,1,ports);
        var meta=Map.of(MARKER,GLNodeValue.of(true),"easyknack.title",GLNodeValue.of("Partial Group"),"easyknack.category",GLNodeValue.of(NodeCategory.SPECTRAL.path()),"easyknack.kind",GLNodeValue.of("PARTIALS"),GroupRouting.INSERT_IN,GLNodeValue.of("partials"),GroupRouting.INSERT_OUT,GLNodeValue.of("out"),GroupRouting.MUTE_IN,GLNodeValue.of("partials"),GroupRouting.MUTE_OUT,GLNodeValue.of("out"));
        return new GLNodeGraphAsset(id,1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),new GLNodeEditorViewState(Map.of(a.id(),new GLNodeViewState(200,120),b.id(),new GLNodeViewState(600,120))),meta,d);
    }
    record Compiled(GLGraphNode node,Map<String,String> inputs,String output){}
    static Compiled compile(GLGraphNode instance,GLNodeGraphAsset asset,GLCompositeGraphRegistry registry,GLCompositeExpansionLimits limits,Set<String> connected){
        var descriptor=asset.compositeDescriptor();var ports=descriptor.portMappings().stream().filter(p->!GroupInputs.editorOnly(p)).toList();
        var resources=ports.stream().filter(p->p.publicPort().direction()==GLPortDirection.INPUT&&p.publicPort().valueTypeId().equals(SignalCategory.PARTIALS.valueTypeId())).toList();
        var outputs=ports.stream().filter(p->p.publicPort().direction()==GLPortDirection.OUTPUT).toList();
        var inputs=ports.stream().filter(p->p.publicPort().direction()==GLPortDirection.INPUT&&!resources.contains(p)).toList();
        if(resources.size()!=1||outputs.size()!=1||!outputs.get(0).publicPort().valueTypeId().equals(SignalCategory.PARTIALS.valueTypeId())||inputs.size()>255||inputs.stream().anyMatch(p->!Set.of(SignalCategory.AUDIO.valueTypeId(),SignalCategory.VALUE.valueTypeId()).contains(p.publicPort().valueTypeId())))throw new IllegalArgumentException(AppText.text("partials.boundary"));
        var graph=new GLNodeGraph();var boundary=GLNodeId.random();var marker=GLNodeId.random();
        graph.edit("Compile partials",e->{
            e.addNode(instance.withoutProperty(GLNodeState.MUTED).withoutProperty(GLNodeState.HIDDEN));
            e.addNode(new GLGraphNode(boundary,new PartialAnalysisNode().registration().id(),new PartialAnalysisNode().defaults()));
            e.addNode(new GLGraphNode(marker,new PartialContextNode().registration().id(),new PartialContextNode().defaults()));
            e.addConnection(wire(boundary,"partials",instance.id(),resources.get(0).publicPort().id()));
            e.addConnection(wire(instance.id(),outputs.get(0).publicPort().id(),marker,"partials"));
            for(int i=0;i<inputs.size();i++)if(connected.contains(inputs.get(i).publicPort().id())){
                var d=new PartialUniformNode();var id=GLNodeId.random();e.addNode(new GLGraphNode(id,d.registration().id(),Map.of("slot",GLNodeValue.of(i))));e.addConnection(wire(id,"out",instance.id(),inputs.get(i).publicPort().id()));
            }
        });
        var root=new GLNodeGraphAsset("partial-program",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty());
        var flat=GLCompositeGraphFlattener.flatten(root,registry,limits);if(!flat.isValid())throw new IllegalArgumentException("Invalid Partial Group: "+flat.diagnostics());
        var source=GroupSourceExporter.materializeUnits(AutomaticRates.resolve(SpectralGroups.resolveMutes(flat.graph().orElseThrow())));
        var endpoint=source.connections().stream().filter(w->w.input().nodeId().equals(marker)).map(GLGraphConnection::output).findFirst().orElseThrow(()->new IllegalArgumentException("Connect Partial Output to the group output"));
        if(!(NodeRegistry.require(source.findNode(endpoint.nodeId()).orElseThrow().typeId()) instanceof PartialOutputNode))throw new IllegalArgumentException("Use Partial Output before the group output");
        var order=new ArrayList<GLGraphNode>();visit(endpoint.nodeId(),source,new HashSet<>(),new HashSet<>(),order);
        if(order.size()>64)throw new IllegalArgumentException("Partial Group supports up to 64 frame processors");
        var indices=new HashMap<GLNodeId,Integer>();for(int i=0;i<order.size();i++)indices.put(order.get(i).id(),i);
        var wires=new HashMap<GLNodeEndpoint,GLNodeEndpoint>();source.connections().forEach(w->wires.put(w.input(),w.output()));
        var program=new StringBuilder("PAR1 ").append(order.size());
        for(var n:order){var def=NodeRegistry.require(n.typeId());if(!supports(def))throw new IllegalArgumentException(def.title()+" is not supported inside Partial Group");
            if(def instanceof FormulaNode&&(new NodeState(n.properties()).number("bufferInputs",0)!=0||new NodeState(n.properties()).number("triggerInputs",0)!=0||new NodeState(n.properties()).number("triggerOutputs",0)!=0))throw new IllegalArgumentException("Partial formulas accept numeric inputs only");
            program.append(' ').append(def.processor().id()).append(" !").append(NativeContractGenerator.fingerprint(def)).append(' ').append(def.encode(new NodeState(n.properties()),p->{
                if(def instanceof PartialContextNode)return -1;var upstream=wires.get(new GLNodeEndpoint(n.id(),p.value()));if(upstream==null)return -1;
                var d=NodeRegistry.require(source.findNode(upstream.nodeId()).orElseThrow().typeId());var rows=d.layout(new NodeState(source.findNode(upstream.nodeId()).orElseThrow().properties())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).toList();
                for(int i=0;i<rows.size();i++)if(rows.get(i).id().equals(upstream.portId()))return indices.get(upstream.nodeId())+256*i;
                throw new IllegalArgumentException("Unknown Partial Group output");
            }));
        }
        var names=new LinkedHashMap<String,String>();names.put(resources.get(0).publicPort().id(),"partials");for(int i=0;i<inputs.size();i++)names.put(inputs.get(i).publicPort().id(),PartialGraphNode.external(i).value());
        var values=new HashMap<String,GLNodeValue>();values.put("program",GLNodeValue.of(SpectralGraphNode.hex(program.toString())));values.put("inputs",GLNodeValue.of(inputs.size()));
        for(String key:List.of(GLNodeState.MUTED,GLNodeState.HIDDEN))if(instance.properties().containsKey(key))values.put(key,instance.properties().get(key));
        return new Compiled(new GLGraphNode(instance.id(),new PartialGraphNode().registration().id(),values),names,outputs.get(0).publicPort().id());
    }
    private static void visit(GLNodeId id,GLNodeGraphSnapshot graph,Set<GLNodeId> visiting,Set<GLNodeId> done,List<GLGraphNode> order){
        if(done.contains(id))return;if(!visiting.add(id))throw new IllegalArgumentException("Partial Group cannot contain feedback");
        var n=graph.findNode(id).orElseThrow();if(!(NodeRegistry.require(n.typeId()) instanceof PartialContextNode))for(var w:graph.connections())if(w.input().nodeId().equals(id))visit(w.output().nodeId(),graph,visiting,done,order);
        visiting.remove(id);done.add(id);order.add(n);
    }
    private static GLGraphConnection wire(GLNodeId a,String out,GLNodeId b,String in){return new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a,out),new GLNodeEndpoint(b,in));}
}
