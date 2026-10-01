#pragma once
#include "GeneratedNodeRegistry.hpp"
#include "GraphRuntime.hpp"
namespace easyknack {
inline AudioGraph AudioGraph::parse(const std::string& message) {
    return compile(message,createNodeProcessor);
}
}
