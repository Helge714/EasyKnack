# ZRR07 reproduction: source questions

The requested replacement is the deterministic threshold construction in sections
5.1/5.2 of [Zivanovic, Roebel and Rodet, DAFx 2007](https://www.dafx.de/paper-archive/2007/Papers/p047.pdf),
not the finite-distribution quantiles currently embedded in EasyKnack. Those
quantiles must not be described as a reproduction of the published thresholds.

The user-provided source is the 2011 second edition of *DAFx*, chapter 10.3.
Its peak-estimation discussion refers to ZRR07 for this classifier. M-file 10.7
(`sinemodel.m`) implements amplitude-threshold peak selection and sinusoidal
synthesis, not the missing classifier. Section 10.3.5's worked residual model
assumes harmonic/monophonic partial selection. Neither is a drop-in reference for
the requested general sinusoidal/noise classifier. Do not silently substitute one
of these book examples and promise equivalent tonal/noise separation.

## Specified parts

- Equations 1-10 define NBD, NDD and FCD.
- Table 1 gives the rounded noise-free Hann example: NBD <= 0.13,
  0.13 <= NDD <= 0.16; FCD >= N/M identifies a sidelobe.
- Equation 11 defines the AM/FM model. Table 2 gives window-specific phase
  settings for the NBD maximum.
- Section 5.1 obtains the finite-SNR NBD limit by considering only the model's
  main-lobe bins above the specified noise floor. It does not use quantiles or
  a monotone envelope fitted to synthetic Gaussian samples.
- Equations 12/13 give the approximate two duration-extremizing signal patterns.
  Direct time-domain moments at the printed example settings give NDD about
  0.134975 and 0.159165, within 1% of the 0.1345/0.1601 extrema noted in Figure 5.
- Equation 14 gives two deterministic narrowband noise patterns. Equation 15
  and Table 3 specify polynomial coefficient rows for their modulation indices.

## Unresolved before a faithful finite-SNR implementation

1. **Coefficient ordering and Hamming values.** Table 3 does not label the exponent
   belonging to each row. Under the usual descending second-order interpretation,
   the Hamming a rows yield
   `0.0003 * 10^2 - 0.2816 * 10 + 2.4716 = -0.3144` at 10 dB.
   Section 5.2 requires the modulation indices to exceed 1. Interpreting the rows
   as ascending powers yields entirely different values. Do not choose an order,
   repair a printed coefficient or clamp the result simply because a test sounds
   better. Establish it from author code, an erratum or the expanded publication.
2. **Amplitude A.** Section 5.2 describes its scaling through the spectral energy
   of each noise pattern relative to the sinusoidal main lobe, but does not give
   the normalization equation. A peak-bin ratio and a band-energy ratio are
   different operations for the two narrowband patterns. An assumed normalization
   would again introduce an implementation choice at the decisive threshold.

The expanded article is *Computer Music Journal* 32(2), 57-67 (2008),
[DOI 10.1162/COMJ.2008.32.2.57](https://doi.org/10.1162/COMJ.2008.32.2.57).
HAL lists it as [hal-01106336](https://hal.science/hal-01106336), with internal
IRCAM reference `Zivanovic08a`, but its public metadata supplies no full-text file.
The author-code/expanded-text clarification is needed before claiming the
finite-SNR algorithm is reproduced faithfully.

## Reproduce these checks

```powershell
target/partial-reference-env/Scripts/python.exe scripts/zrr07_source_audit.py --paper target/dafx-reading/ZRR07.pdf
```

The audit uses literal published coefficients, records both polynomial orderings
without selecting one, checks the duration patterns, and writes an ignored JSON
report. It imports no production classifier and generates no runtime thresholds.

Production replacement is pending these source clarifications. This audit does
not claim improved separation and does not silently exchange the existing
classifier for a guessed finite-SNR implementation or a noise-free-only fallback.
