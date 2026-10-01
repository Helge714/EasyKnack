#pragma once
#include "MidiEvents.hpp"
#include <atomic>
#include <algorithm>

namespace easyknack {
/** Bounded, nonblocking callback queue. Concurrent producers fail safely instead of spinning. */
class MidiQueue {
    struct Timed { std::int64_t time=0;std::uint32_t message=0; };
    std::array<Timed,1024> data{};
    std::atomic<std::size_t> head{0},tail{0};
    std::atomic_flag producer=ATOMIC_FLAG_INIT;
    std::atomic<bool> overflow{false};
public:
    void panic() noexcept {overflow.store(true,std::memory_order_release);}
    void push(std::int64_t time,std::uint32_t message) noexcept {
        if(producer.test_and_set(std::memory_order_acquire)){panic();return;}
        auto h=head.load(std::memory_order_relaxed),t=tail.load(std::memory_order_acquire);
        if(h-t>=data.size())panic();else{data[h%data.size()]={time,message};head.store(h+1,std::memory_order_release);}
        producer.clear(std::memory_order_release);
    }
    void drain(MidiBlock& block,std::int64_t start,std::int64_t end,std::size_t frames) noexcept {
        block.clear();
        if(frames==0)return;
        auto t=tail.load(std::memory_order_relaxed),h=head.load(std::memory_order_acquire);
        if(overflow.exchange(false,std::memory_order_acq_rel)){block.panic=true;tail.store(h,std::memory_order_release);return;}
        while(t<h){
            const auto& e=data[t%data.size()];if(e.time>=end)break;
            auto offset=static_cast<std::uint32_t>(std::clamp<std::int64_t>((e.time-start)*static_cast<std::int64_t>(frames)/std::max<std::int64_t>(1,end-start),0,frames-1));
            if(block.count==MidiBlock::capacity){block.clear();block.panic=true;t=h;break;}
            // Callback arrival order is authoritative; timestamps must not reverse it.
            if(block.count)offset=std::max(offset,block.events[block.count-1].offset);
            block.append(offset,e.message);++t;
        }
        tail.store(t,std::memory_order_release);
    }
};
}
