# Live interface comparison - 2026-11-13

Compared directly on the same Windows desktop:

- **EasyKnack 0.1.0**, build `local-20261113T092120.954884100Z-e77291dd`,
  commit `fcf85a5`, Core `bf17d1b6`. Identity was checked in About EasyKnack.
- **Blender 4.3.2**, installed Blender 4.3, Shading workspace / Shader Editor.

This is a qualitative visual review of representative live states, not legal
clearance, an originality certificate or an informed-user design-law opinion.
Different window sizes and node-canvas zoom levels prevent useful conclusions
from absolute pixel sizes. No similarity percentage or legal safe-distance score
was calculated.

## Finding

**The inspected detail styling is recognizably Blender-inspired, especially the
numeric fields/sliders and node presentation. The inspected complete application
window has a different composition and distinct branding.** It would be misleading
either to call the interfaces unrelated or to call the complete EasyKnack window
an identical Blender interface.

| Area | Direct observation | Visual assessment |
| --- | --- | --- |
| Complete workspace | EasyKnack has one large audio graph with an audio-oriented menu/status line. Blender has workspace tabs, multiple editor regions, a 3D viewport, Outliner, Properties and Shader Editor. | Strong visible difference in overall composition. The mere possibility of rearranging Blender is not a clearance conclusion. |
| Canvas | EasyKnack shows continuous major/minor grid lines; Blender's inspected Shader Editor shows a faint dot pattern. | Same dark graph-editor category, different visible background treatment. |
| Nodes | Both use dark bodies, colored title strips, sockets, curved connections and an active outline. EasyKnack has violet gradient heads and squarer contours; Blender has flat green/brown-red heads, more rounded contours and header collapse arrows. | Meaningful local resemblance with concrete differences; neither function nor original artistic expression is established by appearance alone. |
| Connections/ports | EasyKnack audio links are violet, with round audio sockets and other property/trigger shapes. The inspected Blender material graph uses round typed sockets and a green shader connection. | Shared node-graph mechanics; different implementation details in these views. Other Blender editors may use other socket shapes. |
| Numeric fields/sliders | Both place label and value in compact gray fields and use blue fill/focus feedback. | Closest observed local resemblance. This cluster merits the most attention if stronger visual independence is desired. |
| Numeric text editing | Double-clicking EasyKnack Gain and Blender Roughness exposes inline text entry. EasyKnack has a blue focus border/selection; Blender shows dark text entry in the same field position. | Closely related interaction concept, not a pixel-identical editor. No drag-value equivalence or exhaustive modifier-key test is claimed. |
| Menus/settings | EasyKnack uses dark flat menus with separators/right-aligned shortcuts and a separate language/theme dialog. Blender uses similarly dark menus and more densely icon-organized property areas in this workspace. | Some common conventions; different inspected organization. Blender Preferences was not paired against every EasyKnack dialog. |
| Branding | EasyKnack name and knob icon versus Blender name and orange Blender logo. | Distinct observed product identity. This does not replace a trademark search. |

## Observed states and evidence

EasyKnack: complete default graph, About, inline gain entry, node context menu,
language/theme settings and File/Examples menus. Blender: complete Shading
workspace, Principled BSDF numeric entry and the base-color popover (wheel,
brightness strip, RGB/HSV tabs and numeric/hex fields).

The EasyKnack color and curve editors were **not** opened in this pass, and the
attempt to open a larger example was interrupted by user input/focus changes.
No full paired color/curve-editor review is claimed. No audio was started and no
project/theme file was saved. The application review used a newly opened Untitled
document; it did not overwrite an existing user project.

Local, unedited screenshot evidence and a German side-by-side viewing page:
`dist/gui-comparison-20261113/index.html`. The three screenshot hashes and view
coverage are recorded in [machine-readable evidence](gui-comparison-2026-11-13.json).
Screenshots are kept as local audit artifacts, not uploaded with this review.
Blender interface imagery belongs to Blender Foundation and its contributors;
its presence is not an endorsement of EasyKnack.

## Practical design priorities

If stronger independent styling is wanted, prioritize:

1. **Numeric fields/sliders:** distinguish label/value placement, track/fill
   representation and focus/selection treatment. Changing their behavior is not
   necessary merely to change their visual signature.
2. **Node identity:** develop a recognizable header/body division or type marking
   consistent across node families. Keep useful node-graph interaction conventions.
3. **Accent and selection palette:** align it with EasyKnack's own branding rather
   than relying primarily on a similar blue-on-gray control combination.

These are design recommendations, not findings that each existing component is
unlawful, and not a promise that changing three details produces legal clearance.
This inspection alone does not justify a claim that a complete UI rebuild is
legally required.

## Legal boundary

Independent implementation of similar functionality must be distinguished from
copying protected expression. [CJEU C-406/10](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX%3A62010CJ0406)
addresses functionality/program expression. [CJEU C-393/09](https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=celex%3A62009CJ0393)
explains that an original GUI may be protected as a work, while technically
dictated components do not meet originality on that basis alone.

The comparison does not identify which particular Blender elements are protected,
search design/trademark registrations, assess every possible theme/editor or
determine infringement. A binding assessment should consider the actual screens,
authorship/source-origin evidence and relevant rights together. The original
[license preflight](LEGAL-PREFLIGHT-2026-11-13.md) remains applicable to those limits.
