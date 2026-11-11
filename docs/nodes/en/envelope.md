# Envelope
Generates a curve-shaped envelope from Gate and Trigger.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Envelope | Curve editor; Default: ADSR | Editor |
| Gate | 0–1; Default: 0 | Value; ±∞; Audio |
| Trigger | Button | Trigger; Trigger |
| Attack + Decay | 0–60 000 ms; Default: 300 ms; ms, s, smp | Value; 0–60 000 ms; Auto |
| Release | 0–60 000 ms; Default: 300 ms; ms, s, smp | Value; 0–60 000 ms; Auto |
| Sustain position | 0.01–0.99; Default: 0.5 | Value; 0.01–0.99; Auto |
| Hold | Off / On; Default: On | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| Envelope | Value; 0–1; Audio |

## Behavior
A Gate rise above 0 or a Trigger starts Attack/Decay. Sustain position divides the curve into attack/decay and release portions. With Hold on, a positive Gate holds the sustain value; releasing Gate starts Release from the current level. With Hold off, Release follows Attack/Decay automatically. Retriggers start from the current envelope level. The result is limited to 0–1.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/envelope.md)
