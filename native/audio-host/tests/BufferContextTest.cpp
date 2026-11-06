#include "nodes/HistoryBufferNode.hpp"
#include "nodes/BufferContextNode.hpp"
#include "BufferRead.hpp"
#include <array>
#include <cassert>
using namespace easyknack;
int main(){
    HistoryBufferNode writer;writer.seconds=1;writer.prepare(8,4);
    BufferContextNode node;assert(writer.outputCount()==1&&node.outputCount()==2);
    std::array<float,4> size{},position{};float* outputs[]={size.data(),position.data()};
    const DspResource* resources[]={&writer.history};
    ProcessContext c{};c.bufferSize=4;c.inputCount=1;c.output=size.data();c.outputs=outputs;c.outputCount=2;c.resourceInputs=resources;
    for(unsigned block=0;block<3;block++){
        writer.process(c);node.process(c);
        for(unsigned i=0;i<4;i++){
            double expected=((block*4+i+1)%8)/8.;assert(size[i]==8&&position[i]==expected);
            assert(readBufferPosition(writer.history,position[i]*8,i).distance==0);
        }
    }
    writer.seconds=2;writer.prepare(8,4);writer.process(c);node.process(c);assert(size[0]==16&&position[0]==1.f/16);
    HistoryResource file;file.samples.assign(12,1);file.sampleRate=8;file.immutable=true;file.end=12;resources[0]=&file;node.process(c);
    for(unsigned i=0;i<4;i++)assert(size[i]==12&&position[i]==1);
    file.samples.clear();node.process(c);for(unsigned i=0;i<4;i++)assert(size[i]==0&&position[i]==0);
    resources[0]=nullptr;node.process(c);for(unsigned i=0;i<4;i++)assert(size[i]==0&&position[i]==0);
}
