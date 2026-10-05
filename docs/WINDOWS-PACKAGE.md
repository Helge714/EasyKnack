# Windows packaging (developer guide)

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
and a Start Menu entry. It requires a WiX version supported by the selected JDK on the build
machine. Failure to build an installer leaves the portable image intact. Signing,
an application icon, file associations and release/update testing remain separate
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
Copying license files alone is not a completed redistribution audit. Choose and
add the EasyKnack project license as well. External virtual audio drivers are not
included. These local unsigned images are for validation, not published releases.
