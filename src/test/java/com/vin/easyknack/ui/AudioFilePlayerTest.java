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
    static byte[] wave(int channels, boolean floating) {
        int frames=11025, sampleBytes=floating?4:2, size=frames*channels*sampleBytes;
        var b=ByteBuffer.allocate(44+size).order(ByteOrder.LITTLE_ENDIAN);
        b.put("RIFF".getBytes()).putInt(36+size).put("WAVEfmt ".getBytes()).putInt(16);
        b.putShort((short)(floating?3:1)).putShort((short)channels).putInt(44100).putInt(44100*channels*sampleBytes).putShort((short)(channels*sampleBytes)).putShort((short)(sampleBytes*8));
        b.put("data".getBytes()).putInt(size);
        for(int i=0;i<frames;i++)for(int c=0;c<channels;c++) {
            float v=(float)(.125*Math.sin(2*Math.PI*440*i/44100))*(c==0?1:-1);
            if(floating)b.putFloat(v);else b.putShort((short)(v*32767));
        }
        return b.array();
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void realFormatsDecodeToFinitePcmWithoutChangingMonoGain()throws Exception{
        for(String kind:List.of("mono.wav","stereo.wav","float.WAV","tone.mp3")){
            var source=temp.resolve("test space ä 漢 "+kind);
            if(kind.endsWith("mp3"))try(var in=getClass().getResourceAsStream("/audio/tone.mp3.b64")){
                Files.write(source,Base64.getMimeDecoder().decode(in.readAllBytes()));
            } else Files.write(source,wave(kind.startsWith("stereo")?2:1,kind.startsWith("float")));
            var asset=AudioFileDecoder.decode(source,temp.resolve("cache"));assertEquals(kind.startsWith("stereo")?2:1,asset.channels());assertEquals(12000,asset.frames(),2,kind);
            var data=ByteBuffer.wrap(Files.readAllBytes(asset.pcm())).order(ByteOrder.LITTLE_ENDIAN);data.position(24);double squares=0;int n=0;
            while(data.remaining()>=4){float value=data.getFloat();assertTrue(Float.isFinite(value));squares+=value*value;n++;}
            assertTrue(Math.sqrt(squares/n)>.045&&Math.sqrt(squares/n)<.11,kind);
            if(asset.channels()==2){data.position(24);while(data.remaining()>=8)assertEquals(-data.getFloat(),data.getFloat(),1e-5);}
            assertNotEquals("none",AudioFileDecoder.nativePath(asset.reference()));
            var node=new AudioFileNode();var state=new HashMap<>(node.defaults());state.put("file",com.vingame.glswing.node.model.GLNodeValue.of(asset.reference()));
            assertDoesNotThrow(()->node.encode(new NodeState(state),p->-1));
        }
    }
    @Test void oldMp3CachesAreInvalidatedWithoutInvalidatingWaveFiles()throws Exception{
        var pcm=temp.resolve("old.ekpcm");Files.writeString(pcm,"existing");
        assertEquals("none",AudioFileDecoder.nativePath(temp.resolve("source.mp3")+"\n"+pcm));
        assertNotEquals("none",AudioFileDecoder.nativePath(temp.resolve("source.wav")+"\n"+pcm));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void decodedMp3WrapsSampleForSampleInPlayerAndBufferReader()throws Exception{
        var source=temp.resolve("loop.mp3");try(var in=getClass().getResourceAsStream("/audio/tone.mp3.b64")){Files.write(source,Base64.getMimeDecoder().decode(in.readAllBytes()));}
        var asset=AudioFileDecoder.decode(source,temp.resolve("cache"));var node=new AudioFileNode();var values=new HashMap<>(node.defaults());
        values.put("file",com.vingame.glswing.node.model.GLNodeValue.of(asset.reference()));values.put("play",com.vingame.glswing.node.model.GLNodeValue.of(1.));values.put("loop",com.vingame.glswing.node.model.GLNodeValue.of(true));
        String config="!"+NativeContractGenerator.fingerprint(node)+" "+node.encode(new NodeState(values),p->-1);
        Path cpp=temp.resolve("gapless-loop.cpp");Files.writeString(cpp,"""
            #include "nodes/AudioFileNode.hpp"
            #include "nodes/FileBufferNode.hpp"
            #include "nodes/HistoryReaderNode.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){AudioFileNode player;std::istringstream in("%s");player.configure(in);
              auto file=FileBufferData::load(player.path);auto& history=file->channels[0];auto count=history.samples.size();assert(count==12000);
              float audio[256]{},silence[256]{};const float* inputs[4]{};ProcessContext c{inputs,4,audio,256,48000,silence,256};
              player.position=count-128;player.wasPlaying=true;player.process(c);assert(player.underruns==0);
              for(int i=0;i<256;i++)assert(std::abs(audio[i]-history.samples[(count-128+i)%%count])<1e-7);
              HistoryReaderNode reader;reader.initialized=true;reader.historyIdentity=history.identity;reader.cursor=count-128;const DspResource* resources[]{&history};c.resourceInputs=resources;reader.process(c);
              for(int i=0;i<256;i++)assert(std::abs(audio[i]-history.samples[(count-128+i)%%count])<1e-7);
            }
            """.formatted(config));
        RateConverterTest.compileAndRun(cpp,temp.resolve("gapless-loop.exe"),temp);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void taggedMp3HasNoEncoderGapAndUntaggedAudioIsNotSilenceTrimmed()throws Exception{
        byte[] bytes;try(var in=getClass().getResourceAsStream("/audio/tone.mp3.b64")){bytes=Base64.getMimeDecoder().decode(in.readAllBytes());}
        var source=temp.resolve("tagged.mp3");Files.write(source,bytes);
        var tagged=AudioFileDecoder.decode(source,temp.resolve("cache"));assertEquals(12000,tagged.frames(),2);
        var samples=ByteBuffer.wrap(Files.readAllBytes(tagged.pcm())).order(ByteOrder.LITTLE_ENDIAN);samples.position(24);
        float[] audio=new float[(int)tagged.frames()];for(int i=0;i<audio.length;i++)audio[i]=samples.getFloat();
        for(int start:new int[]{0,audio.length-240}){double sum=0;for(int i=start;i<start+240;i++)sum+=audio[i]*audio[i];assertTrue(Math.sqrt(sum/240)>.04,"No encoder silence at either edge");}
        int tag=-1;for(int i=0;i<bytes.length-4;i++)if(bytes[i]=='I'&&bytes[i+1]=='n'&&bytes[i+2]=='f'&&bytes[i+3]=='o'){tag=i;break;}assertTrue(tag>=0);
        Arrays.fill(bytes,tag,tag+4,(byte)0);var untaggedSource=temp.resolve("untagged.mp3");Files.write(untaggedSource,bytes);
        var untagged=AudioFileDecoder.decode(untaggedSource,temp.resolve("cache"));assertTrue(untagged.frames()>tagged.frames(),"Without metadata do not guess or trim quiet samples");
    }
    @Test void unsupportedFormatIsRejectedBeforeStartingDecoder()throws Exception{
        var source=temp.resolve("old.flac");Files.writeString(source,"not audio");
        assertTrue(assertThrows(java.io.IOException.class,()->AudioFileDecoder.decode(source,temp.resolve("cache"))).getMessage().contains("WAV and MP3"));
        assertFalse(Files.exists(temp.resolve("cache")));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void invalidMediaFailsWithoutPublishingCache()throws Exception{
        for(String kind:List.of("bad.mp3","empty.wav","surround.wav","nonfinite.wav")) {
            var source=temp.resolve(kind);
            if(kind.equals("surround.wav"))Files.write(source,wave(6,false));
            else if(kind.equals("nonfinite.wav")){var bytes=wave(1,true);ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putFloat(44,Float.NaN);Files.write(source,bytes);}
            else Files.writeString(source,"not audio");
            assertThrows(java.io.IOException.class,()->AudioFileDecoder.decode(source,temp.resolve("cache")),kind);
            try(var files=Files.list(temp.resolve("cache"))){assertEquals(0,files.count());}
        }
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
