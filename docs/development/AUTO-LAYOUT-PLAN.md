# Compact graph layout proposal

The next layout revision should optimize readability first, occupied area second,
and wire length third. Shortening wires alone can obscure the signal flow. This
is a proposal; it does not change the current Arrange command.

1. **Stable flow direction.** Place sources on the left and sinks on the right.
   Identify strongly connected components before ranking nodes. Give feedback
   components a local layout and a clear return route instead of arbitrarily
   treating each feedback edge as another forward constraint.
2. **Port-aware alignment.** Minimize distance between the actual output/input
   anchors rather than node centers. Preserve declared port order. Align a main
   signal path; place parameter/modulation sources beside the ports they drive.
   Treat group Input/Output nodes as boundaries at the outer edges.
3. **Compact columns.** Allow independent chains to share vertical space where
   their occupied rectangles do not overlap. Pack disconnected components into
   shelves near the current aspect ratio rather than one tall vertical stack.
   Theme horizontal/vertical spacing remains the minimum clearance.
4. **Local improvement.** Try adjacent row swaps and bounded column compaction.
   Score crossings, backward-wire intrusions, total anchor distance, bends and
   occupied area. Accept improvements only; use deterministic tie breaking.
5. **Predictability.** Repeating Arrange must preserve the result. A topology edit
   should not randomly reorder unrelated branches. Keep camera and zoom unchanged;
   retain one Undo operation including boundary-node locations.

Before replacing the algorithm, keep fixed test graphs for a long chain, many
short chains, a fan-in mixer, shared modulation, different node heights, nested
groups and feedback components. Compare crossings, total anchor distance and
bounding area, and check overlap/spacing plus repeat stability. Offer a balanced
default first; compact and wide presentation presets can follow if useful.

Implementation ownership: Core `GLNodeAutoLayout`, with fixtures exercising real
EasyKnack node/port sizes. Avoid special cases for named audio processors.
