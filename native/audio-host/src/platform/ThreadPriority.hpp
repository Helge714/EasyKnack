#pragma once
#ifdef _WIN32
#ifndef NOMINMAX
#define NOMINMAX
#endif
#include <windows.h>
#elif defined(__APPLE__)
#include <pthread/qos.h>
#endif
namespace easyknack::platform {
inline void prioritizeAudioWorker()noexcept{
#ifdef _WIN32
    SetThreadPriority(GetCurrentThread(),THREAD_PRIORITY_HIGHEST);
#elif defined(__APPLE__)
    pthread_set_qos_class_self_np(QOS_CLASS_USER_INTERACTIVE,0);
#endif
    // POSIX real-time policy needs an explicit platform/permission policy.
}
}
