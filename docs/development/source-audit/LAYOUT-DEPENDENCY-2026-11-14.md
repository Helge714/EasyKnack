# Layout dependency update

Core commit: `292db6a3f2787b8ae203c0fb4dd3727682dc9dcb`. The existing 0.2.0-SNAPSHOT development version covers this compatible feature and correction.

Position-aware layout uses original vertical branch order with signal-flow ranks and port-aware compaction instead of a global position grid. Independent components retain rough placement. The compact layout API remains available. No third-party components, licenses or document formats changed. The full Core suite passed (three gated skips), including long diagonal chains, offset parallel branches, nearby controls, mixed graphs, feedback and bounded large-graph tests.

Reviewed local Maven artifacts and matching build inputs:

- `glswing-0.2.0-SNAPSHOT-sources.jar`: `21f89c2c54dce527e1691810d42faba463f15f7bd6e5c3b450f1a6b46250488b`
- `glswing-0.2.0-SNAPSHOT-build-inputs.zip`: `078b4a5bb62041203ac3c587f7b02a794e317714f65109ce3bd67a799a9af8d6`
- `glswing-0.2.0-SNAPSHOT.jar`: `ef6411879ff10d0f3a4e901c2cb2166f03ef05952c5a7db46e6d4aa15d4fb7a6`
