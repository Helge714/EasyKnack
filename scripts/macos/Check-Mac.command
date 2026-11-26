#!/bin/sh
set -u
base=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
app=${1:-"$base/EasyKnack.app"}
if [ ! -d "$app" ]; then app=/Applications/EasyKnack.app; fi
if [ ! -d "$app" ]; then echo 'EasyKnack.app not found. Pass its path as the first argument.' >&2; exit 1; fi
app=$(CDPATH= cd -- "$app" && pwd)
data="$app/Contents/app"
java="$app/Contents/runtime/Contents/Home/bin/java"
EASYKNACK_NATIVE_LIBDIR="$data/native"
export EASYKNACK_NATIVE_LIBDIR
logs="$HOME/Library/Logs/EasyKnack"
mkdir -p "$logs"
log="$logs/mac-diagnostics-$(date +%Y%m%d-%H%M%S).txt"
result_file=$(mktemp "${TMPDIR:-/tmp}/easyknack-mac-check.XXXXXX")
trap 'rm -f "$result_file"' EXIT
trap 'exit 130' HUP INT TERM
(
  status=0
  date -u
  sw_vers
  uname -m
  "$java" -version || status=1
  codesign --verify --deep --strict "$app" || status=1
  for test in command-reader-test audio-graph-test midi-port-bank-test routed-audio-test mac-midi-test lame-recording-test; do
    "$data/tests/$test" || status=1
  done
  "$data/tests/portable-platform-test" "$data/tests/libplatform-library-fixture.dylib" || status=1
  if command -v python3 >/dev/null 2>&1; then python3 "$data/tests/HostProtocolTest.py" "$data/native/easyknack-audio-host" || status=1; fi
  jar=$(tr -d '\r\n' < "$data/application-jar.txt")
  "$java" --enable-native-access=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.desktop/sun.awt=ALL-UNNAMED --add-opens=java.desktop/sun.java2d=ALL-UNNAMED \
    -cp "$data/$jar:$data/lib/*" "$data/tests/NativeCompilationCheck.java" "$data/tools/apple/bin/clang++" || status=1
  echo 'Manual acceptance still required: GUI/Retina, physical audio/MIDI, device switching, microphone and system/application capture permission, recordings and Bake playback.'
  echo "Check result: $status (0 = automated checks passed; no hardware acceptance claimed)"
  printf '%s\n' "$status" > "$result_file"
) 2>&1 | tee "$log"
tee_result=$?
result=$(cat "$result_file")
case "$result" in 0|1) ;; *) result=1 ;; esac
if [ "$tee_result" -ne 0 ]; then result=1; fi
echo "Diagnostics: $log"
exit "$result"
