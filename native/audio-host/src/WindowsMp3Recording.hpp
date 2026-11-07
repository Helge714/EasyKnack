#pragma once
#include <filesystem>
#include <algorithm>
#include <cstdint>
#include <vector>
#include <cmath>
#ifdef _WIN32
// Define the SDK GUIDs locally; operating-system entry points are loaded on demand.
// This keeps numeric/source-plugin consumers independent of Media Foundation libraries.
#pragma push_macro("DEFINE_GUID")
#include <initguid.h>
#include <mfapi.h>
#include <mfidl.h>
#include <mferror.h>
#include <mfreadwrite.h>
#pragma pop_macro("DEFINE_GUID")
#endif
namespace easyknack {
/** Windows media encoder, used exclusively on the recording worker thread. */
class WindowsMp3Recording {
#ifdef _WIN32
    struct Api {
        HMODULE platform=LoadLibraryW(L"mfplat.dll"),readwrite=LoadLibraryW(L"mfreadwrite.dll"),foundation=LoadLibraryW(L"mf.dll"),ole=LoadLibraryW(L"ole32.dll");
        template<class T> static T load(HMODULE module,const char* name){return module?reinterpret_cast<T>(GetProcAddress(module,name)):nullptr;}
        decltype(&MFStartup) startup=load<decltype(&MFStartup)>(platform,"MFStartup");
        decltype(&MFShutdown) shutdown=load<decltype(&MFShutdown)>(platform,"MFShutdown");
        decltype(&MFCreateFile) file=load<decltype(&MFCreateFile)>(platform,"MFCreateFile");
        decltype(&MFCreateMediaType) type=load<decltype(&MFCreateMediaType)>(platform,"MFCreateMediaType");
        decltype(&MFCreateSample) sample=load<decltype(&MFCreateSample)>(platform,"MFCreateSample");
        decltype(&MFCreateMemoryBuffer) buffer=load<decltype(&MFCreateMemoryBuffer)>(platform,"MFCreateMemoryBuffer");
        decltype(&MFCreateSinkWriterFromURL) writer=load<decltype(&MFCreateSinkWriterFromURL)>(readwrite,"MFCreateSinkWriterFromURL");
        decltype(&MFTranscodeGetAudioOutputAvailableTypes) types=load<decltype(&MFTranscodeGetAudioOutputAvailableTypes)>(foundation,"MFTranscodeGetAudioOutputAvailableTypes");
        decltype(&CoInitializeEx) initialize=load<decltype(&CoInitializeEx)>(ole,"CoInitializeEx");
        decltype(&CoUninitialize) uninitialize=load<decltype(&CoUninitialize)>(ole,"CoUninitialize");
        bool available()const{return startup&&shutdown&&file&&type&&sample&&buffer&&writer&&types&&initialize&&uninitialize;}
    };
    static Api& api(){static Api value;return value;}
    IMFByteStream* stream=nullptr;IMFSinkWriter* sink=nullptr;
    bool com=false,media=false,begun=false;
    DWORD index=0;unsigned channels=0,rate=0;std::uint64_t frames=0;
    static void format(IMFMediaType* type,const GUID& subtype,unsigned channels,unsigned rate,unsigned bytes,unsigned alignment){
        type->SetGUID(MF_MT_MAJOR_TYPE,MFMediaType_Audio);type->SetGUID(MF_MT_SUBTYPE,subtype);
        type->SetUINT32(MF_MT_AUDIO_NUM_CHANNELS,channels);type->SetUINT32(MF_MT_AUDIO_SAMPLES_PER_SECOND,rate);
        type->SetUINT32(MF_MT_AUDIO_AVG_BYTES_PER_SECOND,bytes);type->SetUINT32(MF_MT_AUDIO_BLOCK_ALIGNMENT,alignment);type->SetUINT32(MF_MT_AUDIO_BITS_PER_SAMPLE,16);
    }
#endif
public:
    long status=0;
    bool exists=false,codecFailure=false;
    bool open(const std::filesystem::path& path,bool overwrite,unsigned ch,unsigned sampleRate){
#ifdef _WIN32
        auto& a=api();if(!a.available()){codecFailure=true;return false;}
        auto hr=a.initialize(nullptr,COINIT_MULTITHREADED);com=SUCCEEDED(hr);if(FAILED(hr)){codecFailure=true;return false;}
        hr=a.startup(MF_VERSION,MFSTARTUP_FULL);media=SUCCEEDED(hr);if(FAILED(hr)){codecFailure=true;return false;}
        hr=a.file(MF_ACCESSMODE_READWRITE,overwrite?MF_OPENMODE_DELETE_IF_EXIST:MF_OPENMODE_FAIL_IF_EXIST,MF_FILEFLAGS_NONE,path.c_str(),&stream);
        if(FAILED(hr)){status=hr;std::error_code ec;exists=!overwrite&&std::filesystem::exists(path,ec);return false;}
        hr=a.writer(path.c_str(),stream,nullptr,&sink);IMFMediaType *output=nullptr,*input=nullptr;
        IMFCollection* types=nullptr;
        if(SUCCEEDED(hr))hr=a.types(MFAudioFormat_MP3,MFT_ENUM_FLAG_ALL,nullptr,&types);
        if(SUCCEEDED(hr)){
            DWORD count=0;types->GetElementCount(&count);unsigned best=~0u;
            for(DWORD i=0;i<count;i++){IUnknown* item=nullptr;IMFMediaType* candidate=nullptr;
                if(SUCCEEDED(types->GetElement(i,&item))){item->QueryInterface(__uuidof(IMFMediaType),reinterpret_cast<void**>(&candidate));item->Release();}
                if(!candidate)continue;UINT32 channels=0,rate=0,bytes=0;candidate->GetUINT32(MF_MT_AUDIO_NUM_CHANNELS,&channels);candidate->GetUINT32(MF_MT_AUDIO_SAMPLES_PER_SECOND,&rate);candidate->GetUINT32(MF_MT_AUDIO_AVG_BYTES_PER_SECOND,&bytes);
                unsigned score=static_cast<unsigned>(std::abs(static_cast<int>(bytes)-static_cast<int>(ch==1?16000:24000)));
                if(channels==ch&&rate==sampleRate&&score<best){if(output)output->Release();output=candidate;candidate=nullptr;best=score;}
                if(candidate)candidate->Release();
            }
            hr=output?sink->AddStream(output,&index):MF_E_INVALIDMEDIATYPE;
        }
        if(types)types->Release();
        if(SUCCEEDED(hr))hr=a.type(&input);
        if(SUCCEEDED(hr)){format(input,MFAudioFormat_PCM,ch,sampleRate,sampleRate*ch*2,ch*2);hr=sink->SetInputMediaType(index,input,nullptr);}
        if(output)output->Release();if(input)input->Release();
        if(SUCCEEDED(hr))hr=sink->BeginWriting();
        status=hr;begun=SUCCEEDED(hr);channels=ch;rate=sampleRate;codecFailure=!begun;return begun;
#else
        codecFailure=true;return false;
#endif
    }
    bool write(const float* values,std::size_t count){
#ifdef _WIN32
        if(!begun)return false;IMFSample* sample=nullptr;IMFMediaBuffer* buffer=nullptr;
        auto& a=api();auto hr=a.sample(&sample);if(SUCCEEDED(hr))hr=a.buffer(static_cast<DWORD>(count*2),&buffer);
        BYTE* bytes=nullptr;if(SUCCEEDED(hr))hr=buffer->Lock(&bytes,nullptr,nullptr);
        if(SUCCEEDED(hr)){
            auto* data=reinterpret_cast<short*>(bytes);for(std::size_t i=0;i<count;i++)data[i]=static_cast<short>(std::lround((std::max)(-1.f,(std::min)(1.f,values[i]))*32767));
            buffer->Unlock();buffer->SetCurrentLength(static_cast<DWORD>(count*2));sample->AddBuffer(buffer);
            auto next=frames+count/channels;sample->SetSampleTime(static_cast<LONGLONG>(frames*10000000/rate));sample->SetSampleDuration(static_cast<LONGLONG>(next*10000000/rate-frames*10000000/rate));
            hr=sink->WriteSample(index,sample);if(SUCCEEDED(hr))frames=next;
        }
        if(buffer)buffer->Release();if(sample)sample->Release();return SUCCEEDED(hr);
#else
        return false;
#endif
    }
    bool finish(){
#ifdef _WIN32
        bool ok=true;if(begun){
            // The Windows encoder retains its final analysis/overlap samples.
            // Feed a silent tail so a short take does not lose its last sound.
            std::vector<float> tail(1152*channels,0.f);for(int i=0;i<3;i++)ok=write(tail.data(),tail.size())&&ok;
            ok=SUCCEEDED(sink->Finalize())&&ok;begun=false;
        }return ok;
#else
        return false;
#endif
    }
    ~WindowsMp3Recording(){
#ifdef _WIN32
        finish();if(sink)sink->Release();if(stream){stream->Close();stream->Release();}if(media)api().shutdown();if(com)api().uninitialize();
#endif
    }
};
}
