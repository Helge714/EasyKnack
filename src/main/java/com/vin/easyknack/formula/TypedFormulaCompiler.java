package com.vin.easyknack.formula;

import java.math.BigInteger;
import java.util.*;
import static com.vin.easyknack.formula.FormulaCompiler.*;

/** Version 3: fixed-width values, explicit narrowing and a typed preview instruction stream. */
final class TypedFormulaCompiler {
    enum K {
        BOOL("bool"), INT("std::int32_t"), UINT("std::uint32_t"), LONG("std::int64_t"), ULONG("std::uint64_t"), FLOAT("float"), DOUBLE("double"), BUFFER("void");
        final String cpp; K(String cpp){this.cpp=cpp;}
        boolean unsigned(){return this==UINT||this==ULONG;}
        boolean integer(){return ordinal()<=ULONG.ordinal();}
    }
    static final Set<String> TYPES=Set.of("bool","int","uint","long","ulong","float","double","unsigned");
    static boolean isTyped(String source){
        var ts=FormulaCompiler.tokens(source);
        for(int i=0;i<ts.size();i++)if(Set.of("int","uint","long","ulong","double","unsigned").contains(ts.get(i).text())||ts.get(i).text().equals("(")&&i+2<ts.size()&&TYPES.contains(ts.get(i+1).text())&&ts.get(i+2).text().equals(")"))return true;
        return false;
    }
    static Program compile(String source,List<Port> ports,int version){return new Parser(source,ports,version).compile();}
    record E(K kind,String cpp,int reg){}
    record V(K kind,String cpp,int reg,Port port){}
    record S(String name,K kind,String bits){}
    record I(String op,K kind,int target,int a,int b,int c,String value){}
    static final class Parser {
        final List<Token> ts; final List<Port> ports; final int version;
        final Map<String,V> vars=new LinkedHashMap<>();final Set<String> assigned=new HashSet<>(),used=new LinkedHashSet<>();
        final List<I> ins=new ArrayList<>();final List<S> states=new ArrayList<>();int at,registers,depth,serial;boolean body,contextual;
        Parser(String source,List<Port> ports,int version){
            this.ts=FormulaCompiler.tokens(source);this.ports=List.copyOf(ports);this.version=version;
            var ids=new HashSet<String>();int in=0,out=0;
            for(var p:ports){
                if(p.name()==null||!p.name().matches("[A-Za-z_][A-Za-z0-9_]*")||reserved(p.name())||vars.containsKey(p.name())||p.id()==null||!ids.add(p.id())||p.type()==null||p.type()==Type.BUFFER&&p.output())throw fail("Invalid or duplicate port identifier");
                K k=p.type()==Type.NUMBER?K.FLOAT:p.type()==Type.BOOLEAN?K.BOOL:K.BUFFER;
                int r=registers++;String cpp=p.output()?"output["+(out++)+"]":"input["+(in++)+"]";
                if(k==K.BOOL)cpp="("+cpp+" != 0.0f)";
                vars.put(p.name(),new V(k,cpp,r,p));emit(p.output()||k==K.BUFFER?"constant":"input",k==K.BUFFER?K.FLOAT:k,r,p.output()?0:in-1,0,0,"0");
                if(!p.output()||k==K.BOOL)assigned.add(p.name());
            }
        }
        boolean reserved(String n){return TYPES.contains(n)||Set.of("state","if","else","true","false","pi","tau").contains(n)||CONTEXT.contains(n)||FUNCTIONS.containsKey(n);}
        Program compile(){
            var cpp=new StringBuilder();int index=0;for(var p:ports){if(p.output()){if(p.type()==Type.BOOLEAN)cpp.append("output[").append(index).append("] = 0.0f;\n");index++;}}
            while(!peek("")){cpp.append(statement());if(cpp.length()>1048576)throw fail("Generated formula exceeds 1 MiB");}
            index=0;for(var p:ports)if(p.output()){if(!assigned.contains(p.name()))throw fail("Output must be assigned on every path: "+p.name());var v=vars.get(p.name());emit("output",v.kind(),index++,v.reg(),0,0,"0");}
            for(int i=0;i<states.size();i++){var v=vars.get(states.get(i).name());emit("store",v.kind(),i,v.reg(),0,0,"0");cpp.append("context.typedState[").append(i).append("] = easyknack::formulaPack(").append(v.cpp()).append(");\n");}
            var wire=new StringBuilder("FORM3 ").append(registers).append(' ').append(ins.size()).append(' ').append(states.size()).append(' ').append(contextual||!states.isEmpty()?1:0);
            for(var s:states)wire.append(' ').append(s.name()).append(' ').append(s.kind().ordinal()).append(' ').append(s.bits());
            for(var i:ins)wire.append(' ').append(i.op()).append(' ').append(i.kind().ordinal()).append(' ').append(i.target()).append(' ').append(i.a()).append(' ').append(i.b()).append(' ').append(i.c()).append(' ').append(i.value());
            return new Program(cpp.toString(),used,ports,List.of(),registers,List.of(),wire.toString(),contextual||!states.isEmpty());
        }
        K type(){
            if(take("unsigned")){if(take("int"))return K.UINT;if(take("long"))return K.ULONG;throw fail("Expected int or long after unsigned");}
            if(!TYPES.contains(now().text()))return null;
            String s=now().text();at++;return switch(s){case "bool"->K.BOOL;case "int"->K.INT;case "uint"->K.UINT;case "long"->K.LONG;case "ulong"->K.ULONG;case "float"->K.FLOAT;default->K.DOUBLE;};
        }
        String statement(){
            enter();try{
                if(take("state")){
                    if(depth!=1||body||states.size()>=64)throw fail("Declare at most 64 state variables at the beginning, outside branches");
                    K k=type();if(k==null)throw fail("Expected state type");Token name=identifier();if(vars.containsKey(name.text())||reserved(name.text()))throw fail("Duplicate or reserved variable");expect("=");
                    boolean neg=take("-");if(!neg)take("+");Token literal=now();at++;
                    if(literal.end()==now().start()&&Set.of("f","F","u","U","l","L","ul","UL","ull","ULL").contains(now().text()))at++;
                    String bits=stateBits(k,(neg?"-":"")+literal.text());expect(";");int slot=states.size();states.add(new S(name.text(),k,bits));
                    int reg=registers++;String cpp="state"+slot;vars.put(name.text(),new V(k,cpp,reg,null));assigned.add(name.text());emit("load",k,reg,slot,0,0,"0");
                    return k.cpp+" "+cpp+" = easyknack::formulaUnpack<"+k.cpp+">(context.typedState["+slot+"]);\n";
                }
                body=true;
                if(take("{")){var before=new HashSet<>(vars.keySet());var code=new StringBuilder("{\n");while(!peek("}")){if(peek(""))throw fail("Expected }");code.append(statement());}expect("}");vars.keySet().retainAll(before);assigned.retainAll(before);return code.append("}\n").toString();}
                if(take("if")){
                    expect("(");E e=expression(0);requireBool(e);expect(")");int j=emit("jz",K.BOOL,0,e.reg(),0,0,"0");var before=new HashSet<>(assigned);String yes=branch();var after=new HashSet<>(assigned);assigned.clear();assigned.addAll(before);int end=emit("jump",K.BOOL,0,0,0,0,"0");jump(j,ins.size());String no=take("else")?branch():"{}\n";after.retainAll(assigned);assigned.clear();assigned.addAll(after);jump(end,ins.size());return "if ("+e.cpp()+") "+yes+"else "+no;
                }
                K declared=type();Token name=identifier();V v=vars.get(name.text());
                if(declared!=null){if(v!=null||reserved(name.text()))throw fail("Duplicate or reserved variable");v=new V(declared,"local"+(serial++),registers++,null);}
                else if(v==null)throw fail("Unknown variable: "+name.text());
                else if(v.port()!=null&&!v.port().output())throw fail("Inputs are read-only");
                String op=now().text();boolean compound=Set.of("+=","-=","*=","/=").contains(op);
                if(compound){if(declared!=null||!assigned.contains(name.text()))throw fail("Compound assignment requires an initialized variable");at++;}else expect("=");
                E value=expression(0);expect(";");if(compound)value=binary(op.substring(0,1),new E(v.kind(),v.cpp(),v.reg()),value);
                value=assignment(value,v.kind(),v.port()!=null);vars.put(name.text(),v);assigned.add(name.text());emit("copy",v.kind(),v.reg(),value.reg(),0,0,"0");
                String target=v.port()!=null?v.port().type()==Type.BOOLEAN?"output["+outputIndex(v.port())+"]":v.cpp():v.cpp();
                return (declared==null?"":declared.cpp+" ")+target+" = "+value.cpp()+";\n";
            }finally{depth--;}
        }
        int outputIndex(Port p){int n=0;for(var q:ports)if(q.output()){if(q==p)return n;n++;}throw new AssertionError();}
        String branch(){var before=new HashSet<>(vars.keySet());String s=statement();vars.keySet().retainAll(before);assigned.retainAll(before);return "{\n"+s+"}\n";}
        E expression(int min){enter();try{E left=primary();while(true){String op=now().text();int p=precedence(op);if(p<min)break;at++;
            if(op.equals("&&")||op.equals("||")){requireBool(left);int reg=registers++;emit("copy",K.BOOL,reg,left.reg(),0,0,"0");int jump=emit(op.equals("&&")?"jz":"jnz",K.BOOL,0,left.reg(),0,0,"0");E right=expression(p+1);requireBool(right);emit("copy",K.BOOL,reg,right.reg(),0,0,"0");jump(jump,ins.size());left=new E(K.BOOL,"("+left.cpp()+op+right.cpp()+")",reg);}
            else left=binary(op,left,expression(p+1));
        }return left;}finally{depth--;}}
        E binary(String op,E a,E b){
            K k=common(a.kind(),b.kind());a=cast(a,k);b=cast(b,k);boolean comparison=Set.of("==","!=","<",">","<=",">=").contains(op);
            String name=switch(op){case "+"->"add";case "-"->"sub";case "*"->"mul";case "/"->"div";case "%"->"mod";case "=="->"eq";case "!="->"ne";case "<"->"lt";case ">"->"gt";case "<="->"le";case ">="->"ge";default->throw fail("Unsupported operator");};
            String code=comparison?"("+a.cpp()+op+b.cpp()+")":"easyknack::formulaCalc<"+k.cpp+">(easyknack::FormulaOp::"+name+", "+a.cpp()+", "+b.cpp()+")";
            return operation(comparison?K.BOOL:k,k,code,name,a.reg(),b.reg(),0,"0");
        }
        E primary(){
            if(take("!")){E e=expression(7);requireBool(e);return operation(K.BOOL,K.BOOL,"(!"+e.cpp()+")","not",e.reg(),0,0,"0");}
            if(take("-")){
                Token t=now();boolean unsignedSuffix=at+1<ts.size()&&ts.get(at+1).text().toLowerCase().startsWith("u")&&t.end()==ts.get(at+1).start();
                if(!t.text().isEmpty()&&(Character.isDigit(t.text().charAt(0))||t.text().charAt(0)=='.')&&!unsignedSuffix){at++;return literal(new Token("-"+t.text(),t.start(),t.end()));}
                E e=expression(7);K k=e.kind()==K.BOOL?K.INT:e.kind();numeric(k);e=cast(e,k);return operation(k,k,"easyknack::formulaCalc<"+k.cpp+">(easyknack::FormulaOp::neg, "+e.cpp()+")","neg",e.reg(),0,0,"0");}
            if(take("+")){E e=expression(7);numeric(e.kind());return e.kind()==K.BOOL?cast(e,K.INT):e;}
            if(take("(")){K k=type();if(k!=null){expect(")");return cast(expression(7),k);}E e=expression(0);expect(")");return e;}
            Token t=now();at++;String n=t.text();
            if(CONTEXT.contains(n)){contextual|=!n.equals("sampleRate")&&!n.equals("blockSize");K k=n.equals("sampleRate")?K.DOUBLE:K.ULONG;return operation(k,k,"context."+n,n,0,0,0,"0");}
            V v=vars.get(n);
            if(v!=null&&take("."))return member(v);
            if(n.equals("true")||n.equals("false"))return constant(K.BOOL,n.equals("true")?"1":"0");
            if(n.equals("pi")||n.equals("tau"))return constant(K.DOUBLE,Long.toUnsignedString(Double.doubleToRawLongBits(n.equals("pi")?Math.PI:2*Math.PI)));
            if(v==null&&(n.equals("Audio")||n.equals("Control")))return constant(K.INT,n.equals("Audio")?"1":"0");
            if(!n.isEmpty()&&(Character.isDigit(n.charAt(0))||n.charAt(0)=='.'))return literal(t);
            if(FUNCTIONS.containsKey(n))return function(n);
            if(v==null)throw fail("Unknown symbol: "+n);if(v.kind()==K.BUFFER)throw fail("Use a Buffer member");if(!assigned.contains(n))throw fail("Value is not assigned on every path: "+n);if(v.port()!=null&&!v.port().output())used.add(v.port().id());return new E(v.kind(),v.cpp(),v.reg());
        }
        E member(V v){
            if(v.port()==null||v.port().output())throw fail("Properties belong to inputs only");used.add(v.port().id());int slot=0;for(var p:ports){if(p==v.port())break;if(!p.output())slot++;}
            String name=identifier().text();
            if(v.kind()!=K.BUFFER){if(name.equals(v.kind()==K.BOOL?"active":"value"))return new E(v.kind(),v.cpp(),v.reg());if(name.equals("rate")&&v.kind()==K.FLOAT)return operation(K.FLOAT,K.FLOAT,"context.rate("+slot+")","inputRate",0,0,0,""+slot);throw fail("Invalid input property");}
            if(name.equals("size"))return operation(K.FLOAT,K.FLOAT,"context.size("+slot+")","bufferSize",0,0,0,""+slot);
            int field=List.of("begin","end","writePosition","sampleRate").indexOf(name.equals("writepos")?"writePosition":name);if(field>=0){contextual|=field!=3;return operation(K.FLOAT,K.FLOAT,"context.info("+slot+", "+field+")","bufferInfo",0,0,0,""+(slot*4+field));}
            if(!name.equals("read"))throw fail("Unknown Buffer property");expect("(");E pos=cast(expression(0),K.DOUBLE);contextual=true;
            if(version>=2&&peek(")")||peek(",")&&at+1<ts.size()&&Set.of("Clamp","Wrap","Zero").contains(ts.get(at+1).text())){
                int mode=0,linear=1;if(take(",")){mode=List.of("Clamp","Wrap","Zero").indexOf(now().text());if(mode<0)throw fail("Expected read mode");at++;if(take(",")){linear=List.of("Nearest","Linear").indexOf(now().text());if(linear<0)throw fail("Expected interpolation mode");at++;}}expect(")");return operation(K.FLOAT,K.FLOAT,"context.readTimeline("+slot+", "+pos.cpp()+", "+mode+", "+linear+")","bufferTimeline",pos.reg(),0,0,""+(slot*8+mode*2+linear));
            }
            E wrap=constant(K.BOOL,"0"),linear=constant(K.BOOL,"1");if(take(",")){wrap=expression(0);requireBool(wrap);if(take(",")){linear=expression(0);requireBool(linear);}}expect(")");return operation(K.FLOAT,K.FLOAT,"context.read("+slot+", "+pos.cpp()+", "+wrap.cpp()+", "+linear.cpp()+")","bufferRead",pos.reg(),wrap.reg(),linear.reg(),""+slot);
        }
        E function(String name){
            expect("(");var args=new ArrayList<E>();if(!peek(")"))do{args.add(expression(0));}while(take(","));expect(")");if(args.size()!=FUNCTIONS.get(name)&&!(name.equals("rand")&&args.size()==3))throw fail("Wrong argument count for "+name);
            if(name.equals("sineWave")){E phase=cast(args.get(0),K.DOUBLE);return operation(K.FLOAT,K.FLOAT,"context.sineWave("+phase.cpp()+")","sineWave",phase.reg(),0,0,"0");}
            if(name.equals("rand")){contextual=true;int site=serial++;for(int i=0;i<args.size();i++)args.set(i,cast(args.get(i),K.FLOAT));String code="context.random("+site+", "+String.join(", ",args.stream().map(E::cpp).toList())+")";return operation(K.FLOAT,K.FLOAT,code,args.size()==3?"randSeed":"rand",args.get(0).reg(),args.get(1).reg(),args.size()==3?args.get(2).reg():0,""+site);}
            K k=args.get(0).kind();for(int i=1;i<args.size();i++)k=common(k,args.get(i).kind());numeric(k);if(k==K.BOOL)k=K.INT;
            if(!Set.of("min","max","clamp","abs","sign").contains(name)&&k.integer())k=K.DOUBLE;
            for(int i=0;i<args.size();i++)args.set(i,cast(args.get(i),k));
            String code="easyknack::formulaCalc<"+k.cpp+">(easyknack::FormulaOp::"+name+", "+String.join(", ",args.stream().map(E::cpp).toList())+")";
            return operation(k,k,code,name,args.get(0).reg(),args.size()>1?args.get(1).reg():0,args.size()>2?args.get(2).reg():0,"0");
        }
        E assignment(E e,K target,boolean output){
            if(target==K.BOOL){if(e.kind()!=K.BOOL)throw fail("Use an explicit bool cast or comparison");return e;}
            numeric(e.kind());boolean narrow=e.kind()==K.DOUBLE&&target!=K.DOUBLE||e.kind()==K.FLOAT&&target.integer()||e.kind().integer()&&target.integer()&&e.kind()!=K.BOOL&&(e.kind().unsigned()!=target.unsigned()||e.kind().ordinal()>=K.LONG.ordinal()&&target.ordinal()<K.LONG.ordinal());
            if(narrow&&!output)throw fail("Narrowing conversion requires an explicit ("+target.name().toLowerCase()+") cast");return cast(e,target);
        }
        K common(K a,K b){numeric(a);numeric(b);if(a==K.DOUBLE||b==K.DOUBLE)return K.DOUBLE;if(a==K.FLOAT||b==K.FLOAT)return K.FLOAT;if(a==K.BOOL&&b!=K.BOOL)return b;if(b==K.BOOL&&a!=K.BOOL)return a;if(a==K.BOOL)a=K.INT;if(b==K.BOOL)b=K.INT;if(a.unsigned()!=b.unsigned())throw fail("Mixing signed and unsigned integers requires an explicit cast");return a.ordinal()>b.ordinal()?a:b;}
        E cast(E e,K k){numeric(e.kind());if(e.kind()==k)return e;return operation(k,k,"easyknack::formulaCast<"+k.cpp+">("+e.cpp()+")","cast",e.reg(),e.kind().ordinal(),0,"0");}
        E literal(Token t){
            String s=t.text(),suffix="";if(Set.of("f","F","u","U","l","L","ul","UL","ull","ULL").contains(now().text())&&t.end()==now().start()){suffix=now().text().toLowerCase();at++;}
            try{
                if(s.contains(".")||s.contains("e")||s.contains("E")||suffix.equals("f")){K k=suffix.equals("f")?K.FLOAT:K.DOUBLE;if(!suffix.isEmpty()&&!suffix.equals("f"))throw new NumberFormatException();return constant(k,stateBits(k,s));}
                BigInteger n=new BigInteger(s);K k=suffix.startsWith("u")?(suffix.length()>1?K.ULONG:n.bitLength()<=32?K.UINT:K.ULONG):suffix.equals("l")?K.LONG:n.bitLength()<=31?K.INT:n.bitLength()<=63?K.LONG:K.ULONG;
                return constant(k,stateBits(k,s));
            }catch(NumberFormatException e){throw fail("Invalid numeric literal");}
        }
        String stateBits(K k,String literal){
            try{
                if(k==K.BOOL){if(!literal.equals("true")&&!literal.equals("false"))throw new NumberFormatException();return literal.equals("true")?"1":"0";}
                if(k==K.FLOAT){float f=Float.parseFloat(literal);if(!Float.isFinite(f))throw new NumberFormatException();return Integer.toUnsignedString(Float.floatToRawIntBits(f));}
                if(k==K.DOUBLE){double d=Double.parseDouble(literal);if(!Double.isFinite(d))throw new NumberFormatException();return Long.toUnsignedString(Double.doubleToRawLongBits(d));}
                BigInteger n=new BigInteger(literal);int width=k==K.INT||k==K.UINT?32:64;BigInteger min=k.unsigned()?BigInteger.ZERO:BigInteger.ONE.shiftLeft(width-1).negate(),max=BigInteger.ONE.shiftLeft(k.unsigned()?width:width-1).subtract(BigInteger.ONE);if(n.compareTo(min)<0||n.compareTo(max)>0)throw new NumberFormatException();return n.mod(BigInteger.ONE.shiftLeft(width)).toString();
            }catch(NumberFormatException e){throw fail("Initializer is not a finite literal in the declared type's range");}
        }
        E constant(K k,String bits){return operation(k,k,"easyknack::formulaUnpack<"+k.cpp+">(UINT64_C("+bits+"))","constant",0,0,0,bits);}
        E operation(K result,K operation,String cpp,String op,int a,int b,int c,String value){if(cpp.length()>131072)throw fail("Expression is too complex");int r=registers++;emit(op,operation,r,a,b,c,value);return new E(result,cpp,r);}
        int emit(String op,K k,int target,int a,int b,int c,String value){if(ins.size()>=40000||registers>20000)throw fail("Formula exceeds instruction limit");ins.add(new I(op,k,target,a,b,c,value));return ins.size()-1;}
        void jump(int i,int destination){I old=ins.get(i);ins.set(i,new I(old.op(),old.kind(),destination,old.a(),old.b(),old.c(),old.value()));}
        void numeric(K k){if(k==K.BUFFER)throw fail("Expected a numeric value");}void requireBool(E e){if(e.kind()!=K.BOOL)throw fail("Expected bool; use a comparison or explicit cast");}
        void enter(){if(++depth>128)throw fail("Formula nesting exceeds 128 levels");}
        int precedence(String op){return switch(op){case "||"->0;case "&&"->1;case "==","!="->2;case "<",">","<=",">="->3;case "+","-"->4;case "*","/","%"->5;default->-1;};}
        Token identifier(){Token t=now();if(!t.text().matches("[A-Za-z_][A-Za-z0-9_]*"))throw fail("Expected identifier");at++;return t;}
        Token now(){return ts.get(Math.min(at,ts.size()-1));}boolean peek(String s){return now().text().equals(s);}boolean take(String s){if(!peek(s))return false;at++;return true;}void expect(String s){if(!take(s))throw fail("Expected '"+s+"'");}Failure fail(String s){return new Failure(now(),s);}
    }
}
