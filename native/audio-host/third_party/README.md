# miniaudio

Vendored unmodified miniaudio 0.11.21 from
https://raw.githubusercontent.com/mackron/miniaudio/0.11.21/miniaudio.h

Copyright David Reid. The complete public-domain/MIT-0 licensing options are
included at the end of miniaudio.h. EasyKnack uses the MIT-0 option.
Used for audio-device I/O, WAV import and sample-format/rate conversion;
EasyKnack owns the DSP graph. MP3 import uses the backend below.

SHA-256: `6b2029714f8634c4d7c70cc042f45074e0565766113fc064f20cd27c986be9c9`.

# dr_mp3

Unmodified dr_mp3 0.7.4, used only for gapless MP3 import. The device backend
continues using the miniaudio version above.

- Upstream: https://github.com/mackron/dr_libs
- Commit: `aec2dd20667f54acfdb66031217df6d240f56c52`
- File: https://raw.githubusercontent.com/mackron/dr_libs/aec2dd20667f54acfdb66031217df6d240f56c52/dr_mp3.h
- SHA-256: `f5c8020eeebb79877184432efdd2651f24869f5bb8f379d45450fd0aed1ee915`
- Copyright David Reid; EasyKnack selects the MIT-0 option. Embedded minimp3
  code carries the CC0/public-domain dedication retained at the end of the header.
  The full upstream license section is also bundled with Windows packages.

# Loris

Partial analysis uses a pinned subset of the original C++ Loris library.
See [provenance, local access patches and license](loris/PROVENANCE.md).
COPYING (GPL-2.0-or-later, GPLv3 option), AUTHORS and the bundled Ooura FFT
permission statement are included in SDKs and Windows packages. File hashes are
recorded in loris/SHA256SUMS.json; no FFTW or Python dependency is introduced.

# Linux desktop APIs

`alsa/` and `pulse/` contain upstream ALSA 1.2.14 / PulseAudio 17.0 public headers,
license texts and exact source URL/SHA256 provenance. Runtime libraries are supplied by
the Linux system and loaded dynamically. Pulse `version.h` is instantiated from
the upstream template; other public headers remain unchanged.

# LAME

`lame/` contains the unmodified LAME 3.100 portable encoder sources, public header,
COPYING (LGPL-2.0-or-later) and source URL/SHA256 provenance. The EasyKnack config
is separate in `lame/easyknack/config.h`. Build only the portable encoder as a
replaceable shared library; no upstream decoder, assembler or SIMD sources are
compiled. The application uses this for Linux MP3 recording; Windows retains
Media Foundation. The complete retained sources/configuration accompany packages.
