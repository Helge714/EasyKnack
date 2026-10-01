package com.vin.easyknack.audio;
import javax.sound.sampled.*;
import java.nio.*;
import java.nio.file.*;
import java.io.*;
/** On-demand audition through the system default output; bounded memory and one voice. */
public final class FileAudition {
 private static long generation;private static SourceDataLine active;
 private FileAudition(){}
 public static synchronized void stop(){generation++;if(active!=null){active.stop();active.flush();active.close();active=null;}}
 public static void play(Path path,double start,double end,java.util.function.Consumer<Throwable> done){
  stop();final long token; synchronized(FileAudition.class){token=generation;}
  var worker=new Thread(()->{SourceDataLine line=null;Throwable error=null;
   try(var file=new RandomAccessFile(path.toFile(),"r")){
    byte[] header=new byte[24];file.readFully(header);var h=ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);int channels=h.getInt(12);long frames=h.getLong(16);if(channels<1||channels>2)throw new IOException("Invalid channels");
    long first=Math.max(0,Math.min(frames,Math.round(start*48000))),last=Math.max(first,Math.min(frames,Math.round(end*48000)));file.seek(24+first*channels*4);
    var format=new AudioFormat(48000,16,channels,true,false);line=AudioSystem.getSourceDataLine(format);line.open(format,4096*channels);synchronized(FileAudition.class){if(token!=generation)return;active=line;}line.start();
    byte[] pcm=new byte[1024*channels*4],output=new byte[1024*channels*2];long cursor=first;
    while(cursor<last){synchronized(FileAudition.class){if(token!=generation)return;}int n=(int)Math.min(1024,last-cursor);file.readFully(pcm,0,n*channels*4);var b=ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN);
     for(int i=0;i<n;i++){double fade=Math.min(1,Math.min((cursor+i-first)/240.0,(last-1-cursor-i)/240.0));for(int ch=0;ch<channels;ch++){float v=b.getFloat();int sample=(int)(Math.max(-1,Math.min(1,Float.isFinite(v)?v:0))*32767*fade);int index=(i*channels+ch)*2;output[index]=(byte)sample;output[index+1]=(byte)(sample>>8);}}
     int bytes=n*channels*2,offset=0;while(offset<bytes){int written=line.write(output,offset,bytes-offset);if(written<=0)break;offset+=written;}cursor+=n;
    }line.drain();
   }catch(Exception ex){synchronized(FileAudition.class){if(token==generation)error=ex;}}
   finally{if(line!=null)line.close();synchronized(FileAudition.class){if(active==line)active=null;}Throwable failure=error;java.awt.EventQueue.invokeLater(()->done.accept(failure));}
  },"file-audition");worker.setDaemon(true);worker.start();
 }
}
