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

- [x] GruppenInputs erscheinen noch nicht alle im Inspektor
- [x] Verbindung tauschen klappt noch nicht bei Verbindungen zu Gruppen-Eingängen/Ausgängen
- [x] Verbessertes Autolayouting ist noch ausbaufähig, Ziel muss sein, das ganze übersichtlich und möglichst kompackt anzuordnen. Mach mir dazu bitte Vorschläge — [Vorschlag](docs/development/AUTO-LAYOUT-PLAN.md); Vorschlag umgesetzt: Port-Ausrichtung, Feedback-Bereiche, kompakte Spalten und Skyline-Packing.
- [x] Doppelklick im Filechooser öffnet einen Ordner nur in der Seitenleiste, erst ein erneuter Doppelklick öffnet ihn im Hauptbereich. Die beiden sollten getrennt von einander sein, ein Doppelklick in der Seitenleiste öffnet den Ordner nur dort, ein Einfachklick im Seitenmenu öffnet den Ordner im Hauptbereich. Ein Doppelklick im Hauptbereich öffnet ihn nur dort.

# Nice to have
- [x] Step Switch sollte die Eingänge verschiebbar haben über den Inspector, ähnlich wie der Value-Viewer
- [x] Namensänderungen sollten resetable sein.
- [x] Tooltips von Controls sollten immer auch den Namen des Controls anzeigen
- [x] Gruppen sollten in den Dokumenteneinstellungen als Liste mit derzeitiger Verwendungsanzahl auftauchen und löschbar sein (wenn nicht verwendet), vllt über einen Extra-Dialog wie "Gruppen verwalten" und dann vllt zwei Listen nebeneinander, links die Gruppen mit Verwendungsanzahl, rechts wo eine Gruppe überall eingebunden ist. Gruppen sollten sich nicht löschen lassen, wenn sie sichtbar sind oder Teil einer anderen noch nicht gelöschten oder sichtbaren Gruppe sind. Macht das Sinn?
- [x] beim Import von Gruppen aus einer Datei sollten wir eine Liste bekommen, sodass wir gleich mehrere Gruppen für den Import auswählen könne

- [x] Die Cameraposition von Gruppen sollte gespeichert werden, damit man immer da landet, wo man zuletzt war, also jeder Graph bekommt eine eigene Kameraposition (Zoom wird global behandelt)
- [x] Strg + L sortiert schon gut, sorgt aber manchmal für seltsame Layouts, kannst du Nodes intelligenter anordnen, also zum Beispiel so, dass die Verbindungen möglichst kurz sind?
- [x] Wir brauchen einen Recorder-Output-Node, der per Start-Trigger eine Aufnahme beginnt und dann beim Stop-Trigger beendet und speichert. Mit der Option des Überschreibens, ansonsten wird ein [_nächster freier Index] angehangen, also zum Beispiel Testaufnahmen_0001.wav (die Anzahl der Ziffern gerne einstellbar, wenn ein Index diese Anzahl überschreitet, darf er das, also wenn wir zwei Ziffern festlegen, gibt es trozdem das Element 100 etc.). Unterstützt werden sollten mindestens WAV, gerne auch MP3 sowie Stereo und Mono (einstellbar)
- [x] Betsimmte Nodes sollten Verbindungen tauschen, wenn man etwas neu verbindet, z.B. Math-Node (auch Vector Math etc.): wenn A angeschlossen und B frei dann wird beim anschließen einer neuen Verbindung an A, die derzeitige Verbindung auf B geswitcht. Gleiches für die beiden Eingänge der Mix-Nodes. Bitte überlege, bei welchen das sonst noch Sinn machen könnte und wie man das gut im Code umsetzten könnte, vllt mit einer Funktion switchTo(Aktuelle Verbindung, neue Verbindung) mit return des neuen NodePoints (natürlich nur wenn dieser frei ist, das könnte intern danach geprüft werden) oder null, wenn nicht geswitcht werden kann, dann wird die Verbindung wie bisher einfach ersetzt

- [x] Import von Gruppen sollte eine alle Auswählen-Möglichkeit haben. Den Ganzen Graphen als Gruppe zu importieren sollte besser von den Gruppen getrennt sein. Die NodeGruppen sollten in den gleichen Katheogorien eingordnet werden, wie in ihrer Hauptdatei
- [x] Index digits sollte eine reine Inspector-Einstellung sein
- [x] MP3-Unterstützung für den Recorder
- [x] Recorder braucht einen Savebutton im Inspector zum manuellen Speichern (nur aktiviert, wenn auch etwas aufgenommen wurde) — exportiert die letzte abgeschlossene Aufnahme zusätzlich per „Speichern unter“.

- [x] Autoformatierung scheint schon besser zu sein, es gibt aber die Tendenz aufsteigender Graphen, kannst du leicht verlängerte Verbindungen akzeptieren, wenn sie eine mehr horizontale Ausrichtung der Nodes erlaubt, also dass Eingang und Ausgang ungefähr auf einer Ebene liegen?
- [x] es scheint mehrere Example-Ordner im Projekt zu geben, außerdem ist target enorm groß angewachsen mit irgendwelchen Bakes, ist das nötig? — [Ordner und sichere Bereinigung](docs/development/BUILD-STORAGE.md); bestehende Dateien bleiben erhalten.
- [x] der Seed-Eingang bei Multiplegroup randoms wie partial und spectral group sollte einen Port bekommen


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

- [ ] Offline rendering; transport/tempo synchronization.
- [ ] Reusable group library, presets, automation and plugin management improvements.
- [ ] Dynamics, pitch tools, performance diagnostics and latency compensation.
- [ ] Long-period autocorrelation using a defined low-rate analysis path.
- [ ] Long-running physical-device/USB/Bluetooth routing acceptance tests.
- [ ] VST3 hosting/plugin integration, then JACK if needed. Own virtual-driver
  development remains deferred; do not require Secure Boot/test-signing changes.

The older [event plan](TODO_EVENT.txt) and [routing plan](TODOs_IO-Routing.txt)
are historical design references, not instructions to reintroduce obsolete public
rate semantics or to resume kernel-driver installation.

## Validation (8 November 2026)

- Compact layout pass: Core reactor test passes (162 Core, 11 LSP, one SWT;
  no failures/errors, two optional skips). Core commit `0e5e011` is installed.
  EasyKnack full package passes: 703 tests, no failures/errors, 173 optional skips.
- Twelve Core geometry/performance fixtures cover real anchor offsets, varied
  widths/heights, crossings, shared branches, overlapping feedback, direct and
  unconnected boundaries, 600 mixed-size disconnected nodes and a 1000-node chain.
  Application tests verify group roles, shortcut dispatch, unchanged sizes/graph/
  camera, persistence and one-step Undo, including repeat Arrange without new Undo.
- The rendered comparison fixture uses 992377 world-square units versus 1988766
  before, with Manhattan anchor distance 1081 versus 1671. These are fixture
  measurements, not a global optimality/performance guarantee. Docs validate:
  228 pages, 7691 links/anchors. DSP/host protocols and document schema are unchanged.

### Earlier recorder/import pass

- Core reactor test passes: 150 Core, 11 LSP and one SWT test, no failures/errors,
  two optional skips. That pass used Core commit `945b465`.
- EasyKnack full package passes: 700 tests, no failures/errors, 172 optional skips.
  Twelve final focused tests also pass with native audio checks enabled, including
  WAV/MP3 mono/stereo, Unicode paths, numbered file preservation, graph handover,
  unused-session telemetry and live-host recording/finalization.
- Group-boundary operand relocation survives queued projection and navigation.
  Import selection separates the root and definitions and preserves menu category.
  Save-as enablement/export, inspector-only digits and the actual inspector layout
  are checked; import and recorder inspector were rendered offscreen.
- The development host is rebuilt; the packaged SDK includes the final telemetry
  fix. Bilingual docs: 228 pages and 7691 links/anchors validated. MP3 uses the
  installed Windows media encoder and retains codec delay/padding; use WAV for
  sample-accurate recordings. Hardware routing and clean-machine installs remain
  manual checks. The compact layout proposal is implemented in the following pass.

## Earlier validation (7 November 2026)

- New group/recorder batch: Core reactor test passes (149 Core, 11 LSP, one SWT;
  two optional skips), and the committed Core artifact is installed locally.
  EasyKnack full package passes: 695 tests, no failures/errors, 172 optional skips.
  Fourteen additional focused tests include native WAV and live-host recording;
  the final multi-selection/host-service package checks pass as well (10 tests,
  two optional skips). The development audiohost is rebuilt.
- Inspector defaults, five group editor types, single-step multi-selection Undo,
  per-graph camera restoration and occupied-input rerouting are covered. Layout
  checks retain spacing, independent components, cycles and repeat stability.
  WAV tests cover sample boundaries, mono/stereo, voice folding, Unicode paths,
  exclusive numbered files, wider indices, graph handover and transport stop.
- Bilingual docs: 228 pages, 7689 links/anchors validated. Group inspector rendered
  offscreen. Hardware audio and clean-machine installation remain manual checks.
- Newly saved documents use Core schema 10; old documents remain readable.
  That recorder batch supported float32 WAV; the following pass adds Windows MP3.

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
