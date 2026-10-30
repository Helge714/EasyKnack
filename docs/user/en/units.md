# Units

A unit determines how a number is displayed and interpreted. It changes neither
the [data type](data-types.md) nor the [rate](rates.md).

## Choosing a unit

Open **Unit** in a supported numeric port's or label's context menu. This also
works for connected inputs with hidden sliders. The slider and port menus change
the same setting. Only units allowed for that parameter are offered. The chosen
unit appears after the number or in the port label.

Changing the display unit of a manually set parameter represents the same
physical quantity through conversion, for example 1 s as 1,000 ms.

## Connected values

Connections carry numbers **without automatic unit matching**. The output unit
determines the emitted number; the input unit determines how its receiver
interprets that number.

| Output | Input | Interpretation |
| --- | --- | --- |
| 1 s | s | 1 s |
| 1 s | ms | 1 ms |
| 1,000 ms | ms | 1,000 ms = 1 s |

To preserve the physical quantity, choose the same input and output unit or
insert a **Unit Converter**. Choosing a different input unit alone deliberately
reinterprets the signal.

## Unit groups

| Quantity | Units | Relationship |
| --- | --- | --- |
| Frequency | Hz, kHz, BPM | 1 kHz = 1,000 Hz; 60 BPM = 1 Hz |
| Time | s, ms, smp | 1 s = 1,000 ms; at 48,000 Hz: 1 s = 48,000 smp |
| Angle | turn, °, rad | 1 turn = 360° = 2π rad |
| Length | m, km, cm, mm | 1 m = 100 cm = 1,000 mm |
| Amplitude ratio | Factor, %, dB | Factor 1 = 100 % = 0 dB; dB = 20 · log10(factor) for positive factors |
| Pitch ratio | Factor, cent, st, oct | Factor 2 = 1 oct = 12 st = 1,200 cent |
| Filter quality | Q | Dimensionless quality factor, not a frequency value |

Amplitude factor 0 means silence; no finite dB number is its exact mathematical
equivalent. Not every parameter offers all units within a group.

Unit Converter can also convert frequency to period and back: **T = 1 / f**,
for example 10 Hz = 0.1 s = 100 ms. This is a calculation, not automatic
matching when connecting ports.

## Groups and vectors

Group inputs and outputs can expose their own units. A group converts to the
units required internally at its boundary. The outer connection still follows
the numeric interpretation described above.

Vector units belong to the components or selected vector quantity. Use the
appropriate vector editors and nodes; scalar port conversion is not a substitute
for a vector transformation.

[Fundamentals](index.md) · [Deutsch](../de/units.md)
