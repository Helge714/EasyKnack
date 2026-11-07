#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../LorisStream.hpp"
#include "../LorisWorker.hpp"
#include <cstdio>
namespace easyknack {
struct PartialAnalysisNode : NodeProcessor {
    PartialsResource partials;SpectrumStream residual;LorisStream::Settings settings;
    std::size_t limit=64;
    std::unique_ptr<LorisStream> analyzer;
    std::unique_ptr<LorisWorker> worker;
    const SpectrumStream* pending=nullptr;
    std::uint64_t identity=0;bool reported=false;
    char executionRate()const noexcept override{return 'a';}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    std::string parameterStructure()const override{return std::to_string(limit);}
    std::size_t resourceCapacityHint(ResourceKind k)const noexcept override{return k==ResourceKind::partials?limit:0;}
    void applyParameters(const NodeProcessor& p)noexcept override{settings=static_cast<const PartialAnalysisNode&>(p).settings;}
    ResourceKind inputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::spectrum:ResourceKind::none;}
    std::size_t outputCount()const noexcept override{return 2;}
    ResourceKind outputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::partials:ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t p)noexcept override{return p==0?static_cast<DspResource*>(&partials):&residual;}
    void configure(std::istream& in)override{
        // Previously exported plugins embed old parameter strings. Consume those
        // contracts without bringing their experimental classifier back.
        auto start=in.tellg();std::string marker;in>>marker;
        if(marker=="!fa4a30260a8836d4"||marker=="!944a705e1b770686"||marker=="!23333a42ca0bbca2"){
            sources={readInput(in)};settings.threshold=readNumber(in);
            limit=std::size_t(readNumber(in,1,32768));settings.minimum=readNumber(in);settings.maximum=readNumber(in);
            readNumber(in); // retired tracking tolerance
            if(marker=="!fa4a30260a8836d4"){settings.bandwidth=0;settings.duration=0;return;}
            if(marker=="!944a705e1b770686"){
                double shape=readNumber(in);readNumber(in);readNumber(in);double confidence=readNumber(in);
                settings.bandwidth=shape>0||confidence>0?2:0;
            }else{settings.bandwidth=readNumber(in)!=0?2:0;readNumber(in);}
            settings.duration=readNumber(in);readNumber(in);readNumber(in);
            sources.push_back(readInput(in));readNumber(in);readNumber(in);readNumber(in);readNumber(in);
            return;
        }
        in.seekg(start);
        auto p=parameters::PartialAnalysisNodeParameters::read(in);sources=p.sources();
        settings.threshold=p.threshold_value;limit=std::size_t(p.maximum_partials_value);
        settings.minimum=p.minimum_value;settings.maximum=p.maximum_value;settings.resolution=p.resolution_value;
        settings.drift=p.drift_value;settings.duration=p.duration_value;settings.convergence=p.convergence_value;settings.region=p.region_value;
        settings.bandwidth=p.bandwidth_value=="none"?0:p.bandwidth_value=="residue"?1:2;
    }
    void prepare(double,std::size_t block)override{
        partials.prepare(block,limit);residual.prepare(block);
        analyzer=std::make_unique<LorisStream>();worker=std::make_unique<LorisWorker>();
    }
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& old=static_cast<PartialAnalysisNode&>(previous);
        if(limit==old.limit){analyzer.swap(old.analyzer);identity=old.identity;reported=old.reported;}
    }
    static void run(void* data)noexcept{static_cast<PartialAnalysisNode*>(data)->analyze();}
    void analyze()noexcept{
        try{
            if(identity!=pending->identity){analyzer->reset();identity=pending->identity;}
            for(std::size_t f=0;f<partials.count;f++){
                const auto& source=pending->frames[f];auto& frame=partials.frames[f];frame.start=source.start;frame.count=0;
                if(source.start<0)continue;
                analyzer->analyze(*pending,source,settings,frame);
                std::sort(frame.values.begin(),frame.values.begin()+frame.count,[](const Partial& a,const Partial& b){return a.amplitude>b.amplitude;});
                for(std::size_t j=0;j<frame.count;j++){
                    const auto& p=frame.values[j];
                    addPartial(residual.frames[f],pending->size,pending->windowFrames(),pending->sampleRate,pending->window,p.frequency,p.amplitude,p.phase,-1);
                }
                SpectrumStream::realSymmetry(residual.frames[f],pending->size);
            }
            reported=false;
        }catch(const std::exception& e){
            if(!reported){std::fprintf(stderr,"Loris Partial Analysis failed: %s\n",e.what());reported=true;}
            for(std::size_t f=0;f<partials.count;f++)partials.frames[f].count=0;
            residual.copy(*pending);analyzer->reset();
        }catch(...){
            if(!reported){std::fprintf(stderr,"Loris Partial Analysis failed: unknown exception\n");reported=true;}
            for(std::size_t f=0;f<partials.count;f++)partials.frames[f].count=0;
            residual.copy(*pending);analyzer->reset();
        }
    }
    void process(ProcessContext& c)noexcept override{
        auto* input=static_cast<const SpectrumStream*>(c.resource(0));
        if(!input||!input->size){partials.size=partials.count=residual.size=residual.count=0;identity=0;return;}
        partials.metadata(*input);residual.copy(*input);pending=input;
        if(partials.count){
            // Loris owns allocating containers: compute on a dedicated prepared
            // helper and join to preserve the existing FFT/IFFT frame clock.
            worker->execute(run,this);
        }
    }
};
EASYKNACK_NODE(PartialAnalysisNode,"partialanalysis")
}
