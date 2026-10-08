# Spectral processing inside Instance Groups

Inside an Instance Group, add an FFT, a Spectral Group and an IFFT. Connect:

`Audio -> FFT -> Spectral Group -> IFFT -> Instance Merge -> group output`

The FFT, spectral state and IFFT overlap-add history are independent for each
instance. Use Instance Context outside the Spectral Group and pass its numeric
values through Spectral Group inputs to vary processing by instance. Inside the
Spectral Group, Bin Context continues to describe the current FFT bin.

Processing connected after Instance Merge runs once on the combined audio.
Prefer that arrangement for a shared final effect. FFT latency depends on window
size. Instance count multiplies FFT storage and processing costs; begin with a
small maximum count.

Nested Spectral Groups work in live processing and source export / Bake.
Instance Groups accept Spectrum inputs. Compute a shared FFT outside the group,
connect it to a Spectrum group input, and connect that input to the Spectral Group
inside. Finish each instance with IFFT and Instance Merge. The input shares FFT
frames without recalculating the FFT per instance; each spectral processor keeps
its own state. The same input can also feed a shared processor after Instance Merge.

Instance Group outputs remain numeric audio/value signals: merging Spectrum
outputs is not defined. MIDI ports are also not supported at this boundary.
