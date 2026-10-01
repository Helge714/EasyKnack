package com.vin.easyknack.audio;

import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.bytedeco.javacpp.Loader;

/** Control/background-thread media import. The native callback only sees prepared PCM. */
public final class AudioFileDecoder {
    private AudioFileDecoder(){}
    public record Asset(Path source,Path pcm,int channels,long frames){
        public String reference(){return source.toAbsolutePath()+"\n"+pcm.toAbsolutePath();}
        public double seconds(){return frames/48000.;}
    }
    public static Path source(String reference){return Path.of(reference.split("\n",-1)[0]);}
    public static Path cached(String reference){var parts=reference.split("\n",-1);return parts.length==2?Path.of(parts[1]):null;}
    public static String nativePath(String reference){
        if(reference.isBlank())return "none";
        var path=cached(reference);return path!=null&&Files.isRegularFile(path)?HexFormat.of().formatHex(path.toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8)):"none";
    }
    public static String executable(){return Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);}
    public static Asset decode(Path source,Path cache)throws IOException,InterruptedException{
        source=source.toAbsolutePath();if(!Files.isRegularFile(source))throw new FileNotFoundException(source.toString());
        Files.createDirectories(cache);
        var probe=Files.createTempFile(cache,"probe-",".txt");var raw=Files.createTempFile(cache,"decode-",".raw");var log=Files.createTempFile(cache,"decode-",".log");var pcm=Files.createTempFile(cache,"audio-",".ekpcm");
        boolean success=false;
        try{
            run(List.of(Loader.load(org.bytedeco.ffmpeg.ffprobe.class),"-v","error","-protocol_whitelist","file,pipe","-select_streams","a:0","-show_entries","stream=channels","-of","default=noprint_wrappers=1:nokey=1",source.toString()),probe,log);
            int channels;
            try{channels=Integer.parseInt(Files.readString(probe).trim());}catch(NumberFormatException e){throw new IOException("No readable audio stream",e);}
            if(channels<1||channels>2)throw new IOException("Only mono/stereo audio files are currently supported");
            run(List.of(executable(),"-nostdin","-v","error","-y","-protocol_whitelist","file,pipe","-i",source.toString(),"-map","0:a:0","-vn","-sn","-dn","-ar","48000","-ac",Integer.toString(channels),"-c:a","pcm_f32le","-f","f32le",raw.toString()),probe,log);
            long bytes=Files.size(raw),frames=bytes/(4*channels);
            if(frames==0||bytes%(4*channels)!=0)throw new IOException("Empty or invalid decoded audio");
            try(var out=Files.newOutputStream(pcm)){
                out.write(ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN).put("EKPCM001".getBytes(StandardCharsets.US_ASCII)).putInt(48000).putInt(channels).putLong(frames).array());
                Files.copy(raw,out);
            }
            success=true;return new Asset(source,pcm,channels,frames);
        }finally{Files.deleteIfExists(probe);Files.deleteIfExists(raw);Files.deleteIfExists(log);if(!success)Files.deleteIfExists(pcm);}
    }
    private static void run(List<String> command,Path output,Path error)throws IOException,InterruptedException{
        var process=new ProcessBuilder(command).redirectOutput(output.toFile()).redirectError(error.toFile()).start();
        try{
            if(!process.waitFor(30,TimeUnit.MINUTES))throw new IOException("Audio decoding timed out");
            if(process.exitValue()!=0){String detail;try(var in=Files.newInputStream(error)){detail=new String(in.readNBytes(4096),StandardCharsets.UTF_8);}throw new IOException("Audio decoding failed: "+detail.trim());}
        }finally{if(process.isAlive())process.destroyForcibly();}
    }
}
