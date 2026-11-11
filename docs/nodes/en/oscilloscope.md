# Oscilloscope
Displays a triggered audio waveform.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Oscilloscope | Graph | Editor |
| Audio In | — | Audio; ±∞; Audio |
| Channel | Left / Right / Mix; Default: Left | Editor |
| Vertical range | ≥ 0.001; Default: 1 | Editor |
| Trigger low | ±1; Default: −0.1 | Editor |
| Trigger high | ±1; Default: 0.1 | Editor |
| Window | 6–200 ms; Default: 20 ms; s, ms, smp | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| — | — |

## Behavior
The trigger arms below Trigger low and captures on the following crossing of Trigger high. If no crossing arrives, capture runs automatically. The graph context menu shows/hides threshold lines. Polyphonic voices are summed before measurement; Mix = (L+R)/2. Vertical range r displays −r–r and changes no signal values.

Right-click the graph to Freeze/Resume or open an enlarged view; double-click also enlarges it. The expanded graph supports wheel zoom, Shift+wheel vertical zoom, middle-drag panning and two draggable measurement cursors. Context menus clear cursors individually or reset the view. Export offers an image of the complete graph or visible section and a CSV table. Freeze holds the display, not audio processing.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/oscilloscope.md)
