package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

/** Emits direct calls to existing C++ sample kernels; never duplicates DSP formulas. */
final class SampleFusion {
    private static final Set<String> TYPES=Set.of("math","mix","maprange","unitconvert","clamp","curve","osc","filter","gain","noise","value");
    static boolean supported(GLGraphNode n){var d=NodeRegistry.require(n.typeId());return TYPES.contains(d.processor().id())
        &&d.layout(new NodeState(n.properties())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).count()==1;}
    static List<MathFusion.Region> plan(List<GLGraphNode> nodes,Map<GLNodeEndpoint,GLNodeEndpoint> sources,
            Map<GLNodeId,Integer> indices,java.util.function.ToIntFunction<GLNodeEndpoint> reference){
        var result=new ArrayList<MathFusion.Region>();
        for(int start=0;start<nodes.size();){
            if(!supported(nodes.get(start))){start++;continue;}
            int end=start+1;
            while(end<nodes.size()&&supported(nodes.get(end))){
                var previous=nodes.get(end-1).id();var next=nodes.get(end).id();
                long consumers=sources.values().stream().filter(s->s.nodeId().equals(previous)).count();
                boolean connected=sources.entrySet().stream().anyMatch(e->e.getKey().nodeId().equals(next)&&e.getValue().nodeId().equals(previous));
                // Do not force independent branches/fanout into one scalar loop:
                // retaining block scheduling there preserves vectorization/locality.
                if(consumers!=1||!connected)break;
                end++;
            }
            var region=nodes.subList(start,end);
            if(region.size()>1&&region.stream().anyMatch(n->!MathFusion.supported(n))){
                String method="renderSampleRegion"+result.size();
                result.add(new MathFusion.Region(indices.get(region.get(0).id()),indices.get(region.get(region.size()-1).id()),method,emit(region,sources,indices,reference,method)));
            }
            start=end;
        }
        return result;
    }
    private static String emit(List<GLGraphNode> nodes,Map<GLNodeEndpoint,GLNodeEndpoint> sources,
            Map<GLNodeId,Integer> indices,java.util.function.ToIntFunction<GLNodeEndpoint> reference,String method){
        var members=new HashSet<GLNodeId>();nodes.forEach(n->members.add(n.id()));
        var escaping=new HashSet<GLNodeId>();sources.forEach((target,source)->{if(members.contains(source.nodeId())&&!members.contains(target.nodeId()))escaping.add(source.nodeId());});
        var s=new StringBuilder("    bool "+method+"(int frames,AudioIO* io,MidiSource* midi)noexcept{auto& graph=runtime->graph;if(frames<1||!sampleKernelsPrepared)return false;\n");
        int first=indices.get(nodes.get(0).id());s.append("        const bool stereo=graph.nodes[").append(first).append("].stereo;\n");
        for(var n:nodes){int i=indices.get(n.id());var d=NodeRegistry.require(n.typeId());
            s.append("        auto& n").append(i).append("=graph.nodes[").append(i).append("];if(!n").append(i).append(".active||n").append(i).append(".eventRate||n").append(i).append(".voiceCount()!=1||n").append(i).append(".stereo!=stereo)return false;\n");
            s.append("        const bool control").append(i).append("=n").append(i).append(".controlRate;\n");
            // Wavetable data are entire blocks, not scalar numeric edges.
            if(d instanceof OscillatorNode&&sources.containsKey(new GLNodeEndpoint(n.id(),"waveform")))s.append("        return false;\n");
            for(var key:d.processor().inputs()){
                var source=sources.get(new GLNodeEndpoint(n.id(),key.value()));if(source==null||members.contains(source.nodeId()))continue;
                int j=reference.applyAsInt(source)%256;
                s.append("        if(graph.nodes[").append(j).append("].eventRate||graph.nodes[").append(j).append("].voiceCount()!=1)return false;\n");
            }
        }
        s.append("        for(int channel=0;channel<(stereo?2:1);channel++){float silence=0;\n");
        for(var n:nodes){int i=indices.get(n.id());var d=NodeRegistry.require(n.typeId());String type=java.nio.file.Path.of(d.processor().header()).getFileName().toString().replace(".hpp","");
            s.append("            auto& p").append(i).append("=static_cast<").append(type).append("&>(*(channel?n").append(i).append(".rightProcessor:n").append(i).append(".processor));float value").append(i).append("=0,first").append(i).append("=0;\n");
            if(escaping.contains(n.id()))s.append("            auto* out").append(i).append("=n").append(i).append(".channelCache[channel].outputs[0].data();\n");
            var arguments=new ArrayList<String>();var updates=new StringBuilder();int slot=0;
            for(var key:d.processor().inputs()){
                var source=sources.get(new GLNodeEndpoint(n.id(),key.value()));
                if(source==null)arguments.add("nullptr");
                else if(members.contains(source.nodeId())){int j=indices.get(source.nodeId());arguments.add("(n"+i+".inputRates["+slot+"]=='c'?&first"+j+":&value"+j+")");}
                else {int ref=reference.applyAsInt(source),j=ref%256,p=ref/256;String input="source"+i+"_"+slot;
                    s.append("            const auto* ").append(input).append("=graph.nodes[").append(j).append("].channelCache[graph.nodes[").append(j).append("].stereo?channel:0].outputs[").append(p).append("].data();\n");
                    s.append("            const int stride").append(i).append('_').append(slot).append("=(n").append(i).append(".inputRates[").append(slot).append("]=='c'||graph.nodes[").append(j).append("].outputRate(").append(p).append(")== 'c')?0:1;\n");
                    arguments.add(input);updates.append("                inputs").append(i).append('[').append(slot).append("]=").append(input).append("+sample*stride").append(i).append('_').append(slot).append(";\n");}
                slot++;
            }
            s.append("            const float* inputs").append(i).append("[]={").append(arguments.isEmpty()?"nullptr":String.join(",",arguments)).append("};\n");
            s.append("            ProcessContext context").append(i).append("{inputs").append(i).append(',').append(arguments.size()).append(",&value").append(i).append(",1,sampleRate,&silence,static_cast<std::size_t>(frames),io,static_cast<std::size_t>(channel)};context").append(i).append(".samplewise=!control").append(i).append(";context").append(i).append(".midiSource=midi;\n");
            if(d instanceof OscillatorNode){
                var frequency=sources.get(new GLNodeEndpoint(n.id(),"frequency"));
                String fixed=frequency==null?"p"+i+".currentFrequency==p"+i+".frequency":"n"+i+".inputRates[0]=='c'||graph.nodes["+(reference.applyAsInt(frequency)%256)+"].outputRate("+(reference.applyAsInt(frequency)/256)+")== 'c'";
                s.append("            const bool fixedFrequency").append(i).append('=').append(fixed).append(";OscillatorNode::SampleKernel oscillatorKernel").append(i).append("{};\n");
            }
            s.append("            auto step").append(i).append("=[&](int sample){context").append(i).append(".sampleOffset=sample;\n").append(updates);
            if(d instanceof OscillatorNode)s.append("                if(fixedFrequency").append(i).append("){if(sample==0)oscillatorKernel").append(i).append("=p").append(i).append(".prepareSample(inputs").append(i).append("[0]?*inputs").append(i).append("[0]:p").append(i).append(".frequency,sampleRate);p").append(i).append(".processFixedSample(context").append(i).append(",oscillatorKernel").append(i).append(");return;}\n");
            var state=new NodeState(n.properties());
            String methodName=d instanceof MathNode||d instanceof MixNode?"processKernel":d instanceof OscillatorNode||d instanceof FilterNode?"processSample":"process";
            String constants=d instanceof MathNode?",MathOperation::"+state.text("operation","multiply")+","+state.flag("clamp",false):d instanceof MixNode?","+state.text("mode","linear").equals("equalpower"):"";
            s.append("#if defined(__clang__)\n                [[clang::always_inline]]\n#endif\n                p").append(i).append('.').append(type).append("::").append(methodName).append("(context").append(i).append(constants).append(");\n            };\n");
        }
        // The first sample establishes held values even for audio -> control edges.
        for(var n:nodes){int i=indices.get(n.id());s.append("            step").append(i).append("(0);first").append(i).append("=value").append(i).append(';');
            if(escaping.contains(n.id()))s.append("if(control").append(i).append(")std::fill_n(out").append(i).append(",frames,value").append(i).append(");else out").append(i).append("[0]=value").append(i).append(';');s.append('\n');}
        s.append("            for(int sample=1;sample<frames;sample++){\n");
        for(var n:nodes){int i=indices.get(n.id());s.append("                if(!control").append(i).append("){step").append(i).append("(sample);");if(escaping.contains(n.id()))s.append("out").append(i).append("[sample]=value").append(i).append(';');s.append("}\n");}
        return s.append("            }\n        }return true;\n    }\n").toString();
    }
}
