#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../audio/ProtocolText.hpp"
namespace easyknack {
struct InputNode : NodeProcessor {
    bool canShareChannels() const noexcept override { return mono; }
    std::string endpoint;
    bool mono = false;
    void configure(std::istream& in) override {
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::InputNodeParameters::read(in);
        endpoint=unhexText(p.endpoint_value);mono=p.channels_value=="mono";if(endpoint.empty()||p.capture_mode_value!="copy"||(p.channels_value!="mono"&&p.channels_value!="stereo"))throw std::runtime_error("Invalid capture configuration");
        return;}

        std::string token, mode, channels;
        if (!(in >> token >> mode >> channels) || mode != "copy" || (channels != "mono" && channels != "stereo"))
            throw std::runtime_error("unsupported capture configuration");
        endpoint = unhexText(token); mono = channels == "mono";
        if (endpoint.empty()) throw std::runtime_error("missing capture endpoint");
    }
    std::string inputEndpoint() const override { return endpoint; }
    void process(ProcessContext& c) noexcept override {
        for (std::size_t i=0; i<c.bufferSize; ++i) {
            float value = 0;
            if (c.audioIO) value = mono ? (c.audioIO->input(endpoint, 0, i) + c.audioIO->input(endpoint, 1, i)) * .5f
                                       : c.audioIO->input(endpoint, c.channelIndex, i);
            c.output[i] = std::isfinite(value) ? value : 0;
        }
    }
};
EASYKNACK_NODE(InputNode, "input")
}

