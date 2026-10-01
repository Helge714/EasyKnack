package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioAnalysis;

class AnalysisInspectionTest {
    static AnalysisChart.Snapshot snapshot(float[] data,boolean log,boolean symmetric){return new AnalysisChart.Snapshot(new AudioAnalysis.Frame(0,0,symmetric?.1:24000,symmetric,data),!symmetric,log,1,false,0,0,"2026-09-29T12:00:00Z",true);}
    @Test void freezeIsSharedAndCopiesSamplesWithoutStoppingIncomingFrames()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            try{
                var plot=new AnalysisEditor.Plot(true,true);plot.bind("inspect");var expanded=plot.expandedCopy();
                float[] a={1,2,3};AudioAnalysis.publish(Map.of("inspect",new AudioAnalysis.Frame(0,0,24000,a)));
                expanded.setPaused(true);assertTrue(plot.isPaused());a[1]=100;
                AudioAnalysis.publish(Map.of("inspect",new AudioAnalysis.Frame(0,0,12000,new float[]{4,5,6})));
                assertEquals(2,plot.snapshot().frame().values()[1]);assertEquals(24000,expanded.snapshot().frame().span());
                var exported=expanded.snapshot();plot.setPaused(false);
                assertEquals(5,expanded.snapshot().frame().values()[1]);assertEquals(2,exported.frame().values()[1]);
                expanded.setLogarithmic(true);assertTrue(plot.isLogarithmic());
            }finally{AudioAnalysis.clear();}
        });
    }
    @Test void logarithmicMappingAndGridShareFrequencyCoordinates(){
        var s=snapshot(new float[2049],true,false);
        assertEquals(0,s.position(24000./2048),1e-12);assertEquals(1,s.position(24000),1e-12);
        assertEquals(s.position(1000)-s.position(100),s.position(10000)-s.position(1000),1e-12);
        assertTrue(AnalysisChart.ticks(s).stream().anyMatch(t->t.value()==1000));
        assertTrue(AnalysisChart.ticks(s).stream().allMatch(t->t.value()>0));
    }
    @Test void denseFlatLinesAndSingleBinPeaksSurviveReduction(){
        float[] data=new float[8193];Arrays.fill(data,.25f);data[3141]=1;data[3142]=-.8f;
        for(boolean log:List.of(false,true))for(boolean symmetric:List.of(false,true)){
            var lines=AnalysisChart.geometry(snapshot(data,log,symmetric),120);
            assertFalse(lines.isEmpty());assertTrue(lines.stream().anyMatch(l->l.y1()==1||l.y2()==1));
            assertTrue(lines.stream().anyMatch(l->l.y1()==-.8f||l.y2()==-.8f));
            assertTrue(lines.stream().anyMatch(l->l.x2()>l.x1()&&l.y1()==.25&&l.y2()==.25));
            assertTrue(lines.size()<=120*4+4);
        }
    }
    @Test void exportHasLabeledAxesAndRetainsSnapshot()throws Exception{
        float[] data=new float[4097];for(int i=0;i<data.length;i++)data[i]=(float)(.05+.75*Math.exp(-Math.pow((i-80)/4.,2))+.4*Math.exp(-Math.pow((i-1400)/10.,2)));
        var folder=java.nio.file.Path.of("target","analysis-inspection");java.nio.file.Files.createDirectories(folder);
        for(boolean log:List.of(false,true)){
            var image=AnalysisImageExport.render(snapshot(data,log,false),1600,900);
            assertEquals(1600,image.getWidth());assertEquals(900,image.getHeight());
            int ink=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if(image.getRGB(x,y)!=java.awt.Color.WHITE.getRGB())ink++;
            assertTrue(ink>5000);
            GraphImageExport.write(image,"png",folder.resolve(log?"log.png":"linear.png"));
            if(log){GraphImageExport.write(image,"jpg",folder.resolve("log.jpg"),.95f);assertNotNull(javax.imageio.ImageIO.read(folder.resolve("log.jpg").toFile()));}
        }
    }
}
