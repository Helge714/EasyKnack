#Requires -Version 7.0
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/SourcePackage.ps1"
$root = Split-Path -Parent $PSScriptRoot
$work = Join-Path $root "target/source-package-tests/$([guid]::NewGuid().ToString('N'))"
New-Item -ItemType Directory -Force -Path "$work/src", "$work/.settings" | Out-Null
& git -C $work init -q
if ($LASTEXITCODE -ne 0) { throw 'Cannot initialize test repository.' }
Set-Content -LiteralPath "$work/src/Änderung.java" -Value 'original' -Encoding utf8
Set-Content -LiteralPath "$work/.settings/local.xml" -Value 'must not be included'
Set-Content -LiteralPath "$work/src/private.key" -Value 'must not be included'
$snapshot = Get-SourceSnapshot $work
if ($snapshot.Count -ne 1 -or $snapshot[0].path -cne 'src/Änderung.java') { throw 'Source enumeration or exclusion failed.' }
$entries = [ordered]@{'source.txt'="$work/src/Änderung.java"; 'metadata.json'=[Text.Encoding]::UTF8.GetBytes('{}')}
Write-DeterministicZip "$work/a.zip" $entries
Write-DeterministicZip "$work/b.zip" $entries
if ((Get-FileHash "$work/a.zip").Hash -cne (Get-FileHash "$work/b.zip").Hash) { throw 'Archive is not deterministic.' }
$rejected = $false
try { Write-DeterministicZip "$work/a.zip" $entries } catch { $rejected = $true }
if (!$rejected) { throw 'Existing archive was replaced.' }
$rejected = $false
try { Write-DeterministicZip "$work/unsafe.zip" @{'../escape'=[byte[]]@(1)} } catch { $rejected = $true }
if (!$rejected) { throw 'Unsafe entry was accepted.' }
Assert-SourceSnapshot $work $snapshot
Set-Content -LiteralPath "$work/src/Änderung.java" -Value 'changed'
$rejected = $false
try { Assert-SourceSnapshot $work $snapshot } catch { $rejected = $true }
if (!$rejected) { throw 'Source mutation was missed.' }
Write-Host 'Source-package tests passed: deterministic bytes, Unicode paths, exclusions, no overwrite, no traversal, source mutation detection.'
