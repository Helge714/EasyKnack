#include "nodes/AdditiveOscillatorNode.hpp"
#include <cassert>
#include <cmath>
#include <sstream>
using namespace easyknack;
int main(){
    AdditiveOscillatorNode node;
    {AdditiveOscillatorNode old;std::istringstream encoded("!e64d76dd5d57451d -1 sine 4 1 1200 -1 200 -1 0 inputs -1 -1 -1 -1");old.configure(encoded);assert(old.spread==2&&old.ratios[1]==4&&old.sources.size()==7);}

    for(float exponent : {0.f,.5f,1.f,1.4f,2.f,4.f}){
        node.updateSpectrum(1,exponent,144);assert(node.ratios[0]==1);
        for(unsigned h=1;h<144;++h){double expected=std::pow(h+1,exponent);assert(std::abs(node.ratios[h]-expected)<1e-12*std::max(1.,expected));}
    }
    node.updateSpectrum(0,2,2);node.updateSpectrum(0,2,40);
    for(unsigned h=1;h<40;++h){assert(node.weights[h]==1);assert(node.ratios[h]==double(h+1)*(h+1));}
    assert(node.ratios[1]*200==800&&node.ratios[2]*200==1800&&node.ratios[3]*200==3200);
    node.updateSpectrum(0,1,4);for(unsigned h=0;h<4;++h)assert(node.ratios[h]==h+1);
}
