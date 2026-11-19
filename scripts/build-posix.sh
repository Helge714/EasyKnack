#!/usr/bin/env sh
# Linux first; Darwin follows the same source/build boundary, pending validation.
set -eu
root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
case "$(uname -s)" in
  Linux) platform=linux ;;
  Darwin) platform=macos ;;
  *) echo "Use build-audio-host.ps1 on Windows" >&2; exit 1 ;;
esac
cd "$root"
# Supply MVN=/path/to/mvn and CXX=/path/to/compiler when necessary.
"${MVN:-mvn}" -DskipTests process-classes
cmake -S native/audio-host -B "target/native-$platform" -DCMAKE_BUILD_TYPE=Release
cmake --build "target/native-$platform" --parallel "${BUILD_JOBS:-2}"
ctest --test-dir "target/native-$platform" --output-on-failure
mkdir -p target/native
cp "target/native-$platform/easyknack-audio-host" target/native/easyknack-audio-host
echo "Native host installed in target/native/easyknack-audio-host; run Maven package for the Java distribution."
