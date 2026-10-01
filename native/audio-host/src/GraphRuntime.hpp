#pragma once
#include "NodeProcessor.hpp"
#include "AudioTiming.hpp"
#include <algorithm>
#include <array>
#include <bitset>
#include <atomic>
#include <memory>
#include <sstream>
#include <stdexcept>
#include <string>
#include <thread>
#include <unordered_set>
#include <vector>

namespace easyknack {
// Optional export-only block specialization; ordinary graph dispatch stays unchanged.
template<class T,class=void> struct HasExportProcess:std::false_type{};
template<class T> struct HasExportProcess<T,std::void_t<decltype(&T::processExport)>>:std::true_type{};
constexpr int maxNodes = 256;
constexpr int blockSize = 256;
constexpr int sampleRate = 48000;
struct Node {
    std::string id;
    std::string processorId;
    std::string parameters;
    int voiceDomain=-1;
    std::vector<Node> additionalVoices;
    Node& voice(std::size_t i) noexcept {return i==0?*this:additionalVoices[i-1];}
    const Node& voice(std::size_t i) const noexcept {return i==0?*this:additionalVoices[i-1];}
    std::size_t voiceCount() const noexcept {return additionalVoices.size()+1;}
    std::unique_ptr<NodeProcessor> processor;
    std::unique_ptr<NodeProcessor> rightProcessor;
    std::vector<const float*> inputs;
    std::vector<char> sourceRates;
    std::vector<const DspResource*> resourceInputs,rightResourceInputs;
    std::vector<std::array<const DspResource*,2>> resourceHandles;
    std::array<std::vector<const DspResource* const*>,2> resourceBindings;
    std::string inputRates, outputRates;
    std::vector<bool> denseOutputs;
    std::vector<ResourceKind> resourceKinds;
    char outputRate(std::size_t port)const noexcept{return port<outputRates.size()?outputRates[port]:eventRate?'e':controlRate?'c':'a';}
    std::size_t mergedVoiceCount=1;
    bool controlRate = false;
    bool eventRate = false;
    bool stereo = true;
    bool active = false;
    std::vector<std::array<float, blockSize>> heldInputs;
    std::array<float, blockSize> buffer{};
    std::vector<std::array<float, blockSize>> extraBuffers;
    std::vector<float*> outputPointers;
    std::vector<const MidiBlock*> midiInputs;
    MidiBlock midi;
    std::vector<MidiBlock> extraMidi;
    std::vector<MidiBlock*> midiOutputPointers;
    MidiBlock& midiAt(std::size_t output) noexcept { return output==0?midi:extraMidi[output-1]; }
    // Buffers materialize held event values for ordinary audio consumers. Masks retain
    // exact update positions without allocating event lists in the callback.
    std::vector<std::bitset<blockSize>> eventOffsets;
    std::array<std::vector<ValueEventBuffer>,2> valueEvents;
    std::vector<const ValueEventBuffer*> valueEventInputs,rightValueEventInputs;
    std::vector<ValueEventBuffer*> valueEventOutputs;
    std::vector<std::array<float,2>> eventValues, controlValues;
    std::vector<const float*> eventInputs;
    std::vector<float*> eventOutputs;
    std::array<bool,2> eventReady{};
    std::array<std::uint64_t,2> eventEvaluations{};
    // Allocated only for graphs that actually read across channels.
    struct CachedBuffer {
        std::shared_ptr<std::array<float,blockSize>> storage=std::make_shared<std::array<float,blockSize>>();
        float* data() noexcept{return storage->data();}
        const float* data()const noexcept{return storage->data();}
        float& operator[](std::size_t i)noexcept{return (*storage)[i];}
        const float& operator[](std::size_t i)const noexcept{return (*storage)[i];}
        void fill(float value)noexcept{storage->fill(value);}
    };
    struct ChannelCache {
        std::vector<CachedBuffer> outputs;
        std::vector<std::bitset<blockSize>> events;
        std::vector<MidiBlock> midi;
    };
    std::vector<ChannelCache> channelCache;
    std::vector<const float*> rightInputs;
    void allocateChannels(bool stereo) {
        rightInputs.resize(inputs.size());
        channelCache.resize(stereo?2u:1u);
        for(std::size_t ch=0;ch<channelCache.size();ch++) {
            channelCache[ch].outputs.clear();
            channelCache[ch].outputs.resize(outputPointers.size());
            channelCache[ch].events.resize(outputPointers.size());
            channelCache[ch].midi.resize(outputPointers.size());
        }
    }
    void allocateEvents(){
        sourceRates.resize(inputs.size(),'c');resourceInputs.resize(inputs.size());rightResourceInputs.resize(inputs.size());
        for(auto& channel:resourceBindings)channel.resize(inputs.size(),nullptr);resourceHandles.resize(outputPointers.size());resourceKinds.resize(outputPointers.size());
        for(std::size_t p=0;p<resourceKinds.size();p++)resourceKinds[p]=processor->outputResourceKind(p);
        valueEventInputs.resize(inputs.size());rightValueEventInputs.resize(inputs.size());valueEventOutputs.resize(outputPointers.size());
        for(auto& channel:valueEvents)channel.resize(outputPointers.size());
        eventOffsets.resize(outputPointers.size());eventValues.resize(outputPointers.size());
        controlValues.resize(inputs.size());eventInputs.resize(inputs.size());eventOutputs.resize(outputPointers.size());
    }
};

/** Compiled on the control thread; process() only touches preallocated memory. */
struct AudioGraph {
    std::vector<Node> nodes;
    std::vector<int> order;
    std::vector<int> deferred;
    struct Step {std::vector<int> nodes;bool sample=false;bool blockOnly=false;};
    std::vector<Step> steps;
    std::vector<int> lifetimeEnds;
    bool hasSampleSteps=false;
    std::size_t lastFeedbackChunk=0; // diagnostic: latest island step size
    // Control thread only. Collapse strongly connected regions after cutting block
    // feedback edges; keep sample-feedback edges to identify the minimal islands.
    void compileSampleSteps(){
        const auto count=nodes.size();std::vector<std::bitset<maxNodes>> reach(count);
        for(int target:order){auto& p=*nodes[target].processor;if(p.defersInputs()&&!p.sampleFeedbackBoundary())continue;
            for(int ref:p.sources)if(ref>=0)reach[ref%maxNodes].set(target);
        }
        for(std::size_t k=0;k<count;k++)for(std::size_t i=0;i<count;i++)if(reach[i][k])reach[i]|=reach[k];
        std::vector<int> component(count,-1);std::vector<Step> regions;
        for(int index:order)if(component[index]<0){int id=static_cast<int>(regions.size());regions.push_back({});
            for(int member:order)if(member==index||(reach[index][member]&&reach[member][index])){component[member]=id;regions.back().nodes.push_back(member);}
            bool cyclic=regions.back().nodes.size()>1||reach[index][index];regions.back().sample=cyclic;
            if(cyclic){hasSampleSteps=true;bool boundary=false;
                bool blockSafe=true,needsBlock=false;
                for(int member:regions.back().nodes){auto& n=nodes[member];auto& p=*n.processor;
                    if(p.sampleFeedbackBoundary()&&p.minimumFeedbackFrames(preparedSampleRate,preparedBlockFrames)<preparedBlockFrames)blockSafe=false;
                    if(!p.supportsSampleFeedback()||p.separatesChannels()||p.mergesVoices()||(!p.supportsTriggerFeedback()&&(n.eventRate||n.inputRates.find('e')!=std::string::npos)))needsBlock=true;
                    for(std::size_t port=0;port<p.sources.size();port++)if(p.sources[port]>=0&&p.inputUsesBlockData(port)&&std::find(regions.back().nodes.begin(),regions.back().nodes.end(),p.sources[port]%maxNodes)!=regions.back().nodes.end())needsBlock=true;
                }
                regions.back().blockOnly=blockSafe&&needsBlock;

                for(int member:regions.back().nodes){auto& n=nodes[member];auto& p=*n.processor;boundary|=p.sampleFeedbackBoundary();
                    for(std::size_t port=0;port<p.sources.size();port++)if(!regions.back().blockOnly && p.sources[port]>=0 && p.inputUsesBlockData(port)) {
                        int source=p.sources[port]%maxNodes;
                        if(std::find(regions.back().nodes.begin(),regions.back().nodes.end(),source)!=regions.back().nodes.end())
                            throw std::runtime_error("A feedback wavetable must be produced outside the sample feedback loop: "+n.id);
                    }
                    if(p.sampleFeedbackBoundary())for(std::size_t port=0;port<p.sources.size();port++)if(!p.defersInput(port)&&p.sources[port]>=0){
                        int source=p.sources[port]%maxNodes;
                        if(std::find(regions.back().nodes.begin(),regions.back().nodes.end(),source)!=regions.back().nodes.end())
                            throw std::runtime_error("Delay time must originate outside its feedback loop: "+n.id);
                    }
                    if(!regions.back().blockOnly&&(!p.supportsSampleFeedback()||p.separatesChannels()||p.mergesVoices()||(!p.supportsTriggerFeedback()&&(n.eventRate||n.inputRates.find('e')!=std::string::npos))))
                        throw std::runtime_error("Sample feedback is not supported by "+n.processorId+" ("+n.id+"). Use a fixed Delay of at least one block, or move this node outside the loop.");
                }
                if(!boundary)throw std::runtime_error("Feedback requires Sample Delay or Block Delay");
            }
        }
        std::vector<bool> done(regions.size());auto oldOrder=order;order.clear();steps.clear();
        for(std::size_t pass=0;pass<regions.size();++pass)for(std::size_t id=0;id<regions.size();++id)if(!done[id]){
            bool ready=true;for(int index:regions[id].nodes){auto& p=*nodes[index].processor;if(p.defersInputs()&&!p.sampleFeedbackBoundary())continue;
                for(int ref:p.sources)if(ref>=0&&component[ref%maxNodes]!=static_cast<int>(id)&&!done[component[ref%maxNodes]])ready=false;}
            if(ready){done[id]=true;steps.push_back(regions[id]);order.insert(order.end(),regions[id].nodes.begin(),regions[id].nodes.end());}
        }
        if(order.size()!=oldOrder.size())throw std::runtime_error("Invalid feedback execution plan");
        lifetimeEnds.resize(count);int position=0;for(auto& step:steps){position+=static_cast<int>(step.nodes.size());for(int index:step.nodes)lifetimeEnds[index]=position-1;}
    }
    std::array<float, blockSize> silence{};
    bool crossChannel = false;
    // Export plans own disjoint channel caches, so their processors can write
    // results there directly instead of copying an intermediate block.
    bool directChannelOutputs = false;
    double preparedSampleRate = sampleRate;
    std::size_t preparedBlockFrames = blockSize;
    void prepare(double rate, std::size_t frames) {
        if (!std::isfinite(rate) || rate <= 0 || !frames || frames > blockSize)
            throw std::invalid_argument("Invalid DSP preparation format");
        for(const auto& step:steps)if(step.blockOnly)for(int index:step.nodes){auto& p=*nodes[index].processor;
            if(p.sampleFeedbackBoundary()&&p.minimumFeedbackFrames(rate,frames)<frames)throw std::invalid_argument("Delay is shorter than the prepared block; rebuild the feedback plan");
        }
        preparedSampleRate=rate; preparedBlockFrames=frames;
        for(auto& node:nodes) for(std::size_t v=0;v<node.voiceCount();++v) {
            node.voice(v).processor->prepare(rate,frames);
            node.voice(v).rightProcessor->prepare(rate,frames);
        }
    }
    std::size_t cacheBuffersBefore=0,cacheBuffersAfter=0;

    // Sequential whole-node schedule only. Generated fused schedules retain their
    // own caches because their sample loops have different buffer lifetimes.
    // All shared ownership changes happen here, before graph publication.
    void reuseChannelBuffers(){
        struct Slot{std::shared_ptr<std::array<float,blockSize>> storage;int until;};
        std::vector<Slot> pool;
        std::vector<int> last(nodes.size(),-1);
        for(std::size_t position=0;position<order.size();position++){
            int index=order[position];last[index]=std::max(last[index],lifetimeEnds.empty()?static_cast<int>(position):lifetimeEnds[index]);
            for(int source:nodes[index].processor->sources)if(source>=0)
                last[source%maxNodes]=std::max(last[source%maxNodes],nodes[index].processor->defersInputs()&&!nodes[index].processor->sampleFeedbackBoundary()?static_cast<int>(order.size()):lifetimeEnds.empty()?static_cast<int>(position):lifetimeEnds[index]);
            if(nodes[index].processor->isSink()||nodes[index].processor->isObserver())last[index]=static_cast<int>(order.size());
        }
        cacheBuffersBefore=0;
        for(std::size_t position=0;position<order.size();position++){
            int index=order[position];auto& node=nodes[index];
            for(std::size_t voice=0;voice<node.voiceCount();voice++)for(auto& channel:node.voice(voice).channelCache)for(auto& output:channel.outputs){
                cacheBuffersBefore++;
                auto free=std::find_if(pool.begin(),pool.end(),[position](const Slot& slot){return slot.until<static_cast<int>(position);});
                if(free==pool.end())pool.push_back({output.storage,last[index]});
                else{output.storage=free->storage;free->until=last[index];}
            }
        }
        cacheBuffersAfter=pool.size();
    }

    static AudioGraph parse(const std::string& message);

    template<class Factory>
    static AudioGraph compile(const std::string& message, Factory createNodeProcessor, bool configurationOnly=false) {
        std::istringstream in(message);
        std::string command;
        int count;
        if (!(in >> command >> count) || command != "GRAPH" || count < 0 || count > maxNodes)
            throw std::runtime_error("invalid graph size");
        AudioGraph graph;
        graph.nodes.reserve(count);
        std::unordered_set<std::string> ids;
        for (int i = 0; i < count; ++i) {
            Node node;
            if (!(in >> node.id >> node.processorId) || node.id.size() > 64 || !ids.insert(node.id).second)
                throw std::runtime_error("invalid node");
            node.processor = createNodeProcessor(node.processorId);
            auto parameterStart = in.tellg();
            node.processor->configure(in);
            if(!configurationOnly)node.processor->bindIdentity(node.id);
            auto parameterEnd = in.tellg();
            node.parameters=message.substr(static_cast<std::size_t>(parameterStart),static_cast<std::size_t>(parameterEnd-parameterStart));
            std::istringstream rightParameters(message.substr(static_cast<std::size_t>(parameterStart),
                    static_cast<std::size_t>(parameterEnd - parameterStart)));
            if(!configurationOnly){
            node.rightProcessor = createNodeProcessor(node.processorId);
            node.rightProcessor->configure(rightParameters);
            node.rightProcessor->shareChannelState(*node.processor);
            }
            std::string marker, rate;
            if (!(in >> marker >> rate >> node.inputRates) || marker != "@" || (rate.empty() || rate.find_first_not_of("ace")!=std::string::npos))
                throw std::runtime_error("invalid rate metadata");
            node.outputRates=rate.size()==1?std::string(node.processor->outputCount(),rate[0]):rate;
            // Frequency was appended after Factor/Trigger; old exports retain their port indices.
            if(node.processorId=="digitalnoise"&&node.outputRates=="ae"&&node.processor->outputCount()==3)node.outputRates+="a";
            if(node.outputRates.size()!=node.processor->outputCount())throw std::runtime_error("output rate count mismatch");
            if(node.processor->executionRate())rate=std::string(1,node.processor->executionRate());
            node.controlRate = rate.find_first_not_of('c')==std::string::npos;
            node.eventRate = rate.find('a')==std::string::npos && rate.find('e')!=std::string::npos;
            if (node.inputRates == "-") node.inputRates.clear();
            if (node.inputRates.size() != node.processor->sources.size()
                    || node.inputRates.find_first_not_of("ace") != std::string::npos)
                throw std::runtime_error("input rate count mismatch");
            if (node.controlRate && !node.processor->supportsControlRate())
                throw std::runtime_error("processor does not support control rate");
            if (node.eventRate && !node.processor->supportsEventRate())
                throw std::runtime_error("processor does not support event rate");
            for (int input : node.processor->sources)
                if (input < -1 || input >= maxNodes*8 || input >= 0 && (input % maxNodes >= count || input % maxNodes == i && !node.processor->defersInputs())) throw std::runtime_error("invalid input");
            auto outputs = node.processor->outputCount();
            if(outputs<1||outputs>8)throw std::runtime_error("invalid output count");
            if(!configurationOnly){
            node.denseOutputs.resize(outputs,true);
            for(std::size_t p=0;p<outputs;p++)node.denseOutputs[p]=node.outputRate(p)!='e';
            node.extraBuffers.resize(outputs-1);node.outputPointers.resize(outputs);
            node.extraMidi.resize(outputs-1);node.midiOutputPointers.resize(outputs);
            node.midiInputs.resize(node.processor->sources.size());
            node.inputs.resize(node.processor->sources.size());
            if(!node.processor->supportsScalarInputs())node.heldInputs.resize(node.processor->sources.size());
            }
            graph.nodes.push_back(std::move(node));
        }
        std::string extra;
        if (in >> extra) throw std::runtime_error("unexpected graph data");
        if(configurationOnly)return graph;
        std::vector<bool> done(count);
        for(auto& node:graph.nodes)for(std::size_t p=0;p<node.processor->sources.size();p++){
            int ref=node.processor->sources[p];if(ref<0)continue;
            auto& source=graph.nodes[ref%maxNodes];
            if(static_cast<std::size_t>(ref/maxNodes)>=source.processor->outputCount())throw std::runtime_error("invalid source output");
            if(source.processor->outputResourceKind(ref/maxNodes)!=node.processor->inputResourceKind(p)
                && !(source.processor->outputResourceKind(ref/maxNodes)==ResourceKind::spectrum&&node.processor->inputResourceKind(p)==ResourceKind::analysis))throw std::runtime_error("Incompatible resource connection");
            if(!node.eventRate||!node.processor->processesEventBlocks()||node.inputRates[p]=='c')source.denseOutputs[ref/maxNodes]=true;
            if(source.processor->outputIsMidi(ref/maxNodes)!=node.processor->acceptsMidi(p))throw std::runtime_error("incompatible MIDI connection");
        }
        for (int pass = 0; pass < count; ++pass) for (int i = 0; i < count; ++i) {
            if (done[i]) continue;
            bool ready = true;
            for (std::size_t port=0;port<graph.nodes[i].processor->sources.size();port++) {
                int input=graph.nodes[i].processor->sources[port];
                if(input>=0)input%=maxNodes;
                if (input >= 0 && graph.nodes[input].processor->isSink()) throw std::runtime_error("output cannot be a source");
                if (input >= 0 && !done[input] && !graph.nodes[i].processor->defersInput(port)) ready = false;
            }
            if (ready) { done[i] = true; graph.order.push_back(i); }
        }
        if (graph.order.size() != graph.nodes.size()) throw std::runtime_error("graph cycle");
        for(auto& node:graph.nodes)node.stereo=!node.processor->canShareChannels();
        for(std::size_t pass=0;pass<graph.nodes.size();++pass)for (int index : graph.order) {
            auto& node = graph.nodes[index];
            if (!node.processor->separatesChannels()) for (int source : node.processor->sources)
                if (source >= 0 && graph.nodes[source%maxNodes].stereo) node.stereo = true;
        }
        // Only output consumers make a branch live. Mute/bypass has already been
        // resolved by the workspace, so unused mute inputs disappear naturally.
        // Keep node indices and state, but compile the callback schedule only once.
        for (auto& node : graph.nodes) node.active = node.processor->isSink() || (node.processor->isObserver() && std::any_of(node.processor->sources.begin(),node.processor->sources.end(),[](int source){return source>=0;}));
        for(std::size_t pass=0;pass<graph.nodes.size();++pass)for (auto it = graph.order.rbegin(); it != graph.order.rend(); ++it)
            if (graph.nodes[*it].active)
                for (int source : graph.nodes[*it].processor->sources)
                    if (source >= 0) graph.nodes[source%maxNodes].active = true;
        graph.order.erase(std::remove_if(graph.order.begin(), graph.order.end(),
                [&graph](int index) { return !graph.nodes[index].active; }), graph.order.end());
        graph.compileSampleSteps();
        // Voice domains must reach a fixed point across feedback boundaries before
        // allocating any lanes. Feedback cannot silently collapse polyphony.
        std::vector<std::size_t> voiceCounts(graph.nodes.size(),1);
        for(int index:graph.order){auto& node=graph.nodes[index];voiceCounts[index]=node.processor->voices();node.voiceDomain=voiceCounts[index]>1?index:-1;}
        for(std::size_t pass=0;pass<graph.nodes.size();++pass)for(int index:graph.order){auto& node=graph.nodes[index];
            if(!node.processor->mergesVoices())for(int ref:node.processor->sources)if(ref>=0){auto& source=graph.nodes[ref%maxNodes];
                if(source.voiceDomain>=0){if(node.voiceDomain>=0&&node.voiceDomain!=source.voiceDomain)throw std::runtime_error("Merge independent voice groups before mixing them");node.voiceDomain=source.voiceDomain;voiceCounts[index]=voiceCounts[ref%maxNodes];}
            }
        }
        // All voice storage is allocated before publishing the graph to the audio thread.
        std::size_t totalVoices=0;
        for(int index:graph.order){
            auto& node=graph.nodes[index];
            std::size_t count=voiceCounts[index];
            if(node.processor->isSink()&&count>1)throw std::runtime_error("Insert Voice Merge before Audio Output");
            if((totalVoices+=count)>2048)throw std::runtime_error("Graph voice budget exceeded (2048 instances)");
            if(node.processor->mergesVoices()){
                int ref=node.processor->sources[0];
                node.mergedVoiceCount=ref<0?1:voiceCounts[ref%maxNodes];
                for(std::size_t p=1;p<node.processor->sources.size();p++)if(int auxiliary=node.processor->sources[p];auxiliary>=0){
                    auto& other=graph.nodes[auxiliary%maxNodes];
                    if(voiceCounts[auxiliary%maxNodes]>1&&(ref<0||other.voiceDomain!=graph.nodes[ref%maxNodes].voiceDomain))
                        throw std::runtime_error("Voice Merge pitch must belong to the same voice group as its audio");
                }
                node.inputs.resize(node.mergedVoiceCount*node.processor->sources.size());
                if(!node.processor->supportsScalarInputs())node.heldInputs.resize(node.inputs.size());
            }
            node.allocateEvents();
            for(std::size_t v=1;v<count;v++){
                Node lane;lane.id=node.id;lane.processorId=node.processorId;
                lane.processor=createNodeProcessor(node.processorId);lane.rightProcessor=createNodeProcessor(node.processorId);
                std::istringstream left(node.parameters),right(node.parameters);
                lane.processor->configure(left);lane.rightProcessor->configure(right);
                lane.processor->shareVoiceState(*node.processor);
                lane.rightProcessor->shareChannelState(*lane.processor);
                lane.processor->selectVoice(v);lane.rightProcessor->selectVoice(v);
                lane.inputs.resize(node.inputs.size());lane.heldInputs.resize(node.heldInputs.size());
                lane.midiInputs.resize(node.midiInputs.size());lane.extraBuffers.resize(node.extraBuffers.size());
                lane.outputPointers.resize(node.outputPointers.size());
                lane.extraMidi.resize(node.extraMidi.size());lane.midiOutputPointers.resize(node.outputPointers.size());lane.allocateEvents();node.additionalVoices.push_back(std::move(lane));
            }
        }
        graph.crossChannel=graph.hasSampleSteps;
        for(int index:graph.order){if(graph.nodes[index].processor->defersInputs()&&!graph.nodes[index].processor->sampleFeedbackBoundary())graph.deferred.push_back(index);if(graph.nodes[index].processor->separatesChannels()||graph.nodes[index].processor->defersInputs()) graph.crossChannel=true;}
        if(graph.crossChannel) for(int index:graph.order) {
            auto& node=graph.nodes[index];
            for(std::size_t v=0;v<node.voiceCount();v++)node.voice(v).allocateChannels(node.stereo);
        }
        if(graph.crossChannel)graph.reuseChannelBuffers();
        for(int index:graph.order){auto& node=graph.nodes[index];for(std::size_t v=0;v<node.voiceCount();v++){
            auto& lane=node.voice(v);
            for(std::size_t port=0;port<lane.inputs.size();port++){
                auto sourcePort=node.processor->mergesVoices()?port/node.mergedVoiceCount:port;
                int ref=node.processor->sources[sourcePort];if(ref<0||node.processor->inputResourceKind(sourcePort)==ResourceKind::none)continue;
                auto& upstream=graph.nodes[ref%maxNodes];auto& source=upstream.voice(upstream.voiceCount()>1?(node.processor->mergesVoices()?port%node.mergedVoiceCount:v):0);
                for(unsigned channel=0;channel<2;channel++)lane.resourceBindings[channel][port]=&source.resourceHandles[ref/maxNodes][upstream.stereo?channel:0];
            }
        }}
        graph.prepare(sampleRate,blockSize);
        return graph;
    }

    std::string parameterShape() const {
        std::ostringstream out;
        for(const auto& n:nodes) {
            const auto& p=*n.processor;
            out<<n.id<<':'<<n.processorId<<':'<<n.controlRate<<n.eventRate<<':'<<n.inputRates<<':'<<n.outputRates
               <<':'<<(p.minimumFeedbackFrames(preparedSampleRate,preparedBlockFrames)>=preparedBlockFrames)<<':'<<p.outputCount()<<':'<<p.voices()<<':'<<p.canShareChannels()<<p.separatesChannels()
               <<':'<<p.inputEndpoint()<<':'<<p.outputEndpoint()<<':'<<p.midiDevice();
            for(int source:p.sources)out<<','<<source;
            for(std::size_t i=0;i<p.outputCount();i++)out<<p.outputIsMidi(i);
            out<<';';
        }
        return out.str();
    }

    void adoptState(AudioGraph& old) noexcept {
        // Small generic scheduler state is copied by the existing path; large DSP
        // state is transferred by each processor below, without freeing storage.
        inheritState(old, false);
        for(auto& node:nodes) for(auto& previous:old.nodes)
            if(node.id==previous.id && node.processorId==previous.processorId) {
                for(std::size_t v=0;v<node.voiceCount()&&v<previous.voiceCount();++v) {
                    auto& lane=node.voice(v); auto& prior=previous.voice(v);
                    lane.processor->adoptState(*prior.processor);
                    if(previous.stereo) lane.rightProcessor->adoptState(*prior.rightProcessor);
                    else if(node.stereo)lane.rightProcessor->inheritState(*lane.processor);
                }
                break;
            }
    }

    void inheritState(const AudioGraph& old, bool processors=true) noexcept {
        for (auto& node : nodes) for (const auto& previous : old.nodes)
            if (node.id == previous.id && node.processorId == previous.processorId) {
                bool compatible=node.active&&previous.active&&node.eventRate&&previous.eventRate
                    &&node.parameters==previous.parameters&&node.inputRates==previous.inputRates&&node.voiceCount()==previous.voiceCount();
                if(compatible)for(int source:node.processor->sources)if(source>=0)
                    if(nodes[source%maxNodes].id!=old.nodes[source%maxNodes].id){compatible=false;break;}
                if(compatible)for(std::size_t v=0;v<node.voiceCount();v++){
                    auto& lane=node.voice(v);const auto& prior=previous.voice(v);
                    for(std::size_t ch=0;ch<2;ch++){
                        auto oldChannel=previous.stereo?ch:0;lane.eventReady[ch]=prior.eventReady[oldChannel];
                        lane.eventEvaluations[ch]=prior.eventEvaluations[oldChannel];
                        for(std::size_t p=0;p<lane.eventValues.size();p++)lane.eventValues[p][ch]=prior.eventValues[p][oldChannel];
                        for(std::size_t p=0;p<lane.controlValues.size();p++)lane.controlValues[p][ch]=prior.controlValues[p][oldChannel];
                    }
                }
                if (processors) {
                node.processor->inheritState(*previous.processor);
                // A previously shared processor has the live phase/state for both channels.
                // Collapsing stereo to mono deliberately continues the left channel.
                node.rightProcessor->inheritState(previous.stereo ? *previous.rightProcessor : *previous.processor);
                for(std::size_t v=1;v<node.voiceCount()&&v<previous.voiceCount();v++){
                    auto& lane=node.voice(v);const auto& old=previous.voice(v);
                    lane.processor->inheritState(*old.processor);
                    lane.rightProcessor->inheritState(previous.stereo?*old.rightProcessor:*old.processor);
                }
                }
                break;
            }
    }

    std::vector<AudioRoute> routes() const {
        std::vector<AudioRoute> result;
        for (const auto& node : nodes) {
            if (!node.active) continue;
            auto input = node.processor->inputEndpoint();
            if (!input.empty()) result.push_back({input, true});
            if (node.processor->isSink()) result.push_back({node.processor->outputEndpoint(), false});
        }
        return result;
    }
    std::vector<std::uint64_t> midiDevices() const {
        std::vector<std::uint64_t> result;
        for(const auto& node:nodes)if(node.active)node.processor->collectMidiDevices(result);
        return result;
    }
    void process(float* mono, int frames) noexcept {
        std::array<float, blockSize> right{};
        process(mono, frames, 0, nullptr); process(right.data(), frames, 1, nullptr);
    }
    void process(float* mono, int frames, std::size_t channel, AudioIO* io, MidiSource* midiSource=nullptr) noexcept {
        // Cross-channel graphs evaluate each node's channels before its consumers.
        // The second public pass only delivers the cached right sinks. No block delay.
        if(crossChannel) {
            if(channel==0) for(const auto& step:steps) {
                if(step.sample)processSampleStep(step,frames,io,midiSource);
                else for(int index:step.nodes){processNode(index,frames,0,io,midiSource);if(nodes[index].stereo)processNode(index,frames,1,io,midiSource);}
            }
        } else for(int index:order) processNode(index,frames,channel,io,midiSource);
        if(channel==0)for(int index:deferred){commitDeferred(index,frames,0);if(nodes[index].stereo)commitDeferred(index,frames,1);}
        std::fill_n(mono,frames,0.f);
        for(int index:order) {
            auto& node=nodes[index];
            if(!node.processor->isSink())continue;
            const float* buffer=crossChannel?node.channelCache[node.stereo?channel:0].outputs[0].data():node.buffer.data();
            if(io)io->output(node.processor->outputEndpoint(),channel,buffer,frames);
            for(int i=0;i<frames;i++)mono[i]+=buffer[i];
        }
    }
    void bindFeedbackEvents(Node& node,Node& lane,std::size_t channel,std::size_t voice)noexcept{
        for(std::size_t port=0;port<node.processor->sources.size();++port){
            int ref=node.processor->sources[port];lane.valueEventInputs[port]=nullptr;
            if(ref>=0){auto& source=nodes[ref%maxNodes];
                if(source.outputRate(ref/maxNodes)=='e')lane.valueEventInputs[port]=&source.voice(source.voiceCount()>1?voice:0).valueEvents[source.stereo?channel:0][ref/maxNodes];
            }
        }
    }
    void processSampleStep(const Step& step,int frames,AudioIO* io,MidiSource* midiSource)noexcept{
        // Fixed arrays/vectors were allocated while compiling. Cache lifetimes span
        // the whole island, including external inputs and delayed state commits.
        for(int index:step.nodes){auto& node=nodes[index];
            for(std::size_t channel=0;channel<(node.stereo?2u:1u);channel++)for(std::size_t v=0;v<node.voiceCount();v++){
                auto& lane=node.voice(v);auto& cache=lane.channelCache[channel];
                for(std::size_t port=0;port<lane.outputPointers.size();port++){
                    auto& events=lane.valueEvents[channel][port];cache.events[port].reset();
                    if(events.count&&events.events[events.count-1].kind==ValueEventKind::trigger&&events.events[events.count-1].sampleOffset+1==events.frames)cache.events[port].set(0);
                    events.clear(frames,lane.eventValues[port][channel]);
                }
            }
        }
        std::size_t chunk=frames;
        for(int index:step.nodes)if(nodes[index].processor->sampleFeedbackBoundary()){
            auto& node=nodes[index];for(std::size_t channel=0;channel<(node.stereo?2u:1u);channel++)for(std::size_t v=0;v<node.voiceCount();v++){
                auto& lane=node.voice(v);auto* p=(channel?lane.rightProcessor:lane.processor).get();
                for(std::size_t port=0;port<p->sources.size();port++){int ref=p->sources[port];lane.inputs[port]=nullptr;if(ref>=0){auto& source=nodes[ref%maxNodes];lane.inputs[port]=source.voice(source.voiceCount()>1?v:0).channelCache[source.stereo?channel:0].outputs[ref/maxNodes].data();}}
                ProcessContext c{lane.inputs.data(),lane.inputs.size(),nullptr,static_cast<std::size_t>(frames),preparedSampleRate,silence.data()};c.blockFrames=frames;c.channelIndex=channel;
                chunk=std::min(chunk,std::max(std::size_t(1),p->beginFeedbackBlock(c)));
            }
        }
        lastFeedbackChunk=chunk;
        if(step.blockOnly){
            // This plan was validated for a fixed delay >= the prepared block size.
            // Full-block processors retain their ordinary event/resource/channel path.
            for(int index:step.nodes){auto& node=nodes[index];
                if(!node.processor->sampleFeedbackBoundary()){
                    processNode(index,frames,0,io,midiSource);if(node.stereo)processNode(index,frames,1,io,midiSource);continue;
                }
                for(std::size_t channel=0;channel<(node.stereo?2u:1u);channel++)for(std::size_t v=0;v<node.voiceCount();v++){
                    auto& lane=node.voice(v);auto* p=(channel?lane.rightProcessor:lane.processor).get();
                    ProcessContext c{nullptr,0,lane.channelCache[channel].outputs[0].data(),static_cast<std::size_t>(frames),preparedSampleRate,silence.data()};c.blockFrames=frames;c.channelIndex=channel;p->processSample(c);
                }
            }
            for(int index:step.nodes)if(nodes[index].processor->sampleFeedbackBoundary()){
                auto& node=nodes[index];for(std::size_t channel=0;channel<(node.stereo?2u:1u);channel++)for(std::size_t v=0;v<node.voiceCount();v++){
                    auto& lane=node.voice(v);auto* p=(channel?lane.rightProcessor:lane.processor).get();
                    for(std::size_t port=0;port<p->sources.size();port++){int ref=p->sources[port];lane.inputs[port]=nullptr;lane.sourceRates[port]='c';if(ref>=0){auto& source=nodes[ref%maxNodes];lane.inputs[port]=source.voice(source.voiceCount()>1?v:0).channelCache[source.stereo?channel:0].outputs[ref/maxNodes].data();lane.sourceRates[port]=source.outputRate(ref/maxNodes);}}
                    ProcessContext c{lane.inputs.data(),lane.inputs.size(),nullptr,static_cast<std::size_t>(frames),preparedSampleRate,silence.data()};c.blockFrames=frames;c.sourceRates=lane.sourceRates.data();p->commitSample(c);
                }
            }
            return;
        }

        for(int sample=0;sample<frames;sample+=static_cast<int>(chunk)){
            const auto length=std::min(chunk,static_cast<std::size_t>(frames-sample));

            for(int index:step.nodes){auto& node=nodes[index];
                for(std::size_t channel=0;channel<(node.stereo?2u:1u);channel++)for(std::size_t v=0;v<node.voiceCount();v++){
                    auto& lane=node.voice(v);auto* p=(channel?lane.rightProcessor:lane.processor).get();auto& cache=lane.channelCache[channel];
                    if(node.controlRate&&sample>0){for(auto& output:cache.outputs)std::fill_n(output.data()+sample,length,output[0]);continue;}
                    for(std::size_t port=0;port<p->sources.size();port++){int ref=p->sources[port];lane.inputs[port]=nullptr;lane.sourceRates[port]='c';
                        if(ref>=0){auto& source=nodes[ref%maxNodes];auto& sourceLane=source.voice(source.voiceCount()>1?v:0);bool held=source.outputRate(ref/maxNodes)=='c'||node.inputRates[port]=='c';
                            bool blockData=p->inputUsesBlockData(port);
                            lane.inputs[port]=sourceLane.channelCache[source.stereo?channel:0].outputs[ref/maxNodes].data()+(held||blockData?0:sample);lane.sourceRates[port]=held?'c':'a';
                            if(held&&!blockData&&!p->supportsScalarInputs()){std::fill_n(lane.heldInputs[port].data(),length,lane.inputs[port][0]);lane.inputs[port]=lane.heldInputs[port].data();}}
                    }
                    for(std::size_t port=0;port<lane.outputPointers.size();port++)lane.outputPointers[port]=cache.outputs[port].data()+sample;
                    ProcessContext c{lane.inputs.data(),lane.inputs.size(),lane.outputPointers[0],node.controlRate?1:length,preparedSampleRate,silence.data()};
                    c.blockFrames=frames;c.channelIndex=channel;c.outputs=lane.outputPointers.data();c.outputCount=lane.outputPointers.size();c.sourceRates=lane.sourceRates.data();c.sampleOffset=sample;c.samplewise=!node.controlRate;c.audioIO=io;c.midiSource=midiSource;
                    for(std::size_t port=0;port<lane.valueEventOutputs.size();port++)lane.valueEventOutputs[port]=&lane.valueEvents[channel][port];
                    c.valueEventOutputs=lane.valueEventOutputs.data();
                    bindFeedbackEvents(node,lane,channel,v);c.valueEventInputs=lane.valueEventInputs.data();
                    p->processSample(c);
                    if(node.controlRate)for(auto& output:cache.outputs)std::fill_n(output.data()+sample,length,output[0]);
                }
            }
            // All delay outputs remain the previous sample until every processor
            // has consumed them; commit never overwrites a published output.
            for(int index:step.nodes)if(nodes[index].processor->sampleFeedbackBoundary()){auto& node=nodes[index];
                for(std::size_t channel=0;channel<(node.stereo?2u:1u);channel++)for(std::size_t v=0;v<node.voiceCount();v++){
                    auto& lane=node.voice(v);auto* p=(channel?lane.rightProcessor:lane.processor).get();
                    for(std::size_t port=0;port<p->sources.size();port++){int ref=p->sources[port];lane.inputs[port]=nullptr;lane.sourceRates[port]='c';if(ref>=0){auto& source=nodes[ref%maxNodes];auto& other=source.voice(source.voiceCount()>1?v:0);bool held=source.outputRate(ref/maxNodes)=='c';lane.inputs[port]=other.channelCache[source.stereo?channel:0].outputs[ref/maxNodes].data()+(held?0:sample);lane.sourceRates[port]=held?'c':'a';}}
                    ProcessContext c{lane.inputs.data(),lane.inputs.size(),nullptr,length,preparedSampleRate,silence.data()};c.blockFrames=frames;c.channelIndex=channel;c.sourceRates=lane.sourceRates.data();c.sampleOffset=sample;c.samplewise=true;
                    bindFeedbackEvents(node,lane,channel,v);c.valueEventInputs=lane.valueEventInputs.data();p->commitSample(c);
                }
            }
        }
        for(int index:step.nodes){auto& node=nodes[index];
            for(std::size_t channel=0;channel<(node.stereo?2u:1u);channel++)for(std::size_t v=0;v<node.voiceCount();v++){
                auto& lane=node.voice(v);auto& cache=lane.channelCache[channel];
                for(std::size_t port=0;port<lane.outputPointers.size();port++)if(node.outputRate(port)=='e'){
                    auto& events=lane.valueEvents[channel][port];
                    for(const auto& event:events){cache.events[port].set(event.sampleOffset);if(event.kind==ValueEventKind::trigger&&event.sampleOffset+1<static_cast<unsigned>(frames))cache.events[port].set(event.sampleOffset+1);}
                    lane.eventValues[port][channel]=events.valueAt(frames-1);
                }
            }
        }
    }
    void commitDeferred(int index,int frames,std::size_t channel) noexcept {
        auto& node=nodes[index];
        for(std::size_t v=0;v<node.voiceCount();++v){auto& lane=node.voice(v);auto* processor=(channel?lane.rightProcessor:lane.processor).get();
            for(std::size_t port=0;port<lane.inputs.size();++port){int ref=processor->sources[port];lane.inputs[port]=nullptr;lane.sourceRates[port]='c';
                if(ref>=0){auto& upstream=nodes[ref%maxNodes];auto& source=upstream.voice(upstream.voiceCount()>1?v:0);lane.inputs[port]=source.channelCache[upstream.stereo?channel:0].outputs[ref/maxNodes].data();lane.sourceRates[port]=upstream.outputRate(ref/maxNodes);}
            }
            ProcessContext context{lane.inputs.data(),lane.inputs.size(),nullptr,static_cast<std::size_t>(frames),preparedSampleRate,silence.data()};
            context.blockFrames=frames;context.channelIndex=channel;context.sourceRates=lane.sourceRates.data();processor->commitBlock(context);
        }
    }
    template<class Processor,bool Optimize=false>
    static void invokeProcessor(Processor& processor,ProcessContext& context) noexcept {
        if constexpr(std::is_same_v<Processor,NodeProcessor>)processor.process(context);
        else if constexpr(Optimize && HasExportProcess<Processor>::value) {
#if defined(__clang__)
            [[clang::always_inline]] processor.processExport(context);
#else
            processor.processExport(context);
#endif
        } else processor.Processor::process(context);
    }
    void processNode(int index,int frames,std::size_t channel,AudioIO* io,MidiSource* midiSource) noexcept {
        processNodeAs<NodeProcessor>(index,frames,channel,io,midiSource);
    }
    template<class Processor,bool Optimize=false>
    void processNodeAs(int index,int frames,std::size_t channel,AudioIO* io,MidiSource* midiSource) noexcept {
            auto& node = nodes[index];
            if (channel == 0 || node.stereo) for(std::size_t v=0;v<node.voiceCount();v++) {
                auto& lane=node.voice(v);
                auto* processor = static_cast<Processor*>((channel == 0 ? lane.processor : lane.rightProcessor).get());
                lane.outputPointers[0]=lane.buffer.data();
                for(std::size_t i=0;i<lane.extraBuffers.size();i++)lane.outputPointers[i+1]=lane.extraBuffers[i].data();
                if(directChannelOutputs&&crossChannel)
                    for(std::size_t p=0;p<lane.outputPointers.size();p++)
                        lane.outputPointers[p]=lane.channelCache[node.stereo?channel:0].outputs[p].data();
                for(std::size_t p=0;p<lane.midiOutputPointers.size();p++){lane.midiOutputPointers[p]=&lane.midiAt(p);lane.midiAt(p).clear();}
                for(auto& offsets:lane.eventOffsets)offsets.reset();
                for(std::size_t p=0;p<lane.outputPointers.size();p++){
                    lane.valueEventOutputs[p]=&lane.valueEvents[channel][p];
                    auto& previous=*lane.valueEventOutputs[p];
                    if(previous.count&&previous.events[previous.count-1].kind==ValueEventKind::trigger&&previous.events[previous.count-1].sampleOffset+1==previous.frames)lane.eventOffsets[p].set(0);
                    previous.clear(frames,lane.eventValues[p][channel]);
                }
                std::bitset<blockSize> updates;
                if(node.eventRate&&!lane.eventReady[channel])updates.set(0);
                for (std::size_t port = 0; port < lane.inputs.size(); ++port) {
                    auto sourcePort=processor->mergesVoices()?port/node.mergedVoiceCount:port;
                    int source = processor->sources[sourcePort];
                    int output=source<0?0:source/maxNodes;
                    lane.sourceRates[port]=source<0?'c':nodes[source%maxNodes].outputRate(output);
                    Node* sourceLane=nullptr;
                    if(source>=0){auto& upstream=nodes[source%maxNodes];sourceLane=&upstream.voice(upstream.voiceCount()>1?(processor->mergesVoices()?port%node.mergedVoiceCount:v):0);}
                    lane.inputs[port] = !sourceLane ? nullptr : output==0?sourceLane->buffer.data():sourceLane->extraBuffers[output-1].data();
                    if(!processor->mergesVoices())lane.midiInputs[port]=sourceLane&&processor->acceptsMidi(port)?&sourceLane->midiAt(output):nullptr;
                    std::size_t sourceChannel=sourceLane&&nodes[source%maxNodes].stereo?channel:0;
                    if(crossChannel&&sourceLane) {
                        lane.inputs[port]=sourceLane->channelCache[sourceChannel].outputs[output].data();
                        auto right=nodes[source%maxNodes].stereo?1:0;
                        lane.rightInputs[port]=sourceLane->channelCache[right].outputs[output].data();
                        if(!processor->mergesVoices()&&processor->acceptsMidi(port))lane.midiInputs[port]=&sourceLane->channelCache[sourceChannel].midi[output];
                    } else if(crossChannel)lane.rightInputs[port]=nullptr;
                    lane.resourceInputs[port]=lane.resourceBindings[channel][port]?*lane.resourceBindings[channel][port]:nullptr;
                    lane.rightResourceInputs[port]=lane.resourceBindings[1][port]?*lane.resourceBindings[1][port]:nullptr;
                    lane.valueEventInputs[port]=sourceLane&&nodes[source%maxNodes].outputRate(output)=='e'
                        ?&sourceLane->valueEvents[sourceChannel][output]:nullptr;
                    lane.rightValueEventInputs[port]=sourceLane&&nodes[source%maxNodes].outputRate(output)=='e'?&sourceLane->valueEvents[nodes[source%maxNodes].stereo?1:0][output]:nullptr;
                    if(node.eventRate&&processor->evaluatesOnEvents()&&sourceLane){
                        auto& upstream=nodes[source%maxNodes];
                        if(upstream.outputRate(output)=='e')updates|=crossChannel?sourceLane->channelCache[sourceChannel].events[output]:sourceLane->eventOffsets[output];
                        else if(upstream.outputRate(output)=='c'){
                            float value=lane.inputs[port][0];
                            if(value!=lane.controlValues[port][channel])updates.set(0);
                            lane.controlValues[port][channel]=value;
                        }
                    }
                    if (source >= 0 && node.inputRates[sourcePort] == 'c' && !node.controlRate && !processor->inputUsesBlockData(sourcePort) && !processor->acceptsMidi(sourcePort)) {
                        const bool alreadyHeld=lane.sourceRates[port]=='c';
                        lane.sourceRates[port]='c';
                        if(!processor->supportsScalarInputs()&&!alreadyHeld){
                            std::fill_n(lane.heldInputs[port].data(), frames, lane.inputs[port][0]);
                            lane.inputs[port] = lane.heldInputs[port].data();
                        }
                    }
                }
                ProcessContext context{lane.inputs.data(), lane.inputs.size(), lane.outputPointers[0],
                        node.controlRate || node.eventRate&&processor->producesMidi() ? std::size_t{1} : static_cast<std::size_t>(frames), preparedSampleRate, silence.data(),
                        static_cast<std::size_t>(frames), io, channel,lane.outputPointers.data(),lane.outputPointers.size(),lane.midiInputs.data(),&lane.midi,midiSource,crossChannel?lane.rightInputs.data():nullptr,lane.midiOutputPointers.data(),lane.sourceRates.data()};
                context.resourceInputs=lane.resourceInputs.data();context.rightResourceInputs=lane.rightResourceInputs.data();
                context.eventRefresh=updates.test(0);context.eventEvaluationCount=&lane.eventEvaluations[channel];
                context.valueEventInputs=lane.valueEventInputs.data();context.rightValueEventInputs=lane.rightValueEventInputs.data();context.valueEventOutputs=lane.valueEventOutputs.data();
                if(node.eventRate&&processor->processesEventBlocks()){
                    processor->processEventBlock(context);
                    for(std::size_t p=0;p<lane.outputPointers.size();p++){
                        auto& events=*lane.valueEventOutputs[p];if(node.denseOutputs[p])events.expand(lane.outputPointers[p],frames);
                        for(const auto& event:events){lane.eventOffsets[p].set(event.sampleOffset);
                            if(event.kind==ValueEventKind::trigger&&event.sampleOffset+1<static_cast<unsigned>(frames))lane.eventOffsets[p].set(event.sampleOffset+1);}
                        lane.eventValues[p][channel]=events.valueAt(frames-1);
                    }
                    lane.eventReady[channel]=true;
                }else if(node.eventRate&&processor->evaluatesOnEvents()){
                    // At a shared timestamp all inputs already contain their new values.
                    // Audio inputs alone never populate updates.
                    for(int i=0;i<frames;i++){
                        if(updates.test(i)){
                            for(std::size_t p=0;p<lane.inputs.size();p++)lane.eventInputs[p]=lane.inputs[p]?lane.inputs[p]+i:nullptr;
                            for(std::size_t p=0;p<lane.outputPointers.size();p++)lane.eventOutputs[p]=lane.outputPointers[p]+i;
                            auto event=context;event.inputs=lane.eventInputs.data();event.output=lane.eventOutputs[0];
                            event.outputs=lane.eventOutputs.data();event.bufferSize=1;
                            invokeProcessor<Processor,Optimize>(*processor,event);++lane.eventEvaluations[channel];
                            for(std::size_t p=0;p<lane.outputPointers.size();p++){
                                float value=lane.outputPointers[p][i];
                                if(!lane.eventReady[channel]||value!=lane.eventValues[p][channel])lane.eventOffsets[p].set(i);
                                lane.eventValues[p][channel]=value;
                            }
                            lane.eventReady[channel]=true;
                        }
                        for(std::size_t p=0;p<lane.outputPointers.size();p++)lane.outputPointers[p][i]=lane.eventValues[p][channel];
                    }
                }else{
                    invokeProcessor<Processor,Optimize>(*processor,context);
                    if(!processor->producesMidi()&&!processor->writesEventBuffers()){
                        // MIDI converters produce held values and one-sample trigger pulses.
                        // Both pulse edges become value events, including across blocks.
                        for(std::size_t p=0;p<lane.outputPointers.size();p++)if(node.outputRate(p)=='e'&&!processor->writesEventBuffer(p))for(int i=0;i<frames;i++){
                            float value=lane.outputPointers[p][i];
                            if(i==0&&!lane.eventReady[channel]||value!=lane.eventValues[p][channel])lane.eventOffsets[p].set(i);
                            lane.eventValues[p][channel]=value;
                        }
                        lane.eventReady[channel]=true;
                    }
                }
                for(std::size_t p=0;p<lane.outputPointers.size();p++)if(node.outputRate(p)=='c')std::fill_n(lane.outputPointers[p],frames,lane.outputPointers[p][0]);
                for(std::size_t p=0;p<lane.outputPointers.size();p++)if(node.outputRate(p)=='e'&&processor->writesEventBuffer(p)){
                    auto& events=*lane.valueEventOutputs[p];if(node.denseOutputs[p])events.expand(lane.outputPointers[p],frames);
                    for(auto& event:events){lane.eventOffsets[p].set(event.sampleOffset);if(event.kind==ValueEventKind::trigger&&event.sampleOffset+1<static_cast<unsigned>(frames))lane.eventOffsets[p].set(event.sampleOffset+1);}
                    lane.eventValues[p][channel]=events.valueAt(frames-1);
                }
                if(!processor->writesEventBuffers()&&!(node.eventRate&&processor->processesEventBlocks()))
                    for(std::size_t p=0;p<lane.outputPointers.size();p++)if(node.outputRate(p)=='e'&&!processor->writesEventBuffer(p))
                        for(int i=0;i<frames;i++)if(lane.eventOffsets[p].test(i))lane.valueEventOutputs[p]->push(i,lane.outputPointers[p][i]);
                for(std::size_t p=0;p<lane.resourceHandles.size();p++)if(node.resourceKinds[p]!=ResourceKind::none)lane.resourceHandles[p][channel]=processor->resourceOutput(p);
                if(crossChannel) {
                    auto& cache=lane.channelCache[node.stereo?channel:0];
                    for(std::size_t p=0;p<lane.outputPointers.size();p++) {
                        if(node.denseOutputs[p]&&lane.outputPointers[p]!=cache.outputs[p].data())
                            std::copy_n(lane.outputPointers[p],frames,cache.outputs[p].data());
                        cache.events[p]=lane.eventOffsets[p];
                    }
                    for(std::size_t p=0;p<cache.midi.size();p++)if(processor->outputIsMidi(p))cache.midi[p]=lane.midiAt(p);
                }
            }
    }
};
/** Three ownership slots: control writes a free slot; audio swaps only at block boundaries.
 * Old graphs are reclaimed by the control thread, never by the audio callback. */
class AudioEngine {
    struct Slot { std::atomic<bool> busy{false}; AudioGraph graph; };
    std::array<Slot, 3> slots;
    std::atomic<int> pending{-1};
    int active = -1; // audio thread only
    struct ParameterSlot {
        std::atomic<bool> busy{false};
        AudioGraph configuration;
        std::vector<std::shared_ptr<const void>> retired;
        std::uint64_t generation=0;
    };
    std::array<ParameterSlot,3> parameterSlots;
    std::atomic<int> pendingParameters{-1};
    std::uint64_t publishedGeneration=0, activeGeneration=0;
    std::string publishedShape;
    std::vector<std::string> structuralParameters;
    std::array<std::uint64_t,3> graphGenerations{};
    float envelope = 0;
public:
    MidiSource* midiSource=nullptr; // Assigned before starting the backend; outlives its callbacks.
    AudioTiming timing;
    std::atomic<bool> playing{false};
    std::atomic<unsigned> callbacks{0};
    static_assert(std::atomic<int>::is_always_lock_free && std::atomic<bool>::is_always_lock_free);

    // The protocol reader owns configurations; only validated scalar assignments
    // happen on the render thread. Coalesced updates carry the complete parameter set.
    bool publishParameters(AudioGraph configuration) {
        if(!publishedGeneration||configuration.parameterShape()!=publishedShape)return false;
        for(std::size_t i=0;i<configuration.nodes.size();i++)
            if(!configuration.nodes[i].processor->supportsLiveParameters()
               && configuration.nodes[i].parameters!=structuralParameters[i])return false;
        int destination=pendingParameters.exchange(-1);
        while(destination<0){
            for(int i=0;i<3;i++){bool available=false;if(parameterSlots[i].busy.compare_exchange_strong(available,true)){destination=i;break;}}
            if(destination<0)std::this_thread::yield();
        }
        auto& slot=parameterSlots[destination];slot.configuration=std::move(configuration);slot.generation=publishedGeneration;
        slot.retired.clear();slot.retired.resize((2048+maxNodes)*2);
        pendingParameters.store(destination);return true;
    }
    void publish(AudioGraph graph) {
        publishedShape=graph.parameterShape();structuralParameters.clear();
        for(const auto& node:graph.nodes)structuralParameters.push_back(node.parameters);
        ++publishedGeneration;
        // Reclaim an unconsumed update first. During inheritState(), the audio
        // thread temporarily owns two slots; only the control thread may wait.
        int destination = pending.exchange(-1);
        while (destination < 0) {
            for (int i = 0; i < 3; ++i) {
                bool available = false;
                if (slots[i].busy.compare_exchange_strong(available, true)) {
                    destination = i;
                    break;
                }
            }
            if (destination < 0) std::this_thread::yield();
        }
        slots[destination].graph = std::move(graph);
        graphGenerations[destination]=publishedGeneration;
        pending.store(destination);
    }

    void render(float* stereo, unsigned frames, AudioIO* io = nullptr, bool measure = false) noexcept {
        callbacks.fetch_add(1, std::memory_order_relaxed);
        std::array<float, blockSize> mono{};
        std::array<float, blockSize> right{}, gain{};
        for (unsigned offset = 0; offset < frames;) {
            const auto timingStart=measure?std::chrono::steady_clock::now():std::chrono::steady_clock::time_point{};
            RenderCadence::begin();
            int next = pending.exchange(-1);
            if (next >= 0) {
                timing.resetCycle();
                if (active >= 0) {
                    slots[next].graph.adoptState(slots[active].graph);
                    slots[active].busy.store(false);
                }
                active = next;activeGeneration=graphGenerations[next];
            }
            int patch=pendingParameters.exchange(-1);
            if(patch>=0){
                auto& update=parameterSlots[patch];
                if(active>=0 && update.generation==activeGeneration) {
                    auto& graph=slots[active].graph;std::size_t retired=0;
                    for(std::size_t i=0;i<graph.nodes.size();i++) {
                        auto& node=graph.nodes[i];auto& configuration=update.configuration.nodes[i];auto& p=*configuration.processor;
                        if(node.parameters==configuration.parameters)continue;
                        if(p.supportsLiveParameters()) for(std::size_t v=0;v<node.voiceCount();v++) {
                            auto& lane=node.voice(v);
                            update.retired[retired++]=lane.processor->takeParameterResource();
                            update.retired[retired++]=lane.rightProcessor->takeParameterResource();
                            lane.processor->applyParameters(p);lane.rightProcessor->applyParameters(p);
                            // Parameter edits must wake event-only expressions at block start.
                            lane.eventReady={};
                        }
                        if(p.supportsLiveParameters())node.parameters.swap(configuration.parameters);
                    }
                }
                update.busy.store(false);
            }
            int count = static_cast<int>(std::min<unsigned>(blockSize, frames - offset));
            if (io) io->beginBlock(count);
            if (midiSource) midiSource->beginBlock(count,sampleRate);
            ++manualTriggerEpoch;
            if (active >= 0) {
                slots[active].graph.process(mono.data(), count, 0, io,midiSource);
                slots[active].graph.process(right.data(), count, 1, io,midiSource);
            } else { std::fill_n(mono.data(), count, 0.f); std::fill_n(right.data(), count, 0.f); }
            float target = playing.load(std::memory_order_relaxed) ? 0.2f : 0.f;
            for (int i = 0; i < count; ++i) {
                envelope += (target - envelope) * 0.004f;
                gain[i] = envelope;
                float sample = std::clamp(mono[i] * envelope, -1.f, 1.f);
                stereo[2 * (offset + i)] = sample;
                stereo[2 * (offset + i) + 1] = std::clamp(right[i] * envelope, -1.f, 1.f);
            }
            if (io) io->endBlock(gain.data(), count);
            if(measure)timing.observe(std::chrono::duration<double>(std::chrono::steady_clock::now()-timingStart).count(),
                    count,sampleRate,RenderCadence::period);
            offset += count;
        }
    }
};
}

