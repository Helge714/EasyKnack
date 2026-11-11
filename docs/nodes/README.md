# Node references

Start with the [English index](en/index.md) or [German index](de/index.md).
Each node has one maintained Markdown page per language. The references include
inspector-only settings in their Inputs tables, marked **Inspector / Inspektor**.

Run `python scripts/build-docs.py` from the repository root to render and validate
the HTML preview under `target/docs/`. Open `target/docs/nodes/en/index.html` or
`target/docs/nodes/de/index.html`. Authored content belongs here, never in `target/`.

## Identity catalog

`catalog.json` is a manually maintained navigation and coverage inventory, schema 1:

- `nodes`: stable `id`, filename `slug`, navigation `category`, paired `title`,
  owning `source` locator and `kind` (`node`, `context` or `group`).
- `excluded`: registered internal/legacy node `id` and an explicit English `reason`.

Group concepts use documentation identities `group:ordinary`, `group:instance`,
`group:spectral` and `group:formula`; these are not runtime registration IDs.
Bin Context and Bin Output have references although the application creates them
inside Spectral Groups. Compiler-generated boundaries and superseded legacy
processors are deliberately excluded, rather than advertised as usable nodes.

The preview builder audits registration IDs without executing the application.
It rejects undocumented registrations, exclusions of public nodes, duplicate
IDs/slugs, missing translations/index links, wrong section sequences and broken
local links or anchors. Technical tables and explanations remain manual; changing
a registration does not regenerate them or prove their accuracy.

Follow the [documentation work order](../development/NODE-DOCUMENTATION.md) when
adding or updating references. Update both languages with relevant node changes.
Maven bundles the maintained Markdown and catalog. Runtime **Help → Node reference**
uses the active node; **Node index** always opens the directory. Node-context
**Documentation** resolves the clicked node. Both resolve the independently selected documentation language (English
fallback), then show a native GLSwing dialog with tables, links and browsing
history. Each selected page is read and parsed in the background; no browser or
HTML-file export is needed to read it. Installed help requires no Python,
checkout or host. The portable HTML exporter remains available to tooling/tests.

Incremental development runs also read current files from the verified checkout
anchored to the application's `target/classes` code location. They do not depend
on copied Maven resources surviving an IDE clean. Packaged runs use only bundled
resources; the process working directory is never used to locate documentation.

`NodeDocumentation` reads adjacent `id`/`slug` pairs; keep these fields together.
Coverage tests resolve every public built-in identity and both translations; export
tests check all emitted links and section anchors. Plugin documentation discovery
remains separate work; unknown references produce a disabled action. Archives and
earlier additive oscillator drafts are excluded from runtime help.
