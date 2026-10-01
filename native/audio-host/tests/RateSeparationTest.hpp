#pragma once
void rateSeparation(){
    std::array<float,8> audio{0,1,2,3,4,5,6,7},out{},silence{};float control=2;
    const float* inputs[]{audio.data(),&control};char rates[]{'a','c'};
    ProcessContext c{inputs,2,out.data(),8,48000,silence.data(),8};c.sourceRates=rates;
    MathNode math;math.operation=MathOperation::multiply;math.process(c);
    for(int i=0;i<8;i++)assert(out[i]==2*i);
    control=0;math.process(c);for(float v:out)assert(v==0); // no smoothing of a connected scalar
    rates[0]='c';control=4;audio[0]=3;math.process(c);for(float v:out)assert(v==12);
    inputs[0]=nullptr;inputs[1]=nullptr;math.aValue=1;math.bValue=1;math.currentA=0;math.currentB=1;
    c.bufferSize=1;c.blockFrames=256;math.process(c);assert(out[0]>0&&out[0]<1);
    float before=out[0];math.process(c);assert(out[0]>before&&out[0]<1);
    ValueNode value,updated;std::istringstream initial("0"),next("1");value.configure(initial);updated.configure(next);value.applyParameters(updated);
    c.bufferSize=8;value.process(c);assert(out[0]>0&&out[0]<1);for(float v:out)assert(v==out[0]);

    c.bufferSize=c.blockFrames=8;rates[0]='a';rates[1]='e';inputs[0]=audio.data();
    for(int i=0;i<8;i++)audio[i]=static_cast<float>(i);
    ValueEventBuffer triggers;triggers.clear(8);triggers.push(2,1,ValueEventKind::trigger);triggers.push(3,1,ValueEventKind::trigger);triggers.push(3,1,ValueEventKind::trigger);
    const ValueEventBuffer* events[]{nullptr,&triggers};c.valueEventInputs=events;
    SampleHoldNode hold;hold.process(c);assert(out[1]==0&&out[2]==2&&out[3]==3&&out[7]==3);
    TriggerPulseNode pulse;events[0]=&triggers;pulse.process(c);assert(out[1]==0&&out[2]==1&&out[3]==1&&out[4]==0);
    events[0]=events[1]=nullptr;c.valueEventInputs=nullptr;rates[1]='c';control=3.5;inputs[1]=&control;
    ValueEventBuffer edges;edges.clear(8);ValueEventBuffer* outputs[]{&edges};c.valueEventOutputs=outputs;
    EdgeDetectorNode edge;edge.process(c);assert(edges.count==1&&edges.events[0].sampleOffset==4&&edges.events[0].kind==ValueEventKind::trigger);
    edge.initialized=false;rates[0]='c';edges.clear(8);edge.process(c);assert(edges.count==0); // audio buffer sampled only at zero

    // The same generator loop consumes a scalar or full-rate modulation through the host view.
    DigitalNoiseNode noise;noise.initialized=true;noise.currentFactor=1;
    float frequency=1000,strength=0;
    inputs[0]=&frequency;inputs[1]=&strength;rates[0]=rates[1]='c';c.valueEventOutputs=nullptr;
    noise.process(c);assert(std::abs(noise.phase-8.0*1000/48000)<1e-12);
    for(int i=0;i<8;i++)audio[i]=1000.f*(i+1);
    inputs[0]=audio.data();rates[0]='a';noise.phase=0;
    noise.process(c);assert(std::abs(noise.phase-36000.0/48000)<1e-12);
    rates[0]='c';noise.phase=0;noise.process(c);
    assert(std::abs(noise.phase-8.0*1000/48000)<1e-12);

    struct TwoOutputs : MathNode {std::size_t outputCount()const noexcept override{return 2;}};
    struct MixedPolicies : BakedGraphProcessor {
        void configure(std::istream&)override{
            configureGraph("GRAPH 4 in baked_input 0 0 0 @ a - op pair 0 0 -1 1 multiply 0 @ ac aa first baked_output 1 0 @ a a second baked_output 257 0 @ c c",
                [](const std::string& id)->std::unique_ptr<NodeProcessor>{
                    if(id=="pair")return std::make_unique<TwoOutputs>();
                    if(id=="baked_input")return std::make_unique<BakedInput>();
                    return std::make_unique<BakedOutput>();
                },{0},{2,3},{1});
            runtime->outputPolicies[1]="?c";
        }
        void renderGraph(int,AudioIO*,MidiSource*)noexcept override{}
    } baked;
    std::istringstream config("");baked.configure(config);char source='a';
    assert(baked.resolveAutoRate(&source,1)=='a');assert(baked.runtime->graph.nodes[1].outputRates=="ac");
    source='c';assert(baked.resolveAutoRate(&source,1)=='c');assert(baked.runtime->graph.nodes[1].outputRates=="cc");
    source='a';baked.runtime->graph.nodes[1].inputRates[0]='c';
    assert(baked.resolveAutoRate(&source,1)=='c'); // Explicit sampling survives a bake.
}
