# Oszilloskop (Oscilloscope)

## Übersicht

Zeigt eine getriggerte Audio-Wellenform.

## Eingänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Reglerbereich](../../user/de/reference-tables.md#editor-range) | [Signalbereich](../../user/de/reference-tables.md#signal-range) | [Standardwert](../../user/de/reference-tables.md#default) | [Einheit](../../user/de/units.md) | [Rate](../../user/de/rates.md) | [Sichtbarkeit](../../user/de/reference-tables.md#visibility) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Oszilloskop (Oscilloscope) | [Editor](../../user/de/reference-tables.md) | Graph | — | — | — | — | Immer |
| Audio-Eingang (Audio In) | [Audio](../../user/de/data-types.md#audio) | — | ±∞ | — | — | [Audio](../../user/de/rates.md#audio) | Immer |
| Kanal (Channel) | [Editor](../../user/de/reference-tables.md) | Links / Rechts / Mix | — | Links | — | — | Immer |
| Vertikaler Bereich (Vertical range) | [Editor](../../user/de/reference-tables.md) | ≥ 0.001 | — | 1 | — | — | Immer |
| Untere Triggerschwelle (Trigger low) | [Editor](../../user/de/reference-tables.md) | ±1 | — | −0.1 | — | — | Immer |
| Obere Triggerschwelle (Trigger high) | [Editor](../../user/de/reference-tables.md) | ±1 | — | 0.1 | — | — | Immer |
| Fenster (Window) | [Editor](../../user/de/reference-tables.md) | 6–200 ms | — | 20 ms | [s](../../user/de/units.md), [ms](../../user/de/units.md), [smp](../../user/de/units.md) | — | Immer |

## Ausgänge

| [Bezeichnung](../../user/de/reference-tables.md#name) | [Datentyp](../../user/de/data-types.md) | [Wertebereich](../../user/de/reference-tables.md#range) | [Rate](../../user/de/rates.md) | [Kanal](../../user/de/data-types.md#channels) |
| --- | --- | --- | --- | --- |
| — | — | — | — | — |

## Verhalten

Der Trigger wird unterhalb der unteren Schwelle scharf und erfasst beim folgenden Überschreiten der oberen Schwelle. Ohne Übergang erfolgt die Erfassung automatisch. Das Graph-Kontextmenü blendet die Schwellenlinien ein/aus. Polyphone Stimmen werden vor der Messung summiert; Mix = (L+R)/2. Vertikaler Bereich r zeigt −r–r und verändert keine Signalwerte.

Das Graph-Kontextmenü bietet Anhalten/Fortsetzen und die vergrößerte Ansicht; auch Doppelklick vergrößert. Dort gibt es Mausradzoom, vertikalen Zoom mit Umschalt+Mausrad, Verschieben mit mittlerer Maustaste und zwei ziehbare Messcursor. Kontextmenüs löschen Cursor einzeln oder setzen die Ansicht zurück. Der Export bietet ein Bild des gesamten Graphen oder des sichtbaren Ausschnitts sowie eine CSV-Tabelle. Anhalten hält die Anzeige, nicht die Audioverarbeitung.

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

[Node-Verzeichnis](index.md) · [English](../en/oscilloscope.md)
