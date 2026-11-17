package com.vin.easyknack.formula;

import java.util.*;

/** Tolerant statement layout shared by Enter and Format, including braceless bodies. */
final class FormulaIndentation {
    private final String source;
    private final List<FormulaEditorSymbols.Token> tokens=new ArrayList<>();
    private final Map<Integer,Integer> lines=new HashMap<>();
    private int at,next;private boolean pending;
    private FormulaIndentation(String source){
        this.source=source;var matcher=FormulaEditorSymbols.LEXER.matcher(source);
        while(matcher.find())if(!matcher.group().startsWith("//"))tokens.add(new FormulaEditorSymbols.Token(matcher.group(),matcher.start(),matcher.end()));
        while(at<tokens.size()){int before=at;statement(0,0);if(at==before)at++;}
    }
    static int next(String source){return new FormulaIndentation(source).next;}
    static String format(String source,int width){
        var layout=new FormulaIndentation(source);var result=new StringBuilder();String[] rows=source.split("\n",-1);int offset=0,previous=0;
        for(int i=0;i<rows.length;i++){
            String row=spacing(rows[i].strip());int level=layout.lines.getOrDefault(offset,previous);
            if(!row.isEmpty())result.append(" ".repeat(level*width)).append(row);
            previous=level;if(i+1<rows.length)result.append('\n');offset+=rows[i].length()+1;
        }
        return result.toString();
    }
    private static String spacing(String row){
        int comment=row.indexOf("//");String suffix=comment<0?"":row.substring(comment);
        String code=comment<0?row:row.substring(0,comment).stripTrailing();
        var matcher=java.util.regex.Pattern.compile("\\b([A-Za-z_][A-Za-z0-9_]*)[ \\t]*\\(").matcher(code);var normalized=new StringBuffer();
        while(matcher.find()){
            String name=matcher.group(1);String gap=Set.of("if","for","while","switch","return").contains(name)?" ":"";
            matcher.appendReplacement(normalized,java.util.regex.Matcher.quoteReplacement(name+gap+"("));
        }
        matcher.appendTail(normalized);
        code=normalized.toString().replaceAll("[ \\t]+"," ").replaceAll("\\([ \\t]+","(").replaceAll("[ \\t]+\\)",")")
            .replaceAll("[ \\t]*,[ \\t]*",", ").replaceAll("\\)[ \\t]*\\{",") {")
            .replaceAll("\\belse[ \\t]*\\{","else {").replaceAll("\\}[ \\t]*else\\b","} else")
            .replaceAll("\\{[ \\t]+\\}","{}").stripTrailing();
        return code+(suffix.isEmpty()?"":code.isEmpty()?suffix:" "+suffix);
    }
    private void mark(int level){if(at<tokens.size()){int line=source.lastIndexOf('\n',Math.max(0,tokens.get(at).start()-1))+1;lines.putIfAbsent(line,level);}next=level;}
    private boolean peek(String value){return at<tokens.size()&&tokens.get(at).text().equals(value);}
    private void statement(int level,int depth){
        if(depth>128||at>=tokens.size()){next=level;pending=true;return;}mark(level);
        if(peek("{")){
            at++;while(at<tokens.size()&&!peek("}")){int before=at;statement(level+1,depth+1);if(before==at)at++;}
            if(at==tokens.size()){next=level+1;pending=true;return;}mark(level);at++;next=level;pending=false;return;
        }
        if(peek("if")||peek("for")||peek("while")){
            at++;int parentheses=0;
            while(at<tokens.size()){
                String t=tokens.get(at++).text();if(t.equals("("))parentheses++;else if(t.equals(")")&&--parentheses==0)break;
            }
            if(at==tokens.size()){next=level+1;pending=true;return;}
            statement(peek("{")?level:level+1,depth+1);
            if(peek("else")){mark(level);at++;if(at==tokens.size()){next=level+1;pending=true;return;}statement(peek("{")||peek("if")?level:level+1,depth+1);}
            if(at==tokens.size()&&pending)return;next=level;return;
        }
        int parentheses=0;
        while(at<tokens.size()){
            String t=tokens.get(at).text();
            if(t.equals("{")&&parentheses==0){statement(level,depth+1);return;}
            if(t.equals("}")&&parentheses==0)return;
            at++;if(t.equals("("))parentheses++;else if(t.equals(")"))parentheses--;
            else if(t.equals(";")&&parentheses==0){next=level;pending=false;return;}
        }
        next=parentheses>0?level+1:level;pending=true;
    }
}
