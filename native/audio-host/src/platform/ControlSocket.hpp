#pragma once
#include <string>
#include <stdexcept>
#include <algorithm>
#include <limits>
#ifdef _WIN32
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <winsock2.h>
#include <ws2tcpip.h>
#else
#include <sys/socket.h>
#include <sys/time.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <unistd.h>
#endif
namespace easyknack::platform {
class NetworkRuntime {
    bool initialized=true;
public:
    NetworkRuntime(){
#ifdef _WIN32
        WSADATA data{};initialized=WSAStartup(MAKEWORD(2,2),&data)==0;
#endif
    }
    ~NetworkRuntime(){
#ifdef _WIN32
        if(initialized)WSACleanup();
#endif
    }
    bool ready()const{return initialized;}
};
class ControlSocket {
#ifdef _WIN32
    using Handle=SOCKET;using Length=int;static constexpr Handle invalid=INVALID_SOCKET;
#else
    using Handle=int;using Length=socklen_t;static constexpr Handle invalid=-1;
#endif
    Handle handle=invalid;
    explicit ControlSocket(Handle value):handle(value){
#if defined(SO_NOSIGPIPE) && !defined(_WIN32)
        int valueOn=1;setsockopt(handle,SOL_SOCKET,SO_NOSIGPIPE,&valueOn,sizeof(valueOn));
#endif
    }
public:
    ControlSocket(const ControlSocket&)=delete;ControlSocket& operator=(const ControlSocket&)=delete;
    ControlSocket(ControlSocket&& other)noexcept:handle(other.handle){other.handle=invalid;}
    ~ControlSocket(){if(handle!=invalid){
#ifdef _WIN32
        closesocket(handle);
#else
        ::close(handle);
#endif
    }}
    static ControlSocket listenLoopback(){
        ControlSocket listener(::socket(AF_INET,SOCK_STREAM,IPPROTO_TCP));
        if(listener.handle==invalid)throw std::runtime_error("socket failed");
        sockaddr_in address{};address.sin_family=AF_INET;address.sin_addr.s_addr=htonl(INADDR_LOOPBACK);
        if(::bind(listener.handle,reinterpret_cast<sockaddr*>(&address),sizeof(address))||::listen(listener.handle,1))throw std::runtime_error("loopback bind failed");
        return listener;
    }
    unsigned port()const{sockaddr_in address{};Length size=sizeof(address);if(getsockname(handle,reinterpret_cast<sockaddr*>(&address),&size))throw std::runtime_error("port query failed");return ntohs(address.sin_port);}
    ControlSocket acceptClient(){
        fd_set ready;FD_ZERO(&ready);FD_SET(handle,&ready);timeval timeout{10,0};
#ifdef _WIN32
        int count=0;
#else
        int count=handle+1;
#endif
        if(select(count,&ready,nullptr,nullptr,&timeout)!=1)throw std::runtime_error("client startup timeout");
        ControlSocket client(::accept(handle,nullptr,nullptr));if(client.handle==invalid)throw std::runtime_error("accept failed");
        int enabled=1;setsockopt(client.handle,IPPROTO_TCP,TCP_NODELAY,reinterpret_cast<const char*>(&enabled),sizeof(enabled));
#ifdef _WIN32
        DWORD deadline=5000;
#else
        timeval deadline{5,0};
#endif
        setsockopt(client.handle,SOL_SOCKET,SO_RCVTIMEO,reinterpret_cast<const char*>(&deadline),sizeof(deadline));
        setsockopt(client.handle,SOL_SOCKET,SO_SNDTIMEO,reinterpret_cast<const char*>(&deadline),sizeof(deadline));return client;
    }
    int receive(char* buffer,std::size_t size){return static_cast<int>(::recv(handle,buffer,static_cast<int>(std::min(size,std::size_t(std::numeric_limits<int>::max()))),0));}
    void sendLine(const std::string& line){
        auto data=line+"\n";std::size_t offset=0;
        while(offset<data.size()){
#ifdef MSG_NOSIGNAL
            constexpr int flags=MSG_NOSIGNAL;
#else
            constexpr int flags=0;
#endif
            auto sent=::send(handle,data.data()+offset,static_cast<int>(std::min(data.size()-offset,std::size_t(std::numeric_limits<int>::max()))),flags);
            if(sent<=0)throw std::runtime_error("connection closed");offset+=std::size_t(sent);
        }
    }
};
}
