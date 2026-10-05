# Release notes

## Unreleased

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
