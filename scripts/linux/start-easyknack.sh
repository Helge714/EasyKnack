#!/usr/bin/env sh
set -eu
base=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
case "$(uname -sm)" in
  'Linux x86_64') ;;
  *) echo 'Dieses Testpaket benötigt Linux auf Intel/AMD (x86_64).' >&2; exit 1 ;;
esac
java=${EASYKNACK_JAVA:-"$base/runtime/bin/java"}
if [ ! -x "$java" ]; then
  echo 'Java-Laufzeit fehlt oder ist nicht ausführbar. Bitte das gesamte tar.gz entpacken.' >&2
  exit 1
fi
if [ -z "${DISPLAY:-}" ]; then
  echo 'Kein X11/XWayland-Display gefunden. Bitte in einer grafischen Sitzung starten.' >&2
  exit 1
fi
jar=$(tr -d '\r\n' < "$base/application-jar.txt")
LD_LIBRARY_PATH="$base/native${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
export LD_LIBRARY_PATH
exec "$java" --enable-native-access=ALL-UNNAMED \
  --add-opens=java.base/java.lang=ALL-UNNAMED \
  --add-opens=java.desktop/sun.awt=ALL-UNNAMED \
  --add-opens=java.desktop/sun.java2d=ALL-UNNAMED \
  -jar "$base/$jar" "$@"
