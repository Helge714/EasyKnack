#pragma once
#include <complex>
#include <cmath>
#include <vector>
#include <stdexcept>

namespace easyknack {
/** Worker-only mixed-radix inverse DFT for 2/3/5-smooth table lengths.
 * A separate output array keeps strided recursion allocation-free. */
inline void sineCloudInversePart(const std::complex<double>* input,std::size_t stride,
                                std::complex<double>* output,std::size_t size){
    if(size==1){*output=*input;return;}
    const unsigned radix=size%2==0?2:size%3==0?3:5;
    if(size%radix)throw std::invalid_argument("Unsupported Sine Cloud transform length");
    const std::size_t part=size/radix;
    for(unsigned q=0;q<radix;q++)sineCloudInversePart(input+q*stride,stride*radix,output+q*part,part);
    constexpr double tau=6.283185307179586476925286766559;
    const auto step=std::polar(1.,tau/size);
    std::complex<double> phase=1;
    std::complex<double> roots[5];for(unsigned j=0;j<radix;j++)roots[j]=std::polar(1.,tau*j/radix);
    for(std::size_t k=0;k<part;k++){
        std::complex<double> values[5],factor=1;
        for(unsigned q=0;q<radix;q++){values[q]=output[q*part+k]*factor;factor*=phase;}
        if(radix==2){output[k]=values[0]+values[1];output[part+k]=values[0]-values[1];}
        else for(unsigned j=0;j<radix;j++){
            std::complex<double> sum=0;
            for(unsigned q=0;q<radix;q++)sum+=values[q]*roots[(q*j)%radix];
            output[j*part+k]=sum;
        }
        phase*=step;
        // Bound recurrence drift for long tables without a trig call per bin.
        if((k&1023)==1023)phase=std::polar(1.,tau*(k+1)/size);
    }
}
inline std::vector<std::complex<double>> sineCloudInverse(const std::vector<std::complex<double>>& bins){
    std::vector<std::complex<double>> result(bins.size());
    sineCloudInversePart(bins.data(),1,result.data(),bins.size());
    for(auto& x:result)x/=double(bins.size());
    return result;
}
}
