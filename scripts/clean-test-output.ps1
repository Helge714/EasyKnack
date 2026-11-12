#Requires -Version 7.0
[CmdletBinding(SupportsShouldProcess)]
param([switch]$Remove, [ValidateRange(0,3650)][int]$OlderThanDays = 1)
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../target'))
if (!(Test-Path -LiteralPath $root -PathType Container)) { return }
if ((Get-Item -LiteralPath $root).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Refusing linked target root.' }
# Only literal temporary-directory prefixes owned by the current Java tests.
$prefixes = [Collections.Generic.HashSet[string]]::new()
Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot '../src/test/java') -Filter '*.java' -Recurse -File | ForEach-Object {
    foreach ($match in [regex]::Matches([IO.File]::ReadAllText($_.FullName), 'createTempDirectory\([^;]*?"([a-z][a-z0-9-]+-)"')) {
        [void]$prefixes.Add($match.Groups[1].Value)
    }
}
$pattern = '^(?:' + (($prefixes | ForEach-Object { [regex]::Escape($_) }) -join '|') + ')\d+$'
$cutoff = (Get-Date).AddDays(-$OlderThanDays)
$count = 0; [long]$total = 0
foreach ($entry in Get-ChildItem -LiteralPath $root -Directory) {
    if ($entry.Name -notmatch $pattern -or $entry.LastWriteTime -ge $cutoff) { continue }
    $path = [IO.Path]::GetFullPath($entry.FullName)
    if ([IO.Path]::GetDirectoryName($path) -ne $root) { throw "Outside target root: $path" }
    # Inspect explicitly without traversing junctions; refuse the entire candidate
    # if any link or repository is found, including a link at the candidate root.
    $pending = [Collections.Generic.Stack[IO.FileSystemInfo]]::new(); $pending.Push([IO.DirectoryInfo]::new($path))
    $safe = $true; [long]$bytes = 0
    while ($pending.Count -gt 0 -and $safe) {
        $current = $pending.Pop()
        if ($current.Attributes -band [IO.FileAttributes]::ReparsePoint -or $current.Name -eq '.git') { $safe = $false; break }
        if ($current -is [IO.DirectoryInfo]) {
            foreach ($item in $current.EnumerateFileSystemInfos()) { $pending.Push($item) }
        } else { $bytes += $current.Length }
    }
    if (!$safe) { Write-Warning "Skipping linked or version-controlled directory: $path"; continue }
    $count++; $total += $bytes
    if ($Remove -and $PSCmdlet.ShouldProcess($path,'Remove generated test directory (stop tests first)')) {
        Remove-Item -LiteralPath $path -Recurse -Force
    }
}
[pscustomobject]@{ Directories=$count; MiB=[math]::Round($total/1MB,1); Removed=[bool]$Remove; MinimumAgeDays=$OlderThanDays }