#pragma once
#ifdef _WIN32
#ifndef NOMINMAX
#define NOMINMAX
#endif
#include <windows.h>
#endif
namespace easyknack::platform {
inline void prioritizeAudioWorker()noexcept{
#ifdef _WIN32
    SetThreadPriority(GetCurrentThread(),THREAD_PRIORITY_HIGHEST);
#endif
    // POSIX real-time policy needs an explicit platform/permission policy.
}
}
