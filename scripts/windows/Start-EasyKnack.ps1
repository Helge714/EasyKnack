# Arguments remain literal strings even when opening documents with unusual names.
$documents = @($args)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/JavaPrerequisite.ps1"
Add-Type -AssemblyName System.Windows.Forms
try {
    $jdk = Find-EasyKnackJdk
    while (!$jdk) {
        $choice = [Windows.Forms.MessageBox]::Show(
            "EasyKnack needs a 64-bit JDK 25, including the Java compiler.`n`nYes: install Microsoft OpenJDK 25 using Windows Package Manager (Internet required; the provider's installation and license prompts remain visible).`nNo: select an existing JDK folder.`nCancel: exit.`n`nJava is installed separately and is not removed when EasyKnack is uninstalled.",
            'EasyKnack - Java setup', 'YesNoCancel', 'Information')
        if ($choice -eq 'Cancel') { exit 2 }
        if ($choice -eq 'No') {
            $picker = New-Object Windows.Forms.FolderBrowserDialog
            $picker.Description = 'Select a Windows x64 JDK 25 folder (containing bin and release).'
            try {
                if ($picker.ShowDialog() -eq 'OK' -and (Test-EasyKnackJdk $picker.SelectedPath)) {
                    $jdk = $picker.SelectedPath
                    # Remember the user's explicit choice, without changing system JAVA_HOME.
                    $settings = Join-Path $env:LOCALAPPDATA 'EasyKnack'
                    New-Item -ItemType Directory -Force -Path $settings | Out-Null
                    $jdk | Set-Content -LiteralPath "$settings/jdk-path.txt" -Encoding UTF8
                }
            } finally { $picker.Dispose() }
        } else {
            $winget = Get-Command winget.exe -ErrorAction SilentlyContinue
            if (!$winget) {
                Start-Process 'https://learn.microsoft.com/java/openjdk/install'
                [Windows.Forms.MessageBox]::Show('Install the Windows x64 JDK 25 from the official instructions, then return and select its folder.', 'EasyKnack') | Out-Null
            } else {
                # Interactive on purpose: do not silently accept package/source agreements.
                $install = Start-Process -FilePath $winget.Source -ArgumentList 'install --id Microsoft.OpenJDK.25 --exact --source winget --architecture x64 --interactive' -PassThru -Wait
                if ($install.ExitCode -ne 0) {
                    [Windows.Forms.MessageBox]::Show("Java installation did not complete (exit $($install.ExitCode)). You can retry or select an installed JDK.", 'EasyKnack') | Out-Null
                }
            }
            $jdk = Find-EasyKnackJdk
        }
    }
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
