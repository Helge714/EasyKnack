#include "KernelBridge.hpp"
#include <wdmsec.h>
#include "PcmRing.hpp"

using namespace easyknack::driver;
namespace {
KSPIN_LOCK lock;
PDEVICE_OBJECT control;
PDRIVER_DISPATCH previous[IRP_MJ_MAXIMUM_FUNCTION + 1];
PFILE_OBJECT client;
void* streams[2];
bool running[2];
bool protectedContent[2];
bool expired;
ULONGLONG heartbeat;
PcmRing renderRing, captureRing;
constexpr ULONGLONG Lease = 2 * 10000000ull;
const GUID ControlClass = {0xb062b347, 0x46d3, 0x4a39, {0xb5, 0x78, 0x60, 0xd2, 0x34, 0x36, 0x96, 0xc1}};
UNICODE_STRING linkName = RTL_CONSTANT_STRING(L"\\DosDevices\\EasyKnackAudioBridge");
bool alive() {
    if (client && !expired && KeQueryInterruptTime() - heartbeat > Lease) {
        expired = true;
        renderRing.reset(); captureRing.reset();
    }
    return client && !expired;
}
NTSTATUS complete(PIRP irp, NTSTATUS status, ULONG_PTR bytes = 0) {
    irp->IoStatus.Status = status;
    irp->IoStatus.Information = bytes;
    IoCompleteRequest(irp, IO_NO_INCREMENT);
    return status;
}
_Dispatch_type_(IRP_MJ_CREATE)
_Dispatch_type_(IRP_MJ_CLOSE)
_Dispatch_type_(IRP_MJ_CLEANUP)
_Dispatch_type_(IRP_MJ_DEVICE_CONTROL)
DRIVER_DISPATCH dispatch;
NTSTATUS dispatch(PDEVICE_OBJECT device, PIRP irp) {
    auto stack = IoGetCurrentIrpStackLocation(irp);
    if (device != control) return previous[stack->MajorFunction](device, irp);
    if (KeGetCurrentIrql() > APC_LEVEL) return complete(irp, STATUS_INVALID_DEVICE_STATE);
    NTSTATUS result = STATUS_INVALID_DEVICE_REQUEST;
    ULONG_PTR returned = 0;
    KIRQL old;
    KeAcquireSpinLock(&lock, &old);
    switch (stack->MajorFunction) {
    case IRP_MJ_CREATE:
        if (stack->FileObject->FileName.Length) { result = STATUS_OBJECT_NAME_NOT_FOUND; break; }
        if (client) { result = STATUS_SHARING_VIOLATION; break; }
        client = stack->FileObject;
        expired = false;
        heartbeat = KeQueryInterruptTime();
        renderRing.reset(); captureRing.reset();
        result = STATUS_SUCCESS;
        break;
    case IRP_MJ_CLEANUP:
        if (client == stack->FileObject) {
            client = nullptr;
            renderRing.reset(); captureRing.reset();
        }
        result = STATUS_SUCCESS;
        break;
    case IRP_MJ_CLOSE:
        result = STATUS_SUCCESS;
        break;
    case IRP_MJ_DEVICE_CONTROL:
        if (stack->Parameters.DeviceIoControl.IoControlCode != ExchangeIoctl) break;
        if (stack->Parameters.DeviceIoControl.InputBufferLength != sizeof(Packet) ||
            stack->Parameters.DeviceIoControl.OutputBufferLength != sizeof(Packet) ||
            !irp->AssociatedIrp.SystemBuffer) { result = STATUS_INVALID_BUFFER_SIZE; break; }
        if (client != stack->FileObject || !alive()) { result = STATUS_DEVICE_NOT_CONNECTED; break; }
        {
            auto packet = static_cast<Packet*>(irp->AssociatedIrp.SystemBuffer);
            if (!validRequest(*packet)) { result = STATUS_INVALID_PARAMETER; break; }
            heartbeat = KeQueryInterruptTime();
            packet->written = running[1] ? captureRing.write(packet->toCapture, packet->writeFrames) : 0;
            RtlZeroMemory(packet->fromRender, sizeof(packet->fromRender));
            packet->read = renderRing.read(packet->fromRender, packet->readFrames);
            packet->flags = (running[0] ? RenderRunning : 0) | (running[1] ? CaptureRunning : 0);
            returned = sizeof(Packet);
            result = STATUS_SUCCESS;
        }
        break;
    }
    KeReleaseSpinLock(&lock, old);
    return complete(irp, result, returned);
}
}

NTSTATUS EasyKnackBridgeInitialize(PDRIVER_OBJECT driver) {
    KeInitializeSpinLock(&lock);
    UNICODE_STRING name = RTL_CONSTANT_STRING(L"\\Device\\EasyKnackAudioBridge");
    // Development control channel: SYSTEM or elevated administrator only.
    auto status = IoCreateDeviceSecure(driver, 0, &name, DeviceType, FILE_DEVICE_SECURE_OPEN,
        TRUE, &SDDL_DEVOBJ_SYS_ALL_ADM_ALL, &ControlClass, &control);
    if (!NT_SUCCESS(status)) return status;
    status = IoCreateSymbolicLink(&linkName, &name);
    if (!NT_SUCCESS(status)) { IoDeleteDevice(control); control = nullptr; return status; }
    for (ULONG i = 0; i <= IRP_MJ_MAXIMUM_FUNCTION; ++i) {
        previous[i] = driver->MajorFunction[i];
        driver->MajorFunction[i] = dispatch;
    }
    control->Flags &= ~DO_DEVICE_INITIALIZING;
    return STATUS_SUCCESS;
}
void EasyKnackBridgeShutdown() {
    if (!control) return;
    auto driver = control->DriverObject;
    for (ULONG i = 0; i <= IRP_MJ_MAXIMUM_FUNCTION; ++i) driver->MajorFunction[i] = previous[i];
    IoDeleteSymbolicLink(&linkName);
    IoDeleteDevice(control);
    control = nullptr;
}
bool EasyKnackBridgeClaim(void* stream, bool capture) {
    KIRQL old; KeAcquireSpinLock(&lock, &old);
    const auto index = capture ? 1 : 0;
    const bool available = !streams[index];
    if (available) { streams[index] = stream; protectedContent[index] = false; }
    KeReleaseSpinLock(&lock, old);
    return available;
}
void EasyKnackBridgeRelease(void* stream) {
    KIRQL old; KeAcquireSpinLock(&lock, &old);
    for (unsigned i = 0; i < 2; ++i) if (streams[i] == stream) {
        streams[i] = nullptr; running[i] = false;
        (i ? captureRing : renderRing).reset();
    }
    KeReleaseSpinLock(&lock, old);
}
void EasyKnackBridgeRunning(void* stream, bool enabled) {
    KIRQL old; KeAcquireSpinLock(&lock, &old);
    for (unsigned i = 0; i < 2; ++i) if (streams[i] == stream && running[i] != enabled) {
        running[i] = enabled;
        (i ? captureRing : renderRing).reset();
    }
    KeReleaseSpinLock(&lock, old);
}
void EasyKnackBridgeProtect(void* stream, bool enabled) {
    KIRQL old; KeAcquireSpinLock(&lock, &old);
    for (unsigned i = 0; i < 2; ++i) if (streams[i] == stream) {
        protectedContent[i] = enabled;
        if (enabled) (i ? captureRing : renderRing).reset();
    }
    KeReleaseSpinLock(&lock, old);
}
void EasyKnackBridgeAudio(void* stream, bool capture, void* bytes, ULONG size) {
    auto data = static_cast<int16_t*>(bytes);
    // Callers enforce PCM16 stereo and whole frames. Never parse a user pointer.
    if (size % FrameBytes) { if (capture) RtlZeroMemory(bytes, size); return; }
    auto frames = size / FrameBytes;
    while (frames) {
        const auto count = frames < PacketFrames ? frames : PacketFrames;
        KIRQL old; KeAcquireSpinLock(&lock, &old);
        const auto index = capture ? 1 : 0;
        if (streams[index] == stream && running[index] && !protectedContent[index] && alive()) {
            if (capture) captureRing.read(data, count);
            else renderRing.write(data, count);
        } else if (capture) RtlZeroMemory(data, count * FrameBytes);
        KeReleaseSpinLock(&lock, old);
        frames -= count; data += count * Channels;
    }
}
