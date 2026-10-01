package com.vin.easyknack.audio;
import java.nio.*;
import java.nio.file.*;
import java.io.*;
/** Bounded peak overview; zoom reads only its visible portion of the PCM cache. */
public record FileWaveform(long frames,int channels,float[][] low,float[][] high) {
 public static FileWaveform read(Path path)throws IOException{return read(path,0,1);}
 public static FileWaveform read(Path path,double from,double to)throws IOException{return read(path,from,to,()->false);}
 public static FileWaveform read(Path path,double from,double to,java.util.function.BooleanSupplier cancelled)throws IOException{
  try(var in=new RandomAccessFile(path.toFile(),"r")){
   byte[] header=new byte[24];in.readFully(header);var h=ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);int channels=h.getInt(12);long frames=h.getLong(16);
   if(channels<1||channels>2||frames<=0||!Double.isFinite(from)||!Double.isFinite(to)||from<0||to>1||from>=to)throw new IOException("Invalid PCM overview");
   long first=Math.min(frames-1,(long)(from*frames)),last=Math.max(first+1,Math.min(frames,(long)Math.ceil(to*frames))),count=last-first;
   int buckets=(int)Math.min(2048,count);float[][] low=new float[channels][buckets],high=new float[channels][buckets];
   for(int c=0;c<channels;c++){java.util.Arrays.fill(low[c],Float.POSITIVE_INFINITY);java.util.Arrays.fill(high[c],Float.NEGATIVE_INFINITY);}
   in.seek(24+first*channels*4);byte[] bytes=new byte[65536];long frame=0;
   while(frame<count){if(cancelled.getAsBoolean())throw new java.util.concurrent.CancellationException();int n=(int)Math.min(bytes.length/(4*channels),count-frame);int size=n*4*channels;in.readFully(bytes,0,size);var b=ByteBuffer.wrap(bytes,0,size).order(ByteOrder.LITTLE_ENDIAN);
    for(int i=0;i<n;i++,frame++){int bucket=(int)(frame*buckets/count);for(int c=0;c<channels;c++){float v=b.getFloat();if(!Float.isFinite(v))v=0;low[c][bucket]=Math.min(low[c][bucket],v);high[c][bucket]=Math.max(high[c][bucket],v);}}
   }
   return new FileWaveform(frames,channels,low,high);
  }
 }
 /** Nearest common channel crossing within 10 ms; no crossing means no movement. */
 public static double snap(Path path,double seconds,double minimum,double maximum)throws IOException{
  try(var in=new RandomAccessFile(path.toFile(),"r")){
   byte[] header=new byte[24];in.readFully(header);var h=ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);int channels=h.getInt(12),rate=h.getInt(8);long frames=h.getLong(16);
   if(channels<1||channels>2||rate!=48000||frames<2)return seconds;
   long target=Math.round(seconds*rate),first=Math.max(1,Math.max(target-480,(long)Math.ceil(minimum*rate))),last=Math.min(frames-1,Math.min(target+480,(long)Math.floor(maximum*rate)));
   if(last<first)return seconds;
   byte[] bytes=new byte[(int)(last-first+2)*channels*4];in.seek(24+(first-1)*channels*4);in.readFully(bytes);var b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
   long best=target,distance=Long.MAX_VALUE;
   for(long frame=first;frame<=last;frame++){boolean crossing=true;int index=(int)(frame-first)*channels;
    for(int c=0;c<channels;c++){float a=b.getFloat((index+c)*4),v=b.getFloat((index+channels+c)*4);crossing&=Float.isFinite(a)&&Float.isFinite(v)&&(a==0||v==0||Math.signum(a)!=Math.signum(v));}
    if(crossing&&Math.abs(frame-target)<distance){best=frame;distance=Math.abs(frame-target);}
   }
   return best/(double)rate;
  }
 }
}
