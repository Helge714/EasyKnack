#include "../../experimental/virtual-audio/PcmRing.hpp"
#include <array>
#include <deque>
#include <iostream>
#include <random>
#include <stdexcept>

using namespace easyknack::driver;
void require(bool condition) { if (!condition) throw std::runtime_error("Driver bridge invariant failed"); }
int main() {
    Packet packet{};
    packet.version = Version;
    require(validRequest(packet));
    packet.writeFrames = PacketFrames + 1;
    require(!validRequest(packet));
    packet.writeFrames = 0; packet.readFrames = UINT32_MAX;
    require(!validRequest(packet));
    packet.readFrames = 0; packet.reserved[1] = 1;
    require(!validRequest(packet));
    packet.reserved[1] = 0; packet.version++;
    require(!validRequest(packet));

    PcmRing ring;
    std::deque<std::array<int16_t, 2>> expected;
    std::mt19937 random(721391);
    std::array<int16_t, PacketFrames * Channels + 2> input{}, output{};
    for (unsigned step = 0; step < 30000; ++step) {
        const auto frames = random() % (PacketFrames + 1);
        if (random() % 2) {
            for (unsigned i = 0; i < frames * Channels; ++i) input[i] = static_cast<int16_t>(random());
            const auto accepted = ring.write(input.data(), frames);
            require(accepted == std::min<size_t>(frames, PcmRing::Capacity - expected.size()));
            for (unsigned i = 0; i < accepted; ++i) expected.push_back({input[2*i], input[2*i+1]});
        } else {
            output.fill(12345);
            const auto available = ring.read(output.data() + 1, frames);
            require(available == std::min<size_t>(frames, expected.size()));
            require(output.front() == 12345 && output[frames * Channels + 1] == 12345);
            for (unsigned i = 0; i < frames; ++i) {
                const auto frame = i < available ? expected.front() : std::array<int16_t, 2>{0, 0};
                if (i < available) expected.pop_front();
                require(output[1+2*i] == frame[0] && output[2+2*i] == frame[1]);
            }
        }
        require(ring.count == expected.size());
        if (step % 173 == 0) { ring.reset(); expected.clear(); }
    }
    // A reconnect cannot replay samples retained in ring storage.
    ring.reset(); output.fill(12345);
    require(ring.read(output.data(), PacketFrames) == 0);
    for (unsigned i = 0; i < PacketFrames * Channels; ++i) require(output[i] == 0);
    std::cout << "Driver bridge protocol and PCM ring tests passed\n";
}
