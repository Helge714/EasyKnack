# Value Viewer
Displays numeric inputs as values or time histories.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Values | Graph / Values; Default: Graph | Editor |
| Inputs | 1–8; Default: 1 | Editor |
| Time window | 0.1–600 s; Default: 10 s; s, ms, smp | Editor |
| Record every sample | Off / On; Default: Off | Editor; Inspector |
| Channel | Left / Right / Mix; Default: Left | Editor; Inspector |
| Values: names, units, order | Per-input settings; Default: Value 1…8; no unit | Editor; Inspector |
| Value 1…8 | — | Value; ±∞; Auto; According to Inputs |

## Outputs
| Output | Signal |
| --- | --- |
| — | — |

## Behavior
Inputs determines both the ports and displayed series. Inspector rows change names, unit labels and order for ports, display and exports together; unit labels do not convert values. History mode shows separate plots or an overlay; Values mode shows discrete readouts. Freeze holds a snapshot. Expanded views allow zoom, pan and measurement cursors; export supports images and tables for the full history or visible section.

Without Record every sample, each 10 ms interval retains minimum, maximum and last value. With it enabled, every sample is retained. History is bounded to one million input records, so its effective duration may be shorter than Time window. Transport gaps remain visible gaps. Control values hold over their block; Mix observes (L+R)/2.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/value-viewer.md)
