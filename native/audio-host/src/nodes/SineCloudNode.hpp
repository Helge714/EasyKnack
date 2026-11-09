#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../dsp/SineCloud.hpp"

namespace easyknack {
struct SineCloudNode:NodeProcessor {
    SineCloudTable::Settings settings;
    std::unique_ptr<SineCloudWorker> worker;
    double sampleRate=0;
    int active=-1,previous=-1;
    std::size_t cursor=0,oldCursor=0,fade=0,fadeLength=1;
    bool fading=false;
    std::size_t outputCount()const noexcept override{return 3;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override{
        auto start=in.tellg();std::string marker;in>>marker;
        // Preserve existing baked/source-plugin configurations from the first version.
        if(marker=="!2303724719fab0c8"||marker=="!19022bc74d228d07"){
            int a,b,c,d;unsigned count,seed;double lo,hi;
            if(!(in>>a>>count>>b>>lo>>c>>hi>>d>>seed)||a< -1||b< -1||c< -1||d< -1||count<1||count>(marker=="!2303724719fab0c8"?1000000u:10000000u)||seed>16777215||
                !std::isfinite(lo)||!std::isfinite(hi)||lo<.00001||lo>20000||hi<.00001||hi>20000)throw std::runtime_error("Invalid legacy Sine Cloud parameters");
            int gaussian=0;
            if(marker=="!19022bc74d228d07"&&(!(in>>gaussian)||(gaussian!=0&&gaussian!=1)))throw std::runtime_error("Invalid legacy Gaussian setting");
            sources={a,b,c,d};settings={count,seed,lo,hi,gaussian!=0,true};return;
        }
        in.clear();in.seekg(start);
        auto p=parameters::SineCloudNodeParameters::read(in);sources=p.sources();
        settings={unsigned(p.count_value),unsigned(p.seed_value),p.minimum_value,p.maximum_value,p.gaussian_value!=0,p.randomize_value!=0};
    }
    void applyParameters(const NodeProcessor& other)noexcept override{settings=static_cast<const SineCloudNode&>(other).settings;}
    void prepare(double rate,std::size_t)override{
        sampleRate=rate;fadeLength=std::max<std::size_t>(1,std::size_t(rate*.02));
        active=previous=-1;cursor=oldCursor=fade=0;fading=false;
        worker=std::make_unique<SineCloudWorker>(rate);worker->request(settings);
    }
    void adoptState(NodeProcessor& other)noexcept override{
        auto& old=static_cast<SineCloudNode&>(other);
        if(sampleRate!=old.sampleRate)return;
        worker.swap(old.worker);active=old.active;previous=old.previous;cursor=old.cursor;oldCursor=old.oldCursor;fade=old.fade;fading=old.fading;
        old.active=old.previous=-1;old.fading=false;
    }
    static double finite(float value,double fallback)noexcept{return std::isfinite(value)?double(value):fallback;}
    void process(ProcessContext& c)noexcept override{
        if(!worker){for(std::size_t port=0;port<3;port++)if(auto* out=c.outputAt(port))std::fill_n(out,c.bufferSize,0.f);return;}
        auto p=settings;
        p.count=unsigned(std::llround(std::clamp(finite(c.numericInput(0,float(p.count))[0],p.count),1.,double(SineCloudTable::maximumCount))));
        p.minimum=std::clamp(finite(c.numericInput(1,float(p.minimum))[0],p.minimum),.00001,sampleRate*.5);
        p.maximum=std::clamp(finite(c.numericInput(2,float(p.maximum))[0],p.maximum),.00001,sampleRate*.5);
        p.seed=unsigned(std::llround(std::clamp(finite(c.numericInput(3,float(p.seed))[0],p.seed),0.,16777215.)));
        auto wanted=worker->request(p);
        for(int i=0;i<3;i++)if(worker->slots[i].state.load()==SineCloudWorker::ready){
            if(worker->slots[i].version!=wanted){worker->slots[i].state.store(SineCloudWorker::free);continue;}
            if(!fading){
                previous=active;oldCursor=cursor;active=i;cursor=0;fade=0;fading=true;
                worker->slots[i].state.store(SineCloudWorker::playing);
            }
        }
        auto* countOut=c.outputAt(1);auto* periodOut=c.outputAt(2);
        for(std::size_t i=0;i<c.bufferSize;i++){
            float value=0;
            if(active>=0){const auto& table=worker->slots[active].table;value=table.samples[cursor];cursor=(cursor+1)%table.samples.size();}
            if(fading){
                float old=0;
                if(previous>=0){const auto& table=worker->slots[previous].table;old=table.samples[oldCursor];oldCursor=(oldCursor+1)%table.samples.size();}
                double mix=double(++fade)/fadeLength;value=float(old+(value-old)*mix);
                if(fade>=fadeLength){if(previous>=0)worker->slots[previous].state.store(SineCloudWorker::free);previous=-1;fading=false;}
            }
            c.output[i]=value;if(countOut)countOut[i]=active>=0?float(worker->slots[active].table.count):0.f;
            if(periodOut)periodOut[i]=active>=0?float(worker->slots[active].table.samples.size()/sampleRate):0.f;
        }
    }
};
EASYKNACK_NODE(SineCloudNode,"sinecloud")
}
