#pragma once
#include "ParameterContracts.hpp"
#include "../VectorResource.hpp"
namespace easyknack {
/** Point source, equal-power azimuth panning and optional free-field propagation. */
struct SpatialReceiverNode:NodeProcessor {
    Vector3 source{0,0,1},receiver{};double angle=0,spacing=.18,reference=1;bool stereo=true,attenuation=true,propagation=false;
    std::shared_ptr<std::vector<float>> mono=std::make_shared<std::vector<float>>();
    std::vector<float> delay;std::size_t write=0;
    bool supportsScalarInputs()const noexcept override{return true;}
    char resolveAutoRate(const char* rates,std::size_t count)noexcept override{for(std::size_t p=1;p<count;p++)if(rates[p]=='a')return 'a';return 'c';}
    bool canShareChannels()const noexcept override{return !stereo;}
    bool separatesChannels()const noexcept override{return true;}
    void shareChannelState(NodeProcessor& other)override{mono=static_cast<SpatialReceiverNode&>(other).mono;}
    std::size_t outputCount()const noexcept override{return 2;}
    ResourceKind inputResourceKind(std::size_t p)const noexcept override{return p==1||p==2?ResourceKind::vector:ResourceKind::none;}
    void prepare(double rate,std::size_t frames)override{mono->resize(frames);delay.resize(static_cast<std::size_t>(std::ceil(rate*2))+2);}
    void configure(std::istream& in)override{auto p=parameters::SpatialReceiverNodeParameters::read(in);sources=p.sources();source={vectorParameter(p.source_x_value),vectorParameter(p.source_y_value),vectorParameter(p.source_z_value)};receiver={vectorParameter(p.receiver_x_value),vectorParameter(p.receiver_y_value),vectorParameter(p.receiver_z_value)};angle=p.angle_value;spacing=p.spacing_value;reference=p.reference_value;stereo=p.mode_value=="stereo";attenuation=p.attenuation_value;propagation=p.propagation_value;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{const auto& v=static_cast<const SpatialReceiverNode&>(other);source=v.source;receiver=v.receiver;angle=v.angle;spacing=v.spacing;reference=v.reference;attenuation=v.attenuation;propagation=v.propagation;}
    void adoptState(NodeProcessor& other)noexcept override{auto& v=static_cast<SpatialReceiverNode&>(other);if(delay.size()==v.delay.size()){delay.swap(v.delay);write=v.write;}}
    void process(ProcessContext& c)noexcept override{
        if(c.channelIndex==0){auto left=c.numericInput(0);auto* right=c.rightInputs?c.rightInputs[0]:nullptr;for(std::size_t i=0;i<c.bufferSize;i++)(*mono)[i]=.5f*(left[i]+(right?right[c.sourceRates&&c.sourceRates[0]=='c'?0:i]:left[i]));}
        bool moving=false;for(std::size_t p=1;p<5;p++)moving|=c.sourceRates&&c.sourceRates[p]=='a';
        double distance=0,gain=1;
        for(std::size_t i=0;i<c.bufferSize;i++){
            if(i==0||moving){
            auto src=vectorInput(c,1,i,source),center=vectorInput(c,2,i,receiver);double yaw=c.numericInput(3,static_cast<float>(angle))[i]*.017453292519943295;
            if(!std::isfinite(yaw))yaw=0;Vector3 right{std::cos(yaw),0,-std::sin(yaw)};
            double width=stereo?std::max(0.f,c.numericInput(4,static_cast<float>(spacing))[i]):0;if(!std::isfinite(width))width=0;
            auto ear=center;for(std::size_t axis=0;axis<3;axis++)ear[axis]+=(c.channelIndex==0?-.5:.5)*width*right[axis];
            distance=length(subtract(src,ear));if(!std::isfinite(distance))distance=std::numeric_limits<float>::max();auto relative=subtract(src,center);double radius=length(relative);
            double pan=radius>1e-12?std::clamp((relative[0]*right[0]+relative[2]*right[2])/radius,-1.,1.):0;
            gain=stereo?std::sqrt((1+(c.channelIndex==0?-pan:pan))*.5):1;
            if(attenuation)gain*=reference/std::max(reference,distance);
            }
            float sample=(*mono)[i];delay[write]=sample;
            if(propagation){double requested=distance/343*c.sampleRate;double lag=std::min(requested,static_cast<double>(delay.size()-2));double position=static_cast<double>(write)-lag;if(position<0)position+=delay.size();auto base=static_cast<std::size_t>(position);double t=position-base;sample=static_cast<float>(delay[base]*(1-t)+delay[(base+1)%delay.size()]*t);if(requested>delay.size()-2)sample=0;}
            write=(write+1)%delay.size();c.output[i]=std::isfinite(gain)?sample*static_cast<float>(gain):0;
            if(auto* out=c.outputAt(1))out[i]=static_cast<float>(distance);
        }
    }
};
EASYKNACK_NODE(SpatialReceiverNode,"spatialreceiver")
}
