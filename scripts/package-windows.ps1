#Requires -Version 7.0
param(
    [string]$JdkHome = $env:JAVA_HOME,
    [Parameter(Mandatory=$true)][string]$Toolchain,
    [string]$Maven = 'mvn.cmd',
    [string]$MavenClasspath,
    [string]$Destination,
    [ValidateSet('app-image','exe','msi')][string]$Type = 'app-image'
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = Split-Path -Parent $PSScriptRoot
. "$PSScriptRoot/SourcePackage.ps1"
if (!$JdkHome) { $JdkHome = Split-Path -Parent (Split-Path -Parent (Get-Command javac.exe).Source) }
$JdkHome = (Resolve-Path -LiteralPath $JdkHome).Path
$Toolchain = (Resolve-Path -LiteralPath $Toolchain).Path
$java = Join-Path $JdkHome 'bin/java.exe'
$cxx = Join-Path $Toolchain 'bin/clang++.exe'
foreach ($file in @($java, $cxx, "$JdkHome/bin/javac.exe", "$JdkHome/bin/jpackage.exe")) {
    if (!(Test-Path -LiteralPath $file -PathType Leaf)) { throw "Missing build tool: $file" }
}
[xml]$pom = Get-Content -LiteralPath "$root/pom.xml" -Raw
$version = [string]$pom.project.version
$installerVersion = $version -replace '-.*$', ''
$build = Get-Date -Format 'yyyyMMdd-HHmmss'
if (!$Destination) { $Destination = "$root/target/distribution/$build" }
$Destination = [IO.Path]::GetFullPath($Destination)
if (Test-Path -LiteralPath "$Destination/EasyKnack") { throw 'Destination already contains EasyKnack; choose a fresh destination.' }
$work = Join-Path $root "target/package-work/$([guid]::NewGuid().ToString('N'))"
$inputDir = Join-Path $work 'input'
New-Item -ItemType Directory -Force -Path $inputDir, $Destination | Out-Null
$sourceSnapshot = Get-SourceSnapshot $root
$projectSources = Join-Path $Destination 'EasyKnack-project-sources.zip'
Write-ProjectSources $root $projectSources $sourceSnapshot
Push-Location $root
try {
    Write-Host 'Building and testing Java application...'
    if ($MavenClasspath) {
        & $java "-Dmaven.multiModuleProjectDirectory=$root" -cp $MavenClasspath org.apache.maven.cli.MavenCli -o '-Deasyknack.language=en' package *> "$work/maven.log"
    } else {
        & $Maven '-Deasyknack.language=en' package *> "$work/maven.log"
    }
    if ($LASTEXITCODE -ne 0) { throw "Maven failed: $work/maven.log" }
    Write-Host 'Building the matching native audio host (this can take several minutes)...'
    $native = Join-Path $inputDir 'native'
    New-Item -ItemType Directory -Force -Path $native | Out-Null
    & "$PSScriptRoot/generate-native-nodes.ps1" -OutputDirectory "$root/target/native/generated"
    $sources = @(Get-ChildItem -LiteralPath "$root/native/audio-host/src/nodes" -Filter '*.cpp' -File -Recurse | Sort-Object FullName | ForEach-Object FullName)
    & $cxx -std=c++17 -O2 -static -I "$root/native/audio-host/third_party" -I "$root/native/audio-host/src" -I "$root/target/native/generated" "$root/native/audio-host/src/main.cpp" @sources -o "$native/easyknack-audio-host.exe" -lws2_32 -lole32 -luuid -lwinmm -ladvapi32 -luser32 *> "$work/native.log"
    if ($LASTEXITCODE -ne 0) { throw "Native build failed: $work/native.log" }
    $jar = "easy-knack-$version.jar"
    Copy-Item -LiteralPath "$root/target/$jar" -Destination $inputDir
    New-Item -ItemType Directory -Path "$inputDir/lib", "$inputDir/tools", "$inputDir/licenses" | Out-Null
    # Use this JAR's dependency manifest; target/lib may retain obsolete versions.
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $application = [IO.Compression.ZipFile]::OpenRead("$root/target/$jar")
    try {
        $reader = [IO.StreamReader]::new($application.GetEntry('META-INF/MANIFEST.MF').Open())
        try { $manifest = $reader.ReadToEnd() -replace '\r?\n ', '' } finally { $reader.Dispose() }
        $match = [regex]::Match($manifest, '(?m)^Class-Path: ([^\r\n]+)')
        if (!$match.Success) { throw 'Application dependency manifest is missing.' }
        foreach ($relative in $match.Groups[1].Value.Split(' ', [StringSplitOptions]::RemoveEmptyEntries)) {
            if ($relative -notmatch '^lib/[^/\\]+\.jar$') { throw "Unexpected dependency path: $relative" }
            # Other-platform JogAmp natives are not needed in the Windows x64 image.
            if ($relative -match '-natives-' -and $relative -notmatch '-natives-windows-amd64') { continue }
            Copy-Item -LiteralPath "$root/target/$relative" -Destination "$inputDir/lib"
        }
    } finally { $application.Dispose() }
    Write-Host 'Bundling compiler, headers, libraries and license notices...'
    Copy-Item -LiteralPath $Toolchain -Destination "$inputDir/tools/llvm-mingw" -Recurse
    Copy-Item -LiteralPath "$PSScriptRoot/../docs/WINDOWS-QUICK-START.md" -Destination "$inputDir/README.md"
    Copy-Item -LiteralPath "$PSScriptRoot/../docs/user" -Destination "$inputDir/user" -Recurse
    Copy-Item -LiteralPath "$root/LICENSE", "$root/THIRD-PARTY.md" -Destination $inputDir
    Copy-Item -LiteralPath "$root/src/main/resources/easyknack/notices" -Destination "$inputDir/licenses/upstream" -Recurse
    New-Item -ItemType Directory -Path "$inputDir/licenses/asio", "$inputDir/licenses/miniaudio", "$inputDir/licenses/ffmpeg" | Out-Null
    Copy-Item -LiteralPath "$root/native/audio-host/third_party/asio/LICENSE.txt", "$root/native/audio-host/third_party/asio/COPYING" -Destination "$inputDir/licenses/asio"
    $miniaudio = [IO.File]::ReadAllText("$root/native/audio-host/third_party/miniaudio.h")
    $licenseStart = $miniaudio.LastIndexOf('This software is available as a choice of the following licenses.')
    if ($licenseStart -lt 0) { throw 'miniaudio license section not found.' }
    $miniaudio.Substring($licenseStart).Replace('*/','').Trim() | Set-Content -LiteralPath "$inputDir/licenses/miniaudio/LICENSE.txt" -Encoding utf8
    Copy-Item -Path "$root/src/main/resources/easyknack/third-party/*" -Destination "$inputDir/licenses/ffmpeg"
    @('format=1', "product.version=$version", "platform=windows-x86_64", 'toolchain=tools/llvm-mingw/bin/clang++.exe', "sources.sha256=$((Get-FileHash -LiteralPath $projectSources -Algorithm SHA256).Hash.ToLowerInvariant())") | Set-Content -LiteralPath "$inputDir/installation.properties" -Encoding ascii
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    foreach ($library in Get-ChildItem -LiteralPath "$inputDir/lib" -Filter '*.jar') {
        $archive = [IO.Compression.ZipFile]::OpenRead($library.FullName)
        try {
            foreach ($entry in $archive.Entries | Where-Object { $_.Name -match '(?i)license|copying|notice' }) {
                $folder = Join-Path "$inputDir/licenses" $library.BaseName
                New-Item -ItemType Directory -Force -Path $folder | Out-Null
                $name = $entry.FullName -replace '[^a-zA-Z0-9._-]', '_'
                [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, (Join-Path $folder $name), $true)
            }
        } finally { $archive.Dispose() }
    }
    Write-Host 'Recording package file hashes...'
    $inventory = @(Get-ChildItem -LiteralPath $inputDir -File -Recurse | ForEach-Object {
        [pscustomobject]@{path=$_.FullName.Substring($inputDir.Length+1); bytes=$_.Length; sha256=(Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash}
    })
    $inventory | ConvertTo-Json -Depth 3 | Set-Content -LiteralPath "$inputDir/inventory.json" -Encoding utf8
    Write-Host 'Creating EasyKnack.exe and its bundled Java runtime...'
    & "$JdkHome/bin/jpackage.exe" --type app-image --name EasyKnack --input $inputDir --main-jar $jar --main-class com.vin.easyknack.EasyKnack --dest $Destination --runtime-image $JdkHome --app-version $installerVersion --vendor EasyKnack --description 'Node-based audio workspace' --java-options '--enable-native-access=ALL-UNNAMED' --java-options '--add-opens=java.base/java.lang=ALL-UNNAMED' --java-options '--add-opens=java.desktop/sun.awt=ALL-UNNAMED' --java-options '--add-opens=java.desktop/sun.java2d=ALL-UNNAMED' *> "$work/jpackage.log"
    if ($LASTEXITCODE -ne 0) { throw "jpackage failed: $work/jpackage.log" }
    $image = Join-Path $Destination 'EasyKnack'
    if (!(Test-Path -LiteralPath "$image/runtime/bin/javac.exe")) { throw 'Bundled Java compiler is missing.' }
    Write-Host "Portable application: $image/EasyKnack.exe"
    Assert-SourceSnapshot $root $sourceSnapshot
    & "$PSScriptRoot/package-sources.ps1" -Image $image -ProjectSources $projectSources -Destination $Destination -Download
    if ($Type -ne 'app-image') {
        & "$PSScriptRoot/package-installer.ps1" -Image $image -JdkHome $JdkHome -Type $Type -Destination $Destination
    }
    Assert-SourceSnapshot $root $sourceSnapshot
    Write-Host "Build logs: $work"
} finally { Pop-Location }
