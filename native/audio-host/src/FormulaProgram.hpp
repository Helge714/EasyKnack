#pragma once
#include <algorithm>
#include <cmath>
#include <sstream>
#include <string>
#include <vector>
#include <stdexcept>
#include <array>
#include "FormulaContext.hpp"
#include "TypedFormulaProgram.hpp"
namespace easyknack {
inline std::string formulaUnhex(const std::string& text){
    if(text=="-")return {};if(text.size()%2||text.size()>4000000)throw std::invalid_argument("Invalid formula encoding");
    auto digit=[](char c){if(c>='0'&&c<='9')return c-'0';if(c>='a'&&c<='f')return c-'a'+10;throw std::invalid_argument("Invalid formula hex");};
    std::string out;out.reserve(text.size()/2);for(std::size_t i=0;i<text.size();i+=2)out.push_back(char(digit(text[i])*16+digit(text[i+1])));return out;
}
struct FormulaProgram {
    TypedFormulaProgram typed; bool isTyped=false;
    enum Op {constant,input,output,copy,jump,jz,jnz,add,sub,mul,div,mod,eq,ne,lt,gt,le,ge,neg,not_,abs_,sqrt_,sin_,cos_,tan_,asin_,acos_,atan_,atan2_,exp_,log_,log2_,log10_,floor_,ceil_,round_,pow_,min_,max_,clamp,mix,sign,sampleRate,blockSize,sampleIndex,samplePosition,rand_,randSeed,bufferSize,bufferRead,bufferInfo,bufferTimeline,inputRate,stateLoad,stateStore,sineWave,sinh_,cosh_,tanh_,asinh_,acosh_,atanh_,expm1_,log1p_};
    struct Instruction {Op op;unsigned target,a,b,c;float value;};
    std::vector<Instruction> code;std::vector<float> registers;
    struct State {std::string name;unsigned type;float initial;};
    std::vector<State> states;std::array<float,64> stateValues{};
    void inheritState(const FormulaProgram& previous)noexcept{
        typed.inheritState(previous.typed);
        for(std::size_t i=0;i<states.size();i++)for(std::size_t j=0;j<previous.states.size();j++)
            if(states[i].name==previous.states[j].name&&states[i].type==previous.states[j].type&&states[i].initial==previous.states[j].initial){stateValues[i]=previous.stateValues[j];break;}
    }
    void read(const std::string& text,unsigned inputs,unsigned outputs){
        isTyped=text.rfind("FORM3 ",0)==0;if(isTyped){typed.read(text,inputs,outputs);states.clear();code.clear();return;}typed=TypedFormulaProgram{};
        static const std::vector<std::string> names={"constant","input","output","copy","jump","jz","jnz","add","sub","mul","div","mod","eq","ne","lt","gt","le","ge","neg","not","abs","sqrt","sin","cos","tan","asin","acos","atan","atan2","exp","log","log2","log10","floor","ceil","round","pow","min","max","clamp","mix","sign","sampleRate","blockSize","sampleIndex","samplePosition","rand","randSeed","bufferSize","bufferRead","bufferInfo","bufferTimeline","inputRate","stateLoad","stateStore","sineWave","sinh","cosh","tanh","asinh","acosh","atanh","expm1","log1p"};
        std::istringstream in(text);std::string version;unsigned size,count;
        if(!(in>>version>>size>>count)||(version!="FORM1"&&version!="FORM2")||size==0||size>20000||count>40000)throw std::invalid_argument("Invalid formula program");
        states.clear();stateValues.fill(0);
        if(version=="FORM2"){
            unsigned stateCount;if(!(in>>stateCount)||stateCount>64)throw std::invalid_argument("Invalid formula state count");
            states.reserve(stateCount);for(unsigned i=0;i<stateCount;i++){
                State state;if(!(in>>state.name>>state.type>>state.initial)||state.name.size()>65536||state.type>1||!std::isfinite(state.initial)||(state.type==1&&state.initial!=0&&state.initial!=1))throw std::invalid_argument("Invalid formula state");
                for(const auto& existing:states)if(existing.name==state.name)throw std::invalid_argument("Duplicate formula state");
                stateValues[i]=state.initial;states.push_back(std::move(state));
            }
        }
        registers.assign(size,0);code.clear();code.reserve(count);
        for(unsigned n=0;n<count;n++){
            std::string op;Instruction i{};if(!(in>>op>>i.target>>i.a>>i.b>>i.c>>i.value)||!std::isfinite(i.value))throw std::invalid_argument("Invalid formula instruction");
            auto at=std::find(names.begin(),names.end(),op);if(at==names.end())throw std::invalid_argument("Unknown formula opcode");i.op=Op(at-names.begin());
            if(i.op==jump||i.op==jz||i.op==jnz){if(i.target<=n||i.target>count||i.a>=size)throw std::invalid_argument("Invalid formula branch");}
            else if(i.op==output){if(i.target>=outputs||i.a>=size)throw std::invalid_argument("Invalid formula output");}
            else if(i.op==stateStore){if(i.target>=states.size()||i.a>=size)throw std::invalid_argument("Invalid formula state store");}
            else if(i.op==stateLoad){if(i.target>=size||i.a>=states.size())throw std::invalid_argument("Invalid formula state load");}
            else if(i.target>=size||i.b>=size||i.c>=size||(i.op==input?i.a>=inputs:i.a>=size))throw std::invalid_argument("Invalid formula register");
            if((i.op==bufferInfo||i.op==bufferTimeline||i.op==inputRate)&&(i.value<0||std::floor(i.value)!=i.value||i.value>=(i.op==bufferInfo?4:i.op==bufferTimeline?8:1)*inputs))throw std::invalid_argument("Invalid formula resource operand");code.push_back(i);
            if((i.op==bufferRead||i.op==bufferSize)&&(i.value<0||i.value>=inputs||std::floor(i.value)!=i.value))throw std::invalid_argument("Invalid formula buffer port");
            if((i.op==rand_||i.op==randSeed)&&(i.value<0||i.value>40000))throw std::invalid_argument("Invalid random call site");
        }
        std::string extra;if(in>>extra)throw std::invalid_argument("Trailing formula data");
    }
    void evaluate(const float* inputs,float* outputs,const FormulaContext& context={})noexcept{
        if(isTyped){typed.evaluate(inputs,outputs,context);return;}
        auto* r=registers.data();
        auto* state=context.state?context.state:stateValues.data();
        for(std::size_t pc=0;pc<code.size();){auto i=code[pc++];
            if(i.op==input){r[i.target]=inputs[i.a];continue;}if(i.op==constant){r[i.target]=i.value;continue;}
            if(i.op==stateLoad){r[i.target]=state[i.a];continue;}
            float a=r[i.a],b=i.b<registers.size()?r[i.b]:0,c=i.c<registers.size()?r[i.c]:0;
            switch(i.op){
                case sineWave:r[i.target]=context.sineWave(a);break;case stateLoad:break;case stateStore:state[i.target]=a;continue;
                case bufferInfo:r[i.target]=context.info(unsigned(i.value)/4,unsigned(i.value)%4);break;case bufferTimeline:r[i.target]=context.readTimeline(unsigned(i.value)/8,a,(unsigned(i.value)%8)/2,unsigned(i.value)%2);break;case inputRate:r[i.target]=context.rate(unsigned(i.value));break;case sampleRate:r[i.target]=float(context.sampleRate);break;case blockSize:r[i.target]=float(context.blockSize);break;
                case sampleIndex:r[i.target]=float(context.sampleIndex);break;case samplePosition:r[i.target]=float(context.samplePosition);break;
                case rand_:r[i.target]=context.random(unsigned(i.value),a,b);break;case randSeed:r[i.target]=context.random(unsigned(i.value),a,b,c);break;
                case bufferSize:r[i.target]=context.size(unsigned(i.value));break;case bufferRead:r[i.target]=context.read(unsigned(i.value),a,b!=0,c!=0);break;
                case jump:pc=i.target;continue;case jz:if(a==0)pc=i.target;continue;case jnz:if(a!=0)pc=i.target;continue;
                case output:outputs[i.target]=a;continue;case input:r[i.target]=inputs[i.a];continue;case constant:r[i.target]=i.value;continue;
                case copy:r[i.target]=a;break;case add:r[i.target]=a+b;break;case sub:r[i.target]=a-b;break;case mul:r[i.target]=a*b;break;case div:r[i.target]=a/b;break;case mod:r[i.target]=std::fmod(a,b);break;
                case eq:r[i.target]=a==b;break;case ne:r[i.target]=a!=b;break;case lt:r[i.target]=a<b;break;case gt:r[i.target]=a>b;break;case le:r[i.target]=a<=b;break;case ge:r[i.target]=a>=b;break;case neg:r[i.target]=-a;break;case not_:r[i.target]=!a;break;
                case abs_:r[i.target]=std::abs(a);break;case sqrt_:r[i.target]=std::sqrt(a);break;case sin_:r[i.target]=std::sin(a);break;case cos_:r[i.target]=std::cos(a);break;case tan_:r[i.target]=std::tan(a);break;case asin_:r[i.target]=std::asin(a);break;case acos_:r[i.target]=std::acos(a);break;case atan_:r[i.target]=std::atan(a);break;case atan2_:r[i.target]=std::atan2(a,b);break;
                case sinh_:r[i.target]=std::sinh(a);break;case cosh_:r[i.target]=std::cosh(a);break;case tanh_:r[i.target]=std::tanh(a);break;
                case asinh_:r[i.target]=std::asinh(a);break;case acosh_:r[i.target]=std::acosh(a);break;case atanh_:r[i.target]=std::atanh(a);break;
                case expm1_:r[i.target]=std::expm1(a);break;case log1p_:r[i.target]=std::log1p(a);break;
                case exp_:r[i.target]=std::exp(a);break;case log_:r[i.target]=std::log(a);break;case log2_:r[i.target]=std::log2(a);break;case log10_:r[i.target]=std::log10(a);break;case floor_:r[i.target]=std::floor(a);break;case ceil_:r[i.target]=std::ceil(a);break;case round_:r[i.target]=std::round(a);break;case pow_:r[i.target]=std::pow(a,b);break;case min_:r[i.target]=std::min(a,b);break;case max_:r[i.target]=std::max(a,b);break;case clamp:r[i.target]=std::min(std::max(a,b),c);break;case mix:r[i.target]=a+(b-a)*c;break;case sign:r[i.target]=(a>0)-(a<0);break;
            }
        }
    }
};
}
