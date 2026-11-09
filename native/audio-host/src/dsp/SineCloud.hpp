#pragma once
#include "../analysis/Spectrum.hpp"
#include <atomic>
#include <thread>
#include <chrono>
#include <vector>
#include <algorithm>
#include <cstdint>

namespace easyknack {
/** Bounded, periodic research stimulus. No dependency on the streaming FFT limits. */
struct SineCloudTable {
    static constexpr std::size_t minimumSize=65536,maximumSize=67108864;
    static constexpr unsigned maximumCount=10000000;
    // Peak density of a normal distribution truncated to +/-3 sigma, per unit range.
    static constexpr double gaussianDensity=2.401;
    struct Settings {
        unsigned count=10000,seed=0;
        double minimum=20,maximum=20000;
        bool gaussian=false,randomPhases=true;
        bool operator==(const Settings& p)const{return count==p.count&&seed==p.seed&&minimum==p.minimum&&maximum==p.maximum&&gaussian==p.gaussian&&randomPhases==p.randomPhases;}
    };
    std::vector<float> samples;
    unsigned count=0;
    std::size_t first=0,last=0;
    static std::uint64_t random(std::uint64_t& state){
        auto x=(state+=0x9e3779b97f4a7c15ULL);x=(x^(x>>30))*0xbf58476d1ce4e5b9ULL;
        x=(x^(x>>27))*0x94d049bb133111ebULL;return x^(x>>31);
    }
    // Acklam's rational inverse-normal approximation; absolute error near 1e-8
    // over the retained +/-3 sigma range. Quantiles keep every selected bin unique.
    static double normalQuantile(double p){
        constexpr double a1=-39.6968302866538,a2=220.946098424521,a3=-275.928510446969,
            a4=138.357751867269,a5=-30.6647980661472,a6=2.50662827745924;
        constexpr double b1=-54.4760987982241,b2=161.585836858041,b3=-155.698979859887,
            b4=66.8013118877197,b5=-13.2806815528857;
        constexpr double c1=-.00778489400243029,c2=-.322396458041136,c3=-2.40075827716184,
            c4=-2.54973253934373,c5=4.37466414146497,c6=2.93816398269878;
        constexpr double d1=.00778469570904146,d2=.32246712907004,d3=2.445134137143,d4=3.75440866190742;
        if(p<.02425||p>1-.02425){
            double q=std::sqrt(-2*std::log(p<.5?p:1-p));
            double x=(((((c1*q+c2)*q+c3)*q+c4)*q+c5)*q+c6)/((((d1*q+d2)*q+d3)*q+d4)*q+1);
            return p<.5?x:-x;
        }
        double q=p-.5,r=q*q;
        return (((((a1*r+a2)*r+a3)*r+a4)*r+a5)*r+a6)*q/(((((b1*r+b2)*r+b3)*r+b4)*r+b5)*r+1);
    }
    static double gaussianPosition(double q){
        if(q<=0)return 0;if(q>=1)return 1;
        constexpr double tail=.0013498980316300959;
        return std::clamp(.5+normalQuantile(tail+(1-2*tail)*q)/6,0.,1.);
    }
    struct Plan {std::size_t size,first,last;unsigned count;};
    static Plan plan(Settings p,double rate){
        double lo=std::clamp(std::min(p.minimum,p.maximum),.00001,rate*.5);
        double hi=std::clamp(std::max(p.minimum,p.maximum),lo,rate*.5);
        p.count=std::clamp(p.count,1u,maximumCount);
        std::size_t size=minimumSize;
        double density=p.gaussian?gaussianDensity:1.;
        double needed=p.count>1&&hi>lo?density*rate*(p.count-1)/(hi-lo):minimumSize;
        while(size<maximumSize&&size<needed)size*=2;
        auto first=std::clamp<std::size_t>(std::size_t(std::ceil(lo*size/rate)),1,size/2-1);
        auto last=std::clamp<std::size_t>(std::size_t(std::floor(hi*size/rate)),1,size/2-1);
        if(last<first)first=last=std::clamp<std::size_t>(std::size_t(std::llround((lo+hi)*.5*size/rate)),1,size/2-1);
        unsigned count=std::min<std::size_t>(p.count,std::size_t((last-first)/density)+1);
        if(count==1)last=first=std::clamp<std::size_t>(std::size_t(std::llround((lo+hi)*.5*size/rate)),1,size/2-1);
        return {size,first,last,count};
    }
    void build(Settings p,double rate){
        auto layout=plan(p,rate);auto size=layout.size;first=layout.first;last=layout.last;count=layout.count;
        std::vector<std::complex<double>> bins(size);
        std::uint64_t rng=p.seed;
        constexpr double tau=6.283185307179586476925286766559;
        for(unsigned j=0;j<count;j++){
            auto bin=count==1?first:first+(std::uint64_t(j)*(last-first)+(count-1)/2)/(count-1);
            if(p.gaussian&&count>1)bin=first+std::size_t(std::llround((last-first)*gaussianPosition(double(j)/(count-1))));
            double phase=p.randomPhases?(random(rng)>>11)*0x1.0p-53*tau:-tau/4; // IFFT cosine phase -> sine phase zero.
            bins[bin]=std::polar(1.,phase);bins[size-bin]=std::conj(bins[bin]);
        }
        spectrumTransform(bins.data(),size,true);
        double peak=0;for(const auto& x:bins)peak=std::max(peak,std::abs(x.real()));
        samples.resize(size);
        for(std::size_t i=0;i<size;i++)samples[i]=peak>0?float(bins[i].real()/peak):0.f;
    }
};

/** Three ownership slots: the worker alone allocates; audio only reads or releases.
 * Requests coalesce without locks, waits, allocation or resource destruction on audio.
 * Destruction/join belongs to the control thread with the enclosing processor. */
class SineCloudWorker {
public:
    enum State {free,building,ready,playing};
    struct Slot {std::atomic<int> state{free};std::uint64_t version=0;SineCloudTable table;};
    Slot slots[3];
private:
    const double rate;
    std::atomic<bool> stopping{false};
    std::atomic<std::uint64_t> version{0};
    std::atomic<unsigned> count{10000},seed{0};
    std::atomic<bool> gaussian{false},randomPhases{true};
    std::atomic<double> minimum{20},maximum{20000};
    SineCloudTable::Settings submitted;
    bool requested=false;
    std::thread thread;
    void loop(){
        std::uint64_t completed=0;
        while(!stopping.load()){
            auto v=version.load();
            if(v&&!(v&1)&&v!=completed){
                SineCloudTable::Settings p{count.load(),seed.load(),minimum.load(),maximum.load(),gaussian.load(),randomPhases.load()};
                if(version.load()!=v)continue;
                for(auto& slot:slots){
                    int expected=free;
                    if(!slot.state.compare_exchange_strong(expected,building))continue;
                    try {
                        slot.table.build(p,rate);slot.version=v;
                        slot.state.store(version.load()==v?ready:free);
                    }catch(...){slot.state.store(free);} // Retain the previous playable table on failure.
                    completed=v;break;
                }
            }
            std::this_thread::sleep_for(std::chrono::milliseconds(5));
        }
    }
public:
    explicit SineCloudWorker(double sampleRate):rate(sampleRate),thread([this]{loop();}){}
    ~SineCloudWorker(){stopping.store(true);thread.join();}
    std::uint64_t request(SineCloudTable::Settings p)noexcept{
        if(!p.randomPhases)p.seed=0; // Seed changes cannot restart an unrandomized stimulus.
        if(!requested||!(p==submitted)){
            version.fetch_add(1);count.store(p.count);seed.store(p.seed);minimum.store(p.minimum);maximum.store(p.maximum);gaussian.store(p.gaussian);randomPhases.store(p.randomPhases);
            submitted=p;requested=true;version.fetch_add(1);
        }
        return version.load();
    }
};
}
