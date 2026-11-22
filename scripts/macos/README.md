# EasyKnack for macOS — experimental

Use the image matching your Mac: **arm64** for Apple Silicon, **x86_64** for Intel.
Requires macOS 14.2 or newer and working OpenGL. Drag `EasyKnack.app` to Applications
and launch it. All sixteen examples and German/English instructions are included.
Java is bundled. An unsigned/ad-hoc test build may be blocked by Gatekeeper;
Developer ID signing and notarization are separate from the build checks.

Native Formula compilation, Bake and source plugins require Apple's Command Line
Tools (or Xcode). Install them using Apple's `xcode-select --install` flow before
testing those functions. The Apple SDK is not redistributed. Ordinary formulas
retain their interpreter fallback. Compiler failures include a build log.

Microphone and system/application capture ask for permission only when selected.
Use System Settings > Privacy & Security to grant Microphone / Audio Capture
access, then refresh the device list. System capture excludes EasyKnack's host.
Copy capture leaves the original application's playback unchanged. Route mode
requires external routing. Device selections stored on Windows/Linux need changing.

Core Audio exposes consecutive hardware channel pairs. Mono ports are converted
to/from stereo; devices without hardware volume controls show volume unavailable.
Linked volume controls follow desktop default-volume changes, without a global
keyboard hook. Windows ASIO drivers are not used on macOS.

Run `Check-Mac.command` beside the app (or pass the installed app's path). It writes
a diagnostic log under `~/Library/Logs/EasyKnack`. Automated null-device, compiler
and rendering checks do not establish physical-device or long-duration stability.
Test device switching, recordings, physical MIDI/Learn, Formula/Bake, permissions,
Finder file opening, Quit with unsaved changes, Retina scaling and audio latency.

Licenses, exact project/Core sources, full OpenJDK sources and Temurin build
materials are in the app's `Contents/app/licenses` and `Contents/app/sources`, with
Java's legal documents under `Contents/runtime/Contents/Home/legal`. Keep matching
source materials available when redistributing this build. This is not legal
clearance or a tested stable Mac release.
