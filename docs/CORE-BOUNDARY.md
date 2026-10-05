# EasyKnack / GLSwing ownership audit

Developer architecture review and extraction status, 2026-10-05. This document
records implemented boundaries and remaining optional improvements. It is not
user documentation.

## Dependency rule

EasyKnack depends on the published/local-installed GLSwing Maven artifact.
GLSwing must never depend on EasyKnack classes, resources, DSP protocols, node
names or document migrations. A reusable mechanism belongs in Core; audio policy
and the adapter that supplies that policy belong in EasyKnack. Being graphical
does not by itself make a class a Core component.

## Reviewed responsibilities

| Area / current classes | Ownership and evidence | Action |
| --- | --- | --- |
| Graph editing, selection, navigation, undo and catalog | Core already provides `GLNodeWorkspace`, `GLNodeGraph`, `GLNodeCatalog` and composite infrastructure. | Keep application commands as adapters; do not create a second workspace implementation. |
| Groups and ports | Core owns `GLCompositeGraphRegistry`, interface/property transactions and port presentation. EasyKnack supplies signal categories, rate inference and DSP meanings. | Extend neutral Core descriptors when necessary; do not move audio-specific inference into Core. |
| `ExportDimensions` | Aspect lock, padding and raster bounds contain no application policy. | **Extracted** to Core `GLImageExportDimensions`; the local class is only a constructor alias. Existing behavior and regression coverage retained. |
| `GraphImageExport` | Offscreen rendering and PNG/JPEG encoding are generic; selection preparation instantiates an EasyKnack workspace and strips bake state. | **Extracted:** `GLComponentImageCapture` and `GLImageExport`. EasyKnack retains graph preparation, localization and dialog composition. |
| `ExportPreview` | Scaled image/checkerboard presentation is reusable; dialog sizing is application policy. | **Extracted:** `GLImagePreview`; application keeps a minimal dialog adapter. |
| `AnalysisImageExport`, `AnalysisFocusView`, analysis charts | FFT axes, frequency/bin values, snapshots, measurement cursors and audio capture are domain features. | Keep here; consume generic renderer/encoder if extracted. |
| `NodeTooltipLabel` | Reimplements measuring, truncation and text drawing to obtain a non-focusable full-area hover target and special framed padding. Core `GLLabel` already implements alignment, selection, themed text and ellipsis. | **Implemented:** Core `GLLabel` supplies full-area tooltip picking without focus. Local subclass only translates text and supplies framed-row insets. |
| `BasicNodeView`, `AudioNodeFactory` | Views bind audio nodes, trigger buttons, device volume, analysis and coupled group editors. | Keep domain binding here. Extract neutral row-layout/binding mechanisms only after defining a Core descriptor API. |
| `InspectorEditor`, `EditorPresentation` | Adapt EasyKnack editor definitions and units to Core controls/custom properties. `GLNodePropertyInspector` already suppresses updates during inspector synchronization. | Keep adapters. Consolidate generic layout/application of metadata where shared; preserve standalone synchronization safety. |
| `UnitSlider`, `UnitFormat` | Core already supplies `GLUnitSlider`, `GLUnit` and unit formats. EasyKnack supplies Hz/sample-rate conversion, pitch/gain pools and signal semantics. | Keep physical conversion policy and sample-rate context here; control rendering/editing belongs in Core. |
| `GroupInputSettings`, `GroupEditorCopies` | Mix generic editor choice/defaults with audio rates, unit pools, node structure changes and coupled native parameters. | **Implemented:** `GLNodeEditorCapabilities` and `GLNodeEditorSourceCatalog` own capability data and instance grouping. `GroupEditorCapabilities` adapts defaults, units and portable snapshots; audio-specific settings remain here. |
| `EditorTemplates` | Template identity and persistence can be generic; allowed editor types and signal categories are application-defined. | **Extracted:** `GLNodeEditorTemplate` and `GLNodeEditorTemplateLibrary`, with atomic snapshot validation. Application registration and existing serialized shape are retained. |
| `GroupImport` | Fresh graph IDs are generic, but imports also rewrite EasyKnack template/coupling metadata and group references. | **Extracted:** `GLNodeGraphAssetCopy` plans dependency closure and rewrites structural IDs. Policy callbacks handle application metadata, coupled editors and bake removal. |
| `NodeDocuments` | Already uses Core `GLDocumentManager` and codec registry. Remaining migrations and normalization know EasyKnack node contracts. | Keep adapter/migrations; do not duplicate Core document I/O. |
| `RememberingFileDialogs` | Last-folder persistence is reusable; preference namespace and translated-title identity belong to this app. | **Extracted:** `GLRememberingFileDialogs` with injected store and explicit purpose IDs. EasyKnack owns Preferences and migration of old title-based keys. |
| `AppText`, themes, menus and examples | Core owns language/theme/control mechanics. App owns translations, category order, example discovery and commands. | Keep app resources and command assembly here. |
| `NodeHints` | Audio/control/trigger/reference explanations express EasyKnack semantics. | Keep centralized here, not in the generic widget toolkit. |
| `WorkspaceController` | Coordinates app services as well as UI. Size alone does not imply Core ownership. | **Split:** `WorkspaceGroupBindings` and `WorkspaceGraphDiagnostics` now own inspector coupling and diagnostic presentation. Existing `DocumentActions` and audio services retain their responsibilities. |
| `formula`, `GroupCompiler`, `plugins` | Formula semantics, DSP code generation, bake/cache keys, parameter contracts and native compilation are audio-runtime concerns. | Keep here. Core may provide editor/language-service UI, never the audio language/compiler implementation. |
| `audio`, native host, node processors | Device routing, MIDI, rates, feedback scheduling, FFT, voices, sample state and performance telemetry. | Keep entirely in EasyKnack. |
| Packaging/build identity | Each product owns its version/build metadata; application runtime bundling is EasyKnack deployment. | Keep EXE assembly here. Do not require Core sources on end-user machines. |

## Implemented extraction sequence

1. Reused Core label rendering, including hover picking without selection/focus.
2. Moved component capture, image encoding/atomic replacement and preview rendering
   to Core. Repeated capture invalidates renderer-owned recordings; failure paths
   release the offscreen context. JPEG supports images with alpha via white backing.
3. Added a remembering-dialog decorator with explicit purpose keys, cancel/failure
   handling and correct directory-selection behavior. Existing saved folder keys
   are migrated by the application adapter.
4. Introduced neutral group-editor capabilities and a grouped source catalog.
   EasyKnack supplies editor eligibility, portable snapshots and audio policies.
5. Moved choice-template validation/storage and structural graph copying to Core.
   Import callbacks retain application metadata rewrites. Nested dependencies,
   fanout, property mappings, independent IDs and view positions are preserved.
6. Separated inspector coupling and cached diagnostic presentation from the
   application workspace controller. These services remain application-specific.

## Deliberately retained application code

The Group-IO panel is still an EasyKnack panel: its rate choices, unit conversion,
Formula identifiers, DSP coupling and rebuild requirements are audio policies.
Its generic data/discovery/copy mechanisms now come from Core. Moving the complete
panel would introduce EasyKnack semantics into GLSwing. Likewise, BasicNodeView
still binds audio devices, triggers and analysis controls; the shared label and
editor mechanisms it uses belong to Core.

Further decomposition of row binding or workspace audio compilation is optional
internal cleanup, not a missing dependency for packaging. The library has no
source or resource dependency on EasyKnack. EasyKnack still uses the installed
Maven artifact, not the Core checkout.

## Verification and existing test debt

The dedicated Core service tests include real OpenGL capture after render failure,
PNG alpha, JPEG, atomic file replacement, remembered directories, template loading,
source eligibility/grouping, nested graph copies and label selection/hover behavior.
Application integration tests cover nested imports, copied editors, Group-IO,
workspace behavior and exports. Final checks: 14 dedicated Core tests passed,
including real GL capture; the complete EasyKnack package run had 311 passed and
117 skipped tests (428 total), and a separate GL-enabled integration run had
15 passed and one skipped. A generated padded PNG was also visually inspected.

The complete Core run exposed seven failures in the existing baseline: six
`MavenSmokeTest` cases (controls, configuration editors, localization/themes,
lists, menus and theme-code support), plus
`GLNodeInsertionTest.exclusionReconnectsAndOnlyClosesSimpleChains`. Running the failed cases with the pre-refactor `GLLabel` in an
isolated test classpath reproduces the same seven failures. The packaging follow-up
resolved this debt: obsolete theme constants and preset names were corrected in
the tests, and missing reference-dash schema entries were added. The complete Core
run now passes: 116 passed, one optional GL test skipped (117 total).

## Version and compatibility assessment

The extractions add Core APIs with no intended document,
plugin ABI or host-protocol change. Existing serialized template keys remain valid. Core's existing `0.2.0-SNAPSHOT` development
version covers it. EasyKnack retains its current development version without
inventing a release baseline. Existing geometry tests remain in EasyKnack as
consumer checks; the library also runs the geometry tests independently.
