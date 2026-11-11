# File Buffer
Loads a selected audio-file region into a buffer resource.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| File | WAV / MP3 | Editor |
| Snap to zero | Off / On; Default: Off | Editor |
| Waveform | Region display | Editor |
| Start | 0–End − 1 smp; Default: 0 s; s, ms, smp | Editor |
| End | Start + 1 smp–file duration; Default: File duration; s, ms, smp | Editor |
| Memory | Memory display | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Buffer | Buffer resource; Loaded samples; Control |
| Duration (s) | Value; > 0 s; Control |
| Samples | Value; ≥ 1; Control |

## Behavior
Only the selected region is retained as playable audio. Start/End are fixed-range sliders with at least one sample between them. Waveform handles edit the same region; optional zero snapping helps choose boundaries. Duration and Samples describe the loaded region, with Samples counting frames per channel. The buffer is immutable; its end acts as write head. Both [Speed](buffer-reader-speed.md) and [Position](buffer-reader-position.md) readers can play it. Changing the region replaces the buffer and resets readers.

MP3 import honors available Xing/LAME encoder-delay and end-padding metadata for gapless playback. Without metadata, quiet samples are preserved rather than guessed away. Older MP3 caches are reimported in the background when their file editor is restored; if the node is inside an unopened group, open it or select the source file again. Imported duration and sample count can therefore decrease.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/file-buffer.md)
