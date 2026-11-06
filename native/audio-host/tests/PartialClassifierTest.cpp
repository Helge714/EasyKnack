#include "PartialClassifier.hpp"
#include <cassert>
#include <random>
#include <iostream>
#include <cstdlib>
#include <new>
using namespace easyknack;
static bool watching=false;static unsigned allocations=0;
void* operator new(std::size_t n){if(watching)++allocations;if(auto* p=std::malloc(n?n:1))return p;throw std::bad_alloc();}
void operator delete(void* p)noexcept{std::free(p);}void operator delete(void* p,std::size_t)noexcept{std::free(p);}
constexpr double tau=6.2831853071795864769;
static int maximum(const StreamSpectrumFrame& frame,unsigned size){int peak=1;for(unsigned k=2;k<size/2;k++)if(std::norm(frame.bins[k])>std::norm(frame.bins[peak]))peak=k;return peak;}
int main(){
    unsigned heldout=0,recognized=0,noisePeaks=0,noiseAccepted=0;
    std::mt19937 random(87654321);std::normal_distribution<double> normal;
    for(auto window:{SpectrumWindow::rectangular,SpectrumWindow::hann,SpectrumWindow::hamming,SpectrumWindow::blackman})for(unsigned padding:{1u,2u}){
        PartialClassifier classifier;classifier.prepare();
        for(unsigned length:{256u,1024u,4096u,16384u}){
            unsigned size=length*padding;StreamSpectrumFrame frame;frame.bins.resize(size);std::vector<std::complex<double>> time(size),derivative(size);
            auto effective=window==SpectrumWindow::rectangular?SpectrumWindow::hann:window;
            // Direct time-domain construction is independent of the inverse-FFT
            // recovery used by the production descriptor implementation.
            for(unsigned n=0;n<length;n++){
                double signal=std::cos(tau*(.13+.17/size)*n+.71),w=spectrumWindowValue(window,n,length),v=spectrumWindowValue(effective,n,length);
                frame.bins[n]=signal*w;time[n]=(n-length*.5)*signal*v;derivative[n]=signal*PartialClassifier::windowDerivative(effective,n,length);
            }
            spectrumTransform(frame.bins.data(),size);spectrumTransform(time.data(),size);spectrumTransform(derivative.data(),size);
            watching=true;classifier.analyze(frame,size,length,window);auto d=classifier.measure(maximum(frame,size));
            double error=0,power=0;
            for(unsigned k=0;k<size;k++){error+=std::norm(classifier.time[k]-time[k])+std::norm(classifier.derivative[k]-derivative[k]);power+=std::norm(time[k])+std::norm(derivative[k]);}
            assert(error/std::max(power,1e-30)<1e-18);assert(d.duration>.09&&d.duration<.21);assert(classifier.accepts(d,30));
            auto previous=classifier.bounds(5);
            for(double snr=6;snr<=60;snr++){
                auto next=classifier.bounds(snr);assert(next.nbdMax<=previous.nbdMax+1e-15&&next.nddMin>=previous.nddMin-1e-15&&next.nddMax<=previous.nddMax+1e-15);previous=next;
            }
            watching=false;assert(!allocations);
        }
        // New phases, fractional-bin positions, lengths and RNG seed: not the
        // calibration samples. Smooth windows are those modelled by ZRR07.
        if(window==SpectrumWindow::rectangular)continue;
        constexpr unsigned length=2048;unsigned size=length*padding;StreamSpectrumFrame frame;frame.bins.resize(size);
        for(unsigned test=0;test<32;test++){
            double alpha=.173+test*2.39996,beta=.731+test*1.32472,frequency=(113.137+test*.031)/size;
            for(unsigned n=0;n<length;n++){
                double modulation=tau*n/(4.2*length);
                double value=std::cos(tau*frequency*n+10*std::sin(modulation+alpha))*(1+.5*std::cos(2*modulation+beta));
                frame.bins[n]=value*spectrumWindowValue(window,n,length);
            }
            std::fill(frame.bins.begin()+length,frame.bins.end(),std::complex<double>{});spectrumTransform(frame.bins.data(),size);
            double windowPower=0;for(unsigned n=0;n<length;n++)windowPower+=std::pow(spectrumWindowValue(window,n,length),2);
            double sigma=std::abs(frame.bins[maximum(frame,size)])/std::sqrt(windowPower)*std::pow(10.,-35./20);
            spectrumTransform(frame.bins.data(),size,true);
            for(unsigned n=0;n<length;n++)frame.bins[n]+=sigma*normal(random)*spectrumWindowValue(window,n,length);
            spectrumTransform(frame.bins.data(),size);
            watching=true;classifier.analyze(frame,size,length,window);recognized+=classifier.accepts(classifier.measure(maximum(frame,size)),30);++heldout;watching=false;
        }
        for(unsigned test=0;test<16;test++){
            for(unsigned n=0;n<length;n++)frame.bins[n]=normal(random)*spectrumWindowValue(window,n,length);
            std::fill(frame.bins.begin()+length,frame.bins.end(),std::complex<double>{});spectrumTransform(frame.bins.data(),size);
            watching=true;classifier.analyze(frame,size,length,window);
            for(unsigned k=1;k<size/2;k++)if(std::norm(frame.bins[k])>std::norm(frame.bins[k-1])&&std::norm(frame.bins[k])>=std::norm(frame.bins[k+1])){
                auto d=classifier.measure(k);++noisePeaks;noiseAccepted+=classifier.accepts(d,30);assert(!classifier.accepts(d,40)||classifier.accepts(d,20));
            }
            watching=false;
        }
        assert(!allocations);
    }
    std::cout<<"Held-out modulated sinusoids "<<recognized<<"/"<<heldout<<"; noise peaks "<<noiseAccepted<<"/"<<noisePeaks<<"\n";
    assert(double(recognized)/heldout>.95);assert(double(noiseAccepted)/noisePeaks<.05);
    std::cout<<"Adaptive descriptors, direct-DFT parity, window scaling, SNR monotonicity and allocation checks passed\n";
}
