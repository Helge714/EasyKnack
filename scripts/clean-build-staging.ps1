#Requires -Version 7.0
[CmdletBinding(SupportsShouldProcess)]
param([switch]$Remove, [ValidateRange(0,3650)][int]$OlderThanDays = 7)
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../target/package-work'))
if (!(Test-Path -LiteralPath $root -PathType Container)) { return }
if ((Get-Item -LiteralPath $root).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Refusing linked staging root.' }
$cutoff = (Get-Date).AddDays(-$OlderThanDays)
foreach ($entry in Get-ChildItem -LiteralPath $root -Directory) {
    if ($entry.Name -notmatch '^[0-9a-f]{32}$' -or $entry.LastWriteTime -ge $cutoff) { continue }
    $path = [IO.Path]::GetFullPath($entry.FullName)
    if ([IO.Path]::GetDirectoryName($path) -ne $root) { throw "Outside staging root: $path" }
    if ($entry.Attributes -band [IO.FileAttributes]::ReparsePoint) { continue }
    $items = @(Get-ChildItem -LiteralPath $path -Recurse -Force)
    if ($items | Where-Object { $_.Attributes -band [IO.FileAttributes]::ReparsePoint }) { Write-Warning "Skipping linked contents: $path"; continue }
    $bytes = ($items | Where-Object { !$_.PSIsContainer } | Measure-Object -Property Length -Sum).Sum
    [pscustomobject]@{ Path=$path; MiB=[math]::Round($bytes/1MB,1); Modified=$entry.LastWriteTime }
    if ($Remove -and $PSCmdlet.ShouldProcess($path,'Remove old packaging staging directory (do not run during packaging)')) {
        Remove-Item -LiteralPath $path -Recurse -Force
    }
}
