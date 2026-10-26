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
