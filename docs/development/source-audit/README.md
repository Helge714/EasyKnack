# Reviewed source provenance

The [native distribution review](NATIVE-RUNTIMES.md) adds official archive
comparisons, Temurin SBOM/configuration, GCC/MSYS2 provenance, full compiler
component sources and native notices. It distinguishes resolved source collection
from the remaining FFmpeg builder details and Microsoft redistribution terms.

The FFmpeg outputs here were captured from the executable inside the standard
`ffmpeg-7.1-1.5.11-windows-x86_64.jar` in the reviewed Windows image. Its SHA-256
and the other exact JAR hashes are recorded in `scripts/sources.lock.json`.

Upstream sources:

- Bytedeco FFmpeg recipe and patches: https://github.com/bytedeco/javacpp-presets/tree/1.5.11/ffmpeg
- JavaCPP: https://github.com/bytedeco/javacpp/tree/1.5.11
- FFmpeg: https://ffmpeg.org/releases/ffmpeg-7.1.tar.bz2
- JogAmp JOGL / GlueGen: https://github.com/sgothel/jogl/tree/v2.6.0 and https://github.com/sgothel/gluegen/tree/v2.6.0
- ANTLR 2: https://www.antlr2.org/download/antlr-2.7.7.tar.gz
- LLVM-MinGW build recipes: https://github.com/mstorsjo/llvm-mingw/tree/20250305

The original license files from these Java dependency source releases supplement
missing license entries in their Maven JARs. They are included as application
resources under `easyknack/notices` and in the installed `licenses/upstream` folder.
JogAmp's referenced license documents are included as well.

LAME and the two AMR source archives were fetched from distribution mirrors after
SourceForge served HTML instead of archives. LAME SHA-256
`ddfe36cab873794038ae2c1210557ad34857a4b6bdc515785d1da9e175b1da1e`
and OpenCORE AMR SHA-256
`483eb4061088e2b34b358e47540b5d495a96cd468e361050fae615b1809dc4a1`
match the checksums on the upstream SourceForge download pages. All collected tar
archives were checked for valid archive contents before recording their hashes.

See [source package instructions](../SOURCE-PACKAGES.md) for the scope and unresolved
provenance checks. These observations do not certify complete source compliance.
