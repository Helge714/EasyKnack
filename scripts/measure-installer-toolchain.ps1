#Requires -Version 7.0
param([Parameter(Mandatory)][string]$FullToolchain,[Parameter(Mandatory)][string]$ReducedToolchain,[string]$JdkHome=$env:JAVA_HOME,[string]$WixDirectory)
$ErrorActionPreference='Stop'
$repo=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if (!$WixDirectory) { $WixDirectory=Join-Path $repo '.local/tools/wix-3.14.1' }
if (!(Test-Path -LiteralPath "$JdkHome/bin/javac.exe")) { throw 'A JDK is required for the fixture prerequisite.' }
if ((Get-PSDrive C).Free -lt 4GB) { throw 'At least 4 GiB free is required for the isolated comparison.' }
if (!('EasyKnackPackageFiles' -as [type])) { Add-Type -Path "$PSScriptRoot/PackageFiles.cs" }
$id=[guid]::NewGuid().ToString('N').Substring(0,8)
$work=Join-Path $repo "target/tcb-$id"
New-Item -ItemType Directory -Path $work | Out-Null
[IO.File]::WriteAllText("$work/Noop.cs",'class Noop { static void Main() {} }')
& "$env:WINDIR/Microsoft.NET/Framework64/v4.0.30319/csc.exe" /nologo /target:winexe /platform:x64 "/out:$work\Noop.exe" "$work\Noop.cs"
if ($LASTEXITCODE -ne 0) { throw 'Fixture compilation failed' }
$cases=@(@{Id='full';Source=$FullToolchain},@{Id='reduced';Source=$ReducedToolchain})
$results=[Collections.Generic.List[object]]::new()
$oldJdk=$env:EASYKNACK_JAVA_HOME;$env:EASYKNACK_JAVA_HOME=$JdkHome
try {
    # Build both packages before measuring so package compression does not affect the timings.
    foreach ($case in $cases) {
        $folder=Join-Path $work $case.Id;$name="EK Compiler Bench $($case.Id) $id"
        $case.Folder=$folder;$case.Name=$name;$case.Install=Join-Path $env:LOCALAPPDATA "Programs/$name"
        New-Item -ItemType Directory -Path "$folder/image/app/tools","$folder/scripts/windows" | Out-Null
        [EasyKnackPackageFiles]::CopyTree($case.Source,"$folder/image/app/tools/llvm-mingw")
        Copy-Item -LiteralPath "$work/Noop.exe" -Destination "$folder/image/EasyKnack.exe"
        Copy-Item -LiteralPath "$repo/src/main/resources/branding/EKIcon.ico" -Destination "$folder/image/app/EKIcon.ico"
        Copy-Item -LiteralPath "$repo/LICENSE" -Destination "$folder/image/app/LICENSE"
        [IO.File]::WriteAllText("$folder/image/app/installation.properties",'product.version=0.1.0-SNAPSHOT')
        Copy-Item -LiteralPath "$PSScriptRoot/sources.lock.json" -Destination "$folder/scripts/sources.lock.json"
        Copy-Item "$PSScriptRoot/windows/Installer*","$PSScriptRoot/windows/Java*" -Destination "$folder/scripts/windows"
        $script=[IO.File]::ReadAllText("$PSScriptRoot/package-installer.ps1")
        $script=$script.Replace('50cf4639-3e07-4dd7-8ccf-14eb8df748c6',[guid]::NewGuid().ToString()).Replace('e393418e-2805-43c3-b492-e32794465778',[guid]::NewGuid().ToString()).Replace("Name='EasyKnack'","Name='$name'").Replace('Software\EasyKnack\Installer',"Software\EKCompilerBench$id$($case.Id)\Installer").Replace('EasyKnack.Document',"EKCompilerBench.$id.$($case.Id)").Replace('Classes\.knack',"Classes\.ekcb$id$($case.Id)").Replace('[LocalAppDataFolder]Programs\EasyKnack',"[LocalAppDataFolder]Programs\$name")
        [IO.File]::WriteAllText("$folder/scripts/package-installer.ps1",$script)
        Write-Host "Building $($case.Id) fixture..."
        & "$folder/scripts/package-installer.ps1" -Image "$folder/image" -WixDirectory $WixDirectory -Destination "$folder/output" -Type msi -CompilerLayout Files
        $case.Msi=(Get-ChildItem -LiteralPath "$folder/output" -File -Filter '*.msi').FullName
    }
    foreach ($case in $cases) {
        $folder=$case.Folder;$installed=$false
        try {
            foreach ($operation in @('install','repair','uninstall')) {
                $log=Join-Path $folder "$operation.log"
                $mode=switch($operation){'install'{'/i'}'repair'{'/fvomus'}'uninstall'{'/x'}}
                Write-Host "Measuring $($case.Id) $operation..."
                $clock=[Diagnostics.Stopwatch]::StartNew()
                $process=Start-Process -FilePath "$env:WINDIR/System32/msiexec.exe" -ArgumentList @($mode,('"'+$case.Msi+'"'),'/qn','/norestart','MSIFASTINSTALL=7','/l*vx',('"'+$log+'"')) -WindowStyle Hidden -PassThru
                if (!$process.WaitForExit(300000)) { throw 'MSI fixture timed out; inspect its log before cleanup.' }
                $clock.Stop()
                $installed=Test-Path -LiteralPath $case.Install
                if ($process.ExitCode -notin @(0,3010)) { throw "MSI fixture failed: $($process.ExitCode), $log" }
                $results.Add([ordered]@{profile=$case.Id;operation=$operation;seconds=[math]::Round($clock.Elapsed.TotalSeconds,3);log=$log})
            }
            if(Test-Path -LiteralPath $case.Install){throw 'Fixture folder remains after uninstall'}
        } finally {
            if ($installed) {
                $cleanup=Start-Process -FilePath "$env:WINDIR/System32/msiexec.exe" -ArgumentList @('/x',('"'+$case.Msi+'"'),'/qn','/norestart') -WindowStyle Hidden -PassThru -Wait
                if($cleanup.ExitCode -notin @(0,1605,3010)){Write-Warning "Fixture cleanup failed: $($cleanup.ExitCode)"}
            }
        }
    }
    $sizes=foreach($case in $cases){$files=@(Get-ChildItem -LiteralPath $case.Source -Recurse -File);[ordered]@{profile=$case.Id;files=$files.Count;bytes=($files|Measure-Object Length -Sum).Sum;msiBytes=(Get-Item -LiteralPath $case.Msi).Length}}
    [ordered]@{method='Single local run, full then reduced; same no-op application and MSIFASTINSTALL=7; clean install then repair then removal. No existing EasyKnack product is modified.';profiles=@($sizes);runs=@($results)}|ConvertTo-Json -Depth 5|Set-Content -LiteralPath "$work/results.json" -Encoding utf8
    Write-Host "Comparison complete: $work/results.json"
} finally {$env:EASYKNACK_JAVA_HOME=$oldJdk}
