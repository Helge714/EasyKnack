#include "../src/AudioGraph.hpp"
#include "ChannelSharingTest.hpp"
#include "ChannelRoutingTest.hpp"
#include "CurveTest.hpp"
#include "FilterMidiTest.hpp"
#include "PolyMidiTest.hpp"
#include "ValueEventTest.hpp"
#include "MixUnitsTest.hpp"
#include "BlockDelayTest.hpp"
#include "SampleFeedbackTest.hpp"
#include <iostream>
#include <stdexcept>
#include <thread>

void requireAt(bool ok,int line) { if (!ok) throw std::runtime_error("DSP assertion failed at line "+std::to_string(line)); }
#define require(...) requireAt((__VA_ARGS__),__LINE__)
struct TestAudioIO : easyknack::AudioIO {
    std::array<float, 8> first{}, second{};
    void beginBlock(std::size_t) noexcept override { first.fill(0); second.fill(0); }
    float input(const std::string& id, std::size_t channel, std::size_t) const noexcept override {
        return id == "a" ? (channel == 0 ? .25f : -.75f) : .5f;
    }
    void output(const std::string& id, std::size_t channel, const float* data, std::size_t frames) noexcept override {
        auto& target = id == "x" ? first : second;
        for (std::size_t i=0; i<frames; ++i) target[2*i+channel] += data[i];
    }
    void endBlock(const float*, std::size_t) noexcept override { }
};
#include "UniversalDelayTest.hpp"
int main() {
    for(const auto* id:{"allpass","delayallpass","envelopefollower","dynamicscurve","compressor","limiter","freeverb"})
        assert(easyknack::MultipleGraphNode::factory(id));

    std::cout<<std::unitbuf;
    using namespace easyknack;
    channel_tests::run();
    channel_routing_tests::run();
    curve_tests::run();
    filter_midi_tests::run();
    poly_midi_tests::run();
    value_event_tests::run();
    mix_units_tests::run();
    block_delay_tests::run();
    universal_delay_tests::run();
    sample_feedback_tests::run();
    {
        GainNode gain;std::istringstream parameters("-1 0");gain.configure(parameters);
        float input[]={1,1,1,1},modulation[]={0,-160,6,std::numeric_limits<float>::quiet_NaN()},output[4]{};
        const float* inputs[]={input,modulation};char rates[]={'a','a'};
        ProcessContext c{inputs,2,output,4,48000,nullptr};c.sourceRates=rates;gain.process(c);
        require(output[0]==1&&output[1]==0&&std::abs(output[2]-std::pow(10.f,.3f))<1e-6f&&output[3]==0);
        rates[1]='c';gain.process(c);for(float value:output)require(value==1);
        inputs[1]=nullptr;gain.process(c);require(output[0]>0&&output[0]<1);
        std::cout<<"Gain audio/control modulation and unconnected smoothing passed\n";
    }
    StereoRing<4> ring;
    std::array<float, 12> ringInput{1,2,3,4,5,6,7,8,9,10,11,12}, ringOutput{};
    ring.write(ringInput.data(), 6); require(ring.overruns.load() == 1);
    ring.read(ringOutput.data(), 3); require(ringOutput[0] == 1 && ringOutput[5] == 6);
    ring.write(ringInput.data()+8, 2); ring.read(ringOutput.data(), 4);
    require(ringOutput[0] == 7 && ringOutput[2] == 9 && ringOutput[4] == 11 && ringOutput[6] == 0);
    require(ring.underruns.load() == 1);
    auto stereoGraph = AudioGraph::parse("GRAPH 5 a input 61 copy stereo @ a - b input 62 copy mono @ a - m math 0 0 1 0 add 0 @ a aa x deviceout 2 78 @ a a y deviceout 0 79 @ a a");
    TestAudioIO io; std::array<float,4> stereoDummy{};
    io.beginBlock(4); stereoGraph.process(stereoDummy.data(), 4, 0, &io); stereoGraph.process(stereoDummy.data(), 4, 1, &io);
    require(io.first[0] == .75f && io.first[1] == -.25f && io.second[0] == .25f && io.second[1] == -.75f);
    require(stereoGraph.routes().size() == 4);
    auto monoCapture = AudioGraph::parse("GRAPH 2 a input 61 copy mono @ a - x deviceout 0 78 @ a a");
    io.beginBlock(4); monoCapture.process(stereoDummy.data(), 4, 0, &io); monoCapture.process(stereoDummy.data(), 4, 1, &io);
    require(io.first[0] == -.25f && io.first[1] == -.25f);
    auto additive = [](const std::string& parameters) {
        return AudioGraph::parse("GRAPH 2 osc additive -1 " + parameters + " @ a aaa out out 0 0 @ a a");
    };
    std::array<float, blockSize> additiveBuffer{}, additiveReference{};
    auto valueGraph = AudioGraph::parse("GRAPH 2 value value -1234567.5 @ c - out out 0 0 @ a a");
    valueGraph.process(additiveBuffer.data(), blockSize);
    for (float value : additiveBuffer) require(value == -1234567.5f);
    auto testWave=std::make_shared<WaveformResource>();
    std::array<float,WaveformResource::size> testSamples{};testSamples.fill(.25f);testWave->fromSamples(testSamples);
    struct TestTable:WaveformNode {
        explicit TestTable(std::shared_ptr<const WaveformResource> value){wave=std::move(value);}
        void configure(std::istream&)override{}
    };
    auto parseWave=[&](const std::string& message){return AudioGraph::compile(message,
        [&](const std::string& type)->std::unique_ptr<NodeProcessor>{if(type=="testwave")return std::make_unique<TestTable>(testWave);return createNodeProcessor(type);});};
    auto simpleCustomWave = parseWave("GRAPH 3 table testwave @ c - osc osc -1 440 wave square 0 @ a aa out out 1 0 @ a a");
    simpleCustomWave.process(additiveBuffer.data(), blockSize);
    for (float value : additiveBuffer) require(value == .25f);
    for(std::size_t i=0;i<testSamples.size();i++)testSamples[i]=std::sin(i*6.283185307179586/testSamples.size());
    testWave->fromSamples(testSamples);
    // Control-rate resource selection keeps the complete table, not a held first sample.
    for (bool additiveTarget : {false, true}) {
        std::string prefix = "GRAPH 3 table testwave @ c - target ";
        std::string processor = additiveTarget ? "additive 0 sine 1 1 0 -1 440 -1 0 @ a " : "osc -1 440 wave sine 0 @ a ";
        auto audioTable = parseWave(prefix + processor + (additiveTarget ? "aaa" : "aa") + " out out 1 0 @ a a");
        auto controlTable = parseWave(prefix + processor + (additiveTarget ? "caa" : "ac") + " out out 1 0 @ a a");
        audioTable.process(additiveReference.data(), blockSize); controlTable.process(additiveBuffer.data(), blockSize);
        require(additiveBuffer == additiveReference);
        require(std::any_of(additiveBuffer.begin(), additiveBuffer.end(), [](float value) { return std::abs(value) > .1f; }));
    }
    for (const std::string shape : {"sine", "triangle", "saw", "square"}) {
        auto simple = AudioGraph::parse("GRAPH 2 osc osc -1 440 wave " + shape + " -1 @ a aa out out 0 0 @ a a");
        auto reference = additive(shape + " 1 1 0 -1 440 -1 0");
        simple.process(additiveBuffer.data(), blockSize); reference.process(additiveReference.data(), blockSize);
        for (std::size_t i = 0; i < blockSize; ++i) require(std::abs(additiveBuffer[i] - additiveReference[i]) < .000003f);
    }
    auto controlled = AudioGraph::parse("GRAPH 5 count value 2 @ c - slope value 1 @ c - spread value 1 @ c - osc additive -1 sine 2 1 0 -1 440 -1 0 inputs 0 1 2 @ a aaacaa out out 3 0 @ a a");
    auto controlledReference = additive("sine 2 1 0 -1 440 -1 0");
    controlled.process(additiveBuffer.data(), blockSize); controlledReference.process(additiveReference.data(), blockSize);
    require(additiveBuffer == additiveReference);
    auto beforeEdit = AudioGraph::parse("GRAPH 2 m math -1 1 -1 1 multiply 0 @ a aa out out 0 0 @ a a");
    auto afterEdit = AudioGraph::parse("GRAPH 2 m math -1 1 -1 0 multiply 0 @ a aa out out 0 0 @ a a");
    beforeEdit.process(additiveBuffer.data(), blockSize); afterEdit.inheritState(beforeEdit);
    afterEdit.process(additiveBuffer.data(), blockSize);
    require(additiveBuffer[0] > .99f && additiveBuffer.back() < .4f);
    AdditiveOscillatorNode fullSpectrum, reducedSpectrum;
    std::istringstream fullParameters("-1 sine 144 1 0 -1 440 -1 0"), reducedParameters("-1 sine 1 1 0 -1 440 -1 0");
    fullSpectrum.configure(fullParameters); reducedSpectrum.configure(reducedParameters);
    reducedSpectrum.inheritState(fullSpectrum);
    ProcessContext reducedContext{nullptr, 0, additiveBuffer.data(), blockSize, sampleRate, nullptr};
    for (unsigned block = 0; block < 20; ++block) reducedSpectrum.process(reducedContext);
    require(reducedSpectrum.renderCount == 1);
    auto twoSines = additive("sine 2 1 0 -1 440 -1 0");
    for (int cents : {-100, 0, 100}) {
        AdditiveOscillatorNode spreadNode;
        std::istringstream parameters("-1 sine 2 1 " + std::to_string(cents) + " -1 440 -1 0");
        spreadNode.configure(parameters);
        double factor = std::exp2(cents / 1200.0);
        require(spreadNode.ratios[0] == 1);
        for (unsigned h = 1; h < AdditiveOscillatorNode::maximumPartials; ++h)
            // Legacy cents migrate to the current power-law exponent.
            require(std::abs(spreadNode.ratios[h] - std::pow(double(h + 1),double(spreadNode.spread))) < 1e-12);
        ProcessContext context{nullptr, 0, additiveBuffer.data(), blockSize, sampleRate, nullptr};
        spreadNode.process(context);
        for (unsigned i = 0; i < blockSize; ++i) {
            double angle = AdditiveOscillatorNode::tau * 440 * i / sampleRate;
            require(std::abs(additiveBuffer[i] - (std::sin(angle) + .5 * std::sin(2 * factor * angle)) / 1.5) < .000003);
        }
    }
    twoSines.process(additiveBuffer.data(), blockSize);
    for (std::size_t i = 0; i < blockSize; ++i) {
        double angle = AdditiveOscillatorNode::tau * 440 * i / sampleRate;
        require(std::abs(additiveBuffer[i] - (std::sin(angle) + .5 * std::sin(2 * angle)) / 1.5) < 0.000003);
    }
    for (const std::string shape : {"sine", "triangle", "saw", "square"}) {
        auto fundamental = additive(shape + " 1 1 0 -1 440 -1 0");
        auto overtone = additive(shape + " 1 1 0 -1 880 -1 0");
        auto pair = additive(shape + " 2 1 0 -1 440 -1 0");
        std::array<float, blockSize> upper{};
        fundamental.process(additiveReference.data(), blockSize);
        overtone.process(upper.data(), blockSize); pair.process(additiveBuffer.data(), blockSize);
        for (std::size_t i = 0; i < blockSize; ++i)
            require(std::abs(additiveBuffer[i] - (additiveReference[i] + .5 * upper[i]) / 1.5) < 0.000001);
        auto many = additive(shape + " 144 0 100 -1 440 -1 90");
        for (int block = 0; block < 8; ++block) {
            many.process(additiveBuffer.data(), blockSize);
            for (float value : additiveBuffer) require(std::isfinite(value) && std::abs(value) <= 1.000001f);
        }
        auto single = additive(shape + " 1 1 0 -1 20000 -1 0");
        auto high = additive(shape + " 144 1 0 -1 20000 -1 0");
        single.process(additiveReference.data(), blockSize); high.process(additiveBuffer.data(), blockSize);
        require(additiveReference == additiveBuffer);
        auto continuedAdditive = additive(shape + " 144 0 100 -1 440 -1 90");
        continuedAdditive.inheritState(many);
        many.process(additiveReference.data(), blockSize);
        continuedAdditive.process(additiveBuffer.data(), 17);
        continuedAdditive.process(additiveBuffer.data() + 17, blockSize - 17);
        require(additiveReference == additiveBuffer);
    }
    for (const std::string parameters : {"sine 0 1 0 -1 440 -1 0", "sine 145 1 0 -1 440 -1 0",
            "sine 1.5 1 0 -1 440 -1 0", "alien 1 1 0 -1 440 -1 0", "sine 1 1 2401 -1 440 -1 0"}) {
        bool rejected = false;
        try { additive(parameters); } catch (const std::exception&) { rejected = true; }
        require(rejected);
    }
    AdditiveOscillatorNode customWave;
    std::istringstream customParameters("0 sine 1 1 0 1 440 -1 90");
    customWave.configure(customParameters);
    std::array<float, 4> cycle{0, 1, 0, -1}, zeroHz{}, customResult{};
    const float* customInputs[] = {cycle.data(), zeroHz.data(), nullptr};
    ProcessContext customContext{customInputs, 3, customResult.data(), 4, sampleRate, zeroHz.data()};
    customWave.process(customContext);
    for (auto value : customResult) require(value == 1);
    AudioTiming timing;
    timing.observe(0.0025, 256, 51200); // One complete 5 ms measurement cycle: 50%.
    require(timing.average.load() == 5000 && timing.peak.load() == 5000);
    timing.observe(0.010, 256, 51200);
    require(timing.peak.load() == 20000 && timing.overBudget.load() == 1);
    require(timing.average.load() > 5000 && timing.average.load() < 20000);
    timing.observe(0.005, 256, 51200); // Exactly 100%; do not count as exceeding the budget.
    require(timing.overBudget.load() == 1);
    auto averageBeforeEmpty = timing.average.load();
    timing.observe(1, 0, 48000);
    require(timing.average.load() == averageBeforeEmpty);
    std::array<float, 4> a{2, -2, 0.5f, 6}, b{3, 3, 0, -2}, result{};
    MathNode math;
    auto processMath = [&](const float* inputA, const float* inputB, float* output,
                           std::size_t size, float defaultA, float defaultB,
                           MathOperation operation, bool clamp) {
        math.aValue = math.currentA = defaultA;
        math.bValue = math.currentB = defaultB;
        math.operation = operation;
        math.clamp = clamp;
        const float* inputs[]{inputA, inputB};
        std::array<float, 4> silence{};
        ProcessContext context{inputs, 2, output, size, 48000, silence.data(), size};
        math.process(context);
    };
    processMath(a.data(), b.data(), result.data(), 4, 0, 0, MathOperation::multiply, false);
    require(result == std::array<float, 4>{6, -6, 0, -12});
    processMath(a.data(), b.data(), result.data(), 4, 0, 0, MathOperation::multiply, true);
    require(result == std::array<float, 4>{1, -1, 0, -1});
    processMath(a.data(), b.data(), result.data(), 4, 0, 0, MathOperation::add, false);
    require(result == std::array<float, 4>{5, 1, 0.5f, 4});
    processMath(a.data(), b.data(), result.data(), 4, 0, 0, MathOperation::subtract, false);
    require(result == std::array<float, 4>{-1, -5, 0.5f, 8});
    processMath(a.data(), b.data(), result.data(), 4, 0, 0, MathOperation::divide, false);
    require(result[2] == 0 && result[3] == -3);
    processMath(a.data(), b.data(), result.data(), 4, 0, 0, MathOperation::minimum, false);
    require(result == std::array<float, 4>{2, -2, 0, -2});
    processMath(a.data(), b.data(), result.data(), 4, 0, 0, MathOperation::maximum, false);
    require(result == std::array<float, 4>{3, 3, 0.5f, 6});
    processMath(a.data(), nullptr, result.data(), 4, 0, 0, MathOperation::absolute, false);
    require(result == std::array<float, 4>{2, 2, 0.5f, 6});
    processMath(a.data(), nullptr, result.data(), 4, 0, 0, MathOperation::negate, false);
    require(result == std::array<float, 4>{-2, 2, -0.5f, -6});
    processMath(nullptr, nullptr, result.data(), 4, 3, 4, MathOperation::multiply, false);
    require(result == std::array<float, 4>{12, 12, 12, 12});
    processMath(a.data(), nullptr, result.data(), 4, 0, 2, MathOperation::multiply, false);
    require(result == std::array<float, 4>{4, -4, 1, 12});
    auto mathGraph = AudioGraph::parse("GRAPH 3 out out 2 0 @ a a a math -1 3 -1 4 add 0 @ a aa b math 1 0 -1 2 multiply 0 @ a aa");
    mathGraph.process(result.data(), 4);
    require(result == std::array<float, 4>{14, 14, 14, 14});
    auto frequencyGraph = AudioGraph::parse("GRAPH 3 osc osc 2 20 @ a a out out 0 0 @ a a hz math -1 440 -1 1 multiply 0 @ a aa");
    auto referenceGraph = AudioGraph::parse("GRAPH 2 osc osc -1 440 @ a a out out 0 0 @ a a");
    std::array<float, 4> reference{};
    frequencyGraph.process(result.data(), 4); referenceGraph.process(reference.data(), 4);
    require(result == reference);
    auto unaryGraph = AudioGraph::parse("GRAPH 2 a math -1 -2 -1 99 absolute 0 @ a aa out out 0 0 @ a a");
    unaryGraph.process(result.data(), 4);
    require(result == std::array<float, 4>{2, 2, 2, 2});
    // The new polymorphic processor must carry phase and smoothing state across swaps.
    auto continued = AudioGraph::parse("GRAPH 2 osc osc -1 440 @ a a out out 0 0 @ a a");
    continued.inheritState(referenceGraph);
    continued.process(result.data(), 4); referenceGraph.process(reference.data(), 4);
    require(result == reference);
    auto graph = AudioGraph::parse("GRAPH 3 osc osc -1 440 @ a a gain gain 0 -12 @ a a out out 1 0 @ a a");
    std::array<float, blockSize> buffer{};
    double energy = 0;
    int crossings = 0;
    float previous = 0;
    for (int block = 0; block < 188; ++block) {
        graph.process(buffer.data(), blockSize);
        for (float sample : buffer) {
            require(std::isfinite(sample) && std::abs(sample) < 0.252f);
            energy += sample * sample;
            if (previous <= 0 && sample > 0) ++crossings;
            previous = sample;
        }
    }
    require(energy > 1400 && energy < 1600);
    require(crossings >= 440 && crossings <= 442);
    auto disconnected = AudioGraph::parse("GRAPH 1 out out -1 0 @ a a");
    disconnected.process(buffer.data(), blockSize);
    for (float sample : buffer) require(sample == 0);
    for (auto invalid : {"GRAPH 257", "GRAPH 1 x osc -1 nan @ a a", "GRAPH 1 x osc -1 30000 @ a a",
            "GRAPH 2 a gain 1 0 @ a a b gain 0 0 @ a a", "GRAPH 1 x gain 3 0 @ a a", "GRAPH 1 x alien -1 0 @ a a",
            "GRAPH 2 a math -1 1 1 0 add 0 @ a aa b gain 0 0 @ a a",
            "GRAPH 1 x math -1 1 -1 1 wrong 0 @ a aa", "GRAPH 1 x math -1 1 -1 1 add 2 @ a aa"}) {
        bool rejected = false;
        try { AudioGraph::parse(invalid); } catch (const std::exception&) { rejected = true; }
        require(rejected);
    }
    // Audio-to-control samples the block start; control-to-audio holds that sample.
    auto control = AudioGraph::parse("GRAPH 3 osc osc -1 440 @ a a m math 0 0 -1 1 multiply 0 @ c cc out out 1 0 @ a a");
    auto sampledInput = AudioGraph::parse("GRAPH 3 osc osc -1 440 @ a a m math 0 0 -1 1 multiply 0 @ a ca out out 1 0 @ a a");
    auto audioReference = AudioGraph::parse("GRAPH 2 osc osc -1 440 @ a a out out 0 0 @ a a");
    std::array<float, blockSize> expected{}, sampled{};
    for (auto frames : {1, 17, 256, 256}) {
        control.process(buffer.data(), frames);
        sampledInput.process(sampled.data(), frames);
        audioReference.process(expected.data(), frames);
        for (int i = 0; i < frames; ++i) {
            require(buffer[i] == expected[0]);
            require(sampled[i] == expected[0]);
        }
    }
    for (auto invalid : {"GRAPH 1 x osc -1 440 @ c c", "GRAPH 1 x out -1 0 @ c c",
            "GRAPH 1 x math -1 1 -1 2 add 0 @ c a", "GRAPH 1 x math -1 1 -1 2 add 0 @ x aa",
            "GRAPH 1 x math -1 1 -1 2 add 0 @ a ax", "GRAPH 1 x osc -1 440"}) {
        bool rejected = false;
        try { AudioGraph::parse(invalid); } catch (const std::exception&) { rejected = true; }
        require(rejected);
    }
    auto engine = std::make_unique<AudioEngine>();
    engine->publish(std::move(graph));
    std::array<float, 2000> stereo{};
    engine->render(stereo.data(), 1000); // callback sizes need not equal blockSize
    for (float sample : stereo) require(sample == 0);
    engine->playing.store(true);
    engine->render(stereo.data(), 1000);
    require(std::any_of(stereo.begin(), stereo.end(), [](float x) { return x != 0; }));
    for (int i = 0; i < 1000; ++i) require(stereo[2*i] == stereo[2*i+1]);
    for (int i = 0; i < 100; ++i) {
        engine->publish(AudioGraph::parse("GRAPH 1 out out -1 0 @ a a"));
        engine->render(stereo.data(), 1000);
    }
    for (float sample : stereo) require(sample == 0);
    std::atomic<bool> done{false};
    std::thread audio([&] {
        std::array<float, 512> output{};
        while (!done.load()) engine->render(output.data(), 256);
    });
    for (int i = 0; i < 1000; ++i)
        engine->publish(AudioGraph::parse("GRAPH 2 osc osc -1 440 @ a a out out 0 0 @ a a"));
    done.store(true);
    audio.join();
    {
    // Connected control inputs are exact block constants; no consumer-side glide.
    auto control=AudioGraph::parse("GRAPH 3 value value 0.5 @ c - scale math 0 0 -1 1 multiply 0 @ a aa out out 1 0 @ a a");
    require(control.nodes[1].heldInputs.empty());
    std::array<float,blockSize> trajectory{};control.process(trajectory.data(),blockSize);
    float last=trajectory.back();require(last==.5f);
    for(int update=0;update<30;update++) {
        float target=update%2?.6f:.9f;
        auto next=AudioGraph::parse("GRAPH 3 value value "+std::to_string(target)+" @ c - scale math 0 0 -1 1 multiply 0 @ a aa out out 1 0 @ a a");
        next.inheritState(control);next.process(trajectory.data(),blockSize);
        for(float sample:trajectory)require(sample==target);
        control=std::move(next);
    }
    }
    {
        // Connected but unreachable branches have sources/inputs, but no smoothing storage.
        // Exercise both directions across the pruning boundary and edits while inactive.
        auto previous=AudioGraph::parse("GRAPH 2 a value 1 @ c - b gain 0 -12 @ a a");
        std::array<float,blockSize> samples{};
        for(int i=0;i<60;i++){
            bool connected=i%3==0;
            auto next=AudioGraph::parse(std::string("GRAPH ")+(connected?"3":"2")+
                " a value 1 @ c - b gain 0 "+std::to_string(-i%12)+" @ a a"+
                (connected?" out out 1 0 @ a a":""));
            next.inheritState(previous);next.process(samples.data(),blockSize);
            for(float sample:samples)require(std::isfinite(sample));
            previous=std::move(next);
        }
    }
    std::cout << "Audio graph tests passed\n";
}


