#pragma once
#if defined(_WIN32) && !defined(EASYKNACK_PORTABLE_HOST)
#include "WindowsHostServices.hpp"
#else
#include "DefaultAudio.hpp"
#include "KeyboardMidi.hpp"
#if defined(__linux__)
#include "LinuxMidi.hpp"
#include "LinuxAudio.hpp"
#include "LinuxVolume.hpp"
#include <iostream>
#endif
namespace easyknack::platform {
inline HostServices createHostServices(AudioEngine& engine,bool nullDevice){
    HostServices result;
#if defined(__linux__)
    result.midi=std::make_unique<LinuxMidi>(!nullDevice);
#else
    result.midi=std::make_unique<KeyboardMidi>();
#endif
    result.volume=std::make_unique<UnavailableVolume>();
    engine.midiSource=result.midi.get();
#if defined(__linux__)
    if(!nullDevice){
        try{
            auto session=std::make_shared<PulseSession>();
            result.volume=std::make_unique<LinuxVolume>(session);
            result.audio=std::make_unique<LinuxAudio>(engine,session);return result;
        }catch(const std::exception& error){std::cerr<<"Linux desktop audio unavailable: "<<error.what()<<"; using default-device fallback\n";result.volume=std::make_unique<UnavailableVolume>();}
    }
#endif
    result.audio=std::make_unique<DefaultAudio>(engine,nullDevice);return result;
}
}
#endif
