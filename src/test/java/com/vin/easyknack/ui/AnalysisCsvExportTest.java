package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import com.vin.easyknack.nodes.AnalysisChart;
import com.vin.easyknack.audio.AudioAnalysis;
import java.io.StringWriter;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AnalysisCsvExportTest {
    @TempDir java.nio.file.Path directory;
    @Test void germanExcelUsesSemicolonsDecimalCommasAndUtf8Bom()throws Exception{
        var s=new AnalysisChart.Snapshot(new AudioAnalysis.Frame(0,0,23.4375,new float[]{0,.125f,-.5f}),true,false,1,false,0,0,"2026-09-29T12:00:00.123Z",true);
        var path=directory.resolve("excel.csv");AnalysisCsvExport.write(s,path,AnalysisCsvExport.Format.EXCEL_DE);
        byte[] bytes=Files.readAllBytes(path);
        assertArrayEquals(new byte[]{(byte)0xef,(byte)0xbb,(byte)0xbf},java.util.Arrays.copyOf(bytes,3));
        var lines=Files.readAllLines(path);
        assertEquals("\uFEFFbin;frequency_hz;value;received_at_utc;frozen",lines.get(0));
        assertEquals("1;11,71875;0,125;\"2026-09-29T12:00:00.123Z\";true",lines.get(2));
        for(String row:lines)assertEquals(5,row.split(";",-1).length);
        assertEquals(-.5,Double.parseDouble(lines.get(3).split(";")[2].replace(',','.')));
    }
    @Test void exportsEveryBinIncludingDcWithExactFrequencyAndFrozenValues()throws Exception{
        float[] values=new float[2049];values[17]=.125f;
        var s=new AnalysisChart.Snapshot(new AudioAnalysis.Frame(0,0,24000,values),true,true,1,false,0,0,"2026-09-29T12:00:00Z",true);
        values[17]=99;
        assertEquals(17,s.nearestIndex(200));assertEquals(199.21875,s.sampleX(17));
        assertEquals(17,s.nearestIndex(s.minimumX()*Math.pow(s.maximumX()/s.minimumX(),s.position(s.sampleX(17)))));
        var path=directory.resolve("analysis.csv");Files.writeString(path,"old");AnalysisCsvExport.write(s,path);
        var lines=Files.readAllLines(path);assertEquals(2050,lines.size());
        assertTrue(lines.get(0).startsWith("bin,frequency_hz,value,"));
        assertTrue(lines.get(1).startsWith("0,0.0,0.0,"));
        assertEquals("17,199.21875,0.125,\"2026-09-29T12:00:00Z\",true",lines.get(18));
        assertTrue(lines.get(2049).startsWith("2048,24000.0,"));
    }
    @Test void correlationHasSignedLagsAndScopeHasTimeCoordinates()throws Exception{
        for(boolean symmetric:new boolean[]{false,true}){
            var s=new AnalysisChart.Snapshot(new AudioAnalysis.Frame(0,0,.002,symmetric,new float[]{1,.5f,0}),false,false,1,false,0,0,"",false);
            var out=new StringWriter();AnalysisCsvExport.encode(s,out);var lines=out.toString().lines().toList();
            assertEquals(symmetric?6:4,lines.size());
            assertTrue(lines.get(0).startsWith(symmetric?"lag_index,lag_ms":"sample_index,time_ms"));
            assertTrue(lines.get(1).startsWith(symmetric?"-2,-2.0,0.0,":"0,0.0,1.0,"));
            assertTrue(lines.get(lines.size()-1).startsWith("2,2.0,0.0,"));
        }
    }
}
