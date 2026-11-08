> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](../../user/README.md).

# Audio File player

The Audio File node is under **Input / Output**. Connect Audio Out to an output or
effect, select a file and enable Play / Pause. The host must also be running.

## Files and controls

- WAV and MP3 are decoded using the bundled miniaudio decoder.
  Convert other formats to WAV or MP3 before importing. No external codec is required.
- Mono and stereo are supported. Mono retains its gain and is repeated for both
  destination channels. Multichannel files currently report an explicit error.
- Play / Pause is a Control input: 0 pauses, 1 plays. Pause retains the position.
  Toggling playback after EOF starts again. Reset jumps to the beginning, or the
  loop start when looping; it does not change the play/pause state.
- Seek position is a 0–1 Control input. A changed value seeks; a constant value
  does not repeatedly restart playback. Use Reset to request the same start again.
- Loop start/end use normalized 0–1 file positions. There is no automatic
  crossfade: discontinuous loop boundaries can click. Equal/reversed boundaries
  are clamped to a minimal playable segment in the native processor.
- Outputs: Audio, current Position (0–1), Duration (seconds), Buffer underruns
  (missed processing blocks since initialization, retained during compatible edits).

## Cache and realtime behavior

Import runs in a background task. miniaudio preserves channel count and converts to
48 kHz floating-point PCM in `~/.easyknack/audio-cache`. The node shows decoding or
an error while this happens. Selecting a new file preserves the old reference if
decoding fails. Network protocols are disabled for imports.

Playback begins after import completes. Long files require decoding time and disk
space, not a full in-memory audio copy. The native reader has twelve 8192-frame
pages (at most about 768 KiB of sample storage), populated by a worker. File I/O,
decoding, allocation and waiting do not occur in `process()`. Seeks outside the
prefetched region may briefly produce silence; position waits for available data,
and Buffer underruns reports the miss. Non-48-kHz hosts currently use linear
sample interpolation; the normal 48-kHz path reads samples without resampling.

The document stores the original absolute path and the prepared cache path. If a
cache is missing, the visible file editor rebuilds it from the source in the
background. Files are imported snapshots: changing the original file does not
silently alter a playing patch; select it again to reimport. Cache cleanup is
currently manual, with the host stopped. Relative project paths, portable media
collection and automatic cache housekeeping remain planned. Baked/exported groups
using this node still depend on the local cache; they are not portable media packages.

## Validation

`AudioFilePlayerTest` generates PCM/float WAV files and decodes an original MP3 test tone, tests Unicode
paths and corrupt input, checks native stereo/seek/pause/loop/state transfer and
sends real graph updates to a null-device audio host. The existing host client
tests also run against the rebuilt executable. These are not a real-device
listening test or a disk-load stress benchmark.

## Dependency and distribution

The bundled audio-host executable also offers an isolated `--decode-audio` mode.
It initializes only a miniaudio file decoder and writes the existing EKPCM001
cache format in bounded chunks; it opens no device or control socket. Java invokes
this mode in a separate process with UTF-8 paths encoded as hexadecimal arguments.
FFmpeg and JavaCPP are no longer runtime dependencies. miniaudio's selected MIT-0
license is retained in the vendored header and installer notices.

Existing caches remain readable. Documents referencing other source formats can
use their existing caches, but rebuilding those caches requires converting the
original media and selecting the converted file.

## Playback controls

The unconnected Seek position slider follows host telemetry without changing the
saved graph or creating undo entries. While editing, feedback is held back.
Speed is a Control input from 0 to 4: 1 is normal playback, 0 holds the position
and outputs silence. Speed changes pitch; this is not time stretching.

Seek position and loop boundaries offer fraction, s, ms and smp via Unit in the
slider context menu. These are display conversions relative to the imported
file duration; connections and saved parameter values remain normalized 0–1.
Samples refer to the decoded 48 kHz PCM. Unit selection currently belongs to the
control presentation and is reset when its view is recreated.

## File Buffer

Add **Buffer > File Buffer** and connect Buffer to either existing Buffer Reader.
The file is decoded through the same importer as Audio File, then loaded as planar
float PCM into the native host. Duration (seconds) and sample count are separate
Control outputs. The Memory label estimates PCM RAM, not compressed source size.
Stereo costs 384,000 bytes/second at 48 kHz. No arbitrary duration limit is imposed.

The file has a fixed write head at its end (equivalent to position zero on the
circular buffer). Readers wrap and interpolate normally. The Speed Reader starts
at zero and resets to zero for a file buffer. Its speed accounts for the buffer's
sample rate; recording-buffer behavior is unchanged.

Reader instances and graph replacements share immutable data through a weak
cache keyed by decoded PCM path. Loading is outside the audio callback, in small
chunks without a second full-file temporary allocation. Once no graph owns the
data it is released. A failed native file load leaves the previous graph active.
The per-node estimate is not a system-wide RAM counter; shared buffers must not
be counted once per reader. Audio File remains the bounded-memory streaming option.

File Buffer now has Start and End sliders directly in the node, in s/ms/smp.
End = 0 uses the file end. The selection is rounded to sample boundaries and
only that range is allocated/read into host RAM. Readers wrap within it; Duration
and Samples describe the selection. The disk PCM cache still contains the full
file so the selection can later expand without another media decode. Invalid or
empty selections keep the previous active audio graph. Changes reload outside
the callback and may temporarily retain both old and new selections during the
safe graph swap. The displayed memory is the selected PCM size, not peak swap RAM.

## Waveform selection and sampler

File Buffer displays the full file as a bounded, peak-preserving stereo overview.
Drag the nearest selection boundary: preview updates immediately, the model and
native buffer update once on release (one undo edit). The two range sliders remain
available. Overview generation reads the PCM sequentially on a background worker
and retains at most 2048 min/max pairs per channel, not another full PCM copy.

Examples > Buffer > Sampler contains an editable monophonic sampler. Select a file
in File Buffer; set the Divide node's B value to the sample's recorded root
frequency in Hz (default 440 Hz). Focus the MIDI keyboard to play. Frequency/root
frequency controls Speed; Note-On resets the reader. An envelope gates the audio
and a Gain node provides headroom. This is looping resampling, not time stretching
or automatic pitch detection. Reader speed remains limited to -8..8.

The Position Reader intentionally has no Reset: its external Position signal is
its clock. To synchronize additional readers, connect the Speed Reader's Read
position to their Position input and use the same Buffer. Reset the Speed Reader;
all position followers then jump at the same sample.

Both Buffer Readers now have Wrap (default on for existing graphs). With Wrap off,
file playback clamps to the first/last sample and holds it; it does not output
silence. Interpolation also clamps its second sample. Reverse speed can move away
from the end again; Reset returns the Speed Reader to the beginning.

For live buffers the Speed Reader uses an absolute sample timeline, constrained
to the samples actually available at that sample time. Position Reader coordinates
still address physical buffer slots: without Wrap the Position-minus-Offset value
is clamped to 0..1, and slots refer to their latest recorded occurrence. It can
explicitly seek to older valid samples; interpolation never reads a future write.

Waveform navigation: wheel zooms around the pointer; Shift-wheel, middle drag or
Shift-left-drag pans. Context menu offers full-file/selection fit and audition.
Zoom reads actual samples in the visible region, with a one-sample minimum view;
obsolete overview jobs cancel during chunk reads. View navigation does not edit
the audio graph.

Snap to zero is a persisted node checkbox, applied when releasing a waveform
boundary. It searches +/- 10 ms within the allowed selection limits for the
nearest common sign crossing of all channels. With no shared stereo crossing,
the requested position is retained. Sliders remain exact manual controls.

Audition plays the selection once via Java Sound's system default output,
independent of the DSP graph. It streams the PCM cache with bounded memory,
converts to 16-bit output, and applies 5 ms edge fades for audition only. A new
preview stops the previous one; Stop audition and closing the app stop playback.
The stored audio is unchanged. Device availability/actual listening needs manual
verification on the user's selected Windows output.
