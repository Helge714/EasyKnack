#pragma once
#include "nodes/MathNode.hpp"
#if defined(__SSE2__)
#include <emmintrin.h>
#include <cfloat>
namespace easyknack { namespace simd {
using Float4=__m128;
inline Float4 splat(float x) noexcept{return _mm_set1_ps(x);}
inline Float4 load(const float* p) noexcept{return _mm_loadu_ps(p);}
inline void store(float* p,Float4 x) noexcept{_mm_storeu_ps(p,x);}
inline Float4 select(Float4 mask,Float4 a,Float4 b) noexcept{return _mm_or_ps(_mm_and_ps(mask,a),_mm_andnot_ps(mask,b));}
inline Float4 absolute(Float4 x) noexcept{return _mm_and_ps(x,_mm_castsi128_ps(_mm_set1_epi32(0x7fffffff)));}
inline Float4 evaluate(Float4 a,Float4 b,MathOperation operation,bool clamp) noexcept{
    Float4 value=splat(0);
    switch(operation){
        case MathOperation::multiply:value=_mm_mul_ps(a,b);break;
        case MathOperation::add:value=_mm_add_ps(a,b);break;
        case MathOperation::subtract:value=_mm_sub_ps(a,b);break;
        case MathOperation::divide:{auto zero=_mm_cmplt_ps(absolute(b),splat(1e-12f));value=select(zero,splat(0),_mm_div_ps(a,select(zero,splat(1),b)));break;}
        // SSE min/max have different NaN/signed-zero tie rules from std::min/std::max.
        case MathOperation::minimum:value=select(_mm_cmplt_ps(b,a),b,a);break;
        case MathOperation::maximum:value=select(_mm_cmplt_ps(a,b),b,a);break;
        case MathOperation::absolute:value=absolute(a);break;
        case MathOperation::negate:value=_mm_xor_ps(a,_mm_castsi128_ps(_mm_set1_epi32(0x80000000u)));break;
    }
    value=select(_mm_cmple_ps(absolute(value),splat(FLT_MAX)),value,splat(0));
    if(clamp){value=select(_mm_cmplt_ps(value,splat(-1)),splat(-1),value);value=select(_mm_cmplt_ps(splat(1),value),splat(1),value);}
    return value;
}
}}
#endif
