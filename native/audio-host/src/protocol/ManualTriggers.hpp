#pragma once
#include <atomic>
#include <memory>
#include <mutex>
#include <unordered_map>
#include <string>
#include <cstdint>

namespace easyknack {
// Control-thread lookup; rendering only reads a pre-bound atomic mailbox.
struct ManualTriggerMailbox {
    std::atomic<std::uint64_t> submitted{0};
    std::uint64_t epoch=~std::uint64_t{},latched=0;
};
inline thread_local std::uint64_t manualTriggerEpoch=0;
inline std::mutex manualTriggerMutex;
inline std::unordered_map<std::string,std::weak_ptr<ManualTriggerMailbox>> manualTriggerMailboxes;
inline std::shared_ptr<ManualTriggerMailbox> bindManualTrigger(const std::string& id) {
    std::lock_guard<std::mutex> lock(manualTriggerMutex);
    for(auto it=manualTriggerMailboxes.begin();it!=manualTriggerMailboxes.end();)
        if(it->second.expired())it=manualTriggerMailboxes.erase(it);else ++it;
    auto& entry=manualTriggerMailboxes[id];auto mailbox=entry.lock();
    if(!mailbox){mailbox=std::make_shared<ManualTriggerMailbox>();entry=mailbox;}return mailbox;
}
inline void submitManualTrigger(const std::string& id) {
    std::lock_guard<std::mutex> lock(manualTriggerMutex);
    auto it=manualTriggerMailboxes.find(id);
    if(it!=manualTriggerMailboxes.end())if(auto mailbox=it->second.lock())mailbox->submitted.fetch_add(1,std::memory_order_relaxed);
}
struct ManualTriggerReceiver {
    std::shared_ptr<ManualTriggerMailbox> mailbox;
    std::uint64_t seen=0;
    void bind(const std::string& id){mailbox=bindManualTrigger(id);seen=mailbox->submitted.load(std::memory_order_relaxed);}
    std::uint64_t take()noexcept {
        if(!mailbox)return 0;
        if(mailbox->epoch!=manualTriggerEpoch){mailbox->latched=mailbox->submitted.load(std::memory_order_relaxed);mailbox->epoch=manualTriggerEpoch;}
        if(mailbox->latched<=seen)return 0;
        // All pending clicks are scheduled at sample zero of this block: one trigger.
        seen=mailbox->latched;return 1;
    }
};
}
