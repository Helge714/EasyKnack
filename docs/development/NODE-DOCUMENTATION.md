# Node documentation work order

## Scope and source of truth

Write one complete, manually maintained Markdown reference per node and language.
All content is authored, including technical tables; do not generate selected
cells from Java definitions or infer documentation from C++ code. Check the Java
definition and native behavior while writing, but do not add metadata to processor
contracts solely to generate documentation.

Automate presentation and checks only: Markdown-to-HTML conversion, packaging,
navigation, language selection, file coverage and link validation. The same
complete pages must work offline, in the application and outside it, without
application-only placeholders or a running audio host.

The approved layout pilot is
[the third German additive oscillator draft](../nodes/de/additive-oscillator-v3.md).
Earlier drafts are comparison material, not templates. The pilot currently has
only a German page. Bilingual coverage and production integration remain pending.

Shared foundations now live in [English](../user/en/index.md) and
[German](../user/de/index.md): rates, data types/channels, units and muting.
Link directly on the relevant table terms (Audio, Control, Auto, resource types,
unit symbols, Mutable) and column headings (Data type, Rate, Unit, Channel, etc.),
rather than inserting link lists below section headings. Use the shared reference
table legend for editor/signal ranges, defaults, visibility and port mappings.
Use the shared pages' stable term anchors, identical in both languages. Rate Auto
links to rate selection; channel Auto links to channels. Do not repeat their
explanations. Maintain both language versions and existing anchors when shared
behavior changes.

## Sources, HTML preview and gradual replacement

- `docs/` contains the authoritative, version-controlled Markdown. Start at
  [the documentation index](../README.md). Node references and shared foundations
  live beside the existing guides; do not move authored content into `target/`.
- `scripts/build-docs.py` and `scripts/documentation_renderer.py` reproduce the
  current HTML preview using Python 3.10+ and its standard library:

  ```text
  python scripts/build-docs.py
  ```

  Output defaults to `target/docs/`. `--output <directory>` selects another folder;
  `--page docs/<page>.md` adds a page to the current preview set. HTML pages are
  derived files, not another manually maintained source. Do not edit or commit
  generated output. It may be removed by a clean build and regenerated.
- The preview supports the Markdown subset used by the current reference pages
  and checks local links, generated section targets and paired foundation pages.
  It currently includes the bilingual foundations and the latest German node
  pilot. Links to unconverted legacy pages open their checkout Markdown files.
  This preview is not yet the application's help integration or a portable
  published documentation bundle; those remain separate implementation work.
- Replace legacy pages individually after their replacement covers the relevant
  user-facing content, both language versions are reviewed, and incoming links
  and any packaging references are updated. Retain old pages until then. Move
  debugging/history material to developer history rather than copying it into
  the new user reference. Do not bulk-delete old documentation.
- Consolidate pilot drafts into one canonical page per language when approved;
  keep comparison drafts out of the eventual public node catalog. This cleanup
  has not been performed merely by introducing the preview build.

## Files, identity and languages

- Use `docs/nodes/en/<stable-slug>.md` and `docs/nodes/de/<stable-slug>.md`.
  Keep the same slug in both languages, independent of translated display names.
- Associate each reference with the stable node registration ID when implementing
  the documentation catalog. Renaming a node or translating its title must not
  break the association. Do not use a user's instance title as identity.
- English titles and port names contain only the English name. German titles and
  port names use **German name (English name)**.
- Maintain equivalent technical content in both languages. When ports, controls,
  defaults, ranges, units, rates, visibility, routing or behavior change, update
  both references in the same commit. Treat an outdated translation as unfinished
  work, rather than silently leaving contradictory pages.
- Keep user documentation concise, professional and independent of Eclipse.
  Exclude implementation history, migration/debugging narratives and explanations
  of why a previous design was replaced. Put developer material elsewhere.

## Required page structure

Use these sections in this order. Do not add Settings, Example, Notes or Related
documentation as standard sections.

### 1. Overview / Übersicht

One short sentence stating the node's purpose. For the additive oscillator:
“Der **Additive Oscillator** erzeugt einen Klang aus bis zu 144 Teiltönen.”
Keep calculations, special cases and parameter explanations out of the overview.

### 2. Inputs / Eingänge

Use exactly these columns, in this order:

```text
EN: Name | Data type | Editor range | Signal range | Default | Unit | Rate | Visibility
DE: Bezeichnung | Datentyp | Reglerbereich | Signalbereich | Standardwert | Einheit | Rate | Sichtbarkeit
```

- Do not add a description column or an introductory paragraph about unconnected
  inputs. Default editor behavior and rate fundamentals belong in shared guides.
- Separate the manually editable range from the effective connected-signal range.
  Inspect native clamping, rounding and wrapping; never assume slider bounds also
  constrain a connected signal. Include brief qualifiers such as “abgerundet”.
- Put resource types in Data type, e.g. **Wavetable-Ressource**. Put the available
  combobox choices in Editor range. Use **Audio** for audio, not “Audiosignal”.
- Include units with numeric ranges and defaults. Name the unit-choice column
  **Unit / Einheit**, without “allowed” or “with unit” in any heading.
- Use compact intervals: **0–4**, **1–144**, **0,00001–20.000 Hz** in German;
  **±1** and **±∞ turn** for symmetric ranges. Do not write “von … bis …”, square
  brackets or “beliebiger Phasenversatz”. `±∞` denotes any finite real value;
  it does not promise that infinity or NaN is an accepted signal.
- Use **Auto** when an input automatically follows Audio/Control, **Audio** or
  **Control** when fixed, and **Trigger** for trigger timing. Auto is a policy,
  not a third numeric rate. Do not infer it merely from a color or port shape.
  MIDI remains a separate message type; “samplegenau” is not another rate label.
- Keep visibility conditions short: **Immer**, **Teiltöne > 1 oder Teiltöne
  verbunden**, etc. Include relevant inspector-only controls in the reference;
  mark editor-only parameters with no connected range/rate as **—**, rather than
  inventing a port or omitting an option because there is no Settings section.
- Put inspector-only settings in the same Inputs table, with **Inspector / Inspektor**
  in Visibility. If conditional, retain that location alongside the short condition.
  Do not invent a separate Settings section or omit controls without sockets.
- Use **—** for a missing or inapplicable value, not an invented numeric zero.

### 3. Outputs / Ausgänge

```text
EN: Name | Data type | Range | Rate | Channel
DE: Bezeichnung | Datentyp | Wertebereich | Rate | Kanal
```

Use the same naming, type, range and rate conventions as Inputs. For a usual
normalized audio output, write **Audio**, **±1**, **Audio**; do not expand the range
cell into a paragraph about custom waveforms. Use **Auto** for channel layout
derived from the inputs/runtime; use **Left / Links**, **Right / Rechts**,
**Mono** or **Stereo** only when that is a fixed property. Use **—** when channels
do not apply. Do not add a paragraph explaining general stereo behavior.

### 4. Behavior / Verhalten

Describe only node-specific behavior. Distinguish materially different modes with
subheadings. For the additive oscillator:

- **Partials = 1 / Teiltöne = 1:** refer to the ordinary oscillator; do not repeat
  its full documentation.
- **Partials > 1 / Teiltöne > 1:** give one complete equation including summation,
  normalization, phase and the relevant frequency cutoff. Define its symbols
  briefly. Explain Stretch and Falloff concisely, and refer to the ordinary
  oscillator for shared functions. State when a formula is a steady-state model.

Avoid self-evident deductions, long parameter walkthroughs and obligatory examples.
Link to shared guides where a node is not self-explanatory, particularly Spectral
Group/bin processing. Place those links at the relevant explanation, not in a
standard trailing links section. Do not create broken placeholder links to pages
that do not exist yet. Keep equations readable in Markdown and exported HTML.

### 5. Mute

Use a table, not a prose description:

```text
EN: Mutable | Input | Output
DE: Mutable | Eingang | Ausgang
```

Write **Yes / Ja** or **No / Nein** and the exact bypass port names. Use one row
per mapping; use **—** for absent mappings. A mutable generator with no bypass
therefore reads **Ja | — | —**. Check actual mute routing, including any fallback
selection rule; state a necessary conditional mapping briefly in the table.

### 6. Insertion / Einfügen

```text
EN: Input | Output
DE: Eingang | Ausgang
```

List the exact ports used for insertion into an existing connection, one pair per
row. Use **— | —** if unsupported. Include a concise conditional/fallback rule in
the relevant cell when routing depends on connections or configuration.

### 7. Delayable

Use a one-column table **Sample-wise feedback / Sampleweises Feedback**, containing
**Supported / Unterstützt** or **Not supported / Nicht unterstützt** according to
the actual sample-feedback capability. Do not replace this with a generic Notes
section or imply that support itself makes an undelayed feedback loop valid.

## Integration work still to implement

- Apply this reference format to built-in nodes in both languages. Promote one
  canonical additive oscillator page per language after review; comparison drafts
  must not become duplicate entries in the public catalog.
- Add a node-context-menu documentation action, resolving the stable node ID and
  current language. Fall back to English when a translation is missing. Do not
  display unavailable documentation as if it existed.
- Render readable HTML with tables, equations, internal navigation, cross-links
  and scalable text. Package the pages offline; allow the same output to be read
  in a browser or published as a static site. Keep relative links portable.
- Allow plugins to ship references by node ID and language using the same format.
  Missing documentation must not prevent installation; disable the documentation
  action when no page exists. Define manifest paths and safe local resource
  resolution as part of that implementation, rather than assuming an existing API.
- Add coverage and link checks for built-in pages and plugin documentation
  validation. These checks do not prove that prose matches DSP behavior; review
  both language versions against the implementation during relevant code changes.

## Completion check for each reference

Check completeness of ports and controls; exact defaults, units and both ranges;
actual rate/channel policies; conditional visibility; Mute and insertion mappings;
sample-feedback support; formula accuracy; matching translations; existing links;
and readable HTML tables. Do not run a Java/native rebuild for prose-only edits.
