# Windows native distribution review — 2026-10-06

Scope: the dependencies in `source-help-check-20261006`, built from the existing
Windows packaging script. This is an evidence-based provenance review, not a legal
opinion or a claim of independently reproduced upstream binaries.

## Results

| Area | Verified | Remaining boundary |
|---|---|---|
| Temurin 25.0.4.1+1 | Official archive hash matches release metadata; all 431 files in the local JDK match. The packaged runtime contains 430 unchanged files; only `lib/src.zip` is absent. Exact OpenJDK source, build-script commit, SBOM and configure/build arguments are collected. | Microsoft VC/UCRT files have their own redistribution terms; they are not covered by the OpenJDK source archive. |
| LLVM-MinGW 20250305 | All 9,015 upstream files match in both the local compiler and packaged copy. Four extra Python bytecode cache files were found, with no modified upstream files. Future packages exclude these generated caches. | Source mapping uses the pinned release build recipes, not a fresh independent rebuild. |
| FFmpeg 7.1 / Bytedeco 1.5.11 | Exact JAR hash/configuration and GCC sources recorded. The release-tree Windows recipe pins MinGW CRT/winpthreads to 10.0.0.r258.g530c58e17-1; that upstream source and its notices are now collected separately from LLVM-MinGW. | The workflow calls a mutable `actions` branch. Confirmation of the recipe actually executed for the published JAR remains outstanding. |

The machine-readable comparison results are `jdk-match.json`, `llvm-match.json`,
`packaged-jdk-match.json` and `packaged-llvm-match.json`. Their `exactMatch` field
stays false where a file is absent or extra: expected packaging differences are
documented here rather than hidden by an exclusion rule. Archive origins and
SHA-256 values are in `official-archive-origins.json`. The JDK hash was checked
against the upstream release JSON; the older LLVM release asset has no published
GitHub digest, so its recorded hash identifies the downloaded bytes, not a verified
upstream signature.

## LLVM-MinGW contents and sources

The package contains more than Clang. Sources now collected and hash-locked:

| Component | Source selected by the release recipes | License notes |
|---|---|---|
| LLVM/Clang, libc++, libc++abi, libunwind, compiler-rt, OpenMP, LLDB | `24a30daaa559829ad079f2ff7f73eb4e18095f88` (LLVM 20.1.0; also reported by the actual compiler) | Apache-2.0 with LLVM exceptions, plus component notices |
| MinGW-w64 CRT, headers, winpthreads and tools | `2be9e0f319990e48bfb0b2dd7b9e7045791b465f` | Component-specific licenses; preserve `share/mingw32` notices and tool licenses |
| `gendef` | Included in that MinGW source tree | GPL-3.0-or-later, as stated in its source header; not covered by LLVM's Apache license |
| GNU Make | 4.4.1, also reported by the bundled `mingw32-make.exe` | GPL-3.0-or-later; source archive and COPYING added |
| LLDB-MI | `2340ff25bc46201b20779bef5bc399bf994895f0` | Its separate LICENSE.TXT is included |
| Python for LLDB | cpython-mingw `d21f60d4a8bb03e0309fe3907b104f1878059f90` | Python license and upstream notices retained |
| libffi | 3.4.6 | MIT-style license text added |

The complete LLVM and MinGW source trees are now included, not just their wrapper
build scripts. The release scripts also describe cross-build configuration and
build-time dependencies. Build tools used upstream are distinct from libraries
actually shipped in EasyKnack.

## FFmpeg and GCC

The configuration explicitly requests static libgcc/libstdc++ and links
`gcc_eh`, `pthread`, `z` and Windows import libraries. Native import inspection
finds the expected FFmpeg DLLs and Windows system DLLs, not separate libgcc or
libstdc++ DLLs. `avcodec-61.dll` contains both MSYS2 GCC Rev10 and Rev11 strings
and a `mingw-w64-libraries/winpthreads` source path; see
`ffmpeg-runtime-strings.txt`. These strings identify traces, not the precise
library archives used for every linked object.

The matching Rev11 recipe is commit
`fd5380c8c48c5ab7cc9bc4189961da70f0317bed` in MSYS2/MINGW-packages. Its complete
recipe/patch tree is collected, as is GCC 12.2.0 with SHA-256
`e549cf9cf3594a00e27b6589d4322d70e0720cdd213f39beb4181e06926230ff`.
Do not substitute the much newer LLVM-MinGW CRT source for the FFmpeg build's CRT.

Follow-up: the archived JavaCPP Presets 1.5.11 Windows action explicitly installs
CRT, headers, libwinpthread and winpthreads package version
`10.0.0.r258.g530c58e17-1`, together with GCC 12.2.0-11. This resolves the earlier
absence of a recipe-level MinGW version. The full upstream revision is
`530c58e17adc5bfa1347a71453eca95b248ec683`; its checksum-locked source archive and
four original license documents are now collected. The winpthreads notice grants
MIT/BSD-style permissions and requires attribution; static use is not, by itself,
a requirement to publish EasyKnack under LGPL.

MSYS2 recipe/patch tree `a5f114dba5dcc0511d792b31dc4b713c91367ed7` contains
the matching `pkgrel=1` CRT and winpthreads recipes and is collected separately.
The previously collected GCC recipe tree has CRT `pkgrel=2`; it must not be used
as the exact CRT package recipe merely because it has the same source revision.

This is not a recovered release log. `ffmpeg.yml` uses
`bytedeco/javacpp-presets/.github/actions/deploy-windows@actions`, not the tag's
immutable action content. The upstream question below asks which revision actually
ran, or for the logged `pacman -Q` inventory. Keep the binary-to-recipe confirmation
open; do not label a source collection as a reproducible-build verification.

GCC's Runtime Library Exception matters: eligible combinations with independent
modules can be distributed under the independent modules' terms. Static GCC
runtime use is therefore **not automatically** a requirement to license an entire
application under GPL or supply the complete compiler source. Its conditions still
need to apply; collecting source is not a substitute for checking that distinction.
The complete exception text is included in the native notices.

Enabled FFmpeg dependency license, copyright, author and patent notice documents
are now copied from the locked source archives. `native-notices.json` records each
source member and copied SHA-256. NVIDIA header notices are the unchanged leading
copyright comments, explicitly marked as excerpts in that manifest. This adds
attributions; it does not make a general patent-clearance claim.

## Temurin and Microsoft runtime files

The release JSON and SBOM record OpenJDK commit
`520406d871955300957ef01e406ac2acd0f9b75c`, temurin-build commit
`e6ba7dec3d07654074559310376a3ae89da5f4ac`, the full configure/build arguments,
Visual Studio toolchain details, and the selected devkit. Third-party native
sources such as FreeType 2.14.3 are in the collected OpenJDK tree and their notices
are retained in the runtime's `legal` directory.

The unmodified official runtime also includes `msvcp140.dll`, `vcruntime140.dll`
and `vcruntime140_1.dll` version 14.40.33810.0, plus UCRT/API-set files version
10.0.26100.1742. These are Microsoft redistributables, not missing OpenJDK sources.
Microsoft describes separate redistribution conditions. Confirm the applicable
permission/terms for downstream distribution of this bundle; this review does not
infer them solely from Temurin's GPL notice. No runtime files were removed or
replaced, and no prerequisite installer was silently introduced.

Follow-up: Microsoft's official Visual C++ 2015–2022 Runtime **end-user** agreement
was inspected (EULA ID `Cpp_2015-2022_ENU.1033`). It permits installation/use, but
does not itself grant redistribution. Microsoft's redistribution documentation
points instead to the applicable Visual Studio terms and REDIST list. Therefore,
adding that runtime EULA alone would not resolve the outstanding permission chain.
The document URL and downloaded SHA-256 are recorded in `followup-evidence.json`.

Adoptium's public response in issue 1268 permits bundling its runtime in principle,
but does not specifically explain the Microsoft VC/UCRT terms for this release.
This is an unanswered documentation question, not a finding that Temurin cannot
be redistributed. No purchase or replacement runtime is established as necessary.
The prepared [upstream questions](UPSTREAM-QUESTIONS.md) request the relevant
permission and required notices. They have **not been submitted**.

If that route cannot be confirmed, a separately verified Visual Studio/SDK
redistribution route or installer download from Microsoft can be evaluated.
Either changes the packaging assumptions and requires installation testing;
silently removing DLLs from the working runtime is not a solution.

## Repeating the checks

Download the official ZIPs at the URLs in `official-archive-origins.json`, then run:

```text
python scripts/audit-native-archive.py OFFICIAL.zip INSTALLED_DIRECTORY report.json
python scripts/collect-native-notices.py
```

The first command only reads archive and installed files. It fails on changed,
missing or extra files and rejects unsafe/duplicate archive paths. The second
requires cached, checksum-locked sources, copies original notices and refreshes
the Help license index. Ordinary installer builds do not require Python.

## Primary references

- [LLVM-MinGW release](https://github.com/mstorsjo/llvm-mingw/releases/tag/20250305)
- [Pinned LLVM-MinGW build recipes](https://github.com/mstorsjo/llvm-mingw/tree/20250305)
- [Temurin release and SBOM](https://github.com/adoptium/temurin25-binaries/releases/tag/jdk-25.0.4.1%2B1)
- [Temurin Windows reproduction procedure](https://adoptium.net/en-GB/docs/reproducible-verification-builds/reproduce-windows-x64)
- [MSYS2 GCC recipe](https://github.com/msys2/MINGW-packages/blob/fd5380c8c48c5ab7cc9bc4189961da70f0317bed/mingw-w64-gcc/PKGBUILD)
- [GCC Runtime Library Exception](https://www.gnu.org/licenses/gcc-exception-3.1.en.html)
- [Microsoft redistribution conditions](https://learn.microsoft.com/en-us/cpp/windows/redistributing-visual-cpp-files?view=msvc-170)
- [Microsoft Visual C++ 2015–2022 end-user terms](https://visualstudio.microsoft.com/license-terms/vs2022-cruntime/)
- [Adoptium bundling response](https://github.com/adoptium/temurin-build/issues/1268#issuecomment-529919279)
- [Release-tree Windows action](https://github.com/bytedeco/javacpp-presets/blob/1.5.11/.github/actions/deploy-windows/action.yml)
- [Release-tree FFmpeg workflow](https://github.com/bytedeco/javacpp-presets/blob/1.5.11/.github/workflows/ffmpeg.yml)
