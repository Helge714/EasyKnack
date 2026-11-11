# Documentation consolidation

The workflow manual has twelve paired German/English chapters under
`docs/user/<language>/index.md`. It covers operation, groups and special groups,
spectral synthesis, analysis, channels, MIDI and buffers. Legacy workflow paths
remain short forwarding pages with their old heading anchors.

## Detailed reference remains separate

Node references retain their deliberately detailed eight-column input tables,
five-column output tables and separate Mute, Insertion and Delayable tables.
Their shared rate, type, unit, muting and table-legend pages remain substantive
references, not forwarding pages. Handbook cleanup must not shorten these tables.

The initial consolidation commit ca7c835 incorrectly condensed these references.
The follow-up restores all 214 cataloged language pages to their pre-consolidation
contents, including node-specific prose, and reinstates the strict table checks.
The twelve handbook chapters, independent language selection, Instance Group
naming and legacy workflow navigation remain.

Earlier FILTER, Level-Analysis, Step-Switch, PORT-UNITS, OSCILLATOR-RESET and old
node tables remain archived under `history/node-reference-migration/`.
Developer guides and historical audits are separate from user navigation.

Follow [documentation maintenance](NODE-DOCUMENTATION.md). Keep workflow prose
concise while maintaining complete manually authored technical reference tables.
This correction changes no document format, plugin ABI, audio protocol, product
version or GLSwing dependency; the existing first-development version covers it.
