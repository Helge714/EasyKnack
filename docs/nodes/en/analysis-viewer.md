# Analysis Viewer
Displays a spectrum or autocorrelation resource.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Analysis | Graph | Editor |
| Analysis | — | Analysis resource; Spectrum or correlation; Control |
| Data | Auto / Magnitude / Phase / Power / Correlation; Default: Auto | Editor |
| Vertical range | ≥ 0.001; Default: 1; Phase: π | Editor |

## Outputs
| Output | Signal |
| --- | --- |
| — | — |

## Behavior
Auto follows the resource type. Magnitude displays one-sided amplitude, Power its square, Phase radians relative to the FFT window start; Correlation displays lag values. Unsupported views fall back to the resource default. Vertical range affects only display scaling. Spectrum context menus switch linear/logarithmic frequency axes; hover reports bin, frequency and value. The viewer does not calculate another FFT or autocorrelation.

Right-click the graph to Freeze/Resume or open an enlarged view; double-click also enlarges it. The expanded graph supports wheel zoom, Shift+wheel vertical zoom, middle-drag panning and two draggable measurement cursors. Context menus clear cursors individually or reset the view. Export offers an image of the complete graph or visible section and a CSV table. Freeze holds the display, not audio processing.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/analysis-viewer.md)
