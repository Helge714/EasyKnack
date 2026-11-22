#define EASYKNACK_PORTABLE_HOST
#define MA_NO_FLAC
#define MA_NO_ENCODING
#define MA_NO_RESOURCE_MANAGER
#define MA_NO_NODE_GRAPH
#define MA_NO_ENGINE
#define MA_NO_GENERATION
#ifndef __APPLE__
#define MINIAUDIO_IMPLEMENTATION
#endif
#include "miniaudio.h"
#include "../src/platform/HostPlatform.hpp"
#include "../src/platform/KeyboardMidi.hpp"
#include "../src/AudioGraph.hpp"
#include "../src/platform/SharedLibrary.hpp"
#include <thread>
#include <iostream>

static void requireAt(bool value,int line) { if (!value) throw std::runtime_error("Portable platform assertion failed at "+std::to_string(line)); }
#define require(value) requireAt((value),__LINE__)
int main(int argc, char** argv) {
    using namespace easyknack;
    using namespace easyknack::platform;
    try {
        KeyboardMidi midi;
        midi.prepare({1}); midi.prepareDrivers({1}, false); midi.setRunning(true);
        constexpr std::uint32_t note = 0x643c90;
        midi.keyboardEvent(note);
        std::this_thread::sleep_for(std::chrono::milliseconds(1));
        midi.beginBlock(256, 48000);
        require(midi.events(1)->count == 1 && midi.events(1)->events[0].message == note);
        auto events = midi.driverEvents();
        require(events.size() == 1 && events[0].first == 1 && events[0].second == note);
        midi.setRunning(false); midi.beginBlock(256, 48000);
        require(midi.events(1)->panic && midi.events(1)->count == 0);
        bool rejected = false;
        try { midi.prepare({42}); } catch (const std::runtime_error&) { rejected = true; }
        require(rejected);

        auto engine = std::make_unique<AudioEngine>();
        {
            auto services = createHostServices(*engine, true);
            for (int i=0; i<200 && engine->callbacks.load()==0; ++i)
                std::this_thread::sleep_for(std::chrono::milliseconds(10));
            require(engine->callbacks.load() > 0);
            auto& audio = *services.audio;
            require(audio.enumerateOutputs().at(0).id == "default");
            require(!audio.capabilities().systemCapture);
            require(!services.volume->get("default").available);
            auto generation = audio.generation(); rejected = false;
            try { audio.prepare({{"system", true}}); } catch (const std::runtime_error&) { rejected=true; }
            require(rejected && audio.generation()==generation);
            audio.prepare({{"in:default", true}, {"default", false}});
            std::this_thread::sleep_for(std::chrono::milliseconds(30));
            require(audio.statistics().playedFrames > 0);
        }
        auto stopped = engine->callbacks.load();
        std::this_thread::sleep_for(std::chrono::milliseconds(30));
        require(engine->callbacks.load() == stopped);
        if (argc != 2) throw std::runtime_error("Expected shared library fixture");
        SharedLibrary library;
        require(library.open(argv[1]));
        auto function = library.symbol<int(*)()>("easyknack_platform_fixture");
        require(function && function()==42);
        require(!library.symbol<int(*)()>("missing_symbol"));
        library.close();
        require(!library.open("/missing/easyknack-library"));
        std::cout << "Portable platform tests passed\n";
        return 0;
    } catch (const std::exception& error) { std::cerr << error.what() << '\n'; return 1; }
}
