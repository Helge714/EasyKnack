# EasyKnack user guide

This guide describes the English interface of the Windows application. Choose
English in **Settings → Preferences** if you want menu names to match the guide.
Open this offline guide from **Help → User guide (English)**. **Help → About
EasyKnack** shows the application and GLSwing build identities; the text is
selectable for copying into a bug report. License notices are available in the
same menu.

| Task | Read |
|---|---|
| Edit, save and share a graph | [Workspace and documents](workspace.md) |
| Select devices or process another application's audio | [Audio and routing](audio.md) |
| Find a node or understand rates and units | [Node reference](nodes.md) |
| Apply random frequency modulation | [Digital Noise](digital-noise.md) |
| Smooth mouse and MIDI control changes | [Manual control interpolation](manual-controls.md) |
| Control editors with MIDI hardware | [MIDI editor drivers](midi-drivers.md) |
| Build a reusable interface, Formula or plugin | [Groups and custom processing](groups.md) |
| Process spectra or inspect measurements | [Spectrum and analysis](analysis.md) |
| Check portability and feature boundaries | [Known limitations](limitations.md) |

For a first sound, choose **File → New**, select a physical device in **Audio Output**,
set a low output volume and choose **Audio → Start**. The Oscillator is already connected.

A *document* contains the graph and its groups. A *node* processes or supplies data.
A *port* is a connection point; an *editor* is a slider, checkbox or other control.
The *Inspector* shows the selected node's settings. The *audio host* is the process
that calculates and plays the graph; stopping audio leaves the editor available.
