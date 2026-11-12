# Windows packaging (developer guide)

Double-click **Build-Installer.bat** in the repository root to build an EXE
installer from the current sources, including tests and the native host. The
console stays open on success or failure; on success the output folder opens.
Each build uses a new folder under `dist` and includes the shortcut
selection dialog. Native compilation and installer compression can take several
minutes; detailed logs are referenced by the build scripts.

Each build also creates `EasyKnack-project-sources.zip` and
`EasyKnack-source-materials.zip` beside the installer. The latter downloads pinned
upstream sources on its first run and reuses a checksum-verified cache thereafter.
Its `REVIEW.json` lists incomplete provenance checks; it is not yet a public-release
clearance. See [source packaging](SOURCE-PACKAGES.md).

The launcher detects PowerShell 7, a JDK from `JAVA_HOME`, PATH or `~/.jdks`,
LLVM-MinGW and WiX from `.local/tools`, and Maven from PATH or the local Eclipse
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
directory under `dist`. It never replaces an existing image.
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

`-Type exe` builds one WiX Burn setup window with embedded license text,
installation-folder and independent Start Menu/Desktop options, progress/action
messages, maintenance and a final Start EasyKnack button. The MSI runs without
its own wizard or separate Apps entry. Starting the application closes setup.
`-Type msi` retains the standalone feature-tree wizard. Both install for the
current user and register `.knack`; Windows retains the default-app choice.
Neither contains a Java installer.

The EXE persists options and reads installed choices from the application's HKCU
installer key. Missing registry values retain fresh-install defaults. Bundle-only
MSI properties select optional shortcut features; standalone MSI choices remain
available. The adapted WiX RTF theme/localization and their separate MS-RL notice
are maintained under `scripts/windows/InstallerTheme*`.

Installer outputs are named `EasyKnack-Setup-<version>-x64.exe` (or `.msi`),
distinct from the installed `EasyKnack.exe`. The standalone MSI preparation page uses `scripts/windows/Installer.en-us.wxl`.
The EXE keeps setup progress visible throughout MSI execution, including slow
space/file validation; the progress page explains that an existing installation
can take several minutes to check. This UI change does not eliminate MSI costing.

The EXE registers its uninstall/maintenance entry in Windows Installed apps;
reopening that setup also offers removal. Standalone MSI packages use Windows
Installer's uninstall entry. No separate `uninstall.exe` is shipped. The external
JDK has its own installation lifecycle. User documents and external media are not
installer-owned files.
Signing and clean-machine upgrade/uninstall testing remain separate.

Before copying application files, EXE setup runs an embedded Java prerequisite
helper; standalone MSI runs the same helper before `InstallValidate` (and before removal of an older product). Both share
`JavaPrerequisite.ps1` with the application launcher: Windows x64 JDK 25, compiler
presence and successful `javac -version` validation. A missing compatible JDK offers
folder selection or an explicitly approved external JDK installation; cancellation
stops setup before file installation. Quiet/passive installation never prompts or
downloads Java and fails if no compatible JDK is found. Removal skips the check.
The JDK remains separately installed and is never removed with EasyKnack.

`test-installer-prerequisite.ps1` builds an isolated product with a harmless launcher
and restricted candidate discovery, leaving the user's Java/EasyKnack installation
untouched. It verifies EXE/MSI rejection with no Java, successful installation with
a compatible JDK, and uninstall without Java. `test-java-prerequisite.ps1` also
checks incompatible metadata, JRE/broken compiler rejection and launcher arguments.

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
Copying license files alone is not a completed redistribution audit. EasyKnack uses GPL-3.0-only; see the [licensing checklist](LICENSING.md). External virtual audio drivers are not
included. These local unsigned images are for validation, not published releases.

Current external-JDK candidate `866d426`: Maven, packaged functional tests and the real EXE launcher pass. The workspace opens and closes without a false initial save prompt; repeated image captures retain their text. JDK setup/download and installer upgrade/uninstall still need clean-machine testing. See [the current candidate report](source-audit/CANDIDATE-2026-11-11.md).

The reviewed Windows installer toolchain is pinned to WiX 3.14.1 by component
hashes in scripts/sources.lock.json. Replacing it requires reviewing its source
archive/license and updating the lock. Current candidate evidence and outstanding
clean-machine checks are recorded in development/source-audit/CANDIDATE-2026-11-11.md.

## Packaged compiler profile

`PackageFiles.CopyCompiler` stages a Windows x64 runtime compiler subset of the
reviewed LLVM-MinGW toolchain. It retains the C/C++ drivers, linker, archive/strip
utilities, their runtime DLLs, the two Clang target/default configuration files,
complete shared C/C++ headers, Clang resources, x64 target libraries and vendor
notices. Source toolchains are never modified. ARM/32-bit targets, Python/LLDB and
unrelated analysis tools are omitted; the installed toolchain is not a full
cross-platform development/debugger distribution.

`test-compiler-profile.ps1` checks retention, exclusion and source preservation.
Formula DLL generation, a real exported plugin and native Bake execution were
checked against the staged compiler. The installer comparison uses isolated
products and a no-op application, with equal `MSIFASTINSTALL=7` settings for full
and reduced packages; install/repair/removal results are recorded separately.
`measure-installer-toolchain.ps1` reproduces this comparison without modifying the
installed EasyKnack product. Results and limits: [compiler profile measurement](source-audit/COMPILER-PROFILE-2026-11-12.md).
