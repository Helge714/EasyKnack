#pragma once
#include <cstdint>
#include <cstring>
#include <cmath>
#include <limits>
#include <type_traits>
#include <algorithm>
namespace easyknack {
// Bit transport preserves every uint64 value; floating point is never an integer's backing store.
template<class T> inline std::uint64_t formulaPack(T value)noexcept {
    if constexpr(std::is_same_v<T,bool>)return value?1:0;
    else if constexpr(sizeof(T)==4){std::uint32_t bits;std::memcpy(&bits,&value,4);return bits;}
    else {std::uint64_t bits;std::memcpy(&bits,&value,8);return bits;}
}
template<class T> inline T formulaUnpack(std::uint64_t bits)noexcept {
    if constexpr(std::is_same_v<T,bool>)return bits!=0;
    else if constexpr(sizeof(T)==4){auto low=std::uint32_t(bits);T value;std::memcpy(&value,&low,4);return value;}
    else {T value;std::memcpy(&value,&bits,8);return value;}
}
template<class T,class S> inline T formulaCast(S value)noexcept {
    if constexpr(std::is_same_v<T,bool>)return value!=S(0);
    else if constexpr(std::is_integral_v<T> && std::is_floating_point_v<S>){
        // Never invoke C++ undefined behaviour for a non-finite or out-of-range cast.
        if(std::isnan(value))return 0;
        const double d=double(value),lo=double(std::numeric_limits<T>::lowest()),hi=double(std::numeric_limits<T>::max());
        if(d<=lo)return std::numeric_limits<T>::lowest();if(d>=hi)return std::numeric_limits<T>::max();return static_cast<T>(value);
    }else if constexpr(std::is_integral_v<T> && std::is_integral_v<S>){
        using U=std::make_unsigned_t<T>;U bits=static_cast<U>(value);T out;std::memcpy(&out,&bits,sizeof(T));return out;
    }else return static_cast<T>(value);
}
enum class FormulaOp {add,sub,mul,div,mod,neg,abs,sqrt,sin,cos,tan,asin,acos,atan,atan2,exp,log,log2,log10,floor,ceil,round,pow,min,max,clamp,mix,sign,eq,ne,lt,gt,le,ge};
template<class T> inline T formulaCalc(FormulaOp op,T a,T b=T(0),T c=T(0))noexcept {
    using O=FormulaOp;
    if constexpr(std::is_integral_v<T>){
        using U=std::make_unsigned_t<T>;
        // Promote 32-bit unsigned operands before multiplication to avoid integral promotion UB.
        auto wrap=[](std::uint64_t bits){return formulaCast<T>(bits);};
        switch(op){
            case O::add:return wrap(std::uint64_t(U(a))+std::uint64_t(U(b)));
            case O::sub:return wrap(std::uint64_t(U(a))-std::uint64_t(U(b)));
            case O::mul:return wrap(std::uint64_t(U(a))*std::uint64_t(U(b)));
            case O::neg:return wrap(std::uint64_t(0)-std::uint64_t(U(a)));
            case O::abs:if constexpr(std::is_signed_v<T>)return a<0?wrap(std::uint64_t(0)-std::uint64_t(U(a))):a;else return a;
            case O::div:case O::mod:
                if(b==0)return 0;
                if constexpr(std::is_signed_v<T>)if(a==std::numeric_limits<T>::min()&&b==T(-1))return op==O::div?a:T(0);
                return op==O::div?a/b:a%b;
            default:break;
        }
    }else{
        switch(op){case O::add:return a+b;case O::sub:return a-b;case O::mul:return a*b;case O::div:return a/b;case O::mod:return std::fmod(a,b);case O::neg:return -a;case O::abs:return std::abs(a);default:break;}
    }
    switch(op){
        case O::min:return std::min(a,b);case O::max:return std::max(a,b);case O::clamp:return std::min(std::max(a,b),c);case O::sign:return T((a>0)-(a<0));
        case O::mix:return a+(b-a)*c;
        case O::sqrt:return T(std::sqrt(a));case O::sin:return T(std::sin(a));case O::cos:return T(std::cos(a));case O::tan:return T(std::tan(a));case O::asin:return T(std::asin(a));case O::acos:return T(std::acos(a));case O::atan:return T(std::atan(a));case O::atan2:return T(std::atan2(a,b));
        case O::exp:return T(std::exp(a));case O::log:return T(std::log(a));case O::log2:return T(std::log2(a));case O::log10:return T(std::log10(a));case O::floor:return T(std::floor(a));case O::ceil:return T(std::ceil(a));case O::round:return T(std::round(a));case O::pow:return T(std::pow(a,b));
        default:return 0;
    }
}
template<class T> inline bool formulaCompare(FormulaOp op,T a,T b)noexcept {
    switch(op){case FormulaOp::eq:return a==b;case FormulaOp::ne:return a!=b;case FormulaOp::lt:return a<b;case FormulaOp::gt:return a>b;case FormulaOp::le:return a<=b;default:return a>=b;}
}
}
