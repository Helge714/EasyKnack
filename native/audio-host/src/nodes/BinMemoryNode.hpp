#pragma once
#include "BinHoldNode.hpp"
namespace easyknack {
struct BinMemoryNode : BinHoldNode {
    float update=1;
    void bindIdentity(const std::string&)override{}
    void configure(std::istream& in)override{auto p=parameters::BinMemoryNodeParameters::read(in);sources=p.sources();value=p.in_value;update=p.update_value;}
    void process(ProcessContext& c)noexcept override{
        if(!frame||bin>=held.size()){c.output[0]=0;return;}
        float input=c.numericInput(0,value)[0];if(!std::isfinite(input))input=0;
        float amount=c.numericInput(1,update)[0];amount=std::isfinite(amount)?std::clamp(amount,0.f,1.f):0;
        if(!captured[bin]||amount==1){held[bin]=input;captured[bin]=1;}
        else if(amount>0)held[bin]=float(double(held[bin])*(1-amount)+double(input)*amount);
        c.output[0]=held[bin];
    }
};
EASYKNACK_NODE(BinMemoryNode,"binmemory")
}
