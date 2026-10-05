# EasyKnack

EasyKnack is a node-based audio application for building synthesizers, effects,
analysis tools and reusable processing groups. Connect nodes visually, inspect
signals as they run, and save your work as `.knack` documents.

## Get started

Use the Windows x64 installer or portable application folder. Packaged builds
include Java, the audio host and the tools used by Formula, Bake and source plugins.
You do not need a development environment to use them.

1. Start EasyKnack and choose **File → New**.
2. Select your speakers or headphones in **Audio Output** and use a low device volume.
3. Choose **Audio → Start** to hear the connected Oscillator.
4. Change Frequency, then choose **File → Save** to keep the document.

See the [Windows quick start](docs/WINDOWS-QUICK-START.md) for installation and
first-use troubleshooting, or open the [user guide](docs/user/README.md).

## User documentation

- [Workspace and documents](docs/user/workspace.md): editing, shortcuts, examples and export.
- [Audio and routing](docs/user/audio.md): devices, system audio, levels and diagnostics.
- [Node reference](docs/user/nodes.md): choosing nodes, signal rates and units.
- [Groups, Formula and plugins](docs/user/groups.md): reusable interfaces and custom processing.
- [Spectrum and analysis](docs/user/analysis.md): FFT/IFFT, measurement and data export.
- [Known limitations](docs/user/limitations.md): portability and current feature boundaries.

EasyKnack is in development. Windows x64 packaging is available; a clean-machine
installation/update/uninstall acceptance test is still pending. There is no
virtual audio driver bundled with the application. Consult the limitations before
sharing documents that depend on local media or plugins.

## Development

See [developer documentation](docs/development/README.md) for builds, architecture
and technical references. GLSwing is an external Maven dependency. Product changes
are recorded separately in [release notes](docs/RELEASE-NOTES.md).

## License

EasyKnack is licensed under the [GNU GPL version 3 only](LICENSE)
(`GPL-3.0-only`), unless otherwise indicated. Third-party components retain their
own licenses; GLSwing remains Apache-2.0. See [third-party notices](THIRD-PARTY.md)
and the [distribution checklist](docs/development/LICENSING.md).
