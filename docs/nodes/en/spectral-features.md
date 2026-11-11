# Spectral Features
Measures the frequency distribution and its frame-to-frame change.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Spectrum | — | Analysis resource; Spectrum or analysis resource; Control |
| Frequency scale | Linear / Logarithmic; Default: Linear | Editor; Inspector |
| Weighting | Power / Magnitude; Default: Power | Editor |
| Roll-off fraction | 0–1; Default: 0.85; Factor, % | Editor |
| Normalize flux | Off / On; Default: On | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Centroid | Value; 0–Nyquist Hz; Control |
| Spread | Value; ≥ 0 Hz / oct; Control |
| Skewness | Value; ±∞; Control |
| Kurtosis | Value; ≥ 0; Control |
| Flux | Value; ≥ 0; Control |
| Rolloff | Value; 0–Nyquist Hz; Control |
| Slope | Value; ±∞ / Hz; Control |
| Flatness | Value; 0–1; Control |

## Behavior

[Guide](../../user/en/analysis.md)
Let A[k] = abs(X[k])/Σw[n] and m[k] = 1 at DC/Nyquist, otherwise 2. Weight is W[k] = m[k]·A[k]² for Power or m[k]·A[k] for Magnitude. Set p[k] = W[k]/ΣW. Linear moments use z[k] = f[k]; logarithmic moments use z[k] = log2(f[k]) and exclude DC, renormalizing the remaining weights.

- Centroid: μ = Σp·z; logarithmic output is 2^μ Hz.
- Spread: σ = sqrt(Σp·(z−μ)²), in Hz or octaves.
- Skewness: Σp·(z−μ)³/σ³.
- Kurtosis: Σp·(z−μ)⁴/σ⁴; Gaussian = 3.
- Flux: sqrt(Σ(current−previous)²), using p when normalized, otherwise W. First frame = 0.
- Roll-off: first frequency whose cumulative weight reaches the selected fraction.
- Slope: least-squares slope of p against f, per Hz.
- Flatness: geometric mean of floored W divided by its arithmetic mean; floor = max(1e−300, mean(W)·1e−12).

Logarithmic scale changes only the four moments. Silence and undefined zero-spread moments return zero. Outputs hold between frames. Mean spectral flatness of a single white-noise FFT is below 1; averaging power spectra first reduces estimator variation.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/spectral-features.md)
