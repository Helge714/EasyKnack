#pragma once
#include <algorithm>
#include <cmath>
#include <cstdint>
#include <cstring>
#include <stdexcept>
#include "asio/asiosys.h"
#include "asio/asio.h"

namespace easyknack {
/** PCM only. No aliasing/alignment assumptions about driver-owned buffers. */
struct AsioSampleFormat {
    int bytes, bits;
    bool bigEndian, floating;
    static AsioSampleFormat from(ASIOSampleType type) {
        switch (type) {
        case ASIOSTInt16LSB: return {2,16,false,false};
        case ASIOSTInt24LSB: return {3,24,false,false};
        case ASIOSTInt32LSB: return {4,32,false,false};
        case ASIOSTFloat32LSB: return {4,32,false,true};
        case ASIOSTFloat64LSB: return {8,64,false,true};
        case ASIOSTInt16MSB: return {2,16,true,false};
        case ASIOSTInt24MSB: return {3,24,true,false};
        case ASIOSTInt32MSB: return {4,32,true,false};
        case ASIOSTFloat32MSB: return {4,32,true,true};
        case ASIOSTFloat64MSB: return {8,64,true,true};
        case ASIOSTInt32LSB16: return {4,16,false,false};
        case ASIOSTInt32LSB18: return {4,18,false,false};
        case ASIOSTInt32LSB20: return {4,20,false,false};
        case ASIOSTInt32LSB24: return {4,24,false,false};
        case ASIOSTInt32MSB16: return {4,16,true,false};
        case ASIOSTInt32MSB18: return {4,18,true,false};
        case ASIOSTInt32MSB20: return {4,20,true,false};
        case ASIOSTInt32MSB24: return {4,24,true,false};
        default: throw std::runtime_error("ASIO: unsupported sample format (PCM required)");
        }
    }
    float read(const void* data, std::size_t frame) const noexcept {
        auto p = static_cast<const unsigned char*>(data) + frame * bytes;
        std::uint64_t raw = 0;
        for (int i=0; i<bytes; ++i) raw |= std::uint64_t(p[i]) << (8*(bigEndian ? bytes-1-i : i));
        double value;
        if (floating) {
            if (bytes == 4) { auto v = static_cast<std::uint32_t>(raw); float f; std::memcpy(&f,&v,4); value=f; }
            else std::memcpy(&value,&raw,8);
        } else {
            auto mask = (std::uint64_t{1} << bits)-1;
            raw &= mask;
            auto v = (raw & (std::uint64_t{1} << (bits-1))) ? static_cast<std::int64_t>(raw) - static_cast<std::int64_t>(mask+1) : static_cast<std::int64_t>(raw);
            value = double(v) / double(std::uint64_t{1} << (bits-1));
        }
        float result=static_cast<float>(value);
        return std::isfinite(result) ? result : 0.f;
    }
    void write(void* data, std::size_t frame, float input) const noexcept {
        float value = std::isfinite(input) ? std::clamp(input,-1.f,1.f) : 0.f;
        std::uint64_t raw = 0;
        if (floating) {
            if (bytes == 4) { std::uint32_t v; std::memcpy(&v,&value,4); raw=v; }
            else { double v=value; std::memcpy(&raw,&v,8); }
        } else {
            double scale = double(std::uint64_t{1} << (bits-1));
            auto v = static_cast<std::int64_t>(std::clamp(std::round(double(value)*scale), -scale, scale-1));
            raw = static_cast<std::uint64_t>(v);
        }
        auto p = static_cast<unsigned char*>(data) + frame * bytes;
        for (int i=0; i<bytes; ++i) p[i] = static_cast<unsigned char>(raw >> (8*(bigEndian ? bytes-1-i : i)));
    }
};
}
