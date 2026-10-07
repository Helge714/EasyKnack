#pragma once
#include <algorithm>
#include <atomic>
#include <cmath>
#include <cstdint>
#include <vector>
#include <stdexcept>
#include <cassert>
namespace easyknack {
enum class ResourceKind { none, history, analysis, spectrum, vector };
struct DspResource { virtual ~DspResource()=default; virtual ResourceKind kind()const noexcept=0; };
/** Graph-owned, channel/voice-local ring. Only the scheduled DSP writer mutates it. */
struct HistoryResource final : DspResource {
    inline static std::atomic<std::uint64_t> nextIdentity{1};
    std::uint64_t identity=nextIdentity.fetch_add(1);
    std::vector<float> samples;
    // Old contents of the slots overwritten by the current block. Shared by readers.
    std::vector<float> overwritten;
    std::size_t savedFrames=0,writeStart=0;
    double sampleRate=0;
    bool immutable=false;
    std::uint64_t end=0;
    std::size_t blockFrames=0;
    ResourceKind kind()const noexcept override{return ResourceKind::history;}
    void prepare(double rate,double seconds,std::size_t minimumFrames=1){
        if(!std::isfinite(rate)||rate<=0||!std::isfinite(seconds)||seconds<0)
            throw std::invalid_argument("Invalid buffer duration or sample rate");
        // The ring wraps interpolation indices; no extra guard samples are needed.
        double frames=std::max(static_cast<double>(std::max<std::size_t>(1,minimumFrames)),std::round(rate*seconds));
        if(!std::isfinite(frames)||frames>=static_cast<double>(samples.max_size()))
            throw std::length_error("Buffer capacity is too large");
        auto size=static_cast<std::size_t>(frames);
        if(overwritten.size()!=minimumFrames){overwritten.assign(minimumFrames,0);savedFrames=0;}
        if(sampleRate==rate&&samples.size()==size)return;
        samples.assign(size,0);sampleRate=rate;end=0;blockFrames=0;savedFrames=0;writeStart=0;identity=nextIdentity.fetch_add(1);
    }
    /** Called once before appending a scheduled block; storage was allocated in prepare(). */
    void beginBlock(std::size_t frames)noexcept{
        assert(frames<=overwritten.size()&&frames<=samples.size());
        blockFrames=frames;savedFrames=frames;writeStart=end%samples.size();
        auto first=std::min(frames,samples.size()-writeStart);
        std::copy_n(samples.data()+writeStart,first,overwritten.data());
        std::copy_n(samples.data(),frames-first,overwritten.data()+first);
    }
    /** Write-before-read semantics: at offset i only writes 0..i are visible. */
    float valueAt(std::size_t index,std::size_t offset)const noexcept{
        if(savedFrames){
            auto relative=index>=writeStart?index-writeStart:samples.size()-writeStart+index;
            if(relative<savedFrames&&relative>offset)return overwritten[relative];
        }
        return samples[index];
    }
    std::size_t writeHeadAt(std::size_t offset)const noexcept{
        if(!savedFrames)return end%samples.size(); // Complete immutable/manual snapshots.
        auto position=writeStart+std::min(offset+1,savedFrames);
        return position>=samples.size()?position-samples.size():position;
    }
    void append(float value)noexcept{samples[end%samples.size()]=std::isfinite(value)?value:0;++end;}
    std::uint64_t begin()const noexcept{return end>samples.size()?end-samples.size():0;}
    float sample(double position)const noexcept{
        if(!std::isfinite(position)||position<begin()||position>=end)return 0;
        auto i=static_cast<std::uint64_t>(std::floor(position));auto a=samples[i%samples.size()];
        auto b=i+1<end?samples[(i+1)%samples.size()]:a;return a+(b-a)*static_cast<float>(position-i);
    }
};
}
