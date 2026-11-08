> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](../../user/README.md).

# Trigger Counter and Trigger Filter

Each trigger stream contains at most one trigger per sample. Adjacent samples can
each contain a trigger. Simultaneous triggers are coalesced within a stream;
separate MIDI voices retain separate streams. MIDI messages themselves are never
coalesced. Pending manual button presses in one audio block produce one trigger
at sample zero. Numeric value events remain unchanged.

Trigger Counter outputs a held audio-rate **Index** and the original **Trigger**.
The first trigger has index 0, the next 1, etc. Index is updated at the trigger's
sample, so downstream Math/Comparator nodes see the new value. Reset sets the
index to 0 without emitting a trigger; the next trigger also has index 0. Reset
takes precedence when both inputs trigger at the same sample.

With Wrap enabled, Count selects the number of indices (Count 4: 0, 1, 2, 3, 0).
Count is a control input. Without Wrap, the counter saturates at 16777215, the
largest index before integer increments cease to be exact in the float format.

Trigger Filter samples Condition at each incoming trigger and passes the trigger
only for finite values greater than zero. Other values discard the trigger;
changing Condition alone never creates one. Condition accepts Audio or Control.

Example: Clock → Counter. Counter Index → Comparator A, operation `==`, B = 2.
Counter Trigger → Filter Trigger; Comparator Out → Filter Condition. This passes
index 2 (the third trigger). Enable Wrap with Count 4 to repeat every four triggers.
Leave the Comparator rate on Auto/Audio; forcing Control samples once per block.

Both nodes appear under Modulation and bypass the trigger path when muted.
