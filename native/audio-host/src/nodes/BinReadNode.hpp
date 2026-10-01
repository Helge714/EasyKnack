#pragma once
#include "BinContextNode.hpp"
#include "SpectrumShiftNode.hpp"
namespace easyknack {
struct BinReadNode : BinAccess {
    float index=0;int boundary=0;bool connected=true;
    std::size_t outputCount()const noexcept override{return 6;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t port)const noexcept override{return port==1?ResourceKind::spectrum:ResourceKind::none;}
    void configure(std::istream& in)override{
        auto start=in.tellg();std::string marker;in>>marker;
        if(marker=="!7035393b0097f18c"){
            int source;std::string mode;if(!(in>>source>>index>>mode))throw std::runtime_error("Invalid legacy Bin Read");
            sources={source};boundary=mode=="clamp"?1:mode=="wrap"?2:0;connected=true;return;
        }
        in.seekg(start);auto p=parameters::BinReadNodeParameters::read(in);sources=p.sources();connected=p.spectrum_bound_value=="1";index=p.index_value;boundary=p.boundary_value=="clamp"?1:p.boundary_value=="wrap"?2:0;}
    void process(ProcessContext& c)noexcept override{
        double position=c.numericInput(0,index)[0];bool valid=frame&&connected&&size&&std::isfinite(position);
        if(valid){if(boundary==1)position=std::clamp(position,0.,double(size/2));else if(boundary==2){position=std::fmod(position,double(size/2+1));if(position<0)position+=size/2+1;}else valid=position>=0&&position<=size/2;}
        auto v=valid?readSpectrumBin(*frame,size,position,boundary):std::complex<double>{};
        double magnitude=std::abs(v),scale=windowSum>0?(position==0||position==size/2?1:2)/windowSum:0;
        double values[]{v.real(),v.imag(),magnitude,c.outputAt(3)?std::arg(v):0,valid?magnitude*scale:0,valid?position*rate/size:0};for(std::size_t p=0;p<6;p++)if(auto* out=c.outputAt(p))out[0]=static_cast<float>(values[p]);
    }
};
EASYKNACK_NODE(BinReadNode,"binread")
}
