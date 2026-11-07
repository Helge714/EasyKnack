#pragma once
#include "Partials.hpp"
#include <memory>
namespace easyknack {
/** Original Loris analysis stages, adapted to the upstream STFT frame clock.
 * Call on a helper thread: Loris owns allocating containers. */
class LorisStream {
    struct Impl;
    std::unique_ptr<Impl> impl;
public:
    struct Settings {
        double resolution=0,drift=0,threshold=-60,minimum=20,maximum=20000;
        double duration=20,convergence=.1,region=2000;
        int bandwidth=2; // none, residual association, convergence
    };
    LorisStream();
    ~LorisStream();
    void reset();
    void analyze(const SpectrumStream&,const StreamSpectrumFrame&,const Settings&,PartialFrame&);
};
}
