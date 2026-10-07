#Requires -Version 7.0
param(
    [Parameter(Mandatory)][string]$Image,
    [Parameter(Mandatory)][string]$ProjectSources,
    [string]$Destination,
    [switch]$Download,
    [switch]$RequireComplete
)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/SourcePackage.ps1"
$root = Split-Path -Parent $PSScriptRoot
$Image = (Resolve-Path -LiteralPath $Image).Path
$ProjectSources = (Resolve-Path -LiteralPath $ProjectSources).Path
if (!$Destination) { $Destination = Split-Path -Parent $Image }
$lockPath = Join-Path $PSScriptRoot 'sources.lock.json'
$lock = Get-Content -LiteralPath $lockPath -Raw | ConvertFrom-Json
$cache = Join-Path $root 'target/source-materials'
New-Item -ItemType Directory -Force -Path $cache, $Destination | Out-Null
$entries = [ordered]@{ 'easyknack-sources.zip'=$ProjectSources; 'sources.lock.json'=$lockPath }
$missing = [Collections.Generic.List[string]]::new()
foreach ($source in $lock.sources) {
    if ($source.file -notmatch '^[a-zA-Z0-9._-]+$') { throw 'Unsafe source filename in lock file.' }
    $path = Join-Path $cache $source.file
    if (!(Test-Path -LiteralPath $path) -and $source.PSObject.Properties['localMavenPath']) {
        $local = Join-Path "$env:USERPROFILE/.m2/repository" $source.localMavenPath
        if (Test-Path -LiteralPath $local) {
            if ((Get-FileHash -LiteralPath $local -Algorithm SHA256).Hash -ine $source.sha256) { throw "Local Maven source artifact changed: $($source.id); audit the new build before updating the lock." }
            Copy-Item -LiteralPath $local -Destination $path
        }
    }
    if (!(Test-Path -LiteralPath $path) -and $Download -and $source.PSObject.Properties['url']) {
        Write-Host "Downloading source material: $($source.id)"
        $temporary = $path + '.' + [guid]::NewGuid().ToString('N') + '.part'
        try {
            Invoke-WebRequest -Uri $source.url -OutFile $temporary -TimeoutSec 300
            if ((Get-FileHash -LiteralPath $temporary -Algorithm SHA256).Hash -ine $source.sha256) { throw "Source checksum mismatch: $($source.id)" }
            Move-Item -LiteralPath $temporary -Destination $path
        } finally { if (Test-Path -LiteralPath $temporary) { Remove-Item -LiteralPath $temporary } }
    }
    if (!(Test-Path -LiteralPath $path)) { $missing.Add($source.id); continue }
    if ((Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash -ine $source.sha256) { throw "Source checksum mismatch: $($source.id)" }
    $entries['upstream/' + $source.file] = $path
}
$binaries = @(Get-ChildItem -LiteralPath "$Image/app/lib" -Filter '*.jar' | Sort-Object Name | ForEach-Object {
    [ordered]@{file=$_.Name; sha256=(Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()}
})
$unreviewed = @($binaries | Where-Object { $binary=$_; !($lock.binaries | Where-Object { $_.file -ceq $binary.file -and $_.sha256 -ceq $binary.sha256 }) } | ForEach-Object file)
$runtime = Join-Path $Image 'runtime/release'
$entries['evidence/runtime-release.txt'] = $runtime
if (Test-Path -LiteralPath "$Image/app/inventory.json") { $entries['evidence/package-inventory.json'] = "$Image/app/inventory.json" }
foreach ($file in Get-ChildItem -LiteralPath "$root/docs/development/source-audit" -File) { $entries['evidence/' + $file.Name] = $file.FullName }
$runtimeMatches = (Get-FileHash -LiteralPath $runtime -Algorithm SHA256).Hash -ieq $lock.runtimeReleaseSha256
$sourceHash = (Get-FileHash -LiteralPath $ProjectSources -Algorithm SHA256).Hash.ToLowerInvariant()
$installation = Get-Content -LiteralPath "$Image/app/installation.properties" -Raw
$sourceMatchesBuild = $installation -match "(?m)^sources.sha256=$sourceHash\s*$"
$report = [ordered]@{
    format=1; releaseReady=($missing.Count -eq 0 -and $unreviewed.Count -eq 0 -and $runtimeMatches -and $sourceMatchesBuild -and @($lock.remainingReview).Count -eq 0); sourceArchiveSha256=$sourceHash; sourceMatchesBuild=$sourceMatchesBuild
    binaries=$binaries; unreviewedBinaries=$unreviewed; runtimeMatchesReviewedBuild=$runtimeMatches
    missingSources=@($missing.ToArray()); remainingReview=$lock.remainingReview
}
$entries['REVIEW.json'] = [Text.Encoding]::UTF8.GetBytes(($report | ConvertTo-Json -Depth 6))
$entries['README.txt'] = [Text.Encoding]::UTF8.GetBytes("EasyKnack source materials`n`nIncludes the working-tree project snapshot and checksum-locked upstream archives.`nRead REVIEW.json and evidence before public distribution. This collection is not yet`na certification of complete corresponding sources. Unreviewed binary hashes or a`ndifferent runtime require a new audit. Upstream archives may include their own build-support binaries.`n`nRebuild the project using scripts/build-installer.ps1. Preserve its external Maven`ndependencies. Upstream recipes are in the LLVM-MinGW and temurin-build archives.`n")
$archive = Join-Path $Destination 'EasyKnack-source-materials.zip'
Write-Host "Bundling $($entries.Count) source archives and evidence files..."
Write-DeterministicZip $archive $entries
Write-Host "Source materials: $archive"
if (!$report.releaseReady) {
    Write-Warning "Public-release review remains open; see REVIEW.json. Missing sources: $($missing.Count); unreviewed JARs: $($unreviewed.Count)."
    if ($RequireComplete) { throw 'Corresponding-source review is incomplete. See REVIEW.json in the source materials archive.' }
}

