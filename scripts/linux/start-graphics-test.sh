#!/usr/bin/env sh
set -eu
base=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
log="$base/graphics-diagnostics-$(date -u +%Y%m%dT%H%M%SZ)-$$.txt"
JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:+$JAVA_TOOL_OPTIONS }-Deasyknack.graphics.diagnostics=true -Djogl.debug.GLCanvas=true"
if [ "${1:-}" = '--translucent-dialogs' ]; then
  JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Dglswing.dialog.translucent=true"
  shift
fi
export JAVA_TOOL_OPTIONS
printf 'Graphics test: move the window between displays, open documentation, then close EasyKnack.\nLog: %s\n' "$log"
"$base/Start-EasyKnack.sh" "$@" > "$log" 2>&1
