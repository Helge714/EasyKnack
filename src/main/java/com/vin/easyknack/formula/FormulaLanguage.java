package com.vin.easyknack.formula;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.vingame.glswing.ui.*;

/** Core code-editor integration using the same parser as native code generation. */
public final class FormulaLanguage implements GLCodeLanguage {
    private final List<FormulaCompiler.Port> ports; private final int version;
    public FormulaLanguage(List<FormulaCompiler.Port> ports){this(ports,2);} public FormulaLanguage(List<FormulaCompiler.Port> ports,int version){this.ports=List.copyOf(ports);this.version=version;}
    @Override public String id(){return "easyknack-formula";}
    @Override public boolean supportsAutomaticCompletion(){return true;}
    @Override public String indentationAfter(String source,int offset,int width){return " ".repeat(FormulaIndentation.next(source.substring(0,offset))*width);}
    @Override public String formatSource(String source,int width){return FormulaIndentation.format(source,width);}
    @Override public List<GLCodeToken> tokenize(String source){
        // Keep highlighting usable while the user is in the middle of invalid syntax.
        var result=new ArrayList<GLCodeToken>();
        var pattern=java.util.regex.Pattern.compile("//[^\\r\\n]*|[A-Za-z_][A-Za-z0-9_]*|(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?");
        var m=pattern.matcher(source);while(m.find()){
            String s=m.group();var type=s.startsWith("//")?GLCodeToken.Type.COMMENT:TypedFormulaCompiler.TYPES.contains(s)?GLCodeToken.Type.TYPE:Character.isDigit(s.charAt(0))||s.charAt(0)=='.'?GLCodeToken.Type.NUMBER:Set.of("state","if","else","for","return","true","false","pi","tau").contains(s)||FormulaCompiler.CONTEXT.contains(s)||FormulaCompiler.FUNCTIONS.containsKey(s)?GLCodeToken.Type.KEYWORD:GLCodeToken.Type.PLAIN;
            result.add(new GLCodeToken(m.start(),m.end(),type));
        }return List.copyOf(result);
    }
    @Override public List<GLCodeDiagnostic> diagnose(String source){
        try{FormulaCompiler.compile(source,ports,version);return List.of();}
        catch(FormulaCompiler.Failure e){
            // EOF parser errors are insertion points; Core diagnostics require a nonempty range.
            // Anchor to the last character, or a virtual first character for an empty document.
            int start=Math.max(0,Math.min(e.start(),source.length()-1));
            int end=Math.max(start+1,Math.min(e.end(),source.length()));
            return List.of(new GLCodeDiagnostic(start,end,GLCodeDiagnostic.Severity.ERROR,e.getMessage()));
        }
    }
    @Override public GLCodeDiagnosticProvider codeIntelligenceProvider(){return (document,listener)->new GLCodeDiagnosticProvider.Session(){
        private FormulaEditorSymbols index;
        private FormulaEditorSymbols index(GLCodeDocument d){if(index==null||!index.source.equals(d.text()))index=new FormulaEditorSymbols(d.text());return index;}
        {update(document);}
        @Override public void update(GLCodeDocument d){index(d);listener.accept(new GLCodeDiagnosticProvider.Result(d.version(),diagnose(d.text())));}
        @Override public CompletableFuture<List<GLCodeCompletion>> complete(GLCodeDocument d,int requested,Character trigger){
            String text=d.text();int offset=clamp(requested,text),start=offset,end=offset;var symbols=index(d);
            if(symbols.inComment(offset))return CompletableFuture.completedFuture(List.of());
            while(start>0&&Character.isJavaIdentifierPart(text.charAt(start-1)))start--;
            while(end<text.length()&&Character.isJavaIdentifierPart(text.charAt(end)))end++;
            String prefix=text.substring(start,offset),previous="";for(var t:symbols.tokens){if(t.end()>start)break;previous=t.text();}boolean typed=version>=3||symbols.typed();
            var names=new TreeMap<String,String>();var callable=new HashSet<String>();String owner=memberOwner(text,start);
            if(owner!=null){
                names.putAll(members(owner));if(names.containsKey("read"))callable.add("read");
            }else if(previous.equals("unsigned")){
                names.put("int",FormulaEditorHelp.type("uint"));names.put("long",FormulaEditorHelp.type("ulong"));
            }else if(previous.equals("state")){
                TypedFormulaCompiler.TYPES.forEach(n->names.put(n,FormulaEditorHelp.type(n)));
            }else if(TypedFormulaCompiler.TYPES.contains(previous)){
                return CompletableFuture.completedFuture(List.of()); // A new name belongs here, not an existing symbol.
            }else{
                for(var p:ports)names.put(p.name(),portDescription(p));
                symbols.visible(start).forEach((n,s)->names.put(n,s.description()));
                FormulaCompiler.CONTEXT.forEach(n->names.put(n,contextDescription(n,typed)));
                FormulaCompiler.FUNCTIONS.keySet().forEach(n->{names.put(n,FormulaEditorHelp.function(n,typed).description());callable.add(n);});
                symbols.functions.forEach((n,f)->{names.put(n,f.call().description());callable.add(n);});
                TypedFormulaCompiler.TYPES.forEach(n->names.put(n,FormulaEditorHelp.type(n)));
                for(String n:List.of("state","if","else","for","return","true","false","pi","tau"))names.put(n,keyword(n,typed));
                for(String n:List.of("Clamp","Wrap","Zero","Linear","Nearest","Audio","Control"))names.putIfAbsent(n,option(n));
            }
            var proposals=new ArrayList<GLCodeCompletion>();int next=end;while(next<text.length()&&Character.isWhitespace(text.charAt(next)))next++;boolean parenthesis=next<text.length()&&text.charAt(next)=='(';
            for(var e:names.entrySet())if(e.getKey().startsWith(prefix)){
                boolean call=callable.contains(e.getKey())&&!parenthesis;String insert=e.getKey()+(call?"()":"");
                // Core renders completion details in a single menu row; documentation belongs in hover/signature help.
                String detail=e.getValue().lines().findFirst().orElse("");
                proposals.add(new GLCodeCompletion(e.getKey(),detail,insert,start,end,e.getKey().length()+(call?1:0),List.of()));
            }
            return CompletableFuture.completedFuture(List.copyOf(proposals));
        }
        @Override public CompletableFuture<Optional<GLCodeHover>> hover(GLCodeDocument d,int requested){
            String text=d.text();int offset=clamp(requested,text),start=offset,end=offset;var symbols=index(d);
            if(symbols.inComment(offset))return CompletableFuture.completedFuture(Optional.empty());
            while(start>0&&Character.isJavaIdentifierPart(text.charAt(start-1)))start--;while(end<text.length()&&Character.isJavaIdentifierPart(text.charAt(end)))end++;
            if(start==end)return CompletableFuture.completedFuture(Optional.empty());
            String name=text.substring(start,end),owner=memberOwner(text,start),description=null;boolean typed=version>=3||symbols.typed();
            if(owner!=null)description=members(owner).get(name);
            else{
                var local=symbols.symbol(name,start);if(local.isPresent())description=local.get().description();
                if(description==null)for(var p:ports)if(p.name().equals(name))description=portDescription(p);
                if(description==null&&FormulaCompiler.FUNCTIONS.containsKey(name))description=FormulaEditorHelp.function(name,typed).description();
                if(description==null&&symbols.functions.containsKey(name))description=symbols.functions.get(name).call().description();
                if(description==null&&FormulaCompiler.CONTEXT.contains(name))description=contextDescription(name,typed);
                if(description==null)description=FormulaEditorHelp.type(name);
                if(description==null)description=keyword(name,typed);
                if(description==null&&Set.of("Clamp","Wrap","Zero","Linear","Nearest","Audio","Control").contains(name))description=option(name);
            }
            return CompletableFuture.completedFuture(description==null?Optional.empty():Optional.of(new GLCodeHover(start,end,description)));
        }
        @Override public CompletableFuture<List<GLCodeSignature>> signatureHelp(GLCodeDocument d,int requested,Character trigger){
            int offset=clamp(requested,d.text());var symbols=index(d);if(symbols.inComment(offset))return CompletableFuture.completedFuture(List.of());
            record CallFrame(FormulaEditorHelp.Call call,int argument){}
            var stack=new ArrayList<CallFrame>();var ts=symbols.tokens;boolean typed=version>=3||symbols.typed();
            for(int i=0;i<ts.size()&&ts.get(i).start()<offset;i++){
                String token=ts.get(i).text();if(token.equals("(")){
                    FormulaEditorHelp.Call call=null;if(i>0){String name=ts.get(i-1).text();String owner=memberOwner(d.text(),ts.get(i-1).start());
                        if(owner!=null){if(name.equals("read")&&members(owner).containsKey("read"))call=FormulaEditorHelp.read(version);}
                        else call=symbols.functions.containsKey(name)?symbols.functions.get(name).call():FormulaEditorHelp.function(name,typed);}
                    stack.add(new CallFrame(call,0));
                }else if(token.equals(")")){if(!stack.isEmpty())stack.remove(stack.size()-1);}
                else if(token.equals(",")&&!stack.isEmpty()){var f=stack.get(stack.size()-1);stack.set(stack.size()-1,new CallFrame(f.call(),f.argument()+1));}
                else if(token.equals(";")||token.equals("{")||token.equals("}"))stack.clear();
            }
            for(int i=stack.size()-1;i>=0;i--){var f=stack.get(i);if(f.call()!=null)return CompletableFuture.completedFuture(List.of(f.call().signature(f.argument())));}
            return CompletableFuture.completedFuture(List.of());
        }
        @Override public void close(){index=null;}
        @Override public CompletableFuture<List<GLCodeSemanticToken>> semanticTokens(GLCodeDocument d){
            var symbols=index(d);var tokens=new ArrayList<GLCodeSemanticToken>();
            for(int i=0;i<symbols.tokens.size();i++){
                var token=symbols.tokens.get(i);
                if((symbols.functions.containsKey(token.text())||FormulaCompiler.FUNCTIONS.containsKey(token.text()))&&i+1<symbols.tokens.size()&&symbols.tokens.get(i+1).text().equals("(")){
                    tokens.add(new GLCodeSemanticToken(token.start(),token.end(),"function",Set.of()));
                    continue;
                }
                var symbol=symbols.symbol(token.text(),token.start()).orElse(null);
                if(symbol!=null){
                    boolean parameter=symbols.functions.values().stream().flatMap(f->f.parameters().stream()).anyMatch(p->p.start()==symbol.start());
                    tokens.add(new GLCodeSemanticToken(token.start(),token.end(),parameter?"parameter":"variable",!parameter&&token.start()==symbol.start()?Set.of("declaration"):Set.of()));
                }else for(var port:ports)if(port.name().equals(token.text())){
                    tokens.add(new GLCodeSemanticToken(token.start(),token.end(),"variable",port.output()?Set.of():Set.of("readonly")));break;
                }
            }
            return CompletableFuture.completedFuture(List.copyOf(tokens));
        }
        @Override public CompletableFuture<List<GLCodeInlayHint>> inlayHints(GLCodeDocument d){
            var symbols=index(d);var ts=symbols.tokens;var hints=new ArrayList<GLCodeInlayHint>();boolean typed=version>=3||symbols.typed();
            for(int i=1;i<ts.size();i++)if(ts.get(i).text().equals("(")){
                var name=ts.get(i-1);String owner=memberOwner(d.text(),name.start());FormulaEditorHelp.Call call;
                if(owner!=null)call=name.text().equals("read")&&members(owner).containsKey("read")?FormulaEditorHelp.read(version):null;
                else if(symbols.functions.containsKey(name.text())){var function=symbols.functions.get(name.text());if(name.start()==function.start())continue;call=function.call();}
                else call=FormulaEditorHelp.function(name.text(),typed);
                if(call==null)continue;int level=0,argument=0;boolean start=true;
                for(int j=i+1;j<ts.size();j++){
                    var t=ts.get(j);if(level==0&&Set.of(")",";","{").contains(t.text()))break;
                    if(start&&argument<call.parameters().size()){
                        String label=call.parameters().get(argument).replaceAll("\\s*\\(.*","").strip();int space=label.lastIndexOf(' ');if(space>=0)label=label.substring(space+1);
                        if(!t.text().equals(label))hints.add(new GLCodeInlayHint(t.start(),label+":"));start=false;
                    }
                    if(t.text().equals("("))level++;else if(t.text().equals(")"))level--;else if(t.text().equals(",")&&level==0){argument++;start=true;}
                }
            }
            return CompletableFuture.completedFuture(List.copyOf(hints));
        }
    };}
    private Map<String,String> members(String owner){
        var names=new LinkedHashMap<String,String>();for(var p:ports)if(!p.output()&&p.name().equals(owner))switch(p.type()){
            case BUFFER->{names.put("read",FormulaEditorHelp.read(version).description());names.put("size","float — capacity in samples; zero for an empty buffer.");names.put("begin","float — oldest available absolute sample position.");names.put("end","float — position after the newest available sample.");names.put("writePosition","float — next write position; fixed at end for file buffers.");names.put("writepos","float — alias of writePosition; absolute next write sample position.");names.put("sampleRate","float — buffer sample rate in Hz.");}
            case NUMBER->{names.put("value","float — current sample or held Control value.");names.put("rate","float — Audio (1) or Control (0).");}
            case BOOLEAN->names.put("active","bool — trigger at the current sample.");
        }return names;
    }
    private static int clamp(int offset,String text){return Math.max(0,Math.min(offset,text.length()));}
    private static String memberOwner(String text,int start){
        int dot=start-1;while(dot>=0&&Character.isWhitespace(text.charAt(dot)))dot--;if(dot<0||text.charAt(dot)!='.')return null;
        int end=dot;while(end>0&&Character.isWhitespace(text.charAt(end-1)))end--;int begin=end;while(begin>0&&Character.isJavaIdentifierPart(text.charAt(begin-1)))begin--;return text.substring(begin,end);
    }
    private static String portDescription(FormulaCompiler.Port p){return (p.type()==FormulaCompiler.Type.NUMBER?"float":p.type()==FormulaCompiler.Type.BOOLEAN?"bool":"Buffer")+" "+p.name()+" — "+(p.output()?"output":"read-only input")+"; port ID: "+p.id();}
    private static String contextDescription(String name,boolean typed){String type=typed?(name.equals("sampleRate")?"double":"ulong"):"float";return type+" "+name+" — "+switch(name){case "sampleRate"->"sample rate in Hz.";case "blockSize"->"full audio block length in samples, including during feedback processing.";case "samplePosition"->"samples processed by this node; retained across live edits; advances a full block at Control rate.";default->"zero-based sample offset in the current audio block.";};}
    private static String keyword(String name,boolean typed){return switch(name){case "state"->"state type name = literal; persistent per processor. Matching name/type/initializer retains state across live edits.";case "if"->"if (bool condition) statement; else statement; braces are optional.";case "for"->"for (int i=0; i<8; i++) { ... } — fixed integer bounds, at most 1024 iterations; counter is read-only in the body.";case "return"->"return expression; — return a value from a user-defined function.";case "else"->"Alternative branch when the if condition is false.";case "true","false"->"bool constant: "+name;case "pi"->(typed?"double":"float")+" pi — π radians (half a turn).";case "tau"->(typed?"double":"float")+" tau — 2π radians (one turn).";default->null;};}
    private static String option(String name){return switch(name){case "Clamp"->"Buffer read: hold the nearest valid sample outside the available range.";case "Wrap"->"Buffer read: wrap the position into the available range.";case "Zero"->"Buffer read: return zero outside the available range.";case "Linear"->"Buffer read: linear interpolation between adjacent samples.";case "Nearest"->"Buffer timeline read: nearest sample (legacy boolean false uses the lower index).";case "Audio"->"Signal rate constant: 1.";default->"Signal rate constant: 0.";};}
}
