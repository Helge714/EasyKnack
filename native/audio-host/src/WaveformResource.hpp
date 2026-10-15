#pragma once
#include "DspResources.hpp"
#include "Wavetables.hpp"
#include "Spectrum.hpp"
#include <memory>
namespace easyknack {
/** One periodic cycle, independent of host block size. Constructed off the render thread. */
struct WaveformResource final : DspResource {
    static constexpr std::size_t size=Wavetables::tableSize, levels=Wavetables::levels;
    std::array<std::array<float,size>,levels> tables{};
    std::array<float,size> samples{};
    ResourceKind kind()const noexcept override{return ResourceKind::waveform;}
    void build(std::array<std::complex<double>,size> bins,bool normalize=false)noexcept {
        std::array<std::complex<double>,size> work{};
        double scale=1;
        if(normalize){work=bins;spectrumTransform(work.data(),size,true);double peak=0;for(auto v:work)peak=std::max(peak,std::abs(v.real()));if(peak>0)scale=1/peak;}
        work=bins;spectrumTransform(work.data(),size,true);
        for(std::size_t i=0;i<size;i++)samples[i]=static_cast<float>(work[i].real()*scale);
        for(std::size_t l=0;l<levels;l++){
            work.fill({});auto limit=std::min(size/2-1,std::size_t{1}<<l);
            work[0]=bins[0];for(std::size_t k=1;k<=limit;k++){work[k]=bins[k];work[size-k]=std::conj(bins[k]);}
            spectrumTransform(work.data(),size,true);
            for(std::size_t i=0;i<size;i++)tables[l][i]=static_cast<float>(work[i].real()*scale);
        }
    }
    void fromSamples(const std::array<float,size>& samples)noexcept {
        std::array<std::complex<double>,size> bins{};for(std::size_t i=0;i<size;i++)bins[i]=samples[i];
        spectrumTransform(bins.data(),size);build(bins);this->samples=samples;
    }
    float sample(double phase,double increment)const noexcept{
        if(!std::isfinite(phase)||!std::isfinite(increment)||std::abs(increment)>=.5)return 0;
        unsigned level=0;while(level+1<levels&&std::abs(increment)*(1u<<(level+1))<.5)++level;
        double position=Wavetables::wrap(phase)*size;auto index=static_cast<std::size_t>(position);
        return static_cast<float>(tables[level][index]+(tables[level][(index+1)%size]-tables[level][index])*(position-index));
    }
};
inline const WaveformResource* waveformResource(const DspResource* r)noexcept{return r&&r->kind()==ResourceKind::waveform?static_cast<const WaveformResource*>(r):nullptr;}
}
