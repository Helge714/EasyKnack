# Curve control dependency update

Core commit: `559db93803ecde6eb0f5717074ce84becd5f7813`. Covered by the current 0.2.0-SNAPSHOT development version.

Curve points and Bezier handles render only on hover/focus; fixed point positions lock X and topology for phase editors. Curve data and existing library APIs remain compatible. No new third-party components or license changes. Full Core tests and focused hover/focus/drag tests passed (platform-gated tests remain skipped).

Reviewed local artifacts and matching build inputs:

- `glswing-0.2.0-SNAPSHOT-sources.jar`: `b9380562d1039ea3d2162492f00ba6fd5b57c2ca8226363ed40ddd136de59c07`
- `glswing-0.2.0-SNAPSHOT-build-inputs.zip`: `ddf222e04dc45fc218964907188b3238bb9ca2c713e08d8f2e55425c69b57e33`
- `glswing-0.2.0-SNAPSHOT.jar`: `dc7e3cda256d20a873a4654ef437b3d187c938e3f3db3f58a4050098b43ddef7`
