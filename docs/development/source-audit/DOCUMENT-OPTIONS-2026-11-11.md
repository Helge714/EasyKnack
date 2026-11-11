# Document options follow-up — 2026-11-11

Experimental availability now belongs to document root metadata
(`easyknack.document.experimentalFeatures`) and is edited in Document settings.
New ordinary documents default off. Missing legacy metadata is inferred from
experimental nodes, including stored unused groups. Explicit saved decisions
are retained. Independent controllers/documents do not share availability.

Disabling an enabled document option requires confirmation if experimental nodes
remain. Cancellation applies none of the pending document settings. Existing
nodes retain execution; discovery menus and examples follow the current document.
The former global UI control is removed; briefly exposed application-preference
methods remain deprecated for source compatibility and do not gate documents.

The false unsaved-changes prompt was reproduced after loading an unchanged file
and resizing the viewport: only its camera center changed. Content comparison
now ignores cameras and revisions, including the initialized startup checkpoint.
Actual node movement, node sizes, model values and metadata remain comparable.
Cameras still persist in saved snapshots and restore across group navigation;
recovered documents still require saving.

Validation: 44 focused cases passed, no failures/errors/skips. Coverage includes
legacy detection, explicit-off file round trips, warning conditions, example
filtering, compiled exported metadata, clean close after viewport resizing,
real edits remaining dirty, startup/recovery, document groups, editing history,
OpenGL file/export checks and group-camera restoration. Maven package succeeds.
Both language references validate: 230 pages, 7,781 local links/anchors.
The full native/audio suite was not repeated for this Java/UI/persistence change.

The existing first-development version covers this follow-up. Metadata uses the
existing document schema; no audio protocol, plugin ABI or GLSwing change.
