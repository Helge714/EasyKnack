package com.vin.easyknack.formula;

import java.util.*;
import java.util.regex.Pattern;

/** Tolerant, read-only scope index for incomplete editor documents. Compilation remains authoritative. */
final class FormulaEditorSymbols {
    record Token(String text,int start,int end){}
    static final Pattern LEXER=Pattern.compile("//[^\\r\\n]*|[A-Za-z_][A-Za-z0-9_]*|\\S");
    static final class Scope {int end;Scope(int end){this.end=end;}}
    record Symbol(String name,String type,boolean state,int start,int end,int available,Scope scope){
        String description(){return type+" "+name+" — "+(state?"persistent state; retained between evaluations":"local variable");}
    }
    final String source;final List<Token> tokens=new ArrayList<>(),comments=new ArrayList<>();final List<Symbol> symbols=new ArrayList<>();int cursor;
    FormulaEditorSymbols(String source){
        this.source=source;var m=LEXER.matcher(source);while(m.find()){var t=new Token(m.group(),m.start(),m.end());if(t.text().startsWith("//"))comments.add(t);else tokens.add(t);}
        var root=new Scope(source.length()+1);while(cursor<tokens.size()){int before=cursor;statement(root,0);if(cursor==before)cursor++;}
    }
    boolean inComment(int offset){return comments.stream().anyMatch(t->offset>=t.start()&&offset<=t.end());}
    Map<String,Symbol> visible(int offset){var result=new LinkedHashMap<String,Symbol>();for(var s:symbols)if(offset>=s.available()&&offset<s.scope().end)result.put(s.name(),s);return result;}
    Optional<Symbol> symbol(String name,int offset){
        for(var s:symbols)if(s.name().equals(name)&&offset>=s.start()&&offset<s.end())return Optional.of(s);
        return Optional.ofNullable(visible(offset).get(name));
    }
    boolean typed(){for(int i=0;i<tokens.size();i++){String t=tokens.get(i).text();if(Set.of("double","int","long","uint","ulong","unsigned").contains(t))return true;if(t.equals("(")&&i+2<tokens.size()&&TypedFormulaCompiler.TYPES.contains(tokens.get(i+1).text())&&tokens.get(i+2).text().equals(")"))return true;}return false;}
    private void statement(Scope scope,int depth){
        if(cursor>=tokens.size()||depth>128)return;
        if(take("{")){var inner=new Scope(source.length()+1);while(cursor<tokens.size()&&!peek("}")){int before=cursor;statement(inner,depth+1);if(before==cursor)cursor++;}if(peek("}")){inner.end=tokens.get(cursor).start();cursor++;}return;}
        if(take("if")){if(take("(")){int level=1;while(cursor<tokens.size()&&level>0){String t=tokens.get(cursor++).text();if(t.equals("("))level++;if(t.equals(")"))level--;}if(level>0)return;}branch(depth);if(take("else"))branch(depth);return;}
        boolean state=take("state");String type=cursor<tokens.size()?tokens.get(cursor).text():"";
        if(TypedFormulaCompiler.TYPES.contains(type)){
            cursor++;if(type.equals("unsigned")){if(take("int"))type="uint";else if(take("long"))type="ulong";else return;}
            if(cursor<tokens.size()&&tokens.get(cursor).text().matches("[A-Za-z_][A-Za-z0-9_]*")){
                Token name=tokens.get(cursor++);int available=finish();symbols.add(new Symbol(name.text(),type,state,name.start(),name.end(),available,scope));return;
            }
        }
        finish();
    }
    private void branch(int depth){var scope=new Scope(source.length()+1);statement(scope,depth+1);if(cursor>0&&Set.of(";","}").contains(tokens.get(cursor-1).text()))scope.end=tokens.get(cursor-1).end();}
    private int finish(){while(cursor<tokens.size()){if(take(";"))return tokens.get(cursor-1).end();if(peek("{")||peek("}"))return source.length()+1;cursor++;}return source.length()+1;}
    private boolean peek(String text){return cursor<tokens.size()&&tokens.get(cursor).text().equals(text);}private boolean take(String text){if(!peek(text))return false;cursor++;return true;}
}
