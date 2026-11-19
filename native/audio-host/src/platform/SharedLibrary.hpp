#pragma once
#include <filesystem>
#include <string>
#ifdef _WIN32
#ifndef NOMINMAX
#define NOMINMAX
#endif
#include <windows.h>
#else
#include <dlfcn.h>
#endif
namespace easyknack::platform {
/** Control-thread module ownership; no OS loader calls from DSP evaluation. */
class SharedLibrary {
#ifdef _WIN32
    HMODULE handle=nullptr;
#else
    void* handle=nullptr;
#endif
public:
    SharedLibrary()=default;SharedLibrary(const SharedLibrary&)=delete;SharedLibrary& operator=(const SharedLibrary&)=delete;
    ~SharedLibrary(){close();}
    void close()noexcept{if(!handle)return;
#ifdef _WIN32
        FreeLibrary(handle);
#else
        dlclose(handle);
#endif
        handle=nullptr;
    }
    bool open(const std::string& path){close();
#ifdef _WIN32
        int length=MultiByteToWideChar(CP_UTF8,MB_ERR_INVALID_CHARS,path.data(),int(path.size()),nullptr,0);
        if(length<=0)return false;std::wstring wide(length,L'\0');MultiByteToWideChar(CP_UTF8,MB_ERR_INVALID_CHARS,path.data(),int(path.size()),wide.data(),length);
        handle=LoadLibraryW(wide.c_str());
#else
        handle=dlopen(path.c_str(),RTLD_NOW|RTLD_LOCAL);
#endif
        return handle!=nullptr;
    }
    template<class T> T symbol(const char* name)const noexcept{if(!handle)return nullptr;
#ifdef _WIN32
        return reinterpret_cast<T>(GetProcAddress(handle,name));
#else
        return reinterpret_cast<T>(dlsym(handle,name));
#endif
    }
};
}
