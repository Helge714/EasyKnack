package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.Editor;
import com.vin.easyknack.audio.AudioFileDecoder;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.component.GLDialog;
import com.vingame.glswing.file.GLFileDialogRequest;
import java.nio.file.*;
import java.util.concurrent.*;
import java.awt.EventQueue;

/** File selection is an editor property; decoding never runs on the EDT. */
public record AudioFileEditor() implements Editor {

    static String failureDetails(Throwable error){
        var details=new StringBuilder();boolean codec=false;
        for(Throwable cause=error;cause!=null;cause=cause.getCause()){
            if(details.length()>0)details.append(" | ");
            details.append(cause.getClass().getSimpleName()).append(": ").append(String.valueOf(cause.getMessage()));
            if(cause instanceof LinkageError||cause instanceof ClassNotFoundException)codec=true;
        }
        return (codec?AppText.text("player.dependencies")+" | ":"")+details;
    }
    public GLNodeValue defaultValue(){return GLNodeValue.of("");}
    public void validate(GLNodeValue value){if(!(value.toJavaValue() instanceof String))throw new IllegalArgumentException("Audio file reference must be text");}
    public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
        class FileButton extends GLButton {
            String reference="",attempted="";boolean busy;
            FileButton(){super(AppText.text("player.choose"));addActionListener(e->{
                var files=new RememberingFileDialogs();
                files.show("audio.open",GLFileDialogRequest.open().title(AppText.text("player.choose")).filter("Audio","wav","mp3","flac","ogg","oga","m4a","aac").build(),getGLParent())
                    .whenComplete((choice,error)->EventQueue.invokeLater(()->{files.close();if(error!=null)failed(error);else if(choice.approved())load(choice.selectedPath());}));
            });}
            void failed(Throwable error){
                busy=false;setEnabled(true);String detail=failureDetails(error);setText(AppText.text("player.failed"));setToolTipText(detail);
                System.err.println("EasyKnack audio import: "+detail);error.printStackTrace();
                if(getGLParent()==null)return;
                var dialog=new GLDialog[1];
                var panel=new com.vingame.glswing.component.GLContainer(){
                    final GLLabel text=new GLLabel(detail),hint=new GLLabel(AppText.text("player.errorDetails"));final GLButton ok=new GLButton("OK");
                    {text.setSelectable(true);addGL(text);addGL(hint);addGL(ok);ok.addActionListener(e->dialog[0].close());}
                    @Override protected void resized(float w,float h){hint.setBounds(16,12,w-32,26);text.setBounds(16,46,w-32,32);ok.setBounds(w-100,h-40,84,28);}
                    @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
                };
                dialog[0]=new GLDialog(AppText.text("player.failed"),panel);dialog[0].setDecoration(GLDialog.Decoration.THEMED);dialog[0].setSize(780,140);dialog[0].show(getGLParent());
            }

            void load(Path path){
                if(busy)return;busy=true;setEnabled(false);setText(AppText.text("player.loading"));
                CompletableFuture.supplyAsync(()->{try{return AudioFileDecoder.decode(path,Path.of(System.getProperty("user.home"),".easyknack","audio-cache"));}catch(Exception ex){throw new CompletionException(ex);}})
                    .whenComplete((asset,error)->EventQueue.invokeLater(()->{busy=false;setEnabled(true);if(error!=null){failed(error.getCause());return;}reference=asset.reference();attempted=reference;changed.accept(GLNodeValue.of(reference));display();}));
            }
            void display(){if(!busy){setText(reference.isBlank()?AppText.text("player.choose"):AudioFileDecoder.source(reference).getFileName().toString());setToolTipText(reference.isBlank()?AppText.text("player.choose"):AudioFileDecoder.source(reference).toString());}}
            void update(GLNodeValue value){reference=(String)value.toJavaValue();display();
                if(!reference.isBlank()&&!reference.equals(attempted)&&AudioFileDecoder.nativePath(reference).equals("none")){attempted=reference;load(AudioFileDecoder.source(reference));}
            }
        }
        var button=new FileButton();return new Control(button,button::update);
    }
}
