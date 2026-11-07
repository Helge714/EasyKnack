# Loris source provenance

- Upstream: https://github.com/kellyfitz/loris
- Commit: eccd42ac847b08769643b0d0c36442ef0fd27f38
- Retrieved: 2026-11-06; pinned upstream development snapshot (CMake project version 2.1).
- Copyright 1999–2026 Kelly Fitz and Lippold Haken.
- License: GPL-2.0-or-later. EasyKnack uses the GPLv3 option. See COPYING.
- Only the analysis dependency subset is compiled; unused public headers are
  retained for upstream include compatibility. No Python, SWIG, AIFF/SDIF I/O,
  fast synthesizer or FFTW runtime is required.
- The bundled fftsg.c is Loris' adaptation of Takuya Ooura's split-radix FFT;
  its original header and the author's permission notice are retained.

## Local integration changes

Analyzer.h and PartialBuilder.h each add a friend declaration for
Loris::EasyKnackStreamingAnalyzer. No upstream analysis formula is modified.
The bridge in src/LorisStreamImplementation.hpp executes Analyzer::analyze's
reassignment, selection, thinning, bandwidth and PartialBuilder stages per frame.
It bounds historical storage and adapts phases/resources to EasyKnack's clock.
Offline end-of-file frequency/phase correction and envelope builders are not run.
The unity build in src/nodes/LorisLibrary.cpp defines M_PI for Windows and
renames translation-unit-local Pi constants and Partial.cpp's private wrapPi to
avoid collisions. Original source algorithms remain unchanged. Text line endings are normalized
to LF for reproducible hashes across Git checkouts.

See docs/development/LORIS.md for the real-time adapter and verification limits.
