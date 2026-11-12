# Arguments remain literal strings even when opening documents with unusual names.
$documents = @($args)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/JavaPrerequisite.ps1"
Add-Type -AssemblyName System.Windows.Forms
try {
    $jdk = Resolve-EasyKnackJdk
    if (!$jdk) { exit 2 }
    $jars = @(Get-ChildItem -LiteralPath $PSScriptRoot -Filter 'easy-knack-*.jar')
    if ($jars.Count -ne 1) { throw 'Expected exactly one EasyKnack application JAR. Please reinstall EasyKnack.' }
    $arguments = @('--enable-native-access=ALL-UNNAMED', '--add-opens=java.base/java.lang=ALL-UNNAMED',
        '--add-opens=java.desktop/sun.awt=ALL-UNNAMED', '--add-opens=java.desktop/sun.java2d=ALL-UNNAMED')
    if ($documents.Count -gt 0) {
        $encoded = ($documents | ForEach-Object { [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($_)) }) -join ':'
        $arguments += '-Deasyknack.launch.arguments=' + $encoded
    }
    # Relative JAR name avoids the Windows Java launcher's ANSI conversion of its path.
    $arguments += @('-jar', $jars[0].Name)
    $logDirectory = Join-Path $env:LOCALAPPDATA 'EasyKnack/logs'
    New-Item -ItemType Directory -Force -Path $logDirectory | Out-Null
    $log = Join-Path $logDirectory (Get-Date -Format 'yyyyMMdd-HHmmss-ffff')
    $process = Start-Process -FilePath "$jdk/bin/java.exe" -ArgumentList (($arguments | ForEach-Object { ConvertTo-EasyKnackArgument $_ }) -join ' ') -WorkingDirectory $PSScriptRoot -WindowStyle Hidden -PassThru -Wait -RedirectStandardOutput "$log.out.log" -RedirectStandardError "$log.err.log"
    if ($process.ExitCode -ne 0) { throw "EasyKnack exited with code $($process.ExitCode).`nDetails: $log.err.log" }
    exit 0
} catch {
    [Windows.Forms.MessageBox]::Show($_.Exception.Message, 'EasyKnack', 'OK', 'Error') | Out-Null
    exit 1
}
