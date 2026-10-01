package com.vin.easyknack.formula;

import java.util.*;

/** Bounded sample language. Only validated syntax is emitted as C++. */
public final class FormulaCompiler {
    public enum Type { NUMBER, BOOLEAN, BUFFER }
    public record Port(String id, String name, Type type, boolean output) {}
    public record Token(String text, int start, int end) {}
    public static final class Failure extends IllegalArgumentException {
        private final int start, end;
        Failure(Token t, String message) { super(message); start=t.start(); end=t.end(); }
        public int start(){return start;} public int end(){return end;}
    }
    public record Instruction(String op,int target,int a,int b,int c,float value) {}
    public record State(String name,Type type,float initial){}
    public record Program(String cpp, Set<String> usedInputs, List<Port> ports,List<Instruction> instructions,int registers,List<State> states,String typedBytecode,boolean typedSampleContext) {
        public Program(String cpp,Set<String> used,List<Port> ports,List<Instruction> instructions,int registers,List<State> states){this(cpp,used,ports,instructions,registers,states,null,false);}
        public Program {usedInputs=Set.copyOf(usedInputs);ports=List.copyOf(ports);instructions=List.copyOf(instructions);states=List.copyOf(states);}
        public String bytecode(){if(typedBytecode!=null)return typedBytecode;var s=new StringBuilder("FORM2 ").append(registers).append(' ').append(instructions.size()).append(' ').append(states.size());for(var state:states)s.append(' ').append(state.name()).append(' ').append(state.type()==Type.BOOLEAN?1:0).append(' ').append(state.initial());for(var i:instructions)s.append(' ').append(i.op()).append(' ').append(i.target()).append(' ').append(i.a()).append(' ').append(i.b()).append(' ').append(i.c()).append(' ').append(i.value());return s.toString();}
        public boolean sampleContext(){return typedSampleContext||!states.isEmpty()||instructions.stream().anyMatch(i->Set.of("sampleIndex","samplePosition","rand","randSeed","bufferRead","bufferTimeline").contains(i.op())||i.op().equals("bufferInfo")&&(int)i.value()%4!=3);}
    }
    public static final Map<String,Integer> FUNCTIONS=Map.ofEntries(
        Map.entry("abs",1),Map.entry("sqrt",1),Map.entry("sin",1),Map.entry("cos",1),
        Map.entry("tan",1),Map.entry("asin",1),Map.entry("acos",1),Map.entry("atan",1),
        Map.entry("atan2",2),Map.entry("exp",1),Map.entry("log",1),Map.entry("log2",1),
        Map.entry("log10",1),Map.entry("floor",1),Map.entry("ceil",1),Map.entry("round",1),
        Map.entry("pow",2),Map.entry("min",2),Map.entry("max",2),Map.entry("clamp",3),
        Map.entry("sineWave",1),Map.entry("mix",3),Map.entry("sign",1),Map.entry("rand",2));
    public static final Set<String> CONTEXT=Set.of("sampleRate","blockSize","sampleIndex","samplePosition");
    private static final Set<String> RESERVED=Set.of("state","float","bool","if","else","true","false","pi","tau");
    private FormulaCompiler(){}
    public static Program compile(String source,List<Port> ports){return compile(source,ports,2);}
    public static Program compile(String source,List<Port> ports,int version){if(version<1||version>3)throw new IllegalArgumentException("Unsupported formula language version");if(version==3||TypedFormulaCompiler.isTyped(source))return TypedFormulaCompiler.compile(source,ports,version);return new Parser(source,ports,version).compile();}
    public static List<Token> tokens(String source){
        if(source.length()>65536)throw new Failure(new Token("",0,0),"Formula exceeds 65536 characters");
        var result=new ArrayList<Token>();int i=0;
        while(i<source.length()){
            char c=source.charAt(i);if(Character.isWhitespace(c)){i++;continue;}
            int start=i;
            if(c=='/'&&i+1<source.length()&&source.charAt(i+1)=='/'){while(i<source.length()&&source.charAt(i)!='\n')i++;continue;}
            if(Character.isLetter(c)||c=='_'){i++;while(i<source.length()&&(Character.isLetterOrDigit(source.charAt(i))||source.charAt(i)=='_'))i++;}
            else if(Character.isDigit(c)||c=='.'&&i+1<source.length()&&Character.isDigit(source.charAt(i+1))){
                i++;while(i<source.length()&&(Character.isDigit(source.charAt(i))||source.charAt(i)=='.'))i++;
                if(i<source.length()&&(source.charAt(i)=='e'||source.charAt(i)=='E')){i++;if(i<source.length()&&(source.charAt(i)=='+'||source.charAt(i)=='-'))i++;while(i<source.length()&&Character.isDigit(source.charAt(i)))i++;}
            }else if(i+1<source.length()&&Set.of("&&","||","==","!=","<=",">=","+=","-=","*=","/=").contains(source.substring(i,i+2)))i+=2;
            else if("+-*/%<>=!(){};,.".indexOf(c)>=0)i++;
            else throw new Failure(new Token("",i,i+1),"Unexpected character: "+c);
            result.add(new Token(source.substring(start,i),start,i));
            if(result.size()>8192)throw new Failure(result.get(result.size()-1),"Formula exceeds 8192 tokens");
        }
        result.add(new Token("",source.length(),source.length()));return List.copyOf(result);
    }
    private record Variable(Type type,String cpp,Port port,int register){}
    private record Expr(Type type,String cpp,int register){}
    private static final class Parser {
        final List<Token> tokens;final List<Port> ports;final Map<String,Variable> vars=new LinkedHashMap<>();
        final Set<String> assigned=new HashSet<>(),used=new LinkedHashSet<>();int cursor,depth,serial;
        final List<Instruction> instructions=new ArrayList<>();int registers;
        final List<State> states=new ArrayList<>();boolean bodyStarted;
        int emit(String op,int target,int a,int b,int c,float value){instructions.add(new Instruction(op,target,a,b,c,value));return instructions.size()-1;}
        void jump(int at,int destination){var i=instructions.get(at);instructions.set(at,new Instruction(i.op(),destination,i.a(),0,0,0));}
        final int version;
        Parser(String source,List<Port> ports,int version){
            this.version=version;
            this.tokens=tokens(source);this.ports=List.copyOf(ports);var ids=new HashSet<String>();
            int in=0,out=0;
            for(var p:ports){
                if(p.type()==null||p.type()==Type.BUFFER&&p.output()||p.id()==null||!ids.add(p.id())||p.name()==null||!p.name().matches("[A-Za-z_][A-Za-z0-9_]*")||RESERVED.contains(p.name())||CONTEXT.contains(p.name())||FUNCTIONS.containsKey(p.name())||vars.containsKey(p.name()))throw fail("Invalid or duplicate port identifier: "+p.name());
                String cpp=p.output()?"output["+(out++)+"]":"input["+(in++)+"]";
                if(p.type()==Type.BOOLEAN&&!p.output())cpp="("+cpp+" != 0.0f)";
                int register=registers++;vars.put(p.name(),new Variable(p.type(),cpp,p,register));
                emit(p.output()||p.type()==Type.BUFFER?"constant":"input",register,p.output()?0:in-1,0,0,0);
                if(!p.output()||p.type()==Type.BOOLEAN)assigned.add(p.name());
            }
        }
        Program compile(){
            var cpp=new StringBuilder();int out=0;
            for(var p:ports)if(p.output()){if(p.type()==Type.BOOLEAN)cpp.append("output[").append(out).append("] = 0.0f;\n");out++;}
            while(!peek("")){cpp.append(statement());if(cpp.length()>1048576)throw fail("Generated formula exceeds 1 MiB");}
            for(var p:ports)if(p.output()&&!assigned.contains(p.name()))throw fail("Output must be assigned on every path: "+p.name());
            int index=0;for(var p:ports)if(p.output())emit("output",index++,vars.get(p.name()).register(),0,0,0);
            for(int i=0;i<states.size();i++){var variable=vars.get(states.get(i).name());emit("stateStore",i,variable.register(),0,0,0);cpp.append("context.state[").append(i).append("] = ").append(variable.cpp()).append(";\n");}
            return new Program(cpp.toString(),used,ports,instructions,registers,states);
        }
        String statement(){
            enter();try{
                if(take("state")){
                    if(depth!=1||bodyStarted)throw fail("State declarations must appear at the beginning, outside branches");
                    if(states.size()>=64)throw fail("A formula supports at most 64 state variables");
                    Type type=take("float")?Type.NUMBER:take("bool")?Type.BOOLEAN:null;if(type==null)throw fail("Expected float or bool after state");
                    Token name=current();cursor++;
                    if(!name.text().matches("[A-Za-z_][A-Za-z0-9_]*")||vars.containsKey(name.text())||RESERVED.contains(name.text())||CONTEXT.contains(name.text())||FUNCTIONS.containsKey(name.text()))throw new Failure(name,"Invalid, duplicate or reserved state name");
                    expect("=");float initial;
                    if(type==Type.BOOLEAN){if(take("true"))initial=1;else if(take("false"))initial=0;else throw fail("Boolean state requires true or false initializer");}
                    else {boolean negative=take("-");if(!negative)take("+");Token value=current();cursor++;try{initial=Float.parseFloat(value.text());if(negative)initial=-initial;if(!Float.isFinite(initial))throw new NumberFormatException();}catch(NumberFormatException e){throw new Failure(value,"State requires a finite numeric literal initializer");}}
                    expect(";");int slot=states.size();states.add(new State(name.text(),type,initial));
                    var variable=new Variable(type,"state"+slot,null,registers++);vars.put(name.text(),variable);assigned.add(name.text());emit("stateLoad",variable.register(),slot,0,0,0);
                    return (type==Type.NUMBER?"float ":"bool ")+variable.cpp()+" = context.state["+slot+"]"+(type==Type.BOOLEAN?" != 0.0f":"")+";\n";
                }
                bodyStarted=true;
                if(take("{")){
                    var existing=new HashSet<>(vars.keySet());var b=new StringBuilder("{\n");
                    while(!peek("}")){if(peek(""))throw fail("Expected }");b.append(statement());}expect("}");
                    vars.keySet().retainAll(existing);assigned.retainAll(existing);return b.append("}\n").toString();
                }
                if(take("if")){
                    expect("(");var condition=expression(0);require(condition,Type.BOOLEAN);expect(")");
                    int otherwise=emit("jz",0,condition.register(),0,0,0);
                    var before=new HashSet<>(assigned);String yes=branch();var after=new HashSet<>(assigned);assigned.clear();assigned.addAll(before);
                    int end=emit("jump",0,0,0,0,0);jump(otherwise,instructions.size());
                    String no=take("else")?branch():"{}\n";after.retainAll(assigned);assigned.clear();assigned.addAll(after);
                    jump(end,instructions.size());
                    return "if ("+condition.cpp()+") "+yes+"else "+no;
                }
                Type declared=take("float")?Type.NUMBER:take("bool")?Type.BOOLEAN:null;
                Token name=current();if(!name.text().matches("[A-Za-z_][A-Za-z0-9_]*"))throw fail("Expected variable or assignment");cursor++;
                Variable v=vars.get(name.text());
                if(declared!=null){if(v!=null||RESERVED.contains(name.text())||CONTEXT.contains(name.text())||FUNCTIONS.containsKey(name.text()))throw new Failure(name,"Duplicate or reserved variable");v=new Variable(declared,"local"+(serial++),null,registers++);}
                else if(v==null)throw new Failure(name,"Unknown variable: "+name.text());
                else if(v.port()!=null&&!v.port().output())throw new Failure(name,"Inputs are read-only");
                String assignment=current().text();
                boolean compound=Set.of("+=","-=","*=","/=").contains(assignment);
                if(compound){
                    if(declared!=null)throw fail("Declarations require =");
                    if(v.type()!=Type.NUMBER)throw fail("Compound assignments require numeric variables");
                    if(!assigned.contains(name.text()))throw new Failure(name,"Value is not assigned on every path: "+name.text());
                    cursor++;
                }else expect("=");
                var value=expression(0);require(value,v.type());expect(";");
                if(compound)value=operation(Type.NUMBER,"("+v.cpp()+" "+assignment.charAt(0)+" "+value.cpp()+")",switch(assignment){case "+="->"add";case "-="->"sub";case "*="->"mul";default->"div";},v.register(),value.register(),0,0);
                vars.put(name.text(),v);assigned.add(name.text());
                emit("copy",v.register(),value.register(),0,0,0);
                return (declared==null?"":declared==Type.NUMBER?"float ":"bool ")+v.cpp()+" = "+value.cpp()+";\n";
            }finally{depth--;}
        }
        String branch(){var existing=new HashSet<>(vars.keySet());String code=statement();vars.keySet().retainAll(existing);assigned.retainAll(existing);return "{\n"+code+"}\n";}
        Expr expression(int minimum){
            enter();try{
                Expr left=primary();
                while(true){String op=current().text();int prec=precedence(op);if(prec<minimum)break;cursor++;
                    int shortResult=-1,shortJump=-1;if(op.equals("&&")||op.equals("||")){shortResult=registers++;emit("copy",shortResult,left.register(),0,0,0);shortJump=emit(op.equals("&&")?"jz":"jnz",0,left.register(),0,0,0);}
                    var right=expression(prec+1);
                    boolean logical=op.equals("&&")||op.equals("||"),equality=op.equals("==")||op.equals("!=");
                    if(logical){require(left,Type.BOOLEAN);require(right,Type.BOOLEAN);}
                    else if(equality){require(right,left.type());}else{require(left,Type.NUMBER);require(right,Type.NUMBER);}
                    boolean bool=logical||equality||Set.of("<",">","<=",">=").contains(op);
                    String code=op.equals("%")?"std::fmod("+left.cpp()+", "+right.cpp()+")":"("+left.cpp()+" "+op+" "+right.cpp()+")";
                    if(shortJump>=0){emit("copy",shortResult,right.register(),0,0,0);jump(shortJump,instructions.size());left=new Expr(Type.BOOLEAN,code,shortResult);}
                    else left=operation(bool?Type.BOOLEAN:Type.NUMBER,code,switch(op){case "+"->"add";case "-"->"sub";case "*"->"mul";case "/"->"div";case "%"->"mod";case "=="->"eq";case "!="->"ne";case "<"->"lt";case ">"->"gt";case "<="->"le";case ">="->"ge";default->throw fail("Unsupported operator");},left.register(),right.register(),0,0);
                }return left;
            }finally{depth--;}
        }
        Expr primary(){
            if(take("!")){var e=expression(7);require(e,Type.BOOLEAN);return operation(Type.BOOLEAN,"(!"+e.cpp()+")","not",e.register(),0,0,0);}
            if(take("-")){var e=expression(7);require(e,Type.NUMBER);return operation(Type.NUMBER,"(-"+e.cpp()+")","neg",e.register(),0,0,0);}
            if(take("+")){var e=expression(7);require(e,Type.NUMBER);return e;}
            if(take("(")){var e=expression(0);expect(")");return e;}
            Token t=current();cursor++;
            if(CONTEXT.contains(t.text()))return operation(Type.NUMBER,"static_cast<float>(context."+t.text()+")",t.text(),0,0,0,0);
            var buffer=vars.get(t.text());
            if(buffer!=null&&buffer.type()!=Type.BUFFER&&peek(".")){
                if(buffer.port()==null||buffer.port().output())throw new Failure(t,"Properties belong to inputs only");
                cursor++;Token member=current();cursor++;used.add(buffer.port().id());
                if(member.text().equals(buffer.type()==Type.BOOLEAN?"active":"value"))return new Expr(buffer.type(),buffer.cpp(),buffer.register());
                if(member.text().equals("rate")&&buffer.type()==Type.NUMBER){int slot=0;for(var p:ports){if(p.id().equals(buffer.port().id()))break;if(!p.output())slot++;}return operation(Type.NUMBER,"context.rate("+slot+")","inputRate",0,0,0,slot);}
                throw new Failure(member,buffer.type()==Type.BOOLEAN?"Trigger inputs expose .active":"Numeric inputs expose .value and .rate; use a Buffer input for indexed reads");
            }
            if(buffer!=null&&buffer.type()==Type.BUFFER){
                used.add(buffer.port().id());int slot=0;for(var p:ports){if(p.id().equals(buffer.port().id()))break;if(!p.output())slot++;}
                expect(".");Token member=current();cursor++;
                if(member.text().equals("size"))return operation(Type.NUMBER,"context.size("+slot+")","bufferSize",0,0,0,slot);
                int info=List.of("begin","end","writePosition","sampleRate").indexOf(member.text());
                if(info>=0)return operation(Type.NUMBER,"context.info("+slot+", "+info+")","bufferInfo",0,0,0,slot*4+info);
                if(!member.text().equals("read"))throw new Failure(member,"Buffer exposes size, begin, end, writePosition, sampleRate and read(position[, mode[, interpolation]])");
                expect("(");var position=expression(0);require(position,Type.NUMBER);
                if(version>=2&&peek(")")||peek(",")&&cursor+1<tokens.size()&&Set.of("Clamp","Wrap","Zero").contains(tokens.get(cursor+1).text())){
                    int mode=0,interpolation=1;
                    if(take(",")){String name=current().text();cursor++;mode=switch(name){case "Clamp"->0;case "Wrap"->1;case "Zero"->2;default->throw fail("Expected Clamp, Wrap or Zero");};if(take(",")){String style=current().text();cursor++;interpolation=switch(style){case "Linear"->1;case "Nearest"->0;default->throw fail("Expected Linear or Nearest");};}}expect(")");
                    return operation(Type.NUMBER,"context.readTimeline("+slot+", "+position.cpp()+", "+mode+", "+interpolation+")","bufferTimeline",position.register(),0,0,slot*8+mode*2+interpolation);
                }
                var wrap=operation(Type.BOOLEAN,"false","constant",0,0,0,0);var interpolate=operation(Type.BOOLEAN,"true","constant",0,0,0,1);
                if(take(",")){wrap=expression(0);require(wrap,Type.BOOLEAN);if(take(",")){interpolate=expression(0);require(interpolate,Type.BOOLEAN);}}expect(")");
                return operation(Type.NUMBER,"context.read("+slot+", "+position.cpp()+", "+wrap.cpp()+", "+interpolate.cpp()+")","bufferRead",position.register(),wrap.register(),interpolate.register(),slot);
            }
            if(!vars.containsKey(t.text())&&(t.text().equals("Audio")||t.text().equals("Control")))return operation(Type.NUMBER,t.text().equals("Audio")?"1.0f":"0.0f","constant",0,0,0,t.text().equals("Audio")?1:0);
            if(t.text().equals("true")||t.text().equals("false"))return operation(Type.BOOLEAN,t.text(),"constant",0,0,0,t.text().equals("true")?1:0);
            if(t.text().equals("pi")||t.text().equals("tau"))return operation(Type.NUMBER,t.text().equals("pi")?"3.14159265358979323846f":"6.28318530717958647692f","constant",0,0,0,(float)(t.text().equals("pi")?Math.PI:2*Math.PI));
            if(!t.text().isEmpty()&&(Character.isDigit(t.text().charAt(0))||t.text().charAt(0)=='.')){
                try{float f=Float.parseFloat(t.text());if(!Float.isFinite(f))throw new NumberFormatException();return operation(Type.NUMBER,Float.toString(f)+"f","constant",0,0,0,f);}catch(NumberFormatException e){throw new Failure(t,"Invalid finite float literal");}
            }
            if(FUNCTIONS.containsKey(t.text())){
                expect("(");var args=new ArrayList<String>();var refs=new ArrayList<Integer>();if(!peek(")"))do{var e=expression(0);require(e,Type.NUMBER);args.add(e.cpp());refs.add(e.register());}while(take(","));expect(")");
                if(args.size()!=FUNCTIONS.get(t.text())&&!(t.text().equals("rand")&&args.size()==3))throw new Failure(t,"Wrong argument count for "+t.text());
                if(t.text().equals("rand")){int site=serial++;return operation(Type.NUMBER,"context.random("+site+", "+String.join(", ",args)+")",args.size()==3?"randSeed":"rand",refs.get(0),refs.get(1),args.size()==3?refs.get(2):0,site);}
                if(t.text().equals("sineWave"))return operation(Type.NUMBER,"context.sineWave("+args.get(0)+")","sineWave",refs.get(0),0,0,0);
                String name=t.text();String code=switch(name){
                    case "mix" -> "("+args.get(0)+" + ("+args.get(1)+" - "+args.get(0)+") * "+args.get(2)+")";
                    case "sign" -> "static_cast<float>(("+args.get(0)+" > 0.0f) - ("+args.get(0)+" < 0.0f))";
                    case "clamp" -> "std::min(std::max("+args.get(0)+", "+args.get(1)+"), "+args.get(2)+")";
                    default -> "std::"+name+"("+String.join(", ",args)+")";};
                return operation(Type.NUMBER,code,name,refs.get(0),refs.size()>1?refs.get(1):0,refs.size()>2?refs.get(2):0,0);
            }
            var v=vars.get(t.text());if(v==null)throw new Failure(t,"Unknown symbol: "+t.text());
            if(!assigned.contains(t.text()))throw new Failure(t,"Value is not assigned on every path: "+t.text());
            if(v.port()!=null&&!v.port().output())used.add(v.port().id());return new Expr(v.type(),v.cpp(),v.register());
        }
        Expr operation(Type type,String code,String op,int a,int b,int c,float value){if(code.length()>131072)throw fail("Expression is too complex");int register=registers++;emit(op,register,a,b,c,value);return new Expr(type,code,register);}
        void enter(){if(++depth>128)throw fail("Formula nesting exceeds 128 levels");}
        int precedence(String s){return switch(s){case "||"->0;case "&&"->1;case "==","!="->2;case "<",">","<=",">="->3;case "+","-"->4;case "*","/","%"->5;default->-1;};}
        void require(Expr e,Type t){if(e.type()!=t)throw fail("Expected "+t.name().toLowerCase()+", got "+e.type().name().toLowerCase());}
        Token current(){return tokens.get(Math.min(cursor,tokens.size()-1));}
        boolean peek(String s){return current().text().equals(s);}boolean take(String s){if(!peek(s))return false;cursor++;return true;}
        void expect(String s){if(!take(s))throw fail("Expected '"+s+"'");}Failure fail(String s){return new Failure(current(),s);}
    }
}
