package com.vin.easyknack.audio;
import java.io.IOException;
import java.util.*;
/** Versioned processing-cycle measurements. frames/blocks denote cycle samples/count (wire fields retained). Includes graph IO, not background analysis CPU or output deadlines. */
public record AudioDiagnostics(long epoch,long blocks,double mean,double peak,double p99us,double maxUs,
        long overBudget,long frames,double sampleRate,long dropped,long captureUnder,long captureOver,
        long playbackUnder,long playbackOver,long driverOverload,long priming,String clock,String buffers) {
    public static AudioDiagnostics parse(String response)throws IOException{
        try{
            var p=response.split(" ");if(p.length!=20||!p[0].equals("OK")||!p[1].equals("1"))throw new IllegalArgumentException();
            double[] n=new double[16];for(int i=0;i<n.length;i++){n[i]=Double.parseDouble(p[i+2]);if(!Double.isFinite(n[i])||n[i]<0)throw new IllegalArgumentException();}
            for(int i:new int[]{0,1,6,7,9,10,11,12,13,14,15})if(n[i]!=Math.rint(n[i])||n[i]>Long.MAX_VALUE)throw new IllegalArgumentException();
            return new AudioDiagnostics(Long.parseLong(p[2]),Long.parseLong(p[3]),n[2],n[3],n[4],n[5],Long.parseLong(p[8]),Long.parseLong(p[9]),n[8],Long.parseLong(p[11]),Long.parseLong(p[12]),Long.parseLong(p[13]),Long.parseLong(p[14]),Long.parseLong(p[15]),Long.parseLong(p[16]),Long.parseLong(p[17]),AudioEndpoint.unhex(p[18]),AudioEndpoint.unhex(p[19]));
        }catch(RuntimeException e){throw new IOException(com.vin.easyknack.ui.AppText.literal("Invalid render diagnostics"),e);}
    }
    public double budgetUs(){return sampleRate>0?frames/sampleRate*1e6:0;}
    public String label(){return String.format(Locale.ROOT,"Render %.1f %% | Peak %.1f %% | Over budget %d",mean,peak,overBudget);}
    public String csv(){return String.format(Locale.ROOT,"%d,%d,%.6f,%.6f,%.6f,%.6f,%d,%d,%.6f,%d,%d,%d,%d,%d,%d,%d",epoch,blocks,mean,peak,p99us,maxUs,overBudget,frames,sampleRate,dropped,captureUnder,captureOver,playbackUnder,playbackOver,driverOverload,priming);}
}

