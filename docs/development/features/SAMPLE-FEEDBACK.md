> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](../../user/README.md).

# Universal Delay and feedback

The Add menu exposes one **Delay** node. Its audio input and output use Audio rate;
Time is sampled once per block and supports seconds, milliseconds and samples.
The range remains 0 to 10 seconds so existing ordinary delays keep their values.
Zero is bypass in an acyclic graph; feedback clamps it to at least one sample.
Mute bypasses the delay. If that removes the only protecting delay in a cycle,
the editor shows a warning only on the connection needed to break that unprotected
cycle, preferably the return connection leaving the muted Delay. The rest of the
chain and its side branches remain active. Overlapping cycles are checked again
after each cut; further connections are disabled only while unsafe cycles remain. Inputs use their editor fallback (or silence); independent branches continue.
The wires remain saved and become active again when the loop is repaired. The same
policy applies to other unprotected cycles. Unsupported processors in otherwise
protected sample-feedback loops remain errors.

Without feedback, even a short delay runs as an ordinary block processor. In a
feedback island the host latches all delay times at the start of the actual block.
It chooses the minimum whole-sample delay across channels, voices and boundaries,
capped at the current block length. A four-sample delay therefore permits chunks
of four samples; a delay of at least a block permits a whole block. Fractional
sample delay uses linear interpolation and rounds the scheduling limit down.
All delayed outputs in a chunk are read before any delay commits its new inputs.

Overlapping loops form one strongly connected island. Shared nodes execute once
per chunk, and the shortest delay sets the common step size. Disjoint islands
choose independently. External inputs are retained until their island completes.
No memory is allocated when the delay time or chunk size changes in the callback.
Time controls must originate outside the island: a block's schedule cannot depend
on audio which that same schedule has yet to compute. Rapid time changes can still
produce discontinuities; this is not an artifact-free time-stretch effect.

Audited chunk kernels: Math, Mix, Gain, Unit Converter, Filter, Map Range, Clamp,
Curve/Mapping, Pan, Oscillator, Complex Oscillator, Noise and Digital Noise.
Control outputs still run once per block. Digital Noise preserves trigger offsets.
Custom wavetables must be produced outside a short-step feedback island.
Block-only processors can participate when all feedback delays have a fixed,
unconnected Time of at least one prepared block. Shortening such a delay requires
a new validated plan; unsupported short-loop configurations are rejected safely.
A change of device preparation format also checks this lower bound.

Old Sample Delay and Block Delay remain registered only for compatibility, hidden
from the catalog. Opening documents migrates them to Delay with 1 or 256 samples,
preserving IDs, placement, wiring and group port endpoints. Values are stored in
seconds with Samples selected as display/input unit. Existing ordinary Delay IDs
and values are unchanged; connected Time is now sampled once per block.

Feedback groups can now be baked and exported, including nested groups and legacy
Sample/Block Delay definitions. Generated processors reuse the live graph's
read/compute/commit scheduler for feedback, preserving its chunk size and state
transfer. Acyclic exports keep their existing fusion/specialization paths.
Feedback exports currently prioritize identical scheduling over loop fusion;
exporting them is not a promise of a speedup. An opaque exported node cannot itself
close a new external sample-feedback cycle: keep the complete loop inside the
exported group. The normal host validation rejects unsupported external cycles.

Regression tests cover fixed and fractional delays, zero bypass, partial blocks,
adaptive step sizes, block-rate modulation, simultaneous delay commits, shared and
separate islands, stereo, MIDI voices, state transfers, block-only validation,
Java document migration and block/sample kernel equivalence. Native timing tests
are CPU measurements, not claims about device latency.
