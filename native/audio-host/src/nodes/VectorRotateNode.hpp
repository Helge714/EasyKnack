#pragma once
#include "ParameterContracts.hpp"
#include "../VectorResource.hpp"
namespace easyknack {
struct VectorRotateNode:VectorProcessor {
    Vector3 input{},rotation{0,0,0},origin{};
    ResourceKind inputResourceKind(std::size_t p)const noexcept override {
        return p<3?ResourceKind::vector:ResourceKind::none;
    }
    void configure(std::istream& in)override {
        auto p=parameters::VectorRotateNodeParameters::read(in);sources=p.sources();
        input={vectorParameter(p.input_x_value),vectorParameter(p.input_y_value),vectorParameter(p.input_z_value)};
        rotation={vectorParameter(p.rotation_x_value),vectorParameter(p.rotation_y_value),vectorParameter(p.rotation_z_value)};
        origin={vectorParameter(p.origin_x_value),vectorParameter(p.origin_y_value),vectorParameter(p.origin_z_value)};
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override {
        const auto& v=static_cast<const VectorRotateNode&>(other);input=v.input;rotation=v.rotation;origin=v.origin;
    }
    void process(ProcessContext& c)noexcept override {
        result.count=c.bufferSize;
        const bool varyingRotation=c.sourceRates && c.inputCount>1 && c.sourceRates[1]=='a';
        Vector3 sine{},cosine{};
        for(std::size_t i=0;i<c.bufferSize;i++) {
            auto v=vectorInput(c,0,i,input),o=vectorInput(c,2,i,origin);
            for(int axis=0;axis<3;axis++)v[axis]-=o[axis];
            if(i==0 || varyingRotation) {
                auto r=vectorInput(c,1,i,rotation);
                for(int axis=0;axis<3;axis++) {
                    double angle=std::remainder(r[axis],360.0)*0.017453292519943295;
                    sine[axis]=std::sin(angle);cosine[axis]=std::cos(angle);
                }
            }
            // Right-handed Euler rotation: X, then Y, then Z.
            for(int axis=0;axis<3;axis++) {
                int a=(axis+1)%3,b=(axis+2)%3;
                double first=v[a]*cosine[axis]-v[b]*sine[axis];
                v[b]=v[a]*sine[axis]+v[b]*cosine[axis];v[a]=first;
            }
            for(int axis=0;axis<3;axis++)v[axis]+=o[axis];
            for(auto& component:v)if(!std::isfinite(component))component=0;
            result.samples[i]=v;c.output[i]=0;
        }
    }
};
EASYKNACK_NODE(VectorRotateNode,"vectorrotate")
}
