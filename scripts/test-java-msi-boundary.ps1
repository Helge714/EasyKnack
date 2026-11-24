$ErrorActionPreference = 'Stop'
$work = Join-Path "$PSScriptRoot/../target" ('java-msi-boundary-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $work | Out-Null
# Exercise the real C# launcher and embedded setup script, without installing Java.
@'
function Resolve-EasyKnackJdk {
    param([switch]$NonInteractive)
    [IO.File]::WriteAllText($env:EASYKNACK_TEST_TRACE, [string][bool]$NonInteractive)
    if ($env:EASYKNACK_TEST_JDK -eq 'present') { return 'Test JDK' }
    return $null
}
'@ | Set-Content -LiteralPath "$work/JavaPrerequisite.ps1" -Encoding UTF8
$compiler = "$env:WINDIR/Microsoft.NET/Framework64/v4.0.30319/csc.exe"
& $compiler /nologo /target:winexe /reference:System.Windows.Forms.dll "/out:$work/JavaSetup.exe" "/resource:$work/JavaPrerequisite.ps1,JavaPrerequisite.ps1" "/resource:$PSScriptRoot/windows/JavaSetup.ps1,JavaSetup.ps1" (Resolve-Path "$PSScriptRoot/windows/JavaSetup.cs").Path
if ($LASTEXITCODE -ne 0) { throw 'Prerequisite helper compilation failed.' }
$oldTrace = $env:EASYKNACK_TEST_TRACE
$oldJdk = $env:EASYKNACK_TEST_JDK
try {
    foreach ($case in @(
        @{ Arguments='/check /ui 2'; Jdk='missing'; Code=1603; NonInteractive='True' },
        @{ Arguments='/ui 2'; Jdk='missing'; Code=1603; NonInteractive='True' },
        @{ Arguments='/check /ui 5'; Jdk='present'; Code=0; NonInteractive='True' },
        @{ Arguments='/ui 5'; Jdk='present'; Code=0; NonInteractive='False' }
    )) {
        $env:EASYKNACK_TEST_TRACE = Join-Path $work ([guid]::NewGuid().ToString('N') + '.txt')
        $env:EASYKNACK_TEST_JDK = $case.Jdk
        $process = Start-Process -FilePath "$work/JavaSetup.exe" -ArgumentList $case.Arguments -WindowStyle Hidden -PassThru -RedirectStandardOutput "$work/stdout.txt" -RedirectStandardError "$work/stderr.txt"
        if (!$process.WaitForExit(30000)) { $process.Kill(); throw 'Java prerequisite timed out.' }
        if ($process.ExitCode -ne $case.Code) { throw "Unexpected exit code for $($case.Arguments): $($process.ExitCode). $(Get-Content -LiteralPath "$work/stderr.txt" -Raw)" }
        if ([IO.File]::ReadAllText($env:EASYKNACK_TEST_TRACE) -ne $case.NonInteractive) { throw 'Wrong prerequisite installation policy.' }
    }
    $packager = Get-Content -LiteralPath "$PSScriptRoot/package-installer.ps1" -Raw
    if ($packager -notmatch "Id='CheckJavaBeforeInstall'[^\r\n]*ExeCommand='/check /ui \[UILevel\]'") { throw 'Direct MSI must only check Java.' }
    if ($packager -notmatch "InstallCommand='/ui \[WixBundleUILevel\]'") { throw 'Bootstrapper must retain interactive Java installation.' }
    'Passed: direct MSI never installs Java; bootstrapper retains interactive prerequisite handling.'
} finally {
    $env:EASYKNACK_TEST_TRACE = $oldTrace
    $env:EASYKNACK_TEST_JDK = $oldJdk
}
