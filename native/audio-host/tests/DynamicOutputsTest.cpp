#include "AudioGraph.hpp"
#include <cassert>
#include <cstdlib>
#include <new>
using namespace easyknack;
static thread_local bool watching=false;
static thread_local unsigned allocations=0;
void* operator new(std::size_t n){if(watching)++allocations;if(auto* p=std::malloc(n?n:1))return p;throw std::bad_alloc();}
void operator delete(void* p)noexcept{std::free(p);}
void operator delete(void* p,std::size_t)noexcept{std::free(p);}
struct Wide:NodeProcessor {
    std::size_t count=24;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    void configure(std::istream& in)override{in>>count;}
    std::size_t outputCount()const noexcept override{return count;}
    void process(ProcessContext& c)noexcept override{for(std::size_t p=0;p<count;p++)if(auto* out=c.outputAt(p))std::fill_n(out,c.bufferSize,float(p+1));}
};
struct WideEvents:Wide {
    bool supportsEventRate()const noexcept override{return true;}
    bool evaluatesOnEvents()const noexcept override{return true;}
};
struct WideResources:Wide {
    std::vector<std::unique_ptr<SpectrumStream>> resources;
    void configure(std::istream& in)override{Wide::configure(in);for(std::size_t p=0;p<count;p++){auto resource=std::make_unique<SpectrumStream>();resource->identity=p+1;resources.push_back(std::move(resource));}}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t p)noexcept override{return resources[p].get();}
    void process(ProcessContext&)noexcept override{}
};
struct Sink:NodeProcessor {
    bool resource=false;
    explicit Sink(bool resource=false):resource(resource){}
    bool isSink()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return resource?ResourceKind::spectrum:ResourceKind::none;}
    void configure(std::istream& in)override{sources={readInput(in)};}
    void process(ProcessContext& c)noexcept override{float value=resource?float(static_cast<const SpectrumStream*>(c.resource(0))->identity):c.numericInput(0)[0];std::fill_n(c.output,c.bufferSize,value);}
};
static std::unique_ptr<NodeProcessor> factory(const std::string& id){
    if(id=="wide")return std::make_unique<Wide>();
    if(id=="events")return std::make_unique<WideEvents>();
    if(id=="resources")return std::make_unique<WideResources>();
    if(id=="sink")return std::make_unique<Sink>();
    if(id=="resource_sink")return std::make_unique<Sink>(true);
    return createNodeProcessor(id);
}
static void run(const std::string& command,float expected){
    auto graph=AudioGraph::compile(command,factory);float out[256]{};
    allocations=0;watching=true;graph.process(out,256,0,nullptr);watching=false;
    assert(allocations==0);for(float value:out)assert(value==expected);
    assert(graph.nodes[0].outputPointers.size()==24);
}
int main(){
    for(auto rate:{"a","c"})run(std::string("GRAPH 2 w wide 24 @ ")+rate+" - o sink 5888 @ a a",24);
    run("GRAPH 2 w events 24 @ e - o sink 5888 @ a a",24);
    // Current generated parameter readers must accept references beyond port 7.
    const std::string fingerprint="TEST_RELAY_FINGERPRINT";
    SpectrumRelayNode relay;std::istringstream contract("!"+fingerprint+" 5888");relay.configure(contract);assert(relay.sources[0]==5888);
    run("GRAPH 3 w resources 24 @ c - r spectrumrelay !"+fingerprint+" 5888 @ c c o resource_sink 1 @ a c",24);
    for(auto bad:{"-2","6144","255"}){
        bool rejected=false;try{AudioGraph::compile(std::string("GRAPH 2 w wide 24 @ a - o sink ")+bad+" @ a a",factory);}catch(const std::runtime_error&){rejected=true;}assert(rejected);
    }
}
