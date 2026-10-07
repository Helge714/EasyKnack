package com.vin.easyknack.audio;

import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    public static Asset decode(Path source,Path cache)throws IOException,InterruptedException{
        source=source.toAbsolutePath();if(!Files.isRegularFile(source))throw new FileNotFoundException(source.toString());
        String name=source.getFileName().toString().toLowerCase(Locale.ROOT);
        if(!name.endsWith(".wav")&&!name.endsWith(".mp3"))throw new IOException("Only WAV and MP3 files are supported; convert other formats before importing.");
        Files.createDirectories(cache);
        var log=Files.createTempFile(cache,"decode-",".log");var pcm=Files.createTempFile(cache,"audio-",".ekpcm");
        boolean success=false;
        try{
            Path host=AudioHostClient.defaultExecutable();
            if(!Files.isRegularFile(host))throw new IOException("Audio decoder missing; rebuild or reinstall the EasyKnack audio host.");
            run(List.of(host.toAbsolutePath().toString(),"--decode-audio",pathHex(source),pathHex(pcm)),log);
            byte[] header;try(var in=Files.newInputStream(pcm)){header=in.readNBytes(24);}
            if(header.length!=24)throw new IOException("Invalid decoded audio header");
            var data=ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
            if(!new String(header,0,8,StandardCharsets.US_ASCII).equals("EKPCM001")||data.getInt(8)!=48000)throw new IOException("Invalid decoded audio format");
            int channels=data.getInt(12);long frames=data.getLong(16),bytes=Files.size(pcm)-24;
            if(channels<1||channels>2||frames<=0||bytes%(4*channels)!=0||frames!=bytes/(4*channels))throw new IOException("Empty or invalid decoded audio");
            success=true;return new Asset(source,pcm,channels,frames);
        }finally{Files.deleteIfExists(log);if(!success)Files.deleteIfExists(pcm);}
    }
    private static String pathHex(Path path){return HexFormat.of().formatHex(path.toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8));}
    private static void run(List<String> command,Path error)throws IOException,InterruptedException{
        var process=new ProcessBuilder(command).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(error.toFile()).start();
        try{
            if(!process.waitFor(30,TimeUnit.MINUTES))throw new IOException("Audio decoding timed out");
            if(process.exitValue()!=0){String detail;try(var in=Files.newInputStream(error)){detail=new String(in.readNBytes(4096),StandardCharsets.UTF_8);}throw new IOException("Audio decoding failed: "+detail.trim());}
        }finally{if(process.isAlive()){process.destroyForcibly();process.waitFor();}}
    }
}
