package com.vin.easyknack.formula;

import static org.junit.jupiter.api.Assertions.*;
import java.net.URI;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.ui.*;

class FormulaEditorHelpTest {
    @Test void variablesUsePlainLexicalColorBeforeSemanticResultsArrive() {
        var language = new FormulaLanguage(ports, 3);
        String source = "state double phase=0;const double base=2;final double gain=signal;result=phase+base+gain+sampleRate;";
        for (var token : language.tokenize(source)) {
            String word = source.substring(token.start(), token.end());
            if (Set.of("phase", "base", "gain", "signal", "result", "sampleRate").contains(word)) assertEquals(GLCodeToken.Type.PLAIN, token.type(), word);
        }
    }
    @Test void qualifiersHaveCompletionHoverAndPersistentOrReadonlyEmphasis(){
        String text="state float phase=0;float f(final float x){return x;}const float lo=0.5;final float gain=signal;result=f(gain)+lo+phase+sampleRate;";
        var d=doc(text);var language=new FormulaLanguage(ports,3);
        try(var session=language.codeIntelligenceProvider().open(d,r->{})){
            var tokens=session.semanticTokens(d).toCompletableFuture().join();
            for(String name:List.of("lo","gain","x","signal","sampleRate"))assertTrue(tokens.stream().anyMatch(t->text.substring(t.start(),t.end()).equals(name)&&t.modifiers().contains("readonly")),name);
            assertTrue(tokens.stream().filter(t->text.substring(t.start(),t.end()).equals("phase")).allMatch(t->t.modifiers().contains("persistent")));
        }
        assertTrue(language.semanticModifierStyles().get("readonly").bold());assertTrue(language.semanticModifierStyles().get("persistent").italic());
        assertTrue(hover(text,text.lastIndexOf("gain")).contains("final float gain"));
        assertTrue(hover(text,text.lastIndexOf("lo")).contains("const float lo"));
        assertTrue(complete("const ",6).stream().anyMatch(c->c.label().equals("float")));
        var read=doc("result=buffer.read(0);");try(var session=language.codeIntelligenceProvider().open(read,r->{})){assertEquals(URI.create("easyknack-formula-help:/buffer-read"),session.definitions(read,16).toCompletableFuture().join().get(0).uri());}
    }
    @Test void definitionsResolveUserSymbolsAndBuiltinDocumentationButIgnoreComments(){
        String source="// shape(signal)\r\nresult=shape(signal);\r\nfloat shape(float x){float local=x*2;return local;}";var d=doc(source);
        try(var session=new FormulaLanguage(ports,3).codeIntelligenceProvider().open(d,r->{})){
            var function=session.definitions(d,source.indexOf("shape",source.indexOf("result="))).toCompletableFuture().join().get(0);
            assertEquals(d.uri(),function.uri());assertEquals(2,function.line());assertEquals(6,function.character());
            var parameter=session.definitions(d,source.indexOf("x*2")).toCompletableFuture().join().get(0);assertEquals(18,parameter.character());
            var local=session.definitions(d,source.lastIndexOf("local")).toCompletableFuture().join().get(0);assertEquals(source.indexOf("local=")-source.lastIndexOf('\n',source.indexOf("local="))-1,local.character());
            assertTrue(session.definitions(d,5).toCompletableFuture().join().isEmpty());
        }
        var builtin=doc("result=sin(signal);");try(var session=new FormulaLanguage(ports,3).codeIntelligenceProvider().open(builtin,r->{})){assertEquals(URI.create("easyknack-formula-help:/sin"),session.definitions(builtin,8).toCompletableFuture().join().get(0).uri());}
    }
    @Test void coreNavigationMovesTheCaretToTheFormulaFunctionDeclaration()throws Exception {
        String text="result=shape(signal);\nfloat shape(float x){return x*2;}";var editor=new java.util.concurrent.atomic.AtomicReference<GLCodeEditor>();
        java.awt.EventQueue.invokeAndWait(()->{
            var language=new FormulaLanguage(ports,3);var code=new GLCodeEditor(new GLTextDocument(text),false,language);editor.set(code);
            code.configureDocument(URI.create("easyknack-formula:/navigation"),language);code.setCaretPosition(text.indexOf("shape"));code.goToDefinition();
        });
        java.awt.EventQueue.invokeAndWait(()->{assertEquals(text.lastIndexOf("shape"),editor.get().getCaretPosition());editor.get().clearDiagnosticProvider();});
    }
    @Test void builtinAndUserFunctionsUseSemanticFunctionColors(){
        String text="float shape(float x){return sin(x);} result=shape(signal);";var d=doc(text);
        try(var session=new FormulaLanguage(ports,3).codeIntelligenceProvider().open(d,r->{})){
            var tokens=session.semanticTokens(d).toCompletableFuture().join();
            assertEquals(3,tokens.stream().filter(t->t.type().equals("function")).count());
            assertTrue(tokens.stream().anyMatch(t->t.start()==text.lastIndexOf("shape")));
        }
    }
    @Test void variablesParametersAndPortsReceiveSemanticRoles(){
        String text="float shape(float x){float amount=x*2;return amount;}float gain=1;result=shape(signal)*gain;";var d=doc(text);
        try(var session=new FormulaLanguage(ports,3).codeIntelligenceProvider().open(d,r->{})){
            var tokens=session.semanticTokens(d).toCompletableFuture().join();
            assertTrue(tokens.stream().anyMatch(t->t.start()==text.indexOf("x)")&&t.type().equals("parameter")));
            assertTrue(tokens.stream().anyMatch(t->t.start()==text.lastIndexOf("gain")&&t.type().equals("variable")));
            assertTrue(tokens.stream().anyMatch(t->t.start()==text.indexOf("signal")&&t.modifiers().contains("readonly")));
            assertTrue(tokens.stream().anyMatch(t->t.start()==text.indexOf("amount=")&&t.modifiers().contains("declaration")));
        }
    }
    @Test void userFunctionsParametersLoopsAndPersistentArgumentHints(){
        String source="float scale(float value,float factor){float local=value*factor;return local;} result=scale(signal, mix(1,2,0.5)); for(int i=0;i<3;i++){result+=i;}";
        assertTrue(complete(source,source.indexOf("return local")).stream().anyMatch(c->c.label().equals("factor")));
        assertFalse(complete(source,source.indexOf("result=scale")).stream().anyMatch(c->c.label().equals("factor")));
        assertTrue(complete(source,source.indexOf("result+=i")).stream().anyMatch(c->c.label().equals("i")));
        assertFalse(complete(source,source.length()).stream().anyMatch(c->c.label().equals("i")));
        assertTrue(complete("float scale(float x){return x;}result=sc",38).stream().anyMatch(c->c.label().equals("scale")));
        var d=doc(source);try(var session=new FormulaLanguage(ports,3).codeIntelligenceProvider().open(d,r->{})){
            var hints=session.inlayHints(d).toCompletableFuture().join();
            assertTrue(hints.stream().anyMatch(h->h.offset()==source.indexOf("signal, mix")&&h.label().equals("value:")));
            assertTrue(hints.stream().anyMatch(h->h.offset()==source.indexOf("mix(1")&&h.label().equals("factor:")));
            assertTrue(hints.size()>=5);
        }
        assertTrue(signature("float scale(float value,float factor){return value*factor;}result=scale(signal,").get(0).parameters().get(1).contains("factor"));
    }
    private final List<FormulaCompiler.Port> ports=List.of(FormulaCompilerTest.number("signal",false),FormulaCompilerTest.trigger("clock",false),FormulaCompilerTest.number("result",true),new FormulaCompiler.Port("buffer-id","buffer",FormulaCompiler.Type.BUFFER,false));
    private GLCodeDocument doc(String text){return new GLCodeDocument(URI.create("test:/formula"),"easyknack-formula",1,text);}
    private List<GLCodeCompletion> complete(String source,int offset){var d=doc(source);try(var s=new FormulaLanguage(ports,3).codeIntelligenceProvider().open(d,r->{})){return s.complete(d,offset,null).toCompletableFuture().join();}}
    private String hover(String source,int offset){var d=doc(source);try(var s=new FormulaLanguage(ports,3).codeIntelligenceProvider().open(d,r->{})){return s.hover(d,offset).toCompletableFuture().join().map(GLCodeHover::contents).orElse("");}}
    private List<GLCodeSignature> signature(String source){var d=doc(source);try(var s=new FormulaLanguage(ports,3).codeIntelligenceProvider().open(d,r->{})){return s.signatureHelp(d,source.length(),',').toCompletableFuture().join();}}
    @Test void writeposAliasMatchesExistingBufferPositionAcrossLanguageVersions(){
        for(int version:List.of(1,2,3)){
            var expected=FormulaCompiler.compile("result=buffer.writePosition;",ports,version);
            var actual=FormulaCompiler.compile("result=buffer.writepos;",ports,version);
            assertEquals(expected.bytecode(),actual.bytecode());
            assertEquals(expected.cpp(),actual.cpp());
            assertTrue(actual.sampleContext());
        }
        assertTrue(complete("buffer.",7).stream().anyMatch(c->c.label().equals("writepos")));
        assertTrue(hover("result=buffer.writepos;",17).contains("absolute"));
    }
    @Test void localsRespectBlocksBranchesAndDeclarationOrder(){
        String source="state double phase=0; unsigned long count=1; if(clock){float inner=1; result=inner;} if(clock) float branchOnly=2; result=phase; float future=0;";
        var inner=complete(source,source.indexOf("result=inner"));assertTrue(inner.stream().anyMatch(c->c.label().equals("inner")));
        var outer=complete(source,source.indexOf("result=phase"));var names=outer.stream().map(GLCodeCompletion::label).toList();
        assertTrue(names.containsAll(List.of("phase","count","signal")));assertFalse(names.contains("inner"));assertFalse(names.contains("branchOnly"));assertFalse(names.contains("future"));
        assertTrue(hover(source,source.indexOf("phase;")).contains("double phase — persistent state"));
        assertTrue(hover(source,source.indexOf("count=")).contains("ulong count"));
        assertTrue(hover(source,source.indexOf("inner;")).contains("float inner"));
        assertFalse(complete("float own=ow",12).stream().anyMatch(c->c.label().equals("own")));
    }
    @Test void memberHoverUsesReceiverAndCorrectTypes(){
        String source="double size=1; result=size+buffer.size+sampleRate;";
        assertTrue(hover(source,source.indexOf("size+")).contains("double size"));
        assertTrue(hover(source,source.indexOf("size+sampleRate")).startsWith("float"));
        assertTrue(hover(source,source.indexOf("sampleRate")).startsWith("double sampleRate"));
        assertTrue(hover("result=clock.active;",15).startsWith("bool"));
        assertEquals("",hover("result=signal.size;",16));
        assertEquals(List.of("rate","value"),complete("signal . ",9).stream().map(GLCodeCompletion::label).toList());
        assertEquals(List.of("active"),complete("clock.",6).stream().map(GLCodeCompletion::label).toList());
    }
    @Test void functionCompletionInsertsParenthesesAndReplacesWholeIdentifier(){
        var sine=complete("result=sin",10).stream().filter(c->c.label().equals("sineWave")).findFirst().orElseThrow();
        assertEquals("sineWave()",sine.insertText());assertEquals(9,sine.caretOffset());assertTrue(sine.detail().contains("phase"));assertFalse(sine.detail().contains("\n"));assertTrue(hover("result=sineWave(signal);",10).contains("cycles"));
        var existing=complete("result=sineWxxx(phase);",12).stream().filter(c->c.label().equals("sineWave")).findFirst().orElseThrow();
        assertEquals("sineWave",existing.insertText());assertEquals(7,existing.start());assertEquals(15,existing.end());
        assertTrue(hover("result=sin(signal);",9).contains("radians"));
        assertTrue(complete("state ",6).stream().allMatch(c->TypedFormulaCompiler.TYPES.contains(c.label())));
        assertEquals(List.of("int","long"),complete("unsigned ",9).stream().map(GLCodeCompletion::label).toList());
    }
    @Test void signatureHelpTracksNestedArgumentsAndBufferCalls(){
        var nested=signature("result=mix(signal, clamp(signal, 0, ").get(0);assertTrue(nested.label().startsWith("clamp("));assertEquals(2,nested.activeParameter());
        var outer=signature("result=mix(signal, clamp(signal, 0, 1), ").get(0);assertTrue(outer.label().startsWith("mix("));assertEquals(2,outer.activeParameter());
        var random=signature("result=rand(0, 1, ").get(0);assertEquals(2,random.activeParameter());assertTrue(random.parameters().get(2).contains("seed"));
        assertTrue(signature("result=buffer.read(0, ").get(0).documentation().contains("Clamp"));
        assertTrue(signature("result=signal.read(").isEmpty());
        assertTrue(signature("result=sin(signal);").isEmpty());
    }
    @Test void completionDetailsStayOnOneLineWhileDocumentationRemainsAvailable(){
        var hyperbolic=complete("result=tan",10).stream().filter(c->c.label().equals("tanh")).findFirst().orElseThrow();
        assertEquals("tanh()",hyperbolic.insertText());
        assertTrue(hover("result=tanh(signal);",9).contains("saturation"));
        assertTrue(signature("result=atanh(").get(0).documentation().contains("-1 < value < 1"));
        for(String source:List.of("result=", "buffer."))for(var item:complete(source,source.length())){
            assertFalse(item.detail().contains("\n"),item.label());assertFalse(item.detail().contains("\r"),item.label());
        }
        assertTrue(complete("buffer.re",9).get(0).detail().startsWith("read("));
        assertTrue(hover("buffer.read(0);",9).contains("Modes:"));
        assertTrue(signature("result=sineWave(").get(0).documentation().contains("cycles"));
    }
    @Test void commentsAndIncompleteDocumentsAreSafeAndDoNotLeakSymbols(){
        String source="// double fake=1; sin\nstate double phase=0; if(clock){ unsigned long counter=1; result=si";
        assertTrue(complete(source,15).isEmpty());assertEquals("",hover(source,12));assertTrue(signature("// sin(").isEmpty());
        assertTrue(complete("// state\nsi",11).stream().anyMatch(c->c.label().equals("sin")));
        for(int i=0;i<=source.length();i++){String partial=source.substring(0,i);assertDoesNotThrow(()->complete(partial,partial.length()));assertDoesNotThrow(()->signature(partial));assertDoesNotThrow(()->hover(partial,partial.length()));}
        assertFalse(complete(source,source.length()).stream().anyMatch(c->c.label().equals("fake")));
        var d=doc("float stale=1;result=stale;");try(var s=new FormulaLanguage(ports,3).codeIntelligenceProvider().open(d,r->{})){
            var changed=doc("double fresh=1;result=");s.update(changed);var names=s.complete(changed,changed.text().length(),null).toCompletableFuture().join().stream().map(GLCodeCompletion::label).toList();assertTrue(names.contains("fresh"));assertFalse(names.contains("stale"));
        }
    }
}
