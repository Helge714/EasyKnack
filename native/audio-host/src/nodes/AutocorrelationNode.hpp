#pragma once

#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../Analysis.hpp"
#include "../AnalysisData.hpp"
#include "../OwnedArray.hpp"

#include <algorithm>
#include <array>
#include <cmath>
#include <complex>
#include <cstddef>
#include <memory>

namespace easyknack {

struct AutocorrelationNode : NodeProcessor {
    AnalysisResource analysis;
    std::unique_ptr<AnalysisResult> analysisResult;
    std::uint64_t analysisIdentity=HistoryResource::nextIdentity.fetch_add(1),analysisSequence=0,totalSamples=0;
    ResourceKind outputResourceKind(std::size_t port)const noexcept override{return port==2?ResourceKind::analysis:ResourceKind::none;}
    DspResource* resourceOutput(std::size_t port)noexcept override{return port==2?&analysis:nullptr;}
    void prepare(double,std::size_t)override{
        if(!analysisResult)analysisResult=std::make_unique<AnalysisResult>();
        analysis.view=AnalysisView::correlation;
    }
    bool right=false;
    std::complex<double>* spectralSnapshot=nullptr; // Optional worker-owned FFT reuse target.
    bool mergesVoices()const noexcept override{return true;}
    bool separatesChannels()const noexcept override{return true;}

    /*
     * Analysis window.
     *
     * 8192 samples:
     *
     * 48 kHz -> ~170.7 ms
     */
    static constexpr unsigned size =
        8192;

    /*
     * For linear autocorrelation we need at least:
     *
     *   2 * N - 1
     *
     * samples.
     *
     * For N = 8192:
     *
     *   16383 required
     *
     * 16384 is the next power of two and therefore ideal for FFT.
     */
    static constexpr unsigned fftSize =
        size * 2;

    /*
     * New analysis every 4096 input samples.
     *
     * 48 kHz -> ~85.3 ms
     *        -> ~11.7 analyses / second
     */
    static constexpr unsigned hopSize =
        4096;

    /*
     * Rolling audio history.
     */
    OwnedArray<float, size> samples{};

    /*
     * FFT working buffer.
     *
     * First N elements contain the centered input.
     * Remaining elements are zero padding.
     */
    std::array<
        std::complex<double>,
        fftSize
    > spectrum{};

    /*
     * Exact positive-lag result:
     *
     * correlation[0]        = rho(0)
     * correlation[1]        = rho(1)
     * ...
     * correlation[N - 1]    = rho(N - 1)
     */
    OwnedArray<float, size> correlation{};

    std::shared_ptr<
        AnalysisMailbox
    > mailbox;

    unsigned writeCursor =
        0;

    unsigned filledSamples =
        0;

    unsigned samplesSinceAnalysis =
        0;

    float frequency =
        0.f;

    float confidence =
        0.f;
    double sourceRms=0;

    float minimum =
        40.f;

    float maximum =
        2000.f;


    bool isObserver()
        const noexcept override {

        return true;
    }


    bool canShareChannels()
        const noexcept override {

        return true;
    }


    bool supportsControlRate()
        const noexcept override {

        return true;
    }


    bool inputUsesBlockData(
        std::size_t
    ) const noexcept override {

        return true;
    }


    std::size_t outputCount()
        const noexcept override {

        return 3;
    }


    void bindIdentity(
        const std::string& id
    ) override {

        mailbox =
            analysisMailbox(id);
    }


    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const AutocorrelationNode&>(configuration);
        if(right!=p.right){writeCursor=filledSamples=samplesSinceAnalysis=0;frequency=confidence=0;
            totalSamples=analysisSequence=0;analysisIdentity=HistoryResource::nextIdentity.fetch_add(1);analysis.result=nullptr;}
        minimum=p.minimum;maximum=p.maximum;right=p.right;
    }
    void configure(
        std::istream& in
    ) override {

        auto p =
            parameters::
                AutocorrelationNodeParameters::
                read(in);

        sources =
            p.sources();

        right = p.channel_value == "right";

        minimum =
            static_cast<float>(
                p.minimum_value
            );

        maximum =
            static_cast<float>(
                p.maximum_value
            );

        if (
            minimum >= maximum
        ) {

            throw std::runtime_error(
                "Autocorrelation minimum must be below maximum"
            );
        }
    }


    void adoptState(NodeProcessor& previous)noexcept override{
        auto& p=static_cast<AutocorrelationNode&>(previous);if(right!=p.right)return;
        samples.swap(p.samples);correlation.swap(p.correlation);
        analysisResult.swap(p.analysisResult);analysis.result=p.analysis.result?analysisResult.get():nullptr;p.analysis.result=nullptr;
        analysisIdentity=p.analysisIdentity;analysisSequence=p.analysisSequence;totalSamples=p.totalSamples;
        writeCursor=p.writeCursor;filledSamples=p.filledSamples;samplesSinceAnalysis=p.samplesSinceAnalysis;frequency=p.frequency;confidence=p.confidence;
    }
    void inheritState(
        const NodeProcessor& old
    ) noexcept override {

        const auto& p =
            static_cast<
                const AutocorrelationNode&
            >(old);

        if(right!=p.right)return;
        if(analysisResult&&p.analysisResult){*analysisResult=*p.analysisResult;analysis.result=p.analysis.result?analysisResult.get():nullptr;}
        analysisIdentity=p.analysisIdentity;analysisSequence=p.analysisSequence;totalSamples=p.totalSamples;

        samples =
            p.samples;

        correlation =
            p.correlation;

        writeCursor =
            p.writeCursor;

        filledSamples =
            p.filledSamples;

        samplesSinceAnalysis =
            p.samplesSinceAnalysis;

        frequency =
            p.frequency;

        confidence =
            p.confidence;

        /*
         * spectrum[] is only temporary FFT workspace.
         *
         * It deliberately does not need to be inherited.
         */
    }


    /**
     * Return samples from the rolling window chronologically:
     *
     *   index 0        = oldest
     *   index N - 1    = newest
     */
    float rollingSample(
        unsigned index
    ) const noexcept {

        return samples[
            (
                writeCursor
                + index
            )
            % size
        ];
    }


    /**
     * In-place radix-2 FFT.
     *
     * inverse = false:
     *     time -> frequency
     *
     * inverse = true:
     *     frequency -> time
     */
    void fft(
        bool inverse
    ) noexcept {

        /*
         * Bit-reversal permutation.
         */
        for (
            unsigned i = 1, j = 0;
            i < fftSize;
            ++i
        ) {

            unsigned bit =
                fftSize >> 1;

            for (
                ;
                j & bit;
                bit >>= 1
            ) {

                j ^=
                    bit;
            }

            j ^=
                bit;

            if (i < j) {

                std::swap(
                    spectrum[i],
                    spectrum[j]
                );
            }
        }

        /*
         * Iterative Cooley-Tukey FFT.
         */
        for (
            unsigned length = 2;
            length <= fftSize;
            length <<= 1
        ) {

            const double angle =
                (
                    inverse
                        ? 2.0
                        : -2.0
                )
                * 3.14159265358979323846
                / static_cast<double>(
                    length
                );

            const std::complex<double> step(
                std::cos(angle),
                std::sin(angle)
            );

            for (
                unsigned base = 0;
                base < fftSize;
                base += length
            ) {

                std::complex<double> w(
                    1.0,
                    0.0
                );

                const unsigned half =
                    length >> 1;

                for (
                    unsigned j = 0;
                    j < half;
                    ++j
                ) {

                    const auto even =
                        spectrum[
                            base + j
                        ];

                    const auto odd =
                        spectrum[
                            base
                            + j
                            + half
                        ]
                        * w;

                    spectrum[
                        base + j
                    ] =
                        even + odd;

                    spectrum[
                        base
                        + j
                        + half
                    ] =
                        even - odd;

                    w *=
                        step;
                }
            }
        }

        /*
         * Conventional normalization on inverse FFT.
         */
        if (inverse) {

            const double scale =
                1.0
                / static_cast<double>(
                    fftSize
                );

            for (
                auto& value :
                spectrum
            ) {

                value *=
                    scale;
            }
        }
    }


    void analyze(
        double sampleRate
    ) noexcept {

        /*
         * ----------------------------------------------------
         * 1. Mean / DC removal
         * ----------------------------------------------------
         */

        double mean =
            0.0;

        for (
            unsigned i = 0;
            i < size;
            ++i
        ) {

            mean +=
                static_cast<double>(
                    rollingSample(i)
                );
        }

        mean /=
            static_cast<double>(
                size
            );


        /*
         * ----------------------------------------------------
         * 2. Fill FFT buffer.
         *
         * First N values:
         *     centered input
         *
         * Remaining N values:
         *     zero padding
         * ----------------------------------------------------
         */

        for (
            unsigned i = 0;
            i < size;
            ++i
        ) {

            const double value =
                static_cast<double>(
                    rollingSample(i)
                )
                - mean;

            spectrum[i] =
                {
                    value,
                    0.0
                };
        }

        std::fill(
            spectrum.begin()
                + size,
            spectrum.end(),
            std::complex<double>{
                0.0,
                0.0
            }
        );


        /*
         * ----------------------------------------------------
         * 3. FFT
         *
         * x[n] -> X[k]
         * ----------------------------------------------------
         */

        fft(false);
        if(spectralSnapshot)std::copy(spectrum.begin(),spectrum.end(),spectralSnapshot);


        /*
         * ----------------------------------------------------
         * 4. Power spectrum
         *
         * X[k] * conj(X[k])
         *
         * =
         *
         * |X[k]|²
         * ----------------------------------------------------
         */

        for (
            auto& value :
            spectrum
        ) {

            value =
                {
                    std::norm(value),
                    0.0
                };
        }


        /*
         * ----------------------------------------------------
         * 5. Inverse FFT
         *
         * |X[k]|² -> raw autocorrelation
         *
         * spectrum[lag].real() now contains:
         *
         * sum x[i] * x[i + lag]
         * ----------------------------------------------------
         */

        fft(true);


        /*
         * ----------------------------------------------------
         * 6. Unbiased normalization
         *
         * Python reference:
         *
         * overlap = N - lag
         *
         * unbiased =
         *     raw / overlap
         *
         * normalized =
         *     unbiased / unbiased[0]
         * ----------------------------------------------------
         */

        const double rawZero =
            spectrum[0].real();

        const double zeroLag =
            rawZero
            / static_cast<double>(
                size
            );
        sourceRms=std::sqrt(std::max(0.,zeroLag));

        if (
            !std::isfinite(zeroLag)
            || std::abs(zeroLag)
                <= 1e-12
        ) {

            std::fill(
                correlation.begin(),
                correlation.end(),
                0.f
            );

            frequency =
                0.f;

            confidence =
                0.f;

            publish(
                sampleRate
            );

            return;
        }


        for (
            unsigned lag = 0;
            lag < size;
            ++lag
        ) {

            const unsigned overlap =
                size - lag;

            const double raw =
                spectrum[lag].real();

            const double unbiased =
                raw
                / static_cast<double>(
                    overlap
                );

            double normalized =
                unbiased
                / zeroLag;

            if (
                !std::isfinite(
                    normalized
                )
            ) {

                normalized =
                    0.0;
            }

            /*
             * Tiny FFT rounding errors can slightly exceed ±1.
             */
            correlation[lag] =
                static_cast<float>(
                    std::clamp(
                        normalized,
                        -1.0,
                        1.0
                    )
                );
        }

        /*
         * Guarantee mathematical normalization exactly at lag 0.
         */
        correlation[0] =
            1.f;


        /*
         * ----------------------------------------------------
         * 7. Pitch detection
         *
         * Important:
         *
         * minimum / maximum affect ONLY the pitch detector.
         *
         * The autocorrelation itself was calculated completely
         * for all 8192 positive lags.
         * ----------------------------------------------------
         */

        const unsigned first =
            std::max(
                2u,
                static_cast<unsigned>(
                    sampleRate
                    / static_cast<double>(
                        maximum
                    )
                )
            );

        const unsigned last =
            std::min(
                size - 2,
                static_cast<unsigned>(
                    sampleRate
                    / static_cast<double>(
                        minimum
                    )
                )
            );

        frequency =
            0.f;

        confidence =
            0.f;


        if (first <= last) {

            float best =
                0.f;

            for (
                unsigned lag = first;
                lag <= last;
                ++lag
            ) {

                if (
                    correlation[lag]
                        >= correlation[
                            lag - 1
                        ]
                    &&
                    correlation[lag]
                        > correlation[
                            lag + 1
                        ]
                ) {

                    best =
                        std::max(
                            best,
                            correlation[lag]
                        );
                }
            }


            unsigned peak =
                0;


            const float requiredPeak =
                std::max(
                    0.3f,
                    best * 0.95f
                );


            for (
                unsigned lag = first;
                lag <= last;
                ++lag
            ) {

                if (
                    correlation[lag]
                        >= requiredPeak
                    &&
                    correlation[lag]
                        >= correlation[
                            lag - 1
                        ]
                    &&
                    correlation[lag]
                        > correlation[
                            lag + 1
                        ]
                ) {

                    peak =
                        lag;

                    break;
                }
            }


            if (peak != 0) {

                confidence =
                    std::clamp(
                        correlation[peak],
                        0.f,
                        1.f
                    );


                /*
                 * Parabolic interpolation around the discrete
                 * correlation peak.
                 */
                const double a =
                    correlation[
                        peak - 1
                    ];

                const double b =
                    correlation[
                        peak
                    ];

                const double c =
                    correlation[
                        peak + 1
                    ];

                const double denominator =
                    a
                    - 2.0 * b
                    + c;

                const double delta =
                    std::abs(
                        denominator
                    ) > 1e-12

                        ? 0.5
                            * (a - c)
                            / denominator

                        : 0.0;


                const double period =
                    static_cast<double>(
                        peak
                    )
                    + std::clamp(
                        delta,
                        -0.5,
                        0.5
                    );


                if (period > 0.0) {

                    frequency =
                        static_cast<float>(
                            sampleRate
                            / period
                        );
                }
            }
        }


        publish(
            sampleRate
        );
    }


    /**
     * Transfer all positive lags.
     *
     * No decimation.
     * No interpolation.
     * No resampling.
     *
     * Java mirrors the negative side because:
     *
     * rho(-k) == rho(k)
     */
    void publish(
        double sampleRate
    ) noexcept {

        if(analysisResult){
            auto& result=*analysisResult;result.availableViews=8;result.spectrum.fftFrames=0;
            result.sequence=++analysisSequence;result.historyIdentity=analysisIdentity;result.endSample=totalSamples;
            result.sampleRate=sampleRate;result.windowFrames=size;result.fftFrames=fftSize;
            result.binCount=size;result.lagSpacingSeconds=1/sampleRate;result.binSpacingHz=0;
            result.frequency=frequency;result.confidence=confidence;
            result.correlationRms=sourceRms;
            double peak=0,sum=0,squares=0;
            for(unsigned i=0;i<size;i++){double value=correlation[i];result.views[3][i]=correlation[i];peak=std::max(peak,std::abs(value));sum+=value;squares+=value*value;}
            result.statistics[3]={peak,sum/size,std::sqrt(squares/size),sum/sampleRate};
            analysis.view=AnalysisView::correlation;analysis.result=&result;
        }

        if (!mailbox) {
            return;
        }

        AnalysisFrame frame;

        frame.frequency =
            frequency;

        frame.confidence =
            confidence;

        frame.span =
            static_cast<double>(
                size - 1
            )
            / sampleRate;

        frame.symmetric =
            true;

        frame.valueCount =
            size;


        static_assert(
            size
                <= AnalysisFrame::maxValues,
            "AnalysisFrame too small for autocorrelation"
        );


        std::copy(
            correlation.begin(),
            correlation.end(),
            frame.values.begin()
        );


        mailbox->publish(
            frame
        );
    }


    void process(
        ProcessContext& c
    ) noexcept override {

        // The scheduler exposes every voice and both channels to this mono analyzer.


        const std::size_t frames =
            c.blockFrames
                ? c.blockFrames
                : c.bufferSize;


        for (
            std::size_t i = 0;
            i < frames;
            ++i
        ) {

            float sum = 0.f;
            for (std::size_t voice=0;voice<c.inputCount;++voice) {
                const float* input=right&&c.rightInputs?c.rightInputs[voice]:c.input(voice);
                if(input&&std::isfinite(input[i]))sum+=input[i];
            }
            samples[writeCursor]=std::isfinite(sum)?sum:0.f;
            ++totalSamples;


            writeCursor =
                (
                    writeCursor
                    + 1
                )
                % size;


            if (
                filledSamples
                    < size
            ) {

                ++filledSamples;

                if (
                    filledSamples
                        < size
                ) {

                    continue;
                }

                /*
                 * Analyze immediately when the first complete
                 * window becomes available.
                 */
                samplesSinceAnalysis =
                    hopSize - 1;
            }


            ++samplesSinceAnalysis;


            if (
                samplesSinceAnalysis
                    >= hopSize
            ) {

                samplesSinceAnalysis =
                    0;

                /*
                 * Still executed on the audio thread for now.
                 *
                 * TODO CORE:
                 * move this to the reusable analysis worker.
                 */
                analyze(
                    c.sampleRate
                );
            }
        }


        std::fill_n(
            c.output,
            c.bufferSize,
            frequency
        );


        if (
            auto* out =
                c.outputAt(1)
        ) {

            std::fill_n(
                out,
                c.bufferSize,
                confidence
            );
        }
    }
};


EASYKNACK_NODE(
    AutocorrelationNode,
    "autocorrelation"
)

}
