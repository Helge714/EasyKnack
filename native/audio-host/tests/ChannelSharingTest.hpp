#pragma once
#include "../src/AudioGraph.hpp"

namespace channel_tests {
using namespace easyknack;
inline void checkAt(bool value, int line) { if (!value) throw std::runtime_error("channel sharing assertion failed at " + std::to_string(line)); }
#define CHANNEL_CHECK(...) checkAt((__VA_ARGS__), __LINE__)
struct IO : AudioIO {
    std::array<float, blockSize * 2> mixed{}, direct{};
    void beginBlock(std::size_t) noexcept override { mixed.fill(0); direct.fill(0); }
    float input(const std::string& id, std::size_t channel, std::size_t) const noexcept override {
        return id == "hz" ? (channel == 0 ? 220.f : 330.f) : (channel == 0 ? .25f : -.75f);
    }
    void output(const std::string& id, std::size_t channel, const float* buffer, std::size_t frames) noexcept override {
        auto& target = id == "x" ? mixed : direct;
        for (std::size_t i = 0; i < frames; ++i) target[2*i + channel] += buffer[i];
    }
    void endBlock(const float*, std::size_t) noexcept override { }
};
struct Counted : NodeProcessor {
    std::unique_ptr<NodeProcessor> inner;
    unsigned& calls;
    std::size_t& samples;
    Counted(std::unique_ptr<NodeProcessor> original, unsigned& count, std::size_t& frames)
        : inner(std::move(original)), calls(count), samples(frames) { sources = inner->sources; }
    void configure(std::istream&) override { }
    void process(ProcessContext& c) noexcept override { ++calls; samples += c.bufferSize; inner->process(c); }
    bool isSink() const noexcept override { return inner->isSink(); }
    bool inputUsesBlockData(std::size_t p) const noexcept override { return inner->inputUsesBlockData(p); }
    const std::string& outputEndpoint() const noexcept override { return inner->outputEndpoint(); }
};
inline void render(AudioGraph& graph, IO& io, int frames, std::array<float, blockSize>& left, std::array<float, blockSize>& right) {
    io.beginBlock(frames); graph.process(left.data(), frames, 0, &io); graph.process(right.data(), frames, 1, &io);
}
inline void run() {
    // Channel context must reach downstream oscillators through every numeric rate.
    for(std::string rate:{"a","c","e"}){
        auto graph=AudioGraph::parse("GRAPH 4 ctx context @ c - map maprange 0 0 -1 -1 -1 1 -1 220 -1 440 0 @ "+rate+" "+rate+rate+rate+rate+rate+" osc osc 1 440 @ a a out out 2 0 @ a a");
        auto referenceLeft=AudioGraph::parse("GRAPH 2 osc osc -1 220 @ a a out out 0 0 @ a a");
        auto referenceRight=AudioGraph::parse("GRAPH 2 osc osc -1 440 @ a a out out 0 0 @ a a");
        std::array<float,blockSize> l{},r{},expectedL{},expectedR{};
        for(auto& node:graph.nodes)CHANNEL_CHECK(node.stereo);
        for(int frames:{1,17,256,35}){
            graph.process(l.data(),frames,0,nullptr);graph.process(r.data(),frames,1,nullptr);
            referenceLeft.process(expectedL.data(),frames,0,nullptr);referenceRight.process(expectedR.data(),frames,0,nullptr);
            CHANNEL_CHECK(std::equal(l.begin(),l.begin()+frames,expectedL.begin()));
            CHANNEL_CHECK(std::equal(r.begin(),r.begin()+frames,expectedR.begin()));
            CHANNEL_CHECK(graph.nodes[0].extraBuffers[0][0]==48000);
            CHANNEL_CHECK(graph.nodes[0].extraBuffers[1][0]==frames);
        }
    }

    // A disconnected expensive oscillator and its capture dependency never run.
    // Shared sources survive bypassing a branch.
    const std::string idleText = "GRAPH 3 cap input 61 copy stereo @ a - osc additive -1 sine 144 1 0 0 440 -1 0 @ a caa out out -1 0 @ a a";
    auto idle = AudioGraph::parse(idleText);
    CHANNEL_CHECK(idle.order == std::vector<int>{2});
    CHANNEL_CHECK(idle.routes().size() == 1 && !idle.routes()[0].input);
    std::array<unsigned, 2> idleCalls{}; std::array<std::size_t, 2> idleSamples{};
    idle.nodes[1].processor = std::make_unique<Counted>(std::move(idle.nodes[1].processor), idleCalls[0], idleSamples[0]);
    idle.nodes[1].rightProcessor = std::make_unique<Counted>(std::move(idle.nodes[1].rightProcessor), idleCalls[1], idleSamples[1]);
    std::array<float, blockSize> quiet{};
    for (int i = 0; i < 32; ++i) idle.process(quiet.data(), blockSize);
    CHANNEL_CHECK(idleCalls[0] == 0 && idleCalls[1] == 0);
    CHANNEL_CHECK(std::all_of(quiet.begin(), quiet.end(), [](float v) { return v == 0; }));
    auto noOutput = AudioGraph::parse("GRAPH 2 cap input 61 copy stereo @ a - osc osc 0 440 @ a a");
    CHANNEL_CHECK(noOutput.order.empty() && noOutput.routes().empty());
    auto shared = AudioGraph::parse("GRAPH 4 a value 2 @ c - b osc -1 440 @ a a unused math 0 0 1 0 add 0 @ a aa out out 0 0 @ a a");
    CHANNEL_CHECK(shared.order == std::vector<int>({0, 3}));
    shared.process(quiet.data(), blockSize);
    CHANNEL_CHECK(quiet[0] == 2);
    const std::string text = "GRAPH 7 osc osc -1 440 @ a a cap input 61 copy stereo @ a - mix math 0 0 1 0 add 0 @ a aa val value 2 @ c - control math 1 0 3 0 multiply 0 @ c cc out deviceout 2 78 @ a a direct deviceout 0 79 @ a a";
    auto optimized = AudioGraph::parse(text), reference = AudioGraph::parse(text);
    std::array<bool, 7> expectedStereo{false, true, true, false, true, true, false};
    std::array<unsigned, 14> calls{}; std::array<std::size_t, 14> samples{};
    for (std::size_t i = 0; i < optimized.nodes.size(); ++i) {
        auto& node = optimized.nodes[i]; CHANNEL_CHECK(node.stereo == expectedStereo[i]); reference.nodes[i].stereo = true;
        node.processor = std::make_unique<Counted>(std::move(node.processor), calls[2*i], samples[2*i]);
        node.rightProcessor = std::make_unique<Counted>(std::move(node.rightProcessor), calls[2*i+1], samples[2*i+1]);
    }
    IO actualIO, referenceIO; std::array<float, blockSize> left{}, right{}, expectedLeft{}, expectedRight{};
    unsigned blocks = 0;
    for (int frames : {1, 17, 256, 35, 256}) {
        render(optimized, actualIO, frames, left, right); render(reference, referenceIO, frames, expectedLeft, expectedRight); ++blocks;
        CHANNEL_CHECK(left == expectedLeft && right == expectedRight);
        CHANNEL_CHECK(actualIO.mixed == referenceIO.mixed && actualIO.direct == referenceIO.direct);
        for (int i = 0; i < frames; ++i) CHANNEL_CHECK(actualIO.direct[2*i] == actualIO.direct[2*i+1]);
        for (std::size_t i = 0; i < optimized.nodes.size(); ++i) {
            const bool active = i != 3 && i != 4;
            CHANNEL_CHECK(optimized.nodes[i].active == active);
            CHANNEL_CHECK(calls[2*i] == (active ? blocks : 0) && calls[2*i+1] == (active && expectedStereo[i] ? blocks : 0));
            if (optimized.nodes[i].controlRate) CHANNEL_CHECK(samples[2*i] == (active ? blocks : 0));
        }
    }
    auto monoCapture = AudioGraph::parse("GRAPH 2 in input 61 copy mono @ a - out deviceout 0 78 @ a a");
    CHANNEL_CHECK(!monoCapture.nodes[0].stereo && !monoCapture.nodes[1].stereo);
    render(monoCapture, actualIO, 17, left, right); CHANNEL_CHECK(std::equal(left.begin(), left.begin() + 17, right.begin()));
    // Existing/plugin processors remain stereo unless they explicitly opt in.
    struct Unspecified : NodeProcessor { void configure(std::istream&) override {} void process(ProcessContext&) noexcept override {} };
    CHANNEL_CHECK(!Unspecified{}.canShareChannels());
    for (bool additive : {false, true}) {
        auto graphText = [additive](bool modulated) {
            std::string processor = additive ? "additive -1 sine 8 1 0 " : "osc ";
            processor += modulated ? "0 440" : "-1 440";
            processor += additive ? " -1 0 @ a caa" : " @ a a";
            return "GRAPH 3 hz input 687a copy stereo @ a - osc " + processor + " out out 1 0 @ a a";
        };
        auto mono = AudioGraph::parse(graphText(false)), forcedStereo = AudioGraph::parse(graphText(false));
        for (auto& node : forcedStereo.nodes) node.stereo = true;
        for (int i = 0; i < 3; ++i) {
            render(mono, actualIO, 17, left, right); render(forcedStereo, referenceIO, 17, expectedLeft, expectedRight);
        }
        auto expanded = AudioGraph::parse(graphText(true)), expandedReference = AudioGraph::parse(graphText(true));
        CHANNEL_CHECK(expanded.nodes[1].stereo);
        expanded.inheritState(mono); expandedReference.inheritState(forcedStereo);
        render(expanded, actualIO, 256, left, right); render(expandedReference, referenceIO, 256, expectedLeft, expectedRight);
        CHANNEL_CHECK(left == expectedLeft && right == expectedRight && left != right);
        auto collapsed = AudioGraph::parse(graphText(false)), collapsedReference = AudioGraph::parse(graphText(false));
        collapsed.inheritState(expanded); collapsedReference.inheritState(expandedReference);
        render(collapsed, actualIO, 256, left, right); render(collapsedReference, referenceIO, 256, expectedLeft, expectedRight);
        CHANNEL_CHECK(left == right && left == expectedLeft);
        auto expandedAgain = AudioGraph::parse(graphText(true)); expandedAgain.inheritState(collapsed);
        auto freshReference = AudioGraph::parse(graphText(true)); freshReference.inheritState(collapsedReference);
        render(expandedAgain, actualIO, 35, left, right); render(freshReference, referenceIO, 35, expectedLeft, expectedRight);
        CHANNEL_CHECK(left == expectedLeft && right == expectedRight);
    }
}
}

#undef CHANNEL_CHECK


