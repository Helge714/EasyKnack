# Release notes

## Unreleased

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
