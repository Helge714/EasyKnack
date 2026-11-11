# Measurement Recorder
Appends triggered numeric snapshots to a document table.
<a id="overview"></a>
<a id="inputs"></a>
<a id="mute"></a>
<a id="insertion"></a>
<a id="delayable"></a>

## Controls
| Parameter | Setting | Signal / condition |
| --- | --- | --- |
| Measurements | Table / CSV / Clear; Default: Empty | Editor |
| Record | Button | Trigger; Trigger; Trigger |
| Inputs | 1–8; Default: 1 | Editor |
| Values: names, units, order | Per-input settings; Default: Value 1…8; no unit | Editor; Inspector |
| Value 1…8 | Default: 0 | Value; ±∞; Auto; According to Inputs |

## Outputs
| Output | Signal |
| --- | --- |
| — | — |

## Behavior
Every Record trigger appends one row containing all inputs at that sample position. The audio host must run to capture. The table is stored in the .knack document, with up to 4096 rows. Open the table to edit the test label after the row index. Inspector rows control names, unit labels and common port/column ordering; labels do not convert values. CSV exports offer standard and German-Excel formats. Clear removes stored rows.

## Routing
| Function | Behavior |
| --- | --- |
| Mute | No bypass |

[Signals and Mute](../../user/en/signals.md) · [Manual](../../user/en/index.md)

[Node index](index.md) · [Deutsch](../de/measurement-recorder.md)
