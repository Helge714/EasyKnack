#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
namespace easyknack {
struct VoiceMergeNode : NodeProcessor {
    enum class Mode { sum,mean,spread,pitch };
    Mode mode=Mode::sum;float width=1;
    bool canShareChannels() const noexcept override{return mode==Mode::sum||mode==Mode::mean;}
    bool mergesVoices() const noexcept override{return true;}
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const VoiceMergeNode&>(configuration);
        mode=p.mode;width=p.width;
    }
    void configure(std::istream& in) override{
        in>>std::ws;if(in.peek()=='!'){
            auto position=in.tellg();std::string marker;in>>marker;in.seekg(position);
            // Select using the generated reader; an incompatible version is rejected by both.
            try {auto p=parameters::VoiceMergeNodeParameters::read(in);sources=p.sources();
                if(p.mode_value=="sum")mode=Mode::sum;else if(p.mode_value=="mean")mode=Mode::mean;else throw std::runtime_error("Unknown merge mode");
            } catch(const std::runtime_error&) {
                in.clear();in.seekg(position);auto p=parameters::LegacyVoiceMergeNodeParameters::read(in);sources=p.sources();width=p.width_value;
                if(p.mode_value=="sum")mode=Mode::sum;else if(p.mode_value=="mean")mode=Mode::mean;else if(p.mode_value=="spread")mode=Mode::spread;else if(p.mode_value=="pitch")mode=Mode::pitch;else throw std::runtime_error("Unknown merge mode");
            }
            return;
        }

        sources={readInput(in)};
        in>>std::ws;
        if(in.peek()!='@'&&in.peek()!=std::char_traits<char>::eof()){
            std::string marker,name;in>>marker>>name;
            if(marker=="combine"){
                if(name=="sum")mode=Mode::sum;else if(name=="mean")mode=Mode::mean;
                else throw std::runtime_error("Unknown Voice Merge combination");
                return;
            }
            if(marker!="mix")throw std::runtime_error("Invalid Voice Merge options");
            if(name=="sum")mode=Mode::sum;else if(name=="mean")mode=Mode::mean;
            else if(name=="spread")mode=Mode::spread;else if(name=="pitch")mode=Mode::pitch;
            else throw std::runtime_error("Unknown Voice Merge mode");
            sources.push_back(readInput(in));width=readNumber(in,0,1);
            in>>std::ws;
            if(in.peek()!='@'&&in.peek()!=std::char_traits<char>::eof()){
                in>>marker;if(marker!="width")throw std::runtime_error("Invalid width input");
                sources.push_back(readInput(in));
            }
        }
    }
    void process(ProcessContext& c) noexcept override{
        std::fill_n(c.output,c.bufferSize,0.f);
        const std::size_t voices=c.inputCount/sources.size();
        for(std::size_t v=0;v<voices;v++)if(auto* input=c.input(v)){
            const float* frequency=mode==Mode::pitch?c.input(voices+v):nullptr;
            for(std::size_t i=0;i<c.bufferSize;i++){
                float gain=mode==Mode::mean?1.f/voices:1.f;
                if(mode==Mode::spread||mode==Mode::pitch){
                    float pan=mode==Mode::spread?(voices>1?2.f*v/(voices-1)-1:0):
                        frequency&&frequency[i]>0?std::clamp(std::log2(frequency[i]/261.625565f)/2,-1.f,1.f):0;
                    const auto* modulation=sources.size()>2?c.input(voices*2+v):nullptr;
                    pan*=modulation?std::clamp(std::isfinite(modulation[i])?modulation[i]:0.f,0.f,1.f):width;
                    // Constant-power balance, unity at center, preserving existing stereo channels.
                    gain=std::sqrt(2.f)*(c.channelIndex==0?std::cos((pan+1)*.7853981634f):std::sin((pan+1)*.7853981634f));
                }
                c.output[i]+=input[i]*gain;
            }
        }
    }
};
EASYKNACK_NODE(VoiceMergeNode,"voicemerge")
}
