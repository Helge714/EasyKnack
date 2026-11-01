# Documentation consolidation

The bilingual references under `docs/nodes/` own node ports, settings, ranges,
units, rates and routing. Shared semantics live under `docs/user/en/` and `de/`.
Older pages are reviewed by content, not deleted just because a matching node
reference exists. Original texts replaced below are preserved under
`history/node-reference-migration/`; their historical links are not maintained.

## Replaced references

| Previous page | Current source | Disposition |
| --- | --- | --- |
| `FILTER.md` | `nodes/en/filter.md`, `nodes/de/filter.md` | Archive original; retain forwarding page. Preserve compensation timing and feedback implications in both new pages. |
| `Level-Analysis.md` | `nodes/en/level-detector.md`, `nodes/de/level-detector.md` | Archive original; retain forwarding page. |
| `Step-Switch.md` | `nodes/en/step-switch.md`, `nodes/de/step-switch.md` | Archive original; retain forwarding page. Previous transient-selection and numeric-only descriptions are obsolete. |
| `PORT-UNITS.md` | `user/en/units.md`, `user/de/units.md` | Archive original; retain forwarding page. Group-boundary and vector rules are in the shared reference. |
| `OSCILLATOR-RESET.md` | Oscillator and Additive Oscillator references | Archive implementation/test history; retain forwarding page. |
| `user/nodes.md` | Bilingual node indices and shared fundamentals | Archive duplicated node tables; keep a compact navigation page with its previous section anchors. |

## Retained guides and technical material

| Pages | Why retained |
| --- | --- |
| `user/*.md` except the replaced `nodes.md` tables | Task-based handbook: workspace, devices, groups, drivers, interpolation, recovery and limitations. Node index is linked instead of replacing these workflows with tables. |
| `Analysis-Inspection.md` | Measurement cursors, export schemas, freeze and scientific interpretation beyond individual node settings. |
| `FFT.md`, `SPECTRUM-ANALYSIS.md`, `SPECTRAL-GROUPS.md`, `GROUPS-SPECTRAL.md`, `INSTANCE-SPECTRAL.md`, `MULTIPLE-GROUPS.md` | Cross-node execution/timing, group examples and technical history; partially superseded parameter lists are not promoted to current node references. Consolidate these into a spectral workflow guide separately. |
| `FORMULA-GROUP.md`, `SPECTRAL-FORMULA.md` | Language syntax and per-bin formula examples; the Formula Group page cannot replace a language manual. |
| `WAVEFORMS.md` | Drawing and FFT-capture workflows. |
| `Audio-File-Player.md` | File region editing, zero snapping, audition and Buffer Reader workflows. |
| `TRIGGER-COUNTER.md`, `TRIGGER-DELAY.md`, `TRIGGER-TIMER.md` | Multi-node trigger/feedback/tempo examples. Parameter definitions belong in the new references; examples need later tutorial consolidation. |
| `SPECTRAL-HOLD.md`, `SPECTRUM-BLUR.md`, `SPECTRAL-PERFORMANCE.md`, `SAMPLE-FEEDBACK.md`, `SPATIAL-AUDIO.md`, `HARMONIZER.md` | Multi-node constructions, spectral synthesis/timing and performance guidance that is not duplicated by one node page. |
| Build, distribution, license, DSP architecture and development documents | Developer or release responsibilities; not replaced by user node references. |

Runtime help exports only Markdown reachable from the handbook and bilingual node
indices. It never follows links into `development/`, source files, host filesystem
paths or network URLs. Archives and comparison drafts are not runtime help pages.
Legacy forwarding paths remain valid so existing references do not become dead links.
