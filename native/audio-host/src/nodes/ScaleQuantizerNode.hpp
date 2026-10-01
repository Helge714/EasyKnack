#pragma once
#include <algorithm>
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
namespace easyknack {
struct ScaleQuantizerNode : NodeProcessor {
    float frequency=440,target=440,tuning=440,hysteresis=.1f;int root=0;unsigned mask=4095;bool fixed=false;
    double heldNote=0;bool hasNote=false;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    static unsigned scaleMask(const std::string& name){
        if(name=="chromatic")return 4095;
        if(name=="major")return 0b101010110101;
        if(name=="minor")return 0b010110101101;
        if(name=="major_pentatonic")return 0b001010010101;
        if(name=="minor_pentatonic")return 0b010010101001;
        throw std::runtime_error("Unknown scale");
    }
    void configure(std::istream& in)override{
        auto start=in.tellg();std::string marker;in>>marker;
        if(marker=="!d1b673b46ada3566"){
            int f,t;std::string mode,r,scale;
            if(!(in>>f>>frequency>>t>>target>>mode>>r>>scale>>tuning))throw std::runtime_error("Invalid legacy scale quantizer");
            if(f< -1||f>=2048||t< -1||t>=2048||!std::isfinite(frequency)||frequency<0||frequency>20000||!std::isfinite(target)||target<1||target>20000||!std::isfinite(tuning)||tuning<400||tuning>480||(mode!="scale"&&mode!="fixed"))throw std::runtime_error("Invalid legacy scale quantizer");
            root=std::stoi(r);if(root<0||root>11||r!=std::to_string(root))throw std::runtime_error("Invalid root");
            sources={f,t};fixed=mode=="fixed";mask=scaleMask(scale);hysteresis=0;return;
        }
        in.seekg(start);auto p=parameters::ScaleQuantizerNodeParameters::read(in);sources=p.sources();frequency=p.frequency_value;target=p.target_value;fixed=p.mode_value=="fixed";root=std::stoi(p.root_value);mask=scaleMask(p.scale_value);tuning=p.tuning_value;hysteresis=p.hysteresis_value;
    }
    bool sameScale(const ScaleQuantizerNode& p)const noexcept{return tuning==p.tuning&&root==p.root&&mask==p.mask&&fixed==p.fixed;}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const ScaleQuantizerNode&>(other);if(!sameScale(p))hasNote=false;frequency=p.frequency;target=p.target;tuning=p.tuning;root=p.root;mask=p.mask;fixed=p.fixed;hysteresis=p.hysteresis;}
    void inheritState(const NodeProcessor& other)noexcept override{auto& p=static_cast<const ScaleQuantizerNode&>(other);if(sameScale(p)){heldNote=p.heldNote;hasNote=p.hasNote;}}
    double nearestNote(double note)const noexcept{
        double best=0,distance=1e9;
        int center=static_cast<int>(std::floor(note));
        for(int candidate=center-12;candidate<=center+12;candidate++){
            int pitchClass=((candidate-root)%12+12)%12;
            if(!(mask&(1u<<pitchClass)))continue;
            double d=std::abs(candidate-note);
            if(d<distance-1e-10){distance=d;best=candidate;}
        }
        return best;
    }
    float quantize(float hz)const noexcept{
        if(!std::isfinite(hz)||hz<=0)return 0;
        return static_cast<float>(tuning*std::exp2((nearestNote(69+12*std::log2(double(hz)/tuning))-69)/12));
    }
    void process(ProcessContext& c)noexcept override{
        auto f=c.numericInput(0,frequency),t=c.numericInput(1,target),h=c.numericInput(2,hysteresis);
        for(std::size_t i=0;i<c.bufferSize;i++){
            if(fixed){hasNote=false;c.output[i]=std::isfinite(t[i])&&t[i]>0?t[i]:0;continue;}
            if(!std::isfinite(f[i])||f[i]<=0){hasNote=false;c.output[i]=0;continue;}
            double note=69+12*std::log2(double(f[i])/tuning),candidate=nearestNote(note);
            double margin=std::isfinite(h[i])?std::clamp(double(h[i]),0.,12.):0.;
            if(!hasNote||margin==0||std::abs(note-heldNote)>std::abs(note-candidate)+2*margin){heldNote=candidate;hasNote=true;}
            c.output[i]=static_cast<float>(tuning*std::exp2((heldNote-69)/12));
        }
    }
};
EASYKNACK_NODE(ScaleQuantizerNode,"scalequantizer")
}
