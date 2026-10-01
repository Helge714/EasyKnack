package com.vin.easyknack.audio;
import java.util.*;
/** UI-thread owned bounded history. Every point contains a one-second block-statistics window. */
public final class AudioDiagnosticHistory {
    public record Point(long time,AudioDiagnostics value){}
    private final ArrayDeque<Point> points=new ArrayDeque<>();
    private long epoch=-1;
    public void clear(){points.clear();epoch=-1;}
    public void add(long time,AudioDiagnostics value){
        if(value.epoch()!=epoch){clear();epoch=value.epoch();}
        points.addLast(new Point(time,value));
        while(points.size()>256||(!points.isEmpty()&&time-points.getFirst().time()>60_000_000_000L))points.removeFirst();
    }
    public List<Point> points(){return List.copyOf(points);}
    public String report(String state){
        var out=new StringBuilder("# EasyKnack render diagnostics v1\n# created="+java.time.Instant.now()+"\n# state="+state+"\n# java="+System.getProperty("java.version")+"\n# os="+System.getProperty("os.name")+" "+System.getProperty("os.arch")+"\n# Metrics cover render including graph IO; not CPU utilization or end-to-end latency.\n# Render and Peak use complete common audio/FFT-hop cycles. frames is cycle length, blocks counts cycles.\n# p99 and peak: trailing one-second cycle window; durations in microseconds. Background analysis excluded.\n# Device deadline misses unavailable; ASIO overload notifications listed separately.\n");
        if(!points.isEmpty()){var last=points.getLast().value();out.append("# ").append(last.clock().replace('\n',' ')).append('\n').append("# ").append(last.buffers().replace('\n',' ')).append('\n');}
        out.append("elapsed_seconds,epoch,blocks,mean_percent,peak_percent,p99_us,session_max_us,over_budget,frames,sample_rate,telemetry_lost,capture_under,capture_over,playback_under,playback_over,driver_overload,priming_blocks\n");
        long start=points.isEmpty()?0:points.getFirst().time();
        for(var p:points)out.append(String.format(Locale.ROOT,"%.6f,",(p.time()-start)/1e9)).append(p.value().csv()).append('\n');
        return out.toString();
    }
}
