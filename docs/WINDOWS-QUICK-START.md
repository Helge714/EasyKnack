# Windows quick start

Install EasyKnack and choose whether to create Start Menu/Desktop shortcuts.
On the final page, keep **Check Java prerequisites and start EasyKnack** selected.
If Java is missing, EasyKnack offers to install Microsoft OpenJDK 25 through
Windows Package Manager, select an existing JDK folder, or cancel. The Java
provider handles its own installation and license prompts; Internet access and
administrator approval may be required. Without Windows Package Manager, follow
the official download instructions opened by EasyKnack.

Use a **Windows x64 JDK 25**, including `javac`, not a JRE or ARM64 JDK.
Java is installed separately and remains installed when EasyKnack is removed.
The C++ compiler for Bake and plugins is included in EasyKnack.

## Install or open

Run the installer and choose the installation folder. Desktop and Start Menu
shortcuts are independently selectable. Alternatively, open `EasyKnack.exe` in
the portable application folder. Keep that entire folder together; copying only
the EXE is not sufficient.

The audio host, media decoding and C++ tools for Formula, Bake and source plugins
are included. Java is installed separately as described above. These development
builds are unsigned.

The installer registers `.knack` documents. Double-click one to open it; if Windows
asks which application to use, select EasyKnack. An existing EasyKnack window
receives the document, including requests made during startup. Unsaved changes
are checked before switching documents.

## Make your first sound

1. Choose **File → New**. This creates an Oscillator connected to Audio Output.
2. In **Audio Output**, choose your physical speakers or headphones. Start with a
   low device volume: the initial graph generates a continuous tone.
3. Choose **Audio → Start**. Change the Oscillator's **Frequency** to hear the result.
4. Choose **Audio → Stop** when finished.
5. Choose **File → Save**, select a location and save a `.knack` document.

Add a **Gain** node between the Oscillator and Audio Output to adjust the signal
level within the graph. The output's **Device volume** controls the Windows device
and can affect other applications using it.

## Start building

Use **Nodes → Add Node** to find a node. Drag an output port to a compatible input
port to connect it. An unconnected numeric input uses its editor value; a connected
input uses its incoming signal. Select a node to see its properties in the Inspector.

**File → Examples** opens editable example documents. **Settings → Preferences**
changes the interface language and theme immediately. This guide uses English UI names.

## If you hear nothing

- Check that Audio is started and the nodes are connected.
- Verify the selected output device, its Windows volume and its mute state.
- Check that relevant nodes are not muted. Read the status line and graph warnings.
- After connecting a device, refresh devices in the Audio menu and select it again.
- A virtual cable is not a speaker. To listen through it, route its receiving side
  into EasyKnack and send the result to a physical output.

See [audio routing](user/audio.md) for microphones, system audio and virtual devices.
Capturing system or application audio does not, by itself, silence the original output.

## Keep your work

Save documents wherever you choose. Examples normally live in
`%USERPROFILE%/Documents/EasyKnack/Beispiele`; plugins and media caches use
`%USERPROFILE%/.easyknack`. Media files are not embedded in `.knack` documents.
Keep the source audio files when moving or backing up a project.

Close EasyKnack before replacing a portable installation or installing an update.
Keep document backups while testing development builds.

Continue with the [user guide](user/README.md), [node reference](user/nodes.md),
or [known limitations](user/limitations.md).
