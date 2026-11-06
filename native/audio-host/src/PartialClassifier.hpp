#pragma once
#include "SpectrumStream.hpp"
#include "PartialClassifierCalibration.hpp"
#include <vector>
#include <algorithm>
namespace easyknack {
/** ZRR07 spectral-peak descriptors, with window/SNR-dependent calibrated bounds.
 * Scratch is prepared off the audio thread. Time moments use samples; the window
 * derivative uses radians/sample, so FCD is expressed in FFT bins.
 */
struct PartialClassifier {
    struct Descriptors {double bandwidth=0,duration=0,coherence=0;int left=0,right=0;};
    std::vector<std::complex<double>> spectrum,time,derivative;
    std::vector<double> derivativeRatio,weights;
    std::size_t fftSize=0,length=0;SpectrumWindow window=SpectrumWindow::hann;
    void prepare(){
        spectrum.resize(SpectrumStream::maxSize);time.resize(SpectrumStream::maxSize);derivative.resize(SpectrumStream::maxSize);
        derivativeRatio.resize(SpectrumFrame::maxWindowSize);weights.resize(SpectrumFrame::maxWindowSize);length=0;
    }
    static double windowDerivative(SpectrumWindow w,std::size_t n,std::size_t m)noexcept{
        constexpr double tau=6.283185307179586476925286766559;double a=tau*n/m;
        if(w==SpectrumWindow::hann)return tau/m*.5*std::sin(a);
        if(w==SpectrumWindow::hamming)return tau/m*.46*std::sin(a);
        if(w==SpectrumWindow::blackman)return tau/m*(.5*std::sin(a)-.16*std::sin(2*a));
        return 0;
    }
    void analyze(const StreamSpectrumFrame& frame,std::size_t size,std::size_t m,SpectrumWindow w)noexcept{
        if(length!=m||window!=w){
            length=m;window=w;auto effective=w==SpectrumWindow::rectangular?SpectrumWindow::hann:w;
            for(std::size_t n=0;n<m;n++){
                weights[n]=spectrumWindowValue(effective,n,m);
                derivativeRatio[n]=weights[n]>1e-12?windowDerivative(effective,n,m)/weights[n]:0;
            }
        }
        fftSize=size;std::copy_n(frame.bins.data(),size,time.data());spectrumTransform(time.data(),size,true);
        for(std::size_t n=0;n<m;n++){
            auto y=time[n];
            // A rectangular frame is reanalysed with Hann: the smooth-window
            // derivative classifier is undefined at rectangular discontinuities.
            if(w==SpectrumWindow::rectangular)y*=weights[n];
            spectrum[n]=y;derivative[n]=y*derivativeRatio[n];time[n]=y*(double(n)-m*.5);
        }
        std::fill(spectrum.begin()+m,spectrum.begin()+size,std::complex<double>{});
        std::fill(time.begin()+m,time.begin()+size,std::complex<double>{});
        std::fill(derivative.begin()+m,derivative.begin()+size,std::complex<double>{});
        if(w==SpectrumWindow::rectangular)spectrumTransform(spectrum.data(),size);
        else std::copy_n(frame.bins.data(),size,spectrum.data());
        spectrumTransform(time.data(),size);spectrumTransform(derivative.data(),size);
    }
    Descriptors measure(int peak)const noexcept{
        Descriptors d;const int last=int(fftSize/2);int left=peak,right=peak;
        while(left>0&&std::norm(spectrum[left-1])<std::norm(spectrum[left]))--left;
        while(right<last&&std::norm(spectrum[right+1])<std::norm(spectrum[right]))++right;
        d.left=left;d.right=right;
        double power=0,frequency=0,frequency2=0,moment=0,moment2=0;d.coherence=1e30;
        // Offset frequency coordinates avoid cancellation for high-bin peaks.
        for(int k=left;k<=right;k++){
            double p=std::norm(spectrum[k]),x=k-peak;power+=p;frequency+=x*p;frequency2+=x*x*p;
            moment+=std::real(time[k]*std::conj(spectrum[k]));moment2+=std::norm(time[k]);
        }
        if(!(power>1e-30)){d.bandwidth=d.duration=1e30;return d;}
        for(int k=left;k<=right;k++)if(std::norm(spectrum[k])>power*1e-12){
            double offset=std::abs(std::imag(derivative[k]*std::conj(spectrum[k]))/std::norm(spectrum[k]));
            d.coherence=std::min(d.coherence,offset*fftSize/6.2831853071795864769);
        }
        d.bandwidth=std::sqrt(std::max(0.,frequency2/power-std::pow(frequency/power,2)))/(right-left+1);
        d.duration=std::sqrt(std::max(0.,moment2/power-std::pow(moment/power,2)))/length;
        return d;
    }
    PartialClassifierBounds bounds(double snr)const noexcept{
        unsigned w=window==SpectrumWindow::hamming?1:window==SpectrumWindow::blackman?2:0;
        unsigned padding=fftSize>length?1:0;
        auto& table=partialClassifierBounds[w][padding];
        constexpr auto count=sizeof(partialClassifierSnrs)/sizeof(partialClassifierSnrs[0]);
        snr=std::clamp(snr,partialClassifierSnrs[0],partialClassifierSnrs[count-1]);unsigned i=0;while(i+2<count&&snr>partialClassifierSnrs[i+1])++i;
        double t=(snr-partialClassifierSnrs[i])/(partialClassifierSnrs[i+1]-partialClassifierSnrs[i]);
        const auto& a=table[i];const auto& b=table[i+1];
        return {a.nbdMax+t*(b.nbdMax-a.nbdMax),a.nddMin+t*(b.nddMin-a.nddMin),a.nddMax+t*(b.nddMax-a.nddMax)};
    }
    bool accepts(const Descriptors& d,double snr)const noexcept{
        auto b=bounds(snr);return d.coherence<double(fftSize)/length&&d.bandwidth<=b.nbdMax&&d.duration>=b.nddMin&&d.duration<=b.nddMax;
    }
    double margin(const Descriptors& d,double snr)const noexcept{
        auto b=bounds(snr);double half=(b.nddMax-b.nddMin)*.5;
        return std::clamp(std::min({1-d.bandwidth/b.nbdMax,
            (d.duration-b.nddMin)/half,(b.nddMax-d.duration)/half,
            1-d.coherence/(double(fftSize)/length)}),0.,1.);
    }
};
}
