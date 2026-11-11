# Documentation maintenance

## User-facing structure

The manual has twelve paired chapters under `docs/user/de/` and `docs/user/en/`:
start, signals, groups, instance-group, formula-group, spectral-group,
partial-group, spectral-synthesis, analysis, channels, midi and buffers.
`index.md` is the only chapter directory. Keep each explanation in its owning
chapter; references link it instead of repeating shared concepts.

Node short references live at `docs/nodes/<language>/<stable-slug>.md`.
The manual catalog `docs/nodes/catalog.json` maps stable registration identities
to slugs. Keep technical content manually authored in both languages; never infer
tables from processor metadata. Check owning Java/native behavior when changing
a documented contract. Both translations change together.

## Short references

Start with the title and one purpose sentence. Use these sections:

1. **Controls / Bedienung**: three columns Parameter, Setting/Einstellung,
   Signal / condition (Signal / Bedingung). Combine authored bounds, defaults and
   unit choices in Setting; signal type/range/rate and conditional visibility in
   the final column. Retain distinctions between slider and connected ranges.
2. **Outputs / Ausgänge**: two columns Output/Ausgang and Signal. Keep relevant
   range, type, rate and fixed channel information together.
3. **Behavior / Verhalten**: only node-specific details, surprising behavior and
   material limitations. Prefer a chapter link to another general explanation.
4. **Routing**: two columns Function/Funktion and Behavior/Verhalten. State Mute
   behavior, nonempty insertion mappings and supported sample feedback. Omitted
   insertion/feedback rows mean unsupported. No bypass does not promise silence
   or a zero resource: use the node-specific behavior and shared Mute explanation.

Do not restore separate Overview, Mute, Insertion and Delayable tables or eight
column port tables. Existing authored technical values were condensed, not
recomputed. Preserve old section anchors where possible. Old guide paths remain
small forwarding pages, not a second copy of the manual. New user documentation
must be complete offline and independent of Eclipse.

## Rendering and verification

Run `python scripts/build-docs.py`; output defaults to `target/docs/`.
The builder validates identities, paired pages, table shape, local links and
anchors. Rendering is generated; technical content is not. Never commit previews.
Java tests additionally check bundle coverage, native presentation and navigation.

Maven bundles maintained Markdown. Help → User guide opens the selected-language
manual. Node context help resolves stable identities. The viewer persists its
own language choice independently of application preferences, with English
fallback. Local history and asynchronous page loading remain per viewer.
Checkout runs verify their source location; installed runs use bundled resources.

## Remaining work

Plugin-provided references and generic text-selection/cache improvements remain
separate work. Do not copy implementation history into the user manual.
