#include "CommandReader.hpp"
#include <algorithm>
#include <cassert>
#include <cstring>
#include <iostream>
using namespace easyknack;
struct Input {
    std::string data;std::size_t offset=0,chunk=8192,calls=0;
    int read(char* out,std::size_t size){
        ++calls;auto count=std::min({size,chunk,data.size()-offset});
        std::memcpy(out,data.data()+offset,count);offset+=count;return static_cast<int>(count);
    }
};
int main(){
    for(std::size_t chunk:{1u,3u,8192u}){
        Input input{"HELLO 11 token\r\n"+std::string(200000,'x')+"\nPING\n\n",0,chunk};
        CommandReader reader;auto receive=[&](char* p,std::size_t n){return input.read(p,n);};
        assert(reader.read(receive)=="HELLO 11 token");assert(reader.read(receive)==std::string(200000,'x'));
        assert(reader.read(receive)=="PING");assert(reader.read(receive).empty());
        if(chunk==8192)assert(input.calls<30);
        try{reader.read(receive);assert(false);}catch(const std::runtime_error&){}
    }
    Input input{"12345678\n123456789012345\nPING\n"};CommandReader reader(8);
    auto receive=[&](char* p,std::size_t n){return input.read(p,n);};
    assert(reader.read(receive)=="12345678");
    try{reader.read(receive);assert(false);}catch(const std::runtime_error&){}
    assert(reader.read(receive)=="PING");
    std::cout<<"Large commands, fragmented/coalesced reads, bounds and recovery passed\n";
}
