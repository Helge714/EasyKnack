# Instance Group processor support

Instance Groups execute independent DSP state per instance. Ordinary nested groups
are flattened; nested Instance and Spectral Groups keep their execution domains.
Formula Groups are materialized before instance validation.

The supported set includes oscillators and noise, scalar math and mapping, filters,
panning and channel routing, vectors and spatial receivers, envelopes, trigger
processing, live Buffers and both Buffer Readers. FFT, Spectral Groups, Spectrum
Blur/Shift/Prune and IFFT can form a complete spectral processing chain per instance.
Autocorrelation, spectral/correlation descriptors, level and pitch detection,
Spectrum Statistics and finite averaging can supply internal control signals.

The public boundary currently accepts numeric, Vector, Spectrum and Wavetable
inputs and up to eight numeric or Spectrum outputs. Spectrum outputs use typed
`MultipleSpectrumOutputNode` boundaries and reduce aligned complex frames with
Sum/Mean independently per stereo channel. Other resources and triggers may be
routed internally. Instance Merge remains the numeric boundary between
per-instance processing and shared processing.

## Deliberate exclusions

- Hardware input/output and MIDI device/voice processors need explicit ownership
  and voice-domain rules. Feed the supported public inputs from outside instead.
- Viewers, scopes, pitch monitors and measurement recorders have one visible or
  stored result. They need an instance-selection or aggregation policy first.
- Step Switch has per-step selection telemetry and likewise needs an explicit
  instance policy for its highlighted button.
- Audio File and File Buffer require reviewed sharing of file assets, playback
  state and memory before replication is enabled.
- Spectrum to Waveform combines capture and document/UI telemetry; each instance
  needs an unambiguous capture-result owner before it can be enabled.
- Bin processors belong inside a Spectral Group, including when that Spectral
  Group is nested inside an Instance Group.

Storage and processor state are prepared up to Maximum instances. Internal FFTs
and Buffers therefore multiply their memory and preparation costs by that maximum,
even when fewer instances are active. Place identical processing after Instance
Merge when it should be shared.

## Registration checks

Java validation/menu filtering lives in `MultipleGroups`; the native factory lives
in `MultipleGraphNode.hpp`. Keep both in sync. `InstanceDspCatalogTest` configures
and prepares every Java-allowed built-in processor through the native factory and
compares live/exported FFT -> Blur -> Prune -> Shift -> IFFT execution.
