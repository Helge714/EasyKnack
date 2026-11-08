> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](../../user/README.md).

# Analysis inspection

Double-click an oscilloscope or analysis plot to open the large view. Escape returns to the node editor.

## Freeze and resume

Use the plot context menu or **Freeze recording** in the large view. This copies the currently displayed samples and their horizontal extent; it freezes the display, not the audio host or analysis processors. Node and large view share the frozen snapshot. Resume shows the latest live frame. A freeze is temporary and is not stored in the node document.

## Axes and dense data

FFT frequency can be linear or logarithmic (context menu or large-view checkbox). This preference is saved with the plot editor. Logarithmic spacing starts at the first positive bin: 0 Hz cannot appear on a logarithmic axis. DC is shown separately in the caption and image metadata. Correlation uses signed lag in milliseconds, oscilloscope uses milliseconds. The vertical axis remains linear with the node's configured ±range. FFT values follow the viewer's Data choice; the graph does not convert to dB or spectral density.

Rendering retains the first, last, minimum and maximum data point of each screen pixel column, in their original order. This preserves narrow peaks while connecting flat regions. Lines have a screen-space width. The same mapping and reduction are used for exported images, at the export resolution. Non-finite samples break the curve.

The node and large view offer a mouse readout of the nearest actual value. FFT readouts show the zero-based bin index and its exact bin-center frequency, on both linear and logarithmic axes. No interpolated measurements are invented. The correlation readout references the corresponding nonnegative-lag stored index and signed lag.

## CSV export

In the large view, choose **Export → Export analysis CSV**. The snapshot is captured before opening the file dialog; frozen plots export their frozen data. Choose Standard CSV (comma separators and decimal points) or Excel (German) (semicolon separators and decimal commas, UTF-8 BOM for encoding recognition). Both formats use stable English column names and full numeric precision. FFT exports `bin,frequency_hz,value` including DC and Nyquist; scope exports `sample_index,time_ms,value`; correlation exports `lag_index,lag_ms,value` with the mirrored negative lags shown on screen. Every row also includes `received_at_utc` and `frozen`. Reception time is not acquisition time. The values follow the viewer's selected data mode; they are not implicitly converted to dB or normalized. Logarithmic display and pixel reduction do not affect the export.

## Measurement cursors and navigation

In the enlarged view, click twice to set cursors A and B. Drag the nearest cursor to move it; Shift+click places B explicitly. Both snap to actual samples/bins. The readout shows both coordinates and values, absolute frequency/time separation, signed value difference, and `1/Δt` in Hz for nonzero time separations. Live frames update the cursor values; freeze first for a stable measurement.

The wheel zooms horizontally around the pointer; Shift+wheel zooms vertically. Drag with the middle button to pan. Navigation stays within the original horizontal and vertical ranges. Use **Reset zoom and position** in the context menu to restore the full view; **Remove cursor A/B** removes an individual cursor, and **Clear measurement cursors** removes both. Cursor and zoom settings survive closing and reopening the enlarged view. These inspection settings do not modify the audio graph or CSV data.

Image export offers **Visible section** (current horizontal and vertical bounds) or **Complete analysis** (original axes). Both include measurement cursors and their readout. The selected view and data are captured when opening the export settings.

## Image export

**Export analysis image** captures a separate immutable snapshot when the dialog opens. Subsequent live updates cannot change that export. Choose PNG (lossless, recommended for plots) or JPG with adjustable quality, and independent width/height in pixels. Export uses a white publication-style background and labeled axes, independent of node-editor zoom. Writing happens in the background and replaces the destination atomically when possible.

Metadata reports the number of received display values and their UI reception timestamp in UTC. This timestamp is not a sample-accurate acquisition time. The image contains the analysis data supplied by the host, not a new high-resolution raw audio capture; increasing image resolution cannot increase the source measurement resolution. The existing oscilloscope sends 256 display samples, while FFT resolution depends on its configured window.
