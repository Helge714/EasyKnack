param(
    [ValidateSet('Debug', 'Release')][string]$Configuration = 'Debug',
    [switch]$PrepareOnly
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$manifest = Get-Content "$projectRoot/native/virtual-audio/sysvad-source-manifest.json" -Raw | ConvertFrom-Json
$sourceRoot = "$projectRoot/target/driver-research/Windows-driver-samples"

# Keep the upstream sample separate from EasyKnack's eventual driver changes.
# Check cached sources too: a modified or interrupted download must not pass as upstream.
foreach ($entry in $manifest.files) {
    $destination = Join-Path $sourceRoot $entry.path
    if (-not (Test-Path -LiteralPath $destination)) {
        New-Item -ItemType Directory -Force (Split-Path -Parent $destination) | Out-Null
        Invoke-WebRequest "https://raw.githubusercontent.com/microsoft/Windows-driver-samples/$($manifest.commit)/$($entry.path)" -OutFile $destination
    }
    $bytes = [IO.File]::ReadAllBytes($destination)
    $prefix = [Text.Encoding]::ASCII.GetBytes("blob $($bytes.Length)`0")
    $sha = [Security.Cryptography.SHA1]::Create()
    try {
        $hash = [BitConverter]::ToString($sha.ComputeHash($prefix + $bytes)).Replace('-', '').ToLowerInvariant()
    } finally { $sha.Dispose() }
    if ($hash -ne $entry.sha) { throw "Upstream source checksum mismatch: $($entry.path)" }
}
if ($PrepareOnly) { Write-Output "Verified SysVAD revision $($manifest.commit)"; return }

$vswhere = "${env:ProgramFiles(x86)}/Microsoft Visual Studio/Installer/vswhere.exe"
if (-not (Test-Path $vswhere)) { throw 'Visual Studio Build Tools with C++ and WDK support are required.' }
$msbuild = & $vswhere -latest -products '*' -requires Microsoft.Component.MSBuild -find 'MSBuild\**\Bin\amd64\MSBuild.exe' | Select-Object -First 1
if (-not $msbuild) { throw 'MSBuild not found.' }
$sample = "$sourceRoot/audio/sysvad"
foreach ($project in @('EndpointsCommon/EndpointsCommon.vcxproj', 'TabletAudioSample/TabletAudioSample.vcxproj')) {
    & $msbuild (Join-Path $sample $project) /nologo /m:2 /v:minimal "/p:Configuration=$Configuration" /p:Platform=x64 /p:WindowsTargetPlatformVersion=10.0.28000.0 /p:SignMode=Off
    if ($LASTEXITCODE -ne 0) { throw "SysVAD reference build failed: $project" }
}
Write-Output 'SysVAD reference build passed. This sample has not been installed as a device.'
