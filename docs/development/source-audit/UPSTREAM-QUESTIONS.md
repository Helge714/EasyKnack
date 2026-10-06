# Native distribution questions — drafts, not submitted

These drafts contain public component/build details only. Submit after explicit
authorization; retain the response URL and date in the native review. Neither
question asserts a license violation. No runtime replacement is currently needed
for local testing.

## Adoptium support

Suggested destination: https://github.com/adoptium/adoptium-support/issues/new/choose

Title: Microsoft VC/UCRT redistribution notices for a bundled Temurin Windows runtime

We are preparing an open-source desktop application, EasyKnack, with a bundled
Temurin Windows x64 runtime, so users do not need to install Java separately.

The upstream artifact is
`OpenJDK25U-jdk_x64_windows_hotspot_25.0.4.1_1.zip`, SHA-256
`00c847d804f4a78e9f04f2683faf14fed898535b177b7fc704486cb0284e9283`.
All 431 files of our local JDK match this archive. jpackage copies 430 unchanged
files into the application runtime, omitting only `lib/src.zip`. We preserve the
legal directory and collect the corresponding OpenJDK/Temurin build sources.

The runtime includes Microsoft `msvcp140.dll`, `vcruntime140.dll` and
`vcruntime140_1.dll` (14.40.33810.0), plus UCRT/API-set files (10.0.26100.1742).

We found the general bundling answer in temurin-build issue 1268. Microsoft's
Visual C++ end-user runtime agreement does not itself grant redistribution and
the Microsoft documentation points to Visual Studio/REDIST terms instead.

For these unchanged files shipped as part of Temurin:

1. Which permission/terms apply to a downstream application distributor?
2. Which Microsoft notices or license texts should accompany the application?
3. Does this require any separate Visual Studio/SDK license held by the application
   distributor, or is redistribution covered through the Temurin distribution?

A link to the relevant existing documentation would be sufficient. We are not
requesting a new license or permission to modify the Microsoft binaries.

## Bytedeco / JavaCPP Presets

Suggested destination: https://github.com/bytedeco/javacpp-presets/issues

Title: Windows FFmpeg 7.1-1.5.11 release toolchain provenance

We are collecting source/build materials and notices for the standard, non-GPL
extension artifact `org.bytedeco:ffmpeg:7.1-1.5.11:windows-x86_64` bundled in an
open-source application.

Its FFmpeg configuration reports GCC 12.2.0 (MSYS2 Rev11), static libgcc/libstdc++
and pthread linkage. The 1.5.11 release-tree Windows action pins GCC 12.2.0-11 and
MinGW CRT/headers/libwinpthread/winpthreads 10.0.0.r258.g530c58e17-1. We collected
those upstream sources and the available MSYS2 recipes/patches.

However, ffmpeg.yml invokes the mutable `actions` branch. Can you confirm the
Windows action revision used to produce the published standard artifact, or point
to the release job/package inventory (`pacman -Q`) and applicable recipe/patches?
We also found both GCC Rev10 and Rev11 strings in avcodec-61.dll; are these expected
from prebuilt toolchain/static-library components?

This is a provenance question, not an allegation about the build. We would prefer
to retain the working upstream binaries instead of introducing an unnecessary
custom FFmpeg build.
