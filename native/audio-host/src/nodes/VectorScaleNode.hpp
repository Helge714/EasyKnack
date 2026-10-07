#pragma once
#include "ParameterContracts.hpp"
#include "../VectorResource.hpp"
namespace easyknack {
struct VectorScaleNode:VectorProcessor {
    Vector3 input{},scale{1,1,1},origin{};
    ResourceKind inputResourceKind(std::size_t p)const noexcept override {
        return p<3?ResourceKind::vector:ResourceKind::none;
    }
    void configure(std::istream& in)override {
        auto p=parameters::VectorScaleNodeParameters::read(in);sources=p.sources();
        input={vectorParameter(p.input_x_value),vectorParameter(p.input_y_value),vectorParameter(p.input_z_value)};
        scale={vectorParameter(p.scale_x_value),vectorParameter(p.scale_y_value),vectorParameter(p.scale_z_value)};
        origin={vectorParameter(p.origin_x_value),vectorParameter(p.origin_y_value),vectorParameter(p.origin_z_value)};
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override {
        const auto& v=static_cast<const VectorScaleNode&>(other);input=v.input;scale=v.scale;origin=v.origin;
    }
    void process(ProcessContext& c)noexcept override {
        result.count=c.bufferSize;
        for(std::size_t i=0;i<c.bufferSize;i++) {
            auto v=vectorInput(c,0,i,input),o=vectorInput(c,2,i,origin);
            for(int axis=0;axis<3;axis++)v[axis]-=o[axis];
            auto s=vectorInput(c,1,i,scale);
            for(int axis=0;axis<3;axis++)v[axis]*=s[axis];
            for(int axis=0;axis<3;axis++)v[axis]+=o[axis];
            for(auto& component:v)if(!std::isfinite(component))component=0;
            result.samples[i]=v;c.output[i]=0;
        }
    }
};
EASYKNACK_NODE(VectorScaleNode,"vectorscale")
}
