# Finite averaging

Under **Analysis**, use **Value Average** for numeric signals and **Analysis Average**
for spectra or autocorrelation. Both take one measurement when you press **Start**
or send a trigger. Another Start discards the previous acquisition and restarts it.

Choose **Time** to measure for a duration in seconds, milliseconds or audio samples.
Choose **Measurements** to accept an exact number of observations instead. Duration
and count settings are latched at Start. Every accepted observation has equal weight.
The output is the running arithmetic mean while measuring and holds its final value
afterward. **Count** reports accepted observations; **Finished** triggers once when
the chosen duration or count is reached. Stopping the audio host stops its clock.

## Numeric values

Connect a feature output to **Value Average → Value Viewer** to average that feature.
An Audio source contributes one value per sample; Control contributes one value per
block. An unconnected Value editor also contributes once per block. Invalid numeric
values are skipped. A timed acquisition with no valid observations finishes with zero.

**Standard Deviation**, **Minimum** and **Maximum** use the same observations as
**Mean** and hold their results when the measurement finishes. Standard deviation
uses the population formula `sqrt(sum((x - mean)^2) / count)`, with zero for fewer
than two observations. All four statistics have the same unit as the incoming values.
Start resets all statistics together.

To measure movement of a frequency centre, connect **Spectral Features → Centroid**
to Value Average. Its standard deviation measures variation of the centroid over
time in Hz. This differs from Spectral Spread, which measures frequency dispersion
within each spectrum, and does not indicate how fast the centroid moves.

## Spectra and autocorrelation

For averaged spectral features, connect **FFT → Analysis Average → Spectral Features**.
Connect the averaged analysis to **Analysis Viewer** to inspect or export it.
For correlation, use **Autocorrelation → Analysis Average → Analysis Viewer**.

Spectra are averaged as power per bin, not as complex values: opposing phases do not
cancel. Magnitude display is the square root of mean power, with the usual FFT
scaling. Phase is unavailable and the statistical output cannot connect to IFFT.
Autocorrelations are averaged directly per lag. Averaging features after calculating
them generally gives a different result from calculating features of an averaged spectrum.

Only fresh analysis frames count. Synchronous FFT batches contribute every completed
frame, including when several frames complete in one block. Asynchronous snapshots
contribute once when delivered; the snapshot already present at Start is excluded.
Time mode gates delivery, not the original capture time of asynchronous frames.
FFT windows are indivisible observations and may contain audio from before Start.
Overlap therefore produces correlated observations, not independent measurements.

Keep the source, FFT size, window and sample rate fixed during an acquisition.
Changing analysis format or source cancels an active measurement and clears its
result without a Finished trigger. A completed result stays held until Start.
If no fresh frame arrives during a timed measurement, Count stays zero and no analysis
result is published; count mode waits until enough fresh frames arrive.

## Recording a measurement series

Add **Analysis → Measurement Recorder**. Set **Inputs**, then name each column in
its inspector (include units in names, for example `Centroid [Hz]`). Connect Mean,
Standard Deviation, Minimum and Maximum to separate inputs. Connect the averaging
node's **Finished** output to **Record**. Each trigger captures all inputs at that
sample position; the unconnected Record button captures a row manually.

Open **Table…** to inspect rows. Each row includes its session identifier and audio
time in seconds since that recorder session began. Host restarts or a changed input
count begin a new session. Invalid numeric inputs are shown as empty cells, not zero.
Changing the input count retains earlier rows; missing columns remain empty.

Measurements are stored in the `.knack` document and included in autosave recovery.
Save the document to keep them. Capture continues while inspecting another group;
automatic rows do not add Undo entries. Ordinary graph Undo retains captured rows.
Tables hold up to 4096 rows; when full, earlier rows are retained and the status
asks you to clear the table. **Clear** empties the document table and starts a new
acquisition. It does not affect previously exported files.

Use **CSV export…** for an independent snapshot. CSV uses commas and decimal points;
CSV (Excel DE) uses semicolons, decimal commas and a UTF-8 byte-order mark. The file
location is selected at export; recording never writes automatically to a separate
file. Multiple triggers at the same sample position are treated as one record.

## Measuring periodicity

Connect **FFT → Autocorrelation → Correlation Features**. The new node reads the
existing correlation; it does not perform another FFT. Its Control outputs hold
between analysis frames; there is no Updated trigger.

- **Periodicity** is the strongest interpolated positive local correlation peak,
  normalized by lag zero and limited to 0–1. It is not a probability.
- **Period** is the selected repetition time (seconds, ms or samples).
- **Frequency** is `1 / Period`, a repetition frequency, not necessarily perceived
  pitch. Nearly equal peak heights prefer the shortest period; octave ambiguities
  remain possible for harmonic mixtures, modulation and polyphonic signals.
- **HNR** estimates `10 log10(p / (1 - p))` from Periodicity, capped at ±120 dB.
  It assumes periodic structure plus uncorrelated noise; it is not an independent
  descriptor or a calibrated noise measurement. Window/overlap normalization affects
  the estimate. Perfectly periodic input reaches a finite ceiling.
- **Valid** is 1 only when a peak passes the search and amplitude thresholds.
  Otherwise Period and Frequency are zero. A weak peak can still yield Periodicity
  and HNR while Valid is zero. Silence, missing correlation or an unsuitable resource
  return zero descriptors and an HNR floor of −120 dB.

In the inspector, choose minimum/maximum repetition frequencies, a Periodicity
threshold (default 0.3) and a linear source-RMS silence threshold (default 0.000001).
The search excludes lag zero and monotonic neighbours, respects Nyquist and requires
at least three repetitions within the available correlation window. Increase the
FFT window to measure slower repetition. Reversed bounds yield an invalid result.

The node describes the incoming curve, not a separately corrected estimator.
For a periodicity/HNR comparison with little taper bias, use FFT **Rectangular**,
**2x padding**, **Remove DC**, and Autocorrelation **Unbiased**. Hann and other tapering
windows reduce nonzero-lag peaks; unbiased overlap correction does not undo that
window shape. Use identical settings across experiments. This is not the complete
Praat/Boersma pitch and harmonicity algorithm.

Connect Periodicity or Frequency to **Value Average** to measure its temporal mean,
standard deviation and extrema. Its observations follow the block/sample rules above,
not one observation per analysis frame. Record the completed statistics through
**Finished → Measurement Recorder: Record**. Gate invalid frequency estimates if you
want to exclude their zeros rather than include them in the measurement.
