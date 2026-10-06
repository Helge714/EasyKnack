#Requires -Version 7.0
# Shared, side-effect-free helpers. ZIP order and timestamps are fixed.
Set-StrictMode -Version Latest
function Get-SourceSnapshot([string]$Root) {
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = [Diagnostics.ProcessStartInfo]::new('git')
    $process.StartInfo.WorkingDirectory = $Root
    $process.StartInfo.RedirectStandardOutput = $true
    $process.StartInfo.UseShellExecute = $false
    $process.StartInfo.CreateNoWindow = $true
    foreach ($arg in @('ls-files','-z','--cached','--others','--exclude-standard')) { $process.StartInfo.ArgumentList.Add($arg) }
    $null = $process.Start(); $output = $process.StandardOutput.ReadToEnd(); $process.WaitForExit()
    if ($process.ExitCode -ne 0) { throw 'Cannot enumerate project sources with Git.' }
    $process.Dispose()
    $files = [Collections.Generic.List[object]]::new()
    foreach ($relative in ($output.Split([char]0, [StringSplitOptions]::RemoveEmptyEntries) | Sort-Object -Unique -CaseSensitive)) {
        # Deliberate source roots; never archive IDE settings, caches or credential folders.
        if ($relative -notmatch '^(src/|native/|scripts/|docs/|examples/|\.github/|[^/]+\.(md|xml|bat)|LICENSE$|\.gitignore$|\.gitattributes$)') { continue }
        if ($relative -match '(^|/)(target|build|\.git|\.aws|\.codex|\.settings)/|\.(exe|dll|class|jar|zip|pdb|obj|key|pfx|pem)$') { continue }
        $path = Join-Path $Root $relative
        if (!(Test-Path -LiteralPath $path -PathType Leaf)) { continue }
        if ((Get-Item -LiteralPath $path).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw "Source symlinks require review: $relative" }
        $files.Add([ordered]@{path=$relative; sha256=(Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()})
    }
    return ,$files.ToArray()
}
function Assert-SourceSnapshot([string]$Root, $Snapshot) {
    $current = Get-SourceSnapshot $Root
    if (($current | ConvertTo-Json -Depth 4 -Compress) -cne ($Snapshot | ConvertTo-Json -Depth 4 -Compress)) {
        throw 'Project sources changed during packaging. Build again from the new source state; this package is not ready for distribution.'
    }
}
function Write-DeterministicZip([string]$Path, [Collections.IDictionary]$Entries) {
    if (Test-Path -LiteralPath $Path) { throw "Refusing to replace existing archive: $Path" }
    foreach ($name in $Entries.Keys) {
        if ($name -match '(^/|\\|(^|/)\.\.(/|$)|:)') { throw "Unsafe archive path: $name" }
    }
    $stream = [IO.File]::Open($Path, [IO.FileMode]::CreateNew)
    $zip = [IO.Compression.ZipArchive]::new($stream, [IO.Compression.ZipArchiveMode]::Create)
    try {
        foreach ($name in ($Entries.Keys | Sort-Object -CaseSensitive)) {
            $entry = $zip.CreateEntry($name, [IO.Compression.CompressionLevel]::Optimal)
            $entry.LastWriteTime = [DateTimeOffset]::new(2000,1,1,0,0,0,[TimeSpan]::Zero)
            $target = $entry.Open()
            try {
                if ($Entries[$name] -is [byte[]]) { $target.Write($Entries[$name]) }
                else { $source = [IO.File]::OpenRead($Entries[$name]); try { $source.CopyTo($target) } finally { $source.Dispose() } }
            } finally { $target.Dispose() }
        }
    } finally { $zip.Dispose(); $stream.Dispose() }
}
function Write-ProjectSources([string]$Root, [string]$Path, $Snapshot) {
    $entries = [ordered]@{}
    foreach ($file in $Snapshot) { $entries['easy-knack/' + $file.path] = Join-Path $Root $file.path }
    $commit = (& git -C $Root rev-parse HEAD) -join ''
    if ($LASTEXITCODE -ne 0) { throw 'Cannot identify source commit.' }
    $metadata = [ordered]@{format=1; commit=$commit; containsWorkingTree=$true; files=$Snapshot}
    $entries['source-snapshot.json'] = [Text.Encoding]::UTF8.GetBytes(($metadata | ConvertTo-Json -Depth 5))
    Write-DeterministicZip $Path $entries
    Assert-SourceSnapshot $Root $Snapshot
}
