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
| Workspace / menus | `ui/WorkspaceController.java`, `ui/WorkspaceView.java`, `ui/DocumentActions.java`, `ui/NodeEditingActions.java`, `ui/HelpMenu.java`, `ui/HelpDocuments.java` | Application orchestration and commands; generic editing lives in Core. |
| Node definitions | `nodes/BasicNode.java`, `nodes/NodeBuilder.java`, `nodes/Editor.java`, `nodes/PropertyId.java` | Node layout, properties, editors, native binding and capability hooks. `requiresSpectralContext()` marks bin-only processors for palette and temporal-encoder validation; `supportsSpectralBins()` also permits reusable numeric nodes. Regression: `SpectralPaletteTest`. |
| Node reference documentation | `ui/NodeDocumentation.java`, `ui/DocumentationViewer.java`, `ui/DocumentationPage.java`, `ui/DocumentationMarkdown.java`, `docs/nodes/catalog.json`, `scripts/build-docs.py` | Stable view identities open clicked/active node help; a separate index stays available. Native tables, links and history load per page. Verified checkout fallback is anchored to `target/classes` for incremental runs; JARs use bundled resources. `NodeDocumentationTest` covers source/bundle resolution, real context menus and active-node/group selection; `DocumentationViewerTest` covers presentation/navigation. Plugin help discovery remains pending. |
| Shared documentation foundations | `docs/user/en/index.md`, `docs/user/de/index.md` | Paired manual guides for rates, data types/channels, scalar units and Mute. Node references link these instead of repeating generic behavior. |
| Documentation startup / cache | `ui/DocumentationPreparation.java`, `ui/DocumentationPage.java`, `EasyKnack.java` | Splash background step parses both languages; GL step warms the actual theme/heading/code font atlases in Core's shared CPU cache. Viewer reads current text and reuses matching parsed pages; layout/history remain per view. Regression: `DocumentationPreparationTest`. |
| Registration / add menu | `nodes/RegisterNode.java`, `nodes/NodeRegistry.java`, `nodes/NodeCategory.java`, `nodes/NodeKind.java`, `nodes/SignalCategory.java` | Registration, menu grouping, title categories and signal types are distinct concerns. `NodeRegistry.executionContracts()` reuses thread-confined execution contracts and refreshes after plugin changes. |
| Node views / inspector | `ui/AudioNodeFactory.java`, `ui/BasicNodeView.java`, `nodes/InspectorEditor.java`, `nodes/EditorPresentation.java` | Application view binding and editor presentation. Special views live beside BasicNodeView. |
| Group interface / copied editors | `ui/GroupInputSettings.java`, `ui/GroupInputs.java`, `ui/GroupEditorCopies.java`, `ui/GroupEditorCapabilities.java`, `ui/GroupViewFactory.java` | Group input options, linked internal editors and group-instance presentation. Copy sources include nested public editors; `GroupEditorCopies` forwards their boundary properties (`NestedCopyEditorTest`). `GroupInterfaceValidity` removes stale ports/editor targets/property mappings and invalid signal wires; workspace changes reconcile them, with Core edits and repairs sharing Undo (`GroupInterfaceValidityTest`). Forwarded Instance Count editors lose their private role; ordinary wrappers normalize older copies (`ForwardedInstanceCountTest`). `GroupChoices` recovers uniquely identified retired selections; `GroupCompiler` excludes undeclared generated choice fields from execution. Regression: `RetiredGroupChoiceTest`. |
| Editor templates | `ui/EditorTemplates.java`, `ui/EditorTemplatesPanel.java` | Application template choices and document metadata; generic template storage is in Core. |
| Group defaults / units / rates | `ui/GroupCompiler.java`, `ui/GroupUnits.java`, `ui/GroupPortRates.java`, `ui/GroupRouting.java` | Prepare reachable group dependencies, boundary unit adapters, port-rate display and routing policy. `GroupCompilerReachabilityTest` retains nested dependencies without compiling unused document groups. |
| Document group inventory | `ui/DocumentGroups.java`, `ui/GroupManagerPanel.java`, `ui/DocumentSettings.java`, `ui/WorkspaceController.java` | Direct references across all embedded definitions; open-path protection, undoable registry/catalog removal and workspace navigation cleanup. Regression: `DocumentGroupsTest`. |
| Persistence / recovery | `ui/NodeDocuments.java`, `ui/KnackDocumentCodec.java`, `ui/DocumentSettings.java`, `ui/RecoveryStore.java`, `ui/GroupImport.java` | Knack format, application migrations, document options, recovery and importing groups. |
| Rate inference / feedback / diagnostics | `nodes/AutomaticRates.java`, `ui/WorkspaceGraphDiagnostics.java`, `audio/FeedbackConnections.java`, `audio/SampleFeedback.java` | Resolve application rates and determine executable/ignored connections and feedback support. |
| Audio import / gapless MP3 | `audio/AudioFileDecoder.java`, `ui/AudioFileEditor.java`, native `audio/AudioFileDecode.hpp`, `audio/GaplessMp3Decoder.hpp` | Import-only dr_mp3 backend honors tagged delay/padding before miniaudio resampling. Versioned MP3 cache names trigger existing asynchronous reimport; WAV caches are unchanged. Native third-party README pins provenance; Windows packaging retains license statements. Regression: `AudioFilePlayerTest` covers both loop paths. |
| Device volume / keyboard links | `ui/WorkspaceController.java`, `audio/AudioHostService.java`, native `audio/WindowsDeviceVolume.hpp` | Volume targets use the shared special-group preparation while ignoring view/mute flags. Coalesced link commands acknowledge only the snapshot actually sent. `ui/VolumeKeyOverlay.java` presents themed, nonfocusable desktop feedback on EDT and disposes its window/timer with the workspace (`VolumeKeyOverlayTest`). Regressions: `AudioRoutingTest`, `AudioHostServiceTest`, native `WindowsAudioBackendTest`. |
| Java to host | `audio/AudioGraphEncoder.java`, `audio/AudioHostService.java`, `audio/AudioHostClient.java` | Encode executable graphs, coalesce live updates, manage host process and control protocol. Native `GraphRuntime.hpp` applies recursive parameter snapshots with control-thread resource retirement; regression: `InstanceParametersTest`. |
| Parameters / generated contracts | `nodes/NativeProcessor.java`, `nodes/NativeContractGenerator.java` | Java/native parameter contracts. Registration generation also uses src/processor/. |
| Bin Width units | `nodes/SpectrumBlurNode.java`, `ui/SpectrumBlurMigration.java`, native `nodes/SpectrumBlurNode.hpp` | Single Width source; shared octave/cent/semitone units and an alternative Bins interpretation. `NodeBuilder.Row.unitChoices` separates mode choices from compatible conversions. Regressions: `SpectrumBlurUnitsTest`, `SpectrumBlurTest`; older octave/bin documents retain wiring, Factor mode is removed. |
| FFT size choices | `nodes/SpectrumNode.java`, `nodes/FFTStreamNode.java`, `audio/AudioAnalysis.java` | FFT window choices and Java analysis-frame limit. Native capacities live in Spectrum.hpp, SpectrumStream.hpp, AnalysisData.hpp and AnalysisResources.hpp; preserve 2x padding. Small windows use 16/32-sample hops and tiered stream-frame storage; regression: `SmallFftTest`. |
| Multiple Spectrum outputs | `ui/SpectralGroups.java`, `nodes/SpectralGraphNode.java`, `ui/SpectralSourceSpecialization.java`, native `nodes/SpectralGraphNode.hpp` | One bin schedule writes independent complex output streams, including worker and Bake kernels. `MultipleGroups` and native `MultipleGraphNode` use typed Spectrum output boundaries to sum/mean aligned frames. Regression: `MultiSpectrumOutputTest`. |
| Dynamic output storage | `audio/AudioGraphEncoder.java`, `nodes/NativeContractGenerator.java`, native `GraphRuntime.hpp`, `NodeProcessor.hpp` | Preserve `node + 256 * output` addressing; validate actual source-port counts and allocate output/event scratch before processing. Spectral steps use packed per-voice values and sized frame/Combine buffers. Regressions: `DynamicOutputsTest` (24 ports, callback allocation check), `WideGroupOutputsTest` (12 Spectrum outputs, persistence/export). |
| Dynamic group inputs | `nodes/ParameterContract.java`, `nodes/NativeProcessor.java`, `nodes/NativeContractGenerator.java`, `ui/GroupSourceExporter.java` | Counted source lists and state-specific input order feed compiled groups, rate plans and Bake. Native `LegacyGroupParameters.hpp` reads earlier fixed contracts; `BakedGraph.hpp` and the group processors prepare sized rate/history buffers. Regressions: `WideGroupInputsTest`, `SpectralUniformTest`; `DynamicOutputsTest` also covers 300-input event evaluation without callback allocation. |
| Finite averaging | `nodes/AverageMeasurement.java`, `nodes/ValueAverageNode.java`, `nodes/AnalysisAverageNode.java` | Trigger-started acquisition settings; native `AverageMeasurement.hpp` and matching node headers own sample/frame counting, numeric moments/extrema, mean power/correlation and final hold. Regression: `AverageNodesTest`. |
| Seeded random frequency modulation | `nodes/DigitalNoiseNode.java`, native `nodes/DigitalNoiseNode.hpp`, `NoiseSharing.hpp` | Duration-compensated uniform/Gaussian octave distributions and base-cycle timing; shared kernels serve ordinary, feedback and baked groups. Regression: `DigitalNoiseDistributionTest`, `NoiseSeedsTest`. |
| Spectral features / value history | `nodes/SpectralFeaturesNode.java`, `nodes/ValueViewerNode.java`, `ui/ValueHistory.java`, `ui/ValueViewerPanel.java`, `ui/ValueHistoryChart.java`, `ui/ValueViewerExport.java` | Shared spectral moments; bounded numeric telemetry, peak-preserving multi-input charts and snapshot exports. Native processors have matching names in `nodes/`. |
| Correlation descriptors | `nodes/CorrelationFeaturesNode.java`, native `nodes/CorrelationFeaturesNode.hpp` | Peak selection, held scalar descriptors and finite HNR estimates; `AnalysisData.hpp` / `AnalysisResources.hpp` carry pre-normalization RMS through correlation and averaging. Regression: `CorrelationFeaturesTest`. |
| Numeric observer display settings | `nodes/ValueSeries.java`, `nodes/ValueSeriesEditor.java`, `ui/ValueSeriesSettingsPanel.java` | Shared framed name/unit rows and a single order for ports, Viewer snapshots and Recorder exports; fixed internal slots preserve existing samples/connections. Observer state transfers via the external Core view-factory replacement callback. Regression: `ValueSeriesTest`. |
| Measurement tables | `nodes/MeasurementRecorderNode.java`, `ui/MeasurementTable.java`, `ui/MeasurementRecorders.java`, `ui/MeasurementRecorderPanel.java` | Sample-aligned native capture, document property storage independent of views, bounded tables, directly editable per-row test labels and CSV export. Labels retain numeric slot ordering; Undo restores labels while retaining later captures. Regression: `MeasurementRecorderTest`; native `MeasurementRecorderTest.cpp`. |
| Analysis / file audio | `audio/AudioAnalysis.java`, `nodes/AnalysisEditor.java`, `ui/AnalysisFocusView.java`, `audio/AudioFileDecoder.java` | Analysis data, chart interactions, expanded views and audio-file decoding. |
| Tracked partial pipeline | `ui/PartialGroups.java`, `nodes/PartialAnalysisNode.java`, native `Partials.hpp`, `nodes/PartialGraphNode.hpp`, `nodes/PartialsSpectrumNode.hpp`, `nodes/PartialNoiseNode.hpp`, `PartialRandom.hpp`, `nodes/SpectrumMixNode.hpp`, `nodes/SpectrumMathNode.hpp`, `ui/SpectrumMixMigration.java` | Adaptive NBD/NDD/FCD classification with window/SNR calibration, successive lobe fitting and predictive/gap-aware tracking with centre-referenced phases, ID-local numeric programs, counter-based per-track/frame randomness and a separate 24-bit Context Seed, window-lobe synthesis and complex residual reconstruction. `ResidualModelNode` supplies a separate windowed stochastic model. `NodeProcessor.resourceCapacityHint` and control-thread `ResourcePreparationScope` propagate configured budgets through `GraphRuntime`, `BakedGraph` and Instance wrappers. Regressions: `PartialsTest`, native `PartialClassificationTest.cpp`, `SpectrumArithmeticTest`. Offline comparison: `scripts/partial-reference.py`, `native/audio-host/benchmarks/PartialReference.cpp`; setup and limits in [PARTIAL-REFERENCE.md](PARTIAL-REFERENCE.md). Classifier implementation/calibration: `PartialClassifier.hpp`, `PartialClassifierCalibration.hpp`, `scripts/partial_classifier_calibration.py`; model and tests in [PARTIAL-CLASSIFIER.md](PARTIAL-CLASSIFIER.md). `PartialAnalysisMigration` retires obsolete heuristic bindings. Spectrum Math applies complex arithmetic; Spectrum Mix crossfades aligned frames. Legacy weighted sums migrate to Math. |
| Hilbert quadrature | `nodes/HilbertNode.java`, native `nodes/HilbertNode.hpp` | Selectable 61–2049-tap FIR, aligned direct output and independent channel state. Regression: `HilbertNodeTest`. |
| Complex Oscillator stretch | `nodes/AdditiveOscillatorNode.java`, native `nodes/AdditiveOscillatorNode.hpp` | Power-law partial ratios, scalar cent-setting migration and cached log/ratio tables. Regressions: `AdditiveOscillatorTest`, `AdditiveSpreadTest`. Canonical references: `docs/nodes/de/additive-oscillator.md` and `en/additive-oscillator.md`; earlier drafts remain for comparison. |
| Step Switch | `nodes/StepSwitchNode.java`, `ui/StepSwitchView.java`, `ui/StepSwitchStates.java`, `nodes/StepOrderEditor.java`, native `nodes/StepSwitchNode.hpp` | Typed numeric/resource routing, stable-slot inspector ordering, block-aligned resource selection, inline renaming and revision-checked persisted feedback. Instance telemetry uses the first nested instance; `ui/CompiledManualTargets.java` retains controls/readouts. `BasicNodeView.portContent` decorates resource labels; `NodeBuilder.Row.optional` permits empty resource steps. Regressions: `StepSwitchTest`, `InstanceStepSwitchTest`, `ResourceStepSwitchTest`. |
| Bake / source export | `ui/GroupBakes.java`, `ui/GroupSourceExporter.java`, `ui/MathFusion.java`, `ui/SampleFusion.java`, `ui/InstanceSourceSpecialization.java`, `ui/SpectralSourceSpecialization.java` | Definition-shared local Bake identity with independent instance state, cache invalidation and generated DSP specializations; inspect the relevant specialization only. |
| Special groups / formulas | `ui/MultipleGroups.java`, `ui/SpectralGroups.java`, `ui/FormulaGroups.java`, `formula/FormulaCompiler.java`, `formula/TypedFormulaCompiler.java`, `formula/FormulaNativeCompiler.java` | Instance/spectral execution policy and formula validation/compilation. Typed instance resource boundaries include Buffer, Spectrum, Vector and Wavetable; regressions: `InstanceBufferTest`, `InstanceWavetableTest`, `InstanceDspCatalogTest`. See [processor support and exclusions](INSTANCE-SUPPORT.md). |
| Plugins | `plugins/PluginManager.java`, `plugins/PluginManifest.java`, `plugins/PluginSdk.java` | Plugin install/build/cache, manifests and compiler SDK. |
| MIDI editor drivers | `ui/MidiDrivers.java`, `ui/MidiDriverBinding.java`, `ui/MidiDriverSettingsPanel.java`, `audio/MidiDriverInput.java` | Learn/route MIDI to model parameters without Undo; attach inspector indicators on control attachment; preserve live values through graph history; native taps share existing MIDI device handles. `ControlMetadata` scopes transient host metadata, `ControlInterpolationSettings` stores document/driver timing and native `ControlInterpolation.hpp` renders adaptive ramps or a bounded timestamped timeline with finite edge rounding. `Editor.Slider.continuous()` / `.discrete()` declare interaction policy independently of integer presentation; configuration-only sliders are discrete automatically. Regression: `ControlInterpolationTest`. |
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
| Host entry / graph execution | `main.cpp`, `AudioGraph.hpp`, `GraphRuntime.hpp`, `NodeProcessor.hpp` | Host commands, graph construction/runtime and processor interface. `CommandReader.hpp` owns bounded buffered framing; regression: `CommandReaderTest.cpp` / `LargeCommandsTest`. |
| Individual DSP nodes | `nodes/` | Native processors; usually start with the Java definition and corresponding Node.hpp. |
| Devices / MIDI | `audio/`, `midi/` | Device transport and MIDI backend implementation. `audio/AudioRenderAhead.hpp` isolates spectral DSP bursts from WASAPI callbacks with a consumption-driven queue. |
| Spectrum / workers / resources | `Spectrum.hpp`, `SpectrumStream.hpp`, `SpectralWorker.hpp`, `AnalysisResources.hpp`, `WaveformResource.hpp`, `Wavetables.hpp` | FFT frame resources, spectral work, analysis and oscillator waveforms. |
| Buffer metadata / migration | `nodes/BufferContextNode.java`, native `nodes/BufferContextNode.hpp`, `ui/BufferContextMigration.java` | Resource size and sample-accurate next-write position; legacy writer connections and composite outputs migrate without loss. `NodeDocuments` reports file migrations through `DocumentActions`. Regression: `BufferWritePositionTest`. |
| Buffers / generated execution / telemetry | `BufferRead.hpp`, `BakedGraph.hpp`, `CompiledInstanceAudio.hpp`, `RenderTelemetry.hpp`, `RenderCadence.hpp` | Buffer access, compiled group execution and workload/deadline measurements. |
| Formula math execution | `FormulaProgram.hpp`, `TypedFormulaProgram.hpp`, `FormulaNumeric.hpp` | Keep function registry/help in Java, legacy/typed interpreters and generated kernels aligned. `FormulaMathFunctionsTest` covers domains and native/preview parity, including standalone SDK compilation. |

## Resources, builds and tests

- `src/main/resources/easyknack*.gltheme`: selectable bundles over external Core presets.
- `src/main/resources/easyknack/themes/`: shared color and geometry fragments; fragments are not selectable themes.
- `src/main/resources/application*.properties`, `nodes*.properties`: UI and node translations.
- `src/processor/java/com/vin/easyknack/processor/NodeProcessor.java`: annotation processing; generated registrations are build output.
- `pom.xml`: dependencies, product version and Maven build steps.
- `src/main/java/com/vin/easyknack/LegalDocuments.java`: packaged legal resources and source-checkout fallback after incremental resource cleanup.
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
