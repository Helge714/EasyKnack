# EasyKnack user guide

This guide describes the English interface of the Windows application. Choose
English in **Settings → Preferences** if you want menu names to match the guide.
Open this offline guide from **Help → User guide (English)**. **Help → About
EasyKnack** shows the application and GLSwing build identities; the text is
selectable for copying into a bug report. License notices are available in the
same menu.

Choose **Help → Node reference** for the active node's page, or right-click a node
and choose **Documentation**. With no active node, Node reference opens the index.
**Help → Node index** always opens the full directory in the selected documentation
language.
References open in a themed EasyKnack dialog and work offline. Group instances open the matching
ordinary, Instance, Spectral or Formula Group reference. The Documentation action
is disabled when no reference is available. In the documentation window choose
**Documentation language → Same as application / Deutsch / English**. This choice
is saved independently of the application language and applies to references,
indices and local links when help is opened again. The English workflow handbook
and other untranslated pages remain in English; the status line identifies this
fallback without changing your preferred language. A reference's language link
also selects and remembers that language.
Follow inline links within the dialog; Back/Forward (Alt+Left/Right) restore the
previous page and scroll position. Wide tables scroll horizontally. Copy text
copies the page's text, and Escape closes the dialog.

| Task | Read |
|---|---|
| Understand rates, data types, units and muting | [Fundamentals](en/index.md) · [Deutsch](de/index.md) |
| Edit, save and share a graph | [Workspace and documents](workspace.md) |
| Select devices or process another application's audio | [Audio and routing](audio.md) |
| Find a node, its ports and inspector settings | [English node index](../nodes/en/index.md) · [Deutsch](../nodes/de/index.md) |
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
