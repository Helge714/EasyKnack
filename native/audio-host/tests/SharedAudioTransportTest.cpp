#include "../src/audio/transport/WindowsSharedAudio.hpp"
#include <array>
#include <iostream>
#include <limits>

using namespace easyknack::transport;
void check(bool condition, const char* message) { if (!condition) throw std::runtime_error(message); }
template<class F> void rejects(F action) {
    bool failed = false; try { action(); } catch (const std::exception&) { failed = true; }
    check(failed, "Invalid transport accepted");
}
std::uint64_t simulatedTime = 100;
std::uint64_t testClock() { return simulatedTime; }
struct Child {
    PROCESS_INFORMATION process{};
    explicit Child(const std::wstring& command) {
        STARTUPINFOW startup{}; startup.cb = sizeof(startup);
        std::wstring writable = command;
        if (!CreateProcessW(nullptr, writable.data(), nullptr, nullptr, FALSE, CREATE_NO_WINDOW, nullptr, nullptr, &startup, &process))
            throw std::runtime_error("Cannot start transport peer");
    }
    ~Child() {
        if (WaitForSingleObject(process.hProcess, 0) == WAIT_TIMEOUT) { TerminateProcess(process.hProcess, 2); WaitForSingleObject(process.hProcess, 2000); }
        CloseHandle(process.hThread); CloseHandle(process.hProcess);
    }
    void join() {
        check(WaitForSingleObject(process.hProcess, 5000) == WAIT_OBJECT_0, "Transport peer timed out");
        DWORD code = 1; GetExitCodeProcess(process.hProcess, &code); check(code == 0, "Transport peer failed");
    }
};
float value(std::size_t frame, unsigned channel) { return static_cast<float>(static_cast<int>((frame*2+channel)%127)-63)/128.f; }
int peer(const std::wstring& name, SessionId id, bool crash) {
    auto mapping = WindowsSharedAudio::open(name, id);
    SharedAudioClient client(*mapping, Side::Device);
    auto deadline = uptime()+10000;
    if (crash) {
        // Publish readiness, then exit without destructors after a handshake frame.
        float frame[2]{};
        while (uptime()<deadline) {
            if (client.read(frame, 1).frames) { ExitProcess(0); }
            Sleep(1);
        }
        return 4;
    }
    std::array<float, 67*2> block{};
    std::size_t received = 0, pending = 0, offset = 0;
    while (received < 100000 || pending) {
        check(uptime()<deadline, "Transport echo timed out");
        if (!pending) {
            auto read = client.read(block.data(), std::min<std::size_t>(67, 100000-received));
            pending = read.frames; offset = 0; received += pending;
            for (std::size_t i=0; i<pending*2; ++i) block[i] *= 2;
        }
        if (pending) { auto sent = client.write(block.data()+offset*2, pending).frames; offset += sent; pending -= sent; }
        if (!pending && received == 100000) break;
        Sleep(0);
    }
    // Leave the final packet readable until the other process acknowledges its consumption.
    while (load(mapping->region().rings[0].consumer.position) < 100000) { check(uptime()<deadline, "Missing transport acknowledgement"); client.connected(); Sleep(1); }
    return 0;
}
int wmain(int argc, wchar_t** argv) {
    try {
        if (argc == 6 && std::wstring(argv[1]) == L"--peer")
            return peer(argv[2], {std::stoull(argv[3]), std::stoull(argv[4])}, std::wstring(argv[5]) == L"crash");
        rejects([] { WindowsSharedAudio::create({48000, 0, 64}); });
        rejects([] { WindowsSharedAudio::create({48000, 2, 65}); });
        auto mapping = WindowsSharedAudio::create({48000, 2, 64});
        auto id = mapping->session();
        rejects([&] { WindowsSharedAudio::open(mapping->name(), {id.low+1, id.high}); });
        mapping->region().header.version++;
        rejects([&] { WindowsSharedAudio::open(mapping->name(), id); });
        mapping->region().header.version = abiVersion;
        auto other = WindowsSharedAudio::open(mapping->name(), id);
        SharedAudioClient engine(*mapping, Side::Engine, testClock), device(*other, Side::Device, testClock);
        rejects([&] { SharedAudioClient duplicate(*mapping, Side::Engine); });
        std::array<float, 160> samples{}, output{};
        for (std::size_t i=0; i<samples.size(); ++i) samples[i] = static_cast<float>(i);
        check(engine.write(samples.data(), 80).frames == 64, "Overflow not bounded");
        check(load(mapping->region().rings[1].producer.xruns) == 1, "Overflow not counted");
        check(device.read(output.data(), 31).frames == 31 && output[60] == 60, "Incorrect initial frames");
        check(engine.write(samples.data(), 31).frames == 31, "Wrapped write failed");
        check(device.read(output.data(), 64).frames == 64 && output[0] == 62 && output[66] == 0 && output[126] == 60, "Wrapped frames corrupted");
        check(device.read(output.data(), 3).status == TransferStatus::Underrun && output[0] == 0, "Underrun is not silence");
        store(mapping->region().rings[1].producer.position, 99999);
        check(device.read(output.data(), 3).status == TransferStatus::InvalidCursor && output[0] == 0, "Invalid cursor not rejected");
        // Simulated process death: stale frames must not be played after the lease expires.
        simulatedTime += 2001;
        check(engine.read(output.data(), 3).status == TransferStatus::PeerUnavailable && output[0] == 0, "Expired peer not silent");
        check(!device.connected(), "Expired session resurrected");
        device.close(); engine.close();

        // The survivor keeps its mapping alive after creator unmaps/closes its handles.
        auto owner = WindowsSharedAudio::create({48000, 1, 64});
        auto survivor = WindowsSharedAudio::open(owner->name(), owner->session());
        { SharedAudioClient creator(*owner, Side::Engine); }
        owner.reset();
        SharedAudioClient remaining(*survivor, Side::Device);
        check(remaining.read(output.data(), 1).status == TransferStatus::PeerUnavailable, "Closed owner not detected");

        wchar_t executable[32768]; check(GetModuleFileNameW(nullptr, executable, 32768) != 0, "No executable path");
        for (bool crash : {false, true}) {
            auto shared = WindowsSharedAudio::create({48000, 2, 256});
            SharedAudioClient endpoint(*shared, Side::Engine);
            auto session = shared->session();
            Child child(L"\"" + std::wstring(executable) + L"\" --peer \"" + shared->name() + L"\" "
                    + std::to_wstring(session.low) + L" " + std::to_wstring(session.high) + (crash ? L" crash" : L" echo"));
            auto deadline = uptime()+12000;
            if (crash) {
                while (!endpoint.write(samples.data(), 1).frames) { check(uptime()<deadline, "Peer did not connect"); Sleep(1); }
                child.join(); Sleep(2100);
                check(endpoint.read(output.data(), 4).status == TransferStatus::PeerUnavailable && output[0] == 0, "Killed peer not detected");
            } else {
                std::size_t sent = 0, received = 0;
                while (received < 100000) {
                    check(uptime()<deadline, "Parent transport timed out");
                    auto count = std::min<std::size_t>(31, 100000-sent);
                    for (std::size_t i=0; i<count; ++i) for (unsigned c=0; c<2; ++c) samples[i*2+c] = value(sent+i, c);
                    sent += endpoint.write(samples.data(), count).frames;
                    auto read = endpoint.read(output.data(), 29);
                    for (std::size_t i=0; i<read.frames; ++i) for (unsigned c=0; c<2; ++c)
                        check(output[i*2+c] == value(received+i, c)*2, "Cross-process sample corruption");
                    received += read.frames;
                    Sleep(0);
                }
                child.join();
            }
        }
        std::cout << "Shared audio transport tests passed (including two-process transfer and crash)\n";
        return 0;
    } catch (const std::exception& failure) { std::cerr << failure.what() << '\n'; return 1; }
}
