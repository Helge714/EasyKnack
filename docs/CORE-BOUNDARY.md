# EasyKnack / GLSwing ownership audit

Developer architecture review, 2026-10-05. This is an ownership map and a
prioritized refactoring backlog, not user documentation or a claim that every
candidate has already been moved.

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
| `GraphImageExport` | Offscreen rendering and PNG/JPEG encoding are generic; selection preparation instantiates an EasyKnack workspace and strips bake state. | Split rendering/encoding from graph preparation in a subsequent change. Keep selection, bake handling and export dialog composition here. |
| `ExportPreview` | Scaled image/checkerboard presentation is reusable; dialog sizing is application policy. | Candidate for an ordinary Core image-preview component. |
| `AnalysisImageExport`, `AnalysisFocusView`, analysis charts | FFT axes, frequency/bin values, snapshots, measurement cursors and audio capture are domain features. | Keep here; consume generic renderer/encoder if extracted. |
| `NodeTooltipLabel` | Reimplements measuring, truncation and text drawing to obtain a non-focusable full-area hover target and special framed padding. Core `GLLabel` already implements alignment, selection, themed text and ellipsis. | Prefer extending `GLLabel` with optional tooltip/full-area hit support. Retain only application translation and framed-row padding policy here. Do not move a duplicate label renderer unchanged. |
| `BasicNodeView`, `AudioNodeFactory` | Views bind audio nodes, trigger buttons, device volume, analysis and coupled group editors. | Keep domain binding here. Extract neutral row-layout/binding mechanisms only after defining a Core descriptor API. |
| `InspectorEditor`, `EditorPresentation` | Adapt EasyKnack editor definitions and units to Core controls/custom properties. `GLNodePropertyInspector` already suppresses updates during inspector synchronization. | Keep adapters. Consolidate generic layout/application of metadata where shared; preserve standalone synchronization safety. |
| `UnitSlider`, `UnitFormat` | Core already supplies `GLUnitSlider`, `GLUnit` and unit formats. EasyKnack supplies Hz/sample-rate conversion, pitch/gain pools and signal semantics. | Keep physical conversion policy and sample-rate context here; control rendering/editing belongs in Core. |
| `GroupInputSettings`, `GroupEditorCopies` | Mix generic editor choice/defaults with audio rates, unit pools, node structure changes and coupled native parameters. | Split through a neutral capability/provider interface, not by moving these classes wholesale. |
| `EditorTemplates` | Template identity and persistence can be generic; allowed editor types and signal categories are application-defined. | Candidate generic template model with application validation hooks. Existing document data must round-trip. |
| `GroupImport` | Fresh graph IDs are generic, but imports also rewrite EasyKnack template/coupling metadata and group references. | Candidate Core copy/remapping service with explicit extension callbacks. Keep app-specific remapping here. |
| `NodeDocuments` | Already uses Core `GLDocumentManager` and codec registry. Remaining migrations and normalization know EasyKnack node contracts. | Keep adapter/migrations; do not duplicate Core document I/O. |
| `RememberingFileDialogs` | Last-folder persistence is reusable; preference namespace and translated-title identity belong to this app. | Candidate Core decorator with injected store and stable purpose ID, independent of translated labels. |
| `AppText`, themes, menus and examples | Core owns language/theme/control mechanics. App owns translations, category order, example discovery and commands. | Keep app resources and command assembly here. |
| `NodeHints` | Audio/control/trigger/reference explanations express EasyKnack semantics. | Keep centralized here, not in the generic widget toolkit. |
| `WorkspaceController` | Coordinates app services as well as UI. Size alone does not imply Core ownership. | Split internally into document, audio, group-authoring and presentation orchestration as needed. |
| `formula`, `GroupCompiler`, `plugins` | Formula semantics, DSP code generation, bake/cache keys, parameter contracts and native compilation are audio-runtime concerns. | Keep here. Core may provide editor/language-service UI, never the audio language/compiler implementation. |
| `audio`, native host, node processors | Device routing, MIDI, rates, feedback scheduling, FFT, voices, sample state and performance telemetry. | Keep entirely in EasyKnack. |
| Packaging/build identity | Each product owns its version/build metadata; application runtime bundling is EasyKnack deployment. | Keep EXE assembly here. Do not require Core sources on end-user machines. |

## Ordered follow-up work

1. **Label reuse:** extend the existing Core label rather than maintaining two
   text renderers. Test full-area picking without focus, tooltip disposal, themes,
   clipping, alignment and zoom. Then reduce `NodeTooltipLabel` to app policy.
2. **Image export services:** extract component-to-image capture and encoding.
   Accept a prepared component and dimensions, return pixels/bytes or structured
   errors. No EasyKnack workspace construction, translated error strings or bake
   state in Core. Preserve GL-context cleanup and GPU-size checks; cover PNG alpha,
   JPEG quality and failure cleanup as well as geometry.
3. **Remembered dialogs:** use a stable purpose key plus an injected preference
   store. Test language changes, missing directories, cancel and directory mode.
4. **Group editor capabilities:** represent copy eligibility, editor kind,
   defaults and supported units through neutral descriptors. Keep rate resolution,
   unit conversion and structural DSP rebuilds as application callbacks. Test
   coupled parameters, disconnect/reconnect, undo and document round trips before
   migrating storage or presentation.
5. **Template/import reuse:** introduce generic template data and ID-remapping
   extension points only after the editor contract is explicit. Test nested groups
   and duplicated IDs without losing application metadata.
6. **Application organization:** reduce `WorkspaceController` responsibilities
   inside EasyKnack. Moving a monolith into Core would hide the coupling instead
   of removing it.

These are separate changes, not prerequisites that indefinitely block an EXE.
The release prerequisite is a self-contained runtime using external Core
artifacts; the remaining refactors can proceed independently after regression
checks. Larger extractions above are deliberately pending, not implemented by
this audit.

## Version and compatibility assessment

The geometry extraction is an additive Core API with no intended UI, document,
plugin ABI or host-protocol change. Core's existing `0.2.0-SNAPSHOT` development
version covers it. EasyKnack retains its current development version without
inventing a release baseline. Existing geometry tests remain in EasyKnack as
consumer checks; the library also runs the geometry tests independently.
