#include "nodes/UnitConverterNode.hpp"
#include <cassert>
#include <limits>
using namespace easyknack;
int main(){
    auto check=[](double input,UnitFormat from,UnitFormat to,double expected,double rate=48000){
        assert(std::abs(UnitConverterNode::evaluate(input,from,to,rate)-expected)<1e-5*std::max(1.,std::abs(expected)));
    };
    check(440,UnitFormat::hz,UnitFormat::ms,1000./440);
    check(500,UnitFormat::ms,UnitFormat::hz,2);
    check(120,UnitFormat::bpm,UnitFormat::second,.5);
    check(.5,UnitFormat::second,UnitFormat::bpm,120);
    check(480,UnitFormat::sample,UnitFormat::hz,100);
    check(480,UnitFormat::sample,UnitFormat::hz,200,96000);
    check(100,UnitFormat::hz,UnitFormat::sample,960,96000);
    check(0,UnitFormat::hz,UnitFormat::second,0);
    check(-2,UnitFormat::hz,UnitFormat::second,-.5);
    check(1,UnitFormat::factor,UnitFormat::percent,100);
    assert(std::isfinite(UnitConverterNode::evaluate(std::numeric_limits<double>::denorm_min(),UnitFormat::hz,UnitFormat::second)));
    UnitConverterNode node;std::istringstream config("-1 440 hz ms");node.configure(config);
    float input[]{1,2,4,0},out[4]{},zero[4]{};const float* inputs[]{input};char rates[]{'a'};
    node.from=UnitFormat::hz;node.to=UnitFormat::second;
    ProcessContext c{inputs,1,out,4,48000,zero,4};c.sourceRates=rates;node.process(c);
    assert(out[0]==1&&out[1]==.5f&&out[2]==.25f&&out[3]==0);
    rates[0]='c';node.process(c);for(auto v:out)assert(v==1);
    bool rejected=false;try{std::istringstream invalid("-1 440 hz db");node.configure(invalid);}catch(const std::runtime_error&){rejected=true;}assert(rejected);
}
