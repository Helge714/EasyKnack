#pragma once
#include "ParameterContracts.hpp"
#include "OutputNode.hpp"
#include "../audio/ProtocolText.hpp"
namespace easyknack {
struct DeviceOutputNode : OutputNode {
    std::string endpoint;
    void configure(std::istream& in) override {
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::OutputNodeParameters::read(in);
        sources=p.sources();endpoint=unhexText(p.endpoint_value);if(endpoint.empty())throw std::runtime_error("Empty output endpoint");
        return;}

        sources = {readInput(in)};
        std::string token; if (!(in >> token)) throw std::runtime_error("missing output endpoint");
        endpoint = unhexText(token);
        if (endpoint.empty()) throw std::runtime_error("empty output endpoint");
    }
    const std::string& outputEndpoint() const noexcept override { return endpoint; }
};
EASYKNACK_NODE(DeviceOutputNode, "deviceout")
}

