# Compact graph layout

Core `GLNodeAutoLayout` provides compact layout and the position-aware Arrange
command. It snapshots node sizes and actual port anchors, then changes locations only. It has no audio-node
name/type special cases or external solver dependency.

1. **Stable flow direction.** Iterative graph traversals find strongly connected
   components without recursive call-stack limits. Each feedback region stays a
   contiguous block; return vertices use a lower lane. The acyclic graph of blocks
   is ranked left to right. Existing wire curves are retained; no new wire-routing
   renderer is introduced.
2. **Port-aware alignment.** Median ordering and weighted isotonic compaction use
   actual input/output anchors and retain port order. Short source branches move
   close to their first consumer. Narrow branches do not inherit the maximum width
   of unrelated columns where safe compaction is possible.
3. **Boundary roles.** `GLNode` declares Normal, Input boundary or Output boundary
   roles. Workspace group boundaries set these hints; other nodes use Normal by
   default. Boundaries remain at the outer edges, including unconnected ones.
   Boundary-connected branches keep separate lanes so their wires do not pass
   through unrelated components to their left.
4. **Compact packing.** A bottom-left skyline fills free vertical space beside
   taller independent components. Several candidate widths balance occupied area
   and the viewport aspect ratio. Theme horizontal/vertical spacing remains the
   minimum clearance between node rectangles.
5. **Bounded improvement.** Eight ordering sweeps retain the best measured crossing/
   anchor-distance score. Small components get at most 128 adjacent row-swap trials;
   large edge sets use a deterministic sample for crossing estimates. Skyline
   interval maxima use a sliding queue. This is a heuristic, not a globally optimal
   crossing or area solver.
6. **Rough arrangement.** Workspace Arrange uses `arrangePreservingOrder`.
   Original vertical order guides branches within each flow column. Columns come
   from connections, not original X offsets; port-aware compaction reuses vertical
   space between columns and moves short sources near consumers. Independent
   components retain rough rows and left-to-right order. Feedback blocks and
   boundary lanes remain structural constraints. This favors branch order over
   crossing minimization without imposing a global grid. `arrange` retains fully
   automatic crossing reduction and compact component packing.
7. **Predictability.** Ties use stable editor node order. Repeated Arrange is
   idempotent for unchanged geometry and viewport.
   `GLNodeWorkspace` retains one Undo operation including boundary locations.
   Graph properties/wires, node sizes, camera and zoom remain unchanged.

Validation lives in Core `GLCompactAutoLayoutTest` (port offsets, mixed sizes,
crossings, feedback, boundaries, free-space packing, long chains and bounded work)
and EasyKnack `AutoArrangeTest` (real application views, persistence, shortcut and
Undo). Sources stay at the left of their consumers; this need not place every
parameter source in the globally leftmost column.

The default preserves rough placement. Pinned nodes, selectable packing presets and
dedicated orthogonal feedback routes remain possible future extensions.
