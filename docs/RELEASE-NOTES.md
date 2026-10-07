# Release notes

## Unreleased

- Added product splash artwork and application icons for windows, launcher,
  installer, shortcuts and .knack documents.

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
