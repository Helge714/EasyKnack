package com.vin.easyknack.audio;

/** The host currently has one fixed DSP clock, independent of device clock rates.
 * Keep in sync with GraphRuntime.hpp until the host exposes a configurable DSP rate. */
public final class AudioProcessing {
    public static final double SAMPLE_RATE = 48000;
    public static final int BLOCK_FRAMES = 256;
    private AudioProcessing() { }
}
