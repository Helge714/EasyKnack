#include "WindowsDriverBridge.hpp"
#include <iostream>

int main() {
    easyknack::driver::WindowsDriverBridge bridge;
    if (!bridge.open()) {
        std::cerr << "EasyKnack driver bridge unavailable (Windows error " << bridge.error()
                  << "). Driver must be installed; this development probe requires elevation.\n";
        return 1;
    }
    easyknack::driver::Packet packet{};
    packet.version = easyknack::driver::Version;
    if (!bridge.exchange(packet)) {
        std::cerr << "Bridge protocol check failed (Windows error " << bridge.error() << ").\n";
        return 2;
    }
    std::cout << "EasyKnack bridge ABI " << packet.version << ": 48000 Hz, stereo PCM16. "
              << "Render running: " << !!(packet.flags & easyknack::driver::RenderRunning)
              << ", capture running: " << !!(packet.flags & easyknack::driver::CaptureRunning) << '\n';
}
