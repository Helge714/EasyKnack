#pragma once
#include <algorithm>
#include <cmath>
#include <limits>
namespace easyknack::dynamics {
inline double finite(double x,double fallback=0)noexcept{return std::isfinite(x)?x:fallback;}
inline double bound(double x,double lo,double hi,double fallback=0)noexcept{return std::clamp(finite(x,fallback),lo,hi);}
inline float sample(double x)noexcept{return !std::isfinite(x)||std::abs(x)<1e-25?0.f:static_cast<float>(std::clamp(x,-double(std::numeric_limits<float>::max()),double(std::numeric_limits<float>::max())));}
inline double decibels(double x)noexcept{return 20*std::log10(std::max(std::abs(finite(x)),1e-12));}
inline double amplitude(double db)noexcept{return std::pow(10.,db*.05);}
// Static feed-forward soft-knee curve; W is the full knee width in dB.
// Giannoulis, Massberg & Reiss, JAES 60(6), 2012, section 2.1.
inline double reduction(double level,double threshold,double ratio,double knee)noexcept {
    double x=level-threshold,slope=1/ratio-1;
    if(knee>0&&x>-knee*.5&&x<knee*.5)return slope*(x+knee*.5)*(x+knee*.5)/(2*knee);
    return x<=-knee*.5?0:slope*x;
}
struct TimeCoefficient {
    double last=-1,rate=0,value=0;
    double get(double ms,double fs)noexcept {
        ms=bound(ms,0,10000);
        if(ms!=last||fs!=rate){last=ms;rate=fs;value=ms<=0?0:std::exp(-1000/(ms*fs));}
        return value;
    }
};
}
