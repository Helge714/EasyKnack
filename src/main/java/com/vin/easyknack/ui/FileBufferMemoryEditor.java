package com.vin.easyknack.ui;
import com.vin.easyknack.nodes.Editor;
import com.vin.easyknack.audio.AudioFileDecoder;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.GLLabel;
/** Estimated decoded PCM allocation, without loading the audio into Java memory. */
public record FileBufferMemoryEditor(String reference,double start,double end) implements Editor {
 public GLNodeValue defaultValue(){return GLNodeValue.of("");}
 public String viewKey(){return getClass().getName();}
 public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
  var text=new GLLabel(text(label));return new Control(text,v->{});
 }
 public String text(String label){
  long bytes=0;try{var path=AudioFileDecoder.cached(reference);if(path!=null)try(var in=java.nio.file.Files.newInputStream(path)){var b=java.nio.ByteBuffer.wrap(in.readNBytes(24)).order(java.nio.ByteOrder.LITTLE_ENDIAN);int rate=b.getInt(8),channels=b.getInt(12);long frames=b.getLong(16);long first=Math.min(frames,Math.max(0,Math.round(start*rate))),last=end==0?frames:Math.min(frames,Math.round(end*rate));bytes=Math.max(0,last-first)*channels*4;}}catch(Exception ignored){}
  String size=bytes>=1_000_000_000?String.format(java.util.Locale.ROOT,"%.2f GB",bytes/1e9):String.format(java.util.Locale.ROOT,"%.2f MB",bytes/1e6);
  return label+": "+size;
 }
}
