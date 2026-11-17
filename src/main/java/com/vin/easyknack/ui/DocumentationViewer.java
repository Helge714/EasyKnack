package com.vin.easyknack.ui;

import java.awt.EventQueue;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.vingame.glswing.component.*;
import com.vingame.glswing.input.*;
import com.vingame.glswing.rendering.GLContext2D;
import com.vingame.glswing.ui.*;

/** A themed GL dialog with local links and independent read-only browsing history. */
final class DocumentationViewer extends GLContainer implements GLKeyPressCaptureTarget {
    enum Language {
        APPLICATION("application"),GERMAN("de"),ENGLISH("en");
        final String id;Language(String id){this.id=id;}
        public String toString(){return this==APPLICATION?AppText.text("help.language.application"):this==GERMAN?"Deutsch":"English";}
        static Language read(String id){return Arrays.stream(values()).filter(v->v.id.equals(id)).findFirst().orElse(APPLICATION);}
    }
    final GLLabel languageLabel=new GLLabel(AppText.text("help.language"));
    final GLComboBox<Language> language=new GLComboBox<>(List.of(Language.values()));
    private Language selectedLanguage=Language.read(ApplicationSettings.documentationLanguage());
    private boolean selectingLanguage;
    private Locale contentLocale(){return selectedLanguage==Language.APPLICATION?ApplicationSettings.locale():Locale.forLanguageTag(selectedLanguage.id);}
    record Position(String page,String anchor,float x,float y){}
    DocumentationPage content=new DocumentationPage(this::follow);
    final GLScrollPane scroll=new GLScrollPane(content);
    final GLButton back=new GLButton(AppText.text("help.back")),forward=new GLButton(AppText.text("help.forward"));
    final GLButton index=new GLButton(AppText.text("help.referenceIndex")),manual=new GLButton(AppText.text("help.manual"));
    final GLButton find=new GLButton(AppText.text("help.find"));
    final GLButton copy=new GLButton(AppText.text("help.copyText")),close=new GLButton(AppText.text("help.close"));
    final GLLabel status=new GLLabel("");
    private final Deque<Position> past=new ArrayDeque<>(),future=new ArrayDeque<>();
    private final Runnable onClose;
    private final java.util.concurrent.CompletionStage<Void> readiness;
    private Position current;
    private int revision;
    private volatile boolean disposed;
    private boolean loading;
    DocumentationViewer(Runnable onClose){this(onClose,BackgroundPreparation.documentationReady());}
    DocumentationViewer(Runnable onClose,java.util.concurrent.CompletionStage<Void> readiness){
        this.onClose=onClose;this.readiness=readiness;
        for(var child:List.of(back,forward,index,manual,copy,find,languageLabel,language,close,status,scroll))addGL(child);
        language.setSelectedItem(selectedLanguage);
        language.addActionListener(e->{if(!selectingLanguage&&language.getSelectedItem()!=null)chooseLanguage(language.getSelectedItem());});
        configureSearch();find.addActionListener(e->findInPage());
        status.setSelectable(true);
        back.addActionListener(e->back());forward.addActionListener(e->forward());
        index.addActionListener(e->openPage("nodes/"+NodeDocumentation.language(contentLocale())+"/index.md"));
        manual.addActionListener(e->openPage("user/README.md"));
        copy.addActionListener(e->java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content.document().plain()),null));
        close.addActionListener(e->{content.closeSearchDialog();onClose.run();});setBounds(0,0,1100,740);buttons();
    }
    static void show(GLContainer owner,String page){show(owner,page,"");}
    static void show(GLContainer owner,String page,String anchor){
        var dialog=new GLDialog[1];var panel=new DocumentationViewer(()->dialog[0].close());
        dialog[0]=new GLDialog(AppText.text("help.nodeDocumentation"),panel);dialog[0].setDecoration(GLDialog.Decoration.THEMED);
        int width=1200,height=820;
        if(!java.awt.GraphicsEnvironment.isHeadless()){var screen=java.awt.Toolkit.getDefaultToolkit().getScreenSize();width=Math.max(320,Math.min(width,screen.width-80));height=Math.max(240,Math.min(height,screen.height-100));}
        dialog[0].setSize(width,height);dialog[0].closedFuture().whenComplete((v,e)->{panel.disposed=true;panel.content.closeSearchDialog();});
        dialog[0].show(owner);panel.openPage(page,anchor);
    }
    CompletableFuture<Void> chooseLanguage(Language next){
        try{ApplicationSettings.saveDocumentationLanguage(next.id);}
        catch(Exception failure){selectingLanguage=true;try{language.setSelectedItem(selectedLanguage);}finally{selectingLanguage=false;}status.setText(failure.getMessage());return CompletableFuture.failedFuture(failure);}
        selectedLanguage=next;selectingLanguage=true;try{language.setSelectedItem(next);}finally{selectingLanguage=false;}
        return current==null?CompletableFuture.completedFuture(null):navigate(new Position(current.page(),current.anchor(),0,0),false);
    }
    private String pageStatus(DocumentationPage.Document document,String page){
        return document.title()+(contentLocale().getLanguage().equals("de")&&!page.contains("/de/")?" \u2014 "+AppText.text("help.englishFallback"):"");
    }
    Position position(){return current==null?null:new Position(current.page(),"",scroll.getHorizontalScrollOffset(),scroll.getVerticalScrollOffset());}
    CompletableFuture<Void> openPage(String page){return openPage(page,"");}
    CompletableFuture<Void> openPage(String page,String anchor){return navigate(new Position(page,anchor,0,0),true);}
    CompletableFuture<Void> follow(String link){
        if(current==null)return CompletableFuture.completedFuture(null);
        String target=NodeDocumentation.target(current.page(),link);String anchor=link.contains("#")?link.substring(link.indexOf('#')+1):"";
        if(target==null||!anchor.matches("[a-z0-9-]*")||!NodeDocumentation.exists(target))return CompletableFuture.completedFuture(null);
        if(!target.equals(current.page())&&target.replace("/de/","/en/").equals(current.page().replace("/de/","/en/")))
            return chooseLanguage(target.contains("/de/")?Language.GERMAN:Language.ENGLISH);
        return navigate(new Position(target,anchor,0,0),true);
    }
    CompletableFuture<Void> back(){if(past.isEmpty()||loading)return CompletableFuture.completedFuture(null);future.push(position());return navigate(past.pop(),false);}
    CompletableFuture<Void> forward(){if(future.isEmpty()||loading)return CompletableFuture.completedFuture(null);past.push(position());return navigate(future.pop(),false);}
    private CompletableFuture<Void> navigate(Position requested,boolean remember){
        var next=new Position(NodeDocumentation.localizedPage(requested.page(),contentLocale()),requested.anchor(),requested.x(),requested.y());
        var finished=new CompletableFuture<Void>();
        if(NodeDocumentation.target(next.page(),"")==null||!NodeDocumentation.exists(next.page())){finished.completeExceptionally(new IOException("Unknown help page: "+next.page()));return finished;}
        if(remember&&current!=null){past.push(position());future.clear();}
        boolean same=current!=null&&current.page().equals(next.page())&&!loading;
        current=next;int request=++revision;
        if(same){status.setText(pageStatus(content.document(),next.page()));scroll.setScrollOffset(next.x(),next.anchor().isEmpty()?next.y():content.anchor(next.anchor()));buttons();finished.complete(null);return finished;}
        loading=true;status.setText(AppText.text("help.loading"));content.show(new DocumentationPage.Document("",List.of()));scroll.setScrollOffset(0,0);buttons();
        readiness.thenCompose(ignored->CompletableFuture.supplyAsync(()->{
            try{return DocumentationPreparation.document(next.page());}
            catch(IOException failure){throw new java.util.concurrent.CompletionException(failure);}
        })).whenComplete((document,error)->EventQueue.invokeLater(()->{
            if(disposed||request!=revision){finished.complete(null);return;}
            loading=false;
            if(error!=null){status.setText(error.getCause()==null?error.toString():error.getCause().getMessage());buttons();finished.completeExceptionally(error);return;}
            content=new DocumentationPage(this::follow);content.reflow(Math.max(240,getWidth()-64));content.show(document);
            scroll.setContent(content);configureSearch();status.setText(pageStatus(document,next.page()));
            scroll.setScrollOffset(next.x(),next.anchor().isEmpty()?next.y():content.anchor(next.anchor()));buttons();finished.complete(null);
        }));
        return finished;
    }
    private void configureSearch(){content.setSearchMatchListener(hit->scroll.scrollRectToVisible(hit.x(),hit.y(),hit.width(),hit.height()));}
    private void findInPage(){if(!loading)content.showSearchDialog(this);}
    private void buttons(){find.setEnabled(!loading&&!content.document().blocks().isEmpty());back.setEnabled(!loading&&!past.isEmpty());forward.setEnabled(!loading&&!future.isEmpty());copy.setEnabled(!loading&&!content.document().blocks().isEmpty());}
    @Override public boolean capturesKeyPress(GLKeyEvent event){
        if(getGLInputMap().matches(GLInputActions.TEXT_FIND,event)){findInPage();event.consume();return true;}
        if(event.getKeyCode()==KeyEvent.VK_F3){content.findNext(event.getOriginalEvent().isShiftDown());event.consume();return true;}
        if(event.getKeyCode()==KeyEvent.VK_ESCAPE){content.closeSearchDialog();onClose.run();event.consume();return true;}
        if(event.getOriginalEvent().isAltDown()&&event.getKeyCode()==KeyEvent.VK_LEFT){back();event.consume();return true;}
        if(event.getOriginalEvent().isAltDown()&&event.getKeyCode()==KeyEvent.VK_RIGHT){forward();event.consume();return true;}
        return false;
    }
    @Override protected void resized(float width,float height){
        if(scroll==null)return;boolean compact=width<850;float buttonWidth=Math.max(50,(width-40)/(compact?3:6));
        var tools=List.of(back,forward,index,manual,copy,find);for(int i=0;i<tools.size();i++)tools.get(i).setBounds(16+(i%(compact?3:6))*(buttonWidth+2),12+(compact&&i>=3?34:0),buttonWidth,28);
        float languageY=compact?80:46;
        boolean narrow=width<500;
        float labelWidth=narrow?Math.max(1,width-32):150;
        languageLabel.setBounds(16,languageY,labelWidth,28);
        float languageX=narrow?16:176;
        language.setBounds(languageX,languageY+(narrow?32:0),Math.max(1,Math.min(230,width-languageX-16)),28);
        float top=languageY+(narrow?68:38);scroll.setBounds(16,top,Math.max(1,width-32),Math.max(1,height-top-62));
        content.reflow(Math.max(240,width-64));status.setBounds(16,Math.max(0,height-44),Math.max(1,width-152),28);close.setBounds(Math.max(0,width-120),Math.max(0,height-44),104,28);
    }
    @Override protected void record(GLContext2D c){c.fillRect(0,0,getWidth(),getHeight(),getGLTheme().menuBarStyle().background());}
}
