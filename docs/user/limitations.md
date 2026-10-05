# Known limitations

[User guide](README.md)

## Installation and portability

- The packaged build targets Windows x64. Linux/macOS packages are not currently
  validated distributions.
- Development installers are unsigned. A clean-Windows installation, upgrade and
  uninstall acceptance test is still pending; successful use on the development
  machine does not replace that check.
- Virtual audio routing drivers/tools are not bundled. Copy capture does not suppress
  the source application's original output.
- `.knack` documents do not embed referenced audio files. Keep originals and verify
  references after moving a document. Media caches and source-plugin build caches
  are local; a document is not a self-contained media/plugin distribution.
- Source plugins need compilation. Packaged builds include the tools; ready-made
  binary plugin distribution is not the current installation format.

## Graphs and processing

- Bake can improve some graphs but is not a universal performance guarantee.
- Export of coupled standalone sliders, checkboxes and trigger buttons is not fully
  supported. Unsupported combinations are rejected; test the interface before
  distributing a group plugin. Live groups and supported coupled comboboxes remain usable.
- Instance Groups have limits on capacity and supported nesting/resources. Use the
  palette and graph diagnostics to check a particular configuration.
- Spectral processing currently uses a single spectrum source at the group interface.
  Frame-domain results and generated audio need their corresponding output mechanisms.
- Asynchronous FFT can omit frames. Use synchronous FFT when continuous IFFT output matters.
- Feedback needs a protecting delay. Not every processor supports short sample-feedback
  loops. Muting the only delay can disable an unsafe return connection with a warning.
- Audio File Player supports mono/stereo media; multichannel files are not supported.
  Speed changes pitch and is not independent time stretching.
- Level Detector is Peak/RMS, not LUFS. Analysis export is not a calibrated measurement
  certificate; reception timestamps are not acquisition timestamps.

## Reporting a problem

Include the application/build information, relevant status message, selected audio
backend/device and steps to reproduce. A small `.knack` document is useful; mention
any required plugins or external media. For performance/audio-delivery issues, attach
a report from Render diagnostics and distinguish audible glitches from the load display.
