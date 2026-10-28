#pragma once
#include <array>
#include <string>
#include <stdexcept>
#include <cstddef>

namespace easyknack {
/** Bounded command framing with buffered reads and recovery after oversized lines. */
class CommandReader {
    std::array<char,8192> buffer{};
    std::size_t position=0, available=0, maximum;
public:
    static constexpr std::size_t maximumBytes=64u*1024u*1024u;
    explicit CommandReader(std::size_t limit=maximumBytes):maximum(limit){}
    template<class Receive> std::string read(Receive receive){
        std::string line;
        bool oversized=false;
        for(;;){
            if(position==available){
                auto count=receive(buffer.data(),buffer.size());
                if(count<=0)throw std::runtime_error("connection closed or timed out");
                position=0;available=static_cast<std::size_t>(count);
            }
            char ch=buffer[position++];
            if(ch=='\n'){
                if(oversized)throw std::runtime_error("command exceeds 64 MiB transport limit");
                return line;
            }
            if(ch=='\r')continue;
            if(line.size()==maximum)oversized=true;
            if(!oversized)line+=ch;
        }
    }
};
}
