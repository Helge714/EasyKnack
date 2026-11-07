// Offline adapter: no SMS source is included in the host or this executable.
#include "nodes/PartialAnalysisNode.hpp"
#include <fstream>
#include <iomanip>
#include <iostream>
using namespace easyknack;
int main(int argc,char** argv){
    if(argc!=6){std::cerr<<"Usage: partial-reference samples.f64 window hop defaults|sinusoidal output.csv\n";return 2;}
    const auto length=std::stoul(argv[2]),hop=std::stoul(argv[3]);
    if(length<64||2*length>SpectrumStream::maxSize||!hop){std::cerr<<"Invalid frame dimensions\n";return 2;}
    std::ifstream file(argv[1],std::ios::binary|std::ios::ate);if(!file)return 2;
    const auto bytes=file.tellg();if(bytes<0||std::size_t(bytes)%sizeof(double))return 2;
    std::vector<double> samples(std::size_t(bytes)/sizeof(double));file.seekg(0);file.read(reinterpret_cast<char*>(samples.data()),bytes);if(!file)return 2;
    SpectrumStream input;input.prepare(256);input.size=2*length;input.windowSize=length;input.hop=hop;input.sampleRate=48000;input.identity=1;input.window=SpectrumWindow::hann;input.count=1;
    PartialAnalysisNode analysis;
    if(std::string(argv[4])=="sinusoidal")analysis.settings.bandwidth=0;
    else if(std::string(argv[4])!="defaults")return 2;
    analysis.prepare(48000,256);float silence[256]{},output[256]{};const DspResource* resources[]={&input,nullptr};
    ProcessContext c{};c.resourceInputs=resources;c.inputCount=2;c.bufferSize=256;c.sampleRate=48000;c.silence=silence;c.output=output;
    std::ofstream csv(argv[5]);if(!csv)return 2;
    csv<<std::setprecision(17)<<"start,frequency,amplitude,phase,id,tonality,residual_power_ratio,reconstruction_error\n";
    for(std::size_t start=0;start+length<=samples.size();start+=hop){
        auto& f=input.frames[0];f.start=start;input.blockStart=start+length-256;
        std::fill_n(f.bins.data(),input.size,std::complex<double>{});
        for(std::size_t i=0;i<length;i++)f.bins[i]=samples[start+i]*spectrumWindowValue(input.window,i,length);
        spectrumTransform(f.bins.data(),input.size);analysis.process(c);
        const auto& partials=analysis.partials.frames[0];
        StreamSpectrumFrame combined;combined.bins=analysis.residual.frames[0].bins;
        for(std::size_t i=0;i<partials.count;i++){const auto& p=partials.values[i];addPartial(combined,input.size,length,48000,input.window,p.frequency,p.amplitude,p.phase);}
        double before=0,after=0,error=0;
        for(std::size_t k=0;k<=input.size/2;k++){before+=std::norm(f.bins[k]);after+=std::norm(analysis.residual.frames[0].bins[k]);error=std::max(error,std::abs(combined.bins[k]-f.bins[k]));}
        double ratio=before>0?after/before:0;
        if(!partials.count)csv<<start<<",0,0,0,0,0,"<<ratio<<","<<error<<"\n";
        for(std::size_t i=0;i<partials.count;i++){const auto& p=partials.values[i];csv<<start<<","<<p.frequency<<","<<p.amplitude<<","<<p.phase<<","<<p.id<<","<<p.tonality<<","<<ratio<<","<<error<<"\n";}
    }
    return csv?0:2;
}
