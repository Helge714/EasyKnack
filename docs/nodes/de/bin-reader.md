# Binleser (Bin Reader)

## Übersicht

Liest einen indizierten Bin des ursprünglichen Eingangsspektrums.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Spektrum (Spectrum) | [Spectrum-Ressource](../../user/de/data-types.md#spectrum) | — | — | — | — | [Control](../../user/de/rates.md#control) | Immer |
| Binindex (Bin index) | [Wert](../../user/de/data-types.md#value) | ±∞ | Randmodusabhängig | 0 | — | [Auto](../../user/de/rates.md#auto) | Immer |
| Außerhalb des Spektrums (Outside spectrum) | [Editor](../../user/de/reference-tables.md) | Null, Begrenzen, Wrap | — | Null | — | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| Realteil (Real) | [Wert](../../user/de/data-types.md#value) | ±∞ | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Imaginärteil (Imaginary) | [Wert](../../user/de/data-types.md#value) | ±∞ | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Betrag (Magnitude) | [Wert](../../user/de/data-types.md#value) | 0–∞ | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Phase | [Wert](../../user/de/data-types.md#value) | ±π rad | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Amplitude | [Wert](../../user/de/data-types.md#value) | 0–∞ | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |
| Frequenz (Frequency) | [Wert](../../user/de/data-types.md#value) | 0–fs/2 Hz | [Audio (Bin/Frame)](../../user/de/rates.md) | [Auto](../../user/de/data-types.md#channels) |

## Verhalten

In einer [Spectral Group](spectral-group.md) interpolieren gebrochene Indizes benachbarte komplexe Bins. Gelesen wird immer der ursprüngliche Quellframe, unabhängig von anderen Binschreibvorgängen. Zero liefert außerhalb des einseitigen Spektrums null, Clamp hält den Rand, Wrap wiederholt 0…N/2. Ohne Spectrum werden Nullen ausgegeben. Amplitude teilt den Rohbetrag durch die Analysefenstersumme und verdoppelt Bins außer DC/Nyquist; sie eignet sich für Oszillatorgewichte. Phase ist in Radiant.

## Mute

| [Mutable](../../user/de/muting.md) | [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- | --- |
| Ja | — | — |

## Einfügen

| [Eingang](../../user/de/reference-tables.md#input-output) | [Ausgang](../../user/de/reference-tables.md#input-output) |
| --- | --- |
| — | — |

## Delayable

| [Sampleweises Feedback](../../user/de/reference-tables.md#feedback) |
| --- |
| Nicht unterstützt |

[Node-Verzeichnis](index.md) · [English](../en/bin-reader.md)
