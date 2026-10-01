# Compiled Instance audio kernel

Fixture generated from the user's `InstanceGroup_Vergleichstest.knack`, without
changing the document: 20 partials, maximum 40, 440 Hz, falloff 0. Numeric public
inputs are unconnected. InstanceGraphAudit.cpp places each source before Gain
(-12 dB) and Output, processes both channels and checks live/baked samples before
timing. No audio device is used.

Compile from repository root:

```powershell
& target/tools/llvm-mingw-20250305-ucrt-x86_64/bin/clang++.exe -std=c++17 -O3 -ffp-contract=off -static -I native/audio-host/benchmarks/instance-audio-kernel-2026-09-26 -I native/audio-host/src native/audio-host/benchmarks/InstanceGraphAudit.cpp -o target/instance-performance/graph-audit.exe
& target/instance-performance/graph-audit.exe
& target/instance-performance/graph-audit.exe reverse
```

Final measurements, after the build finished (microseconds per stereo block of
256 frames at 48 kHz; median of nine batches, 500 blocks each):

| Order | Complex | Live | Bake |
| --- | ---: | ---: | ---: |
| forward | 58.37 | 61.79 | 44.52 |
| reverse | 48.24 | 59.66 | 47.02 |
| forward repeat | 50.33 | 59.55 | 44.49 |

Bake saves about 21–28% versus live in this fixture. CPU scheduling/frequency
still varies; these results do not predict WASAPI peaks or arbitrary groups.

`CompiledInstanceAudio` is emitted through the same generator for Bake and
source export. It uses compact phase/increment/table/weight arrays, sums partials
locally per sample and uses one mono accumulation buffer. Weight normalization
is block-constant. Canonical phase/smoothing state stays in the existing graph
for continuity when using the generic fallback. Control expressions and rate/
channel validation still run once per block; this is not full graph elimination.
Count fades, stereo/audio inputs, unsupported layouts and unsettled unconnected
oscillator frequency use the general path. The general path is not removed.

Validation: full native-audio Maven package succeeded, 255 tests, zero failures/
errors, 18 skipped, plus native script tests. The specialization test explicitly
enters this kernel and covers count changes, short blocks, modulation, stereo,
defaults and state adoption. Package SDK refreshed after the final guard edit.
