param([string]$Cxx = 'clang++')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$sourceRoot = Join-Path $projectRoot 'native/audio-host'
$outputRoot = Join-Path $projectRoot 'target/native'
New-Item -ItemType Directory -Force $outputRoot | Out-Null
& (Join-Path $PSScriptRoot 'generate-native-nodes.ps1') -OutputDirectory "$outputRoot/generated"
$nodeSources = @(Get-ChildItem -LiteralPath "$sourceRoot/src/nodes" -Filter '*.cpp' -File -Recurse | Sort-Object FullName | ForEach-Object { $_.FullName })
& $Cxx -std=c++17 -O2 -static -I "$sourceRoot/third_party" -I "$sourceRoot/src" -I "$outputRoot/generated" "$sourceRoot/src/main.cpp" @nodeSources -o "$outputRoot/easyknack-audio-host.exe" -lws2_32 -lole32 -luuid -lwinmm -ladvapi32 -luser32
if ($LASTEXITCODE -ne 0) { throw 'Audiohost compilation failed' }
& $Cxx -std=c++17 -O2 -static -I "$sourceRoot/third_party" -I "$sourceRoot/src" -I "$outputRoot/generated" "$sourceRoot/tests/WindowsAudioBackendTest.cpp" @nodeSources -o "$outputRoot/windows-audio-backend-test.exe" -lws2_32 -lole32 -luuid -lwinmm -ladvapi32 -luser32
if ($LASTEXITCODE -ne 0) { throw 'WASAPI callback test compilation failed' }
& "$outputRoot/windows-audio-backend-test.exe"
if ($LASTEXITCODE -ne 0) { throw 'WASAPI callback tests failed' }
& $Cxx -std=c++17 -O2 -static -I "$sourceRoot/third_party" -I "$sourceRoot/src" -I "$outputRoot/generated" "$sourceRoot/tests/AsioBackendTest.cpp" @nodeSources -o "$outputRoot/asio-backend-test.exe" -lole32 -ladvapi32 -luser32
if ($LASTEXITCODE -ne 0) { throw 'ASIO backend test compilation failed' }
& "$outputRoot/asio-backend-test.exe"
if ($LASTEXITCODE -ne 0) { throw 'ASIO backend tests failed' }
& $Cxx -std=c++17 -O2 -static -I "$sourceRoot/src" -I "$outputRoot/generated" "$sourceRoot/tests/AudioGraphTest.cpp" @nodeSources -o "$outputRoot/audio-graph-test.exe"
if ($LASTEXITCODE -ne 0) { throw 'DSP test compilation failed' }
& "$outputRoot/audio-graph-test.exe"
if ($LASTEXITCODE -ne 0) { throw 'DSP tests failed' }
& $Cxx -std=c++17 -O2 -static -I "$sourceRoot/src" -I "$outputRoot/generated" "$sourceRoot/tests/FoundationTest.cpp" @nodeSources -o "$outputRoot/foundation-test.exe"
if ($LASTEXITCODE -ne 0) { throw 'DSP foundation test compilation failed' }
& "$outputRoot/foundation-test.exe"
if ($LASTEXITCODE -ne 0) { throw 'DSP foundation tests failed' }
& $Cxx -std=c++17 -O2 -static -I "$sourceRoot/src" -I "$outputRoot/generated" "$sourceRoot/tests/SpectrumStreamTest.cpp" @nodeSources -o "$outputRoot/spectrum-stream-test.exe"
if ($LASTEXITCODE -ne 0) { throw 'Spectral test compilation failed' }
& "$outputRoot/spectrum-stream-test.exe"
if ($LASTEXITCODE -ne 0) { throw 'Spectral tests failed' }
& $Cxx -std=c++17 -O2 -static -municode "$sourceRoot/tests/SharedAudioTransportTest.cpp" -o "$outputRoot/shared-audio-transport-test.exe" -lole32 -ladvapi32
if ($LASTEXITCODE -ne 0) { throw 'Shared audio transport test compilation failed' }
& "$outputRoot/shared-audio-transport-test.exe"
if ($LASTEXITCODE -ne 0) { throw 'Shared audio transport tests failed' }
& $Cxx -std=c++17 -O2 -static "$sourceRoot/tests/DriverBridgeTest.cpp" -o "$outputRoot/driver-bridge-test.exe"
if ($LASTEXITCODE -ne 0) { throw 'Driver bridge test compilation failed' }
& "$outputRoot/driver-bridge-test.exe"
if ($LASTEXITCODE -ne 0) { throw 'Driver bridge tests failed' }
& $Cxx -std=c++17 -O2 -static "$projectRoot/native/virtual-audio/BridgeProbe.cpp" -o "$outputRoot/driver-bridge-probe.exe"
if ($LASTEXITCODE -ne 0) { throw 'Driver bridge probe compilation failed' }
