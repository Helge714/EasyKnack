#pragma once
#include "../Partials.hpp"
#include "../NodeProcessor.hpp"
#include "ParameterContracts.hpp"
#include "MathNode.hpp"
#include "MapRangeNode.hpp"
#include "ClampNode.hpp"
#include "ComparatorNode.hpp"
#include "MixNode.hpp"
#include "CurveNode.hpp"
#include "UnitConverterNode.hpp"
#include "ValueNode.hpp"
#include "DigitalNoiseNode.hpp"
#include "NoiseNode.hpp"
#include "FormulaNode.hpp"
#include "PartialNoiseNode.hpp"
#include <sstream>
namespace easyknack {
struct PartialContextNode : NodeProcessor {
    const Partial* partial=nullptr;std::size_t index=0;double time=0;
    std::size_t outputCount()const noexcept override{return 9;}
    void configure(std::istream& in)override{sources=parameters::PartialContextNodeParameters::read(in).sources();}
    void process(ProcessContext& c)noexcept override{
        const float values[]={partial?float(partial->frequency):0,partial?float(partial->amplitude):0,partial?float(partial->phase/partialTau):0,partial?float(partial->id):0,partial?float(partial->age):0,float(index),float(time),partial?partialSeed(partial->id):0,partial?float(partial->tonality):0};
        for(std::size_t i=0;i<9;i++)if(auto* out=c.outputAt(i))out[0]=values[i];
    }
};
struct PartialOutputNode : NodeProcessor {
    double frequency=440,amplitude=1,phase=0;float f=0,a=0,p=0;
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{const auto& o=static_cast<const PartialOutputNode&>(other);frequency=o.frequency;amplitude=o.amplitude;phase=o.phase;}
    void configure(std::istream& in)override{auto v=parameters::PartialOutputNodeParameters::read(in);sources=v.sources();frequency=v.frequency_value;amplitude=v.amplitude_value;phase=v.phase_value;}
    void process(ProcessContext& c)noexcept override{f=c.numericInput(0,float(frequency))[0];a=c.numericInput(1,float(amplitude))[0];p=c.numericInput(2,float(phase))[0];}
};
struct PartialUniformNode : NodeProcessor {
    std::size_t slot=0;float value=0;
    void configure(std::istream& in)override{auto p=parameters::PartialUniformNodeParameters::read(in);slot=std::size_t(p.slot_value);if(slot>255)throw std::runtime_error("Invalid Partial Group input");}
    void process(ProcessContext& c)noexcept override{c.output[0]=value;}
};
struct PartialGraphNode : NodeProcessor {
    PartialsResource stream;
    struct Step {std::string type,parameters;std::unique_ptr<NodeProcessor> initial;std::size_t offset=0;};
    struct Evaluation {std::unique_ptr<NodeProcessor> processor;std::vector<const float*> inputs;std::vector<float*> outputs;std::vector<char> rates;};
    struct Voice {std::uint64_t id=0;std::vector<float> values;std::vector<Evaluation> steps;};
    std::vector<Step> steps;std::vector<Voice> voices;std::size_t slots=0,result=0;
    std::uint64_t identity=0;std::string shape;
    static std::unique_ptr<NodeProcessor> factory(const std::string& id){
        if(id=="partialnoise")return std::make_unique<PartialNoiseNode>();
        if(id=="partialcontext")return std::make_unique<PartialContextNode>();if(id=="partialoutput")return std::make_unique<PartialOutputNode>();if(id=="partialuniform")return std::make_unique<PartialUniformNode>();
        if(id=="math")return std::make_unique<MathNode>();if(id=="maprange")return std::make_unique<MapRangeNode>();if(id=="clamp")return std::make_unique<ClampNode>();if(id=="comparator")return std::make_unique<ComparatorNode>();
        if(id=="mix")return std::make_unique<MixNode>();if(id=="curve")return std::make_unique<CurveNode>();if(id=="unitconvert")return std::make_unique<UnitConverterNode>();if(id=="value")return std::make_unique<ValueNode>();
        if(id=="digitalnoise")return std::make_unique<DigitalNoiseNode>();if(id=="noise")return std::make_unique<NoiseNode>();if(id=="formula")return std::make_unique<FormulaNode>();
        throw std::runtime_error("Unsupported Partial Group processor: "+id);
    }
    char executionRate()const noexcept override{return 'a';}bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t port)const noexcept override{return port==0?ResourceKind::partials:ResourceKind::none;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::partials;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &stream;}
    std::string parameterStructure()const override{return shape;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{
        const auto& o=static_cast<const PartialGraphNode&>(other);if(shape!=o.shape)return;
        for(std::size_t s=0;s<steps.size();s++)if(steps[s].initial->supportsLiveParameters()){
            steps[s].initial->applyParameters(*o.steps[s].initial);for(auto& v:voices)v.steps[s].processor->applyParameters(*o.steps[s].initial);
        }
    }
    void configure(std::istream& in)override{
        auto p=parameters::PartialGraphNodeParameters::read(in);sources=p.sources();const auto& hex=p.program_value;
        if(hex.size()%2||hex.size()>1000000)throw std::runtime_error("Invalid Partial Group program");
        auto digit=[](char c){if(c>='0'&&c<='9')return c-'0';if(c>='a'&&c<='f')return c-'a'+10;throw std::runtime_error("Invalid Partial Group encoding");};
        std::string decoded;decoded.reserve(hex.size()/2);for(std::size_t i=0;i<hex.size();i+=2)decoded.push_back(char(digit(hex[i])*16+digit(hex[i+1])));
        std::istringstream program(decoded);std::string marker;std::size_t count;
        if(!(program>>marker>>count)||marker!="PAR1"||!count||count>64)throw std::runtime_error("Invalid Partial Group header");
        slots=0;steps.clear();steps.resize(count);shape.clear();std::size_t results=0;
        for(std::size_t i=0;i<count;i++){
            auto& s=steps[i];program>>s.type;auto begin=program.tellg();s.initial=factory(s.type);s.initial->configure(program);auto end=program.tellg();
            s.parameters=decoded.substr(std::size_t(begin),end<0?std::string::npos:std::size_t(end-begin));s.offset=slots;slots+=s.initial->outputCount();
            shape+=s.type+":"+s.initial->parameterStructure();
            if(!s.initial->supportsLiveParameters())shape+=s.parameters;
            for(int ref:s.initial->sources){if(ref>=0&&(std::size_t(ref%256)>=i||std::size_t(ref/256)>=steps[ref%256].initial->outputCount()))throw std::runtime_error("Cyclic or invalid Partial Group input");shape+=":"+std::to_string(ref);}
            if(s.type=="partialoutput"){result=i;++results;}
            if(auto* uniform=dynamic_cast<PartialUniformNode*>(s.initial.get()))if(uniform->slot+1>=sources.size())throw std::runtime_error("Missing Partial Group external input");
            if(auto* formula=dynamic_cast<FormulaNode*>(s.initial.get()))if(formula->bufferInputs||formula->triggerInputs||formula->triggerOutputs)throw std::runtime_error("Partial formulas accept numeric inputs only");
            shape+=';';
        }
        if(results!=1||program>>marker)throw std::runtime_error("Use exactly one Partial Output");
    }
    void prepare(double rate,std::size_t block)override{
        stream.prepare(block);voices.resize(stream.preparedCapacity);
        for(auto& s:steps)s.initial->prepare(rate,1);
        for(std::size_t slot=0;slot<voices.size();slot++){
            auto& voice=voices[slot];voice.values.resize(slots);voice.steps.resize(steps.size());
            for(std::size_t s=0;s<steps.size();s++){
                auto& v=voice.steps[s];v.processor=factory(steps[s].type);std::istringstream p(steps[s].parameters);v.processor->configure(p);v.processor->prepare(rate,1);
                v.processor->bindIdentity("partial/"+std::to_string(slot)+"/"+std::to_string(s));
                v.inputs.resize(v.processor->sources.size());v.rates.assign(v.inputs.size(),'c');v.outputs.resize(v.processor->outputCount());
                for(std::size_t j=0;j<v.inputs.size();j++){int ref=v.processor->sources[j];v.inputs[j]=ref<0?nullptr:&voice.values[steps[ref%256].offset+ref/256];}
                for(std::size_t j=0;j<v.outputs.size();j++)v.outputs[j]=&voice.values[steps[s].offset+j];
            }
        }
    }
    void inheritState(const NodeProcessor& p)noexcept override{
        const auto& o=static_cast<const PartialGraphNode&>(p);if(shape!=o.shape||voices.size()!=o.voices.size())return;identity=o.identity;
        for(std::size_t i=0;i<voices.size();i++){voices[i].id=o.voices[i].id;for(std::size_t s=0;s<steps.size();s++)voices[i].steps[s].processor->inheritState(*o.voices[i].steps[s].processor);}
    }
    void process(ProcessContext& c)noexcept override{
        const auto* input=static_cast<const PartialsResource*>(c.resource(0));if(!input||!input->size){stream.size=stream.count=0;identity=0;return;}
        if(!stream.copy(*input))return;if(identity!=input->identity){for(auto& v:voices)v.id=0;identity=input->identity;}
        float silence=0;
        for(std::size_t f=0;f<stream.count;f++){
            auto& frame=stream.frames[f];const auto offset=std::size_t(std::clamp<std::int64_t>(frame.start+std::int64_t(input->windowSize?input->windowSize:input->size)-std::int64_t(input->blockStart)-1,0,std::int64_t(c.bufferSize)-1));
            for(std::size_t i=0;i<frame.count;i++){
                auto& partial=frame.values[i];if(partial.slot>=voices.size())continue;auto& voice=voices[partial.slot];
                if(voice.id!=partial.id){for(std::size_t s=0;s<steps.size();s++)voice.steps[s].processor->inheritState(*steps[s].initial);voice.id=partial.id;}
                for(std::size_t s=0;s<steps.size();s++){
                    auto& v=voice.steps[s];
                    if(auto* context=dynamic_cast<PartialContextNode*>(v.processor.get())){context->partial=&partial;context->index=i;context->time=frame.start/input->sampleRate;}
                    if(auto* noise=dynamic_cast<PartialNoiseNode*>(v.processor.get())){noise->active=true;noise->partialId=partial.id;noise->frameStart=frame.start;}
                    if(auto* uniform=dynamic_cast<PartialUniformNode*>(v.processor.get()))uniform->value=c.numericInput(uniform->slot+1)[offset];
                    ProcessContext scalar{v.inputs.data(),v.inputs.size(),v.outputs[0],1,input->sampleRate/std::max<std::size_t>(1,input->hop),&silence};scalar.outputs=v.outputs.data();scalar.outputCount=v.outputs.size();scalar.sourceRates=v.rates.data();scalar.blockFrames=1;v.processor->process(scalar);
                }
                auto& out=static_cast<PartialOutputNode&>(*voice.steps[result].processor);
                // Preserve the exact analysis values on identity wires despite float scalar controls.
                if(out.f!=float(partial.frequency))partial.frequency=std::isfinite(out.f)?out.f:0;
                if(out.a!=float(partial.amplitude))partial.amplitude=std::isfinite(out.a)?std::max(0.f,out.a):0;
                if(out.p!=float(partial.phase/partialTau))partial.phase=std::isfinite(out.p)?out.p*partialTau:0;
            }
        }
    }
};
EASYKNACK_NODE(PartialContextNode,"partialcontext")
EASYKNACK_NODE(PartialOutputNode,"partialoutput")
EASYKNACK_NODE(PartialUniformNode,"partialuniform")
EASYKNACK_NODE(PartialGraphNode,"partialgraph")
}
