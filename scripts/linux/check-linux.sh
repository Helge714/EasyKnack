#!/usr/bin/env sh
# Run from an extracted Linux test package, with no changes to the system.
set -u
base=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
LD_LIBRARY_PATH="$base/native${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
export LD_LIBRARY_PATH
log="$base/linux-diagnostics.txt"
result_file=$(mktemp "${TMPDIR:-/tmp}/easyknack-check.XXXXXX")
trap 'rm -f "$result_file"' EXIT
trap 'exit 130' HUP INT TERM
(
  status=0
  echo 'EasyKnack Linux test package'
  date -u
  uname -a
  if [ -r /etc/os-release ]; then cat /etc/os-release; fi
  printf 'DISPLAY=%s WAYLAND_DISPLAY=%s\n' "${DISPLAY:-}" "${WAYLAND_DISPLAY:-}"
  printf 'XDG_SESSION_TYPE=%s XDG_CURRENT_DESKTOP=%s\n' "${XDG_SESSION_TYPE:-}" "${XDG_CURRENT_DESKTOP:-}"
  echo '--- Displays (optional X11/XWayland information) ---'
  if command -v xrandr >/dev/null 2>&1; then xrandr --listmonitors 2>&1 || true; fi
  echo '--- Java ---'
  "$base/runtime/bin/java" -version || status=1
  "$base/runtime/bin/javac" -version || status=1
  "$base/tools/zig/bin/clang++" --version || status=1
  echo '--- Native dependencies ---'
  if command -v ldd >/dev/null 2>&1; then ldd "$base/native/easyknack-audio-host"; fi
  echo '--- Tests (no audio hardware required) ---'
  "$base/tests/CommandReaderTest" || status=1
  "$base/tests/AudioGraphTest" || status=1
  "$base/tests/MidiPortBankTest" || status=1
  "$base/tests/LinuxAudioTest" || status=1
  "$base/tests/LameRecordingTest" || status=1
  "$base/tests/PortablePlatformTest" "$base/tests/libplatform-fixture.so" || status=1
  if command -v python3 >/dev/null 2>&1; then
    python3 "$base/tests/HostProtocolTest.py" "$base/native/easyknack-audio-host" || status=1
  else
    echo 'SKIPPED: Protocol test requires python3.'
  fi
  echo '--- Real Linux desktop audio/MIDI integration (temporary test sinks only) ---'
  "$base/tests/LinuxDesktopTest" || status=1
  echo '--- Native Formula, source-plugin/Bake compiler and cache (first build can take several minutes) ---'
  jar=$(tr -d '\r\n' < "$base/application-jar.txt")
  "$base/runtime/bin/java" --enable-native-access=ALL-UNNAMED \
    --add-opens=java.base/java.lang=ALL-UNNAMED \
    --add-opens=java.desktop/sun.awt=ALL-UNNAMED \
    --add-opens=java.desktop/sun.java2d=ALL-UNNAMED \
    -cp "$base/$jar:$base/lib/*" "$base/tests/NativeCompilationCheck.java" "$base/tools/zig/bin/clang++" || status=1
  printf 'Check result: %s (0 = passed)\n' "$status"
  printf '%s\n' "$status" > "$result_file"
) 2>&1 | tee "$log"
tee_result=$?
result=$(cat "$result_file")
case "$result" in 0|1) ;; *) result=1 ;; esac
if [ "$tee_result" -ne 0 ]; then result=1; fi
printf '\nDiagnose gespeichert: %s\n' "$log"
exit "$result"
