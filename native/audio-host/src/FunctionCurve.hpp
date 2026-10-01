#pragma once
#include <algorithm>
#include <array>
#include <cmath>
#include <istream>
#include <stdexcept>
#include <vector>
#include <memory>

namespace easyknack {
/** Built off the audio thread. Exact linear/step segments; Bezier lookup per segment. */
struct FunctionCurve {
    struct Point { double x,y,ix,iy,ox,oy; int mode; };
    struct Segment { Point a,b; std::array<float,257> table{}; };
    std::shared_ptr<const std::vector<Segment>> storage=std::make_shared<std::vector<Segment>>();
    static double cubic(double a,double b,double c,double d,double t) noexcept {
        double u=1-t;return u*u*u*a+3*u*u*t*b+3*u*t*t*c+t*t*t*d;
    }
    void read(std::istream& in) {
        int count;if(!(in>>count)||count<2||count>64)throw std::runtime_error("invalid curve count");
        std::vector<Point> points(count);
        for(auto& p:points){
            if(!(in>>p.x>>p.y>>p.ix>>p.iy>>p.ox>>p.oy>>p.mode)||p.mode<0||p.mode>2)throw std::runtime_error("invalid curve point");
            for(double v:{p.x,p.y,p.ix,p.iy,p.ox,p.oy})if(!std::isfinite(v)||v<0||v>1)throw std::runtime_error("invalid curve coordinate");
            if(p.ix>p.x||p.ox<p.x)throw std::runtime_error("reversed curve handle");
        }
        if(points.front().x!=0||points.back().x!=1)throw std::runtime_error("invalid curve endpoints");
        auto prepared=std::make_shared<std::vector<Segment>>();auto& segments=*prepared;
        segments.clear();segments.reserve(count-1);
        for(int i=0;i<count-1;i++){
            const auto& a=points[i];const auto& b=points[i+1];
            if(b.x-a.x<.000099999||a.ox>b.ix)throw std::runtime_error("crossing curve handles");
            Segment segment{a,b,{}};
            if(a.mode==2)for(int n=0;n<=256;n++){
                double x=a.x+(b.x-a.x)*n/256,lo=0,hi=1;
                for(int k=0;k<40;k++){double t=(lo+hi)/2;if(cubic(a.x,a.ox,b.ix,b.x,t)<x)lo=t;else hi=t;}
                segment.table[n]=static_cast<float>(cubic(a.y,a.oy,b.iy,b.y,(lo+hi)/2));
            }
            segment.table.front()=static_cast<float>(a.y);segment.table.back()=static_cast<float>(b.y);
            segments.push_back(std::move(segment));
        }
        storage=std::move(prepared);
    }
    double sample(double x,bool extend=false) const noexcept {
        const auto& segments=*storage;
        if(segments.empty()||!std::isfinite(x))return 0;
        const auto& first=segments.front();const auto& last=segments.back();
        if(x<=0){double slope=first.a.mode==1?0:first.a.mode==0?(first.b.y-first.a.y)/(first.b.x-first.a.x):
                    (first.table[1]-first.table[0])*256/(first.b.x-first.a.x);
            return first.a.y+(extend?x*slope:0);}
        if(x>=1){double slope=last.a.mode==1?0:last.a.mode==0?(last.b.y-last.a.y)/(last.b.x-last.a.x):
                    (last.table[256]-last.table[255])*256/(last.b.x-last.a.x);
            return last.b.y+(extend?(x-1)*slope:0);}
        auto it=std::upper_bound(segments.begin(),segments.end(),x,[](double value,const Segment& s){return value<s.b.x;});
        if(it==segments.end())return last.b.y;
        const auto& s=*it;
        if(s.a.mode==1)return s.a.y;
        double t=(x-s.a.x)/(s.b.x-s.a.x);
        if(s.a.mode==0)return s.a.y+t*(s.b.y-s.a.y);
        double pos=t*256;int i=std::clamp(static_cast<int>(pos),0,255);
        return s.table[i]+(s.table[i+1]-s.table[i])*(pos-i);
    }
};
}
