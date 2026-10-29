# Formulas in a Spectral Group

Inside a Spectral Group, add **Formula** from the Math category. Configure its
inputs and outputs using the usual group interface and write the formula in its
code editor. Numeric values and Trigger inputs are supported. File and delay
Buffer inputs are not supported in this domain.

The formula runs once per bin per FFT frame, regardless of its normal Audio or
Control setting. Arithmetic, functions, conditions, multiple outputs and typed
variables work as in an ordinary Formula. External values and triggers follow
the Spectral Group's frame sampling rules, including asynchronous FFTs.

Persistent `state` variables are independent for each bin and channel. Matching
state declarations survive compatible graph rebuilds; a changed FFT identity or
size resets them. Explicitly discarded bins do not advance their state. Automatic
empty-bin skipping and parallel bin workers are disabled for stateful formulas.

In this domain, `sampleIndex` is the bin index, `blockSize` is the number of
nonnegative-frequency bins, `samplePosition` is the FFT window end in samples,
and `sampleRate` remains the audio sample rate. Numeric input `.rate` reports
Control: each value is held for this frame. `rand(min, max)` varies across bins
and frames. An explicit random seed follows the regular Formula semantics.

## Gaussian band filter

Create numeric inputs named `frequency`, `magnitude`, `center` and `factor`,
and an output named `result`. Connect Bin Context Frequency and Magnitude to the
first two inputs. Expose Center and Factor on the surrounding Spectral Group.
Set Bin Output to Magnitude / Phase; connect Result to Magnitude and the original
Bin Context Phase directly to Phase.

```cpp
double amount = clamp(factor, 0, 1);
if (amount == 0) {
    result = magnitude;
} else if (center <= 0) {
    result = 0;
} else if (amount == 1) {
    double nearest = round(center * (2.0 * ((double) blockSize - 1.0)) / sampleRate);
    if ((double) sampleIndex == nearest) { result = magnitude; }
    else { result = 0; }
} else if (frequency <= 0) {
    result = 0;
} else {
    double distance = log2(frequency / center);
    double sharpness = amount / (1 - amount);
    double x = distance * sharpness;
    result = magnitude * exp(-0.5 * x * x);
}
```

Factor 0 passes everything; approaching 1 narrows the Gaussian in octave space.
At exactly 1 only the nearest FFT bin survives. Finite FFT resolution still
limits frequency selectivity. This magnitude filter leaves phase unchanged and
does not compensate for removed energy.

Spectral formulas also work in Bake and source/plugin exports. Their embedded
bytecode runs in the Formula VM, without depending on a local live-code DLL;
they are not yet fused into a native C++ expression by spectral baking.
