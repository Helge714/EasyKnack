# Background font preparation — 2026-11-12

GLSwing commit `bf17d1b62f14eefb6927afbcdfafa1c07c05349e` keeps font rasterization outside the shared CPU atlas
cache monitor. Cache lookup/publication remains synchronized, and texture ownership
remains per context. The current `0.2.0-SNAPSHOT` covers this compatible fix.

Three focused font tests pass, including a blocked-raster concurrency regression
that proves other threads can acquire the shared cache monitor. Maven installation
succeeded. All 473 Java source-JAR entries match the clean checkout. Runtime
class changes are confined to `GLFont` and its five nested classes; other changed
entries are generated build metadata. No new full Core suite was run.

The clean tracked build-input snapshot contains 775 files.
Matching runtime, sources, build inputs and comparison details are retained under
`dist/glswing-background-20261112-bf17d1b`; current source/runtime locks and disposable source caches refer
to these bytes. Earlier candidates and their evidence remain unchanged.

Runtime SHA-256: `07d223a840521cdd96f5465a9116720a27475e4f21a2de92faf58e82c8afa609`.
