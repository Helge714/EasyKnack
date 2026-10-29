# Step Switch

Connect the values to switch between to the numbered inputs. Set **Steps** to the
number of inputs required. Connected inputs can carry audio or control values.

In the inspector, choose **Selection**:

- **Trigger**: each **Next** trigger advances one step and wraps at the end.
  **Reset** selects the first step and takes priority over simultaneous Next
  triggers. Click a numbered button to select that step manually.
- **Index**: supply a zero-based **Selected index**, or set it with its slider.
  Fractional indices round down; values outside the available steps are clamped.
  The numbered buttons are disabled while index selection controls the node.

Set **Step name 1**, **Step name 2**, etc. in the inspector to label the inputs.
An empty name restores the translated default. Names do not change connections.

**Value** outputs the selected input; **Index** reports its zero-based index.
**Auto** uses audio rate for connected triggers or audio inputs; otherwise it
uses control rate. Control rate samples the selection at block start. Immediate
audio switching can click; crossfade between signals if smooth switching is needed.
