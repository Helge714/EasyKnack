#pragma once
#include <algorithm>
#include <cmath>
#include <cstddef>

namespace easyknack {
/** Sample-continuous, bounded RMS correction. No lookahead or frame normalization. */
struct EnvelopeStabilizer {
    double fast=0,slow=0,gain=1,amount=0;
    double fastStep=0,slowStep=0,gainStep=0;
    std::size_t warmup=0,seen=0;
    void reset()noexcept{fast=slow=amount=0;gain=1;seen=0;}
    void prepare(double rate)noexcept{
        fastStep=-std::expm1(-1/(rate*.010));
        slowStep=-std::expm1(-1/(rate*.500));
        gainStep=-std::expm1(-1/(rate*.002));
        warmup=static_cast<std::size_t>(rate*.010);reset();
    }
    double process(double sample,double strength)noexcept{
        if(!std::isfinite(sample))sample=0;
        if(strength==0&&amount<1e-12){if(amount!=0)reset();return sample;}
        const double power=sample*sample;
        fast+=(power-fast)*fastStep;
        if(seen<warmup){slow=fast;++seen;}
        else slow+=(power-slow)*slowStep;
        // Gain is bounded to +/-12 dB. Near silence must never provoke division
        // by zero or resurrect numerical residue. Smooth the gain, not the audio.
        double target=fast>1e-18?std::clamp(std::sqrt(slow/fast),.25,4.):1.;
        gain+=(target-gain)*gainStep;
        amount+=(std::clamp(strength,0.,1.)-amount)*gainStep;
        return sample*(1+amount*(gain-1));
    }
};
}
