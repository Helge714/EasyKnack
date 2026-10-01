#include "NodeProcessor.hpp"
#include "nodes/MathNode.hpp"
#include <array>
#include <chrono>
#include <iostream>
#include <algorithm>
using namespace easyknack;
volatile double benchmarkSink=0;
double measure(bool scalar){
    std::array<float,256> audio{},control{},out{},silence{};
    for(unsigned i=0;i<256;i++)audio[i]=float(i)/255;
    const float* inputs[]{audio.data(),control.data()};char rates[]{'a',scalar?'c':'a'};
    ProcessContext c{inputs,2,out.data(),256,48000,silence.data(),256};c.sourceRates=rates;
    MathNode math;constexpr unsigned blocks=100000;
    double sum=0;auto start=std::chrono::steady_clock::now();
    for(unsigned block=0;block<blocks;block++){
        float value=float(block%1024)/1024;
        if(scalar)control[0]=value;else control.fill(value);
        math.process(c);for(float sample:out)sum+=sample;
    }
    auto stop=std::chrono::steady_clock::now();benchmarkSink=sum;
    return std::chrono::duration<double,std::nano>(stop-start).count()/blocks;
}
int main(){
    std::array<double,9> scalar{},expanded{};
    for(unsigned run=0;run<9;run++)if(run%2){scalar[run]=measure(true);expanded[run]=measure(false);}else{expanded[run]=measure(false);scalar[run]=measure(true);}
    std::sort(scalar.begin(),scalar.end());std::sort(expanded.begin(),expanded.end());
    std::cout<<"median ns/block (256 samples): scalar="<<scalar[4]<<", expanded="<<expanded[4]<<"\n";
}
