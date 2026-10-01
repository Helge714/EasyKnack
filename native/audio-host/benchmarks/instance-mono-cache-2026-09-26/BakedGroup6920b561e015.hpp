#pragma once
#include "ParameterContracts.hpp"
#include "nodes/MultipleGraphNode.hpp"
#include "CompiledInstanceAudio.hpp"
namespace easyknack {
struct BakedGroup6920b561e015:MultipleGraphNode {
    CompiledInstanceAudio audioKernel;
    std::shared_ptr<CompiledScalarMemo> scalarMemo=std::make_shared<CompiledScalarMemo>();
    struct CompiledInstance final:Instance {
    std::shared_ptr<CompiledScalarMemo> memo;
    bool scalarCacheValid=false;std::array<double,5> scalarKeys{};
    void renderScalars(int frames)noexcept{auto& g=runtime->graph;
        const auto& p1=static_cast<const MathNode&>(*g.nodes[1].processor);
        if(p1.currentB!=1.0f){scalarCacheValid=false;Instance::renderScalars(frames);return;}
        const auto& p2=static_cast<const MathNode&>(*g.nodes[2].processor);
        const auto& p7=static_cast<const MathNode&>(*g.nodes[7].processor);
        if(p7.currentB!=-1.0f){scalarCacheValid=false;Instance::renderScalars(frames);return;}
        const auto& p8=static_cast<const MathNode&>(*g.nodes[8].processor);
        if(p8.currentA!=0.0f){scalarCacheValid=false;Instance::renderScalars(frames);return;}
        const auto& p5=static_cast<const MathNode&>(*g.nodes[5].processor);
        const auto& p6=static_cast<const MathNode&>(*g.nodes[6].processor);
        if(p6.currentA!=1.0f){scalarCacheValid=false;Instance::renderScalars(frames);return;}
        const std::array<double,5> keys{g.preparedSampleRate,double(static_cast<const InstanceContextNode&>(*g.nodes[0].processor).count),double(static_cast<const InstanceContextNode&>(*g.nodes[0].processor).index),([&]{const auto& v=static_cast<const BakedInput&>(*g.nodes[12].processor);return double(v.external&&v.external->input(1)?v.external->input(1)[0]:v.fallback);}()),([&]{const auto& v=static_cast<const BakedInput&>(*g.nodes[13].processor);return double(v.external&&v.external->input(2)?v.external->input(2)[0]:v.fallback);}())};
        if(scalarCacheValid&&keys==scalarKeys)return;scalarKeys=keys;scalarCacheValid=true;
        float values0[4]{};float* outputs0[]={&values0[0],&values0[1],&values0[2],&values0[3]};
        ProcessContext context0{nullptr,0,values0,1,g.preparedSampleRate,g.silence.data()};context0.outputs=outputs0;context0.outputCount=4;
        static_cast<InstanceContextNode&>(*g.nodes[0].processor).InstanceContextNode::process(context0);
        const float n0p0=values0[0];
        const float n0p1=values0[1];
        const float n0p2=values0[2];
        const float n0p3=values0[3];
        const float n1p0=memo->evaluate(1,n0p0,1.0f,g.preparedSampleRate,[&]{return MathNode::evaluate(n0p0,1.0f,MathOperation::add,false);});
        const auto& p12=static_cast<const BakedInput&>(*g.nodes[12].processor);
        const float n12p0=p12.external&&p12.external->input(1)?p12.external->input(1)[0]:p12.fallback;
        const float n9p0=UnitConverterNode::evaluate(n12p0,UnitFormat::factor,UnitFormat::factor,g.preparedSampleRate);
        const float n2p0=memo->evaluate(2,n1p0,n9p0,g.preparedSampleRate,[&]{return MathNode::evaluate(n1p0,n9p0,MathOperation::multiply,false);});
        const auto& p13=static_cast<const BakedInput&>(*g.nodes[13].processor);
        const float n13p0=p13.external&&p13.external->input(2)?p13.external->input(2)[0]:p13.fallback;
        const float n10p0=UnitConverterNode::evaluate(n13p0,UnitFormat::factor,UnitFormat::factor,g.preparedSampleRate);
        const float n7p0=memo->evaluate(7,n10p0,-1.0f,g.preparedSampleRate,[&]{return MathNode::evaluate(n10p0,-1.0f,MathOperation::multiply,false);});
        const float n8p0=memo->evaluate(8,0.0f,n7p0,g.preparedSampleRate,[&]{return MathNode::evaluate(0.0f,n7p0,MathOperation::add,false);});
        const float n5p0=memo->evaluate(5,n1p0,n8p0,g.preparedSampleRate,[&]{return MathNode::evaluate(n1p0,n8p0,MathOperation::power,false);});
        const float n6p0=memo->evaluate(6,1.0f,n5p0,g.preparedSampleRate,[&]{return MathNode::evaluate(1.0f,n5p0,MathOperation::multiply,false);});
        g.nodes[2].voice(0).channelCache[0].outputs[0][0]=n2p0;
        g.nodes[6].voice(0).channelCache[0].outputs[0][0]=n6p0;
    }
    void prepare(double rate,std::size_t frames)override{Instance::prepare(rate,frames);scalarCacheValid=false;}
    void adoptState(NodeProcessor& p)noexcept override{Instance::adoptState(p);scalarCacheValid=false;}
    };
    void prepare(double rate,std::size_t frames)override{MultipleGraphNode::prepare(rate,frames);audioKernel.invalidate();}
    std::unique_ptr<Instance> createInstance()override{auto instance=std::make_unique<CompiledInstance>();instance->memo=scalarMemo;return instance;}
    std::array<float,3> defaults{};
    bool inputUsesBlockData(std::size_t)const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{defaults=static_cast<const BakedGroup6920b561e015&>(p).defaults;}
    void configure(std::istream& in)override{auto p=parameters::BakedGroup6920b561e015Parameters::read(in);
        std::istringstream setup("!be6c5b3b1b5d2183 -1 20 40 mean -1 -1 -1 -1 -1 -1 -1 -1 4d554c5433203320312031312031322031332034203120382031203f2032203f2035203f2036203f2037203f2038203f2039203f203130203f2047524150482031342033623230663132302d303562632d333035642d613061662d37373061316331366333616420696e7374616e6365636f6e746578742021316231396133626461613537333339662020402063202d2062376331643433302d626266312d333134372d613230622d353532303738643934373535206d617468202166326331323030393334393833326432203020302e30202d3120312e30206164642030204020632063632063393337646665382d383339362d333636332d383564632d353661646361313364343537206d617468202166326331323030393334393833326432203120302e302039203232302e30206d756c7469706c792030204020612063612061323162366535632d306232622d336264362d616665382d616330333265633337353438206f73632021366631633066333636636362343766642032203434302e3020776176652073696e65202d31204020612061632035303763636162662d313534382d333436622d626163662d64316538363864373930663020696e7374616e6365636f6d62696e65202162336137363439363239303034363932203320302e30203620312e30207765696768746564204020612061612031656639633563612d643962362d333538372d616135392d636530323364366362313832206d617468202166326331323030393334393833326432203120302e393939393939393939393939393638322038202d312e3020706f7765722030204020612063612062393337613131382d333734312d333662342d623932612d366463316239333231346665206d617468202166326331323030393334393833326432202d3120312e30203520322e30206d756c7469706c792030204020612063612039356133306633662d323139322d333130622d396430352d626362326233663465306639206d61746820216632633132303039333439383332643220313020312e30202d31202d312e30206d756c7469706c792030204020612061632034383762386237362d356531662d333636382d613431622d666236303461626663323661206d617468202166326331323030393334393833326432202d3120302e30203720312e30206164642030204020612063612031346466376330342d356235662d333133322d613438362d62656661383631376232366620756e6974636f6e76657274202137663762633135663265623932333337203132203434302e3020666163746f7220666163746f722040206120612061663564313462632d663434382d336563372d393066642d63326461366139316432626120756e6974636f6e7665727420213766376263313566326562393233333720313320302e3020666163746f7220666163746f722040206120612035383463363233312d373533662d333135382d386233332d656335313665356262613161206d756c7469706c65696e707574202139353639376465386131353533326634203020402061202d2066353837646536302d613232622d333031662d616639362d393433333964616139313338206d756c7469706c65696e707574202139353639376465386131353533326634203120402061202d2037353963366362632d656532382d333766382d393639392d323033313033316637663966206d756c7469706c65696e707574202139353639376465386131353533326634203220402061202d");MultipleGraphNode::configure(setup);sources=p.sources();
        defaults[0]=static_cast<float>(p.input_0_value);
        defaults[1]=static_cast<float>(p.input_1_value);
        defaults[2]=static_cast<float>(p.input_2_value);
    }
    void process(ProcessContext& c)noexcept override{
        std::array<const float*,9> left{},right{};std::array<char,9> rates{};rates.fill('c');
        left[0]=c.input(0);if(!left[0])left[0]=&defaults[0];
        right[0]=c.rightInputs&&0<c.inputCount?c.rightInputs[0]:c.input(0);if(!right[0])right[0]=&defaults[0];
        rates[0]=c.input(0)?(c.sourceRates?c.sourceRates[0]:'a'):'c';
        left[1]=c.input(1);if(!left[1])left[1]=&defaults[1];
        right[1]=c.rightInputs&&1<c.inputCount?c.rightInputs[1]:c.input(1);if(!right[1])right[1]=&defaults[1];
        rates[1]=c.input(1)?(c.sourceRates?c.sourceRates[1]:'a'):'c';
        left[2]=c.input(2);if(!left[2])left[2]=&defaults[2];
        right[2]=c.rightInputs&&2<c.inputCount?c.rightInputs[2]:c.input(2);if(!right[2])right[2]=&defaults[2];
        rates[2]=c.input(2)?(c.sourceRates?c.sourceRates[2]:'a'):'c';
        auto inner=c;inner.inputs=left.data();inner.rightInputs=right.data();inner.sourceRates=rates.data();inner.inputCount=9;
        if(audioKernel.process<CompiledInstance>(*this,inner))return;
        if(inner.channelIndex==0){audioKernel.invalidate();for(auto& instance:state->instances)static_cast<CompiledInstance&>(*instance).scalarCacheValid=false;}
        processInstances<CompiledInstance>(inner);
    }
};
EASYKNACK_NODE(BakedGroup6920b561e015,"baked_6920b561e015")
}
