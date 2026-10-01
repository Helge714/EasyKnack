# ASIO SDK interfaces

Unmodified `common/asio.h`, `common/asiosys.h`, `common/iasiodrv.h`,
`LICENSE.txt`, and `README.md` from the ASIO SDK 2.3 mirror:
https://github.com/audiosdk/asio/tree/496a0765b8bb9c26f764f22f9a9712a937177db2

Copyright Steinberg Media Technologies GmbH. This integration uses the **GPLv3
option**, not the proprietary agreement. Full GPLv3 text: `COPYING`, retrieved
from https://www.gnu.org/licenses/gpl-3.0.txt. Official licensing information:
https://www.steinberg.net/developers/asiosdk-open/

SHA-256:

| File | Hash |
| --- | --- |
| asio.h | 6d83d9b6815d21c002b055c628860e8b0a1ca94c5ae1d9d18f137e308ba98096 |
| asiosys.h | 387fc21d036c3f39d7f74704bebb1e9cfe0bd8564ccd48088827162b06afe583 |
| iasiodrv.h | 0b7d9786fc77ad47dc16324629eb2ae9bff716a0edb023727df3c7ab1b59bf6d |

No vendor audio drivers, proprietary SDK implementation, or routing applications
are included. The host accesses separately installed, architecture-compatible
drivers through the SDK interface. ASIO is a trademark of Steinberg Media
Technologies GmbH; no certification or endorsement is claimed.

Redistribution of the ASIO-enabled native host must comply with GPLv3, including
corresponding source and notices. This notice does not change the license of
GLSwing or declare a new license for the Java application. A distributable
release must provide compatible licensing for the native host's own sources.
