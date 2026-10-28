#define WIN32_LEAN_AND_MEAN
#include <winsock2.h>
#include <ws2tcpip.h>
#define MA_NO_FLAC
#define MA_NO_ENCODING
#define MA_NO_RESOURCE_MANAGER
#define MA_NO_NODE_GRAPH
#define MA_NO_ENGINE
#define MA_NO_GENERATION
#define MINIAUDIO_IMPLEMENTATION
#include "miniaudio.h"
#include "audio/AudioFileDecode.hpp"
#include "AudioGraph.hpp"
#include "audio/RoutingAudioBackend.hpp"
#include "audio/WindowsDeviceVolume.hpp"
#include "midi/WindowsMidiSource.hpp"
#include <iostream>
#include <chrono>
#include "CommandReader.hpp"

namespace {
struct Socket {
    SOCKET value = INVALID_SOCKET;
    ~Socket() { if (value != INVALID_SOCKET) closesocket(value); }
};
void sendLine(SOCKET socket, const std::string& line) {
    std::string message = line + "\n";
    std::size_t offset = 0;
    while (offset < message.size()) {
        int sent = send(socket, message.data() + offset, static_cast<int>(message.size() - offset), 0);
        if (sent <= 0) throw std::runtime_error("connection closed");
        offset += sent;
    }
}
std::string readLine(SOCKET socket,easyknack::CommandReader& reader) {
    return reader.read([socket](char* buffer,std::size_t size){
        return recv(socket,buffer,static_cast<int>(size),0);
    });
}
}

int main(int argc, char** argv) {
    if (argc > 1 && std::string(argv[1]) == "--decode-audio") {
        try {
            if (argc != 4) throw std::runtime_error("Expected source and destination paths encoded as UTF-8 hex");
            easyknack::decodeAudioFile(argv[2], argv[3]);
            return 0;
        } catch (const std::exception& error) { std::cerr << error.what() << '\n'; return 1; }
    }
    if (argc < 2 || std::string(argv[1]).size() < 16) {
        std::cerr << "Usage: easyknack-audio-host <session-token> [--null]\n";
        return 2;
    }
    WSADATA wsa{};
    if (WSAStartup(MAKEWORD(2, 2), &wsa)) return 3;
    int result = 0;
    auto engine = std::make_unique<easyknack::AudioEngine>();
    try {
        bool nullDevice = argc > 2 && std::string(argv[2]) == "--null";
        auto midi=std::make_unique<easyknack::WindowsMidiSource>(!nullDevice);
        engine->midiSource=midi.get();
        easyknack::RoutingAudioBackend backend(*engine, nullDevice);
        easyknack::WindowsDeviceVolume volume(!nullDevice);
        Socket listener;
        listener.value = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
        if (listener.value == INVALID_SOCKET) throw std::runtime_error("socket failed");
        sockaddr_in address{};
        address.sin_family = AF_INET;
        address.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
        if (bind(listener.value, reinterpret_cast<sockaddr*>(&address), sizeof(address)) || listen(listener.value, 1))
            throw std::runtime_error("loopback bind failed");
        int length = sizeof(address);
        if (getsockname(listener.value, reinterpret_cast<sockaddr*>(&address), &length))
            throw std::runtime_error("port query failed");
        std::cout << "READY 13 " << ntohs(address.sin_port) << std::endl;
        fd_set ready;
        FD_ZERO(&ready); FD_SET(listener.value, &ready);
        timeval timeout{10, 0};
        if (select(0, &ready, nullptr, nullptr, &timeout) != 1) throw std::runtime_error("client startup timeout");
        Socket client;
        client.value = accept(listener.value, nullptr, nullptr);
        if (client.value == INVALID_SOCKET) throw std::runtime_error("accept failed");
        BOOL noDelay = TRUE;
        setsockopt(client.value, IPPROTO_TCP, TCP_NODELAY, reinterpret_cast<char*>(&noDelay), sizeof(noDelay));
        DWORD deadline = 5000;
        setsockopt(client.value, SOL_SOCKET, SO_RCVTIMEO, reinterpret_cast<char*>(&deadline), sizeof(deadline));
        setsockopt(client.value, SOL_SOCKET, SO_SNDTIMEO, reinterpret_cast<char*>(&deadline), sizeof(deadline));
        easyknack::CommandReader commands;
        if (readLine(client.value,commands) != "HELLO 13 " + std::string(argv[1])) throw std::runtime_error("invalid handshake");
        sendLine(client.value, "OK 13");
        std::uint64_t diagnosticGeneration=0;
        easyknack::AudioIOStatistics diagnosticBase;
        for (;;) {
            std::string line;
            try {
                line=readLine(client.value,commands);
                volume.poll();
                if(line.rfind("VOLUME ",0)==0) {
                    std::istringstream args(line.substr(7)); std::string token,extra; float scalar;
                    if(!(args>>token>>scalar) || args>>extra || !std::isfinite(scalar) || scalar<0 || scalar>1)
                        { sendLine(client.value,"ERR invalid volume"); continue; }
                    sendLine(client.value,volume.set(easyknack::unhexText(token),scalar)?"OK":"ERR volume device unavailable"); continue;
                } else if(line.rfind("VOLUME_LINKS",0)==0) {
                    std::istringstream args(line.substr(12)); std::string token; std::set<std::string> ids;
                    while(args>>token) { ids.insert(easyknack::unhexText(token)); if(ids.size()>32) break; }
                    volume.links(std::move(ids)); sendLine(client.value,"OK"); continue;
                } else if(line=="VOLUME_KEY_EVENT") {
                    sendLine(client.value,"OK "+volume.keyEvent()); continue;
                } else if(line=="VOLUMES") {
                    std::string response="OK "+std::string(volume.keysActive()?"1":"0");
                    for(const auto& endpoint:backend.enumerateOutputs()) {
                        auto value=volume.get(endpoint.id);
                        if(value.available) response+=" "+easyknack::hexText(endpoint.id)+" "+std::to_string(value.scalar)+" "+(value.muted?"1":"0")+" "+std::to_string(backend.takePeak(endpoint.id));
                    }
                    sendLine(client.value,response); continue;
                }
                if (line == "QUIT") { sendLine(client.value, "OK"); break; }
                if (line == "PLAY") { midi->setRunning(true); backend.setRunning(true); engine->playing.store(true); }
                else if (line == "STOP") { midi->setRunning(false); engine->playing.store(false); backend.setRunning(false); }
                else if(line=="MIDI_PANIC"){midi->panic();}
                else if(line.rfind("TRIGGER ",0)==0){
                    std::istringstream input(line.substr(8));std::string id,extra;
                    if(!(input>>id)||input>>extra)throw std::runtime_error("invalid trigger target");
                    if(engine->playing.load())easyknack::submitManualTrigger(id);
                }
                else if(line.rfind("MIDI_KEYBOARD ",0)==0){
                    std::istringstream input(line.substr(14));int count;
                    if(!(input>>count)||count<0||count>256)throw std::runtime_error("invalid keyboard event count");
                    std::array<std::uint32_t,256> messages{};
                    for(int i=0;i<count;i++)if(!(input>>messages[i])||messages[i]>0xffffffU)throw std::runtime_error("invalid keyboard MIDI event");
                    std::string extra;if(input>>extra)throw std::runtime_error("unexpected keyboard data");
                    for(int i=0;i<count;i++)midi->keyboardEvent(messages[i]);
                }
                else if(line.rfind("MIDI_DRIVERS ",0)==0){
                    std::istringstream input(line.substr(13));int learning,count;
                    if(!(input>>learning>>count)||(learning!=0&&learning!=1)||count<0||count>64)throw std::runtime_error("invalid MIDI driver subscription");
                    std::vector<std::uint64_t> devices(count);for(auto& id:devices)if(!(input>>id))throw std::runtime_error("invalid driver device");
                    std::string extra;if(input>>extra)throw std::runtime_error("unexpected driver data");midi->prepareDrivers(std::move(devices),learning!=0);
                }
                else if(line=="MIDI_DRIVER_EVENTS"){
                    auto events=midi->driverEvents();std::string response="OK "+std::to_string(events.size());
                    for(auto event:events)response+=" "+std::to_string(event.first)+" "+std::to_string(event.second);
                    sendLine(client.value,response);continue;
                }
                else if(line=="MIDI_INPUTS"){
                    auto devices=midi->enumerate();midi->refresh();
                    std::string response="OK "+std::to_string(devices.size());
                    for(const auto& device:devices)response+=" "+std::to_string(device.key)+" "+easyknack::hexText(device.name);
                    sendLine(client.value,response);continue;
                }
                else if (line == "PING") {
                    sendLine(client.value, "OK " + std::to_string(engine->callbacks.load()));
                    continue;
                } else if (line == "ANALYSIS") {
                    sendLine(client.value,easyknack::analysisResponse());continue;
                } else if(line=="DIAGNOSTICS" || line=="DIAGNOSTICS_RESET") {
                    auto io=backend.statistics();
                    if(diagnosticGeneration!=backend.generation()||line=="DIAGNOSTICS_RESET"){
                        diagnosticGeneration=backend.generation();engine->timing.telemetry.reset();diagnosticBase=io;
                    }
                    if(line=="DIAGNOSTICS_RESET"){sendLine(client.value,"OK");continue;}
                    auto s=engine->timing.telemetry.snapshot();
                    auto delta=[](std::uint64_t value,std::uint64_t base){return value>=base?value-base:value;};
                    std::ostringstream out;out<<"OK 1 "<<s.epoch<<' '<<s.total<<' '<<s.mean<<' '<<s.peak<<' '<<s.p99us<<' '<<s.maxUs<<' '<<s.overBudget<<' '<<s.frames<<' '<<s.rate<<' '<<s.dropped
                        <<' '<<delta(io.captureUnder,diagnosticBase.captureUnder)<<' '<<delta(io.captureOver,diagnosticBase.captureOver)
                        <<' '<<delta(io.playbackUnder,diagnosticBase.playbackUnder)<<' '<<delta(io.playbackOver,diagnosticBase.playbackOver)
                        <<' '<<delta(io.driverOverload,diagnosticBase.driverOverload)<<' '<<delta(io.priming,diagnosticBase.priming)
                        <<' '<<easyknack::hexText("Clock: "+backend.clockDescription())<<' '<<easyknack::hexText("Buffers: "+backend.bufferDescription());
                    sendLine(client.value,out.str());continue;
                } else if (line == "STATS") {
                    sendLine(client.value, "OK " + std::to_string(engine->timing.average.load(std::memory_order_relaxed))
                            + " " + std::to_string(engine->timing.peak.load(std::memory_order_relaxed))
                            + " " + std::to_string(engine->timing.overBudget.load(std::memory_order_relaxed)));
                    continue;
                } else if (line == "ENDPOINTS") {
                    auto inputs = backend.enumerateInputs();
                    auto outputs = backend.enumerateOutputs();
                    backend.refresh();
                    auto caps = backend.capabilities();
                    std::string response = "OK " + std::string(caps.systemCapture ? "1" : "0")
                            + (caps.applicationCapture ? "1" : "0") + "000 " + std::to_string(inputs.size() + outputs.size());
                    auto append = [&](const easyknack::AudioEndpoint& endpoint, char direction) {
                        response += " " + std::string(1, direction) + " " + std::to_string(static_cast<int>(endpoint.type))
                                + " " + easyknack::hexText(endpoint.id) + " " + easyknack::hexText(endpoint.name)
                                + " " + (endpoint.available ? "1" : "0") + " " + (endpoint.canRoute ? "1" : "0");
                    };
                    for (const auto& endpoint : inputs) append(endpoint, 'i');
                    for (const auto& endpoint : outputs) append(endpoint, 'o');
                    sendLine(client.value, response); continue;
                } else if (line == "ROUTING_STATUS") {
                    auto status=backend.status();if(!midi->status().empty())status+=(status.empty()?"":" | ")+midi->status();
                    sendLine(client.value, "OK " + easyknack::hexText(status)); continue;
                } else if (line == "IO_CLOCK") {
                    sendLine(client.value, "OK " + backend.clockDescription()); continue;
                } else if (line == "IO_BUFFERS") {
                    sendLine(client.value, "OK " + backend.bufferDescription()); continue;
                } else if (line == "IOSTATS") {
                    auto stats = backend.statistics();
                    sendLine(client.value, "OK " + std::to_string(stats.capturedFrames) + " " + std::to_string(stats.playedFrames)
                            + " " + std::to_string(stats.underruns) + " " + std::to_string(stats.overruns)); continue;
                } else if (line.rfind("GRAPH ", 0) == 0) {
                    auto configuration=easyknack::AudioGraph::compile(line,easyknack::createNodeProcessor,true);
                    if(!engine->publishParameters(std::move(configuration))) {
                        auto graph = easyknack::AudioGraph::parse(line);
                        backend.prepare(graph.routes());midi->prepare(graph.midiDevices());engine->publish(std::move(graph));
                    }
                }
                else throw std::runtime_error("unknown command");
                sendLine(client.value, "OK");
            } catch (const easyknack::FileBufferLoadError& error) {
                sendLine(client.value, "ERR " + std::string(error.what()));
            } catch (const std::exception& error) {
                engine->playing.store(false); backend.setRunning(false);
                midi->setRunning(false);
                sendLine(client.value, "ERR " + std::string(error.what()));
            }
        }
    } catch (const std::exception& error) {
        std::cerr << error.what() << '\n';
        result = 1;
    }
    WSACleanup();
    return result;
}

