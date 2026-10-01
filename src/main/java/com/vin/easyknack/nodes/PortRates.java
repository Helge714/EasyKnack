package com.vin.easyknack.nodes;

/** Numeric port capability, independent of the rate currently used by a connection. */
public enum PortRates {
    AUDIO, CONTROL, AUDIO_CONTROL;

    ProcessingRate resolve(ProcessingRate incoming) {
        return switch (this) {
            case AUDIO -> ProcessingRate.AUDIO;
            case CONTROL -> ProcessingRate.CONTROL;
            case AUDIO_CONTROL -> incoming == ProcessingRate.AUDIO ? ProcessingRate.AUDIO : ProcessingRate.CONTROL;
        };
    }
}
