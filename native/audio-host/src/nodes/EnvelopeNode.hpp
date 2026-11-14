#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../FunctionCurve.hpp"
namespace easyknack {
struct EnvelopeNode : NodeProcessor {
    ManualTriggerReceiver manual;
    void bindIdentity(const std::string& id)override{manual.bind(id+":trigger");}
    void shareChannelState(NodeProcessor& p)override{manual=static_cast<EnvelopeNode&>(p).manual;}
    void shareVoiceState(NodeProcessor& p)override{shareChannelState(p);}

    FunctionCurve curve;
    double duration=300,decay=180,release=300,sustain=.5,position=0,level=0,startLevel=0,releaseLevel=0;
    float gateValue=0,triggerValue=0;
    enum Stage {IDLE,ATTACK,HOLD,RELEASE,DECAY};Stage stage=IDLE;
    bool lastGate=false,lastTrigger=false,hold=true,legacy=true,started=false,retriggerCurrent=true;
    bool supportsScalarInputs()const noexcept override{return true;}
    bool canShareChannels() const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    std::shared_ptr<const void> takeParameterResource()noexcept override{return std::move(curve.storage);}
    void applyParameters(const NodeProcessor& configuration)noexcept override{
        const auto& p=static_cast<const EnvelopeNode&>(configuration);
        curve.storage=p.curve.storage;duration=p.duration;decay=p.decay;legacy=p.legacy;retriggerCurrent=p.retriggerCurrent;release=p.release;sustain=p.sustain;gateValue=p.gateValue;triggerValue=p.triggerValue;hold=p.hold;
    }
    void configure(std::istream& in) override{
        auto start=in.tellg();std::string marker;in>>marker;
        if(marker=="!6e6e611f517ae942"){
            sources={readInput(in)};gateValue=readNumber(in,0,1);sources.push_back(readInput(in));triggerValue=readNumber(in,0,1);
            sources.push_back(readInput(in));duration=readNumber(in,0,60000);sources.push_back(readInput(in));decay=readNumber(in,0,60000);
            sources.push_back(readInput(in));release=readNumber(in,0,60000);hold=readNumber(in,0,1)!=0;curve.read(in);legacy=false;retriggerCurrent=true;return;
        }
        if(marker=="!10ad0836639a66ae"){
            sources={readInput(in)};gateValue=readNumber(in,0,1);sources.push_back(readInput(in));triggerValue=readNumber(in,0,1);
            sources.push_back(readInput(in));duration=readNumber(in,0,60000);sources.push_back(readInput(in));release=readNumber(in,0,60000);
            sources.push_back(readInput(in));sustain=readNumber(in,.01f,.99f);hold=readNumber(in,0,1)!=0;curve.read(in);legacy=true;return;
        }
        if(marker!="!a11bbab0677e0e68")in.seekg(start);
        in>>std::ws;if(marker!="!a11bbab0677e0e68"&&in.peek()=='!'){auto p=parameters::EnvelopeNodeParameters::read(in);
            sources=p.sources();gateValue=p.gate_value;triggerValue=p.trigger_value;duration=p.duration_value;decay=p.decay_value;release=p.release_value;curve=std::move(p.curve_value);hold=p.hold_value!=0;retriggerCurrent=p.retrigger_current_value!=0;legacy=false;
            return;
        }

        sources={readInput(in)};gateValue=readNumber(in,0,1);sources.push_back(readInput(in));triggerValue=readNumber(in,0,1);
        duration=readNumber(in,0,60000);release=readNumber(in,0,60000);sustain=readNumber(in,.01f,.99f);curve.read(in);hold=true;
    }
    void inheritState(const NodeProcessor& old) noexcept override{
        auto* p=dynamic_cast<const EnvelopeNode*>(&old);if(!p)return;
        position=p->position;level=p->level;startLevel=p->startLevel;releaseLevel=p->releaseLevel;
        started=p->started||p->stage!=IDLE;manual.seen=p->manual.seen;stage=p->stage;lastGate=p->lastGate;lastTrigger=p->lastTrigger;
    }
    void beginRelease() noexcept{stage=RELEASE;position=0;releaseLevel=level;}
    void processPhases(ProcessContext& c) noexcept{
        const auto clicks=manual.take();auto gates=c.numericInput(0,gateValue),triggers=c.numericInput(1,triggerValue);
        auto attacks=c.numericInput(2,duration),decays=c.numericInput(3,decay),releases=c.numericInput(4,release);
        const auto& segments=*curve.storage;
        const double attackEnd=segments.size()>=2?segments.front().b.x:1.0/3;
        const double holdX=segments.size()>=2?segments.back().a.x:2.0/3;
        std::size_t cursor=0;auto* events=c.events(1);
        auto milliseconds=[](double input,double fallback){return std::isfinite(input)?std::clamp(input,0.0,60000.0):fallback;};
        for(std::size_t i=0;i<c.bufferSize;i++){
            bool gate=gates[i]>0,trigger=triggers[i]>0;
            bool retrigger=i==0&&clicks&&!c.input(1)&&!events;
            if(events)while(cursor<events->count&&events->events[cursor].sampleOffset<=i)retrigger|=events->events[cursor++].kind==ValueEventKind::trigger;
            if(retrigger||(gate&&!lastGate)||(trigger&&!lastTrigger)){startLevel=retriggerCurrent&&started?level:curve.sample(0);started=true;position=0;stage=ATTACK;}
            else if(!gate&&lastGate&&(stage==ATTACK||stage==DECAY||stage==HOLD))beginRelease();
            lastGate=gate;lastTrigger=trigger;
            for(int transition=0;transition<5;transition++){
                double ms=stage==ATTACK?milliseconds(attacks[i],duration):stage==DECAY?milliseconds(decays[i],decay):milliseconds(releases[i],release);
                double t=ms==0?1:std::min(1.0,position/(ms*.001*c.sampleRate));
                if(stage==ATTACK){
                    level=curve.sample(t*attackEnd)+(startLevel-curve.sample(0))*(1-t);
                    if(t>=1){stage=DECAY;position=0;continue;}position++;
                }else if(stage==DECAY){
                    level=curve.sample(attackEnd+t*(holdX-attackEnd));
                    if(t>=1){if(gate&&hold){stage=HOLD;position=0;}else beginRelease();continue;}position++;
                }else if(stage==HOLD){
                    level=curve.sample(holdX);if(!gate||!hold){beginRelease();continue;}
                }else if(stage==RELEASE){
                    double end=curve.sample(1),atHold=curve.sample(holdX);
                    double value=curve.sample(holdX+t*(1-holdX));
                    level=std::abs(atHold-end)>1e-9?end+(releaseLevel-end)*(value-end)/(atHold-end):value+(releaseLevel-atHold)*(1-t);
                    if(t>=1){stage=IDLE;level=end;}else position++;
                }else level=started?curve.sample(1):0;
                break;
            }
            level=std::isfinite(level)?std::clamp(level,0.0,1.0):0;c.output[i]=static_cast<float>(level);
        }
    }
    void process(ProcessContext& c) noexcept override{
        if(!legacy){processPhases(c);return;}
        const auto clicks=manual.take();
        auto gates=c.numericInput(0,gateValue),triggers=c.numericInput(1,triggerValue);
        auto durations=c.numericInput(2,duration),releases=c.numericInput(3,release),sustains=c.numericInput(4,sustain);
        std::size_t eventCursor=0;auto* triggerEvents=c.events(1);
        for(std::size_t i=0;i<c.bufferSize;i++){
            double duration=std::isfinite(durations[i])?std::clamp(double(durations[i]),0.0,60000.0):this->duration;
            double release=std::isfinite(releases[i])?std::clamp(double(releases[i]),0.0,60000.0):this->release;
            double sustain=std::isfinite(sustains[i])?std::clamp(double(sustains[i]),.01,.99):this->sustain;
            bool gate=gates[i]>0,trigger=triggers[i]>0;
            bool retrigger=i==0&&clicks&&!c.input(1)&&!triggerEvents;if(triggerEvents)while(eventCursor<triggerEvents->count&&triggerEvents->events[eventCursor].sampleOffset<=i){retrigger|=triggerEvents->events[eventCursor++].kind==ValueEventKind::trigger;}
            if(retrigger||(gate&&!lastGate)||(trigger&&!lastTrigger)){startLevel=level;position=0;stage=ATTACK;}
            else if(!gate&&lastGate&&(stage==ATTACK||stage==HOLD))beginRelease();
            lastGate=gate;lastTrigger=trigger;
            if(stage==HOLD&&!hold)beginRelease();
            if(stage==ATTACK){
                double t=duration==0?1:std::min(1.0,position/(duration*.001*c.sampleRate));
                // Retrigger smoothly from the current level, fading its offset over attack/decay.
                level=curve.sample(t*sustain)+(startLevel-curve.sample(0))*(1-t);
                position++;
                if(t>=1){if(gate&&hold)stage=HOLD;else beginRelease();}
            }else if(stage==HOLD){level=curve.sample(sustain);}
            else if(stage==RELEASE){
                double t=release==0?1:std::min(1.0,position/(release*.001*c.sampleRate));
                double y=curve.sample(sustain+t*(1-sustain)),s=curve.sample(sustain),end=curve.sample(1);
                // Scale the release shape to the actual note-off level; terminal level is zero.
                double shape=std::abs(s-end)>1e-9?(y-end)/(s-end):1-t;
                level=releaseLevel*shape;position++;
                if(t>=1){stage=IDLE;level=0;}
            }else level=0;
            // Curve reshaping/retrigger offsets must keep a normalized envelope bounded.
            level=std::isfinite(level)?std::clamp(level,0.0,1.0):0;
            c.output[i]=static_cast<float>(level);
        }
    }
};
EASYKNACK_NODE(EnvelopeNode,"envelope")
}
