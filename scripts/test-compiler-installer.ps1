#Requires -Version 7.0
param([string]$CompilerSource, [string]$WixDirectory)
$ErrorActionPreference='Stop'
$repo=[IO.Path]::GetFullPath("$PSScriptRoot/..")
if (!$WixDirectory) { $WixDirectory=(Resolve-Path "$repo/.local/tools/wix-3.14.1").Path }
$id=[guid]::NewGuid().ToString('N').Substring(0,8)
$work=Join-Path $repo "target/compiler-archive-test-$id"
$name="EasyKnack Compiler Archive Test $id"
$install=Join-Path $env:LOCALAPPDATA "Programs/$name"
$upgrade=[guid]::NewGuid().ToString()
New-Item -ItemType Directory -Path "$work/scripts/windows","$work/image/app/tools/llvm-mingw/bin" | Out-Null
Copy-Item "$PSScriptRoot/SourcePackage.ps1","$PSScriptRoot/PackageFiles.cs","$PSScriptRoot/sources.lock.json" -Destination "$work/scripts"
Copy-Item "$PSScriptRoot/windows/Installer*","$PSScriptRoot/windows/Java*","$PSScriptRoot/windows/CompilerSetup.cs" -Destination "$work/scripts/windows"
$script=[IO.File]::ReadAllText("$PSScriptRoot/package-installer.ps1")
$script=$script.Replace('50cf4639-3e07-4dd7-8ccf-14eb8df748c6',$upgrade).Replace("Name='EasyKnack'","Name='$name'").Replace('Software\EasyKnack\Installer',"Software\EKCompilerArchiveTest$id\Installer").Replace('EasyKnack.Document',"EKCompilerArchiveTest.$id").Replace('Classes\.knack',"Classes\.ekcat$id")
[IO.File]::WriteAllText("$work/scripts/package-installer.ps1",$script)
Copy-Item -LiteralPath "$repo/src/main/resources/branding/EKIcon.ico" -Destination "$work/image/app/EKIcon.ico"
Copy-Item -LiteralPath "$repo/LICENSE" -Destination "$work/image/app/LICENSE"
[IO.File]::WriteAllText("$work/Noop.cs",'class Noop { static void Main() {} }')
& "$env:WINDIR/Microsoft.NET/Framework64/v4.0.30319/csc.exe" /nologo /target:winexe /platform:x64 "/out:$work\image\EasyKnack.exe" "$work\Noop.cs"
if ($LASTEXITCODE -ne 0) { throw 'Fixture launcher compilation failed.' }
$compiler="$work/image/app/tools/llvm-mingw"
if ($CompilerSource) {
    # Destination exists only because the small fixture starts with a bin directory.
    [IO.Directory]::Delete("$compiler/bin"); [IO.Directory]::Delete($compiler)
    if (!('EasyKnackPackageFiles' -as [type])) { Add-Type -Path "$PSScriptRoot/PackageFiles.cs" }
    [EasyKnackPackageFiles]::CopyCompiler((Resolve-Path $CompilerSource).Path,$compiler)
} else {
    [IO.File]::WriteAllText("$compiler/bin/compiler.exe",'old compiler fixture')
    [IO.File]::WriteAllText("$compiler/LICENSE",'test license')
}
$probe=if ($CompilerSource) { 'bin/clang++.exe' } else { 'bin/compiler.exe' }
$original=(Get-FileHash -LiteralPath "$compiler/$probe").Hash
function Build([string]$Version,[string]$Layout='Archive',[switch]$Fail) {
    [IO.File]::WriteAllText("$work/image/app/installation.properties","product.version=$Version-SNAPSHOT")
    $source=$script
    if ($Fail) {
        $source=$source.Replace('$compilerActions' + "`n<Binary Id='JavaPrerequisite'", '$compilerActions' + "`n<CustomAction Id='FixtureFailure' BinaryKey='CompilerSetup' ExeCommand='fail' Execute='deferred' Impersonate='yes' Return='check'/><InstallExecuteSequence><Custom Action='FixtureFailure' After='InstallCompiler'>1</Custom></InstallExecuteSequence>`n<Binary Id='JavaPrerequisite'")
    }
    [IO.File]::WriteAllText("$work/scripts/package-installer.ps1",$source)
    & "$work/scripts/package-installer.ps1" -Image "$work/image" -WixDirectory $WixDirectory -Destination "$work/$Version" -Type msi -CompilerLayout $Layout
    return [IO.Path]::GetFullPath("$work/$Version/EasyKnack-Setup-$Version-x64.msi")
}
function RunMsi([string]$Operation,[string]$Msi,[string]$Log,[int]$Expected=0) {
    $timer=[Diagnostics.Stopwatch]::StartNew()
    $process=Start-Process "$env:WINDIR/System32/msiexec.exe" -ArgumentList @($Operation,('"'+$Msi+'"'),'/qn','/norestart','EK_BUNDLE=1','EK_STARTMENU=0','EK_DESKTOP=0','MSIFASTINSTALL=5',('INSTALLDIR="'+$install+'"'),'/l*v',('"'+$work+'\'+$Log+'.log"')) -WindowStyle Hidden -PassThru
    if (!$process.WaitForExit(300000)) { throw "Installer timed out; inspect $work/$Log.log" }
    if ($process.ExitCode -ne $Expected) { throw "Installer exit $($process.ExitCode), expected $Expected; inspect $work/$Log.log" }
    $timer.Stop(); Write-Host "$Log : $($timer.Elapsed.TotalSeconds.ToString('F3')) seconds"
    $results.Add([ordered]@{action=$Log;seconds=$timer.Elapsed.TotalSeconds;exitCode=$process.ExitCode})
}
function AssertCompiler { if ((Get-FileHash -LiteralPath "$install/app/tools/llvm-mingw/$probe").Hash -ne $original) { throw 'Compiler contents differ.' } }
$results=[Collections.Generic.List[object]]::new()
$active=$null
try {
    # Small fixture covers migration and failed major-upgrade rollback. Large run
    # measures the production archive without repeating the old per-file benchmark.
    if (!$CompilerSource) {
        $legacy=Build '0.1.0' 'Files'
        RunMsi '/i' $legacy 'legacy-install'; $active=$legacy; AssertCompiler
    }
    $archive=Build '0.2.0'
    RunMsi '/i' $archive 'archive-install'; $active=$archive; AssertCompiler
    [IO.File]::WriteAllText("$install/app/tools/llvm-mingw/$probe",'damaged')
    RunMsi '/fvomus' $archive 'archive-repair'; AssertCompiler
    if (!$CompilerSource) {
        $failed=Build '0.3.0' -Fail
        RunMsi '/i' $failed 'failed-upgrade' 1603; AssertCompiler
        if ([IO.File]::ReadAllText("$install/app/installation.properties") -notmatch '0.2.0') { throw 'MSI did not restore the prior version.' }
        $updated=Build '0.4.0'
        RunMsi '/i' $updated 'archive-upgrade'; $active=$updated; AssertCompiler
    }
    if (!$CompilerSource) {
        $helper=Get-ChildItem -LiteralPath "$work/0.4.0" -Recurse -Filter CompilerSetup.exe | Select-Object -First 1 -ExpandProperty FullName
        function Helper([string]$Operation,[int]$Expected=0) {
            $p=Start-Process -FilePath $helper -ArgumentList @($Operation,('"'+$install+'"')) -WindowStyle Hidden -PassThru -Wait
            if ($p.ExitCode -ne $Expected) { throw "Compiler helper $Operation returned $($p.ExitCode)" }
        }
        Helper 'remove'; Helper 'rollback'; AssertCompiler
        Helper 'install'; Helper 'publish'; Helper 'rollback'; AssertCompiler
        $zip="$install/app/tools/llvm-mingw.zip"
        $bytes=[IO.File]::ReadAllBytes($zip)
        try { [IO.File]::WriteAllText($zip,'corrupted archive'); Helper 'install' 1603; Helper 'rollback'; AssertCompiler }
        finally { [IO.File]::WriteAllBytes($zip,$bytes) }
        [IO.File]::WriteAllText("$install/app/tools/llvm-mingw/user-extra.txt",'preserve me')
        Helper 'install'; Helper 'publish'; Helper 'commit'; AssertCompiler
        $extra=@(Get-ChildItem -LiteralPath "$install/app/tools" -Directory -Filter '.ek-compiler-backup-*')
        if ($extra.Count -ne 1 -or [IO.File]::ReadAllText((Join-Path $extra[0].FullName 'user-extra.txt')) -ne 'preserve me') { throw 'Unknown compiler files were not preserved.' }
        # Remove only this test-owned extra; production leaves extras in their backup.
        [IO.File]::Delete((Join-Path $extra[0].FullName 'user-extra.txt')); [IO.Directory]::Delete($extra[0].FullName)
    } else {
        [IO.File]::WriteAllText("$work/probe.cpp",'#include <iostream>
#include <thread>
#include <filesystem>
int main() { std::thread t([]{}); t.join(); std::cout << std::filesystem::current_path(); }')
        & "$install/app/tools/llvm-mingw/bin/clang++.exe" -std=c++20 -static ([IO.Path]::GetFullPath("$work/probe.cpp")) -o ([IO.Path]::GetFullPath("$work/probe.exe"))
        if ($LASTEXITCODE -ne 0) { throw 'Extracted compiler failed its C++ probe.' }
        & "$work/probe.exe"
        if ($LASTEXITCODE -ne 0) { throw 'Compiled C++ probe failed.' }
    }
    RunMsi '/x' $active 'archive-uninstall'; $active=$null
    if (Test-Path -LiteralPath $install) { throw "Installer left files behind: $install" }
    if (!$CompilerSource) {
        RunMsi '/i' $failed 'failed-fresh-install' 1603
        if (Test-Path -LiteralPath $install) { throw 'Failed fresh installation left files behind.' }
    }
    $results | ConvertTo-Json | Set-Content -LiteralPath "$work/results.json"
    Write-Host "Compiler archive installer tests passed. Evidence: $work"
} finally {
    if ($active) { RunMsi '/x' $active 'cleanup' }
}
