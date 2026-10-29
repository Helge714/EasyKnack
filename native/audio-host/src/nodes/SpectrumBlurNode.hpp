#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../SpectrumStream.hpp"
#include "../SpectrumNoise.hpp"
namespace easyknack {
struct SpectrumBlurNode : NodeProcessor {
    SpectrumStream stream;float width=0,factor=1,octaves=0;bool logarithmic=false,octaveMode=false;std::vector<double> changes,logPrefix;
    std::string kernel="gaussian";bool normalize=false,legacy=false,preservePhase=false;
    bool noiseMode=false;SpectrumNoise noise;
    std::vector<double> phaseOffset,amplitudeOffset;
    bool noiseHistory=false;
    std::uint64_t noiseIdentity=0;
    std::int64_t noiseStart=0;
    std::size_t noiseSize=0,noiseWindow=0;
    std::vector<double> magnitude,prefix,measure,positions,phaseSeeds;
    std::vector<std::size_t> maximumQueue;
    static constexpr std::size_t shells=32;
    std::array<double,shells> gaussian{};
    char executionRate()const noexcept override{return 'a';}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::spectrum:ResourceKind::none;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &stream;}
    void configure(std::istream& in)override{
        auto start=in.tellg();std::string marker;in>>marker;
        if(marker=="!4d61336fc66a85f1"){
            legacy=true;
            int spectrum,control;if(!(in>>spectrum>>control>>width)||!std::isfinite(width)||width<0||width>8192||spectrum< -1||spectrum>=2048||control< -1||control>=2048)throw std::runtime_error("Invalid legacy spectrum blur");
            sources={spectrum,control,-1,-1};factor=1;logarithmic=false;octaveMode=false;return;
        }
        if(marker=="!5098765edd81760e"){
            legacy=true;
            int spectrum,control,factorSource;std::string mode;
            if(!(in>>spectrum>>control>>width>>factorSource>>factor>>mode)||!std::isfinite(width)||!std::isfinite(factor)||width<0||width>8192||(mode!="bins"&&mode!="factor"))throw std::runtime_error("Invalid legacy spectrum blur");
            for(int s:{spectrum,control,factorSource})if(s< -1||s>=2048)throw std::runtime_error("Invalid source reference");
            sources={spectrum,control,factorSource,-1};logarithmic=mode=="factor";octaveMode=false;return;
        }
        if(marker=="!952cc8bcb1530996"){
            int a,b,d,e;std::string mode;
            if(!(in>>a>>b>>width>>d>>factor>>mode>>e>>octaves)||!std::isfinite(width)||!std::isfinite(factor)||!std::isfinite(octaves)||width<0||width>8192||factor<1||octaves<0||(mode!="bins"&&mode!="factor"&&mode!="octaves"))throw std::runtime_error("Invalid legacy spectrum blur");
            for(int s:{a,b,d,e})if(s< -1||s>=2048)throw std::runtime_error("Invalid source reference");
            sources={a,b,d,e};legacy=true;octaveMode=mode=="octaves";logarithmic=mode!="bins";return;
        }
        if(marker=="!e42c18dad91419dd"||marker=="!f9908cbb5233710c"){
            int a,b,d,e,n;std::string mode;
            if(!(in>>a>>b>>width>>d>>factor>>mode>>e>>octaves>>kernel>>n)||!std::isfinite(width)||!std::isfinite(factor)||!std::isfinite(octaves)||width<0||width>8192||factor<1||octaves<0||(mode!="bins"&&mode!="factor"&&mode!="octaves")||(kernel!="gaussian"&&kernel!="box"&&kernel!="maximum")||(n!=0&&n!=1))throw std::runtime_error("Invalid legacy spectrum blur");
            for(int s:{a,b,d,e})if(s< -1||s>=2048)throw std::runtime_error("Invalid source reference");
            int preserve=1;if(marker=="!f9908cbb5233710c"&&(!(in>>preserve)||(preserve!=0&&preserve!=1)))throw std::runtime_error("Invalid phase flag");
            sources={a,b,d,e};legacy=false;noiseMode=false;preservePhase=preserve!=0;normalize=n!=0;octaveMode=mode=="octaves";logarithmic=mode!="bins";return;
        }
        in.seekg(start);auto p=parameters::SpectrumBlurNodeParameters::read(in);sources=p.sources();width=p.width_value;factor=p.factor_value;octaves=p.octaves_value;octaveMode=p.mode_value=="octaves";logarithmic=p.mode_value!="bins";kernel=p.kernel_value;normalize=p.normalize_value;preservePhase=p.synthesis_value=="preserve";noiseMode=p.synthesis_value=="noise";legacy=false;
    }
    void applyParameters(const NodeProcessor& p)noexcept override{auto& other=static_cast<const SpectrumBlurNode&>(p);width=other.width;factor=other.factor;logarithmic=other.logarithmic;octaves=other.octaves;octaveMode=other.octaveMode;kernel=other.kernel;normalize=other.normalize;legacy=other.legacy;preservePhase=other.preservePhase;noiseMode=other.noiseMode;}
    void prepare(double,std::size_t block)override{
        noise.prepare();
        phaseOffset.assign(SpectrumStream::maxSize/2+1,0);amplitudeOffset.assign(phaseOffset.size(),0);noiseHistory=false;
        stream.prepare(block);changes.resize(SpectrumStream::maxSize/2+2);logPrefix.resize(changes.size());
        for(std::size_t k=1;k<logPrefix.size();k++)logPrefix[k]=logPrefix[k-1]+1./k;
        magnitude.resize(changes.size());prefix.resize(changes.size());measure.resize(changes.size());positions.resize(changes.size());maximumQueue.resize(changes.size());
        phaseSeeds.resize(changes.size());
        for(std::size_t k=0;k<phaseSeeds.size();k++){
            // Fixed, decorrelated offsets avoid phase-aligned impulses. No per-frame randomization.
            std::uint32_t h=static_cast<std::uint32_t>(k)+0x9e3779b9u;
            h=(h^(h>>16))*0x85ebca6bu;h=(h^(h>>13))*0xc2b2ae35u;h^=h>>16;
            phaseSeeds[k]=6.283185307179586*(double(h)/4294967296.);
        }
        for(std::size_t i=0;i<shells;i++){
            double x=3.*i/shells,y=3.*(i+1)/shells;
            gaussian[i]=std::exp(-.5*x*x)-(i+1==shells?0:std::exp(-.5*y*y));
        }
    }
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& old=static_cast<SpectrumBlurNode&>(previous);
        phaseOffset.swap(old.phaseOffset);amplitudeOffset.swap(old.amplitudeOffset);
        noiseHistory=old.noiseHistory;noiseIdentity=old.noiseIdentity;noiseStart=old.noiseStart;noiseSize=old.noiseSize;noiseWindow=old.noiseWindow;
    }
    void process(ProcessContext& c)noexcept override{
        if(legacy){legacyProcess(c);return;}
        auto* input=static_cast<const SpectrumStream*>(c.resource(0));
        if(!input||!input->size){stream.size=0;stream.count=0;noiseHistory=false;return;}
        const std::size_t last=input->size/2,first=logarithmic?1:0;
        double amount=c.numericInput(octaveMode?3:logarithmic?2:1,octaveMode?octaves:logarithmic?factor:width)[0];
        double radius=std::isfinite(amount)?(logarithmic?(octaveMode?std::max(0.,amount):std::log2(std::max(1.,amount))):std::max(0.,amount)):0.;
        radius=std::min(radius,logarithmic?std::log2(double(input->size)):double(last));
        if(radius==0){stream.copy(*input);noiseHistory=false;return;}
        if(!noiseMode)noiseHistory=false;
        stream.metadata(*input);
        const double noiseSpan=(logarithmic?std::sinh(radius*.6931471805599453):radius)*input->windowFrames()/input->size;
        // Prefix integrals over log-frequency cells avoid O(N^2) work at large radii.
        // Gaussian is approximated by 32 concentric boxes; maximum uses a monotonic queue.
        for(std::size_t k=first;k<=last;k++)positions[k]=logarithmic?std::log2(double(k)):double(k);
        for(std::size_t f=0;f<stream.count;f++){
            const auto& source=input->frames[f];auto& out=stream.frames[f];out.start=source.start;
            if(noiseMode){
                noise.analyze(*input,source.start);
                if(!noiseHistory||noiseIdentity!=input->identity||noiseSize!=input->size||noiseWindow!=input->windowFrames()||source.start-noiseStart!=static_cast<std::int64_t>(input->hop)){
                    std::fill(phaseOffset.begin(),phaseOffset.end(),0);std::fill(amplitudeOffset.begin(),amplitudeOffset.end(),0);
                }
                noiseHistory=true;noiseIdentity=input->identity;noiseSize=input->size;noiseWindow=input->windowFrames();noiseStart=source.start;
            }
            double before=0,after=0;prefix[first]=measure[first]=0;
            for(std::size_t k=0;k<=last;k++){
                double m=std::abs(source.bins[k]);magnitude[k]=std::isfinite(m)?m:0;
                if(k<first)continue;
                double cell=logarithmic?std::log2((k+.5)/(k-.5)):1.;
                before+=magnitude[k]*magnitude[k]*(k==0||k==last?1:2);
                // For noise, blur amplitude density per octave, then convert back to
                // power per linear FFT cell. A flat octave envelope has PSD proportional to 1/f.
                if(noiseMode&&logarithmic)magnitude[k]/=std::sqrt(cell);
                prefix[k+1]=prefix[k]+magnitude[k]*cell;measure[k+1]=measure[k]+cell;
            }
            if(logarithmic)out.bins[0]=source.bins[0];
            std::array<std::size_t,shells> lo{},hi{};lo.fill(first);hi.fill(first);
            std::size_t head=0,tail=0;
            for(std::size_t k=first;k<=last;k++){
                double value=0,weight=0;
                std::size_t count=kernel=="gaussian"?shells:1;
                for(std::size_t s=0;s<count;s++){
                    double r=radius*(count==1?1.:double(s+1)/shells);
                    while(lo[s]<=last&&positions[lo[s]]<positions[k]-r-1e-12)lo[s]++;
                    while(hi[s]<=last&&positions[hi[s]]<=positions[k]+r+1e-12){
                        if(kernel=="maximum"){
                            while(tail>head&&magnitude[maximumQueue[tail-1]]<=magnitude[hi[s]])tail--;
                            maximumQueue[tail++]=hi[s];
                        }
                        hi[s]++;
                    }
                    if(kernel=="maximum"){
                        while(head<tail&&maximumQueue[head]<lo[s])head++;
                        value=head<tail?magnitude[maximumQueue[head]]:0;weight=1;
                    }else{
                        double w=count==1?1.:gaussian[s];
                        value+=w*(prefix[hi[s]]-prefix[lo[s]]);weight+=w*(measure[hi[s]]-measure[lo[s]]);
                    }
                }
                value=weight>0?value/weight:0;
                if(noiseMode&&logarithmic)value*=std::sqrt(measure[k+1]-measure[k]);
                after+=value*value*(k==0||k==last?1:2);
                if(noiseMode){
                    // Radius measured against physical window resolution (not zero padding).
                    // Narrow kernels modulate the source's polar coordinates, never mix
                    // a second complex/audio signal into it. Both deviations vanish at zero.
                    const double span=(logarithmic?k:1)*noiseSpan;
                    const double x=std::clamp(span,0.,1.),depth=x*x*(3-2*x);
                    if(depth>=1){
                        phaseOffset[k]=amplitudeOffset[k]=0;
                        out.bins[k]=noise.bins[k]*(value/noise.rms);
                    }else if(k==0||k==last){
                        // Real-spectrum endpoints have no continuously variable phase.
                        out.bins[k]=source.bins[k];phaseOffset[k]=amplitudeOffset[k]=0;
                    }else{
                        const double delta=std::remainder(std::arg(noise.bins[k])-std::arg(source.bins[k]),6.283185307179586);
                        const double amplitude=std::abs(noise.bins[k])/noise.rms-1;
                        const double step=6.283185307179586*span*input->hop/input->windowFrames()/std::max(1-depth,1e-9);
                        const double limit=3.141592653589793*depth;
                        phaseOffset[k]=std::clamp(phaseOffset[k]+std::clamp(depth*delta-phaseOffset[k],-depth*step,depth*step),-limit,limit);
                        amplitudeOffset[k]+=(depth*amplitude-amplitudeOffset[k])*std::min(1.,step);
                        // A reduced width must also reduce an already accumulated deviation.
                        amplitudeOffset[k]=std::clamp(amplitudeOffset[k],-depth,depth*std::max(1.,std::abs(amplitude)));
                        out.bins[k]=std::polar(value*(1+amplitudeOffset[k]),std::arg(source.bins[k])+phaseOffset[k]);
                    }
                }
                else if(k==0)out.bins[k]={std::signbit(source.bins[k].real())?-value:value,0};
                else if(preservePhase){
                    if(k==last)out.bins[k]={std::signbit(source.bins[k].real())?-value:value,0};
                    else out.bins[k]=std::polar(value,std::arg(source.bins[k]));
                }else{
                    // Frames are local to frame.start. Advance by k*hop/N, not by the
                    // source tone's phase: copied leakage phases cancel in overlap-add.
                    // Integer modulo before conversion keeps long-running sessions precise.
                    auto n=static_cast<std::int64_t>(input->size);
                    auto cycles=((source.start%n)*static_cast<std::int64_t>(k))%n;
                    if(k==last)out.bins[k]={(source.start%2)?-value:value,0};
                    else out.bins[k]=std::polar(value,phaseSeeds[k]+6.283185307179586*double(cycles)/n);
                }
            }
            if(normalize&&after>0){double gain=std::sqrt(before/after);for(std::size_t k=first;k<=last;k++)out.bins[k]*=gain;}
            SpectrumStream::realSymmetry(out,stream.size);
        }
    }
    void legacyProcess(ProcessContext& c)noexcept {
        auto* input=static_cast<const SpectrumStream*>(c.resource(0));
        if(!input||!input->size){stream.size=0;stream.count=0;return;}
        double amount=c.numericInput(octaveMode?3:logarithmic?2:1,octaveMode?octaves:logarithmic?factor:width)[0];
        if(octaveMode)amount=std::isfinite(amount)?std::exp2(std::clamp(amount,0.,std::log2(double(input->size)))):1.;
        double ratio=std::isfinite(amount)?std::max(1.,amount):1.;
        std::size_t last=input->size/2,radius=std::isfinite(amount)?static_cast<std::size_t>(std::round(std::clamp(amount,0.,double(last)))):0;
        if(logarithmic?ratio==1:!radius){stream.copy(*input);return;}
        stream.metadata(*input);
        for(std::size_t f=0;f<stream.count;f++){
            const auto& source=input->frames[f];auto& out=stream.frames[f];out.start=source.start;
            std::fill_n(changes.data(),last+2,0.);
            // Range additions distribute power in O(N), independently of radius.
            for(std::size_t k=0;k<=last;k++){
                if(logarithmic&&k==0)continue;
                auto lo=k>radius?k-radius:0,hi=std::min(last,k+radius);
                if(logarithmic){lo=std::max<std::size_t>(1,static_cast<std::size_t>(std::ceil(k/ratio)));hi=static_cast<std::size_t>(std::floor(std::min(double(last),k*ratio)));}
                double energy=std::norm(source.bins[k])*(k==0||k==last?1:2);
                double share=std::isfinite(energy)?energy/(logarithmic?logPrefix[hi]-logPrefix[lo-1]:double(hi-lo+1)):0;
                changes[lo]+=share;changes[hi+1]-=share;
            }
            double sum=0;
            for(std::size_t k=0;k<=last;k++){
                if(logarithmic&&k==0){out.bins[k]=source.bins[k];continue;}
                sum+=changes[k];double magnitude=std::sqrt(std::max(0.,sum)/(logarithmic?k:1)/(k==0||k==last?1:2));
                if(k==0||k==last)out.bins[k]={std::signbit(source.bins[k].real())?-magnitude:magnitude,0};
                else out.bins[k]=std::polar(magnitude,std::arg(source.bins[k]));
            }
            SpectrumStream::realSymmetry(out,stream.size);
        }
    }
};
EASYKNACK_NODE(SpectrumBlurNode,"spectrumblur")
}
