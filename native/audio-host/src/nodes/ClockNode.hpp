#pragma once

#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"

#include <algorithm>
#include <cmath>
#include <cstddef>

namespace easyknack {

struct ClockNode : NodeProcessor {
    bool supportsScalarInputs()const noexcept override{return true;}
    char resolveAutoRate(const char*,std::size_t)noexcept override{return 'e';}
    ManualTriggerReceiver manual;
    void bindIdentity(const std::string& id)override{manual.bind(id+":sync");}
    void shareChannelState(NodeProcessor& p)override{manual=static_cast<ClockNode&>(p).manual;}
    void shareVoiceState(NodeProcessor& p)override{shareChannelState(p);}


    bool canShareChannels() const noexcept override {
        return true;
    }

    bool supportsControlRate() const noexcept override {
        return true;
    }

    bool supportsEventRate() const noexcept override {
        return true;
    }

    bool evaluatesOnEvents() const noexcept override {
        return true;
    }

    float frequencyValue = 1.f;
    float syncValue = 0.f;

    double phase = 0.0;

    float previousSync = 0.f;

    static constexpr float syncThreshold = 0.5f;

    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const ClockNode&>(configuration);
        frequencyValue=p.frequencyValue;syncValue=p.syncValue;
    }
    void configure(std::istream& in) override {

        in >> std::ws;

        if (in.peek() == '!') {

            auto p =
                parameters::ClockNodeParameters::read(in);

            sources = p.sources();

            frequencyValue =
                static_cast<float>(p.frequency_value);

            syncValue =
                static_cast<float>(p.sync_value);

            return;
        }

        sources = {
            readInput(in)
        };

        frequencyValue =
            readNumber(in, 0.001, 20000.0);

        sources.push_back(
            readInput(in)
        );

        syncValue =
            readNumber(in, 0.0, 1.0);
    }

    bool processesEventBlocks()const noexcept override{return true;}
    void processEventBlock(ProcessContext& context)noexcept override {
        auto* out=context.eventOutput();if(!out)return;out->initial=0;
        const auto clicks=manual.take();
        auto* syncEvents=context.events(1);auto* frequencyEvents=context.events(0);
        std::size_t syncCursor=0,frequencyCursor=0;
        const bool dense=context.sourceRates&&((context.input(0)&&context.sourceRates[0]=='a')||(context.input(1)&&context.sourceRates[1]=='a'));
        std::uint32_t i=0;
        while(i<context.blockFrames){
            bool reset=i==0&&clicks&&!context.input(1)&&!syncEvents;
            if(reset)for(std::uint64_t click=0;click<std::min<std::uint64_t>(clicks,ValueEventBuffer::capacity);click++)out->push(0,1,ValueEventKind::trigger);
            if(syncEvents)while(syncCursor<syncEvents->count&&syncEvents->events[syncCursor].sampleOffset==i){
                const auto& event=syncEvents->events[syncCursor++];
                if(event.kind==ValueEventKind::trigger||risingSync(event.value)){out->push(i,1,ValueEventKind::trigger);reset=true;}
                if(event.kind==ValueEventKind::value)previousSync=event.value;
            }
            else {float sync=context.valueAt(1,i,syncValue);if(risingSync(sync)){reset=true;out->push(i,1,ValueEventKind::trigger);}previousSync=sync;}
            if(reset){phase=0;++i;continue;}
            while(frequencyEvents&&frequencyCursor<frequencyEvents->count&&frequencyEvents->events[frequencyCursor].sampleOffset<=i)++frequencyCursor;
            std::uint32_t end=static_cast<std::uint32_t>(context.blockFrames);
            if(dense)end=i+1;
            if(syncEvents&&syncCursor<syncEvents->count)end=std::min(end,syncEvents->events[syncCursor].sampleOffset);
            if(frequencyEvents&&frequencyCursor<frequencyEvents->count)end=std::min(end,frequencyEvents->events[frequencyCursor].sampleOffset);
            double step=validFrequency(context.valueAt(0,i,frequencyValue))/context.sampleRate;
            while(step>0&&i<end){
                auto remaining=end-i;
                double until=std::max(1.,std::ceil((1-phase)/step-1e-10));
                if(until>remaining){phase+=step*remaining;i=end;break;}
                auto advance=static_cast<std::uint32_t>(until);phase+=step*advance;phase-=std::floor(phase+1e-10);phase=std::max(0.,phase);
                i+=advance;out->push(i-1,1,ValueEventKind::trigger);
            }
            i=end;
        }
    }

    void process(
        ProcessContext& context
    ) noexcept override {

        const float* frequency =
            context.input(0);

        const float* sync =
            context.input(1);

        /*
         * Transitional implementation for the current
         * Event/Control execution model.
         *
         * This should later emit sparse events once the
         * new EventBuffer architecture is in place.
         */
        if (context.bufferSize == 1) {

            const float freq =
                validFrequency(
                    frequency
                        ? frequency[0]
                        : frequencyValue
                );

            const float syncNow =
                safeValue(
                    sync
                        ? sync[0]
                        : syncValue
                );

            bool pulse = false;

            if (risingSync(syncNow)) {

                phase = 0.0;
                pulse = true;

            } else {

                phase +=
                    static_cast<double>(freq)
                    / context.sampleRate;

                if (phase >= 1.0) {

                    phase -= std::floor(phase);
                    pulse = true;
                }
            }

            previousSync = syncNow;

            context.output[0] =
                pulse ? 1.f : 0.f;

            return;
        }

        /*
         * Fallback dense processing.
         * Mainly useful until EventRate is migrated.
         */
        for (
            std::size_t i = 0;
            i < context.bufferSize;
            ++i
        ) {

            const float freq =
                validFrequency(
                    frequency
                        ? frequency[i]
                        : frequencyValue
                );

            const float syncNow =
                safeValue(
                    sync
                        ? sync[i]
                        : syncValue
                );

            bool pulse = false;

            if (risingSync(syncNow)) {

                phase = 0.0;
                pulse = true;

            } else {

                phase +=
                    static_cast<double>(freq)
                    / context.sampleRate;

                if (phase >= 1.0) {

                    phase -= std::floor(phase);
                    pulse = true;
                }
            }

            previousSync = syncNow;

            context.output[i] =
                pulse ? 1.f : 0.f;
        }
    }

    void processExport(
        ProcessContext& context
    ) noexcept {

        process(context);
    }

    void inheritState(
        const NodeProcessor& previous
    ) noexcept override {

        const auto& old =
            static_cast<const ClockNode&>(
                previous
            );

        manual.seen=old.manual.seen;
        phase =
            old.phase;

        previousSync =
            old.previousSync;
    }

private:

    bool risingSync(
        float value
    ) const noexcept {

        return
            previousSync <= syncThreshold
            && value > syncThreshold;
    }

    static float safeValue(
        float value
    ) noexcept {

        return std::isfinite(value)
            ? value
            : 0.f;
    }

    static float validFrequency(
        float value
    ) noexcept {

        if (!std::isfinite(value))
            return 0.f;

        return std::clamp(
            value,
            0.f,
            20000.f
        );
    }
};

EASYKNACK_NODE(
    ClockNode,
    "clock"
)

}