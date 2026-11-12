#Requires -Version 7.0
param([string]$JdkHome, [string]$WixDirectory)
$ErrorActionPreference='Stop'
. "$PSScriptRoot/windows/JavaPrerequisite.ps1"
if (!$JdkHome) { $JdkHome=Find-EasyKnackJdk }
if (!(Test-EasyKnackJdk $JdkHome)) { throw 'A working Windows x64 JDK 25 is required for this test.' }
if (!$WixDirectory) { $WixDirectory=(Resolve-Path "$PSScriptRoot/../.local/tools/wix-3.14.1").Path }
$root=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../target'))
$work=Join-Path $root ('installer-java-test-'+[guid]::NewGuid().ToString('N'))
$id=[guid]::NewGuid().ToString('N').Substring(0,8)
$name="EasyKnack Java Setup Test $id"
$install=Join-Path $env:LOCALAPPDATA "Programs/$name"
New-Item -ItemType Directory -Path "$work/scripts/windows","$work/image/app" | Out-Null
Copy-Item -LiteralPath "$PSScriptRoot/sources.lock.json" -Destination "$work/scripts/sources.lock.json"
Copy-Item "$PSScriptRoot/windows/Installer*" -Destination "$work/scripts/windows"
Copy-Item "$PSScriptRoot/windows/JavaSetup.*" -Destination "$work/scripts/windows"
# Restrict only the fixture's discovery to one test candidate. Validation and
# interactive/noninteractive logic remain the production implementation.
$helper=[IO.File]::ReadAllText("$PSScriptRoot/windows/JavaPrerequisite.ps1")
$helper=[regex]::Replace($helper,'(?s)function Get-EasyKnackJdkCandidates \{.*?\r?\n\}', 'function Get-EasyKnackJdkCandidates { return $env:EASYKNACK_TEST_JDK }')
if (!$helper.Contains('function Get-EasyKnackJdkCandidates { return $env:EASYKNACK_TEST_JDK }')) { throw 'Fixture discovery isolation failed.' }
[IO.File]::WriteAllText("$work/scripts/windows/JavaPrerequisite.ps1",$helper)
$script=[IO.File]::ReadAllText("$PSScriptRoot/package-installer.ps1")
$script=$script.Replace('50cf4639-3e07-4dd7-8ccf-14eb8df748c6',[guid]::NewGuid().ToString()).Replace('e393418e-2805-43c3-b492-e32794465778',[guid]::NewGuid().ToString()).Replace("Name='EasyKnack'","Name='$name'").Replace('Software\EasyKnack\Installer',"Software\EasyKnackSetupTest$id\Installer").Replace('EasyKnack.Document',"EasyKnack.SetupTest$id").Replace('Classes\.knack',"Classes\.eksetuptest$id").Replace('[LocalAppDataFolder]Programs\EasyKnack',"[LocalAppDataFolder]Programs\$name")
[IO.File]::WriteAllText("$work/scripts/package-installer.ps1",$script)
Copy-Item -LiteralPath "$PSScriptRoot/../src/main/resources/branding/EKIcon.ico" -Destination "$work/image/app/EKIcon.ico"
Copy-Item -LiteralPath "$PSScriptRoot/../LICENSE" -Destination "$work/image/app/LICENSE"
[IO.File]::WriteAllText("$work/image/app/installation.properties",'product.version=0.1.0-SNAPSHOT')
[IO.File]::WriteAllText("$work/Noop.cs",'class Noop { static void Main() {} }')
$csc="$env:WINDIR/Microsoft.NET/Framework64/v4.0.30319/csc.exe"
& $csc /nologo /target:winexe /platform:x64 "/out:$work\image\EasyKnack.exe" "$work\Noop.cs"
if ($LASTEXITCODE -ne 0) { throw 'Fixture launcher compilation failed.' }
& "$work/scripts/package-installer.ps1" -Image "$work/image" -WixDirectory $WixDirectory -Destination "$work/output"
$exe=Join-Path $work 'output/EasyKnack-Setup-0.1.0-x64.exe'
$msi=Get-ChildItem -LiteralPath "$work/output" -Recurse -Filter '*.msi' | Select-Object -First 1 -ExpandProperty FullName
function Run([string]$File,[string[]]$Arguments) {
    $process=Start-Process -FilePath $File -ArgumentList $Arguments -WindowStyle Hidden -PassThru
    if (!$process.WaitForExit(60000)) { $process.Kill(); throw 'Fixture operation timed out.' }
    return $process.ExitCode
}
$previous=$env:EASYKNACK_TEST_JDK
$installed=$false
try {
    $env:EASYKNACK_TEST_JDK=Join-Path $work 'missing-jdk'
    $failed=Run -File $exe -Arguments @('/quiet','/norestart','/log',('"'+$work+'\missing.log"'))
    if ($failed -eq 0 -or (Test-Path -LiteralPath $install)) { throw 'Missing JDK did not block installation.' }
    $failed=Run -File "$env:WINDIR/System32/msiexec.exe" -Arguments @('/i',('"'+$msi+'"'),'/qn','/norestart','/log',('"'+$work+'\standalone-missing.log"'))
    if ($failed -ne 1603 -or (Test-Path -LiteralPath $install)) { throw 'Standalone MSI did not reject a missing JDK before copying.' }
    $msiLog=Get-Content -LiteralPath "$work/standalone-missing.log" -Raw
    if ($msiLog -notmatch 'CheckJavaBeforeInstall') { throw 'Standalone MSI failed before reaching the prerequisite test.' }
    $env:EASYKNACK_TEST_JDK=$JdkHome
    $passed=Run -File $exe -Arguments @('/quiet','/norestart','/log',('"'+$work+'\valid.log"'))
    $installed=Test-Path -LiteralPath $install
    if ($passed -ne 0 -or !(Test-Path -LiteralPath "$install/EasyKnack.exe")) { throw 'Compatible JDK did not allow installation.' }
    # Removing Java must never prevent uninstalling the application.
    $env:EASYKNACK_TEST_JDK=Join-Path $work 'missing-again'
    $removed=Run -File $exe -Arguments @('/uninstall','/quiet','/norestart','/log',('"'+$work+'\uninstall-without-java.log"'))
    if ($removed -ne 0 -or (Test-Path -LiteralPath $install)) { throw 'Uninstall without Java failed.' }
    $installed=$false
    Write-Host "Installer prerequisite tests passed: missing Java blocks EXE/MSI, compatible JDK allows install, uninstall needs no Java. Logs: $work"
} finally {
    if ($installed -or (Test-Path -LiteralPath $install)) { [void](Run -File $exe -Arguments @('/uninstall','/quiet','/norestart')) }
    $env:EASYKNACK_TEST_JDK=$previous
}
