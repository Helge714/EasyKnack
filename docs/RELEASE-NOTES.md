# Release notes

## Unreleased

- Add shared, compact name/unit inspector rows with up/down ordering to Value Viewer and Measurement Recorder. Display order applies to frozen histories, table columns and exports while input sockets, trace colours and recorded slot associations remain stable. Recorder units are display labels only. Settings persist with the document and support Undo; legacy names/units are retained. Native contracts and document schema are unchanged. Covered by the current development version.

- Correct Digital Noise cycle-duration bias and fractional cycle timing so stationary octave deviations are symmetric over elapsed time. Add a bounded Gaussian distribution alongside Uniform; Strength remains the maximum octave deviation, with Gaussian sigma = Strength / 3. Strength zero immediately passes the base frequency. This intentionally changes existing sounds and seeded sequences. Rebuild Java, host, plugin SDK and affected baked/exported nodes together. Port IDs and document/protocol versions are unchanged. With no release baseline, retain the current development version and include this behaviour change in the first release notes.

- Add an inspector frequency-scale choice to Spectral Features. Logarithmic moments exclude DC, return a geometric Centroid in Hz and Spread in octaves (with semitone/cent choices), and preserve the definitions of Flux, Roll-off, Slope and Flatness. Linear remains the default; changing scale changes Spread connection semantics. Rebuild Java, host and plugin SDK together; document and protocol versions are unchanged. Covered by the current development version.

- Fix right-channel autocorrelation processing when FFT preserves stereo. Correlation descriptors now remain valid on both channels instead of being halved by a downstream channel mix. Rebuild the audio host and plugin SDK; document and protocol versions are unchanged. Covered by the current development version.

- Add Correlation Features with held control outputs for periodicity, period, repetition frequency, estimated HNR and validity; no Updated trigger. Inspector search/threshold settings and source-level metadata support silence gating, including averaged correlations. Rebuild Java, host and plugin SDK together. Document and host protocol versions are unchanged; the current development version covers this addition.

- Fix Measurement Recorder capture and Undo retention with multiple inputs by recognizing dynamically registered node variants. The current development version covers the fix; document and host contracts are unchanged.

- Add Measurement Recorder: trigger-aligned numeric rows, named inputs, persistent document tables, enlarged inspection and CSV/Excel export. Automatic capture marks the document modified without adding Undo entries. Tables retain up to 4096 rows without overwriting; Clear starts a new acquisition. Rebuild Java, host and plugin SDK together. No existing protocol or document-schema changes; the current development version covers this addition.

- Add population standard deviation, minimum and maximum to Value Average, using the same finite acquisition and held results as Mean. Rebuild Java, audio host and plugin SDK together. Existing port IDs and output indices are retained; document and protocol versions are unchanged. Covered by the current development version.

- Preserve direct group input-to-output routes when outer parameters change. Keep custom Value Viewer dimensions when returning from a group, including hidden nodes. Requires the updated external Core artifact; document and audio contracts are unchanged. Covered by the current development version.

- Fix group connection Undo after view replacement and preserve boundary wiring through presentation/property changes. Copied choice editors now allocate independent persistent parameter IDs; duplicate legacy IDs are repaired deterministically, retaining the first parameter value and using each additional editor's own default. Requires the updated external Core artifact; existing valid IDs, document format and audio protocol are unchanged. The current development version covers these fixes.

- Fix Analysis Average dropping delayed frames from asynchronous FFT spectrum streams; each new delivered frame now counts once. Both averaging nodes support insertion into compatible connections and mute bypass; auxiliary Count/Finished outputs are silenced while muted. Existing contracts and versions are unchanged; the current development version covers these fixes.

- Add Value Average and Analysis Average for trigger-started finite arithmetic measurements, count/time limits, held results and a Finished trigger. Spectra average power per bin; correlations average per lag. Spectral Features accepts statistical analysis resources; IFFT still requires a phase-bearing spectrum. Rebuild Java, host and plugin SDK together. The current development version covers these compatible additions; existing protocol and document versions are unchanged.

- Preserve renamed nodes through diagnostic refresh, language changes, copying and group navigation. Custom names are saved in Core graph schema 9; older documents still load, while new saves require the updated Core. The current development version covers this fix.

- Buffer spectral render bursts on a dedicated WASAPI producer, with reserve based on FFT hop cadence. Ordinary audio graphs keep direct rendering; ASIO stays synchronous. This trades extra spectral-path latency for callback deadline isolation. Playback queue underruns are reported independently of cycle-averaged DSP load. The current development version covers the fix.

- Add MIDI editor drivers with learning, per-slider mapping/pickup, note/CC control of choices, toggles and trigger buttons, and MIDI-colored frames. Assignments persist in documents; graph connections take precedence without removing them. MIDI changes do not add Undo entries and retain current values through graph Undo. Requires the updated external GLSwing artifact and rebuilt audio/plugin host; audio protocol remains 11 with additive driver commands. The current development version covers these compatible additions.

- Add Spectral Features (eight descriptors) and a multi-input Value Viewer with bounded history, peak-preserving plots, Freeze, numeric display, zoom/measurement cursors and PNG/JPG/CSV snapshot export. Rebuild Java, host and plugin SDK together for the new node types; existing contracts and documents are unchanged. The current development version covers these compatible additions.

- Read the original project license/notices in verified source checkouts when an incremental IDE build removes generated legal resources. Packaged applications continue to require their bundled documents; plugin SDK licensing uses the same lookup. The current development version covers this fix.

- Keep Help > Licenses responsive by browsing complete license documents individually, starting with an overview. Requires the accompanying GLSwing multiline-layout fix for large texts. The current development version covers this compatible fix.

- Offer EasyKnack, Blue, Dark Blue and Light themes using external GLSwing presets with shared application color/layout fragments. Theme selection previews live; Light adapts workspace overlays for contrast. Existing theme selection IDs remain valid. The current development version covers this compatible feature.

- Add 16k, 32k and 64k FFT windows for synchronous and background processing, including 2x padding, IFFT and analysis readouts. Rebuild the audio host and source plugins with the updated SDK; new window sizes require this version. The current development version covers this compatible feature.

- Keep direct group input-to-output connections when leaving a group and reopening documents. Preserve numeric defaults and units during execution. Groups using direct forwarding require the updated GLSwing dependency; no audio-host protocol change. The current development version covers this fix.

- Add a sample-based Wavetable Viewer, lossless resource copy/paste and freehand sample editing alongside curves. Add an inspector-only minimum interval for asynchronous FFT requests. Existing curves remain readable; sample-mode documents require this version. Rebuild host and plugins together for the updated waveform and FFT parameter contracts. The current development version covers these additions.


- Add dark-blue Waveform resources with a periodic curve editor and band-limited oscillator playback. Add background Spectrum-to-Waveform capture, document snapshots and an explicit 64-point editable-curve copy. Waveform ports now require resources rather than numeric blocks; rebuild custom waveform plugins. The current unreleased development version covers this addition.

- Center unpacked groups on their combined node bounds. Fix live curvature preview with the event-driven application Canvas while Preferences remains open; verified using the actual application host. No document or host-protocol changes.

- Preview theme, language and connection curvature while Preferences is open; cancel restores the previous appearance. Add one-level Ungroup (Alt+G) with undo, preserved boundary values and connections, and retained group definitions. Special execution groups remain intact. No document or host-protocol migration required.

- Make autosave interval and recovery retention configurable in Preferences. Prepare and compare detached document snapshots on a low-priority worker. Add a live connection-curvature slider from straight lines to the current theme geometry; cancelling restores the previous setting.

- Add atomic crash-recovery snapshots every 30 seconds with a startup recovery dialog, original save targets, and seven-day retention. Ignored connections use muted opacity; unused muted-node inputs are included. Group titles show a geometric group marker.

- Add document-local unused-group retention (off by default when saving), fit-all view (Ctrl+Shift+C), and audio start/stop shortcuts (Ctrl+Enter / Ctrl+Shift+Enter). Missing mute bypass sources now mark connections as ignored and expose destination editors. Existing document metadata remains compatible.

- Replaced Vector Mapping with Vector Scale and Vector Rotate, each with an
  Audio/Control Origin input. Existing documents/plugins using Vector Mapping
  must replace it with Scale followed by Rotate (both Origins zero) and rebuild.
  Java, the audio host and plugin SDK must be updated together.

- Removed group inputs no longer block execution through stale instance defaults.
  Formula kernels consistently use their declared input units in spectral graphs.
- Unconnected numeric group inputs retain editable defaults without blocking audio,
  including newly added Formula inputs awaiting a valid code update.


- Vector ports now align with the first XYZ slider and collapse while connected.
  Vector Math and Vector Mapping can be inserted directly into vector connections.
  Vector Input is listed under Inputs; Spatial Receiver is under Routing.
  Receiver yaw is hidden and disconnected from processing in Mono mode.
  Added Vector Mapping (vector scale and Euler rotation) and Context Time output
  in seconds at audio rate. Existing port indices are retained. New nodes and the
  Time output require the updated host and SDK.


- Added Audio/Control vector signals, grouped XYZ editors with length units,
  vector operations and scalar measurements, and a mono/stereo Spatial Receiver
  with channel-specific distances, yaw, spacing and optional propagation.
  Vector group boundaries support bake/export. Existing documents retain their
  behavior; documents using these nodes require this build or newer.

- Explorer startup opens the requested document before showing the workspace,
  without asking to discard the default graph. The splash shows the packaged
  application version (or development status for unpackaged IDE launches).

- Release packaging now includes verified GLSwing build inputs and WiX sources/
  notices. SDK and source-plugin exports carry license notices; existing plugins
  may rebuild once because the SDK archive changed. Source review and installation
  validation are reported separately.

- Added product splash artwork and application icons for windows, launcher,
  installer, shortcuts and .knack documents. The splash retains its standard
  text layout directly over the artwork, with narrower text margins and a
  full-width progress bar at the bottom edge.

- Windows packages now require a separately installed x64 JDK 25. The launcher
  detects Java and offers interactive Microsoft OpenJDK installation or folder
  selection. LLVM-MinGW remains included for Bake/plugins; Java is not removed
  when EasyKnack is uninstalled. Bundled Temurin is deferred.


- Replaced bundled FFmpeg/JavaCPP with the existing miniaudio decoder for WAV and
  MP3 import. Other formats must be converted before import; existing PCM caches
  remain readable. Player/Buffer behavior and the audio-host protocol are unchanged.

- Expanded Windows native-source and license coverage, including GNU Make,
  MinGW gendef and FFmpeg dependencies. Recorded official runtime/toolchain
  comparisons and remaining provenance limits; exclude generated compiler Python
  caches from future installers. No DSP, document or plugin-contract change.

- Added Help with an offline English handbook, selectable application/Core build
  details and license notices. Installer builds capture local sources and collect
  checksum-locked upstream materials beside the installer; incomplete provenance
  review is explicitly reported. No document, plugin ABI or audio protocol change.

- Declared EasyKnack GPL-3.0-only; GLSwing and third-party licenses remain unchanged.
  Packages include project and native-component notices. Corresponding-source
  preparation remains a release requirement. No runtime or document-format change.

- Replaced the mixed-language README with an English quick start and linked user
  guide, node reference and known limitations. Windows packages include the guide.
  Developer and historical notes are separate; no runtime or document-format change.

- Windows installers register .knack documents. File launches reuse the running
  application, queue during startup and preserve the unsaved-changes prompt.
  No document, plugin ABI or audio protocol change.

- Windows EXE/MSI installers let users choose desktop and Start Menu shortcuts independently.

- Added a self-contained Windows app-image build with bundled Java/C++ tools,
  native audio host and media support. Installed host/tool discovery no longer
  depends on the working directory. No document, plugin ABI or protocol change.

- Shared UI/export and group-authoring infrastructure now comes from GLSwing.
  Dialogs remember folders by stable purpose across language changes; directory
  selections remember the selected folder. Existing group/template documents remain compatible.

- Image-export geometry now uses GLSwing's shared dimension model; sizing and
  padding behavior are unchanged. No document or audio-protocol migration.

- Audio diagnostic exports identify the packaged EasyKnack and GLSwing builds,
  including the source revision and whether uncommitted changes were present.
- Local development launches are explicitly distinguished from packaged builds.

The first published release version has not been designated yet.

- Instance Groups now support internal FFT / Spectral Group / IFFT chains, with independent frame state per instance, including source export. Public Instance Group boundaries remain numeric.

- Instance Groups accept shared Spectrum inputs, including source export and Bake; per-instance processing retains independent state. Existing numeric instance-input contracts remain unchanged; the host and plugin SDK include the new typed resource boundary.

- Instance Groups support all Vector nodes and Spatial Receiver, including shared Vector inputs, sample-rate/channel preservation and Bake/source export. Group outputs still require numeric Instance Merge results.

- Instance Groups can contain other Instance Groups, including in Bake and source
  export. Counts and context indices are scoped to each level; capacity limits
  account for the entire nested graph. Update the audio host and plugin SDK together.

- Copied integer group sliders retain usable increments and drag sensitivity, including older editor snapshots. Group-authored sliders can select integer values in Group-IO.
- Newly extracted groups inherit their first output signal kind once. All signal categories have matching node kinds and shared highlight colors; trigger-producing, buffer and spectrum nodes use their corresponding palettes. Existing manual group kinds remain unchanged.

- Group extraction preserves the semantic category of connected outputs instead of their shared numeric transport type. Editing group slider limits or defaults adjusts dependent values automatically.
