# Bin Output
Writes a complex Spectrum output from per-bin values.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Components | Real / imaginary, Magnitude / phase; Default: Real / imaginary | Editor |
| Real / Magnitude | ±∞; Default: 0 | Value; ±∞; polar magnitude ≥ 0; Auto |
| Imaginary / Phase | ±∞; Default: 0 | Value; ±∞; Auto |

## Outputs
| Output | Signal |
| --- | --- |
| Spectrum | Spectrum resource; Control |

## Behavior
Cartesian writes `Real + i × Imaginary`; Polar writes `max(0,Magnitude) × exp(i×Phase)` with Phase in radians. Each Bin Output creates its own Spectrum while sharing the group's bin schedule. Real-signal conjugate symmetry is restored by the group. Only frame/bin computations may feed it, not oscillator audio. See [Spectral Group](spectral-group.md).

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Not mutable |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/bin-output.md)
