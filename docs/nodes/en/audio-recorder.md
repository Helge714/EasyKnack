# Audio Recorder
Records triggered audio takes as WAV or MP3 files.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Recording file | WAV / MP3 target; Default: None | Editor |
| Recording status | Ready / Recording / Saved / Error; Default: Ready; s | Editor |
| Channels | Mono / Stereo; Default: Stereo | Editor |
| Overwrite | Off / On; Default: Off | Editor |
| Index digits | 1–12; Default: 4 | Editor; Inspector; disabled with Overwrite |
| Save recording as | Save dialog; Default: Disabled | Editor; Inspector; completed take required |
| Audio In | Default: 0 | Audio; ±∞; Audio |
| Start | Button | Trigger; Trigger; Trigger |
| Stop | Button | Trigger; Trigger; Trigger |

## Outputs
| Output | Signal |
| --- | --- |
| — | — |

## Behavior
Start includes the triggering sample; Stop ends the take before that sample. Repeated Start triggers during recording are ignored. Stop takes precedence when both arrive together. The audio host must run. Stopping the host or removing the node finalizes the take.

WAV stores 32-bit floating-point audio at the host sample rate. Stereo preserves both channels; Mono averages left and right. Polyphonic voices are summed. Without Overwrite, `Take.wav` produces `Take_0001.wav`, then the next free index. Index digits specifies a minimum width: larger indices remain valid. Overwrite replaces the selected file on each new take.

A background thread writes a bounded queue. If the disk cannot keep up, recording stops with an error; accepted audio is finalized as a partial take. Status shows duration and the output path in its tooltip. File/open, queue-overflow, write and WAV size-limit errors are distinguished. A WAV take is limited to the standard WAV/RIFF size (approximately 4 GiB); MP3 requires an available Windows media encoder. MP3 clips input to ±1 before encoding and includes codec delay/padding; use WAV for sample-accurate timing. Choose the format in the file dialog or by the target extension. WAV remains the default.

Save recording as exports a completed take in its recorded format without ending another take or altering the automatic recording target. It is disabled until recorded audio has been finalized. Changing recording target or format/channel settings closes the current take.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/audio-recorder.md)
