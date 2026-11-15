package com.vin.easyknack.ui;

import java.nio.charset.StandardCharsets;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

/** One source path for Inspector Bake and source plugins. No second DSP formula set. */
final class InstanceSourceSpecialization {
    private InstanceSourceSpecialization() { }
    private static String quote(String value){return "\""+value.replace("\\","\\\\").replace("\"","\\\"")+"\"";}

    static String emit(String name,String processor,List<GLGraphNode> nodes,
            Map<GLNodeEndpoint,GLNodeEndpoint> wires,List<GLNodeId> external,List<GLNodeId> targets,
            List<GroupSourceExporter.Port> inputs,List<GroupSourceExporter.Port> outputs){
        if(nodes.size()!=1||!(NodeRegistry.require(nodes.get(0).typeId()) instanceof MultipleGraphNode))return null;
        if(inputs.stream().anyMatch(p->p.nativeKind()!=0||p.signal()==SignalCategory.WAVETABLE||!p.choiceValues().isEmpty()))return null;
        var node=nodes.get(0);var definition=NodeRegistry.require(node.typeId());
        for(int i=0;i<outputs.size();i++){
            var source=wires.get(new GLNodeEndpoint(targets.get(i),outputs.get(i).endpoint(false)));
            if(source==null||!source.equals(new GLNodeEndpoint(node.id(),"output"+i)))return null;
        }
        var slots=new ArrayList<Integer>();
        for(var port:definition.processor().inputs(new NodeState(node.properties()))){
            var source=wires.get(new GLNodeEndpoint(node.id(),port.value()));
            if(source!=null&&!external.contains(source.nodeId()))return null;
            slots.add(source==null?-1:external.indexOf(source.nodeId()));
        }
        String program=new NodeState(node.properties()).text("program","");
        String kernel=scalarKernel(program);
        String payload="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(node.properties()),p->-1);
        var s=new StringBuilder("#pragma once\n#include \"ParameterContracts.hpp\"\n#include \"nodes/MultipleGraphNode.hpp\"\n#include \"CompiledInstanceAudio.hpp\"\nnamespace easyknack {\n");
        s.append("struct ").append(name).append(":MultipleGraphNode {\n");
        if(kernel!=null){
            s.append("    CompiledInstanceAudio audioKernel;\n    std::shared_ptr<CompiledScalarMemo> scalarMemo=std::make_shared<CompiledScalarMemo>();\n    struct CompiledInstance final:Instance {\n    std::shared_ptr<CompiledScalarMemo> memo;\n").append(kernel)
                .append("    void prepare(double rate,std::size_t frames)override{Instance::prepare(rate,frames);scalarCacheValid=false;}\n")
                .append("    void adoptState(NodeProcessor& p)noexcept override{Instance::adoptState(p);scalarCacheValid=false;}\n    };\n")
                .append("    void prepare(double rate,std::size_t frames)override{MultipleGraphNode::prepare(rate,frames);audioKernel.invalidate();}\n")
                .append("    std::unique_ptr<Instance> createInstance()override{auto instance=std::make_unique<CompiledInstance>();instance->memo=scalarMemo;return instance;}\n");
        }
        s.append("    std::array<float,").append(Math.max(1,inputs.size())).append("> defaults{};\n")
            .append("    bool inputUsesBlockData(std::size_t)const noexcept override{return true;}\n")
            .append("    bool supportsLiveParameters()const noexcept override{return true;}\n")
            .append("    void applyParameters(const NodeProcessor& p)noexcept override{defaults=static_cast<const ").append(name).append("&>(p).defaults;}\n")
            .append("    void configure(std::istream& in)override{auto p=parameters::").append(name).append("Parameters::read(in);\n")
            .append("        std::istringstream setup(").append(quote(payload)).append(");MultipleGraphNode::configure(setup);sources=p.sources();\n");
        for(int i=0;i<inputs.size();i++)if(inputs.get(i).editor()!=null)
            s.append("        defaults[").append(i).append("]=static_cast<float>(p.").append(inputs.get(i).id().value()).append("_value);\n");
        s.append("    }\n    void process(ProcessContext& c)noexcept override{\n")
            .append("        std::array<const float*,").append(slots.size()).append("> left{},right{};std::array<char,").append(slots.size()).append("> rates{};rates.fill('c');\n");
        for(int i=0;i<slots.size();i++)if(slots.get(i)>=0){int p=slots.get(i);
            s.append("        left[").append(i).append("]=c.input(").append(p).append(");if(!left[").append(i).append("])left[").append(i).append("]=&defaults[").append(p).append("];\n")
                .append("        right[").append(i).append("]=c.rightInputs&&").append(p).append("<c.inputCount?c.rightInputs[").append(p).append("]:c.input(").append(p).append(");if(!right[").append(i).append("])right[").append(i).append("]=&defaults[").append(p).append("];\n")
                .append("        rates[").append(i).append("]=c.rawInput(").append(p).append(")?(c.sourceRates?c.sourceRates[").append(p).append("]:'a'):c.input(").append(p).append(")?(c.numericInput(").append(p).append(").stride?'a':'c'):'c';\n");
        }
        s.append("        auto inner=c;inner.inputs=left.data();inner.rightInputs=right.data();inner.sourceRates=rates.data();inner.inputCount=").append(slots.size()).append(";inner.controls=nullptr;\n")
            .append(kernel==null?"":"        if(audioKernel.process<CompiledInstance>(*this,inner))return;\n")
            .append(kernel==null?"":"        if(inner.channelIndex==0){audioKernel.invalidate();for(auto& instance:state->instances)static_cast<CompiledInstance&>(*instance).scalarCacheValid=false;}\n")
            .append("        processInstances<").append(kernel==null?"Instance":"CompiledInstance").append(">(inner);\n    }\n};\n")
            .append("EASYKNACK_NODE(").append(name).append(',').append(quote(processor)).append(")\n}\n");
        return s.toString();
    }

    private record Operation(String type,Map<String,String> fields,List<Integer> sources) {
        String field(String name){String value=fields.get(name);if(value==null)throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Unsupported field: ")+name);return value;}
        String value(String id){return field(id+"_value");}
        int source(String id){return Integer.parseInt(field(id+"_source"));}
    }
    /** Read fields through the Java/C++ contract, not positional offsets. */
    private static List<Operation> parse(String hex){
        var definitions=new HashMap<String,BasicNode>();
        NodeRegistry.builtinDefinitions().forEach(d->definitions.putIfAbsent(d.processor().id(),d));
        var tokens=new ArrayDeque<>(List.of(new String(HexFormat.of().parseHex(hex),StandardCharsets.UTF_8).trim().split("\\s+")));
        if(!tokens.remove().equals("MULT3"))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Legacy instance program"));
        int inputs=Integer.parseInt(tokens.remove()),outputs=Integer.parseInt(tokens.remove());
        for(int i=0;i<inputs+outputs;i++)tokens.remove();
        if(!tokens.remove().equals("1"))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Legacy reduction"));
        int policies=Integer.parseInt(tokens.remove());for(int i=0;i<policies*2;i++)tokens.remove();
        if(!tokens.remove().equals("GRAPH"))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Missing graph"));
        int count=Integer.parseInt(tokens.remove());var result=new ArrayList<Operation>();
        for(int i=0;i<count;i++){
            tokens.remove();String type=tokens.remove();var d=definitions.get(type);
            if(d==null||!Set.of("math","unitconvert","multipleinput","instancecontext","osc","instancecombine","digitalnoise","noise","formula").contains(type))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Unsupported scalar kernel"));
            if(!tokens.remove().equals("!"+NativeContractGenerator.fingerprint(d)))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Unknown contract"));
            var fields=new HashMap<String,String>();var sources=new ArrayList<Integer>();
            for(var f:d.processor().contract().fields()){
                if(f.kind()==ParameterContract.Kind.CURVE)throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Variable field"));
                String value=tokens.remove();
                if(f.kind()==ParameterContract.Kind.TOKEN){if(!value.equals(f.literal()))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Token mismatch"));}
                else fields.put(NativeContractGenerator.fieldName(f),value);
                if(f.kind()==ParameterContract.Kind.SOURCE)sources.add(Integer.parseInt(value));
            }
            if(!tokens.remove().equals("@"))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Missing rates"));tokens.remove();tokens.remove();
            if("~".equals(tokens.peek())){tokens.remove();while(!"~".equals(tokens.remove())){} }
            result.add(new Operation(type,fields,sources));
        }
        if(!tokens.isEmpty())throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Trailing program data"));
        return result;
    }
    private static String scalarKernel(String program){
        try{
            var ops=parse(program);var scalarOrder=new ArrayList<Integer>();var visited=new HashSet<Integer>();var visiting=new HashSet<Integer>();
            var stores=new LinkedHashSet<Integer>();
            for(int i=0;i<ops.size();i++)if(ops.get(i).type.equals("osc")||ops.get(i).type.equals("instancecombine"))
                for(int ref:ops.get(i).sources)if(ref>=0&&!ops.get(ref%256).type.equals("osc")){visit(ref%256,ops,visited,visiting,scalarOrder);stores.add(ref);}
            var dynamic=new LinkedHashSet<Integer>();
            for(int i:scalarOrder){var op=ops.get(i);
                if(Set.of("digitalnoise","noise").contains(op.type)||op.sources.stream().anyMatch(ref->ref>=0&&dynamic.contains(ref%256)))dynamic.add(i);
            }
            var audioBody=new StringBuilder();
            for(int i:dynamic){var op=ops.get(i);
                String type=switch(op.type){case "digitalnoise"->"DigitalNoiseNode";case "noise"->"NoiseNode";case "math"->"MathNode";case "unitconvert"->"UnitConverterNode";default->throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Unsupported modulator"));};
                audioBody.append("        renderUpstream(").append(i).append(",frames,[](NodeProcessor& p,ProcessContext& c){static_cast<").append(type).append("&>(p).").append(type).append("::process(c);});\n");
                for(int ref:op.sources)if(ref>=0&&!dynamic.contains(ref%256))stores.add(ref);
            }
            stores.removeIf(ref->dynamic.contains(ref%256));
            scalarOrder.removeIf(dynamic::contains);
            if(scalarOrder.isEmpty()&&dynamic.isEmpty())return null;
            var guard=new StringBuilder();var body=new StringBuilder();var keys=new ArrayList<String>();
            keys.add("g.preparedSampleRate");
            keys.add("double(frames)");
            for(int i:scalarOrder){var op=ops.get(i);String p="p"+i;
                guard.append("        if(!g.nodes[").append(i).append("].controlRate){scalarCacheValid=false;Instance::renderScalars(frames);return;}\n");
                if(op.type.equals("multipleinput"))keys.add("([&]{const auto& v=static_cast<const BakedInput&>(*g.nodes["+i+"].processor);return double(v.external&&v.external->input("+op.value("slot")+")?v.external->input("+op.value("slot")+")[0]:v.fallback);}())");
                if(op.type.equals("instancecontext")){keys.add("double(static_cast<const InstanceContextNode&>(*g.nodes["+i+"].processor).count)");keys.add("double(static_cast<const InstanceContextNode&>(*g.nodes["+i+"].processor).index)");}
                switch(op.type){
                    case "formula" -> {
                        guard.append("        auto& ").append(p).append("=static_cast<FormulaNode&>(*g.nodes[").append(i).append("].processor);\n")
                            .append("        if(").append(p).append(".sampleContext||").append(p).append(".bufferInputs||").append(p).append(".triggerInputs||").append(p).append(".triggerOutputs){scalarCacheValid=false;Instance::renderScalars(frames);return;}\n");
                        for(int port=0;port<FormulaNode.INPUTS;port++)keys.add("double("+p+".fallback["+port+"])");
                        for(int ref:op.sources)if(ref>=0)body.append("        g.nodes[").append(ref%256).append("].voice(0).channelCache[0].outputs[").append(ref/256).append("][0]=").append(variable(ref)).append(";\n");
                        body.append("        renderUpstream(").append(i).append(",frames,[](NodeProcessor& p,ProcessContext& c){p.process(c);});\n");
                        var payload=new String(HexFormat.of().parseHex(op.value("program")),StandardCharsets.UTF_8).split("\\s+");
                        int outputs=Integer.parseInt(payload[2]);
                        for(int port=0;port<outputs;port++)body.append("        const float n").append(i).append('p').append(port).append("=g.nodes[").append(i).append("].voice(0).channelCache[0].outputs[").append(port).append("][0];\n");
                    }
                    case "multipleinput" -> body.append("        const auto& ").append(p).append("=static_cast<const BakedInput&>(*g.nodes[").append(i).append("].processor);\n")
                        .append("        const float n").append(i).append("p0=").append(p).append(".external&&").append(p).append(".external->input(").append(op.value("slot")).append(")?").append(p).append(".external->input(").append(op.value("slot")).append(")[0]:").append(p).append(".fallback;\n");
                    case "instancecontext" -> {
                        // Reuse native context semantics (including its deterministic seed).
                        body.append("        float values").append(i).append("[4]{};float* outputs").append(i).append("[]={&values").append(i).append("[0],&values").append(i).append("[1],&values").append(i).append("[2],&values").append(i).append("[3]};\n")
                            .append("        ProcessContext context").append(i).append("{nullptr,0,values").append(i).append(",1,g.preparedSampleRate,g.silence.data()};context").append(i).append(".outputs=outputs").append(i).append(";context").append(i).append(".outputCount=4;\n")
                            .append("        static_cast<InstanceContextNode&>(*g.nodes[").append(i).append("].processor).InstanceContextNode::process(context").append(i).append(");\n");
                        for(int port=0;port<4;port++)body.append("        const float n").append(i).append('p').append(port).append("=values").append(i).append('[').append(port).append("];\n");
                    }
                    case "math" -> {
                        guard.append("        const auto& ").append(p).append("=static_cast<const MathNode&>(*g.nodes[").append(i).append("].processor);\n");
                        for(String input:List.of("a","b"))if(op.source(input)<0)
                            guard.append("        if(").append(p).append(input.equals("a")?".currentA!=":".currentB!=").append(number(op.value(input))).append("){scalarCacheValid=false;Instance::renderScalars(frames);return;}\n");
                        body.append("        const float n").append(i).append("p0=memo->evaluate(").append(i).append(',').append(input(op,"a")).append(',').append(input(op,"b")).append(",g.preparedSampleRate,[&]{return MathNode::evaluate(").append(input(op,"a")).append(',').append(input(op,"b"))
                            .append(",MathOperation::").append(identifier(op.value("operation"))).append(',').append(op.value("clamp").equals("1")?"true":"false").append(");});\n");
                    }
                    case "unitconvert" -> body.append("        const float n").append(i).append("p0=UnitConverterNode::evaluate(").append(input(op,"value"))
                        .append(",UnitFormat::").append(identifier(op.value("from"))).append(",UnitFormat::").append(identifier(op.value("to"))).append(",g.preparedSampleRate);\n");
                    default -> throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Non-scalar dependency"));
                }
            }
            for(int ref:stores)body.append("        g.nodes[").append(ref%256).append("].voice(0).channelCache[0].outputs[").append(ref/256).append("][0]=").append(variable(ref)).append(";\n");
            return "    bool scalarCacheValid=false;std::array<double,"+keys.size()+"> scalarKeys{};\n"
                +"    void renderScalars(int frames)noexcept{auto& g=runtime->graph;\n"+guard
                +"        const std::array<double,"+keys.size()+"> keys{"+String.join(",",keys)+"};\n"
                +"        if(!scalarCacheValid||keys!=scalarKeys){scalarKeys=keys;scalarCacheValid=true;\n"+body+"        }\n"+audioBody+"    }\n";
        }catch(IllegalArgumentException|NoSuchElementException e){return null;}
    }
    private static void visit(int i,List<Operation> ops,Set<Integer> done,Set<Integer> visiting,List<Integer> order){
        if(done.contains(i))return;if(!visiting.add(i))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Feedback"));
        for(int ref:ops.get(i).sources)if(ref>=0)visit(ref%256,ops,done,visiting,order);
        visiting.remove(i);done.add(i);order.add(i);
    }
    private static String variable(int ref){return "n"+(ref%256)+"p"+(ref/256);}
    private static String input(Operation op,String id){int ref=op.source(id);return ref<0?number(op.value(id)):variable(ref);}
    private static String number(String text){float value=Float.parseFloat(text);if(!Float.isFinite(value))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Non-finite constant"));return Float.toString(value)+"f";}
    private static String identifier(String value){if(!value.matches("[a-z][a-z_]*"))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid identifier"));return value;}
}
