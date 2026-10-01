#pragma once
void harmonizerTests(){
    std::array<float,1024> out{},valid{},silence{};float input=0,rise=.01f,fall=.02f;
    const float* inputs[]{&input,&rise,&fall};char rates[]{'c','c','c'};
    ProcessContext c{inputs,3,out.data(),1,48000,silence.data(),1};c.sourceRates=rates;
    SlewNode audio;audio.process(c);input=1;c.bufferSize=480;c.blockFrames=480;audio.process(c);
    double risen=1-std::exp(-1.0);assert(std::abs(out[479]-risen)<1e-6);
    for(int i=1;i<480;i++)assert(out[i]>=out[i-1]&&out[i]<=1);
    SlewNode control;control.initialized=true;control.current=0;c.bufferSize=1;control.process(c);assert(std::abs(out[0]-risen)<1e-6);
    input=0;c.bufferSize=960;c.blockFrames=960;audio.process(c);assert(std::abs(out[959]-risen*std::exp(-1.0))<1e-6);
    SlewNode adopted;adopted.inheritState(audio);assert(adopted.current==audio.current&&adopted.initialized);
    input=10;rise=0;c.bufferSize=17;c.blockFrames=17;adopted.process(c);for(int i=0;i<17;i++)assert(out[i]==10);
    input=std::numeric_limits<float>::quiet_NaN();adopted.process(c);for(int i=0;i<17;i++)assert(out[i]==10);
    // Same elapsed time, independent of block partition and sample rate.
    for(double sampleRate:{44100.,48000.,96000.}){
        c.sampleRate=sampleRate;input=1;rise=.1f;SlewNode partitioned;partitioned.initialized=true;
        int total=static_cast<int>(sampleRate*.1);for(int done=0;done<total;){int n=std::min(137,total-done);c.bufferSize=n;c.blockFrames=n;partitioned.process(c);done+=n;}
        assert(std::abs(partitioned.current-risen)<1e-6);
    }
    c.sampleRate=48000;c.bufferSize=1;c.blockFrames=256;
    PitchHoldNode hold;float f=660,confidence=.9f,threshold=.8f;inputs[0]=&f;inputs[1]=&confidence;inputs[2]=&threshold;
    float* outputs[]{out.data(),valid.data()};c.outputs=outputs;c.outputCount=2;
    hold.process(c);assert(out[0]==660&&valid[0]==1);
    f=123;confidence=.1f;hold.process(c);assert(out[0]==660&&valid[0]==0);
    f=0;confidence=1;hold.process(c);assert(out[0]==660&&valid[0]==0);
    f=std::numeric_limits<float>::infinity();hold.process(c);assert(out[0]==660&&valid[0]==0);
    f=440;confidence=threshold;hold.process(c);assert(out[0]==440&&valid[0]==1);
    PitchHoldNode inherited;inherited.inheritState(hold);f=0;inherited.process(c);assert(out[0]==440);
    PitchHoldNode initial;initial.initial=220;initial.process(c);assert(out[0]==220&&valid[0]==0);
    ScaleQuantizerNode q;assert(std::abs(q.quantize(445)-440)<1e-5);assert(q.quantize(0)==0);assert(q.quantize(NAN)==0);
    auto hz=[](double midi){return float(440*std::exp2((midi-69)/12));};
    q.mask=ScaleQuantizerNode::scaleMask("major");
    assert(std::abs(q.quantize(hz(61.1))-hz(62))<1e-4);assert(std::abs(q.quantize(hz(60.9))-hz(60))<1e-4);
    q.root=2;assert(std::abs(q.quantize(hz(65.8))-hz(66))<1e-4);
    for(auto name:{"chromatic","major","minor","major_pentatonic","minor_pentatonic"})for(int root=0;root<12;root++){
        q.mask=ScaleQuantizerNode::scaleMask(name);q.root=root;
        for(int n=12;n<108;n++){float result=q.quantize(hz(n+.2));double note=69+12*std::log2(result/440.0);int rounded=int(std::round(note));assert(std::abs(note-rounded)<1e-5);assert(q.mask&(1u<<((rounded-root+120)%12)));}
    }
    q.fixed=true;q.target=432;inputs[0]=inputs[1]=nullptr;c.inputCount=2;c.outputCount=1;c.outputs=nullptr;q.process(c);assert(out[0]==432);
    ScaleQuantizerNode stable;inputs[0]=&f;inputs[1]=inputs[2]=nullptr;c.inputCount=3;
    auto step=[&](double note){f=hz(note);stable.process(c);return out[0];};
    assert(std::abs(step(69)-hz(69))<1e-4);
    for(double n:{69.49,69.51,69.59,69.45})assert(std::abs(step(n)-hz(69))<1e-4);
    assert(std::abs(step(69.61)-hz(70))<1e-4);
    for(double n:{69.51,69.49,69.41})assert(std::abs(step(n)-hz(70))<1e-4);
    assert(std::abs(step(69.39)-hz(69))<1e-4);
    assert(std::abs(step(81)-hz(81))<1e-4); // Large changes are not limited to one note per block.
    stable.hysteresis=0;assert(std::abs(step(69.51)-hz(70))<1e-4);assert(std::abs(step(69.49)-hz(69))<1e-4);
    stable.hysteresis=.1f;ScaleQuantizerNode handover;handover.inheritState(stable);assert(handover.hasNote&&handover.heldNote==69);
    ScaleQuantizerNode changed;changed.root=2;handover.applyParameters(changed);assert(!handover.hasNote);
    f=NAN;stable.process(c);assert(out[0]==0&&!stable.hasNote);
    stable.mask=ScaleQuantizerNode::scaleMask("major");step(60);
    assert(std::abs(step(61.09)-hz(60))<1e-4);assert(std::abs(step(61.11)-hz(62))<1e-4);
    ScaleQuantizerNode legacy;std::istringstream old("!d1b673b46ada3566 -1 440 -1 440 scale 0 chromatic 440");legacy.configure(old);assert(legacy.hysteresis==0&&legacy.sources.size()==2);
    // Control and audio evaluations use the same pitch thresholds.
    stable.mask=4095;stable.hasNote=false;step(69);std::array<float,4> notes{hz(69.55),hz(69.61),hz(69.45),hz(69.39)};
    inputs[0]=notes.data();rates[0]='a';c.bufferSize=4;stable.process(c);
    for(int i=0;i<4;i++)assert(std::abs(out[i]-hz(i==1||i==2?70:69))<1e-4);
    // Snapshot transport is throttled and never modifies the pass-through target.
    PitchMonitorNode monitor;monitor.bindIdentity("test-pitch-monitor");
    float detected=660,targetHz=440,certainty=.9f;inputs[0]=&detected;inputs[1]=&targetHz;inputs[2]=&certainty;rates[0]='c';c.bufferSize=1;c.blockFrames=256;
    monitor.process(c);assert(out[0]==440);AnalysisFrame frame;assert(monitor.mailbox->latest(frame));assert(frame.frequency==660&&frame.values[0]==440&&frame.confidence==certainty&&frame.valueCount==1);
    monitor.process(c);assert(!monitor.mailbox->latest(frame));
}
