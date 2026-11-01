> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](user/README.md).

# Oscillator Reset

Oscillator and Complex Oscillator have a Trigger input named **Reset**. Without a
connection it displays a manual trigger button.

- A connected trigger resets the running phase **before** evaluating that sample.
  Adjacent triggers reset on both samples; duplicate triggers on one sample have
  the same result as one reset.
- The manual button is latched at the next render block. The same logical node's
  channels and voices receive the reset together.
- The phase accumulator is set to zero. The existing Phase offset is subsequently
  applied once by the normal waveform lookup. Phase is still an offset without Reset.
- Complex Oscillator resets every partial accumulator together. Its existing
  fundamental-phase convention remains: the Phase offset is multiplied by each
  partial's frequency ratio.
- Frequency, spectrum weights and parameter smoothing are not reset.

Block processing, sample-feedback chunks and generated sample loops use the same
trigger reader. The closed oscillator/gain/filter kernel and the live/baked mono
instance reduction kernels also consume manual resets. Connected Reset sources
use the general scheduler where those closed optimizations are not applicable.
Old oscillator parameter contracts in saved generated graphs remain readable.

Validation: OscillatorResetTest covers adjacent/end-of-block events, chunk offsets,
Complex Oscillator with three partials, shared manual reset, state handover,
old contracts and exported Trigger ports. The existing feedback, instance and
fusion regression suites exercise the updated parameter layout.
