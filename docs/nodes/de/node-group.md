# Nodegruppe (Node Group)
Stellt eine wiederverwendbare Schnittstelle zu einem internen Graphen bereit.
<a id="ubersicht"></a>
<a id="eingange"></a>
<a id="mute"></a>
<a id="einfugen"></a>
<a id="delayable"></a>

## Bedienung
| Parameter | Einstellung | Signal / Bedingung |
| --- | --- | --- |
| Öffentliche Eingänge (Public inputs) | Schnittstellenabhängig; Standard: Schnittstellenabhängig | Schnittstellenabhängig; Schnittstellenabhängig; Auto |
| Allgemein (General) | Name, Kategorie, Typ, Routing, Bake; Standard: Gruppendefiniert | Editor; Inspektor |
| IO | Ports, Editoren, Einheiten, Standardwerte, Raten; Standard: Gruppendefiniert | Editor; Inspektor |
| Editoren (Editors) | Editorvorlagen des Dokuments | Editor; Inspektor |

## Ausgänge
| Ausgang | Signal |
| --- | --- |
| Öffentliche Ausgänge (Public outputs) | Schnittstellenabhängig |

## Verhalten

[Gruppeneinstellungen und Copy Editor](../../user/de/groups.md) erklären Aufbau und Bedienung. Die Ports oben beschreiben eine konfigurierbare Schnittstelle, keine feste Beschränkung auf numerische Signale.

Bei Copy Editor an einem unverbundenen Eingang muss die Quelle ausdrücklich gewählt werden; die Auswahl erzeugt kein implizites Kabel. Kopierbare standalone Editoren verschachtelter Gruppen erscheinen als Quellen; Eingänge mit sichtbarem Socket werden per Kabel verbunden. Gekoppelte Originale sind deaktiviert, nach dem Entkoppeln wieder bedienbar.

Änderungen entfernen ungültige Verbindungen zu gelöschten Nodes, Ports oder Editor-Eigenschaften automatisch. Gültige Ziele bleiben erhalten; Undo stellt entfernte Nodes und Bindungen wieder her. Geänderte Comboboxvorlagen aktualisieren bestehende Gruppenregler. Jede geöffnete Gruppendefinition hat ihren eigenen Undo-Verlauf; Speichern betrifft das ganze Dokument.

## Routing
| Funktion | Verhalten |
| --- | --- |
| Mute | Gewählter Durchreicheingang (Configured bypass input) → Gewählter Durchreicheausgang (Configured bypass output) |
| Einfügen | Gewählter Einfügeeingang (Configured insertion input) → Gewählter Einfügeausgang (Configured insertion output) |

[Grundlagen und Mute](../../user/de/signals.md) · [Handbuch](../../user/de/index.md)

[Node-Verzeichnis](index.md) · [English](../en/node-group.md)
