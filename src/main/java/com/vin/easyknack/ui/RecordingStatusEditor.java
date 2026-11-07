package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.Editor;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.GLLabel;

public record RecordingStatusEditor() implements Editor {
 public GLNodeValue defaultValue(){return GLNodeValue.of("");}
 public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){return new Control(new Status(),v->{});}
 public static final class Status extends GLLabel {
  private String id="";private final Runnable refresh=this::refresh;
  public Status(){super(AppText.text("recording.ready"));AudioAnalysis.listen(refresh);setPreferredGLSize(180,28);}
  public void bind(String id){this.id=id;refresh();}
  private void refresh(){var frame=AudioAnalysis.frame(id);if(frame==null||frame.values().length<4||frame.values()[0]!=-9151)return;
   var v=frame.values();int state=(int)v[1],error=(int)v[2];setText(AppText.text("recording."+(state==1?"active":state==2?"saved":state==3?"error":"ready"))+String.format(java.util.Locale.ROOT," · %.2f s",frame.frequency()));
   int length=Math.max(0,Math.min((int)v[3],v.length-4));byte[] bytes=new byte[length];for(int i=0;i<length;i++)bytes[i]=(byte)v[4+i];var path=new String(bytes,java.nio.charset.StandardCharsets.UTF_8);
   setToolTipText(error==0?path:AppText.text("recording.error."+error)+"\n"+path);
  }
 }
}
