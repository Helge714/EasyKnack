#pragma once
#if defined(_WIN32) && !defined(EASYKNACK_PORTABLE_HOST)
#include "WindowsHostServices.hpp"
#else
#include "DefaultAudio.hpp"
#include "KeyboardMidi.hpp"
namespace easyknack::platform {
inline HostServices createHostServices(AudioEngine& engine,bool nullDevice){
    HostServices result;result.midi=std::make_unique<KeyboardMidi>();
    result.volume=std::make_unique<UnavailableVolume>();
    engine.midiSource=result.midi.get();
    result.audio=std::make_unique<DefaultAudio>(engine,nullDevice);return result;
}
}
#endif
