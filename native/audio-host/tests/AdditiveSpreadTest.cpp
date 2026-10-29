#include "nodes/AdditiveOscillatorNode.hpp"
#include <cassert>
#include <cmath>
using namespace easyknack;
int main(){
    AdditiveOscillatorNode node;
    for(float cents : {100.f,-100.f,1200.f,-1200.f,0.f}){
        node.updateSpectrum(1,cents,144);
        assert(node.ratios[0]==1);
        for(unsigned h=1;h<144;++h)
            assert(std::abs(node.ratios[h]-(h+1)*std::exp2(cents/1200.0))<1e-9);
    }
    // Changing only Count must keep every detuned ratio and reveal the new weights.
    node.updateSpectrum(0,100,2);node.updateSpectrum(0,100,40);
    for(unsigned h=1;h<40;++h){assert(node.weights[h]==1);assert(node.ratios[h]>(h+1));}
}
