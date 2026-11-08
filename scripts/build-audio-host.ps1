param([string]$Cxx = 'clang++', [string]$OutputDirectory, [switch]$SkipTests)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$sourceRoot = Join-Path $projectRoot 'native/audio-host'
if (!$OutputDirectory) { $OutputDirectory = Join-Path $projectRoot 'target/native' }
$generated = Join-Path $projectRoot 'target/native/generated'
New-Item -ItemType Directory -Force $OutputDirectory | Out-Null
. "$PSScriptRoot/NativeBuild.ps1"
& "$PSScriptRoot/generate-native-nodes.ps1" -OutputDirectory $generated
$nodes = @(Get-ChildItem -LiteralPath "$sourceRoot/src/nodes" -Filter '*.cpp' -File -Recurse | Sort-Object FullName | ForEach-Object FullName)
Invoke-EasyKnackNativeCompiler -Cxx $Cxx -SourceRoot $sourceRoot -Generated $generated -Sources (@("$sourceRoot/src/main.cpp") + $nodes) -Output "$OutputDirectory/easyknack-audio-host.exe"
if ($SkipTests) { return }
foreach ($test in @('WindowsAudioBackendTest', 'AsioBackendTest', 'AudioGraphTest', 'SharedAudioTransportTest', 'DriverBridgeTest')) {
    Write-Host "Building native test: $test"
    $sources = @("$sourceRoot/tests/$test.cpp")
    if ($test -in @('WindowsAudioBackendTest','AsioBackendTest','AudioGraphTest')) { $sources += $nodes }
    $extra = @(); if ($test -eq 'SharedAudioTransportTest') { $extra += '-municode' }
    $executable = "$OutputDirectory/$test.exe"
    Invoke-EasyKnackNativeCompiler -Cxx $Cxx -SourceRoot $sourceRoot -Generated $generated -Sources $sources -Output $executable -ExtraArguments $extra
    & $executable
    if ($LASTEXITCODE -ne 0) { throw "Native test failed: $test" }
}
