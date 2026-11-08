#pragma once
#include <windows.h>
#include "BridgeProtocol.hpp"

namespace easyknack::driver {
// Synchronous development transport. Use on an IO worker, never the DSP callback.
// Opening requires elevation until a dedicated broker service is introduced.
class WindowsDriverBridge {
    HANDLE handle_ = INVALID_HANDLE_VALUE;
    DWORD error_ = ERROR_SUCCESS;
public:
    WindowsDriverBridge() = default;
    WindowsDriverBridge(const WindowsDriverBridge&) = delete;
    WindowsDriverBridge& operator=(const WindowsDriverBridge&) = delete;
    ~WindowsDriverBridge() { close(); }
    void close() {
        if (handle_ != INVALID_HANDLE_VALUE) CloseHandle(handle_);
        handle_ = INVALID_HANDLE_VALUE;
    }
    bool open() {
        close();
        handle_ = CreateFileW(L"\\\\.\\EasyKnackAudioBridge", GENERIC_READ | GENERIC_WRITE,
            0, nullptr, OPEN_EXISTING, 0, nullptr);
        error_ = handle_ == INVALID_HANDLE_VALUE ? GetLastError() : ERROR_SUCCESS;
        return handle_ != INVALID_HANDLE_VALUE;
    }
    DWORD error() const { return error_; }
    bool exchange(Packet& packet) {
        if (handle_ == INVALID_HANDLE_VALUE) { error_ = ERROR_INVALID_HANDLE; return false; }
        if (!validRequest(packet)) { error_ = ERROR_INVALID_PARAMETER; return false; }
        DWORD returned = 0;
        if (!DeviceIoControl(handle_, ExchangeIoctl, &packet, sizeof(packet),
            &packet, sizeof(packet), &returned, nullptr)) {
            error_ = GetLastError(); close(); return false;
        }
        if (returned != sizeof(packet) || !validRequest(packet) ||
            packet.written > packet.writeFrames || packet.read > packet.readFrames ||
            (packet.flags & ~(RenderRunning | CaptureRunning))) {
            error_ = ERROR_INVALID_DATA; close(); return false;
        }
        error_ = ERROR_SUCCESS;
        return true;
    }
};
}
