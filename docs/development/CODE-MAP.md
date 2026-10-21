# EasyKnack code map

Developer navigation index. Start with the task row, then inspect only the relevant
files and their callers/tests. This is a locator, not an API contract or a complete
file inventory; verify behavior in current code. Update affected rows when moving
files, adding a subsystem or changing ownership. Do not add debugging transcripts.

## Ownership and flow

EasyKnack owns audio policy, DSP, units, node definitions, application menus and
its document migrations. GLSwing owns generic widgets, graph editing, composites,
history, document mechanisms and rendering. EasyKnack consumes an external Maven
artifact; a Core checkout is never a source/build dependency of the application.
See [ownership details](../CORE-BOUNDARY.md).

Typical execution path: WorkspaceController -> GroupCompiler / special-group
preparation -> Core graph flattening -> AudioGraphEncoder -> AudioHostService /
AudioHostClient -> native host. Bake/export has its own source-generation path.

## Java entry points

Paths below are relative to `src/main/java/com/vin/easyknack/`.

| Task | Start here | Responsibility |
| --- | --- | --- |
| Startup / launch files | `EasyKnack.java`, `DocumentLaunch.java`, `LaunchArguments.java` | Application launch, startup work and opening documents passed to the process. |
| Workspace / menus | `ui/WorkspaceController.java`, `ui/WorkspaceView.java`, `ui/DocumentActions.java`, `ui/NodeEditingActions.java` | Application orchestration and commands; generic editing lives in Core. |
| Node definitions | `nodes/BasicNode.java`, `nodes/NodeBuilder.java`, `nodes/Editor.java`, `nodes/PropertyId.java` | Node layout, properties, editors, native binding and capability hooks. |
| Registration / add menu | `nodes/RegisterNode.java`, `nodes/NodeRegistry.java`, `nodes/NodeCategory.java`, `nodes/NodeKind.java`, `nodes/SignalCategory.java` | Registration, menu grouping, title categories and signal types are distinct concerns. |
| Node views / inspector | `ui/AudioNodeFactory.java`, `ui/BasicNodeView.java`, `nodes/InspectorEditor.java`, `nodes/EditorPresentation.java` | Application view binding and editor presentation. Special views live beside BasicNodeView. |
| Group interface / copied editors | `ui/GroupInputSettings.java`, `ui/GroupInputs.java`, `ui/GroupEditorCopies.java`, `ui/GroupEditorCapabilities.java`, `ui/GroupViewFactory.java` | Group input options, linked internal editors and group-instance presentation. |
| Editor templates | `ui/EditorTemplates.java`, `ui/EditorTemplatesPanel.java` | Application template choices and document metadata; generic template storage is in Core. |
| Group defaults / units / rates | `ui/GroupCompiler.java`, `ui/GroupUnits.java`, `ui/GroupPortRates.java`, `ui/GroupRouting.java` | Prepare group execution, boundary unit adapters, port-rate display and routing policy. |
| Persistence / recovery | `ui/NodeDocuments.java`, `ui/KnackDocumentCodec.java`, `ui/DocumentSettings.java`, `ui/RecoveryStore.java`, `ui/GroupImport.java` | Knack format, application migrations, document options, recovery and importing groups. |
| Rate inference / feedback / diagnostics | `nodes/AutomaticRates.java`, `ui/WorkspaceGraphDiagnostics.java`, `audio/FeedbackConnections.java`, `audio/SampleFeedback.java` | Resolve application rates and determine executable/ignored connections and feedback support. |
| Java to host | `audio/AudioGraphEncoder.java`, `audio/AudioHostService.java`, `audio/AudioHostClient.java` | Encode executable graphs, coalesce live updates, manage host process and control protocol. |
| Parameters / generated contracts | `nodes/NativeProcessor.java`, `nodes/NativeContractGenerator.java` | Java/native parameter contracts. Registration generation also uses src/processor/. |
| FFT size choices | `nodes/SpectrumNode.java`, `nodes/FFTStreamNode.java`, `audio/AudioAnalysis.java` | FFT window choices and Java analysis-frame limit. Native capacities live in Spectrum.hpp, SpectrumStream.hpp, AnalysisData.hpp and AnalysisResources.hpp; preserve 2x padding. |
| Analysis / file audio | `audio/AudioAnalysis.java`, `nodes/AnalysisEditor.java`, `ui/AnalysisFocusView.java`, `audio/AudioFileDecoder.java` | Analysis data, chart interactions, expanded views and audio-file decoding. |
| Bake / source export | `ui/GroupBakes.java`, `ui/GroupSourceExporter.java`, `ui/MathFusion.java`, `ui/SampleFusion.java`, `ui/InstanceSourceSpecialization.java`, `ui/SpectralSourceSpecialization.java` | Bake/cache state and generated DSP specializations; inspect the relevant specialization only. |
| Special groups / formulas | `ui/MultipleGroups.java`, `ui/SpectralGroups.java`, `ui/FormulaGroups.java`, `formula/FormulaCompiler.java`, `formula/TypedFormulaCompiler.java`, `formula/FormulaNativeCompiler.java` | Instance/spectral execution policy and formula validation/compilation. |
| Plugins | `plugins/PluginManager.java`, `plugins/PluginManifest.java`, `plugins/PluginSdk.java` | Plugin install/build/cache, manifests and compiler SDK. |
| Preferences / theme / language / units | `ui/SettingsDialog.java`, `ui/ApplicationSettings.java`, `ui/ApplicationTheme.java`, `ui/BundledThemes.java`, `ui/LiveLanguage.java`, `ui/PortUnitMenus.java`, `nodes/UnitFormat.java` | Application settings, semantic colors, translations and physical-unit policy. |
| Image / CSV export | `ui/GraphImageExport.java`, `ui/AnalysisImageExport.java`, `ui/AnalysisCsvExport.java` | Application selection, dialogs and analysis data; generic raster capture/encoding belongs to Core. |

For a particular node, start with `nodes/<Name>Node.java`; search its registration
ID and native processor name when the Java/C++ names differ. Display names are
translated and need not match filenames (for example Complex Oscillator uses
AdditiveOscillatorNode; Buffer nodes retain History names in some classes).

## Native entry points

Paths below are relative to `native/audio-host/src/`.

| Task | Start here | Responsibility |
| --- | --- | --- |
| Host entry / graph execution | `main.cpp`, `AudioGraph.hpp`, `GraphRuntime.hpp`, `NodeProcessor.hpp` | Host commands, graph construction/runtime and processor interface. |
| Individual DSP nodes | `nodes/` | Native processors; usually start with the Java definition and corresponding Node.hpp. |
| Devices / MIDI | `audio/`, `midi/` | Device transport and MIDI backend implementation. |
| Spectrum / workers / resources | `Spectrum.hpp`, `SpectrumStream.hpp`, `SpectralWorker.hpp`, `AnalysisResources.hpp`, `WaveformResource.hpp`, `Wavetables.hpp` | FFT frame resources, spectral work, analysis and oscillator waveforms. |
| Buffers / generated execution / telemetry | `BufferRead.hpp`, `BakedGraph.hpp`, `CompiledInstanceAudio.hpp`, `RenderTelemetry.hpp`, `RenderCadence.hpp` | Buffer access, compiled group execution and workload/deadline measurements. |

## Resources, builds and tests

- `src/main/resources/easyknack*.gltheme`: selectable bundles over external Core presets.
- `src/main/resources/easyknack/themes/`: shared color and geometry fragments; fragments are not selectable themes.
- `src/main/resources/application*.properties`, `nodes*.properties`: UI and node translations.
- `src/processor/java/com/vin/easyknack/processor/NodeProcessor.java`: annotation processing; generated registrations are build output.
- `pom.xml`: dependencies, product version and Maven build steps.
- `scripts/build-audio-host.ps1`, `scripts/generate-native-nodes.ps1`: native build and generation.
- `Build-Installer.bat`, `scripts/build-installer.ps1`, `scripts/package-windows.ps1`: Windows packaging entry points.
- `src/test/java/com/vin/easyknack/`: Java tests, generally mirroring packages/features.
- `native/audio-host/tests/`, `native/audio-host/benchmarks/`: native validation and performance cases.
- `docs/user/`: user instructions; `docs/development/`: contributor references.
- `docs/RELEASE-NOTES.md`: product changes; `TODO.md`: pending work.
- `target/`: generated/build artifacts and local diagnostic scripts; not authoritative source or permanent build instructions.

For a small fix, locate the nearest existing regression test first. Run focused
checks, then any required broader check once after the changes are assembled.
Documentation-only navigation changes do not need a Maven/native rebuild.

## Core navigation

In a separate GLSwingNext checkout, start with `docs/CODE-MAP.md`.
For group boundary wiring specifically: GLNodeWorkspace -> composite interface
transactions/descriptors -> GLCompositeGraphFlattener / GLCompositeForwarding.
For sliders, picking, tabs or labels: start in Core ui/, not AudioNodeFactory.
