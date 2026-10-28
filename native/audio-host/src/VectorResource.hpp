#pragma once
#include "NodeProcessor.hpp"
#include <array>
namespace easyknack {
using Vector3=std::array<double,3>;
inline Vector3 subtract(Vector3 a,Vector3 b)noexcept{return {a[0]-b[0],a[1]-b[1],a[2]-b[2]};}
inline double length(Vector3 a)noexcept{return std::hypot(a[0],a[1],a[2]);}
struct VectorResource final:DspResource {
    std::vector<Vector3> samples;
    std::size_t count=0;
    ResourceKind kind()const noexcept override{return ResourceKind::vector;}
    Vector3 at(std::size_t i)const noexcept{return count?samples[std::min(i,count-1)]:Vector3{};}
};
inline Vector3 vectorInput(const ProcessContext& c,std::size_t port,std::size_t i,Vector3 fallback)noexcept{
    auto* r=c.resource(port);if(!r||r->kind()!=ResourceKind::vector){if(c.controls)for(std::size_t axis=0;axis<3;axis++)fallback[axis]=c.numericInput(32+port*3+axis,float(fallback[axis]))[i];return fallback;}
    if(c.controls)for(std::size_t axis=0;axis<3;axis++)c.controls->connected(32+port*3+axis);
    auto* vector=static_cast<const VectorResource*>(r);
    return vector->at(c.sourceRates&&c.sourceRates[port]=='c'?0:c.sampleOffset+i);
}
struct VectorProcessor:NodeProcessor {
    VectorResource result;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    void prepare(double,std::size_t frames)override{result.samples.resize(frames);}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::vector;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &result;}
};
inline double vectorParameter(const std::string& value){std::size_t used=0;double v=std::stod(value,&used);if(used!=value.size()||!std::isfinite(v))throw std::runtime_error("Invalid vector coordinate");return v;}
}
