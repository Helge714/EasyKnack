#pragma once
void positionReaderTests(){
    for(float speed:{440.f/660.f,-1.f,2.f,0.f}){
        HistoryResource history;history.prepare(48000,8192.0/48000,256);
        HistoryReaderNode clock;clock.speed=speed;PositionReaderNode follower;follower.offset=.5f;
        std::array<float,256> audio{},distance{},position{},other{},otherDistance{},otherPosition{},silence{};
        const float* inputs[]{nullptr,nullptr,nullptr,nullptr};const DspResource* resources[]{&history,nullptr,nullptr,nullptr};
        float* outputs[]{audio.data(),distance.data(),position.data()};float* otherOutputs[]{other.data(),otherDistance.data(),otherPosition.data()};
        ProcessContext c{inputs,4,audio.data(),256,48000,silence.data(),256};c.resourceInputs=resources;c.outputCount=3;c.outputs=outputs;
        const float* following[]{nullptr,position.data(),nullptr};char rates[]{'c','a','c'};
        auto f=c;f.inputs=following;f.inputCount=3;f.output=other.data();f.outputs=otherOutputs;f.sourceRates=rates;
        double previous=0,maxJump=0;
        for(unsigned block=0;block<700;block++){
            history.beginBlock(256);for(unsigned i=0;i<256;i++)history.append(std::sin(6.283185307179586*660*(block*256+i)/48000));
            clock.process(c);
            if(block<50)continue; // Follower can be attached after the clock is already running.
            follower.process(f);
            for(unsigned i=0;i<256;i++){
                assert(position[i]>=0&&position[i]<1&&otherPosition[i]>=0&&otherPosition[i]<1);
                assert(std::abs(std::abs(position[i]-otherPosition[i])-.5)<2e-7);
                assert(std::abs(distance[i]+otherDistance[i]-1)<2e-7);
                double mixed=MixNode::evaluate(audio[i],other[i],otherDistance[i]);
                if(block>51)maxJump=std::max(maxJump,std::abs(mixed-previous));previous=mixed;
            }
        }
        if(speed==440.f/660.f)assert(maxJump<.06); // The user's 660 -> 440 Hz crossfade has no reset clicks.
        // Control-rate position is read once per block, with the same circular addressing.
        position[0]=1.25f;rates[1]='c';follower.process(f);for(float p:otherPosition)assert(p==.75f);
        following[1]=nullptr;follower.position=.25f;follower.process(f);for(float p:otherPosition)assert(p==.75f);
        following[1]=position.data();position[0]=std::numeric_limits<float>::quiet_NaN();follower.process(f);for(float p:otherPosition)assert(p==.5f);
        resources[0]=nullptr;follower.process(f);for(float sample:other)assert(sample==0);for(float p:otherPosition)assert(p==0);
    }
    HistoryReaderNode legacy;std::istringstream payload("!e0e39780cef2821c normalized-offset-v2 -1 -1 1 -1 0 -1");legacy.configure(payload);
    assert(legacy.legacyOutputLayout&&legacy.sources.size()==4); // Old plugins never reinterpret position as trigger events.
}
