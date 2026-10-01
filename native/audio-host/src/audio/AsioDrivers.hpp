#pragma once
#include <windows.h>
#include <objbase.h>
#include "asio/iasiodrv.h"
#include <functional>
#include <memory>
#include <string>
#include <vector>

namespace easyknack {
struct AsioRelease { void operator()(IASIO* p) const { if (p) p->Release(); } };
using AsioHandle = std::unique_ptr<IASIO, AsioRelease>;
struct AsioDriverEntry { std::string id, name; };
struct AsioDrivers {
    std::function<std::vector<AsioDriverEntry>()> enumerate;
    std::function<AsioHandle(const std::string&)> open;
};
inline std::string asioUtf8(const wchar_t* text) {
    int n=WideCharToMultiByte(CP_UTF8,0,text,-1,nullptr,0,nullptr,nullptr);
    if (n<=1) return {};
    std::string result(n,0); WideCharToMultiByte(CP_UTF8,0,text,-1,result.data(),n,nullptr,nullptr);
    result.pop_back(); return result;
}
inline AsioDrivers windowsAsioDrivers() {
    return {
        [] {
            std::vector<AsioDriverEntry> result;
            HKEY root=nullptr;
            // Only drivers matching this process's architecture are usable.
            if (RegOpenKeyExW(HKEY_LOCAL_MACHINE,L"SOFTWARE\\ASIO",0,KEY_READ,&root)!=ERROR_SUCCESS) return result;
            for (DWORD index=0;;++index) {
                wchar_t name[256]{}; DWORD size=256;
                auto error=RegEnumKeyExW(root,index,name,&size,nullptr,nullptr,nullptr,nullptr);
                if (error==ERROR_NO_MORE_ITEMS) break;
                if (error!=ERROR_SUCCESS) continue;
                HKEY key=nullptr;
                if (RegOpenKeyExW(root,name,0,KEY_READ,&key)!=ERROR_SUCCESS) continue;
                wchar_t clsid[128]{}, description[512]{}; DWORD bytes=sizeof(clsid);
                auto ok=RegGetValueW(key,nullptr,L"CLSID",RRF_RT_REG_SZ,nullptr,clsid,&bytes);
                bytes=sizeof(description);
                if (RegGetValueW(key,nullptr,L"Description",RRF_RT_REG_SZ,nullptr,description,&bytes)!=ERROR_SUCCESS) description[0]=0;
                RegCloseKey(key);
                CLSID guid{}; wchar_t canonical[40]{};
                if (ok!=ERROR_SUCCESS || FAILED(CLSIDFromString(clsid,&guid))) continue;
                StringFromGUID2(guid,canonical,40);
                auto id=asioUtf8(canonical);
                bool duplicate=false; for (const auto& old:result) if (old.id==id) duplicate=true;
                if (!duplicate) result.push_back({id,asioUtf8(description[0]?description:name)});
            }
            RegCloseKey(root); return result;
        },
        [](const std::string& id) {
            std::wstring wide(id.begin(),id.end()); CLSID guid{}; IASIO* driver=nullptr;
            if (FAILED(CLSIDFromString(wide.c_str(),&guid))) return AsioHandle{};
            // ASIO uses the driver's CLSID as its interface ID (SDK host/pc/asiolist.cpp).
            if (FAILED(CoCreateInstance(guid,nullptr,CLSCTX_INPROC_SERVER,guid,reinterpret_cast<void**>(&driver)))) return AsioHandle{};
            AsioHandle result(driver);
            if (!driver->init(GetDesktopWindow())) return AsioHandle{};
            return result;
        }
    };
}
}
