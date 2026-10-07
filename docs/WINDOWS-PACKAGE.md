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

Build a Windows x64 application folder using a separately installed JDK:

```powershell
./scripts/package-windows.ps1 -JdkHome C:/tools/jdk-25 -Toolchain C:/tools/llvm-mingw
```

Requirements on the build machine: PowerShell 7, Maven, a Windows x64 JDK 25, Windows .NET Framework 4.x (for the small launcher),
and LLVM-MinGW. The script runs the Java tests, rebuilds the native host from
the matching generated parameter contracts and creates a fresh, dated output
directory under `target/distribution`. It never replaces an existing image.
GLSwing remains an external Maven dependency. Build it and install its required
version in Maven before packaging when working on unreleased Core changes.

Run `EasyKnack.exe` from the resulting `EasyKnack` folder. Keep the complete folder
together: it contains the C++ toolchain, application, native audio host, media
decoder and OpenGL libraries. Users need a separate Windows x64 JDK 25 including
`javac`. Maven and Eclipse are not required. No JDK or Microsoft Java-runtime
DLLs are copied into new EasyKnack images. This is an application folder, not a
single-file executable. Its size includes development tools for Bake and plugins.

Plugin sources, compiled plugins, Formula caches and media caches belong in the
user profile, not the installation. Built-in examples are installed into the
user's example library without replacing existing files. An empty plugin library
does not trigger compilation at startup. Existing source plugins may need a
one-time rebuild after upgrading the application or relocating its compiler.

`-Type exe` or `-Type msi` builds a per-user WiX 3 installer. Its feature tree
lets users independently enable the Start Menu and Desktop shortcuts; `.knack`
opening is registered for the current user. Windows retains control over the
user's default-app choice. The final page offers to check Java and start EasyKnack.
The EXE wraps the MSI in a WiX bootstrapper; neither contains a Java installer.
Signing, a custom icon and clean-machine upgrade/uninstall testing remain separate.

At first launch (including the installer finish action), a small .NET Framework
launcher invokes the packaged Windows PowerShell 5.1 script. It finds JDK 25 x64
via `EASYKNACK_JAVA_HOME`, a saved explicit choice, `JAVA_HOME`, PATH, common vendor
folders and JavaSoft registry entries. It checks release metadata and runs `javac
-version`. Unsupported versions/architectures and JRE-only installs are rejected.
If missing, users can select a JDK folder, cancel, or explicitly start interactive
`winget install --id Microsoft.OpenJDK.25 --exact --source winget --architecture x64 --interactive`.
Without winget, official Microsoft installation instructions open instead. No
license acceptance flags are supplied. Installation may require Internet access
and elevation; failure/cancellation offers retry or manual selection. JDK updates
and uninstall remain separate from EasyKnack. A user's existing Temurin JDK 25
works too; bundling Temurin is deferred, not a current packaging switch.

The native launcher and PowerShell execute no document text as code. Application
arguments are quoted literally. Logs are in `%LOCALAPPDATA%/EasyKnack/logs`; a
selected JDK is remembered in `%LOCALAPPDATA%/EasyKnack/jdk-path.txt` without
changing system `JAVA_HOME`. The packaged compiler stays local to EasyKnack.

To package an already verified image without recompiling it:

```powershell
./scripts/package-installer.ps1 -Image <EasyKnack folder> -WixDirectory C:/tools/wix
```

See [Microsoft OpenJDK installation](https://learn.microsoft.com/java/openjdk/install).

## Validation

The previous bundled-JDK installer tests do not validate this external-JDK
installer. Test its install, upgrade and uninstall on a clean Windows system
before publication. Never run prerequisite installation unattended in tests.

Use `scripts/test-windows-package.ps1 -Image <EasyKnack folder>` to test using
a detected external JDK (or `-JdkHome`) and the bundled compiler, an unrelated working directory and isolated
user/cache directories. It checks the native parameter contract, a playing null
audio device, media decoding, Formula compilation and plugin compilation/cache.
Add `-Window` to open and close the real EXE launcher as well. `-WindowOnly`
repeats just that check. Real audio devices and detailed visual interaction still
need a desktop smoke test.

## Distribution checklist

`app/inventory.json` records hashes of the staged application files. License
entries found inside dependency JARs are copied to `app/licenses`; LLVM-MinGW's original notices are retained with its distribution.
Before public distribution, review the bundled toolchain and all
dependency licenses, and arrange any required corresponding sources and notices.
Copying license files alone is not a completed redistribution audit. EasyKnack uses GPL-3.0-only; see the [licensing checklist](development/LICENSING.md). External virtual audio drivers are not
included. These local unsigned images are for validation, not published releases.

Current external-JDK validation: Maven and functional package tests pass, and the
real launcher opens the workspace. Automated graceful close remains unverified:
the initial document is marked modified and opens a save confirmation. JDK
download/setup and installer upgrade/uninstall still need clean-machine testing.
