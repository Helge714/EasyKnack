# Build storage and examples

`target` is generated storage, but local developer tools and diagnostics may also
live there. Do not remove it wholesale while using these tools.

| Location | Purpose |
| --- | --- |
| `target/package-work/<GUID>` | Temporary Windows packaging inputs and build logs. Failed builds retain these for diagnosis. |
| `target/distribution` | Finished packages, installers and corresponding source archives. Keep the versions you need. |
| `target/tools` | Local compiler/tool downloads; removal requires downloading them again. |
| `target/*audit*`, reference and benchmark folders | Local investigation and measurement artifacts; review individually. |
| `%USERPROFILE%/.easyknack/plugins/cache` | Runtime plugin and Bake cache, separate from Maven output. Do not delete while EasyKnack runs. |

Successful future package builds retain logs in the destination's `build-logs`
folder and remove their own staging directory. Existing staging can be inspected
with PowerShell 7:

```powershell
./scripts/clean-build-staging.ps1
./scripts/clean-build-staging.ps1 -Remove -WhatIf
./scripts/clean-build-staging.ps1 -Remove
```

The default is report-only. Removal is limited to GUID directories immediately
under `target/package-work` older than seven days. Stop packaging before cleanup;
failed-build logs inside removed directories are lost. Packages, toolchains,
runtime caches and user documents are excluded.

## Example folders

- `examples/` in the repository contains source examples and reproducible test/
  benchmark fixtures. `examples/reference-v1` has its own documentation.
- The application uses `Documents/EasyKnack/Beispiele` by default. It generates
  editable examples there without overwriting existing files. The
  `easyknack.examples` system property can select another folder.
- Package smoke tests and other tests create isolated example folders under
  `target`. These are test outputs, not another user example library.

These folders have different owners and purposes. Keep source fixtures and user
examples separate; they account for little of the packaging storage.
