#include "platform/ControlSocket.hpp"
#define MA_NO_FLAC
#define MA_NO_ENCODING
#define MA_NO_RESOURCE_MANAGER
#define MA_NO_NODE_GRAPH
#define MA_NO_ENGINE
#define MA_NO_GENERATION
#ifndef __APPLE__
#define MINIAUDIO_IMPLEMENTATION
#endif
#include "miniaudio.h"
#include "audio/AudioFileDecode.hpp"
#include "AudioGraph.hpp"
#include "AudioRecording.hpp"
#include "platform/HostPlatform.hpp"
#include <iostream>
#include <chrono>
#include "CommandReader.hpp"

namespace {
void sendLine(easyknack::platform::ControlSocket& socket,const std::string& line){socket.sendLine(line);}
std::string readLine(easyknack::platform::ControlSocket& socket,easyknack::CommandReader& reader){
    return reader.read([&](char* buffer,std::size_t size){return socket.receive(buffer,size);});
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
    easyknack::platform::NetworkRuntime network;
    if(!network.ready())return 3;
    int result = 0;
    auto engine = std::make_unique<easyknack::AudioEngine>();
    try {
        bool nullDevice = argc > 2 && std::string(argv[2]) == "--null";
        auto services=easyknack::platform::createHostServices(*engine,nullDevice);
        auto* midi=services.midi.get();auto& backend=*services.audio;auto& volume=*services.volume;
        auto listener=easyknack::platform::ControlSocket::listenLoopback();
        std::cout << "READY 13 " << listener.port() << std::endl;
        auto client=listener.acceptClient();
        easyknack::CommandReader commands;
        if (readLine(client,commands) != "HELLO 13 " + std::string(argv[1])) throw std::runtime_error("invalid handshake");
        sendLine(client, "OK 13");
        std::uint64_t diagnosticGeneration=0;
        easyknack::AudioIOStatistics diagnosticBase;
        for (;;) {
            std::string line;
            try {
                line=readLine(client,commands);
                backend.poll();midi->poll();volume.poll();
                if(line.rfind("VOLUME ",0)==0) {
                    std::istringstream args(line.substr(7)); std::string token,extra; float scalar;
                    if(!(args>>token>>scalar) || args>>extra || !std::isfinite(scalar) || scalar<0 || scalar>1)
                        { sendLine(client,"ERR invalid volume"); continue; }
                    sendLine(client,volume.set(easyknack::unhexText(token),scalar)?"OK":"ERR volume device unavailable"); continue;
                } else if(line.rfind("VOLUME_LINKS",0)==0) {
                    std::istringstream args(line.substr(12)); std::string token; std::set<std::string> ids;
                    while(args>>token) { ids.insert(easyknack::unhexText(token)); if(ids.size()>32) break; }
                    volume.links(std::move(ids)); sendLine(client,"OK"); continue;
                } else if(line=="VOLUME_KEY_EVENT") {
                    sendLine(client,"OK "+volume.keyEvent()); continue;
                } else if(line=="VOLUMES") {
                    std::string response="OK "+std::string(volume.keysActive()?"1":"0");
                    auto endpoints=backend.enumerateOutputs();
                    struct PeakRead {easyknack::HostAudioBackend& backend;explicit PeakRead(easyknack::HostAudioBackend& b):backend(b){backend.beginPeakRead();}~PeakRead(){backend.endPeakRead();}} peakRead(backend);
                    for(const auto& endpoint:endpoints) {
                        auto value=volume.get(endpoint.id);
                        if(value.available) response+=" "+easyknack::hexText(endpoint.id)+" "+std::to_string(value.scalar)+" "+(value.muted?"1":"0")+" "+std::to_string(backend.takePeak(endpoint.id));
                    }
                    sendLine(client,response); continue;
                }
                if (line == "QUIT") { sendLine(client, "OK"); break; }
                if (line == "PLAY") { midi->setRunning(true); backend.setRunning(true); engine->playing.store(true); }
                else if (line == "STOP") { midi->setRunning(false); engine->playing.store(false); backend.setRunning(false); easyknack::AudioRecording::stopAll(); }
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
                    sendLine(client,response);continue;
                }
                else if(line=="MIDI_INPUTS"){
                    auto devices=midi->enumerate();midi->refresh();
                    std::string response="OK "+std::to_string(devices.size());
                    for(const auto& device:devices)response+=" "+std::to_string(device.key)+" "+easyknack::hexText(device.name);
                    sendLine(client,response);continue;
                }
                else if (line == "PING") {
                    sendLine(client, "OK " + std::to_string(engine->callbacks.load()));
                    continue;
                } else if (line == "ANALYSIS") {
                    sendLine(client,easyknack::analysisResponse());continue;
                } else if(line=="DIAGNOSTICS" || line=="DIAGNOSTICS_RESET") {
                    auto io=backend.statistics();
                    if(diagnosticGeneration!=backend.generation()||line=="DIAGNOSTICS_RESET"){
                        diagnosticGeneration=backend.generation();engine->timing.telemetry.reset();diagnosticBase=io;
                    }
                    if(line=="DIAGNOSTICS_RESET"){sendLine(client,"OK");continue;}
                    auto s=engine->timing.telemetry.snapshot();
                    auto delta=[](std::uint64_t value,std::uint64_t base){return value>=base?value-base:value;};
                    std::ostringstream out;out<<"OK 1 "<<s.epoch<<' '<<s.total<<' '<<s.mean<<' '<<s.peak<<' '<<s.p99us<<' '<<s.maxUs<<' '<<s.overBudget<<' '<<s.frames<<' '<<s.rate<<' '<<s.dropped
                        <<' '<<delta(io.captureUnder,diagnosticBase.captureUnder)<<' '<<delta(io.captureOver,diagnosticBase.captureOver)
                        <<' '<<delta(io.playbackUnder,diagnosticBase.playbackUnder)<<' '<<delta(io.playbackOver,diagnosticBase.playbackOver)
                        <<' '<<delta(io.driverOverload,diagnosticBase.driverOverload)<<' '<<delta(io.priming,diagnosticBase.priming)
                        <<' '<<easyknack::hexText("Clock: "+backend.clockDescription())<<' '<<easyknack::hexText("Buffers: "+backend.bufferDescription());
                    sendLine(client,out.str());continue;
                } else if (line == "STATS") {
                    sendLine(client, "OK " + std::to_string(engine->timing.average.load(std::memory_order_relaxed))
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
                    sendLine(client, response); continue;
                } else if (line == "ROUTING_STATUS") {
                    auto status=backend.status();if(!midi->status().empty())status+=(status.empty()?"":" | ")+midi->status();
                    sendLine(client, "OK " + easyknack::hexText(status)); continue;
                } else if (line == "IO_CLOCK") {
                    sendLine(client, "OK " + backend.clockDescription()); continue;
                } else if (line == "IO_BUFFERS") {
                    sendLine(client, "OK " + backend.bufferDescription()); continue;
                } else if (line == "IOSTATS") {
                    auto stats = backend.statistics();
                    sendLine(client, "OK " + std::to_string(stats.capturedFrames) + " " + std::to_string(stats.playedFrames)
                            + " " + std::to_string(stats.underruns) + " " + std::to_string(stats.overruns)); continue;
                } else if (line.rfind("GRAPH ", 0) == 0) {
                    auto configuration=easyknack::AudioGraph::compile(line,easyknack::createNodeProcessor,true);
                    if(!engine->publishParameters(std::move(configuration))) {
                        auto graph = easyknack::AudioGraph::parse(line);
                        backend.prepare(graph.routes());midi->prepare(graph.midiDevices());engine->publish(std::move(graph));
                    }
                }
                else throw std::runtime_error("unknown command");
                sendLine(client, "OK");
            } catch (const easyknack::FileBufferLoadError& error) {
                sendLine(client, "ERR " + std::string(error.what()));
            } catch (const std::exception& error) {
                engine->playing.store(false); backend.setRunning(false);
                midi->setRunning(false);
                sendLine(client, "ERR " + std::string(error.what()));
            }
        }
    } catch (const std::exception& error) {
        std::cerr << error.what() << '\n';
        result = 1;
    }
    return result;
}

