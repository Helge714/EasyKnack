#pragma once
#include "SpectrumStream.hpp"
namespace easyknack {
constexpr double partialTau=6.283185307179586476925286766559;
struct Partial {
    std::uint64_t id=0;std::size_t slot=0;
    double frequency=0,amplitude=0,phase=0,age=0;
    double originalFrequency=0,originalAmplitude=0,originalPhase=0;
    double tonality=0;
};
struct PartialFrame {std::int64_t start=0;std::size_t count=0;std::vector<Partial> values;};
/** Borrowed frame batches; stable track IDs, original parameters and exact STFT convention. */
struct PartialsResource final : DspResource {
    std::size_t preparedCapacity=0;
    std::size_t size=0,windowSize=0,hop=0,count=0,blockFrames=0;bool dcRemoved=false;
    std::uint64_t identity=0,blockStart=0;double sampleRate=0;SpectrumWindow window=SpectrumWindow::hann;
    std::vector<PartialFrame> frames;
    ResourceKind kind()const noexcept override{return ResourceKind::partials;}
    void prepare(std::size_t block,std::size_t capacity=ResourcePreparation::capacity(ResourceKind::partials)){preparedCapacity=capacity;frames.resize((block+SpectrumStream::minHop-1)/SpectrumStream::minHop+1);for(auto& f:frames)f.values.resize(capacity);}
    template<class T> void metadata(const T& s)noexcept{size=s.size;windowSize=s.windowSize;hop=s.hop;count=std::min(s.count,frames.size());blockFrames=s.blockFrames;dcRemoved=s.dcRemoved;identity=s.identity;blockStart=s.blockStart;sampleRate=s.sampleRate;window=s.window;RenderCadence::include(hop);}
    bool copy(const PartialsResource& s)noexcept{for(std::size_t i=0;i<s.count;i++)if(s.frames[i].count>preparedCapacity){size=count=0;return false;}metadata(s);for(std::size_t i=0;i<count;i++){frames[i].start=s.frames[i].start;frames[i].count=s.frames[i].count;std::copy_n(s.frames[i].values.data(),frames[i].count,frames[i].values.data());}return true;}
    void spectrumMetadata(SpectrumStream& s)const noexcept{s.size=size;s.windowSize=windowSize;s.hop=hop;s.count=count;s.blockFrames=blockFrames;s.dcRemoved=dcRemoved;s.identity=identity;s.blockStart=blockStart;s.sampleRate=sampleRate;s.window=window;}
};
// Exact DFT of a periodic analysis window at a fractional bin; no sampled lookup.
inline std::complex<double> partialDirichlet(double radians,std::size_t length)noexcept{
    radians=std::remainder(radians,partialTau);double denominator=std::sin(radians*.5);
    if(std::abs(denominator)<1e-12)return {double(length),0};
    return std::polar(1.,-radians*(length-1)*.5)*(std::sin(length*radians*.5)/denominator);
}
inline std::complex<double> partialWindow(double radians,std::size_t length,SpectrumWindow window)noexcept{
    const auto d=partialDirichlet(radians,length);if(window==SpectrumWindow::rectangular)return d;
    const double omega=partialTau/length;const auto side=partialDirichlet(radians-omega,length)+partialDirichlet(radians+omega,length);
    if(window==SpectrumWindow::hann)return .5*d-.25*side;
    if(window==SpectrumWindow::hamming)return .54*d-.23*side;
    return .42*d-.25*side+.04*(partialDirichlet(radians-2*omega,length)+partialDirichlet(radians+2*omega,length));
}
/** Compact window-shaped complex lobes, with conjugate contributions at DC/Nyquist.
 * O(partials * padding ratio), rather than a full FFT or full-bin loop per partial.
 * Omitted sidelobes remain in the residual, so unchanged resynthesis is an identity.
 */
inline void addPartial(StreamSpectrumFrame& out,std::size_t size,std::size_t length,double sampleRate,SpectrumWindow window,double frequency,double amplitude,double phase,double gain=1)noexcept{
    if(!std::isfinite(frequency)||!std::isfinite(amplitude)||!std::isfinite(phase)||frequency<=0||frequency>=sampleRate*.5||amplitude<=0)return;
    const double bin=frequency*size/sampleRate,omega=partialTau*frequency/sampleRate;
    const int radius=int(std::ceil(8.*size/length));
    const auto positive=std::polar(amplitude*.5,phase),negative=std::conj(positive);
    for(int k=std::max(0,int(std::floor(bin))-radius);k<=std::min(int(size/2),int(std::ceil(bin))+radius);++k){
        const double angle=partialTau*k/size;
        out.bins[k]+=gain*(positive*partialWindow(angle-omega,length,window)+negative*partialWindow(angle+omega,length,window));
    }
}
}
