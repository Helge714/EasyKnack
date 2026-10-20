#pragma once
#include <array>
#include <memory>
namespace easyknack {
/** Fixed working storage, allocated during construction and transferred in O(1).
 * Explicit copy assignment is retained for offline compatibility tests only. */
template<class T,std::size_t N> class OwnedArray {
    std::unique_ptr<std::array<T,N>> storage=std::make_unique<std::array<T,N>>();
public:
    OwnedArray()=default;
    OwnedArray(const OwnedArray& other):OwnedArray(){*this=other;}
    T& operator[](std::size_t i)noexcept{return (*storage)[i];}
    const T& operator[](std::size_t i)const noexcept{return (*storage)[i];}
    auto begin()noexcept{return storage->begin();}auto end()noexcept{return storage->end();}
    auto begin()const noexcept{return storage->begin();}auto end()const noexcept{return storage->end();}
    T* data()noexcept{return storage->data();}const T* data()const noexcept{return storage->data();}
    constexpr std::size_t size()const noexcept{return N;}
    void fill(const T& value){storage->fill(value);}
    void swap(OwnedArray& other)noexcept{storage.swap(other.storage);}
    OwnedArray& operator=(const OwnedArray& other){*storage=*other.storage;return *this;}
    OwnedArray& operator=(const std::array<T,N>& other){*storage=other;return *this;}
};
}
