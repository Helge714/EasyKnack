# Sinusoidal analysis reference comparison

## Purpose and scope

Compare EasyKnack's causal general sinusoidal analysis with an independently
installed, executable reference before adjusting its classifier. Neither analysis
requires a common fundamental. The optional harmonic restriction is disabled.

This is a developer experiment, not an additional runtime dependency. SMS Tools
and its sources are not copied into EasyKnack, the SDK or an installer.

Primary references:

- [MTG SMS Tools](https://www.upf.edu/web/mtg/sms-tools), specifically
  [sineModel](https://github.com/MTG/sms-tools/blob/master/smstools/models/sineModel.py)
  and [Sinusoidal plus Residual](https://github.com/MTG/sms-tools/blob/master/smstools/models/sprModel.py).
  General peak tracking is separate from the harmonic/fundamental models.
- McAulay and Quatieri, 1986:
  [Speech Analysis/Synthesis Based on a Sinusoidal Representation](https://www.ll.mit.edu/r-d/publications/speech-analysissynthesis-based-sinusoidal-representation).
  A tracking reference, not a complete sinusoidal/noise classifier.
- Zivanovic, Roebel and Rodet, 2007:
  [Adaptive Threshold Determination for Spectral Peak Classification](https://www.dafx.de/paper-archive/details/OxNHIJWjLWl3HcTplXYymQ).
  Defines peak regions between neighbouring minima and uses normalized bandwidth,
  normalized duration and reassigned-frequency coherence. Window- and SNR-dependent
  decision boundaries are derived from distributions of modulated sinusoids,
  sidelobes and noise. This is **not** equivalent to our current window-fit score.

## Reproduce on Windows

Use a separate Python 3.12 environment under ignored build output. From the
repository root, with generated native contracts present (`Maven package`):

```powershell
python -m venv target/partial-reference-env
target/partial-reference-env/Scripts/python.exe -m pip install -r scripts/requirements-partial-reference.txt
target/partial-reference-env/Scripts/python.exe scripts/partial-reference.py --cxx target/tools/llvm-mingw-20250305-ucrt-x86_64/bin/clang++.exe
```

`--sizes 2048 4096` selects a smaller run. `--output <directory>` preserves separate
runs. `--native-executable <previous-adapter.exe>` measures a previously built
adapter; the output records its hash rather than attributing it to current source.
`--wav <file.wav>` additionally measures the left channel of a 48 kHz WAV. A real
recording has no known partial ground truth: count, residual power and identity
are reported without inventing recall or false-positive labels.

Outputs in `target/partial-reference/`:

- `environment.json`: package versions, Python/compiler identity, Git state,
  executable and relevant reference/native source hashes, parameter conventions.
- `.f64`: deterministic mono double-precision input fixtures, little endian.
- Per-frame CSV: frequencies and native amplitude, phase, ID, tonality, residual
  power ratio and reconstruction error; separate reference peak CSVs.
- `summary.csv` and `summary.json`: aggregate measurements.

Do not commit recordings, virtual environments, executables or generated results.

## Measurement conventions

The seven fixtures are a 440 Hz cosine, eight harmonics at 110 Hz spacing,
inharmonic tones at 180/317/541/997 Hz, four harmonics with 5 Hz vibrato at 2%
frequency depth, inharmonic tones plus 10 dB Gaussian noise, Gaussian noise alone,
and repeated exponentially decaying noise bursts. The random seed is 5072007.

Every model receives the same samples at 48 kHz, a periodic Hann window, twice
the window length as FFT size, and a hop of one quarter of the window. Each case
contains 65 complete frames. Window lengths range from 512 through 65536 samples.
The fixtures deliberately include components that cannot be resolved with short
windows and modulation that is not locally stationary with long windows.

The adapter runs the actual `PartialAnalysisNode` in two profiles:

- `defaults`: current product defaults, with classification enabled.
- `sinusoidal`: classification is disabled;
  minimum track duration remains 20 ms. This isolates tracking from classification.

SMS calls public DFT analysis, peak interpolation, `sineTracking` and
`cleaningSineTracks` functions. It is an aligned-frame adapter, not a claim that
all defaults, padding and synthesis conventions of the high-level SPR function
are identical. Both use a 20-20000 Hz band and capacity 64. SMS's positive complex
exponential has half the real cosine amplitude: its peak threshold is -66.0206 dB
for the host's -60 dB amplitude threshold. SMS frequency assignment uses its
20 Hz + 1% gate; EasyKnack retains its predictive 100-cent gate.

The reference retrospectively removes short complete track fragments. The host
waits causally before confirming a track and never rewrites earlier frames. Ignore
the first max(50 ms, two hops) and final two hops for recall/count/error metrics.
Match each expected frequency to at most one detection within max(2 Hz, 1%).
Frequency truth for vibrato is evaluated at the window centre. This deliberately
ceases to be a complete representation of a long-window FM spectrum: resolved
sidebands can be valid stationary components, not necessarily classifier errors.

Residual power is a ratio of spectral squared norms, **not** a noise probability.
Reconstruction checks add accepted window lobes back to the residual and require
maximum absolute complex-bin error below 1e-7. This is an arithmetic identity
check; it does not prove correct classification. Neither residual power nor recall
alone establishes perceptual separation quality.

## Implementation status

The host now uses the adaptive NBD/NDD/FCD classifier described in
[PARTIAL-CLASSIFIER.md](PARTIAL-CLASSIFIER.md), including calibration reproduction,
parameter meanings, tests, cost and limits. Its `sinusoidal` comparison profile
sets Classify peaks to false; minimum track duration remains 20 ms.

The snapshot below records the earlier window-fit classifier and its interference
fix. Keep it as a labelled baseline, not as the current algorithm description.

## Measurement snapshot (2026-11-06)

Python 3.12, SMS Tools 1.2, NumPy 1.26.4, SciPy 1.17.1 and clang 20.1.0 with
`-O2 -static`. The old adapter was built against `e44a6c9`; the new adapter includes
successive lobe fitting and centre-referenced phase comparisons. Values below use
2048-sample windows and default host settings. Recall refers only to the known
fixture components and the stated frequency tolerance.

| Fixture | Old host recall | New host recall | SMS recall | New host mean track count |
| --- | --- | --- | --- | --- |
| Eight 110 Hz-spaced components | 12.5% | 100% | 100% | 8.00 |
| Four inharmonic components | 100% | 100% | 100% | 4.00 |
| Four components with vibrato | 25% | 100% | 100% | 4.00 |
| Four components plus 10 dB noise | 100% | 100% | 100% | 4.19 |

At 2048 samples, pure noise produces 0.19 accepted tracks per frame with the new
host, versus 0.17 previously and 63.69 with the general SMS reference. Approximately
99.87% of noise power remains in the new host residual. This demonstrates why
unrestricted tracking alone is not a tone/noise classifier. In the noisy-tone
fixture, residual power is 8.98% of input power, close to the imposed 9.09% noise
fraction; that agreement does not establish exact recovery of the noise signal.

All 56 synthetic fixture/window combinations were measured in three profiles.
An additional private music recording was decoded locally with the existing host
and measured in the same eight windows and three profiles. It was not distributed
or uploaded, and no ground-truth separation or listening score is claimed.
The largest reconstruction error across the 128 native cases was 1.29e-12 in an
unnormalized complex FFT bin, below the 1e-7 assertion.

The 110 Hz-spaced stationary fixture reaches full recall from 2048 through 65536
samples; it does not at 512 or 1024. Vibrato recall at 8192 improves from 74.6% to
87.7%, but drops with still longer windows as local stationarity breaks down.
At 65536 the new classifier accepts about 18.82 tracks per noise frame, although
over 99% of noise power remains in the residual. These counterexamples are retained
in the benchmark: larger FFTs and a correct reconstruction identity must not be
mistaken for better classification.
