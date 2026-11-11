# Documentation follow-up — 2026-11-11

The native documentation window now offers Same as application, Deutsch and
English. Only the documentation preference is persisted; application language,
theme and document content are unaffected. Node-context help, active-node help,
the index, local navigation and reopened windows use the preference. Switching
language retains the logical current page. Explicit counterpart-language links
also select that language; ordinary links use the selected language.

Missing translations fall back to English without replacing the preference.
Untranslated workflow pages stay English and show the fallback in the status
line. This does not claim a complete German translation of the English handbook.
History remains local to each window, and language changes reuse the existing
request-revision guard against stale asynchronous page loads.

Six older references had already been archived and replaced with forwarding
pages. This follow-up additionally removes the stale FFT-window range from the
spectrum workflow, replaces repeated Spectral Features definitions/output tables
with canonical bilingual references, and links Viewer limits/defaults instead of
copying them. Interpretation examples and inspection/export workflows remain.
The old node overview now links to bundled file/reader references instead of an
unreachable developer page. Technical spectral/trigger guides still need their
separate tutorial consolidation; archives and unique workflow material are retained.

Validation: 20 targeted tests pass, no failures/errors/skips, including persisted
choice/reopening, unchanged application locale, fallback, local links, history,
narrow-toolbar bounds, native reference presentation and existing preparation.
The final documentation preview explicitly includes four changed legacy workflow
pages: 234 pages and 7,823 local links/anchors validated. Maven package succeeds.
No native/audio suite was repeated for this documentation/UI preference change.

The current first-development version covers the feature; no document schema,
audio protocol, plugin ABI or GLSwing version changes. Local classes/resources
are refreshed for the Eclipse launch. This is not installer/release acceptance.
