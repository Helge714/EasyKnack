package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.Editor;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.GLButton;
import com.vingame.glswing.file.GLFileDialogRequest;
import java.nio.file.*;

/** Export a completed take; file I/O never runs on the UI/audio thread. */
public record RecordingExportEditor() implements Editor {
 public GLNodeValue defaultValue(){return GLNodeValue.of("");}
 public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){var control=new Save();return new Control(control,v->control.refresh());}
 static Path completed(AudioAnalysis.Frame frame){
  if(frame==null||frame.values().length<4||frame.frequency()<=0)return null;var v=frame.values();
  if(v[0]!=-9151||(v[1]!=2&&v[1]!=3)||v[3]<=0||v[3]>v.length-4)return null;
  byte[] bytes=new byte[(int)v[3]];for(int i=0;i<bytes.length;i++)bytes[i]=(byte)v[4+i];
  try{return Path.of(new String(bytes,java.nio.charset.StandardCharsets.UTF_8));}catch(InvalidPathException ex){return null;}
 }
 static void copy(Path source,Path destination)throws java.io.IOException{
  if(Files.exists(destination)&&Files.isSameFile(source,destination))return;
  var before=Files.readAttributes(source,java.nio.file.attribute.BasicFileAttributes.class);
  var temporary=Files.createTempFile(destination.toAbsolutePath().getParent(),".easyknack-recording-",".tmp");
  try{
   Files.copy(source,temporary,StandardCopyOption.REPLACE_EXISTING);var after=Files.readAttributes(source,java.nio.file.attribute.BasicFileAttributes.class);
   if(before.size()!=after.size()||!before.lastModifiedTime().equals(after.lastModifiedTime())||!java.util.Objects.equals(before.fileKey(),after.fileKey()))throw new java.io.IOException(AppText.text("recording.changed"));
   try{Files.move(temporary,destination,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ex){Files.move(temporary,destination,StandardCopyOption.REPLACE_EXISTING);}
  }finally{Files.deleteIfExists(temporary);}
 }
 public static final class Save extends com.vingame.glswing.component.GLContainer {
  final GLButton button=new GLButton(AppText.text("recording.saveAs"));
  private String id="";private boolean busy;private final Runnable refresh=this::refresh;
  public Save(){addGL(button);setPreferredGLSize(180,28);button.setEnabled(false);AudioAnalysis.listen(refresh);button.addActionListener(e->choose());}
  public void bind(String node){id=node;refresh();}
  private void refresh(){button.setText(AppText.text("recording.saveAs"));button.setEnabled(!busy&&completed(AudioAnalysis.frame(id))!=null);}
  @Override protected void resized(float w,float h){button.setBounds(0,0,w,h);}
  @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
  private void choose(){var source=completed(AudioAnalysis.frame(id));if(source==null)return;
   String extension=source.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".mp3")?"mp3":"wav";
   var files=new RememberingFileDialogs();files.show("audio.record.export",GLFileDialogRequest.save().title(AppText.text("recording.saveAs")).suggestedFileName(source.getFileName().toString()).filter(extension.toUpperCase(java.util.Locale.ROOT),extension).build(),getGLParent())
    .whenComplete((result,error)->java.awt.EventQueue.invokeLater(()->{files.close();if(error!=null){failed(error);return;}if(!result.approved())return;
     if(!source.equals(completed(AudioAnalysis.frame(id)))){failed(new IllegalStateException(AppText.text("recording.changed")));return;}
     var chosen=result.selectedPath();if(!chosen.toString().toLowerCase(java.util.Locale.ROOT).endsWith("."+extension))chosen=chosen.resolveSibling(chosen.getFileName()+"."+extension);final Path destination=chosen;
     busy=true;refresh();java.util.concurrent.CompletableFuture.runAsync(()->{try{copy(source,destination);}catch(java.io.IOException ex){throw new java.io.UncheckedIOException(ex);}})
      .whenComplete((unused,failure)->java.awt.EventQueue.invokeLater(()->{busy=false;if(failure!=null)failed(failure);else button.setToolTipText(destination.toString());refresh();}));
    }));
  }
  private void failed(Throwable failure){button.setToolTipText(failure.toString());System.err.println("EasyKnack recording export: "+failure);}
 }
}
