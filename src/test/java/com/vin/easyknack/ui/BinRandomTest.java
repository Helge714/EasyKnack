package com.vin.easyknack.ui;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.quote;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.GLNodeValue;

class BinRandomTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void binRandomIsReproducibleAndFrameModeChangesOnlyWithFramePosition()throws Exception{
        var random=new BinRandomNode();var output=new BinOutputNode();var spectral=new SpectralGraphNode();
        var code=new StringBuilder("#include \"AudioGraph.hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){\n");
        for(String mode:List.of("fixed","frame","constant","shared-frame")){
            var values=new HashMap<String,GLNodeValue>();values.put("mode",GLNodeValue.of(mode));values.put("seed",GLNodeValue.of(0));
            String program="BIN1 2 binrandom !"+NativeContractGenerator.fingerprint(random)+" "+random.encode(new NodeState(values),p->-1)+" binoutput !"+NativeContractGenerator.fingerprint(output)+" "+output.encode(new NodeState(output.defaults()),p->p.equals(BinOutputNode.A)?0:-1);
            String payload="!"+NativeContractGenerator.fingerprint(spectral)+" "+spectral.encode(new NodeState(Map.of("program",GLNodeValue.of(SpectralGraphNode.hex(program)))),p->0);
            code.append("{SpectralGraphNode node;std::istringstream params(").append(quote(payload)).append(");node.configure(params);node.prepare(48000,256);SpectrumStream input;input.prepare(256);input.size=input.windowSize=256;input.sampleRate=48000;input.count=1;input.hop=64;const DspResource* resources[]{&input};float silence=0,out=0;ProcessContext c{nullptr,1,&out,1,48000,&silence,1};c.resourceInputs=resources;node.process(c);auto first=node.stream.frames[0].bins;node.process(c);assert(first==node.stream.frames[0].bins);double sum=0;for(int k=0;k<=128;k++){auto v=first[k].real();assert(v>=0&&v<1);sum+=v;}assert((first[0]!=first[1])=="+(mode.equals("fixed")||mode.equals("frame")?"true":"false")+");input.frames[0].start=64;node.process(c);assert((first==node.stream.frames[0].bins)==")
                .append((mode.equals("fixed")||mode.equals("constant"))?"true":"false").append(");input.frames[0].start=0;node.process(c);assert(first==node.stream.frames[0].bins);auto* r=dynamic_cast<BinRandomNode*>(node.steps[0].processor.get());assert(r);r->seed=1;node.process(c);assert(first!=node.stream.frames[0].bins);}\n");
        }
        code.append("}");var dir=Files.createTempDirectory(Path.of("target"),"binrandom-").toAbsolutePath();var source=dir.resolve("test.cpp");Files.writeString(source,code);
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
