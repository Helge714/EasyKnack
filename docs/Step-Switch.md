> **Technical reference:** This page includes implementation details and may contain
> historical behaviour. For current usage, start with the [English user guide](user/README.md).

# Step Switch

Available under Routing. Starts on input 1 (Index 0). Set Steps to 1–32 directly in the node; unconnected inputs supply their slider values, so the same node works as a step sequencer.

Next advances once per trigger and wraps. Reset selects the first input and takes precedence over Next at the same sample. Unconnected trigger ports have manual buttons. Each input has a grouped slider and numbered selection button. While rendering, the active button uses the theme's pressed color. Clicking a selection button selects that step at the next block, taking precedence over Next but not Reset. Selection is transient and does not change the document or undo history. Connected inputs show their label instead of the slider, keeping the selection button available. Value outputs the selected input; Index outputs its zero-based index.

Auto uses audio rate when any trigger or audio input is connected, otherwise control rate. Inputs retain their own rates. An explicit control-rate override emits the value at block start and consumes later triggers for the next block. Switching audio is immediate, without crossfade.

Increasing Steps appends stable input ports. Decreasing it removes the last ports and their cables; undo restores them. Removing the active step selects the first. When dragging Steps, topology updates on release so the slider keeps mouse focus. Muting bypasses the first input to Value and silences Index.
