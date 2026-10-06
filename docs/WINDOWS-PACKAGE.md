# Windows packaging (developer guide)

Double-click **Build-Installer.bat** in the repository root to build an EXE
installer from the current sources, including tests and the native host. The
console stays open on success or failure; on success the output folder opens.
Each build uses a new folder under `target/distribution` and includes the shortcut
selection dialog. Native compilation and installer compression can take several
minutes; detailed logs are referenced by the build scripts.

Each build also creates `EasyKnack-project-sources.zip` and
`EasyKnack-source-materials.zip` beside the installer. The latter downloads pinned
upstream sources on its first run and reuses a checksum-verified cache thereafter.
Its `REVIEW.json` lists incomplete provenance checks; it is not yet a public-release
clearance. See [source packaging](development/SOURCE-PACKAGES.md).

The launcher detects PowerShell 7, a JDK from `JAVA_HOME`, PATH or `~/.jdks`,
LLVM-MinGW and WiX from `target/tools`, and Maven from PATH or the local Eclipse
Maven runtime. It does not download tools. GLSwing must already be available as
the required external Maven dependency. Tool locations can be overridden:

```bat
Build-Installer.bat -JdkHome "C:\tools\jdk-25" -Toolchain "C:\tools\llvm-mingw" -WixDirectory "C:\tools\wix" -Maven "C:\tools\maven\bin\mvn.cmd"
Build-Installer.bat -CheckTools
```

Build a self-contained Windows x64 application folder:

```powershell
./scripts/package-windows.ps1 -JdkHome C:/tools/jdk-25 -Toolchain C:/tools/llvm-mingw
```

Requirements on the build machine: PowerShell 7, Maven, a Windows x64 JDK with `jpackage`,
and LLVM-MinGW. The script runs the Java tests, rebuilds the native host from
the matching generated parameter contracts and creates a fresh, dated output
directory under `target/distribution`. It never replaces an existing image.
GLSwing remains an external Maven dependency. Build it and install its required
version in Maven before packaging when working on unreleased Core changes.

Run `EasyKnack.exe` from the resulting `EasyKnack` folder. Keep the complete folder
together: it contains Java (including javac), the C++ toolchain, the application,
native audio host, media decoder and OpenGL libraries. No system Java, compiler,
Maven or Eclipse is required by users. This is an application folder, not a
single-file executable. Its size includes development tools for Bake and plugins.

Plugin sources, compiled plugins, Formula caches and media caches belong in the
user profile, not the installation. Built-in examples are installed into the
user's example library without replacing existing files. An empty plugin library
does not trigger compilation at startup. Existing source plugins may need a
one-time rebuild after upgrading the application or relocating its compiler.

`-Type exe` or `-Type msi` additionally invokes jpackage's per-user installer
backend. Both installer formats offer independent checkboxes for a desktop shortcut
and a Start Menu entry, and register `.knack` file opening for the current user.
Windows retains control over the user's default-app choice. Document launch requests
are handed to the existing application through a per-user lock and local request
directory under `~/.easyknack/instance`; no network listener is required.
It requires a WiX version supported by the selected JDK on the build
machine. Failure to build an installer leaves the portable image intact. Signing,
an application icon and release/update testing remain separate
release work; this local build does not declare a release.

To package an already verified image without recompiling it:

```powershell
./scripts/package-installer.ps1 -Image <EasyKnack folder> -JdkHome C:/tools/jdk-25 -WixDirectory C:/tools/wix
```

See the [JDK packaging requirements](https://docs.oracle.com/en/java/javase/25/jpackage/packaging-overview.html).

## Validation

The local Windows x64 image and EXE installer have been built with JDK 25 and
WiX 3.14.1. The isolated functional tests and EXE window open/close test pass,
including an image path containing spaces and a non-ASCII character. The EXE
installer has not yet been installed or uninstalled on a clean machine. The MSI
option is available but has not been separately validated.

Use `scripts/test-windows-package.ps1 -Image <EasyKnack folder>` to test using
only the bundled Java and compiler, an unrelated working directory and isolated
user/cache directories. It checks the native parameter contract, a playing null
audio device, media decoding, Formula compilation and plugin compilation/cache.
Add `-Window` to open and close the real EXE launcher as well. `-WindowOnly`
repeats just that check. Real audio devices and detailed visual interaction still
need a desktop smoke test.

## Distribution checklist

`app/inventory.json` records hashes of the staged application files. License
entries found inside dependency JARs are copied to `app/licenses`; Java legal
notices and LLVM-MinGW's original notices are retained with their distributions.
Before public distribution, review the exact bundled FFmpeg build and all
dependency licenses, and arrange any required corresponding sources and notices.
Copying license files alone is not a completed redistribution audit. EasyKnack uses GPL-3.0-only; see the [licensing checklist](development/LICENSING.md). External virtual audio drivers are not
included. These local unsigned images are for validation, not published releases.
