param(
    [string]$JdkHome,
    [string]$Toolchain,
    [string]$WixDirectory,
    [string]$Maven,
    [string]$MavenClasspath,
    [switch]$CheckTools,
    [switch]$NoOpen
)
$ErrorActionPreference = 'Stop'
try {
    # The BAT can start with Windows PowerShell; packaging itself needs PowerShell 7.
    if ($PSVersionTable.PSVersion.Major -lt 7) {
        $pwsh = Get-Command pwsh.exe -ErrorAction SilentlyContinue
        $candidates = @(
            $(if ($pwsh) { $pwsh.Source }),
            "$env:ProgramFiles/PowerShell/7/pwsh.exe",
            "$env:USERPROFILE/.cache/codex-runtimes/codex-primary-runtime/dependencies/native/powershell/pwsh.exe"
        )
        $shell = $candidates | Where-Object { $_ -and (Test-Path -LiteralPath $_ -PathType Leaf) } | Select-Object -First 1
        if (!$shell) { throw 'PowerShell 7 is required. Install it and run this file again.' }
        & $shell -NoLogo -NoProfile -File $PSCommandPath @PSBoundParameters
        exit $LASTEXITCODE
    }
    $root = Split-Path -Parent $PSScriptRoot
    if (!$JdkHome) {
        $javac = Get-Command javac.exe -ErrorAction SilentlyContinue
        $candidates = @(
            $env:JAVA_HOME,
            $(if ($javac) { Split-Path -Parent (Split-Path -Parent $javac.Source) }),
            @(Get-ChildItem "$env:USERPROFILE/.jdks" -Directory -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | ForEach-Object FullName)
        ) | ForEach-Object { $_ }
        $JdkHome = $candidates | Where-Object {
            $_ -and (Test-Path -LiteralPath "$_/bin/jpackage.exe") -and (Test-Path -LiteralPath "$_/bin/javac.exe")
        } | Select-Object -First 1
    }
    if (!$Toolchain) {
        $Toolchain = Get-ChildItem "$root/target/tools" -Directory -Filter 'llvm-mingw*' -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending | Where-Object { Test-Path -LiteralPath "$($_.FullName)/bin/clang++.exe" } |
            Select-Object -First 1 -ExpandProperty FullName
    }
    if (!$WixDirectory) {
        $candle = Get-Command candle.exe -ErrorAction SilentlyContinue
        if ($candle) { $WixDirectory = Split-Path -Parent $candle.Source }
        else {
            $WixDirectory = Get-ChildItem "$root/target/tools" -Directory -Filter 'wix*' -ErrorAction SilentlyContinue |
                Sort-Object Name -Descending | Where-Object { Test-Path -LiteralPath "$($_.FullName)/candle.exe" } |
                Select-Object -First 1 -ExpandProperty FullName
        }
    }
    foreach ($entry in @(
        @('JDK', $JdkHome, 'bin/jpackage.exe'),
        @('LLVM-MinGW', $Toolchain, 'bin/clang++.exe'),
        @('WiX', $WixDirectory, 'light.exe')
    )) {
        if (!$entry[1] -or !(Test-Path -LiteralPath (Join-Path $entry[1] $entry[2]) -PathType Leaf)) {
            throw "Missing $($entry[0]). See docs/WINDOWS-PACKAGE.md for tool paths and overrides."
        }
        Write-Host "$($entry[0]): $($entry[1])"
    }
    if (!$Maven -and !$MavenClasspath) {
        $mvn = Get-Command mvn.cmd -ErrorAction SilentlyContinue
        if ($mvn) { $Maven = $mvn.Source }
        else {
            $runtime = Get-ChildItem "$env:USERPROFILE/.p2/pool/plugins" -Directory -Filter 'org.eclipse.m2e.maven.runtime_*' -ErrorAction SilentlyContinue |
                Sort-Object Name -Descending | Where-Object { Test-Path -LiteralPath "$($_.FullName)/jars" } | Select-Object -First 1
            if ($runtime) {
                # Eclipse keeps Maven's shared dependencies in sibling OSGi bundles.
                $shared = Get-ChildItem $runtime.Parent.FullName -File | Where-Object {
                    $_.Name -match '^(org.apache.commons\.(cli|commons-codec|commons-io|lang3)_|com.google.guava[_.]|jakarta.inject.jakarta.inject-api_1\.)'
                } | ForEach-Object FullName
                $logging = Get-ChildItem "$env:USERPROFILE/.m2/repository/org/slf4j/slf4j-api/1.7.*/*.jar" -ErrorAction SilentlyContinue |
                    Sort-Object FullName -Descending | Select-Object -First 1 -ExpandProperty FullName
                if (!$logging) { throw 'Embedded Maven requires SLF4J 1.7 in the local Maven cache. Alternatively supply -Maven with a standalone Maven installation.' }
                $MavenClasspath = (@("$($runtime.FullName)/jars/*", $logging) + @($shared)) -join ';'
            }
            else { throw 'Maven was not found. Install Maven on PATH or supply -Maven / -MavenClasspath.' }
        }
    }
    if ($MavenClasspath) { Write-Host 'Maven: Java classpath (embedded runtime)' }
    else { Write-Host "Maven: $Maven" }
    # Ensure a PATH Maven also runs with the selected packaging JDK.
    $env:JAVA_HOME = $JdkHome
    $env:PATH = "$JdkHome/bin;$WixDirectory;$env:PATH"
    if ($MavenClasspath) {
        & "$JdkHome/bin/java.exe" "-Dmaven.multiModuleProjectDirectory=$root" -cp $MavenClasspath org.apache.maven.cli.MavenCli --version
    } else { & $Maven --version }
    if ($LASTEXITCODE -ne 0) { throw 'Maven tool check failed.' }
    if ($CheckTools) { Write-Host 'All build tools found. No installer was built.'; exit 0 }
    $destination = Join-Path $root "target/distribution/$(Get-Date -Format 'yyyyMMdd-HHmmss-fff')"
    $arguments = @{JdkHome=$JdkHome; Toolchain=$Toolchain; Destination=$destination; Type='exe'}
    if ($MavenClasspath) { $arguments.MavenClasspath = $MavenClasspath }
    else { $arguments.Maven = $Maven }
    Write-Host "Output: $destination"
    Write-Host 'Building current sources, tests, audio host and installer. This can take several minutes.'
    & "$PSScriptRoot/package-windows.ps1" @arguments
    Write-Host "Installer build complete: $destination" -ForegroundColor Green
    if (!$NoOpen) { & explorer.exe $destination }
} catch {
    Write-Host "Build failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
