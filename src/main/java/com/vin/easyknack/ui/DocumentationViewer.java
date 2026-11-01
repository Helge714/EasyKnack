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
    record Position(String page,String anchor,float x,float y){}
    DocumentationPage content=new DocumentationPage(this::follow);
    final GLScrollPane scroll=new GLScrollPane(content);
    final GLButton back=new GLButton(AppText.text("help.back")),forward=new GLButton(AppText.text("help.forward"));
    final GLButton index=new GLButton(AppText.text("help.nodeReference")),manual=new GLButton(AppText.text("help.manual"));
    final GLButton copy=new GLButton(AppText.text("help.copyText")),close=new GLButton(AppText.text("help.close"));
    final GLLabel status=new GLLabel("");
    private final Deque<Position> past=new ArrayDeque<>(),future=new ArrayDeque<>();
    private final Runnable onClose;
    private Position current;
    private int revision;
    private volatile boolean disposed;
    private boolean loading;
    DocumentationViewer(Runnable onClose){
        this.onClose=onClose;
        for(var child:List.of(back,forward,index,manual,copy,close,status,scroll))addGL(child);
        status.setSelectable(true);
        back.addActionListener(e->back());forward.addActionListener(e->forward());
        index.addActionListener(e->openPage("nodes/"+NodeDocumentation.language(ApplicationSettings.locale())+"/index.md"));
        manual.addActionListener(e->openPage("user/README.md"));
        copy.addActionListener(e->java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content.document().plain()),null));
        close.addActionListener(e->onClose.run());setBounds(0,0,1100,740);buttons();
    }
    static void show(GLContainer owner,String page){
        var dialog=new GLDialog[1];var panel=new DocumentationViewer(()->dialog[0].close());
        dialog[0]=new GLDialog(AppText.text("help.nodeDocumentation"),panel);dialog[0].setDecoration(GLDialog.Decoration.THEMED);
        int width=1200,height=820;
        if(!java.awt.GraphicsEnvironment.isHeadless()){var screen=java.awt.Toolkit.getDefaultToolkit().getScreenSize();width=Math.max(320,Math.min(width,screen.width-80));height=Math.max(240,Math.min(height,screen.height-100));}
        dialog[0].setSize(width,height);dialog[0].closedFuture().whenComplete((v,e)->panel.disposed=true);
        dialog[0].show(owner);panel.openPage(page);
    }
    Position position(){return current==null?null:new Position(current.page(),"",scroll.getHorizontalScrollOffset(),scroll.getVerticalScrollOffset());}
    CompletableFuture<Void> openPage(String page){return navigate(new Position(page,"",0,0),true);}
    CompletableFuture<Void> follow(String link){
        if(current==null)return CompletableFuture.completedFuture(null);
        String target=NodeDocumentation.target(current.page(),link);String anchor=link.contains("#")?link.substring(link.indexOf('#')+1):"";
        if(target==null||!anchor.matches("[a-z0-9-]*")||!NodeDocumentation.exists(target))return CompletableFuture.completedFuture(null);
        return navigate(new Position(target,anchor,0,0),true);
    }
    CompletableFuture<Void> back(){if(past.isEmpty()||loading)return CompletableFuture.completedFuture(null);future.push(position());return navigate(past.pop(),false);}
    CompletableFuture<Void> forward(){if(future.isEmpty()||loading)return CompletableFuture.completedFuture(null);past.push(position());return navigate(future.pop(),false);}
    private CompletableFuture<Void> navigate(Position next,boolean remember){
        var finished=new CompletableFuture<Void>();
        if(NodeDocumentation.target(next.page(),"")==null||!NodeDocumentation.exists(next.page())){finished.completeExceptionally(new IOException("Unknown help page: "+next.page()));return finished;}
        if(remember&&current!=null){past.push(position());future.clear();}
        boolean same=current!=null&&current.page().equals(next.page())&&!loading;
        current=next;int request=++revision;
        if(same){scroll.setScrollOffset(next.x(),next.anchor().isEmpty()?next.y():content.anchor(next.anchor()));buttons();finished.complete(null);return finished;}
        loading=true;status.setText(AppText.text("help.loading"));content.show(new DocumentationPage.Document("",List.of()));scroll.setScrollOffset(0,0);buttons();
        CompletableFuture.supplyAsync(()->{
            try{return DocumentationPage.parse(NodeDocumentation.read(next.page()),next.page().contains("/de/")?"de":"en");}
            catch(IOException failure){throw new java.util.concurrent.CompletionException(failure);}
        }).whenComplete((document,error)->EventQueue.invokeLater(()->{
            if(disposed||request!=revision){finished.complete(null);return;}
            loading=false;
            if(error!=null){status.setText(error.getCause()==null?error.toString():error.getCause().getMessage());buttons();finished.completeExceptionally(error);return;}
            content=new DocumentationPage(this::follow);content.reflow(Math.max(240,getWidth()-64));content.show(document);
            scroll.setContent(content);status.setText(document.title());
            scroll.setScrollOffset(next.x(),next.anchor().isEmpty()?next.y():content.anchor(next.anchor()));buttons();finished.complete(null);
        }));
        return finished;
    }
    private void buttons(){back.setEnabled(!loading&&!past.isEmpty());forward.setEnabled(!loading&&!future.isEmpty());copy.setEnabled(!loading&&!content.document().blocks().isEmpty());}
    @Override public boolean capturesKeyPress(GLKeyEvent event){
        if(event.getKeyCode()==KeyEvent.VK_ESCAPE){onClose.run();event.consume();return true;}
        if(event.getOriginalEvent().isAltDown()&&event.getKeyCode()==KeyEvent.VK_LEFT){back();event.consume();return true;}
        if(event.getOriginalEvent().isAltDown()&&event.getKeyCode()==KeyEvent.VK_RIGHT){forward();event.consume();return true;}
        return false;
    }
    @Override protected void resized(float width,float height){
        if(scroll==null)return;boolean compact=width<850;float buttonWidth=Math.max(50,(width-40)/(compact?3:5));
        var tools=List.of(back,forward,index,manual,copy);for(int i=0;i<tools.size();i++)tools.get(i).setBounds(16+(i%(compact?3:5))*(buttonWidth+2),12+(compact&&i>=3?34:0),buttonWidth,28);
        float top=compact?84:50;scroll.setBounds(16,top,Math.max(1,width-32),Math.max(1,height-top-62));
        content.reflow(Math.max(240,width-64));status.setBounds(16,Math.max(0,height-44),Math.max(1,width-152),28);close.setBounds(Math.max(0,width-120),Math.max(0,height-44),104,28);
    }
    @Override protected void record(GLContext2D c){c.fillRect(0,0,getWidth(),getHeight(),getGLTheme().menuBarStyle().background());}
}
