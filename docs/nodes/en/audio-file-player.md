# Audio File Player
Plays an audio file with transport and looping controls.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| File | WAV / MP3 | Editor |
| Play / Pause | Off / On; Default: 0 | Value; 0–1; Control |
| Reset | Button | Trigger; Trigger; Trigger |
| Seek position | 0–1; Default: 0; Factor, s, ms, smp | Value; 0–1; Control |
| Speed | 0–4; Default: 1 | Value; 0–4; Control |
| Loop | Off / On; Default: Off | Editor |
| Loop start | 0–1; Default: 0; Factor, s, ms, smp | Editor |
| Loop end | 0–1; Default: 1; Factor, s, ms, smp | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Audio Out | Audio; ±∞ |
| Position | Value; 0–1; Control |
| Duration (s) | Value; ≥ 0 s; Control |
| Buffer underruns | Value; ≥ 0; Control |

## Behavior
A background stream supplies decoded audio. Speed changes pitch and duration; 0 stops advancement. Play ≥ 0.5 enables playback. Reset starts at Loop start when looping, otherwise at the file beginning. Seek values are fractions of the whole file; seconds and samples are alternative displays of that position. The unconnected position slider follows playback and can be dragged to seek. Without Loop, playback becomes silent at the end. Buffer underruns counts blocks missing decoded data. For reusable in-memory audio use [File Buffer](file-buffer.md).

MP3 import honors available Xing/LAME encoder-delay and end-padding metadata for gapless playback. Without metadata, quiet samples are preserved rather than guessed away. Older MP3 caches are reimported in the background when their file editor is restored; if the node is inside an unopened group, open it or select the source file again. Imported duration and sample count can therefore decrease.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/audio-file-player.md)
