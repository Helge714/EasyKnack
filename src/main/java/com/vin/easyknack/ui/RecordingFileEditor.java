package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.Editor;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.GLButton;
import com.vingame.glswing.file.GLFileDialogRequest;

/** A recording target, never an audio import/decode operation. */
public record RecordingFileEditor() implements Editor {
 public GLNodeValue defaultValue(){return GLNodeValue.of("");}
 public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
  var button=new GLButton(AppText.text("recording.choose"));
  button.addActionListener(e->{var files=new RememberingFileDialogs();files.show("audio.record",GLFileDialogRequest.save().title(AppText.text("recording.choose")).filter("WAV","wav").filter("MP3","mp3").build(),button.getGLParent())
   .whenComplete((result,error)->java.awt.EventQueue.invokeLater(()->{files.close();if(error!=null){button.setToolTipText(error.toString());return;}if(result.approved()){
    var path=result.selectedPath();var name=path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);if(!name.endsWith(".wav")&&!name.endsWith(".mp3"))path=path.resolveSibling(path.getFileName()+".wav");changed.accept(GLNodeValue.of(path.toAbsolutePath().toString()));
   }}));});
  return new Control(button,v->{var path=(String)v.toJavaValue();button.setText(path.isBlank()?AppText.text("recording.choose"):java.nio.file.Path.of(path).getFileName().toString());button.setToolTipText(path);});
 }
}
