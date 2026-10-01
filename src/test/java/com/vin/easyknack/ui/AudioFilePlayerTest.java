package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import com.vin.easyknack.audio.AudioFileDecoder;
import com.vin.easyknack.nodes.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class AudioFilePlayerTest {
    @TempDir Path temp;
    @Test void realFormatsDecodeToFinitePcmWithoutChangingMonoGain()throws Exception{
        for(String extension:List.of("wav","mp3","flac","ogg","m4a","aac")){
            var source=temp.resolve("test space ä."+extension);var log=temp.resolve("encode.log");
            var command=new ArrayList<>(List.of(AudioFileDecoder.executable(),"-nostdin","-v","error","-y","-f","lavfi","-i","sine=frequency=440:sample_rate=44100:duration=0.25"));
            if(extension.equals("ogg"))command.addAll(List.of("-ac","2","-c:a","vorbis","-strict","-2"));
            command.add(source.toString());var process=new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            assertTrue(process.waitFor(30,java.util.concurrent.TimeUnit.SECONDS));assertEquals(0,process.exitValue(),Files.readString(log));
            var asset=AudioFileDecoder.decode(source,temp.resolve("cache"));assertEquals(extension.equals("ogg")?2:1,asset.channels());assertTrue(asset.frames()>=11900&&asset.frames()<15000,extension+": "+asset.frames());
            var data=ByteBuffer.wrap(Files.readAllBytes(asset.pcm())).order(ByteOrder.LITTLE_ENDIAN);data.position(24);double squares=0;int n=0;
            while(data.remaining()>=4){float value=data.getFloat();assertTrue(Float.isFinite(value));squares+=value*value;n++;}
            assertTrue(Math.sqrt(squares/n)>.045&&Math.sqrt(squares/n)<.11,extension);
            assertFalse(AudioFileDecoder.nativePath(asset.reference()).equals("none"));
            var node=new AudioFileNode();var state=new HashMap<>(node.defaults());state.put("file",com.vingame.glswing.node.model.GLNodeValue.of(asset.reference()));
            assertDoesNotThrow(()->node.encode(new NodeState(state),p->-1));
        }
    }
    @Test void corruptMediaFailsWithoutPublishingCache()throws Exception{
        var source=temp.resolve("bad.mp3");Files.writeString(source,"not audio");
        assertThrows(java.io.IOException.class,()->AudioFileDecoder.decode(source,temp.resolve("cache")));
        try(var files=Files.list(temp.resolve("cache"))){assertEquals(0,files.count());}
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeStereoPlaybackSeekPauseLoopAndState()throws Exception{
        var pcm=temp.resolve("stereo.ekpcm");var bytes=ByteBuffer.allocate(24+16*8).order(ByteOrder.LITTLE_ENDIAN);
        bytes.put("EKPCM001".getBytes(java.nio.charset.StandardCharsets.US_ASCII)).putInt(48000).putInt(2).putLong(16);
        for(int i=0;i<16;i++)bytes.putFloat(i).putFloat(-i);Files.write(pcm,bytes.array());
        var node=new AudioFileNode();var state=new HashMap<>(node.defaults());state.put("file",com.vingame.glswing.node.model.GLNodeValue.of(temp.resolve("source.wav")+"\n"+pcm));state.put("play",com.vingame.glswing.node.model.GLNodeValue.of(1.));
        String config="!"+NativeContractGenerator.fingerprint(node)+" "+node.encode(new NodeState(state),p->-1);
        var source=temp.resolve("test.cpp");Files.writeString(source,"""
            #include "nodes/AudioFileNode.hpp"
            #include <sstream>
            #include <cassert>
            using namespace easyknack;
            int main(){
                AudioFileNode n;std::istringstream in("%s");n.configure(in);
                float audio[8]{},pos[8]{},duration[8]{},miss[8]{},silence[8]{};float* outs[]={audio,pos,duration,miss};
                const float* inputs[3]{};ProcessContext c{inputs,3,audio,8,48000,silence,8};c.outputs=outs;c.outputCount=4;
                n.process(c);for(int i=0;i<8;i++)assert(audio[i]==i);assert(pos[0]==.5f);
                n.playValue=0;n.process(c);for(auto v:audio)assert(v==0);assert(n.position==8);
                n.playValue=1;n.seekValue=.25;c.channelIndex=1;n.process(c);for(int i=0;i<8;i++)assert(audio[i]==-(i+4));
                n.loop=true;n.start=.25;n.end=.5;n.seekValue=.25;n.lastSeek=0;c.channelIndex=0;n.process(c);for(int i=0;i<8;i++)assert(audio[i]==4+i%%4);
                AudioFileNode replacement;std::istringstream in2("%s");replacement.configure(in2);auto old=n.stream;replacement.adoptState(n);assert(replacement.stream==old);assert(replacement.position==n.position);
                c.sampleRate=96000;replacement.position=0;replacement.seekValue=0;replacement.lastSeek=0;replacement.process(c);for(int i=0;i<8;i++)assert(audio[i]==i*.5f);
                assert(miss[0]==0);
                replacement.position=0;replacement.speedValue=2;c.sampleRate=48000;replacement.process(c);for(int i=0;i<8;i++)assert(audio[i]==i*2);
                replacement.speedValue=0;double held=replacement.position;replacement.process(c);assert(replacement.position==held);for(auto v:audio)assert(v==0);
                // Seek beyond the bounded initial read-ahead without doing file I/O on the reader.
                auto path=std::filesystem::u8path(n.path).parent_path()/"large.ekpcm";
                {std::ofstream file(path,std::ios::binary);file.write("EKPCM001",8);std::uint32_t rate=48000,ch=1;std::uint64_t count=400000;file.write(reinterpret_cast<char*>(&rate),4);file.write(reinterpret_cast<char*>(&ch),4);file.write(reinterpret_cast<char*>(&count),8);for(unsigned i=0;i<count;i++){float v=float(i);file.write(reinterpret_cast<char*>(&v),4);}}
                AudioFileStream streaming(path.u8string());float sample=0;assert(!streaming.sample(300000,0,sample));streaming.request(300000,0);
                auto deadline=std::chrono::steady_clock::now()+std::chrono::seconds(3);bool ready=false;
                while(!(ready=streaming.sample(300000,0,sample))&&std::chrono::steady_clock::now()<deadline)std::this_thread::sleep_for(std::chrono::milliseconds(1));
                assert(ready&&sample==300000);
            }
            """.formatted(config,config));
        RateConverterTest.compileAndRun(source,temp.resolve("test.exe"),temp);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void hostAcceptsPlayerAndKeepsRunningDuringParameterChanges()throws Exception{
        var pcm=temp.resolve("host.ekpcm");var bytes=ByteBuffer.allocate(24+48000*4).order(ByteOrder.LITTLE_ENDIAN);
        bytes.put("EKPCM001".getBytes(java.nio.charset.StandardCharsets.US_ASCII)).putInt(48000).putInt(1).putLong(48000);
        for(int i=0;i<48000;i++)bytes.putFloat((float)(.1*Math.sin(i*.03)));Files.write(pcm,bytes.array());
        var node=new AudioFileNode();var state=new HashMap<>(node.defaults());state.put("file",com.vingame.glswing.node.model.GLNodeValue.of(temp.resolve("source.wav")+"\n"+pcm));state.put("play",com.vingame.glswing.node.model.GLNodeValue.of(1.));state.put("loop",com.vingame.glswing.node.model.GLNodeValue.of(true));
        try(var client=new com.vin.easyknack.audio.AudioHostClient()){
            client.start(com.vin.easyknack.audio.AudioHostClient.defaultExecutable(),true);
            for(int i=0;i<5;i++){
                state.put("position",com.vingame.glswing.node.model.GLNodeValue.of(i*.1));
                String config="!"+NativeContractGenerator.fingerprint(node)+" "+node.encode(new NodeState(state),p->-1);
                assertEquals("OK",client.command("GRAPH 2 file audiofile "+config+" @ a cecc out out 0 0 @ a a"));
                assertEquals("OK",client.command("PLAY"));assertTrue(client.command("STATS").startsWith("OK"));
            }
            assertEquals("OK",client.command("STOP"));
        }
    }
}
