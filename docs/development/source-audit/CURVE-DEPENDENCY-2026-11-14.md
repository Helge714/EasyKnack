# Curve control dependency update

Core commit: `2762bc2384147c111956a9e5a172f8b40a94a243`. Covered by the current 0.2.0-SNAPSHOT development version.

Curve points and Bezier handles render only on hover/focus; fixed positions lock X and topology. Point glyphs distinguish Bezier, Linear and Step. Phase grids support explicit column counts and edge-aligned plots. Existing library APIs remain compatible. No new third-party components or license changes. Full Core tests including enabled OpenGL glyph tests passed.

Reviewed local artifacts and matching build inputs:

- `glswing-0.2.0-SNAPSHOT-sources.jar`: `26ac2d65bbd562884bbb0d857aa5773289cb24ff6bc46496db748e4f3dd6e834`
- `glswing-0.2.0-SNAPSHOT-build-inputs.zip`: `a3da906bf689df54c52455b20d67ec87a6f8ad583b36bbdf6beffd9add7ead3d`
- `glswing-0.2.0-SNAPSHOT.jar`: `fe8390eb3a83e9ae9a7bd4e45e6772ad200048064fc637c6431840d279bd0a23`
