# Bugs
- [x] Wenn man einen Step Switch innerhalb einer Gruppe von Trigger auf Index umstellt, verschwindet er, bis man die Gruppe verlässt und neu öffnet.
- [x] manchmal funktioniert undo nicht mehr, ohne Fehlermeldung
- [x] wenn man Einträge in einer custom-Combobox ändert, erscheinen diese Änderungen erst im Node, wenn man die Combobox entfernt und dann wieder hinzufügt
- [x] Gruppen sollten im Add-Menu ebenfalls ihr Gruppensymbol bekommen
- [x] Das Addmenu hält jetzt Platz frei im Hauptmenu für Icons, ob wohl die Einträge gar keine Icons besitzen
- [x] Die Gruppenicons im Addmenu sind viel Dicker gezeichnet als bei den Gruppen selbst, bitte angleichen.
- [x] Undo wird gerade über das komplette Dokument aufgezeichnet oder? Sinnvoller wären eigene Manager für das Hauptfenster und für jede Gruppe. Man undot also außerhalb der Gruppe nicht, was in der Gruppe angelegt wurde. Was meinst du?
- [x] Einfügen von Nodes zwischen Gruppen-Ein-und Ausgängen und Gruppeninternen Nodes funktioniert nicht, Verbindungen veschwinden dann einfach
- [x] Copy Editor verbindet automatisch den erstbesten Node-Eingang, wenn kein kopierbarer Editor verfügbar ist, das sollte nicht so sein, besser copyEditor ausblenden, wenn es keinen kopierbaren gibt und der Port nicht verbunden ist. Eingänge von Nodes erscheinen außerdem automatisch in der Auswahl, auch wenn sie Ports besitzen, das sollte nicht gehen, diese Verbindungen werden ja physisch hergestellt.
- [x] Tooltips bleiben manchmal noch stehen, obwohl ihr Host längst nicht mehr sichtbar ist oder die Maus zumindest nicht mehr über ihm hovert
- [x] die Icons zum Verschieben der Verbindungen im Step-Switch werden nicht gezeichnet. Außerdem sollte das ganze viel näher an den Valueviewer-node angepasst sein, also eine Zeile pro Eingang mit Name, Wert und Buttons für hoch/runter und das ganze als eine Core-ControlGruppe.

- [x] Inspector: N outside text input and Esc from sidebar controls/text editors work reliably.

# Nice to have
- [x] Step Switch sollte die Eingänge verschiebbar haben über den Inspector, ähnlich wie der Value-Viewer
- [x] Namensänderungen sollten resetable sein.
- [x] Tooltips von Controls sollten immer auch den Namen des Controls anzeigen
- [x] Gruppen sollten in den Dokumenteneinstellungen als Liste mit derzeitiger Verwendungsanzahl auftauchen und löschbar sein (wenn nicht verwendet), vllt über einen Extra-Dialog wie "Gruppen verwalten" und dann vllt zwei Listen nebeneinander, links die Gruppen mit Verwendungsanzahl, rechts wo eine Gruppe überall eingebunden ist. Gruppen sollten sich nicht löschen lassen, wenn sie sichtbar sind oder Teil einer anderen noch nicht gelöschten oder sichtbaren Gruppe sind. Macht das Sinn?
- [x] beim Import von Gruppen aus einer Datei sollten wir eine Liste bekommen, sodass wir gleich mehrere Gruppen für den Import auswählen können

# EasyKnack backlog

Current work only. Historical implementation notes, superseded plans and original
bug reports are preserved in [the October archive](docs/development/history/TODO-2026-10.md).

## Current stabilization pass

- [x] Extend Step Switch with Vector, Wavetable, Buffer, Spectrum and Analysis
  routing, block-aligned resource selection, editor-free step controls and
  Instance/Bake/export support. Preserve the previous numeric source contract.

- [x] Support Step Switch in nested Instance Groups and Bake/export, with
  independent selection state and one representative UI telemetry publisher.

- [x] Preserve direct group forwarding, copied editor identity and custom viewer
  sizes across outer parameter edits, navigation and Undo/Redo (regression tests).
- [x] Reuse a local Bake for equivalent instances of one group definition, including
  Multiple Groups; keep processor state independent for each instance. Rate/unit
  specializations remain distinct. Internal edits invalidate the affected Bakes.
- [x] Attach MIDI inspector indicators when controls enter the inspector hierarchy.
- [x] Add Step Switch index selection and editable input names in the inspector.
- [x] Replace Complex Oscillator cent spreading with power-law partial stretching
  (exponent 0–4, neutral 1); verify all 144 ratios and one-time scalar migration.
  Review old modulation, inherited group editors and generated exports manually.
- [x] Finish review, validation and focused commits for accumulated Core and
  EasyKnack source changes. Keep machine-specific Eclipse settings local.
- [x] Share one Spectral Group bin plan across multiple complex Spectrum outputs;
  support typed Spectrum outputs through Instance Groups, stereo Sum/Mean and
  Bake/export. Verify complementary reconstruction and mixed audio/resource outputs.
- [x] Remove the fixed eight-output execution/export limit; prepare dynamic output
  and spectral/instance work storage. Verify 24-port audio/control/event/resource
  routing without callback allocations and 12-port Spectrum/numeric groups.
- [x] Replace fixed Instance/Spectral input fields with counted source lists;
  preserve legacy contracts and update native buffers, rate plans and Bake/export.
  Verify 20 numeric, 12 Spectrum and 24 frame inputs including high-slot triggers.
- [ ] Manual acceptance: nested group Undo/Redo and directly forwarded ports;
  independently copied FFT window editors; repeated inspector selection; duplicate
  ordinary/Multiple Group Bakes; Step Switch mode changes and named inputs.

## Next technical work

- [ ] Add optional Formula Group oversampling (Off/default, 2x, 4x, 8x) as a host
  execution feature: filtered upsampling, formula execution at the effective
  sample rate, anti-alias filtering and downsampling. Define sample/time/block
  context, trigger timing, signal/resource reads, state, stereo/voice handling
  and feedback restrictions; preserve behavior in preview, native execution and
  Bake/export. Report/compensate filter latency and verify alias suppression,
  pass-through gain and timing with repeatable tests. Keep effect-specific
  output gain/loudness compensation separate from oversampling.

- [x] Keep authored documentation under `docs/`; add a reproducible Python HTML
  preview build in `scripts/build-docs.py` with heading/term links and validation.
  Generated output goes to `target/docs/`. Replace legacy pages individually after
  bilingual content and incoming links/packaging have been reviewed.

- [x] Write the bilingual, manual built-in references following
  [NODE-DOCUMENTATION.md](docs/development/NODE-DOCUMENTATION.md), including
  inspector settings, groups and spectral context nodes. Validate catalog,
  paired pages, section structure, index coverage, links and anchors.
- [x] Bundle built-in references and shared foundations; open localized native
  help from node/group context menus and Help. Verify portable HTML links, anchors,
  escaping and public-node coverage. Consolidate six replaced legacy references,
  keeping forwarding paths and archived originals; retain workflow guides.
- [ ] Add plugin documentation discovery/manifest paths and validation. Retain
  manual tables; consolidate overlapping spectral workflow guides separately.
- [x] Preload parsed documentation and prepare its theme-dependent font atlases
  during visible splash steps. Reuse parsed pages while detecting source edits;
  keep each viewer's layout and navigation state independent.
- [ ] Replace the application-specific documentation drawing with a standardized,
  efficient Markdown viewer in GLSwing Core. Keep document lookup, node identities
  and language policy in EasyKnack. The Core backlog owns the reusable control.
- [ ] Add a persisted documentation-language preference independent of the UI:
  Same as application (default), English or Deutsch. Apply it consistently to node
  references, the index and help navigation, including reopening the viewer.
  Allow German documentation with an English interface; use English fallback for
  missing translations without changing either saved language preference.

- [x] Audit the optional native contract sweep: Frame Trigger requires spectral
  frame context. Validate all temporal contracts separately, reject bin-only nodes
  before host submission, and verify frame triggers in native spectral execution.

- [ ] Generate shared instance-processor capability declarations instead of keeping
  separate Java supported-node IDs and a native factory whitelist. Cover live
  execution, stereo/rates and Bake/export; document deliberate exclusions.
- [ ] Extend export contracts for coupled standalone sliders, checkboxes and trigger
  actions. Retain explicit export errors until each binding can be represented.
- [ ] Extend spectral-group boundaries and shared effects deliberately, with defined
  frame timing and resource ownership. Keep spectral execution a distinct domain.
- [ ] Support precompiled plugin installation as an optional future distribution
  format; preserve editable source-plugin packages and compatibility checks.
- [ ] Profile remaining mixed/stateful Bake kernels with repeatable benchmarks.
  Share compiled code without accidentally sharing per-instance signal state.

## Release readiness

- [ ] Test installation, upgrade, uninstallation, file association and recovery on a
  clean Windows machine without development tools. Test read-only install paths,
  external x64 JDK 25 discovery/installation assistance and compiler prerequisites.
- [ ] Complete exact-artifact license/source/notice verification before publication;
  use the current legal/distribution documentation, not archived FFmpeg plans.
- [ ] Perform a first-time-user walkthrough using only installed English user docs.
- [ ] Establish the first release baseline and official CI/publication workflow.
  Keep product versions in Maven and local build identities explicitly local.
- [ ] Bundled Temurin is optional future work, pending a complete redistribution
  inventory. Current distribution uses an external JDK.

## Later product work

- [ ] Audio recording and offline rendering; transport/tempo synchronization.
- [ ] Reusable group library, presets, automation and plugin management improvements.
- [ ] Dynamics, pitch tools, performance diagnostics and latency compensation.
- [ ] Long-period autocorrelation using a defined low-rate analysis path.
- [ ] Long-running physical-device/USB/Bluetooth routing acceptance tests.
- [ ] VST3 hosting/plugin integration, then JACK if needed. Own virtual-driver
  development remains deferred; do not require Secure Boot/test-signing changes.

The older [event plan](TODO_EVENT.txt) and [routing plan](TODOs_IO-Routing.txt)
are historical design references, not instructions to reintroduce obsolete public
rate semantics or to resume kernel-driver installation.

## Validation (7 November 2026)

- Core reactor `test`: 146 Core, 11 LSP and one SWT test; no failures/errors,
  two optional skips in total. Core Maven artifact installed locally.
- EasyKnack full `package`: 687 tests, no failures/errors, 170 optional/native
  skips. Six final focused UI tests also pass, including one additional direct
  Step Switch name/value editing and Undo regression.
- Group boundary insertion is checked after queued projection, navigation and
  Undo; graph-local history survives reopening. Inspector N/Esc dispatch is
  tested through actual control ancestry, with text-entry protection.
- Bilingual documentation: 226 HTML pages and 7623 local links/anchors checked.
  Step Switch control groups and geometric reorder icons rendered offscreen.
- Packaged Core consumer checked directly from a temporary fixture using the
  current artifact versions and DARK_BLUE theme. The stock invoker fixture still
  needs its old 0.1.0/DARK references and launcher setup maintained separately.
- Hardware audio and installer acceptance were not rerun for these UI-only changes.

## Earlier validation (30 October 2026)

- Core: full Maven test/install, 132 tests, no failures/errors, one optional skip.
- EasyKnack: full Maven package, 598 tests, no failures/errors, 158 optional tests
  skipped in the regular profile. Selected native checks were run separately.
- Additional native checks passed for Step Switch sample/trigger/index behavior,
  all-partial power stretching and legacy cent-contract conversion, generated Instance kernels, spectral hold/memory/formulas,
  filters, Blur synthesis/power, IFFT stability and unit conversions.
- A real transient plugin build reused its cache; Force rebuild produced a fresh
  host while the earlier binary remained usable. The development host was rebuilt.
- Multi-output native checks passed for complementary complex spectra, stereo,
  worker/Bake/export kernels, Instance Sum/Mean and mixed audio/Spectrum outputs.
- Dynamic-output checks passed for high source-port references, rejected invalid
  references, 12-port persistence/export and existing spectral voice/state behavior.
- Variable-input native checks passed for legacy group contracts, wide numeric and
  Spectrum boundaries, frame capture/export, asynchronous value history and existing
  Wavetable/choice exports. The generic event adapter handles 300 inputs without
  callback allocations. The rebuilt development host passed the contract sweep.
- Hardware, clean-machine installation and the manual acceptance list above remain
  separate checks; these results do not imply those tests were performed.
