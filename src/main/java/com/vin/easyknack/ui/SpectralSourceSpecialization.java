package com.vin.easyknack.ui;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

/** Emits typed calls in a single FFT-bin loop, sharing the native node kernels. */
final class SpectralSourceSpecialization {
    private record Step(String type,String nativeType,List<Integer> sources,Map<String,String> values) {}
    private record Program(String hex,List<Step> steps) {}
    private static final Set<String> SUPPORTED=Set.of("formula","binmemory","binhold","bintrigger","bincontext","binread","binrandom","binuniform","binoutput","bindiscard","instancecontext","math","comparator","maprange","mix","clamp","unitconvert","value","curve");
    record Result(String type,String code){}
    static Result emit(String name,List<GLGraphNode> nodes,Set<GLNodeId> dynamic){
        var programs=new LinkedHashMap<String,Program>();
        for(var n:nodes)if(NodeRegistry.require(n.typeId()) instanceof SpectralGraphNode&&!dynamic.contains(n.id())){
            var hex=new NodeState(n.properties()).text("program","");var program=parse(hex);
            if(program!=null)programs.putIfAbsent(hex,program);
        }
        if(programs.isEmpty())return new Result("SpectralGraphNode","");
        String type=name+"SpectralKernel";
        var s=new StringBuilder("struct ").append(type).append(":SpectralGraphNode {\n    int kernel=-1;\n    void configure(std::istream& in)override{SpectralGraphNode::configure(in);kernel=-1;\n");
        int index=0;for(var p:programs.values())s.append("        if(programIdentity==").append(quote(p.hex())).append(")kernel=").append(index++).append(";\n");
        s.append("    }\n    std::unique_ptr<SpectralGraphNode> makeFrameWorker()const override{return std::make_unique<").append(type).append(">();}\n    void processBinRange(const SpectrumStream& source,const StreamSpectrumFrame& input,StreamSpectrumFrame& frame,ProcessContext& c,std::size_t offset,std::size_t begin,std::size_t end)noexcept override{\n        switch(kernel){\n");
        index=0;for(var p:programs.values()){
            boolean[] invariant=new boolean[p.steps().size()],condition=new boolean[p.steps().size()];
            for(int i=0;i<p.steps().size();i++){
                var step=p.steps().get(i);invariant[i]=!Set.of("formula","binmemory","binhold","bincontext","binread","binrandom","binoutput","bindiscard","instancecontext").contains(step.type());
                for(int ref:step.sources())if(ref>=0&&!invariant[ref%256])invariant[i]=false;
                if(step.type().equals("bindiscard"))markCondition(i,p.steps(),condition);
            }
            s.append("        case ").append(index++).append(":{\n");
            for(int i=0;i<p.steps().size();i++)s.append("            auto& s").append(i).append("=steps[").append(i).append("];auto& p").append(i).append("=static_cast<").append(p.steps().get(i).nativeType()).append("&>(*s").append(i).append(".processor);\n");
            s.append("            for(std::size_t k=begin;k<end;k++){\n                if(skipEmptyBins&&input.bins[k]==std::complex<double>{}){rejectBin(frame,k);continue;}\n");
            for(int i=0;i<p.steps().size();i++)if(!invariant[i]&&condition[i])append(s,p.steps().get(i),i);
            s.append("                if(discard>=0&&static_cast<BinDiscardNode&>(*steps[discard].processor).discarded){rejectBin(frame,k);continue;}\n");
            for(int i=0;i<p.steps().size();i++)if(!invariant[i]&&!condition[i])append(s,p.steps().get(i),i);
            s.append("                frame.bins[k]=result?result->value:std::complex<double>{};\n            }return;\n        }\n");
        }
        return new Result(type,s.append("        default:SpectralGraphNode::processBinRange(source,input,frame,c,offset,begin,end);\n        }\n    }\n};\n").toString());
    }
    private static void markCondition(int i,List<Step> steps,boolean[] marked){if(marked[i])return;marked[i]=true;for(int ref:steps.get(i).sources())if(ref>=0)markCondition(ref%256,steps,marked);}
    private static void append(StringBuilder s,Step step,int i){
        s.append("                {");
        if(step.type().startsWith("bin")&&!Set.of("binoutput","bindiscard").contains(step.type()))s.append("p").append(i).append(".bin=k;");
        if(step.type().equals("formula"))s.append("p").append(i).append(".bin=k;"); if(step.type().equals("instancecontext"))s.append("p").append(i).append(".index=k;");
        if(step.type().equals("math")){
            // Parse at export time, but evaluate via the same native arithmetic implementation.
            s.append("p").append(i).append(".processKernel(s").append(i).append(".scalar,MathOperation::").append(step.values().get("operation")).append(',').append(step.values().get("clamp")).append("!=0);");
        }else s.append("p").append(i).append('.').append(step.nativeType()).append("::process(s").append(i).append(".scalar);");
        s.append("}\n");
    }
    private static Program parse(String hex){
        try{
            var tokens=new String(HexFormat.of().parseHex(hex),StandardCharsets.UTF_8).strip().split("\\s+");
            int at=0;String version=tokens[at++];if(!Set.of("BIN1","BIN2").contains(version))return null;
            int count=Integer.parseInt(tokens[at++]);if(version.equals("BIN2"))at++;
            var definitions=new HashMap<String,BasicNode>();for(var d:NodeRegistry.builtinDefinitions())definitions.put(d.processor().id(),d);
            var steps=new ArrayList<Step>();
            for(int i=0;i<count;i++){
                String type=tokens[at++];if(!SUPPORTED.contains(type))return null;var d=definitions.get(type);
                if(d==null||!tokens[at++].equals("!"+NativeContractGenerator.fingerprint(d)))return null;
                var refs=new ArrayList<Integer>();var values=new HashMap<String,String>();
                for(var field:d.processor().contract().fields()){
                    if(field.kind()==ParameterContract.Kind.CURVE){int points=Integer.parseInt(tokens[at++]);if(points<2||points>64)return null;at+=points*7;continue;}
                    String value=tokens[at++];
                    if(field.kind()==ParameterContract.Kind.SOURCE)refs.add(Integer.valueOf(value));
                    else if(field.id()!=null)values.put(field.id().value(),value);
                }
                for(int ref:refs)if(ref>=0&&(ref%256>=i||ref>=2048))return null;
                if(type.equals("math")&&Arrays.stream(com.vin.easyknack.model.MathOperation.values()).noneMatch(op->op.id().equals(values.get("operation"))))return null;
                steps.add(new Step(type,type.equals("formula")?"BinFormulaNode":Path.of(d.processor().header()).getFileName().toString().replace(".hpp",""),refs,values));
            }
            return at==tokens.length?new Program(hex,steps):null;
        }catch(IllegalArgumentException|IndexOutOfBoundsException e){return null;}
    }
    private static String quote(String text){return "\""+text.replace("\\","\\\\").replace("\"","\\\"")+"\"";}
}
