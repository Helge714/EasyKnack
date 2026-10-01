#pragma once

#include <algorithm>
#include <array>
#include <atomic>
#include <cstddef>
#include <map>
#include <memory>
#include <sstream>
#include <string>

namespace easyknack {

struct AnalysisFrame {

    /*
     * Large enough for scientific analyzers such as autocorrelation.
     *
     * Other existing analyzers remain at 256 samples unless they
     * explicitly increase valueCount.
     */
    static constexpr std::size_t maxValues = 8193;
    static constexpr std::size_t defaultValues = 256;

    float frequency = 0.f;
    float confidence = 0.f;

    /*
     * For autocorrelation this is the positive lag span in seconds:
     *
     *   lag 0 ... lag N-1
     */
    double span = 0.0;

    /*
     * Number of valid entries at the beginning of values[].
     */
    std::size_t valueCount = defaultValues;

    /*
     * true means values[] stores only:
     *
     *   f(0), f(1), ... f(N-1)
     *
     * and the negative side is mathematically identical:
     *
     *   f(-k) == f(k)
     *
     * The renderer may mirror it without information loss.
     */
    bool symmetric = false;

    std::array<float, maxValues> values{};
};


/**
 * Bounded SPSC mailbox.
 *
 * The audio/render producer never waits, allocates or formats text.
 */
struct AnalysisMailbox {

    std::array<AnalysisFrame, 4> frames{};

    std::atomic<unsigned> write{0};
    std::atomic<unsigned> read{0};

    void publish(
        const AnalysisFrame& frame
    ) noexcept {

        const unsigned w =
            write.load(
                std::memory_order_relaxed
            );

        const unsigned next =
            (w + 1)
            % frames.size();

        if (
            next
            == read.load(
                std::memory_order_acquire
            )
        ) {
            return;
        }

        frames[w] =
            frame;

        write.store(
            next,
            std::memory_order_release
        );
    }

    bool latest(
        AnalysisFrame& frame
    ) noexcept {

        bool found =
            false;

        unsigned r =
            read.load(
                std::memory_order_relaxed
            );

        const unsigned w =
            write.load(
                std::memory_order_acquire
            );

        while (r != w) {

            frame =
                frames[r];

            r =
                (r + 1)
                % frames.size();

            found =
                true;
        }

        read.store(
            r,
            std::memory_order_release
        );

        return found;
    }
};


/*
 * Registry access belongs exclusively to the host control thread.
 * Graphs retain the mailboxes.
 */
inline std::map<
    std::string,
    std::weak_ptr<AnalysisMailbox>
> analysisMailboxes;


inline std::shared_ptr<AnalysisMailbox>
analysisMailbox(
    const std::string& id
) {

    auto& weak =
        analysisMailboxes[id];

    auto value =
        weak.lock();

    if (!value) {

        value =
            std::make_shared<
                AnalysisMailbox
            >();

        weak =
            value;
    }

    return value;
}


/**
 * Analysis protocol version 2:
 *
 * OK 2
 *
 * followed by zero or more frames:
 *
 * id
 * frequency
 * confidence
 * span
 * symmetric
 * valueCount
 * value[0] ... value[valueCount-1]
 *
 * valueCount makes the protocol self-delimiting.
 */
inline std::string analysisResponse() {

    std::ostringstream out;

    out << "OK 2";

    for (
        auto it =
            analysisMailboxes.begin();

        it !=
            analysisMailboxes.end();
    ) {

        auto mailbox =
            it->second.lock();

        if (!mailbox) {

            it =
                analysisMailboxes.erase(
                    it
                );

            continue;
        }

        AnalysisFrame frame;

        if (
            mailbox->latest(frame)
        ) {

            const std::size_t count =
                std::min(
                    frame.valueCount,
                    frame.values.size()
                );

            out
                << ' '
                << it->first

                << ' '
                << frame.frequency

                << ' '
                << frame.confidence

                << ' '
                << frame.span

                << ' '
                << (
                    frame.symmetric
                        ? 1
                        : 0
                )

                << ' '
                << count;

            for (
                std::size_t i = 0;
                i < count;
                ++i
            ) {

                out
                    << ' '
                    << frame.values[i];
            }
        }

        ++it;
    }

    return out.str();
}

}
