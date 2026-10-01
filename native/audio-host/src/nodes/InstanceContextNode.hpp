#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
namespace easyknack {
struct InstanceContextNode : NodeProcessor {
    std::size_t index=0,count=1;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    std::size_t outputCount()const noexcept override{return 4;}
    void configure(std::istream& in)override{
        auto position=in.tellg();std::string marker;in>>marker;
        if(marker=="!5ae0a236eb803f8c"){
            int source,legacyCount;if(!(in>>source>>legacyCount)||source< -1||source>=2048||legacyCount<0||legacyCount>256)throw std::runtime_error("Invalid legacy Instance Context");
            sources={source};count=legacyCount;return;
        }
        in.clear();in.seekg(position);auto p=parameters::InstanceContextNodeParameters::read(in);sources=p.sources();
    }
    void process(ProcessContext& c)noexcept override{
        std::uint32_t seed=static_cast<std::uint32_t>(index)+0x9e3779b9u;seed^=seed>>16;seed*=0x7feb352du;seed^=seed>>15;seed*=0x846ca68bu;seed^=seed>>16;
        float values[]{float(index),float(count),count>1?std::min(1.f,float(index)/float(count-1)):0.f,float(seed&0xffffffu)};
        for(std::size_t p=0;p<4;p++)if(auto* out=c.outputAt(p))std::fill_n(out,c.bufferSize,values[p]);
    }
};
EASYKNACK_NODE(InstanceContextNode,"instancecontext")
}
