# Analysis Average
Performs one triggered arithmetic measurement and holds its final result.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Analysis | — | Analysis resource; Spectrum or correlation; Control |
| Start | Button | Trigger; Trigger; Trigger |
| Measure by | Time / Measurements; Default: Time | Editor |
| Duration | 1 smp–86400 s; Default: 1 s; s, ms, smp | Editor; Time |
| Measurements | 1–16777216; Default: 16 | Editor; Measurements |

## Outputs
| Output | Signal |
| --- | --- |
| Mean | Analysis resource; Spectrum or correlation; Control |
| Count | Value; ≥ 0; Audio |
| Finished | Trigger |

## Behavior
Start clears and restarts acquisition, latching Duration/Measurements. Time measures audio-clock duration; Measurements stops after the selected number of accepted values. Finished emits once at completion. The running result is available during acquisition and held afterward until Start.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | Analysis → Mean |
| Insertion | Analysis → Mean |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/analysis-average.md)
