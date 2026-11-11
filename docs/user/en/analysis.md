# Analysis and measurement

| Question | Signal path |
| --- | --- |
| What is the time waveform? | Audio → Oscilloscope |
| What are Peak and RMS? | Audio → Level Detector |
| Which frequencies are present? | Audio → FFT → Analysis Viewer |
| Which spectral properties change? | FFT → Spectral Features → Value Viewer |
| What is the periodicity? | FFT → Autocorrelation → Analysis Viewer |
| Which partials are tracked? | Partial Analysis → Partial Group |

Level Detector does not measure LUFS. Autocorrelation is not a reconstructible spectrum. Peak, RMS, amplitude, power and dB are different quantities. Mix channel mode averages Left/Right; opposite-phase content can cancel. FFT resolution follows the window, not plot size.

## Inspection and export

Double-click to enlarge a viewer. **Freeze** holds the display, not audio. Click for measurement cursors A/B; Shift+click places B. Wheel zooms horizontally, Shift+wheel vertically, middle-drag pans. The context menu resets the view or cursors.

Export offers CSV and images from a captured snapshot. CSV contains measurement values rather than screen-reduced lines; choose decimal point/comma for your destination. Units follow display mode without implicit dB/power conversion. Reception timestamps are not sample-accurate acquisition times. Record sample rate, FFT window and mode when comparing results.

## Finite measurements

**Value Average** averages numbers; **Analysis Average** spectra or autocorrelation. Start triggers a finite measurement window, Finished marks completion, and the result stays held. Spectral averaging uses power rather than averaging arbitrary phases. **Measurement Recorder** collects series for export. Node references explain the individual measurement modes and limits.

[Manual](index.md) · [Node index](../../nodes/en/index.md) · [Deutsch](../de/analysis.md)
