package com.vin.easyknack.audio;

import java.io.IOException;
import java.util.Locale;

/** Callback execution time / callback frame budget, not machine CPU utilization. */
public record AudioLoad(double averagePercent, double peakPercent, long overBudget) {
    public static AudioLoad parse(String response) throws IOException {
        var values = response.split(" ");
        if (values.length != 4 || !values[0].equals("OK")) throw new IOException(com.vin.easyknack.ui.AppText.literal("Ungültige DSP-Statistik"));
        try {
            long average = Long.parseLong(values[1]), peak = Long.parseLong(values[2]), count = Long.parseLong(values[3]);
            if (average < 0 || peak < 0 || count < 0) throw new NumberFormatException();
            return new AudioLoad(average / 100.0, peak / 100.0, count);
        } catch (NumberFormatException invalid) { throw new IOException(com.vin.easyknack.ui.AppText.literal("Ungültige DSP-Statistik"), invalid); }
    }
    public String label() {
        return String.format(Locale.GERMAN, "DSP %.1f %% · Peak %.1f %% · Über Budget: %d", averagePercent, peakPercent, overBudget);
    }
}
