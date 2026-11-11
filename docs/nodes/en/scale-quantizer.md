# Scale Quantizer
Rounds frequencies to an equal-tempered scale.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Mode | Scale, Fixed; Default: Scale | Editor |
| Root | C, C#, D, D#, E, F, F#, G, G#, A, A#, B; Default: C | Editor |
| Scale | Chromatic, Major, Minor, Major pentatonic, Minor pentatonic; Default: Chromatic | Editor |
| Rate | Auto, Audio, Control; Default: Auto | Editor; Inspector |
| Frequency | 0–20 000 Hz; Default: 440 Hz; Hz, kHz | Value; Positive Hz; Auto |
| Fixed target | 1–20 000 Hz; Default: 440 Hz; Hz, kHz | Value; Positive Hz; Auto |
| A4 tuning | 400–480 Hz; Default: 440 Hz; Hz, kHz | Value; Inspector |
| Hysteresis | 0–12 st; Default: 0.1 st; st, cent | Value; 0–12 st; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Target | Value; 0–∞ Hz |

## Behavior
Scale maps `69 + 12 log2(f / A4)` to the nearest allowed note, then converts back to Hz. Exact ties select the lower note. Hysteresis adds a switching margin beyond the midpoint; 0 switches immediately. Nonpositive/invalid input gives 0. Chromatic ignores Root. Fixed uses Fixed target instead of the scale. An inclusive octave interval can contain 13 output notes: twelve semitone intervals have thirteen endpoints.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Frequency → Target |
| Insertion | Frequency → Target |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/scale-quantizer.md)
