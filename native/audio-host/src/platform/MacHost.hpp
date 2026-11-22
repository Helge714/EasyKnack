#pragma once
#include "HostServices.hpp"
namespace easyknack::platform {
HostServices createMacHostServices(AudioEngine&,bool nullDevice);
std::unique_ptr<HostMidiSource> createMacMidiSource(bool hardware);
}
