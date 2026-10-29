#pragma once
#include "BinContextNode.hpp"
#include "BinReadNode.hpp"
#include "BinRandomNode.hpp"
#include "BinUniformNode.hpp"
#include "BinTriggerNode.hpp"
#include "BinHoldNode.hpp"
#include "BinMemoryNode.hpp"
#include "BinFormulaNode.hpp"
#include "BinOutputNode.hpp"
#include "BinDiscardNode.hpp"
#include "OscillatorNode.hpp"
#include "NoiseNode.hpp"
#include "DigitalNoiseNode.hpp"
#include "InstanceContextNode.hpp"
#include "InstanceCombineNode.hpp"
#include "MathNode.hpp"
#include "ComparatorNode.hpp"
#include "MapRangeNode.hpp"
#include "MixNode.hpp"
#include "CurveNode.hpp"
#include "ClampNode.hpp"
#include "UnitConverterNode.hpp"
#include "ValueNode.hpp"
#include "../SpectralWorker.hpp"
#include <sstream>
namespace easyknack {
/** FFT-frame values feed persistent per-bin audio voices; Combine ends the bin domain. */
struct SpectralGraphNode : NodeProcessor {
    struct Step {
        std::unique_ptr<NodeProcessor> processor;
        std::array<float,8> values{};std::array<float*,8> outputs{};
        std::vector<const float*> inputs;std::vector<char> rates;std::string type,parameters;
        bool audio=false,condition=false,invariant=false;
        BinAccess* access=nullptr;InstanceContextNode* instance=nullptr;BinUniformNode* uniform=nullptr;
        float silence=0;
        ProcessContext scalar{};
        void (*evaluate)(NodeProcessor&,ProcessContext&)noexcept=nullptr;
    };
    template<class T> static void evaluateScalar(NodeProcessor& node,ProcessContext& c)noexcept{static_cast<T&>(node).T::process(c);}
    template<class T> static void bindScalar(Step& s){s.evaluate=&evaluateScalar<T>;}
    // Resolve types once on the control thread, never inside the bin loop.
    static void bindScalar(Step& s){
        if(s.type=="bincontext")bindScalar<BinContextNode>(s);
        else if(s.type=="binread")bindScalar<BinReadNode>(s);
        else if(s.type=="binrandom")bindScalar<BinRandomNode>(s);
        else if(s.type=="binuniform")bindScalar<BinUniformNode>(s);
        else if(s.type=="bintrigger")bindScalar<BinTriggerNode>(s);
        else if(s.type=="binhold")bindScalar<BinHoldNode>(s);
        else if(s.type=="binmemory")bindScalar<BinMemoryNode>(s); else if(s.type=="formula")bindScalar<BinFormulaNode>(s);
        else if(s.type=="binoutput")bindScalar<BinOutputNode>(s);
        else if(s.type=="bindiscard")bindScalar<BinDiscardNode>(s);
        else if(s.type=="instancecontext")bindScalar<InstanceContextNode>(s);
        else if(s.type=="math")bindScalar<MathNode>(s);
        else if(s.type=="comparator")bindScalar<ComparatorNode>(s);
        else if(s.type=="maprange")bindScalar<MapRangeNode>(s);
        else if(s.type=="mix")bindScalar<MixNode>(s);
        else if(s.type=="curve")bindScalar<CurveNode>(s);
        else if(s.type=="clamp")bindScalar<ClampNode>(s);
        else if(s.type=="unitconvert")bindScalar<UnitConverterNode>(s);
        else if(s.type=="value")bindScalar<ValueNode>(s);
    }
    std::vector<std::size_t> conditionSteps,valueSteps,invariantSteps;
    bool skipEmptyBins=false;
    bool statefulBins=false;
    bool parallelBins=true,workerCopy=false;
    std::shared_ptr<SpectralWorker> worker;
    std::unique_ptr<SpectralGraphNode> frameWorker;
    std::uint64_t parallelFrames=0;
    static constexpr std::size_t parallelWorkThreshold=8192;
    virtual std::unique_ptr<SpectralGraphNode> makeFrameWorker()const{return std::make_unique<SpectralGraphNode>();}
    SpectrumStream stream;std::vector<Step> steps;BinOutputNode* result=nullptr;
    struct Voice {std::vector<std::array<float,8>> values;std::vector<std::unique_ptr<NodeProcessor>> processors;std::array<float,7> weights{};float gain=0,target=0;bool valid=false;std::uint64_t lastTime=0;};
    std::vector<Voice> voices;std::vector<std::size_t> combines;int discard=-1;
    std::vector<std::size_t> activeVoices;
    std::array<std::array<float,256>,7> normalization{};
    std::array<float,256> activity{};
    std::vector<std::array<std::array<float,256>,8>> audioBuffers;
    std::size_t activeBins=0;std::uint64_t voiceIdentity=0;float fadeStep=1,weightSmoothing=1;
    std::string programIdentity;
    std::string logicalIdentity="spectral";
    std::size_t normalizedWindow=0;SpectrumWindow normalizedShape=SpectrumWindow::hann;double windowSum=1;
    void bindIdentity(const std::string& id)override{
        logicalIdentity=id;
        for(std::size_t s=0;s<steps.size();s++)steps[s].processor->bindIdentity(id+"/"+std::to_string(s));
        for(auto& voice:voices)for(std::size_t s=0;s<voice.processors.size();s++)
            if(voice.processors[s])voice.processors[s]->bindIdentity(id+"/"+std::to_string(s));
    }
    void shareChannelState(NodeProcessor& other)override{bindIdentity(static_cast<SpectralGraphNode&>(other).logicalIdentity);}
    std::uint64_t renderedBinSamples=0,discardedBinFrames=0;
    std::size_t outputCount()const noexcept override{return 1+combines.size();}
    static constexpr std::size_t uniformCount=16,historyCapacity=131072;
    std::array<std::vector<float>,uniformCount> history;
    std::array<bool,uniformCount> used{};
    std::array<bool,uniformCount> triggerUsed{};
    std::uint64_t historyBegin=0,historyEnd=0,historyIdentity=0;
    bool historyValid=false;
    bool supportsScalarInputs()const noexcept override{return true;}
    static std::string encodeProgram(const std::string& program){
        static constexpr char digits[]="0123456789abcdef";std::string result;result.reserve(program.size()*2);
        for(unsigned char c:program){result.push_back(digits[c>>4]);result.push_back(digits[c&15]);}return result;
    }
    char executionRate()const noexcept override{return 'a';}
    bool canShareChannels()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t port)const noexcept override{return port==0?ResourceKind::spectrum:ResourceKind::none;}
    ResourceKind outputResourceKind(std::size_t port)const noexcept override{return port==0?ResourceKind::spectrum:ResourceKind::none;}
    DspResource* resourceOutput(std::size_t port)noexcept override{return port==0?&stream:nullptr;}
    static std::unique_ptr<NodeProcessor> processor(const std::string& type){
        if(type=="binhold")return std::make_unique<BinHoldNode>();
        if(type=="binmemory")return std::make_unique<BinMemoryNode>(); if(type=="formula")return std::make_unique<BinFormulaNode>();
        if(type=="bintrigger")return std::make_unique<BinTriggerNode>();
        if(type=="bindiscard")return std::make_unique<BinDiscardNode>();
        if(type=="instancecontext")return std::make_unique<InstanceContextNode>();
        if(type=="instancecombine")return std::make_unique<InstanceCombineNode>();
        if(type=="osc")return std::make_unique<OscillatorNode>();
        if(type=="noise")return std::make_unique<NoiseNode>();
        if(type=="digitalnoise")return std::make_unique<DigitalNoiseNode>();
        if(type=="binuniform")return std::make_unique<BinUniformNode>();
        if(type=="binrandom")return std::make_unique<BinRandomNode>();
        if(type=="bincontext")return std::make_unique<BinContextNode>();if(type=="binread")return std::make_unique<BinReadNode>();if(type=="binoutput")return std::make_unique<BinOutputNode>();
        if(type=="comparator")return std::make_unique<ComparatorNode>();
        if(type=="math")return std::make_unique<MathNode>();if(type=="maprange")return std::make_unique<MapRangeNode>();if(type=="mix")return std::make_unique<MixNode>();if(type=="curve")return std::make_unique<CurveNode>();if(type=="clamp")return std::make_unique<ClampNode>();if(type=="unitconvert")return std::make_unique<UnitConverterNode>();if(type=="value")return std::make_unique<ValueNode>();
        throw std::runtime_error("Unsupported spectral bin processor: "+type);
    }
    void configure(std::istream& in)override{
        auto start=in.tellg();std::string marker,hex;in>>marker;
        if(marker=="!62017619f30a4e31"){
            int source;if(!(in>>source>>hex)||source< -1||source>=2048)throw std::runtime_error("Invalid legacy spectral parameters");sources={source};
        }else{in.seekg(start);auto p=parameters::SpectralGraphNodeParameters::read(in);sources=p.sources();hex=p.program_value;}
        steps.clear();result=nullptr;used.fill(false);triggerUsed.fill(false);statefulBins=false;combines.clear();discard=-1;programIdentity=hex;
        if(hex.size()>2000000||hex.size()%2)throw std::runtime_error("Invalid bin program");std::string decoded;decoded.reserve(hex.size()/2);
        auto digit=[](char c)->unsigned{if(c>='0'&&c<='9')return c-'0';if(c>='a'&&c<='f')return c-'a'+10;throw std::runtime_error("Invalid bin program encoding");};
        for(std::size_t i=0;i<hex.size();i+=2)decoded.push_back(static_cast<char>(digit(hex[i])*16+digit(hex[i+1])));
        std::istringstream program(decoded);std::size_t count;
        if(!(program>>marker>>count)||(marker!="BIN1"&&marker!="BIN2")||!count||count>256)throw std::runtime_error("Invalid bin program header");
        skipEmptyBins=false;parallelBins=true;
        if(marker=="BIN2"){unsigned flags;if(!(program>>flags)||flags>3)throw std::runtime_error("Invalid bin program flags");skipEmptyBins=(flags&1)!=0;parallelBins=(flags&2)==0;}
        steps.resize(count);
        for(std::size_t i=0;i<count;i++){
            std::string type;if(!(program>>type))throw std::runtime_error("Missing bin processor");auto& step=steps[i];step.type=type;
            auto begin=program.tellg();step.processor=processor(type);step.processor->configure(program);auto end=program.tellg();
            step.parameters=decoded.substr(std::size_t(begin),end<0?std::string::npos:std::size_t(end-begin));
            step.audio=type=="osc"||type=="noise"||type=="digitalnoise"||type=="instancecombine";
            if(type=="osc"&&step.processor->sources.size()>1&&step.processor->sources[1]>=0)throw std::runtime_error("Custom wavetable connections are not scalar bin values");
            if(type=="instancecombine")combines.push_back(i);
            if(type=="bindiscard"){if(discard>=0)throw std::runtime_error("Use one Bin Discard");discard=int(i);}
            if(type=="binhold"||type=="binmemory")statefulBins=true; if(auto* formula=dynamic_cast<BinFormulaNode*>(step.processor.get()))statefulBins|=formula->hasState()||formula->triggerInputs!=0;
            if(auto* uniform=dynamic_cast<BinUniformNode*>(step.processor.get())){used[uniform->slot]=true;triggerUsed[uniform->slot]=uniform->trigger;}
            if(auto* out=dynamic_cast<BinOutputNode*>(step.processor.get())){if(result)throw std::runtime_error("Multiple bin outputs");result=out;}
            step.inputs.resize(step.processor->sources.size());step.rates.assign(step.inputs.size(),'c');for(std::size_t port=0;port<8;port++)step.outputs[port]=&step.values[port];
            for(std::size_t port=0;port<step.inputs.size();port++){int ref=step.processor->sources[port];if(ref<0)continue;
                auto node=static_cast<std::size_t>(ref%256),output=static_cast<std::size_t>(ref/256);if(node>=i||output>=steps[node].processor->outputCount())throw std::runtime_error("Invalid/cyclic bin input");step.inputs[port]=&steps[node].values[output];step.audio|=steps[node].audio;
                if(steps[node].type=="instancecombine")throw std::runtime_error("Use shared processing outside the spectral group after Instance Combine");}
            if((type=="bindiscard"||type=="binoutput"||type=="binhold"||type=="binmemory"||type=="formula")&&step.audio)throw std::runtime_error("Frame processors cannot depend on temporal audio; use FFT-frame values");
        }
        if((!result&&combines.empty())||combines.size()>7||program>>marker)throw std::runtime_error("Invalid bin program output");
        // A held bin can remain nonzero after its live input becomes empty.
        if(statefulBins)skipEmptyBins=false;
        if(discard>=0)markCondition(std::size_t(discard));
        for(auto& step:steps)step.outputs.fill(nullptr);
        for(auto& step:steps)for(int ref:step.processor->sources)if(ref>=0)steps[ref%256].outputs[ref/256]=&steps[ref%256].values[ref/256];
        conditionSteps.clear();valueSteps.clear();invariantSteps.clear();
        for(std::size_t i=0;i<steps.size();i++){
            auto& s=steps[i];s.access=dynamic_cast<BinAccess*>(s.processor.get());
            s.instance=dynamic_cast<InstanceContextNode*>(s.processor.get());s.uniform=dynamic_cast<BinUniformNode*>(s.processor.get());
            if(s.audio)continue;
            bindScalar(s);
            s.scalar={s.inputs.data(),s.inputs.size(),s.values.data(),1,48000,&s.silence,1};
            s.scalar.sourceRates=s.rates.data();s.scalar.outputs=s.outputs.data();s.scalar.outputCount=s.processor->outputCount();
            // Bin values are independent evaluations, not a time series to smooth.
            s.scalar.legacyEventEvaluation=true;
            s.invariant=(!s.access||s.uniform)&&!s.instance&&s.type!="binoutput"&&s.type!="bindiscard"&&s.type!="formula";
            for(int ref:s.processor->sources)if(ref>=0&&!steps[ref%256].invariant)s.invariant=false;
            (s.invariant?invariantSteps:s.condition?conditionSteps:valueSteps).push_back(i);
        }
        if(!combines.empty()&&steps.size()>64)throw std::runtime_error("Spectral audio program exceeds prepared state budget (64 nodes)");
    }
    void markCondition(std::size_t index){auto& step=steps[index];if(step.condition)return;step.condition=true;for(int ref:step.processor->sources)if(ref>=0)markCondition(std::size_t(ref%256));}
    void prepare(double rate,std::size_t block)override{
        for(auto& step:steps)step.processor->prepare(rate,1);
        if(workerCopy)return;
        stream.prepare(block);frameWorker.reset();worker.reset();
        if(parallelBins&&!statefulBins&&combines.empty()&&std::thread::hardware_concurrency()>1){
            frameWorker=makeFrameWorker();frameWorker->workerCopy=true;
            std::istringstream parameters("!62017619f30a4e31 -1 "+programIdentity);
            frameWorker->configure(parameters);frameWorker->prepare(rate,block);
            try{worker=SpectralWorker::shared();}catch(const std::system_error&){frameWorker.reset();}
        }
        for(std::size_t i=0;i<uniformCount;i++)if(used[i])history[i].assign(historyCapacity,0);
        historyValid=false;
        bindIdentity(logicalIdentity);
        voices.clear();audioBuffers.clear();
        activeBins=0;voiceIdentity=0;activeVoices.clear();activeVoices.reserve(SpectrumStream::maxSize/2+1);fadeStep=float(1/std::max(1.,.005*rate));weightSmoothing=float(1-std::exp(-1/std::max(1.,.005*rate)));
        if(!combines.empty()){
            audioBuffers.resize(steps.size());
            voices.resize(SpectrumStream::maxSize/2+1);
            for(std::size_t k=0;k<voices.size();k++){auto& voice=voices[k];voice.values.resize(steps.size());voice.processors.resize(steps.size());
                for(std::size_t s=0;s<steps.size();s++)if(steps[s].audio){auto p=processor(steps[s].type);std::istringstream in(steps[s].parameters);p->configure(in);p->bindIdentity(logicalIdentity+"/"+std::to_string(s));p->prepare(rate,block);voice.processors[s]=std::move(p);}}
        }
    }
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& old=static_cast<SpectralGraphNode&>(previous);
        if(used!=old.used||triggerUsed!=old.triggerUsed||sources!=old.sources)return;
        history.swap(old.history);historyBegin=old.historyBegin;historyEnd=old.historyEnd;historyIdentity=old.historyIdentity;historyValid=old.historyValid;
        bool layout=voices.size()==old.voices.size()&&steps.size()==old.steps.size();
        for(std::size_t i=0;layout&&i<steps.size();i++)layout=steps[i].type==old.steps[i].type&&steps[i].audio==old.steps[i].audio;
        if(layout){
            for(std::size_t s=0;s<steps.size();s++)if(steps[s].type=="binhold"||steps[s].type=="binmemory"||steps[s].type=="formula")steps[s].processor->adoptState(*old.steps[s].processor);
            if(programIdentity==old.programIdentity)voices.swap(old.voices);
            else for(std::size_t k=0;k<voices.size();k++){
                auto& v=voices[k];auto& previous=old.voices[k];v.values.swap(previous.values);v.weights=previous.weights;v.gain=previous.gain;v.target=previous.target;v.valid=previous.valid;v.lastTime=previous.lastTime;
                for(std::size_t s=0;s<steps.size();s++)if(v.processors[s])v.processors[s]->adoptState(*previous.processors[s]);
            }
            activeVoices.swap(old.activeVoices);activeBins=old.activeBins;voiceIdentity=old.voiceIdentity;
        }
    }
    void capture(const SpectrumStream& source,ProcessContext& c)noexcept{
        if(!historyValid||source.identity!=historyIdentity||source.blockStart!=historyEnd){historyBegin=source.blockStart;historyValid=true;}
        historyIdentity=source.identity;
        auto count=c.blockFrames?c.blockFrames:c.bufferSize;
        for(std::size_t port=0;port<uniformCount;port++)if(used[port]){
            auto input=c.numericInput(port+1);
            auto* events=triggerUsed[port]?c.events(port+1):nullptr;
            for(std::size_t i=0;i<count;i++)history[port][(source.blockStart+i)%historyCapacity]=events?0:input[i];
            if(events)for(const auto& event:*events)if(event.kind==ValueEventKind::trigger&&event.sampleOffset<count)
                history[port][(source.blockStart+event.sampleOffset)%historyCapacity]=1;
        }
        historyEnd=source.blockStart+count;
        if(historyEnd-historyBegin>historyCapacity)historyBegin=historyEnd-historyCapacity;
    }
    void frameStep(Step& step,const SpectrumStream& source,const StreamSpectrumFrame& frame,std::size_t bin,double rate)noexcept{
        if(step.access)step.access->bin=bin;
        if(step.instance)step.instance->index=bin; if(step.type=="formula")static_cast<BinFormulaNode&>(*step.processor).bin=bin;
        step.evaluate(*step.processor,step.scalar);
    }
    void beginFrame(const SpectrumStream& source,const StreamSpectrumFrame& frame,double rate)noexcept{
        for(auto& s:steps){
            if(s.access){s.access->frame=&frame;s.access->size=source.size;s.access->rate=source.sampleRate;s.access->windowSum=windowSum;}
            if(s.instance)s.instance->count=source.size/2+1;
            if(auto* hold=dynamic_cast<BinHoldNode*>(s.processor.get()))hold->beginFrame(frame.start+static_cast<std::int64_t>(source.windowFrames()));
            if(s.type=="formula")static_cast<BinFormulaNode&>(*s.processor).beginFrame(source,frame); s.scalar.sampleRate=rate;
        }
        for(auto i:invariantSteps)frameStep(steps[i],source,frame,0,rate);
    }
    // Pure frame programs may split disjoint bins; temporal voices retain serial state.
    virtual void processBins(const SpectrumStream& source,const StreamSpectrumFrame& input,StreamSpectrumFrame& frame,ProcessContext& c,std::size_t offset)noexcept{
        const auto count=source.size/2+1;
        if(frameWorker&&count>=512&&count*(conditionSteps.size()+valueSteps.size())>=parallelWorkThreshold){
            auto& other=*frameWorker;other.windowSum=windowSum;other.discardedBinFrames=0;
            for(std::size_t i=0;i<steps.size();i++)if(steps[i].uniform)other.steps[i].uniform->held=steps[i].uniform->held;
            other.beginFrame(source,input,c.sampleRate);
            struct Job {SpectralGraphNode* node;const SpectrumStream* source;const StreamSpectrumFrame* input;StreamSpectrumFrame* output;ProcessContext context;std::size_t offset,begin,end;};
            Job job{&other,&source,&input,&frame,c,offset,count/2,count};
            if(worker->submit([](void* p)noexcept{auto& j=*static_cast<Job*>(p);j.node->processBinRange(*j.source,*j.input,*j.output,j.context,j.offset,j.begin,j.end);},&job)){
                processBinRange(source,input,frame,c,offset,0,count/2);worker->join();
                discardedBinFrames+=other.discardedBinFrames;++parallelFrames;return;
            }
        }
        processBinRange(source,input,frame,c,offset,0,count);
    }
    // Bake emits this same range kernel, including for the separately prepared worker.
    virtual void processBinRange(const SpectrumStream& source,const StreamSpectrumFrame& input,StreamSpectrumFrame& frame,ProcessContext& c,std::size_t offset,std::size_t begin,std::size_t end)noexcept{
        for(std::size_t k=begin;k<end;k++){
            if(skipEmptyBins&&input.bins[k]==std::complex<double>{}){rejectBin(frame,k);continue;}
            for(auto i:conditionSteps)frameStep(steps[i],source,input,k,c.sampleRate);
            if(discard>=0&&static_cast<BinDiscardNode&>(*steps[discard].processor).discarded){rejectBin(frame,k);continue;}
            for(auto i:valueSteps)frameStep(steps[i],source,input,k,c.sampleRate);
            frame.bins[k]=result?result->value:std::complex<double>{};
            if(!voices.empty()){auto& voice=voices[k];advancePhase(voice,source.blockStart+offset,c.sampleRate);
                for(std::size_t s=0;s<steps.size();s++)if(!steps[s].audio)voice.values[s]=steps[s].values;
                voice.valid=true;voice.target=1;voice.lastTime=source.blockStart+offset;}
        }
    }
    void rejectBin(StreamSpectrumFrame& frame,std::size_t k)noexcept{frame.bins[k]={};++discardedBinFrames;if(!voices.empty())voices[k].target=0;}
    void advancePhase(Voice& voice,std::uint64_t time,double rate)noexcept{
        if(!voice.valid||time<=voice.lastTime)return;
        for(std::size_t i=0;i<steps.size();i++)if(auto* osc=dynamic_cast<OscillatorNode*>(voice.processors[i].get())){
            int ref=osc->sources.empty()?-1:osc->sources[0];
            if(ref>=0&&steps[ref%256].audio)continue; // Arbitrary temporal modulators retain paused state.
            double hz=ref<0?osc->frequency:voice.values[ref%256][ref/256];
            auto kernel=osc->prepareSample(hz,rate);osc->phase=OscillatorNode::wrap(osc->phase/OscillatorNode::tau+kernel.increment*double(time-voice.lastTime))*OscillatorNode::tau;
        }
        voice.lastTime=time;
    }
    void renderAudio(std::size_t begin,std::size_t end,ProcessContext& c)noexcept{
        if(begin>=end||combines.empty())return;
        std::fill(activity.begin()+begin,activity.begin()+end,0);
        for(std::size_t p=0;p<combines.size();p++)std::fill(normalization[p].begin()+begin,normalization[p].begin()+end,0);
        for(auto k:activeVoices){auto& voice=voices[k];
            advancePhase(voice,stream.blockStart+begin,c.sampleRate);
            auto count=end-begin;
            if(voice.target==0)count=std::min(count,std::size_t(std::ceil(voice.gain/fadeStep)));
            if(!count)continue;
            for(std::size_t s=0;s<steps.size();s++)if(steps[s].audio){
                auto& p=*voice.processors[s];std::array<const float*,8> inputs{};std::array<float*,8> outputs{};std::array<char,8> rates{};rates.fill('c');
                for(std::size_t port=0;port<p.sources.size();port++){int ref=p.sources[port];if(ref>=0){bool audio=steps[ref%256].audio;inputs[port]=audio?audioBuffers[ref%256][ref/256].data():&voice.values[ref%256][ref/256];rates[port]=audio?'a':'c';}}
                for(std::size_t port=0;port<p.outputCount();port++)outputs[port]=audioBuffers[s][port].data();
                std::array<float,256> ramp{},silence{};
                auto combine=std::find(combines.begin(),combines.end(),s);
                if(combine!=combines.end()&&rates[1]=='c'){
                    auto slot=std::size_t(combine-combines.begin());auto& node=static_cast<InstanceCombineNode&>(p);
                    float target=inputs[1]?inputs[1][0]:node.weight;target=std::isfinite(target)?std::max(0.f,target):0;
                    for(std::size_t i=0;i<count;i++){voice.weights[slot]+=weightSmoothing*(target-voice.weights[slot]);ramp[i]=voice.weights[slot];}
                    inputs[1]=ramp.data();rates[1]='a';
                }
                ProcessContext block{inputs.data(),p.sources.size(),outputs[0],count,c.sampleRate,silence.data(),count};block.outputs=outputs.data();block.outputCount=p.outputCount();block.sourceRates=rates.data();p.process(block);
                for(std::size_t port=0;port<p.outputCount();port++)voice.values[s][port]=outputs[port][count-1];
            }
            const float initialGain=voice.gain;
            for(std::size_t i=0;i<count;i++){
                auto sample=begin+i;
                voice.gain=voice.target>initialGain?std::min(voice.target,initialGain+float(i+1)*fadeStep):std::max(voice.target,initialGain-float(i+1)*fadeStep);
                activity[sample]+=voice.gain;
                for(std::size_t p=0;p<combines.size();p++){
                    auto index=combines[p];auto& combine=static_cast<InstanceCombineNode&>(*steps[index].processor);
                    if(auto* out=c.outputAt(p+1))out[sample]+=audioBuffers[index][0][i]*voice.gain;
                    normalization[p][sample]+=(combine.mode=="weighted"?audioBuffers[index][1][i]:1.f)*voice.gain;
                }
                voice.lastTime=stream.blockStart+sample+1;++renderedBinSamples;
            }
        }
        activeVoices.erase(std::remove_if(activeVoices.begin(),activeVoices.end(),[&](std::size_t k){return voices[k].target==0&&voices[k].gain==0;}),activeVoices.end());
        for(std::size_t p=0;p<combines.size();p++)if(static_cast<InstanceCombineNode&>(*steps[combines[p]].processor).mode!="sum")
            if(auto* out=c.outputAt(p+1))for(std::size_t i=begin;i<end;i++)out[i]=normalization[p][i]>0?out[i]/normalization[p][i]*std::min(1.f,activity[i]):0;
    }
    void process(ProcessContext& c)noexcept override{
        for(std::size_t p=1;p<outputCount();p++)if(auto* out=c.outputAt(p))std::fill_n(out,c.bufferSize,0);
        auto* source=static_cast<const SpectrumStream*>(c.resource(0));if(!source||!source->size||source->size>SpectrumStream::maxSize||c.bufferSize>256){stream.count=0;stream.size=0;activeVoices.clear();activeBins=0;voiceIdentity=0;return;}stream.metadata(*source);stream.count=0;
        if(normalizedWindow!=source->windowFrames()||normalizedShape!=source->window){normalizedWindow=source->windowFrames();normalizedShape=source->window;windowSum=0;for(std::size_t i=0;i<normalizedWindow;i++)windowSum+=spectrumWindowValue(normalizedShape,i,normalizedWindow);}
        if(!voices.empty()&&(voiceIdentity!=source->identity||activeBins!=source->size/2+1)){
            activeBins=source->size/2+1;voiceIdentity=source->identity;activeVoices.clear();for(auto& v:voices){v.valid=false;v.gain=v.target=0;}
        }
        std::size_t rendered=0;
        for(auto& step:steps){if(auto* hold=dynamic_cast<BinHoldNode*>(step.processor.get()))hold->beginBlock(*source);if(step.type=="formula")static_cast<BinFormulaNode&>(*step.processor).beginBlock(*source);}
        bool hasUniforms=std::any_of(used.begin(),used.end(),[](bool value){return value;});
        if(hasUniforms)capture(*source,c);
        for(std::size_t f=0;f<source->count;f++){
            if(hasUniforms){
                auto position=source->frames[f].start+static_cast<std::int64_t>(source->windowFrames())-1;
                // Never substitute current values for a delayed frame with missing history.
                if(position<0||static_cast<std::uint64_t>(position)<historyBegin||static_cast<std::uint64_t>(position)>=historyEnd)continue;
                for(auto& step:steps)if(auto* uniform=step.uniform){
                    uniform->held=history[uniform->slot][static_cast<std::uint64_t>(position)%historyCapacity];
                    if(uniform->trigger){
                        uniform->held=0;
                        auto begin=std::max<std::int64_t>(static_cast<std::int64_t>(historyBegin),position-static_cast<std::int64_t>(std::max<std::size_t>(1,source->hop))+1);
                        for(auto t=begin;t<=position;t++)if(history[uniform->slot][static_cast<std::uint64_t>(t)%historyCapacity]!=0){uniform->held=1;break;}
                    }
                }
            }
            if(stream.count>=stream.frames.size())break;
            auto ready=source->frames[f].start+static_cast<std::int64_t>(source->windowFrames());
            auto offset=std::size_t(std::clamp<std::int64_t>(ready-static_cast<std::int64_t>(source->blockStart),0,c.bufferSize));
            offset=std::max(rendered,offset);renderAudio(rendered,offset,c);rendered=offset;
            auto& frame=stream.frames[stream.count++];frame.start=source->frames[f].start;
            beginFrame(*source,source->frames[f],c.sampleRate);
            processBins(*source,source->frames[f],frame,c,offset);
            SpectrumStream::realSymmetry(frame,stream.size);
            if(!voices.empty()){activeVoices.clear();for(std::size_t k=0;k<activeBins;k++)if(voices[k].valid&&(voices[k].target>0||voices[k].gain>0))activeVoices.push_back(k);}
        }
        renderAudio(rendered,c.bufferSize,c);
    }
};
EASYKNACK_NODE(SpectralGraphNode,"spectralgraph")
}
