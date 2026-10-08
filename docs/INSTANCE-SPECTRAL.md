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
Instance Group public boundaries remain numeric: place the FFT inside the group,
or perform shared spectral processing outside it.
