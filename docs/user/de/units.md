# Einheiten

Eine Einheit legt fest, wie ein Zahlenwert dargestellt und interpretiert wird.
Sie ändert weder den [Datentyp](data-types.md) noch die [Rate](rates.md).

## Einheit wählen

Öffne an einem unterstützten numerischen Port oder seinem Label das Kontextmenü
**Einheit (Unit)**. Dies funktioniert auch bei verbundenen Eingängen mit
ausgeblendetem Regler. Das Regler-Kontextmenü und das Port-Kontextmenü ändern
dieselbe Einstellung. Angeboten werden nur die für diesen Parameter erlaubten
Einheiten. Die gewählte Einheit erscheint hinter der Zahl beziehungsweise im
Port-Label.

Eine andere Anzeigeeinheit eines manuell eingestellten Parameters stellt
denselben physikalischen Wert umgerechnet dar, beispielsweise 1 s als 1.000 ms.

## Verbundene Werte

Eine Verbindung transportiert Zahlen **ohne automatische Einheitenanpassung**.
Die Ausgangseinheit bestimmt die ausgegebene Zahl; die Eingangseinheit bestimmt,
wie der Empfänger diese Zahl interpretiert.

| Ausgang | Eingang | Interpretation |
| --- | --- | --- |
| 1 s | s | 1 s |
| 1 s | ms | 1 ms |
| 1.000 ms | ms | 1.000 ms = 1 s |

Zum Erhalten der physikalischen Größe wählst du am Eingang dieselbe Einheit wie
am Ausgang oder setzt einen **Unit Converter** dazwischen. Eine andere Einheit
nur am Eingang zu wählen ist eine gezielte Neuinterpretation des Signals.

## Einheitengruppen

| Größe | Einheiten | Beziehung |
| --- | --- | --- |
| Frequenz | Hz, kHz, BPM | 1 kHz = 1.000 Hz; 60 BPM = 1 Hz |
| Zeit | s, ms, smp | 1 s = 1.000 ms; bei 48.000 Hz: 1 s = 48.000 smp |
| Winkel | turn, °, rad | 1 turn = 360° = 2π rad |
| Länge | m, km, cm, mm | 1 m = 100 cm = 1.000 mm |
| Amplitudenverhältnis | Faktor, %, dB | Faktor 1 = 100 % = 0 dB; dB = 20 · log10(Faktor) für positive Faktoren |
| Tonhöhenverhältnis | Faktor, cent, st, oct | Faktor 2 = 1 oct = 12 st = 1.200 cent |
| Filtergüte | Q | Dimensionslose Güte, kein Frequenzwert |

Faktor 0 bedeutet beim Amplitudenverhältnis Stille; eine endliche dB-Zahl ist
dafür keine exakte mathematische Entsprechung. Nicht jeder Parameter bietet
sämtliche Einheiten einer Gruppe an.

Der Unit Converter kann zusätzlich Frequenz und Periodendauer ineinander
umrechnen: **T = 1 / f**, beispielsweise 10 Hz = 0,1 s = 100 ms. Das ist eine
Berechnung und keine automatische Anpassung beim Verbinden.

## Gruppen und Vektoren

Gruppeneingänge und -ausgänge können eigene Einheiten anbieten. Die Gruppe
rechnet an ihrer Grenze in die intern benötigte Einheit um. Die äußere Leitung
behält trotzdem die oben beschriebene Zahleninterpretation.

Bei Vektoren gehören Einheiten zu den Komponenten beziehungsweise zur gewählten
Vektorgröße. Verwende dafür die vorgesehenen Vektor-Editoren und -Nodes, nicht
die skalare Portumrechnung als Ersatz für eine Vektortransformation.

[Grundlagen](index.md) · [English](../en/units.md)
