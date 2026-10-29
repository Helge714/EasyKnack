#pragma once
#include "SpectrumStream.hpp"
namespace easyknack {
/** A shared noise timeline, analyzed with the input window. Overlapping frames use
 * the same samples, so increasing overlap does not increase randomization speed. */
struct SpectrumNoise {
    std::vector<std::complex<double>> bins;
    std::vector<double> weights;
    std::size_t length=0;
    SpectrumWindow window=SpectrumWindow::hann;
    double rms=1;
    void prepare(){bins.resize(SpectrumStream::maxSize);weights.resize(SpectrumStream::maxSize);length=0;}
    static double sample(std::int64_t position)noexcept{
        std::uint64_t h=static_cast<std::uint64_t>(position)+0x9e3779b97f4a7c15ULL;
        h=(h^(h>>30))*0xbf58476d1ce4e5b9ULL;
        h=(h^(h>>27))*0x94d049bb133111ebULL;h^=h>>31;
        return (double(h>>11)*(1./9007199254740992.)*2-1)*1.7320508075688772;
    }
    void analyze(const SpectrumStream& input,std::int64_t start)noexcept{
        if(length!=input.windowFrames()||window!=input.window){
            length=input.windowFrames();window=input.window;double energy=0;
            for(std::size_t i=0;i<length;i++){weights[i]=spectrumWindowValue(window,i,length);energy+=weights[i]*weights[i];}
            rms=std::sqrt(std::max(energy,1e-30));
        }
        for(std::size_t i=0;i<length;i++)bins[i]={sample(start+static_cast<std::int64_t>(i))*weights[i],0};
        std::fill(bins.begin()+length,bins.begin()+input.size,std::complex<double>{});
        spectrumTransform(bins.data(),input.size);
    }
};
}
