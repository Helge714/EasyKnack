#pragma once
void spectrumTests(){
    constexpr double tau=6.283185307179586476925286766559;
    auto job=std::make_shared<AnalysisJob>();
    // Mix is a mono-compatible average, including independent polyphonic channels.
    {
        SpectrumNode mix;mix.mix=true;mix.size=mix.hop=256;mix.window=SpectrumWindow::rectangular;
        mix.all=false;mix.synchronous=false;mix.prepare(48000,256);mix.job=std::make_shared<AnalysisJob>();
        std::array<float,256> left{},right{},out{},silent{};left.fill(.25f);right.fill(.75f);
        const float* ins[]{left.data()};const float* rhs[]{right.data()};
        ProcessContext ctx{ins,1,out.data(),256,48000,silent.data(),256};ctx.rightInputs=rhs;
        mix.process(ctx);mix.job->work();ctx.bufferSize=0;mix.process(ctx);
        assert(std::abs(mix.resource.result->views[0][0]-.5)<1e-7);
        right.fill(-.25f);ctx.bufferSize=256;mix.process(ctx);mix.job->work();ctx.bufferSize=0;mix.process(ctx);
        assert(mix.resource.result->views[0][0]==0);
        ctx.rightInputs=nullptr;ctx.bufferSize=256;mix.process(ctx);mix.job->work();ctx.bufferSize=0;mix.process(ctx);
        assert(std::abs(mix.resource.result->views[0][0]-.25)<1e-7);
        SpectrumNode selected;selected.size=selected.hop=256;selected.window=mix.window;
        mix.applyParameters(selected);assert(mix.end==0&&!mix.mix&&mix.resource.result==nullptr);
        std::istringstream old("!d4eac824a0d8961f -1 2048 hann 0.75 right double 1");
        selected.configure(old);assert(selected.right&&!selected.mix&&selected.padded&&selected.removeDc);
        std::istringstream current("!d4eac824a0d8961f -1 2048 hann 0.75 mix double 1");
        selected.configure(current);assert(selected.mix&&!selected.right&&selected.padded&&selected.removeDc);
        OscilloscopeNode scope;std::istringstream scopeConfig("!b1e724b051579b12 -1 -0.1 0.1 6 mix");scope.configure(scopeConfig);
        assert(scope.mix);scope.bindIdentity("mix-scope-regression");left.fill(.2f);right.fill(.8f);ctx.rightInputs=rhs;ctx.bufferSize=256;
        for(int i=0;i<8;i++)scope.process(ctx);
        auto captured=std::make_unique<AnalysisFrame>();assert(scope.mailbox->latest(*captured));
        for(unsigned i=0;i<captured->valueCount;i++)assert(std::abs(captured->values[i]-.5)<1e-6);
    }
    // Reuse complex FFT coefficients; compare every lag to a direct reference.
    auto correlationJob=std::make_shared<AnalysisJob>();
    {
        auto average=std::make_shared<AnalysisJob>();auto producer=std::make_shared<AnalysisJob>();
        std::array<float,256> pulse{};pulse[0]=1;
        auto frame=[&](std::uint64_t end,std::uint64_t id,double factor){
            assert(producer->submitSpectrum(pulse.data(),0,256,256,SpectrumWindow::rectangular,48000,end,id,true));producer->work();
            assert(average->submitCorrelation(producer->acquire()->spectrum,false,0,factor));average->work();return average->acquire()->views[3][0];
        };
        assert(frame(256,900,.5)==1); // Initialize from the first real curve, not zeros.
        pulse.fill(0);assert(frame(512,900,.5)==.5f);assert(frame(768,900,.5)==.25f);
        assert(frame(768,900,.1)==.25f); // Parameter edits cannot smooth the same snapshot twice.
        assert(frame(1024,900,0)==.25f);assert(frame(1280,900,1)==0);
        pulse[0]=1;assert(frame(1536,900,0)==0);assert(frame(256,901,0)==1); // New source resets even while held.
        SpectrumCorrelationNode compatibility;std::istringstream old("!998d05d9c008fe0e -1 unbiased");
        compatibility.configure(old);assert(compatibility.unbiased&&compatibility.smoothing==1);
    }
    std::array<float,256> reference{};
    for(unsigned i=0;i<256;i++)reference[i]=static_cast<float>(.2+std::sin(tau*7*i/256)+.13*std::cos(tau*31*i/256));
    for(bool padded:{false,true})for(auto window:{SpectrumWindow::rectangular,SpectrumWindow::hann}){
        assert(job->submitSpectrum(reference.data(),0,256,64,window,48000,256,123,padded));job->work();
        auto* fft=job->acquire();assert(fft->spectrum.fftFrames==(padded?512u:256u));
        assert(correlationJob->submitCorrelation(fft->spectrum));correlationJob->work();
        auto* result=correlationJob->acquire();assert(result&&result->availableViews==8&&!result->spectrum.valid());
        assert(result->correlationCount==256&&result->historyIdentity==123&&result->endSample==256);
        double energy=0;for(unsigned i=0;i<256;i++)energy+=std::pow(reference[i]*spectrumWindowValue(window,i,256),2);
        for(unsigned lag=0;lag<256;lag++){
            double sum=0;for(unsigned i=0;i<(padded?256-lag:256);i++){
                unsigned k=(i+lag)%256;sum+=reference[i]*spectrumWindowValue(window,i,256)*reference[k]*spectrumWindowValue(window,k,256);
            }
            assert(std::abs(result->views[3][lag]-sum/energy)<1e-6);
        }
    }
    // Largest padded FFT includes Nyquist and retains exact input-window metadata.
    std::array<float,8192> large{};for(unsigned i=0;i<8192;i++)large[i]=i%2?-.4f:.4f;
    assert(job->submitSpectrum(large.data(),0,8192,2048,SpectrumWindow::rectangular,48000,8192,124,true));job->work();
    auto* paddedResult=job->acquire();assert(paddedResult->binCount==8193&&paddedResult->spectrum.fftFrames==16384);
    assert(std::abs(paddedResult->views[0][8192]-.4)<1e-6);
    for(unsigned i=0;i<8192;i++)large[i]=static_cast<float>(std::sin(tau*440*i/48000));
    assert(job->submitSpectrum(large.data(),0,8192,2048,SpectrumWindow::rectangular,48000,8192,125,true));job->work();
    assert(correlationJob->submitCorrelation(job->acquire()->spectrum));correlationJob->work();
    PitchDetectionNode pitch;pitch.detect(*correlationJob->acquire());assert(std::abs(pitch.frequency-440)<1&&pitch.confidence>.95);
    large.fill(0);assert(job->submitSpectrum(large.data(),0,8192,2048,SpectrumWindow::rectangular,48000,8192,126,true));job->work();
    assert(correlationJob->submitCorrelation(job->acquire()->spectrum));correlationJob->work();
    pitch.detect(*correlationJob->acquire());assert(pitch.frequency==0&&pitch.confidence==0);
    // Exactly the old DC-centered, overlap-corrected curve, including far-edge lags.
    auto legacyCorrelation=std::make_unique<AutocorrelationNode>();
    for(unsigned i=0;i<8192;i++)large[i]=static_cast<float>(.7+std::sin(tau*440*i/48000)+.2*std::cos(tau*713*i/48000));
    legacyCorrelation->samples=large;legacyCorrelation->filledSamples=8192;legacyCorrelation->analyze(48000);
    assert(job->submitSpectrum(large.data(),0,8192,4096,SpectrumWindow::rectangular,48000,8192,127,true,true));job->work();
    auto* centered=job->acquire();assert(centered->spectrum.dcRemoved&&std::abs(centered->spectrum.bins[0])<1e-8);
    assert(correlationJob->submitCorrelation(centered->spectrum,true));correlationJob->work();
    auto* unbiased=correlationJob->acquire();
    for(unsigned i=0;i<8192;i++)assert(std::abs(unbiased->views[3][i]-legacyCorrelation->correlation[i])<2e-5);
    // Constant DC becomes exact silence; defaults still retain DC.
    large.fill(.7f);assert(job->submitSpectrum(large.data(),0,8192,4096,SpectrumWindow::rectangular,48000,8192,128,true,true));job->work();
    assert(correlationJob->submitCorrelation(job->acquire()->spectrum,true));correlationJob->work();
    for(unsigned i=0;i<8192;i++)assert(correlationJob->acquire()->views[3][i]==0);
    std::array<float,8192> samples{};
    std::array<std::complex<double>,8192> inverse{};
    for(std::size_t size:{256u,512u,1024u,2048u,4096u,8192u})
        for(auto window:{SpectrumWindow::rectangular,SpectrumWindow::hann,SpectrumWindow::hamming,SpectrumWindow::blackman}){
            for(std::size_t i=0;i<size;i++)samples[i]=static_cast<float>(.2+.7*std::cos(tau*13*i/size+.4)+.1*(i%2?-1:1));
            assert(job->submitSpectrum(samples.data(),0,size,size/4,window,48000,size,71));job->work();
            auto* result=job->acquire();assert(result&&result->spectrum.valid());auto& frame=result->spectrum;
            assert(frame.windowFrames==size&&frame.fftFrames==size&&frame.hopFrames==size/4);
            assert(frame.startSample==0&&frame.endSample==size&&frame.sourceIdentity==71&&frame.frameIndex==0);
            assert(result->binCount==size/2+1&&result->binSpacingHz==48000.0/size);
            assert(std::abs(result->views[0][0]-.2)<1e-6); // DC is retained; never doubled.
            assert(std::abs(result->views[0][13]-.7)<1e-6);
            assert(std::abs(result->views[1][13]-.4)<1e-6);
            assert(std::abs(result->views[0][size/2]-.1)<1e-6); // Nyquist is included, not doubled.
            std::copy_n(frame.bins.data(),size,inverse.data());spectrumTransform(inverse.data(),size,true);
            for(std::size_t i=0;i<size;i++){
                assert(std::abs(inverse[i].real()-samples[i]*spectrumWindowValue(window,i,size))<1e-9);
                assert(std::abs(inverse[i].imag())<1e-9);
            }
        }
    // Inverse/weighted overlap-add feasibility, including absolute frame positions.
    std::array<double,2048> reconstructed{},weight{};constexpr std::size_t size=256,hop=64;
    for(std::size_t start=0;start+size<=2048;start+=hop){
        for(std::size_t i=0;i<size;i++)samples[i]=static_cast<float>(.3+std::sin(tau*173*(start+i)/48000));
        assert(job->submitSpectrum(samples.data(),0,size,hop,SpectrumWindow::hann,48000,start+size,81));job->work();
        auto& frame=job->acquire()->spectrum;assert(frame.startSample==start&&frame.frameIndex==start/hop);
        std::copy_n(frame.bins.data(),size,inverse.data());spectrumTransform(inverse.data(),size,true);
        for(std::size_t i=0;i<size;i++){double w=spectrumWindowValue(frame.window,i,size);reconstructed[start+i]+=inverse[i].real()*w;weight[start+i]+=w*w;}
    }
    for(std::size_t i=size;i<2048-size;i++)assert(std::abs(reconstructed[i]/weight[i]-(.3+std::sin(tau*173*i/48000)))<1e-7);

    // Scheduling is sample-based even when host blocks do not divide the hop size.
    SpectrumNode node;node.size=256;node.hop=64;node.window=SpectrumWindow::rectangular;node.right=true;node.all=false;node.synchronous=false;node.prepare(48000,128);node.job=std::make_shared<AnalysisJob>();
    std::array<float,128> left{},rightA{},rightB{},silence{},out{};rightA.fill(.25f);rightB.fill(.75f);
    const float* inputs[]{left.data(),left.data()};const float* rights[]{rightA.data(),rightB.data()};
    ProcessContext c{inputs,2,out.data(),17,48000,silence.data(),17};c.rightInputs=rights;
    for(int block=0;block<60;block++){
        c.bufferSize=block%2?91:17;node.process(c);node.job->work();
    }
    c.bufferSize=0;node.process(c);auto* result=node.resource.result;assert(result);
    assert(result->endSample<=node.end&&(result->endSample-node.size)%node.hop==0);
    assert(node.end-result->endSample<node.hop);assert(std::abs(result->views[0][0]-1)<1e-7);
    assert(result->spectrum.sourceIdentity==node.identity);
    // Independent consumers read the same immutable snapshot, without a View node.
    AnalysisResource resource;resource.result=result;const DspResource* resources[]{&resource};c.resourceInputs=resources;c.inputCount=1;c.bufferSize=1;
    SpectrumStatisticsNode stats;stats.view=0;stats.statistic=0;stats.process(c);assert(std::abs(out[0]-1)<1e-7);
    stats.view=1;stats.process(c);assert(std::abs(out[0])<1e-7);
    auto oldRing=node.ring.data();SpectrumNode replacement;replacement.size=node.size;replacement.hop=node.hop;replacement.window=node.window;replacement.right=node.right;replacement.all=false;replacement.synchronous=false;replacement.prepare(node.rate,128);
    replacement.adoptState(node);assert(replacement.ring.data()==oldRing&&replacement.resource.result==result);
    samples.fill(0);assert(job->submitSpectrum(samples.data(),0,256,64,SpectrumWindow::hann,48000,256,91));job->work();
    result=job->acquire();assert(result->statistics[0][0]==0&&result->binCount==129&&result->spectrum.sourceIdentity==91);
    // Bounded queue explicitly records dropped snapshots; the audio thread never waits.
    auto saturated=std::make_shared<AnalysisJob>();
    for(int i=0;i<3;i++)assert(saturated->submitSpectrum(samples.data(),0,256,64,SpectrumWindow::hann,48000,256+i*64,91));
    assert(!saturated->submitSpectrum(samples.data(),0,256,64,SpectrumWindow::hann,48000,448,91));assert(saturated->dropped==1);
    assert(SpectrumNode::hopFor(2048,.637)==743);assert(SpectrumNode::hopFor(2048,1)==1);assert(SpectrumNode::hopFor(2048,0)==2048);
    SpectrumNode legacy;std::istringstream old("!df5db079f2749a23 -1 2048 hann half left");legacy.configure(old);assert(legacy.hop==1024);
    auto retainedJob=replacement.job;auto retainedRing=replacement.ring.data();auto retainedEnd=replacement.end;
    SpectrumNode edited;edited.size=replacement.size;edited.window=replacement.window;edited.right=replacement.right;edited.hop=93;edited.all=false;edited.synchronous=false;
    replacement.applyParameters(edited);assert(replacement.hop==93&&replacement.end==retainedEnd&&replacement.ring.data()==retainedRing&&replacement.job==retainedJob);
    assert(replacement.resource.result==nullptr); // Old-window metadata must not masquerade as a new hop.
    auto correlation=std::make_unique<AutocorrelationNode>();correlation->prepare(48000,256);
    for(unsigned i=0;i<8192;i++)correlation->samples[i]=static_cast<float>(std::sin(tau*440*i/48000));
    correlation->filledSamples=8192;correlation->totalSamples=8192;correlation->analyze(48000);
    auto* shared=static_cast<AnalysisResource*>(correlation->resourceOutput(2));assert(shared&&shared->result&&shared->result->availableViews==8);
    assert(std::abs(shared->result->frequency-440)<2&&shared->view==AnalysisView::correlation);
    SpectrumViewerNode viewer;viewer.selectedView=0;viewer.bindIdentity("correlation-generic-viewer");
    const DspResource* correlationInputs[]{shared};c.resourceInputs=correlationInputs;c.channelIndex=0;viewer.process(c);
    auto plotted=std::make_unique<AnalysisFrame>();assert(viewer.mailbox->latest(*plotted));
    assert(plotted->symmetric&&plotted->valueCount==8192); // Incompatible old Magnitude selection follows actual source.
    for(unsigned i=0;i<8192;i++)assert(plotted->values[i]==correlation->correlation[i]);
}
