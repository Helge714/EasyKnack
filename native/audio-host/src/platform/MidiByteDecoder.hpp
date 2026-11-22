#pragma once
#include <cstddef>
#include <cstdint>
namespace easyknack::platform {
/** MIDI 1 byte stream, including running status and interleaved real-time events. */
class MidiByteDecoder {
    std::uint8_t status=0,data[2]{};unsigned used=0,needed=0;bool sysex=false;
public:
    template<class Emit> void read(const std::uint8_t* bytes,std::size_t size,Emit emit)noexcept{
        for(std::size_t i=0;i<size;i++){
            auto b=bytes[i];
            if(b>=0xf8){emit(b);continue;}
            if(b&0x80){used=0;
                if(b==0xf0){sysex=true;status=0;continue;}if(b==0xf7){sysex=false;status=0;continue;}
                sysex=false;status=b;needed=(b<0xf0?((b&0xe0)==0xc0?1:2):b==0xf1||b==0xf3?1:b==0xf2?2:0);
                if(!needed){emit(b);status=0;}continue;
            }
            if(sysex||!status)continue;data[used++]=b;
            if(used==needed){emit(std::uint32_t(status)|(std::uint32_t(data[0])<<8)|(needed==2?std::uint32_t(data[1])<<16:0));used=0;if(status>=0xf0)status=0;}
        }
    }
};
}
