# Documentation consolidation

The user manual now has twelve paired German/English chapters, owned by
`docs/user/<language>/index.md`. Shared definitions are explained once; individual
node pages retain compact authored controls, outputs, behavior and routing.
Technical developer guides and historical audits are not part of user navigation.

## Completed 2026-11-11

- Replaced scattered workflow guides with twelve concise chapters per language.
- Condensed all cataloged node references from eight-column input tables and
  separate routing sections to three-/two-column tables and one routing summary.
  Existing technical values and special behavior were retained, not generated.
- Replaced legacy user guides and shared term pages with short forwarding pages;
  preserved their old heading/term anchors for incoming links.
- Kept stable catalog IDs, filenames and saved document contracts. Standardized
  visible English naming on Instance Group; German uses Instanzgruppe.
- Help opens the selected-language manual directly. The former English-only
  handbook remains a compatibility entry point rather than a second manual.

Earlier FILTER, Level-Analysis, Step-Switch, PORT-UNITS, OSCILLATOR-RESET and old
node tables remain archived under `history/node-reference-migration/`.
Those are historical developer sources, not maintained public alternatives.

## Maintenance

Follow [documentation maintenance](NODE-DOCUMENTATION.md). Add concepts to the
owning chapter, node exceptions to its paired short reference, and implementation
history to developer documentation. Preserve useful legacy links with forwarding
pages rather than keeping duplicate instructions.

## Validation

All 214 cataloged language references retain their original input/output names.
Reference Markdown is 49.5% shorter; rendered-text word count is 32.2% lower.
Each language manual has approximately 3,000 words across twelve chapters.
The preview checks all user-facing legacy forwarding pages as well as current
chapters and references. Targeted native documentation tests cover language,
navigation, rendering, bundled coverage and both chapter directories.
The existing first-development product version covers this documentation/UI
change; no document format, plugin ABI, audio protocol or Core version changes.
