param([string]$JdkHome)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/windows/JavaPrerequisite.ps1"
if (!$JdkHome) { $JdkHome = Find-EasyKnackJdk }
if (!(Test-EasyKnackJdk $JdkHome)) { throw 'Tests require a working x64 JDK 25.' }
$work = Join-Path "$PSScriptRoot/../target" ('jdk-test-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path "$work/fake/bin", "$work/image space/app", "$work/classes" | Out-Null

$invalid=Join-Path $work 'invalid-jdk'
New-Item -ItemType Directory -Force -Path "$invalid/bin" | Out-Null
foreach ($part in @('java.exe','javaw.exe','javac.exe')) { [IO.File]::WriteAllBytes("$invalid/bin/$part",[byte[]]@()) }
foreach ($case in @(
    @('JAVA_VERSION="24.0.1"','OS_ARCH="amd64"','OS_NAME="Windows"'),
    @('JAVA_VERSION="25.0.1"','OS_ARCH="aarch64"','OS_NAME="Windows"'),
    @('JAVA_VERSION="25.0.1"','OS_ARCH="amd64"','OS_NAME="Linux"'),
    @('JAVA_VERSION="25.0.1"','OS_ARCH="amd64"','OS_NAME="Windows"')
)) {
    $case | Set-Content -LiteralPath "$invalid/release" -Encoding ascii
    if (Test-EasyKnackJdk $invalid) { throw 'Incompatible metadata or a broken compiler was accepted.' }
}
Remove-Item -LiteralPath "$invalid/bin/javac.exe"
if (Test-EasyKnackJdk $invalid) { throw 'A JRE without javac was accepted.' }
foreach ($file in @('java.exe','javaw.exe','javac.exe')) { New-Item -ItemType File -Path "$work/fake/bin/$file" | Out-Null }
foreach ($metadata in @('JAVA_VERSION="17.0.1"', 'JAVA_VERSION="25.0.1"', 'JAVA_VERSION="26"')) {
    "$metadata`nOS_ARCH=`"aarch64`"`nOS_NAME=`"Windows`"" | Set-Content "$work/fake/release"
    if (Test-EasyKnackJdk "$work/fake") { throw 'Accepted wrong architecture/version.' }
}
'JAVA_VERSION="25.0.1"', 'OS_ARCH="amd64"', 'OS_NAME="Windows"' | Set-Content "$work/fake/release"
if (Test-EasyKnackJdk "$work/fake") { throw 'Accepted a broken compiler.' }
if (Test-EasyKnackJdk 'relative-jdk') { throw 'Accepted relative JDK path.' }
Copy-Item "$PSScriptRoot/windows/*.ps1" "$work/image space/app"
@'
import java.nio.file.*;
import java.util.*;
public class Probe {
    public static void main(String[] args) throws Exception {
        Files.write(Path.of(System.getenv("EASYKNACK_PROBE_OUTPUT")), List.of(com.vin.easyknack.LaunchArguments.decode(args)));
    }
}
'@ | Set-Content "$work/Probe.java" -Encoding utf8
& "$JdkHome/bin/javac.exe" -encoding UTF-8 -d "$work/classes" "$work/Probe.java" "$PSScriptRoot/../src/main/java/com/vin/easyknack/LaunchArguments.java"
if ($LASTEXITCODE -ne 0) { throw 'Probe compile failed.' }
& "$JdkHome/bin/jar.exe" --create --file "$work/image space/app/easy-knack-probe.jar" --main-class Probe -C "$work/classes" .
if ($LASTEXITCODE -ne 0) { throw 'Probe JAR failed.' }
& "$env:WINDIR/Microsoft.NET/Framework64/v4.0.30319/csc.exe" /nologo /target:winexe /platform:x64 /reference:System.Windows.Forms.dll "/out:$work/image space/EasyKnack.exe" (Resolve-Path "$PSScriptRoot/windows/Launcher.cs").Path
if ($LASTEXITCODE -ne 0) { throw 'Launcher compile failed.' }
$oldJdk = $env:EASYKNACK_JAVA_HOME; $oldLocal = $env:LOCALAPPDATA; $oldProbe = $env:EASYKNACK_PROBE_OUTPUT
try {
    $env:EASYKNACK_JAVA_HOME = $JdkHome; $env:LOCALAPPDATA = "$work/local"
    $env:EASYKNACK_PROBE_OUTPUT = "$work/arguments.txt"
    $expected = @('space document.knack', "quote'file.knack", 'semi;dollar$(no).knack', 'C:\trailing\', 'embedded"quote', '', 'C:\Musik ä\音楽.knack')
    $process = Start-Process -FilePath "$work/image space/EasyKnack.exe" -ArgumentList (($expected | ForEach-Object { ConvertTo-EasyKnackArgument $_ }) -join ' ') -WorkingDirectory $env:TEMP -WindowStyle Hidden -PassThru
    if (!$process.WaitForExit(30000)) { $process.Kill(); throw 'Launcher timed out.' }
    if ($process.ExitCode -ne 0) { throw "Launcher failed: $work" }
    $actual = @(Get-Content -LiteralPath "$work/arguments.txt")
    if ($actual.Count -ne $expected.Count) { throw 'Argument count changed.' }
    for ($i=0; $i -lt $actual.Count; $i++) { if ($actual[$i] -cne $expected[$i]) { throw "Argument $i changed." } }
    Write-Host 'Java prerequisite and real launcher argument tests passed. No prerequisite installed.'
} finally {
    $env:EASYKNACK_JAVA_HOME=$oldJdk; $env:LOCALAPPDATA=$oldLocal; $env:EASYKNACK_PROBE_OUTPUT=$oldProbe
}
