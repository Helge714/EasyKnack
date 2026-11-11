# Voice Merge
Combines polyphonic voices into one signal per channel.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Mode | Add, Mean; Default: Add | Editor |
| Voices | — | Audio; ±∞; Audio |

## Outputs
| Output | Signal |
| --- | --- |
| Audio | Audio; ±∞ |

## Behavior
Add sums all voices; Mean divides by the configured voice count, including silent or released voices. Stereo positions are retained. A mono voice domain passes through. Effects after Merge run on the combined signal instead of once per voice.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |
| Insertion | Voices → Audio |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/voice-merge.md)
