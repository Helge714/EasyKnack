#!/usr/bin/env sh
# Run from an extracted Linux test package, with no changes to the system.
set -u
base=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
log="$base/linux-diagnostics.txt"
(
  status=0
  echo 'EasyKnack Linux test package'
  date -u
  uname -a
  if [ -r /etc/os-release ]; then cat /etc/os-release; fi
  printf 'DISPLAY=%s WAYLAND_DISPLAY=%s\n' "${DISPLAY:-}" "${WAYLAND_DISPLAY:-}"
  echo '--- Java ---'
  "$base/runtime/bin/java" -version || status=1
  echo '--- Native dependencies ---'
  if command -v ldd >/dev/null 2>&1; then ldd "$base/native/easyknack-audio-host"; fi
  echo '--- Tests (no audio hardware required) ---'
  "$base/tests/CommandReaderTest" || status=1
  "$base/tests/AudioGraphTest" || status=1
  "$base/tests/PortablePlatformTest" "$base/tests/libplatform-fixture.so" || status=1
  if command -v python3 >/dev/null 2>&1; then
    python3 "$base/tests/HostProtocolTest.py" "$base/native/easyknack-audio-host" || status=1
  else
    echo 'SKIPPED: Protocol test requires python3.'
  fi
  printf 'Check result: %s (0 = passed)\n' "$status"
  exit "$status"
) > "$log" 2>&1
result=$?
cat "$log"
printf '\nDiagnose gespeichert: %s\n' "$log"
exit "$result"
