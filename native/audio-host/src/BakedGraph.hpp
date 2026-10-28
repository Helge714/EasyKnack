#pragma once
#include "GraphRuntime.hpp"
#include "ManualTriggers.hpp"

namespace easyknack {
/** Boundary processors are private implementation details of a source export. */
struct BakedInput : NodeProcessor {
    const ProcessContext* external=nullptr;
    bool triggerInput=false;ManualTriggerReceiver trigger;
    void shareChannelState(NodeProcessor& p)override{trigger=static_cast<BakedInput&>(p).trigger;}
    void inheritState(const NodeProcessor& p)noexcept override{trigger.seen=static_cast<const BakedInput&>(p).trigger.seen;}
    bool processesEventBlocks()const noexcept override{return triggerInput;}
    void processEventBlock(ProcessContext& c)noexcept override{process(c);}
    std::size_t port=0;float fallback=0;bool midi=false;ResourceKind resourceKind=ResourceKind::none;DspResource* resource=nullptr;
    void configure(std::istream& in) override { int kind;in>>port>>kind>>fallback;midi=kind==1;triggerInput=kind==5;resourceKind=kind==2?ResourceKind::history:kind==3?ResourceKind::analysis:kind==4?ResourceKind::spectrum:kind==6?ResourceKind::vector:kind==7?ResourceKind::waveform:ResourceKind::none;sources.clear();if(!in)throw std::runtime_error("Invalid baked input"); }
    bool producesMidi()const noexcept override{return midi;}
    bool writesEventBuffers()const noexcept override{return true;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return resourceKind;}
    DspResource* resourceOutput(std::size_t)noexcept override{return resource;}
    // MIDI events are shared by both channels. Numeric boundary inputs remain
    // conservative until the enclosing graph can supply their channel layout.
    bool canShareChannels()const noexcept override{return midi;}
    bool supportsControlRate()const noexcept override{return true;}
    bool supportsEventRate()const noexcept override{return true;}
    void process(ProcessContext& c)noexcept override {
        if(resourceKind!=ResourceKind::none){resource=const_cast<DspResource*>(external?(c.channelIndex==1&&external->rightResourceInputs?external->rightResourceInputs[port]:external->resource(port)):nullptr);return;}
        if(external&&c.eventOutput()){auto* events=c.channelIndex==1&&external->rightValueEventInputs?external->rightValueEventInputs[port]:external->events(port);if(events)*c.eventOutput()=*events;}
        if(triggerInput&&external&&!external->input(port)){
            if(auto* out=c.eventOutput()){out->initial=0;auto count=trigger.take();auto kept=std::min<std::uint64_t>(count,ValueEventBuffer::capacity);for(std::uint64_t i=0;i<kept;i++)out->push(0,1,ValueEventKind::trigger);out->dropped+=count-kept;}
        }
        if(midi){if(c.midiOutput){const auto* input=external?external->midi(port):nullptr;if(input)*c.midiOutput=*input;else *c.midiOutput={};}return;}
        const float* input=nullptr;
        if(external)input=c.channelIndex==0?external->input(port):external->rightInputs?external->rightInputs[port]:external->input(port);
        if(input){auto values=external->numericInput(port,fallback);if(c.channelIndex==1&&external->rightInputs&&external->rightInputs[port])values={external->rightInputs[port],external->sourceRates&&external->sourceRates[port]=='c'?std::size_t{0}:std::size_t{1},fallback};for(std::size_t i=0;i<c.bufferSize;i++)c.output[i]=values[i];}
        else std::fill_n(c.output,c.bufferSize,fallback);
    }
};
struct BakedOutput : NodeProcessor {
    bool midi=false;ResourceKind resourceKind=ResourceKind::none;DspResource* resource=nullptr;
    void configure(std::istream& in)override{int source,kind;in>>source>>kind;sources={source};midi=kind==1;resourceKind=kind==2?ResourceKind::history:kind==3?ResourceKind::analysis:kind==4?ResourceKind::spectrum:kind==6?ResourceKind::vector:kind==7?ResourceKind::waveform:ResourceKind::none;if(!in)throw std::runtime_error("Invalid baked output");}
    bool isObserver()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool producesMidi()const noexcept override{return midi;}
    bool writesEventBuffers()const noexcept override{return true;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return resourceKind;}
    DspResource* resourceOutput(std::size_t)noexcept override{return resource;}
    bool acceptsMidi(std::size_t)const noexcept override{return midi;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return resourceKind;}
    bool supportsControlRate()const noexcept override{return true;}
    bool supportsEventRate()const noexcept override{return true;}
    void process(ProcessContext& c)noexcept override{
        if(resourceKind!=ResourceKind::none){resource=const_cast<DspResource*>(c.resource(0));return;}
        if(c.events(0)&&c.eventOutput())*c.eventOutput()=*c.events(0);
        if(midi){if(c.midiOutput){if(auto* input=c.midi(0))*c.midiOutput=*input;else *c.midiOutput={};}return;}
        std::copy_n(c.inputOrSilence(0),c.bufferSize,c.output);
    }
};

/** Shared execution semantics, statically emitted call order. All allocation is in configure(). */
struct BakedGraphProcessor : NodeProcessor {
    void bindIdentity(const std::string& id)override{
        if(!runtime)return;
        for(std::size_t p=0;p<runtime->inputs.size();p++){
            auto& input=static_cast<BakedInput&>(*runtime->graph.nodes[runtime->inputs[p]].processor);
            if(input.triggerInput)input.trigger.bind(id+":input_"+std::to_string(p));
        }
        for(auto& node:runtime->graph.nodes){
            node.processor->bindIdentity(id+"/"+node.id);
            node.rightProcessor->shareChannelState(*node.processor);
            for(auto& lane:node.additionalVoices){lane.processor->shareVoiceState(*node.processor);lane.rightProcessor->shareChannelState(*lane.processor);}
        }
    }
    struct Runtime {
        AudioGraph graph;
        std::vector<int> inputs,outputs;
        std::vector<int> autoNodes;
        std::vector<std::string> outputPolicies;
        std::size_t voices=1;
    };
    std::shared_ptr<Runtime> runtime;
    bool sampleKernelsPrepared=false;
    // Boundary caches are allocated conservatively at compile time. At execution
    // time identical left/right sources can safely share the numeric work.
    void resolveChannels(const ProcessContext& c)noexcept{
        bool changed=false;
        for(std::size_t p=0;p<runtime->inputs.size();p++){
            auto& n=runtime->graph.nodes[runtime->inputs[p]];
            const auto& input=static_cast<const BakedInput&>(*n.processor);
            if(input.midi||input.triggerInput||input.resourceKind!=ResourceKind::none)continue;
            const float* left=c.input(p);const float* right=c.rightInputs&&p<c.inputCount?c.rightInputs[p]:left;
            bool stereo=left!=right;changed|=n.stereo!=stereo;n.stereo=stereo;
        }
        if(!changed)return;
        for(int index:runtime->graph.order){
            if(std::find(runtime->inputs.begin(),runtime->inputs.end(),index)!=runtime->inputs.end())continue;
            auto& n=runtime->graph.nodes[index];n.stereo=!n.processor->canShareChannels();
        }
        for(std::size_t pass=0;pass<(runtime->graph.hasSampleSteps?runtime->graph.nodes.size():1);pass++)for(int index:runtime->graph.order){
            auto& n=runtime->graph.nodes[index];if(n.processor->separatesChannels())continue;
            for(int source:n.processor->sources)if(source>=0&&runtime->graph.nodes[source%maxNodes].stereo)n.stereo=true;
        }
    }
    std::size_t voiceIndex=0;
    bool primaryChannel=true;
    bool canShareChannels()const noexcept override{
        if(!runtime)return false;
        // A boundary that reads across channels may still produce a mono result.
        // Propagate the compiled output layout to consumers outside the plugin.
        for(int index:runtime->outputs)if(runtime->graph.nodes[index].stereo)return false;
        return true;
    }
    bool separatesChannels()const noexcept override{
        if(!runtime)return true;
        // Only connected numeric boundaries need the enclosing graph's right
        // channel during the left pass. Internal stereo work is cached here.
        for(std::size_t p=0;p<runtime->inputs.size();p++)
            if(p<sources.size()&&sources[p]>=0&&!acceptsMidi(p))return true;
        return false;
    }
    bool supportsControlRate()const noexcept override{return true;}
    bool supportsEventRate()const noexcept override{return true;}
    std::size_t voices()const noexcept override{return runtime?runtime->voices:1;}
    std::size_t outputCount()const noexcept override{return runtime?runtime->outputs.size():1;}
    bool acceptsMidi(std::size_t port)const noexcept override{
        return runtime&&port<runtime->inputs.size()&&runtime->graph.nodes[runtime->inputs[port]].processor->producesMidi();
    }
    bool outputIsMidi(std::size_t port)const noexcept override{
        return runtime&&port<runtime->outputs.size()&&runtime->graph.nodes[runtime->outputs[port]].processor->producesMidi();
    }
    bool producesMidi()const noexcept override{
        if(!runtime)return false;for(std::size_t p=0;p<outputCount();p++)if(!outputIsMidi(p))return false;return true;
    }
    bool writesEventBuffers()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t port)const noexcept override{
        return runtime&&port<runtime->inputs.size()?runtime->graph.nodes[runtime->inputs[port]].processor->outputResourceKind(0):ResourceKind::none;
    }
    ResourceKind outputResourceKind(std::size_t port)const noexcept override{
        return runtime&&port<runtime->outputs.size()?runtime->graph.nodes[runtime->outputs[port]].processor->outputResourceKind(0):ResourceKind::none;
    }
    DspResource* resourceOutput(std::size_t port)noexcept override{
        if(!runtime||port>=runtime->outputs.size())return nullptr;
        auto& node=runtime->graph.nodes[runtime->outputs[port]];
        auto& lane=node.voice(node.voiceCount()>1?voiceIndex:0);
        return (node.stereo&&!primaryChannel?lane.rightProcessor:lane.processor)->resourceOutput(0);
    }
    bool inputUsesBlockData(std::size_t)const noexcept override{return true;}
    void collectMidiDevices(std::vector<std::uint64_t>& devices)const override{
        NodeProcessor::collectMidiDevices(devices);
        if(runtime){auto inner=runtime->graph.midiDevices();devices.insert(devices.end(),inner.begin(),inner.end());}
    }
    void selectVoice(std::size_t v)noexcept override{voiceIndex=v;}
    void shareChannelState(NodeProcessor& left)override{
        runtime=static_cast<BakedGraphProcessor&>(left).runtime;primaryChannel=false;
    }
    void shareVoiceState(NodeProcessor& first)override{
        auto& p=static_cast<BakedGraphProcessor&>(first);
        if(p.voices()>1)runtime=p.runtime;
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& configuration)noexcept override{
        const auto& p=static_cast<const BakedGraphProcessor&>(configuration);
        if(primaryChannel&&(voiceIndex==0||voices()==1)&&runtime&&p.runtime)
            for(std::size_t i=0;i<runtime->inputs.size();++i)
                setFallback(i,static_cast<const BakedInput&>(*p.runtime->graph.nodes[p.runtime->inputs[i]].processor).fallback);
    }
    std::string parameterStructure()const override{
        if(!runtime)return {};
        std::ostringstream out;out<<runtime->graph.parameterShape()<<" inputs";
        for(int input:runtime->inputs)out<<','<<input;
        out<<" outputs";for(int output:runtime->outputs)out<<','<<output;
        out<<" policies";for(const auto& policy:runtime->outputPolicies)out<<','<<policy;
        return out.str();
    }
    std::size_t parameterResourceSlots()const noexcept override{
        return 1+(runtime?runtime->graph.parameterResourceSlots():0);
    }
    void applyLiveParameters(const NodeProcessor& configuration,ParameterRetirement& retired)noexcept override{
        NodeProcessor::applyLiveParameters(configuration,retired);
        const auto& incoming=static_cast<const BakedGraphProcessor&>(configuration);
        if(primaryChannel&&(voiceIndex==0||voices()==1)&&runtime&&incoming.runtime)
            runtime->graph.applyLiveParameters(incoming.runtime->graph,retired);
    }
    void prepare(double rate,std::size_t frames) override {
        if(primaryChannel&&(voiceIndex==0||voices()==1)&&runtime)runtime->graph.prepare(rate,frames);
        sampleKernelsPrepared=true;
    }
    void adoptState(NodeProcessor& previous) noexcept override {
        auto& p=static_cast<BakedGraphProcessor&>(previous);
        if(primaryChannel&&(voiceIndex==0||voices()==1)&&runtime&&p.runtime)runtime->graph.adoptState(p.runtime->graph);
    }
    void inheritState(const NodeProcessor& previous)noexcept override{
        const auto& p=static_cast<const BakedGraphProcessor&>(previous);
        if(primaryChannel&&(voiceIndex==0||voices()==1)&&runtime&&p.runtime)runtime->graph.inheritState(p.runtime->graph);
    }
    template<class Factory>
    void configureGraph(const std::string& graph,Factory factory,std::vector<int> inputs,std::vector<int> outputs,std::vector<int> autoNodes){
        runtime=std::make_shared<Runtime>();runtime->graph=AudioGraph::compile(graph,factory);
        runtime->inputs=std::move(inputs);runtime->outputs=std::move(outputs);
        runtime->autoNodes=std::move(autoNodes);
        runtime->outputPolicies.resize(runtime->graph.nodes.size());
        int domain=-1;bool midi=false;
        for(int index:runtime->outputs){const auto& n=runtime->graph.nodes[index];
            midi=midi||n.processor->producesMidi();
            if(n.voiceDomain>=0){if(domain>=0&&domain!=n.voiceDomain)throw std::runtime_error("Merge independent voice domains before exporting their outputs");domain=n.voiceDomain;runtime->voices=n.voiceCount();}
        }
        if(midi&&runtime->voices>1)throw std::runtime_error("Merge polyphonic numeric outputs before combining them with MIDI outputs in one exported node");
        // The generated schedule always computes each node's two channels before its consumers.
        runtime->graph.crossChannel=true;
        runtime->graph.directChannelOutputs=true;
        for(int index:runtime->graph.order){auto& n=runtime->graph.nodes[index];for(std::size_t v=0;v<n.voiceCount();v++)n.voice(v).allocateChannels(n.stereo);}
    }
    void setFallback(std::size_t port,float value){
        auto& node=runtime->graph.nodes[runtime->inputs[port]];
        static_cast<BakedInput&>(*node.processor).fallback=value;
        static_cast<BakedInput&>(*node.rightProcessor).fallback=value;
    }
    virtual void renderGraph(int frames,AudioIO* io,MidiSource* midi)noexcept=0;
    // Reuse the live graph's read/compute/commit schedule for feedback islands.
    void renderFeedbackGraph(int frames,AudioIO* io,MidiSource* midi)noexcept{
        auto& graph=runtime->graph;
        for(const auto& step:graph.steps){
            if(step.sample)graph.processSampleStep(step,frames,io,midi);
            else for(int index:step.nodes)if(graph.nodes[index].active){
                graph.processNode(index,frames,0,io,midi);
                if(graph.nodes[index].stereo)graph.processNode(index,frames,1,io,midi);
            }
        }
        for(int index:graph.deferred){graph.commitDeferred(index,frames,0);if(graph.nodes[index].stereo)graph.commitDeferred(index,frames,1);}
    }

    void resolveAuto(int index)noexcept {
        auto& node=runtime->graph.nodes[index];std::array<char,maxNodes> inputs{};
        const auto& policy=runtime->outputPolicies[index];
        for(std::size_t p=0;p<node.processor->sources.size();p++)inputs[p]=!policy.empty()&&node.inputRates[p]=='c'&&!node.processor->inputUsesBlockData(p)?'c':sourceRate(node.processor->sources[p]);
        char rate=node.processor->resolveAutoRate(inputs.data(),node.processor->sources.size());
        if(policy.empty()){
            setRate(node,rate); // Older generated source packages.
            for(std::size_t p=0;p<node.inputRates.size();p++)node.inputRates[p]=inputs[p];
        }
        else {
            for(std::size_t p=0;p<node.outputRates.size();p++)node.outputRates[p]=policy[p]=='?'?rate:policy[p];
            bool control=node.outputRates.find_first_not_of('c')==std::string::npos;
            bool event=node.outputRates.find('a')==std::string::npos&&node.outputRates.find('e')!=std::string::npos;
            if(node.controlRate!=control||node.eventRate!=event)
                for(std::size_t v=0;v<node.voiceCount();v++)node.voice(v).eventReady={};
            node.controlRate=control;node.eventRate=event;
        }
    }
    char sourceRate(int source)const noexcept {
        if(source<0)return 'c';const auto& node=runtime->graph.nodes[source%maxNodes];
        return node.processor->outputIsMidi(source/maxNodes)?'e':node.outputRate(source/maxNodes);
    }
    char resolveAutoRate(const char* rates,std::size_t count)noexcept override {
        for(std::size_t p=0;p<runtime->inputs.size();p++){
            auto& node=runtime->graph.nodes[runtime->inputs[p]];
            auto* boundary=dynamic_cast<BakedInput*>(node.processor.get());
            bool trigger=boundary&&boundary->triggerInput;
            setRate(node,node.processor->producesMidi()||trigger?'e':rates&&p<count?rates[p]:'c');
        }
        for(std::size_t pass=0;pass<=(runtime->graph.hasSampleSteps?runtime->autoNodes.size():0);pass++){
            bool changed=false;
            for(int index:runtime->autoNodes){auto before=runtime->graph.nodes[index].controlRate;auto event=runtime->graph.nodes[index].eventRate;resolveAuto(index);changed|=before!=runtime->graph.nodes[index].controlRate||event!=runtime->graph.nodes[index].eventRate;}
            if(!changed)break;
        }
        char rate='c';for(int index:runtime->outputs){char source=sourceRate(runtime->graph.nodes[index].processor->sources[0]);if(source=='a'){rate='a';break;}if(source=='e')rate='e';}
        for(int index:runtime->outputs){auto& node=runtime->graph.nodes[index];setRate(node,node.processor->producesMidi()?'e':sourceRate(node.processor->sources[0]));}
        return rate;
    }
    static void setRate(Node& node,char rate)noexcept{
        bool control=rate=='c',event=rate=='e';
        if(node.controlRate!=control||node.eventRate!=event)for(std::size_t v=0;v<node.voiceCount();v++)node.voice(v).eventReady={};
        node.controlRate=control;node.eventRate=event;std::fill(node.outputRates.begin(),node.outputRates.end(),rate);
    }
    void process(ProcessContext& c)noexcept override{
        auto frames=c.blockFrames?c.blockFrames:c.bufferSize;
        if(!runtime||frames>blockSize)return;
        if(c.channelIndex==0&&(voiceIndex==0||voices()==1)){
            resolveChannels(c);
            std::array<char,maxNodes> inferred{};
            for(std::size_t p=0;p<c.inputCount;p++)inferred[p]=c.input(p)?'a':'c';
            resolveAutoRate(c.sourceRates?c.sourceRates:inferred.data(),c.inputCount);
            for(int index:runtime->inputs){auto& n=runtime->graph.nodes[index];
                auto& input=static_cast<BakedInput&>(*n.processor);input.external=&c;static_cast<BakedInput&>(*n.rightProcessor).external=&c;
            }
            renderGraph(static_cast<int>(frames),c.audioIO,c.midiSource);
        }
        for(std::size_t p=0;p<runtime->outputs.size();p++){
            auto& node=runtime->graph.nodes[runtime->outputs[p]];
            // An unbound boundary is inactive and has no allocated channel cache.
            if(!node.active){
                if(auto* out=c.outputAt(p))std::fill_n(out,c.bufferSize,0.f);
                if(auto* out=c.midiOutputAt(p))*out={};
                if(auto* out=c.eventOutput(p))*out={};
                continue;
            }
            auto& cache=node.voice(node.voiceCount()>1?voiceIndex:0).channelCache[node.stereo?c.channelIndex:0];
            if(node.processor->producesMidi()){if(auto* out=c.midiOutputAt(p))*out=cache.midi[0];}
            else if(outputResourceKind(p)==ResourceKind::none){
                if(auto* out=c.outputAt(p))std::copy_n(cache.outputs[0].data(),c.bufferSize,out);
                if(node.outputRate(0)=='e'&&c.eventOutput(p))*c.eventOutput(p)=node.voice(node.voiceCount()>1?voiceIndex:0).valueEvents[node.stereo?c.channelIndex:0][0];
            }
        }
    }
};
}
