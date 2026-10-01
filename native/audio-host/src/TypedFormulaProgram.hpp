#pragma once
#include "FormulaContext.hpp"
#include <array>
#include <sstream>
#include <string>
#include <vector>
#include <stdexcept>
namespace easyknack {
struct TypedFormulaProgram {
    enum Op {constant,input,output,copy,cast,load,store,jump,jz,jnz,not_,sampleRate,blockSize,sampleIndex,samplePosition,rand_,randSeed,bufferSize,bufferInfo,bufferRead,bufferTimeline,inputRate,sineWave,arithmetic};
    struct Instruction {Op op;unsigned kind,target,a,b,c;std::uint64_t value;FormulaOp math{};};
    struct State {std::string name;unsigned kind;std::uint64_t initial;};
    std::vector<Instruction> code;std::vector<std::uint64_t> registers;std::vector<State> states;std::array<std::uint64_t,64> values{};bool sampleContext=false;
    void inheritState(const TypedFormulaProgram& old)noexcept{for(std::size_t i=0;i<states.size();i++)for(std::size_t j=0;j<old.states.size();j++)if(states[i].name==old.states[j].name&&states[i].kind==old.states[j].kind&&states[i].initial==old.states[j].initial){values[i]=old.values[j];break;}}
    void read(const std::string& text,unsigned inputs,unsigned outputs){
        static const std::vector<std::string> ops={"constant","input","output","copy","cast","load","store","jump","jz","jnz","not","sampleRate","blockSize","sampleIndex","samplePosition","rand","randSeed","bufferSize","bufferInfo","bufferRead","bufferTimeline","inputRate","sineWave"};
        static const std::vector<std::string> math={"add","sub","mul","div","mod","neg","abs","sqrt","sin","cos","tan","asin","acos","atan","atan2","exp","log","log2","log10","floor","ceil","round","pow","min","max","clamp","mix","sign","eq","ne","lt","gt","le","ge"};
        std::istringstream in(text);std::string version;unsigned size,count,sc,context;
        auto bad=[](){throw std::invalid_argument("Invalid typed formula program");};
        if(!(in>>version>>size>>count>>sc>>context)||version!="FORM3"||size==0||size>20000||count>40000||sc>64||context>1)bad();
        code.clear();states.clear();values.fill(0);sampleContext=context!=0;registers.assign(size,0);std::vector<unsigned> kinds(size,99);
        for(unsigned n=0;n<sc;n++){State s;if(!(in>>s.name>>s.kind>>s.initial)||s.name.empty()||s.name.size()>65536||s.kind>6||(s.kind==0&&s.initial>1)||((s.kind==1||s.kind==2||s.kind==5)&&s.initial>UINT32_MAX))bad();for(const auto& x:states)if(x.name==s.name)bad();states.push_back(s);values[n]=s.initial;}
        for(unsigned n=0;n<count;n++){
            std::string name;Instruction i{};if(!(in>>name>>i.kind>>i.target>>i.a>>i.b>>i.c>>i.value)||i.kind>6)bad();
            auto op=std::find(ops.begin(),ops.end(),name);if(op!=ops.end())i.op=Op(op-ops.begin());else{auto m=std::find(math.begin(),math.end(),name);if(m==math.end()||i.kind==0)bad();i.op=arithmetic;i.math=FormulaOp(m-math.begin());}
            auto reg=[&](unsigned r){if(r>=size)bad();};
            if(i.op==jump||i.op==jz||i.op==jnz){if(i.target<=n||i.target>count)bad();if(i.op!=jump){reg(i.a);if(kinds[i.a]!=0)bad();}}
            else if(i.op==output){if(i.target>=outputs)bad();reg(i.a);if(kinds[i.a]!=i.kind)bad();}
            else if(i.op==store){if(i.target>=sc)bad();reg(i.a);if(kinds[i.a]!=i.kind||states[i.target].kind!=i.kind)bad();}
            else {
                reg(i.target);
                if(i.op==input){if(i.a>=inputs||(i.kind!=0&&i.kind!=5))bad();}
                else if(i.op==load){if(i.a>=sc||states[i.a].kind!=i.kind)bad();}
                else if(i.op==sineWave){reg(i.a);if(kinds[i.a]!=6||i.kind!=5)bad();}
                else if(i.op==copy||i.op==not_){reg(i.a);if(kinds[i.a]!=i.kind||(i.op==not_&&i.kind!=0))bad();}
                else if(i.op==cast){reg(i.a);if(i.b>6||kinds[i.a]!=i.b)bad();}
                else if(i.op==arithmetic){reg(i.a);reg(i.b);reg(i.c);if(kinds[i.a]!=i.kind)bad();
                    if(i.kind<5&&!(i.math<=FormulaOp::abs||i.math==FormulaOp::min||i.math==FormulaOp::max||i.math==FormulaOp::clamp||i.math==FormulaOp::sign||i.math>=FormulaOp::eq))bad();
                    bool binary=i.math<=FormulaOp::mod||i.math==FormulaOp::atan2||i.math==FormulaOp::pow||i.math>=FormulaOp::min&&i.math<=FormulaOp::mix||i.math>=FormulaOp::eq;
                    if(binary&&kinds[i.b]!=i.kind)bad();if((i.math==FormulaOp::clamp||i.math==FormulaOp::mix)&&kinds[i.c]!=i.kind)bad();
                }
                else if(i.op==bufferRead||i.op==bufferTimeline){reg(i.a);if(kinds[i.a]!=6||i.kind!=5)bad();if(i.op==bufferRead){reg(i.b);reg(i.c);if(kinds[i.b]!=0||kinds[i.c]!=0)bad();}}
                else if(i.op==rand_||i.op==randSeed){reg(i.a);reg(i.b);if(kinds[i.a]!=5||kinds[i.b]!=5||i.kind!=5||i.value>40000)bad();if(i.op==randSeed){reg(i.c);if(kinds[i.c]!=5)bad();}}
                if((i.op==bufferSize||i.op==bufferRead||i.op==inputRate)&&i.value>=inputs)bad();if(i.op==bufferInfo&&i.value>=4*inputs)bad();if(i.op==bufferTimeline&&i.value>=8*inputs)bad();
                if(i.op==sampleRate&&i.kind!=6)bad();if((i.op==blockSize||i.op==sampleIndex||i.op==samplePosition)&&i.kind!=4)bad();
                if((i.op==bufferSize||i.op==bufferInfo||i.op==inputRate)&&i.kind!=5)bad();
                if(i.op==constant&&(i.kind==0&&i.value>1||((i.kind==1||i.kind==2||i.kind==5)&&i.value>UINT32_MAX)))bad();
                kinds[i.target]=i.op==arithmetic&&i.math>=FormulaOp::eq?0:i.kind;
            }
            code.push_back(i);
        }
        std::string extra;if(in>>extra)bad();
    }
    template<class T>static std::uint64_t convert(std::uint64_t bits,unsigned source)noexcept {
        switch(source){case 0:return formulaPack(formulaCast<T>(formulaUnpack<bool>(bits)));case 1:return formulaPack(formulaCast<T>(formulaUnpack<std::int32_t>(bits)));case 2:return formulaPack(formulaCast<T>(formulaUnpack<std::uint32_t>(bits)));case 3:return formulaPack(formulaCast<T>(formulaUnpack<std::int64_t>(bits)));case 4:return formulaPack(formulaCast<T>(bits));case 5:return formulaPack(formulaCast<T>(formulaUnpack<float>(bits)));default:return formulaPack(formulaCast<T>(formulaUnpack<double>(bits)));}
    }
    static std::uint64_t convert(std::uint64_t bits,unsigned source,unsigned target)noexcept {
        switch(target){case 0:return convert<bool>(bits,source);case 1:return convert<std::int32_t>(bits,source);case 2:return convert<std::uint32_t>(bits,source);case 3:return convert<std::int64_t>(bits,source);case 4:return convert<std::uint64_t>(bits,source);case 5:return convert<float>(bits,source);default:return convert<double>(bits,source);}
    }
    template<class T> static std::uint64_t calculate(const Instruction& i,std::uint64_t a,std::uint64_t b,std::uint64_t c)noexcept {
        T x=formulaUnpack<T>(a),y=formulaUnpack<T>(b),z=formulaUnpack<T>(c);
        if(i.math>=FormulaOp::eq)return formulaPack(formulaCompare(i.math,x,y));return formulaPack(formulaCalc(i.math,x,y,z));
    }
    void evaluate(const float* input,float* output,const FormulaContext& context)noexcept {
        auto* r=registers.data();auto* state=context.typedState?context.typedState:values.data();
        for(std::size_t pc=0;pc<code.size();){const auto& i=code[pc++];switch(i.op){
            case constant:r[i.target]=i.value;break;case TypedFormulaProgram::input:r[i.target]=i.kind==0?formulaPack(input[i.a]!=0):formulaPack(input[i.a]);break;
            case TypedFormulaProgram::output:output[i.target]=formulaUnpack<float>(convert(r[i.a],i.kind,5));break;case copy:r[i.target]=r[i.a];break;case cast:r[i.target]=convert(r[i.a],i.b,i.kind);break;
            case load:r[i.target]=state[i.a];break;case store:state[i.target]=r[i.a];break;
            case jump:pc=i.target;break;case jz:if(!r[i.a])pc=i.target;break;case jnz:if(r[i.a])pc=i.target;break;case not_:r[i.target]=formulaPack(!r[i.a]);break;
            case sineWave:r[i.target]=formulaPack(context.sineWave(formulaUnpack<double>(r[i.a])));break;
            case sampleRate:r[i.target]=formulaPack(context.sampleRate);break;case blockSize:r[i.target]=context.blockSize;break;case sampleIndex:r[i.target]=context.sampleIndex;break;case samplePosition:r[i.target]=context.samplePosition;break;
            case inputRate:r[i.target]=formulaPack(context.rate(unsigned(i.value)));break;case bufferSize:r[i.target]=formulaPack(context.size(unsigned(i.value)));break;case bufferInfo:r[i.target]=formulaPack(context.info(unsigned(i.value)/4,unsigned(i.value)%4));break;
            case bufferRead:r[i.target]=formulaPack(context.read(unsigned(i.value),formulaUnpack<double>(r[i.a]),r[i.b]!=0,r[i.c]!=0));break;
            case bufferTimeline:r[i.target]=formulaPack(context.readTimeline(unsigned(i.value)/8,formulaUnpack<double>(r[i.a]),(unsigned(i.value)%8)/2,unsigned(i.value)%2));break;
            case rand_:r[i.target]=formulaPack(context.random(unsigned(i.value),formulaUnpack<float>(r[i.a]),formulaUnpack<float>(r[i.b])));break;
            case randSeed:r[i.target]=formulaPack(context.random(unsigned(i.value),formulaUnpack<float>(r[i.a]),formulaUnpack<float>(r[i.b]),formulaUnpack<float>(r[i.c])));break;
            case arithmetic:{auto a=r[i.a],b=r[i.b],c=r[i.c];switch(i.kind){case 1:r[i.target]=calculate<std::int32_t>(i,a,b,c);break;case 2:r[i.target]=calculate<std::uint32_t>(i,a,b,c);break;case 3:r[i.target]=calculate<std::int64_t>(i,a,b,c);break;case 4:r[i.target]=calculate<std::uint64_t>(i,a,b,c);break;case 5:r[i.target]=calculate<float>(i,a,b,c);break;case 6:r[i.target]=calculate<double>(i,a,b,c);break;default:break;}break;}
        }}
    }
};
}
