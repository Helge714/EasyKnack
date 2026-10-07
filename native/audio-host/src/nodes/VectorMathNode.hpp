#pragma once
#include "ParameterContracts.hpp"
#include "../VectorResource.hpp"
namespace easyknack {
struct VectorMathNode:VectorProcessor {
    Vector3 a{},b{};double factor=1;int operation=0;
    ResourceKind inputResourceKind(std::size_t p)const noexcept override{return p<2?ResourceKind::vector:ResourceKind::none;}
    void configure(std::istream& in)override{auto p=parameters::VectorMathNodeParameters::read(in);sources=p.sources();a={vectorParameter(p.a_x_value),vectorParameter(p.a_y_value),vectorParameter(p.a_z_value)};b={vectorParameter(p.b_x_value),vectorParameter(p.b_y_value),vectorParameter(p.b_z_value)};factor=p.factor_value;const char* names[]={"add","subtract","multiply","divide","scale","normalize","negate","absolute","minimum","maximum","cross"};operation=-1;for(int i=0;i<11;i++)if(p.operation_value==names[i])operation=i;if(operation<0)throw std::runtime_error("Unknown vector operation");}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{const auto& v=static_cast<const VectorMathNode&>(other);a=v.a;b=v.b;factor=v.factor;operation=v.operation;}
    void process(ProcessContext& c)noexcept override{result.count=c.bufferSize;for(std::size_t i=0;i<c.bufferSize;i++){
        auto x=vectorInput(c,0,i,a),y=vectorInput(c,1,i,b);Vector3 v{};auto scale=c.numericInput(2,static_cast<float>(factor))[i];double norm=length(x);
        for(std::size_t axis=0;axis<3;axis++){double a=x[axis],b=y[axis];switch(operation){case 0:v[axis]=a+b;break;case 1:v[axis]=a-b;break;case 2:v[axis]=a*b;break;case 3:v[axis]=b==0?0:a/b;break;case 4:v[axis]=a*scale;break;case 5:v[axis]=norm>0?a/norm:0;break;case 6:v[axis]=-a;break;case 7:v[axis]=std::abs(a);break;case 8:v[axis]=std::min(a,b);break;case 9:v[axis]=std::max(a,b);break;case 10:v[axis]=x[(axis+1)%3]*y[(axis+2)%3]-x[(axis+2)%3]*y[(axis+1)%3];break;}if(!std::isfinite(v[axis]))v[axis]=0;}result.samples[i]=v;c.output[i]=0;
    }}
};
EASYKNACK_NODE(VectorMathNode,"vectormath")
}
