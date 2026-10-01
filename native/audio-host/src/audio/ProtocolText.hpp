#pragma once
#include <stdexcept>
#include <string>
namespace easyknack {
inline std::string hexText(const std::string& value) {
    if (value.empty()) return "-";
    static constexpr char digits[] = "0123456789abcdef";
    std::string result; result.reserve(value.size()*2);
    for (unsigned char c : value) { result += digits[c >> 4]; result += digits[c & 15]; }
    return result;
}
inline std::string unhexText(const std::string& value) {
    if (value == "-") return {};
    if (value.size() % 2 || value.size() > 32768) throw std::runtime_error("invalid encoded text");
    auto digit = [](char c) -> int { if (c >= '0' && c <= '9') return c-'0'; if(c >= 'a' && c <= 'f') return c-'a'+10; throw std::runtime_error("invalid hex"); };
    std::string result;
    for (std::size_t i=0; i<value.size(); i+=2) result += static_cast<char>(digit(value[i])*16+digit(value[i+1]));
    return result;
}
}
