# Step Switch

Connect the values to switch between to the numbered inputs. Set **Steps** to the
number of inputs required. Connected inputs can carry audio or control values.

Choose **Data type** in the inspector: **Value**, **Vector**, **Wavetable**,
**Buffer**, **Spectrum** or **Analysis**. All numbered inputs and the selected
output share that type; Index always remains numeric. Only Value inputs have
sliders. Resource inputs may be left empty; selecting one supplies no resource.
For resources, click an input label to rename it, and use its numbered button to select it.

Resource selection is held for one DSP block. A trigger at block start applies
immediately; later triggers affect the next block. The Index output reports the
resource selected for that block. Vector resources retain their audio/control
rate, including their per-sample values. Resources are forwarded without copying;
all connected sources still run. This is a hard switch: changing FFT window size,
hop size or source timing before an IFFT can create a gap or audible jump. Use
aligned sources when continuous reconstruction matters. MIDI is not supported.

In the inspector, choose **Selection**:

- **Trigger**: each **Next** trigger advances one step and wraps at the end.
  **Reset** selects the first step and takes priority over simultaneous Next
  triggers. Click a numbered button to select that step manually.
- **Index**: supply a zero-based **Selected index**, or set it with its slider.
  Fractional indices round down; values outside the available steps are clamped.
  The numbered buttons are disabled while index selection controls the node.

Right-click an input slider and choose **Rename input** to edit its name directly
in the node. When the input is connected, click its displayed label instead.
Enter or leaving the field commits the name; Escape cancels. The inspector's
**Step name 1**, **Step name 2**, etc. fields remain available. An empty name
restores the translated default. Names do not change connections.

The selected step is saved in the document. Numbered buttons, Next and Reset
also work while audio is stopped. Trigger-driven selection is remembered without
adding runtime activity to Undo history. External index connections take priority
in Index mode; the numbered buttons remain disabled in that mode.

**Value** outputs the selected input; **Index** reports its zero-based index.
**Auto** uses audio rate for connected triggers or audio inputs; otherwise it
uses control rate. Control rate samples the selection at block start. Immediate
audio switching can click; crossfade between signals if smooth switching is needed.

Inside an Instance Group, each instance keeps its own selected step. Connect
instance-dependent triggers or indices to switch them independently. The visible
highlight and saved selection represent the first instance; in nested groups this
is the first instance at every level. Manual selection applies to all instances.
The Index output still reports each instance's own selection. This also works
with Bake and exported groups.
