#include "nodes/MultipleGraphNode.hpp"
#include <cassert>
#include <cstdlib>
#include <new>
using namespace easyknack;
thread_local bool checkingRender=false;
void* operator new(std::size_t size){assert(!checkingRender);if(auto* p=std::malloc(size?size:1))return p;throw std::bad_alloc();}
void operator delete(void* p)noexcept{assert(!checkingRender||!p);std::free(p);}
void operator delete(void* p,std::size_t)noexcept{::operator delete(p);}
struct Saved {
 std::vector<MultipleGraphNode::State*> groups;
 std::vector<OscillatorNode*> oscillators;
 std::vector<const WaveformResource*> waves;
};
void visit(MultipleGraphNode& group,Saved& saved){
 saved.groups.push_back(group.state.get());
 for(auto& instance:group.state->instances)for(auto& node:instance->runtime->graph.nodes){
  if(auto* child=dynamic_cast<MultipleGraphNode*>(node.processor.get()))visit(*child,saved);
  if(auto* osc=dynamic_cast<OscillatorNode*>(node.processor.get())){osc->phase=1.234;saved.oscillators.push_back(osc);}
  if(auto* wave=dynamic_cast<WaveformNode*>(node.processor.get()))saved.waves.push_back(wave->wave.get());
 }
}
void check(const char* initial,const char* changed,const char* resource,const char* structural){
 AudioEngine engine;auto graph=AudioGraph::compile(initial,MultipleGraphNode::factory);
 auto* group=dynamic_cast<MultipleGraphNode*>(graph.nodes[0].processor.get());assert(group);Saved saved;visit(*group,saved);assert(!saved.oscillators.empty());
 engine.publish(std::move(graph));std::array<float,512> out{};engine.render(out.data(),256);
 auto patch=[&](const char* command,double frequency){
  auto config=AudioGraph::compile(command,MultipleGraphNode::factory,true);
  auto* prototype=static_cast<MultipleGraphNode*>(config.nodes[0].processor.get());assert(prototype->state->instances.size()==1);
  assert(engine.publishParameters(std::move(config)));
  checkingRender=true;engine.render(out.data(),256);checkingRender=false;
  for(auto* osc:saved.oscillators){assert(osc->frequency==frequency);assert(osc->phase==1.234);}
 };
 patch(changed,660);patch(initial,440);patch(changed,660);
 Saved after;visit(*group,after);assert(saved.groups==after.groups);assert(saved.waves==after.waves);
 patch(resource,660);Saved replaced;visit(*group,replaced);assert(saved.groups==replaced.groups);assert(saved.waves!=replaced.waves);
 patch(initial,440);
 assert(!engine.publishParameters(AudioGraph::compile(structural,MultipleGraphNode::factory,true)));
}
int main(){// CHECKS
}
