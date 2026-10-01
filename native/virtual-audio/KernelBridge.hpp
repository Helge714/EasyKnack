#pragma once
#include <ntddk.h>
NTSTATUS EasyKnackBridgeInitialize(PDRIVER_OBJECT driver);
void EasyKnackBridgeShutdown();
bool EasyKnackBridgeClaim(void* stream, bool capture);
void EasyKnackBridgeRelease(void* stream);
void EasyKnackBridgeRunning(void* stream, bool running);
void EasyKnackBridgeProtect(void* stream, bool protectedContent);
void EasyKnackBridgeAudio(void* stream, bool capture, void* bytes, ULONG size);
