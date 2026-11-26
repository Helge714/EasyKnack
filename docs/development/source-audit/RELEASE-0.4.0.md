# EasyKnack 0.4.0 release - 2026-11-26

[Public release and downloads](https://github.com/Helge714/EasyKnack/releases/tag/v0.4.0).
Source commit `2afd7179adb68eb256de7f8b6bac4eb3ccc28e00`, immutable tag `v0.4.0`.
The Windows x64 installer is unsigned and uses an external x64 JDK 25.
Development continues at `0.4.1-SNAPSHOT`; the external Core dependency is the
checksum-pinned public GLSwing 0.3.0 SDK.

## Validation

- Full application suite against the frozen release: 828 cases, zero failures/errors,
  167 explicitly gated skips. OpenGL and the new native dynamics/Freeverb tests ran.
- Six native regression executables passed: WASAPI callbacks/volume COM lifetime,
  simulated ASIO, graph/feedback/channel kernels, shared transport including crashes,
  driver bridge and dynamics/allpass DSP. The matching host also built successfully.
- The exact packaged application passed document/help/image/media/null-audio checks,
  Formula compilation, group export, Java/C++ plugins and restart-cache acceptance.
  Its actual launcher opened a responsive workspace and closed normally.
- All sixteen bundled examples and both language guides were validated.
- Core: 247 tests, zero failures/errors, two gated skips; external consumer and
  isolated SDK installation passed. Its 975 runtime class entries match the tested
  snapshot. Public SDK download/bootstrap was additionally checked after publication.
- Project sources: 1839 files compared byte-for-byte with the frozen source tree.
  Source materials report complete source review, zero missing sources and zero
  unreviewed JARs. Uploaded asset digests match the local SHA-256 manifest.

The initial packaging attempt received a nonmatching SourceForge download for
LAME. The pinned archive was obtained from Debian with the identical expected
SHA-256; no checksum was relaxed. Packaging resumed from the unchanged image.
The strict source command also reports the pre-existing distribution/manual gates;
source review itself passes. No clean-system/hardware acceptance was invented.

## Distribution and remaining acceptance

The release includes the installer, project sources, source materials, release notes,
validation JSON and checksums. Sources and binaries remain fixed to the same build.
The source-materials REVIEW.json preserves the pre-publication distribution checks;
matching downloads are now published. Clean-machine Java setup, upgrade/uninstall,
associations and new physical WASAPI/ASIO/MIDI acceptance remain open. Linux/macOS
remain separate experimental downloads. Official release CI/signing are not claimed.

Newly saved documents use schema 11; keep copies before saving projects intended
for older readers. Source plugins may rebuild after updating. Limiter latency equals
its selected lookahead; it is a sample-peak limiter, not oversampled true peak.

[Machine-readable validation](https://github.com/Helge714/EasyKnack/releases/download/v0.4.0/validation.json)
and [checksums](https://github.com/Helge714/EasyKnack/releases/download/v0.4.0/SHA256SUMS.txt)
accompany the downloads.
