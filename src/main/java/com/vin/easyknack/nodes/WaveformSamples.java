package com.vin.easyknack.nodes;

import com.vingame.glswing.ui.GLFunctionCurve;
import java.util.*;

/** Lossless, document-owned IEEE float samples. The clipboard never aliases a live resource. */
public final class WaveformSamples {
    public static final int SIZE=2048;
    public static final String PREFIX="samples:";
    private static float[] clipboard;
    private WaveformSamples(){}
    public static void copy(float[] samples){validate(samples);clipboard=samples.clone();}
    public static float[] copied(){return clipboard==null?null:clipboard.clone();}
    public static void validate(float[] samples){
        if(samples.length!=SIZE)throw new IllegalArgumentException("Invalid wavetable length");
        for(float v:samples)if(!Float.isFinite(v))throw new IllegalArgumentException("Non-finite wavetable sample");
    }
    public static String encode(float[] samples){
        validate(samples);var out=new StringBuilder(SIZE*8);
        for(float v:samples)out.append(HexFormat.of().toHexDigits(Float.floatToRawIntBits(v)));
        return out.toString();
    }
    public static float[] decode(String hex){
        if(hex.length()!=SIZE*8)throw new IllegalArgumentException("Invalid wavetable data");
        float[] samples=new float[SIZE];
        for(int i=0;i<SIZE;i++)samples[i]=Float.intBitsToFloat(HexFormat.fromHexDigits(hex,i*8,i*8+8));
        validate(samples);return samples;
    }
    public static float[] fromValue(String value){
        if(value.startsWith(PREFIX))return decode(value.substring(PREFIX.length()));
        var curve=CurveData.decode(value);float[] result=new float[SIZE];
        for(int i=0;i<SIZE;i++)result[i]=(float)(2*curve.evaluate(i/(double)SIZE)-1);
        return result;
    }
    public static GLFunctionCurve curve(float[] samples){
        validate(samples);var points=new ArrayList<GLFunctionCurve.Point>();
        for(int i=0;i<64;i++){int index=i==63?0:(int)Math.round(i*SIZE/63.);points.add(GLFunctionCurve.Point.at(i/63.,Math.max(0,Math.min(1,(samples[index]+1)/2.))));}
        return new GLFunctionCurve(points);
    }
}
