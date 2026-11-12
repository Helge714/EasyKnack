# Build storage and examples

`target` contains disposable builds and older local investigations. Compiler tools
now live in `.local/tools`; new release images go to `dist`. Existing research
material and older release outputs under `target` still require individual review.

| Location | Purpose |
| --- | --- |
| `target/package-work/<GUID>` | Temporary Windows packaging inputs and build logs. Failed builds retain these for diagnosis. |
| `dist/` | New finished packages, installers and matching source archives. Old packages under `target/distribution` are preserved. |
| `.local/tools` | Durable local compilers and packaging tools, excluded from Git and Maven clean. |
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
  benchmark fixtures. `src/test/resources/reference-projects` has its own documentation.
- The application uses `Documents/EasyKnack/Beispiele` by default. It generates
  editable examples there without overwriting existing files. The
  `easyknack.examples` system property can select another folder.
- Package smoke tests and other tests create isolated example folders under
  `target`. These are test outputs, not another user example library.

These folders have different owners and purposes. Keep source fixtures and user
examples separate; they account for little of the packaging storage.

## Generated test runs

Java tests leave isolated `target/<prefix><random digits>` directories for failure
diagnosis. Stop tests, then use PowerShell 7:

```powershell
./scripts/clean-test-output.ps1
./scripts/clean-test-output.ps1 -Remove -WhatIf
./scripts/clean-test-output.ps1 -Remove
```

The default reports candidates older than one day. `-OlderThanDays 0` also includes
recent completed runs. Prefixes come from literal temporary-directory declarations
in the current Java tests; other folders are excluded. The cleaner refuses linked
contents and Git repositories. It preserves normal Maven outputs, source archives,
packages, toolchains and named investigations. Do not run it concurrently with tests.

The November 12 inventory found 5.45 GiB in 2,999 target subdirectories: about
674 MiB belonged to 2,839 generated test runs. Larger retained items include old
packages (`distribution`, 1.33 GiB), release audits/source checkouts, reference
measurements and source-material downloads. They are not all disposable test junk.

The first cleanup removed 2,818 runs older than one day, freeing 673.9 MiB.
Recent runs and all named audit/research/package directories were preserved.
A fixture verified removal of ordinary generated output and preservation of a
Git marker, unknown directory and junction target.
