#pragma once
#include "../analysis/Spectrum.hpp"
#include "SineCloudTransform.hpp"
#include <atomic>
#include <thread>
#include <chrono>
#include <vector>
#include <algorithm>
#include <cstdint>
#include <numeric>

namespace easyknack {
/** Bounded, periodic research stimulus. No dependency on the streaming FFT limits. */
struct SineCloudTable {
    static constexpr std::size_t minimumSize=65536,maximumSize=67108864;
    static constexpr unsigned maximumCount=10000000;
    struct Settings {
        unsigned count=10000,seed=0;
        double minimum=20,maximum=20000;
        bool gaussian=false,randomPhases=true; // Gaussian retained only for legacy source layout; ignored.
        double binSpacing=.01;
        bool operator==(const Settings& p)const{return count==p.count&&seed==p.seed&&minimum==p.minimum&&maximum==p.maximum&&gaussian==p.gaussian&&randomPhases==p.randomPhases&&binSpacing==p.binSpacing;}
    };
    std::vector<float> samples;
    unsigned count=0;
    std::size_t first=0,last=0;
    double periodSeconds=0;
    static std::uint64_t random(std::uint64_t& state){
        auto x=(state+=0x9e3779b97f4a7c15ULL);x=(x^(x>>30))*0xbf58476d1ce4e5b9ULL;
        x=(x^(x>>27))*0x94d049bb133111ebULL;return x^(x>>31);
    }
    struct Plan {std::size_t size,first,last;unsigned count;};
    static Plan plan(Settings p,double rate){
        double lo=std::clamp(std::min(p.minimum,p.maximum),.00001,rate*.5);
        double hi=std::clamp(std::max(p.minimum,p.maximum),lo,rate*.5);
        p.count=std::clamp(p.count,1u,maximumCount);
        if(!std::isfinite(p.binSpacing)||p.binSpacing<=0)throw std::invalid_argument("Invalid Sine Cloud bin spacing");
        const double needed=std::ceil(rate/p.binSpacing);
        std::size_t size=maximumSize+1;
        for(std::size_t a=1;a<=maximumSize;a*=2)
            for(std::size_t b=a;b<=maximumSize;b*=3)
                for(std::size_t c=b;c<=maximumSize;c*=5)
                    if(c>=minimumSize&&c>=needed&&c<size)size=c;
        if(size>maximumSize)throw std::length_error("Sine Cloud bin spacing exceeds table memory limit");
        const auto highest=(size-1)/2; // Odd lengths have no Nyquist bin.
        auto first=std::clamp<std::size_t>(std::size_t(std::ceil(lo*size/rate)),1,highest);
        auto last=std::clamp<std::size_t>(std::size_t(std::floor(hi*size/rate)),1,highest);
        if(last<first)first=last=std::clamp<std::size_t>(std::size_t(std::llround((lo+hi)*.5*size/rate)),1,highest);
        unsigned count=std::min<std::size_t>(p.count,last-first+1);
        if(count==1)last=first=std::clamp<std::size_t>(std::size_t(std::llround((lo+hi)*.5*size/rate)),1,highest);
        return {size,first,last,count};
    }
    void build(Settings p,double rate){
        auto layout=plan(p,rate);auto size=layout.size;first=layout.first;last=layout.last;count=layout.count;
        std::vector<std::complex<double>> bins(size);
        std::uint64_t rng=p.seed;
        std::size_t fundamental=0;
        constexpr double tau=6.283185307179586476925286766559;
        for(unsigned j=0;j<count;j++){
            auto bin=count==1?first:first+(std::uint64_t(j)*(last-first)+(count-1)/2)/(count-1);
            fundamental=std::gcd(fundamental,bin);
            double phase=p.randomPhases?(random(rng)>>11)*0x1.0p-53*tau:-tau/4; // IFFT cosine phase -> sine phase zero.
            bins[bin]=std::polar(1.,phase);bins[size-bin]=std::conj(bins[bin]);
        }
        periodSeconds=double(size)/(rate*fundamental);
        bins=sineCloudInverse(bins);
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
    std::atomic<bool> randomPhases{true};
    std::atomic<double> minimum{20},maximum{20000},binSpacing{.01};
    SineCloudTable::Settings submitted;
    bool requested=false;
    std::thread thread;
    void loop(){
        std::uint64_t completed=0;
        while(!stopping.load()){
            auto v=version.load();
            if(v&&!(v&1)&&v!=completed){
                SineCloudTable::Settings p{count.load(),seed.load(),minimum.load(),maximum.load(),false,randomPhases.load(),binSpacing.load()};
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
        p.gaussian=false; // Retained only for old source configurations.
        if(!p.randomPhases)p.seed=0; // Seed changes cannot restart an unrandomized stimulus.
        if(!requested||!(p==submitted)){
            version.fetch_add(1);count.store(p.count);seed.store(p.seed);minimum.store(p.minimum);maximum.store(p.maximum);binSpacing.store(p.binSpacing);randomPhases.store(p.randomPhases);
            submitted=p;requested=true;version.fetch_add(1);
        }
        return version.load();
    }
};
}
