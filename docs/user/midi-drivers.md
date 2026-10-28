# MIDI editor drivers

[User guide](README.md)

MIDI drivers remotely operate sliders, choices, checkboxes and trigger buttons.
They change the same parameters as mouse editing. For sample-accurate musical
signals, continue to use MIDI nodes in the audio graph.

## Assign a controller

1. Right-click an editor and choose **MIDI Driver > Learn MIDI**.
2. Move the desired hardware controller or press a MIDI note. Sliders learn
   Control Change (CC) messages; other supported editors also accept Note On.
3. Learning records the device, channel and controller/note number. The learning
   gesture assigns the driver without immediately changing the parameter.

An assigned editor has a thin frame in the theme's MIDI color. The assignment
survives disconnected devices and is saved with the document. A graph connection
overrides its input editor: MIDI is ignored while that connection exists. Remove
the connection to reactivate the driver; ignored messages are not replayed.

Drivers also operate the exposed editors of groups. The group performs its
normal parameter forwarding to internal nodes. Internally coupled editors do not
accept a competing driver while controlled by the group.

## Behavior

| Editor | Notes | CC |
|---|---|---|
| Slider | Not assigned by note learning | Map 0–127 into the configured target range |
| Choice | Each Note On advances to the next entry and wraps | Divide 0–127 uniformly across all entries |
| Checkbox | Toggle on each Note On, or hold until Note Off | Off below the threshold, on at/above it |
| Trigger button | Fire on Note On | Fire when crossing the threshold upwards |

Note On with velocity zero is Note Off. MIDI Clock and unrelated message types
are ignored by learning. Trigger edges and note ordering are preserved when
continuous controller updates are coalesced.

## Mapping

Choose **Configure MIDI driver** from the editor's driver menu. The assignment
also appears in the node's Properties inspector. Numeric mappings provide target
minimum and maximum, reversed direction, logarithmic interpolation (positive
ranges only), and **Pickup**. Mapping endpoints use the parameter's stored unit,
as indicated by the labels; a display-unit change does not silently change them.
Integer editors round their driven values.

Direct mappings update the target with the next incoming CC. Continuous sliders
can interpolate the rendered value; see [Manual control interpolation](manual-controls.md). Pickup waits until the
hardware controller reaches or crosses the current software value. Connecting an
input suspends pickup; after disconnecting it must acquire the value again.

MIDI works while audio is stopped, provided the host is available. Triggering an
audio action while playback is stopped does not queue it for a later start.

## History and saving

Creating/removing a driver and editing its mapping are ordinary undoable edits.
Incoming MIDI values and actions never create Undo entries. Changed parameter
values still mark the document modified and are included when saving. Graph Undo
preserves the latest driven values while the assignment remains present. Manual
parameter edits retain their normal Undo behavior.
