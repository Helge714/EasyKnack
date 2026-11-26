# macOS experimental test 1: distribution review

Scope: `v0.4.0-macos-experimental.1`, Apple Silicon arm64 and Intel x86_64.
Build source commit: `b66fd3485b958c4886a83b2268eacf9c1c26fa31`.
[Build and audit run](https://github.com/Helge714/EasyKnack/actions/runs/45018029056).

This is a technical distribution review, not a legal clearance certificate.
Experimental status does not relax license requirements. Matching source access
is supplied beside the downloads, consistent with the
[GNU distribution guidance](https://www.gnu.org/licenses/gpl-faq.html#SourceAndBinaryOnDifferentSites).

Both architecture jobs passed all build/package checks and the distribution audit.
Each project snapshot verifies 1,782 source-file hashes; all 252 regular Java
legal files compare unchanged against the exact upstream runtime archive.

| DMG architecture | SHA-256 |
| --- | --- |
| arm64 | `2b4e3a25b2e456ce60bda6c3c5e5817e11c5d778950abf141e1ccf968649f170` |
| x86_64 | `641092f950414e80999a392ed1508a3617f1579103d57f99f1ea4076b2758821` |

## Materials and checks

- EasyKnack GPL-3.0-only sources, native dependency sources, local modifications,
  source hashes and build scripts are included in the exact project snapshot.
- External GLSwing Apache-2.0 build inputs are frozen, supplied and compiled on
  each runner; GLSwing remains independently licensed.
- Full matching OpenJDK sources, exact Temurin build sources, build metadata and
  architecture-specific SBOM accompany the bundled JDK and jpackage launcher.
- Java legal texts are compared byte for byte against the exact hash-verified
  upstream Mac JDK archive. App signing changes signature metadata; no claim of
  byte-identical signed runtime reproduction is made.
- Loris/Ooura, LAME, miniaudio, dr_mp3 and runtime JAR notices are retained.
  JogAmp/GlueGen/ANTLR supplemental release notices are present. JAR contents are
  inventoried and unreviewed Oculus native components are rejected.
- LAME remains a separate replaceable dylib, with its source/build configuration.
  Apple frameworks, compiler and SDK remain external system/tool prerequisites;
  no Apple SDK or Windows DLL is redistributed in the app payload.
- Each architecture has a separate `license-materials.zip` and JSON review. The
  ZIP contains the same project/dependency source materials, notices and Java
  legal tree as its app. `SHA256SUMS.txt` identifies every published payload.

The existing component terms are recorded in `THIRD-PARTY.md`. This review does
not resolve unrelated originality, trademark or design questions. Physical-device,
permission-dialog and interactive desktop acceptance remain pending; the builds
are ad-hoc signed, not Developer ID notarized.
