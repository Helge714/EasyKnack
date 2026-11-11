# Core capture-font correspondence — 2026-11-11

GLSwing remains an external `com.vingame:glswing:0.2.0-SNAPSHOT` dependency.
The new clean Core commit is `fc1261bbb03fe5d6c90cfd912439822c08b8cce5`.
Its compatible fix is covered by the existing development version.

A probe against the first packaged candidate reproduced a user-visible defect:
the first label capture contained 663 nonblack pixels and the second contained
zero. Offscreen captures reused font textures owned by a destroyed GL context.
The fix resolves fonts in a per-capture scope, disposes them in that context and
restores normal resolution afterwards. Source hierarchy, geometry and live-window
font ownership are preserved. The first candidate `dist/rc-20261111-dc45cbf` is
marked rejected, while its original binary/source bytes remain available.

All 172 Core tests pass with OpenGL enabled, with no failures/errors/skips. Both
new repeated-capture assertions and existing alpha/error/size-limit checks pass.
The runtime, sources, parent/build inputs, reports and prior cached artifacts
are retained under `dist/glswing-capture-20261111`.

The exact build-input ZIP captures 774 tracked source/build files, excluding IDE,
cache and agent-context folders. An independent build from that ZIP matches all
962 runtime classes and every runtime resource. Only generated build identity and
Maven properties differ; whole-JAR byte reproducibility is not claimed. All 532
source-JAR Java/resource entries match the captured source bytes.

[sources.lock.json](../../../scripts/sources.lock.json) is updated only after this
comparison. The [machine-readable review](glswing-capture-2026-11-11.json) records
all new hashes. Existing frozen artifacts and previous source-cache files are
preserved. No document format, plugin ABI or audio protocol change is involved;
no release tag, push or publication was performed.
