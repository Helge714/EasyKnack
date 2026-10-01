#pragma once
#include <array>
#include <complex>
#include <cmath>
#include <cstdint>
#include <string>
#include <stdexcept>

namespace easyknack {
enum class SpectrumWindow { rectangular, hann, hamming, blackman };
inline SpectrumWindow spectrumWindow(const std::string& name){
    if(name=="hann")return SpectrumWindow::hann;
    if(name=="hamming")return SpectrumWindow::hamming;
    if(name=="blackman")return SpectrumWindow::blackman;
    if(name=="rectangular")return SpectrumWindow::rectangular;
    throw std::runtime_error("Unknown FFT window");
}
/** Periodic analysis windows. The matching synthesis window/overlap normalization
 * will belong to a future IFFT processor, not to these raw complex coefficients. */
inline double spectrumWindowValue(SpectrumWindow window,std::size_t i,std::size_t size)noexcept{
    constexpr double tau=6.283185307179586476925286766559;
    double phase=tau*static_cast<double>(i)/size;
    switch(window){
        case SpectrumWindow::hann:return .5-.5*std::cos(phase);
        case SpectrumWindow::hamming:return .54-.46*std::cos(phase);
        case SpectrumWindow::blackman:return .42-.5*std::cos(phase)+.08*std::cos(2*phase);
        default:return 1;
    }
}
/** In-place radix-2 transform: raw forward DFT; inverse divides by N. No allocation.
 * Caller validates power-of-two size outside the audio thread. */
inline void spectrumTransform(std::complex<double>* data,std::size_t size,bool inverse=false)noexcept{
    for(std::size_t i=1,j=0;i<size;i++){
        std::size_t bit=size>>1;for(;j&bit;bit>>=1)j^=bit;j^=bit;
        if(i<j)std::swap(data[i],data[j]);
    }
    for(std::size_t length=2;length<=size;length<<=1){
        double angle=(inverse?2:-2)*3.14159265358979323846/length;
        std::complex<double> step(std::cos(angle),std::sin(angle));
        for(std::size_t base=0;base<size;base+=length){
            std::complex<double> w(1,0);
            for(std::size_t j=0;j<length/2;j++){
                auto even=data[base+j],odd=data[base+j+length/2]*w;
                data[base+j]=even+odd;data[base+j+length/2]=even-odd;w*=step;
            }
        }
    }
    if(inverse)for(std::size_t i=0;i<size;i++)data[i]/=static_cast<double>(size);
}
/** Immutable while borrowed. Full complex spectrum, including negative frequencies,
 * DC and Nyquist. Phase is referenced to startSample, with no DC removal or rotation.
 * Forward scaling is 1; inverse scaling is 1/fftFrames. These are asynchronous
 * analysis snapshots: frameIndex gaps are permitted, not an IFFT audio stream. */
struct SpectrumFrame {
    static constexpr std::size_t capacity=16384;
    std::array<std::complex<double>,capacity> bins{};
    std::size_t fftFrames=0,windowFrames=0,hopFrames=0;
    std::uint64_t startSample=0,endSample=0,frameIndex=0,sourceIdentity=0;
    double sampleRate=0,windowSum=0,windowSumSquares=0;
    SpectrumWindow window=SpectrumWindow::hann;
    bool dcRemoved=false;
    bool valid()const noexcept{return fftFrames!=0;}
};
}
