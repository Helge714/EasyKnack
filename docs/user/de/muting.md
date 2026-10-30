# Mute

Mute verändert den wirksamen Signalpfad eines Nodes. Es bedeutet nicht bei jedem
Node, dessen Ausgang auf null zu setzen. Die jeweilige Node-Referenz nennt unter
**Mute** die unterstützte Funktion und die Durchreicheverdrahtung.

## Durchreichen oder ignorieren

| Fall | Wirkung |
| --- | --- |
| Mutable: Nein | Der Node unterstützt Mute nicht. |
| Mutable: Ja, Eingang und Ausgang angegeben | Das am angegebenen Eingang angeschlossene Signal wird zum Ausgang durchgereicht; die eigentliche Verarbeitung entfällt. |
| Mutable: Ja, Eingang und Ausgang jeweils — | Es gibt kein Durchreichesignal; ausgehende Verbindungen werden ignoriert. |
| Durchreicheingang nicht verbunden | Ohne angeschlossene Quelle entsteht kein Durchreichesignal. Ein bloßer Editor-Standardwert ersetzt diese Quelle nicht. |

Ein gemuteter Math-Node kann beispielsweise seinen festgelegten Eingang
durchreichen. Ein gemuteter Oszillator erzeugt dagegen kein Ersatzsignal.
Gruppen können eigene Durchreicheports festlegen. Bei mehreren möglichen
Eingängen gilt die in der Node-Referenz angegebene Auswahl- oder Ersatzregel.

## Verbindungen und Editoren

Die Leitungen bleiben im Dokument erhalten. Ignorierte Verbindungen werden
abgeschwächt dargestellt. Das betrifft auch Eingänge eines gemuteten Nodes,
die nicht für eine wirksame Durchreicheverbindung verwendet werden.

Hat ein nachfolgender Eingang durch Mute keine wirksame Quelle mehr, kann sein
Editor wieder erscheinen und seinen eigenen Wert liefern. Ein benötigter
Ressourceneingang ohne Editor erhält dadurch keine Ersatzressource; seine
Anforderungen gelten weiterhin.

## Nullsignal und Geräte

Ein **verbundenes Signal mit Wert 0** bleibt eine wirksame Verbindung. Sein
Empfänger verwendet daher nicht stattdessen den Editorwert. Das ist etwas
anderes als eine ignorierte Leitung.

Audio Input ist ein besonderer Fall: Beim Muten liefert er ein verbundenes
Nullsignal. Audio Output gibt beim Muten kein Graphsignal aus. Das schaltet
das Windows-Gerät selbst nicht stumm und verändert nicht dessen Lautstärke.

Mute ist weder Löschen noch Ausblenden. Es ist auch keine Zusage, dass beim
Umschalten ein Crossfade stattfindet.

[Grundlagen](index.md) · [English](../en/muting.md)
