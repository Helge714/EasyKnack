package com.vin.easyknack.compiler;
import com.vin.easyknack.i18n.AppText;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

/** Pure-operation regions with constant folding, exact expression reuse and guarded SIMD. */
public final class MathFusion {
    public record Region(int first,int last,String method,String code){}
    public record Op(GLGraphNode node,BasicNode definition,NodeState state,List<String> ports,List<String> fallbacks,String type){}
    public static boolean supported(GLGraphNode n){var d=NodeRegistry.require(n.typeId());
        // Sample conversion depends on the processing clock, so must not become a
        // class-level folded constant. Keep it on the context-aware processor path.
        if(d instanceof UnitConverterNode){var s=new NodeState(n.properties());if(s.text("from","").equals("sample")||s.text("to","").equals("sample"))return false;}
        return d instanceof MathNode||d instanceof MixNode||d instanceof MapRangeNode||d instanceof UnitConverterNode;}
    public static List<Region> plan(List<GLGraphNode> nodes,Map<GLNodeEndpoint,GLNodeEndpoint> sources,
            Map<GLNodeId,Integer> indices,java.util.function.ToIntFunction<GLNodeEndpoint> reference){
        var result=new ArrayList<Region>();
        for(int start=0;start<nodes.size();){
            if(!supported(nodes.get(start))){start++;continue;}
            int end=start+1;while(end<nodes.size()&&supported(nodes.get(end)))end++;
            if(end-start>=2){var region=nodes.subList(start,end);String method="renderFusedMath"+(result.isEmpty()?"":result.size());
                result.add(new Region(indices.get(region.get(0).id()),indices.get(region.get(region.size()-1).id()),method,
                    emit(region,sources,indices,reference,method+"Vector")+emitMixed(region,sources,indices,reference,method)));}
            start=end;
        }
        return List.copyOf(result);
    }
    private static Op operation(GLGraphNode node){
        var d=NodeRegistry.require(node.typeId());var values=new HashMap<>(d.defaults());values.putAll(node.properties());var s=new NodeState(values);
        if(d instanceof MathNode)return new Op(node,d,s,List.of("a","b"),List.of("aValue","bValue"),"MathNode");
        if(d instanceof MixNode)return new Op(node,d,s,List.of("a","b","factor"),List.of("values[0]","values[1]","values[2]"),"MixNode");
        if(d instanceof MapRangeNode)return new Op(node,d,s,List.of("audio-in","from-min","from-max","to-min","to-max"),List.of("valueValue","fromMinValue","fromMaxValue","toMinValue","toMaxValue"),"MapRangeNode");
        return new Op(node,d,s,List.of("value"),List.of("value"),"UnitConverterNode");
    }
    private static String literal(double number){float value=(float)number;if(!Float.isFinite(value))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Non-finite baked constant"));return Float.toHexString(value)+"f";}
    private static List<String> ports(Op op){return op.definition instanceof MathNode&&com.vin.easyknack.model.MathOperation.fromId(op.state.text("operation","multiply")).unary()?List.of("a"):op.ports;}
    private static String expression(Op op,List<String> args,boolean vector){
        String call=op.type+"::evaluate(";
        if(op.definition instanceof MathNode){String a=args.get(0),b=args.size()>1?args.get(1):vector?"simd::splat(0.f)":"0.f";
            return (vector?"simd::evaluate(":call)+a+","+b+",MathOperation::"+op.state.text("operation","multiply")+","+op.state.flag("clamp",false)+")";}
        if(op.definition instanceof MixNode)return call+String.join(",",args)+","+op.state.text("mode","linear").equals("equalpower")+")";
        if(op.definition instanceof MapRangeNode)return call+String.join(",",args)+","+op.state.flag("clamp",false)+")";
        if(op.definition instanceof UnitConverterNode)return call+args.get(0)+",UnitFormat::"+op.state.text("from","factor")+",UnitFormat::"+op.state.text("to","percent")+")";
        return call+String.join(",",args)+")";
    }
    private static String emit(List<GLGraphNode> nodes,Map<GLNodeEndpoint,GLNodeEndpoint> sources,
            Map<GLNodeId,Integer> indices,java.util.function.ToIntFunction<GLNodeEndpoint> reference,String method){
        var members=new HashSet<GLNodeId>();nodes.forEach(n->members.add(n.id()));
        var escaping=new HashSet<GLNodeId>();var boundary=new TreeSet<Integer>();
        sources.forEach((target,source)->{if(members.contains(source.nodeId())&&!members.contains(target.nodeId()))escaping.add(source.nodeId());
            if(members.contains(target.nodeId())&&!members.contains(source.nodeId()))boundary.add(reference.applyAsInt(source));});
        var ops=nodes.stream().map(MathFusion::operation).toList();
        var constant=new HashSet<GLNodeId>();var aliases=new HashMap<GLNodeId,String>();var expressions=new HashMap<String,String>();
        var fields=new StringBuilder();var scalar=new StringBuilder();var vector=new StringBuilder();int folded=0,reused=0;
        for(var op:ops){
            int index=indices.get(op.node.id());var args=new ArrayList<String>();var vargs=new ArrayList<String>();boolean fixed=true;
            for(String port:ports(op)){
                var src=sources.get(new GLNodeEndpoint(op.node.id(),port));
                if(src==null){String value=literal(op.state.number(port,0));args.add(value);vargs.add("simd::splat("+value+")");}
                else if(members.contains(src.nodeId())){String value=aliases.get(src.nodeId());args.add(value);vargs.add(constant.contains(src.nodeId())?"simd::splat("+value+")":"x"+value);fixed&=constant.contains(src.nodeId());}
                else {int ref=reference.applyAsInt(src),i=ref%256,p=ref/256;String data="graph.nodes["+i+"].channelCache[graph.nodes["+i+"].stereo?channel:0].outputs["+p+"].data()";
                    args.add("("+data+")[sample]");vargs.add("simd::load("+data+"+sample)");fixed=false;}
            }
            String expression=expression(op,args,false),previous=expressions.get(expression),value;
            if(previous!=null){value=previous;reused++;}
            else {value=(fixed?"folded_":"v")+index;expressions.put(expression,value);
                if(fixed){fields.append("    const float ").append(value).append('=').append(expression).append(";\n");folded++;}
                else {scalar.append("                const float ").append(value).append('=').append(expression).append(";\n");
                    if(op.definition instanceof MathNode)vector.append("                const auto x").append(value).append('=').append(expression(op,vargs,true)).append(";\n");}
            }
            aliases.put(op.node.id(),value);if(fixed)constant.add(op.node.id());
            if(escaping.contains(op.node.id())){
                String dest="graph.nodes["+index+"].channelCache[graph.nodes["+index+"].stereo?channel:0].outputs[0].data()";
                scalar.append("                (").append(dest).append(")[sample]=").append(value).append(";\n");
                vector.append("                simd::store(").append(dest).append("+sample,").append(fixed?"simd::splat("+value+")":"x"+value).append(");\n");
            }
        }
        int first=indices.get(nodes.stream().filter(n->!constant.contains(n.id())).findFirst().orElse(nodes.get(0)).id());
        var s=new StringBuilder("    // Pure DSP region: folded=").append(folded).append(", reused=").append(reused).append(".\n").append(fields);
        s.append("    bool ").append(method).append("(int frames,AudioIO*,MidiSource*)noexcept{auto& graph=runtime->graph;\n")
            .append("        if(frames<=1)return false;\n        const bool stereo=graph.nodes[").append(first).append("].stereo;\n");
        for(var op:ops){int i=indices.get(op.node.id());s.append("        if(");
            s.append("graph.nodes[").append(i).append("].controlRate||");
            if(!constant.contains(op.node.id()))s.append("graph.nodes[").append(i).append("].stereo!=stereo||");
            s.append("graph.nodes[").append(i).append("].eventRate||!graph.nodes[").append(i).append("].active||graph.nodes[").append(i).append("].voiceCount()!=1)return false;\n");
            // State may differ after a live swap: the ordinary scheduler finishes smoothing first.
            if(op.definition instanceof MathNode||op.definition instanceof MixNode){
                s.append("        for(int ch=0;ch<(graph.nodes[").append(i).append("].stereo?2:1);ch++){auto& p=static_cast<").append(op.type).append("&>(*(ch?graph.nodes[").append(i).append("].rightProcessor:graph.nodes[").append(i).append("].processor));\n");
                if(op.definition instanceof MathNode)s.append("            if(p.currentA!=p.aValue||p.currentB!=p.bValue)return false;\n");
                else s.append("            if(p.current!=p.values)return false;\n");
                s.append("        }\n");
            }
        }
        for(int ref:boundary){int i=ref%256;s.append("        if(graph.nodes[").append(i).append("].controlRate||graph.nodes[").append(i).append("].eventRate||!graph.nodes[").append(i).append("].active||graph.nodes[").append(i).append("].voiceCount()!=1)return false;\n");}
        s.append("        for(std::size_t channel=0;channel<(stereo?2u:1u);channel++){int sample=0;\n");
        if(ops.stream().allMatch(op->op.definition instanceof MathNode&&Set.of("add","subtract","multiply","divide","minimum","maximum","absolute","negate").contains(op.state.text("operation","multiply"))))s.append("#if defined(__SSE2__) && !defined(EASYKNACK_DISABLE_SIMD)\n            for(;sample+4<=frames;sample+=4){\n").append(vector).append("            }\n#endif\n");
        return s.append("            for(;sample<frames;sample++){\n").append(scalar).append("            }\n        }\n        return true;\n    }\n").toString();
    }

    /** Block values are computed once; audio values share one loop and no internal buffers. */
    private static String emitMixed(List<GLGraphNode> nodes,Map<GLNodeEndpoint,GLNodeEndpoint> sources,
            Map<GLNodeId,Integer> indices,java.util.function.ToIntFunction<GLNodeEndpoint> reference,String method){
        var members=new HashSet<GLNodeId>();nodes.forEach(n->members.add(n.id()));
        var escaping=new HashSet<GLNodeId>();var boundary=new TreeSet<Integer>();
        sources.forEach((target,source)->{if(members.contains(source.nodeId())&&!members.contains(target.nodeId()))escaping.add(source.nodeId());
            if(members.contains(target.nodeId())&&!members.contains(source.nodeId()))boundary.add(reference.applyAsInt(source));});
        var s=new StringBuilder("    bool "+method+"(int frames,AudioIO* io,MidiSource* midi)noexcept{\n");
        s.append("        if(").append(method).append("Vector(frames,io,midi))return true;\n        if(frames<1)return false;auto& graph=runtime->graph;bool stereo=false;\n");
        for(var n:nodes){int i=indices.get(n.id());var op=operation(n);
            s.append("        auto& n").append(i).append("=graph.nodes[").append(i).append("];if(n").append(i).append(".eventRate||!n").append(i).append(".active||n").append(i).append(".voiceCount()!=1)return false;stereo|=n").append(i).append(".stereo;\n");
            if(op.definition instanceof MathNode||op.definition instanceof MixNode){
                s.append("        for(int ch=0;ch<(n").append(i).append(".stereo?2:1);ch++){auto& p=static_cast<").append(op.type).append("&>(*(ch?n").append(i).append(".rightProcessor:n").append(i).append(".processor));\n");
                for(int p=0;p<op.ports.size();p++)if(!sources.containsKey(new GLNodeEndpoint(n.id(),op.ports.get(p)))){
                    String current=op.definition instanceof MathNode?(p==0?"currentA":"currentB"):"current["+p+"]";
                    s.append("            if(p.").append(current).append("!=p.").append(op.fallbacks.get(p)).append(")return false;\n");
                }
                s.append("        }\n");
            }
        }
        for(int ref:boundary)s.append("        if(graph.nodes[").append(ref%256).append("].eventRate||graph.nodes[").append(ref%256).append("].voiceCount()!=1)return false;\n");
        s.append("        for(int channel=0;channel<(stereo?2:1);channel++){\n");
        var expressions=new HashMap<GLNodeId,String>();
        for(var n:nodes){int i=indices.get(n.id());var op=operation(n);var args=new ArrayList<String>();
            for(String port:ports(op)){
                int slot=op.ports.indexOf(port);var source=sources.get(new GLNodeEndpoint(n.id(),port));
                if(source==null)args.add(literal(op.state.number(port,0)));
                else if(members.contains(source.nodeId())){int j=indices.get(source.nodeId());args.add("(n"+i+".inputRates["+slot+"]=='c'?first"+j+":v"+j+")");}
                else {int ref=reference.applyAsInt(source),j=ref%256,p=ref/256;String data="in"+i+"_"+slot;
                    s.append("            const auto* ").append(data).append("=graph.nodes[").append(j).append("].channelCache[graph.nodes[").append(j).append("].stereo?channel:0].outputs[").append(p).append("].data();\n");
                    s.append("            const bool scalar").append(i).append('_').append(slot).append("=n").append(i).append(".inputRates[").append(slot).append("]=='c'||graph.nodes[").append(j).append("].outputRate(").append(p).append(")=='c';\n");
                    args.add(data+"[scalar"+i+"_"+slot+"?0:sample]");
                }
            }
            String expression=expression(op,args,false);expressions.put(n.id(),expression);
            s.append("            float v").append(i).append(";{const int sample=0;v").append(i).append('=').append(expression).append(";}const float first").append(i).append("=v").append(i).append(";\n");
            if(escaping.contains(n.id()))s.append("            auto* out").append(i).append("=n").append(i).append(".channelCache[n").append(i).append(".stereo?channel:0].outputs[0].data();if(n").append(i).append(".controlRate)std::fill_n(out").append(i).append(",frames,v").append(i).append(");else out").append(i).append("[0]=v").append(i).append(";\n");
        }
        s.append("            for(int sample=1;sample<frames;sample++){\n");
        for(var n:nodes){int i=indices.get(n.id());s.append("                if(!n").append(i).append(".controlRate){v").append(i).append('=').append(expressions.get(n.id())).append(';');
            if(escaping.contains(n.id()))s.append("out").append(i).append("[sample]=v").append(i).append(';');s.append("}\n");}
        return s.append("            }\n        }return true;\n    }\n").toString();
    }
}
