# Port units

Right-click a numeric port or its label and choose **Unit**. Connected inputs keep
this menu available when their slider is hidden. The slider and port menu share
one setting. Only units supported by that port are offered.

An output's unit controls the numbers it emits. An input's unit controls how it
interprets incoming numbers. Connections carry numbers unchanged: `1` from a
seconds output becomes `1 ms` at an input set to milliseconds. Select seconds on
that input to preserve the duration, or insert a Unit Converter.

Group outputs support the same selection. Their internal units are converted at
the group boundary. Unit settings are saved with the document and included in
baked groups and exported nodes. Trigger Timer retains its shared unit selection
for both time outputs. Vector component display units remain separate from these
scalar port conversions.
