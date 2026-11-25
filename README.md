# EasyKnack

EasyKnack is a node-based audio application for building synthesizers, effects,
analysis tools and reusable processing groups. Connect nodes visually, inspect
signals as they run, and save your work as `.knack` documents.

## Get started

Download the [Windows x64 release](https://github.com/Helge97/EasyKnack/releases/latest)
with its matching sources and checksums. The installer is unsigned.

Use the Windows x64 installer or portable application folder. Packaged builds
require an external Windows x64 JDK 25, including `javac`. They include the audio
host and native tools used by Formula, Bake and source plugins. See the quick start
for JDK selection and installation.

1. Start EasyKnack and choose **File → New**.
2. Select your speakers or headphones in **Audio Output** and use a low device volume.
3. Choose **Audio → Start** to hear the connected Oscillator.
4. Change Frequency, then choose **File → Save** to keep the document.

See the [Windows quick start](docs/user/WINDOWS-QUICK-START.md) for installation and
first-use troubleshooting, or open the [user guide](docs/user/README.md).

## User documentation

- Node references are the primary documentation: [Deutsch](docs/nodes/de/index.md) · [English](docs/nodes/en/index.md).
- The supplementary handbook explains workflows and concepts: [Deutsch](docs/user/de/index.md) · [English](docs/user/en/index.md).

Older guide URLs forward to these maintained pages. They are retained for existing
links rather than maintained as parallel manuals.

EasyKnack is in development. Windows x64 packaging is available; a clean-machine
installation/update/uninstall acceptance test is still pending. There is no
virtual audio driver bundled with the application. Consult the limitations before
sharing documents that depend on local media or plugins.

## Development

This development branch requires the external GLSwing `0.3.0-SNAPSHOT`
artifact. Build and install that version from a separate GLSwing checkout:

```sh
cd /path/to/GLSwing
mvn -pl glswing-core -am install
cd /path/to/easy-knack
mvn package
```

EasyKnack still consumes only the Maven artifact; the checkouts are independent.
The checksum-pinned [published 0.2.0 SDK](https://github.com/Helge714/GLSwing/releases/tag/v0.2.0)
and its bootstrap remain available for matching release branches. A public SDK
for the new development version has not been published yet.

See [developer documentation](docs/development/README.md) for builds, architecture
and technical references. GLSwing is an external Maven dependency. Product changes
are recorded separately in [release notes](docs/RELEASE-NOTES.md).

## License

EasyKnack is licensed under the [GNU GPL version 3 only](LICENSE)
(`GPL-3.0-only`), unless otherwise indicated. Third-party components retain their
own licenses; GLSwing remains Apache-2.0. See [third-party notices](THIRD-PARTY.md)
and the [distribution checklist](docs/development/LICENSING.md).
