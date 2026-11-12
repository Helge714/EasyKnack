# Windows PowerShell 5.1 compatible; no Java is bundled or installed by these functions.
function Get-EasyKnackJdkCandidates {
    $env:EASYKNACK_JAVA_HOME
    $saved = Join-Path $env:LOCALAPPDATA 'EasyKnack/jdk-path.txt'
    if (Test-Path -LiteralPath $saved) { (Get-Content -LiteralPath $saved -Raw).Trim() }
    $env:JAVA_HOME
    [Environment]::GetEnvironmentVariable('JAVA_HOME', 'User')
    [Environment]::GetEnvironmentVariable('JAVA_HOME', 'Machine')
    $javac = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($javac) { Split-Path -Parent (Split-Path -Parent $javac.Source) }
    foreach ($base in @("$env:ProgramFiles/Microsoft", "$env:ProgramFiles/Eclipse Adoptium",
        "$env:ProgramFiles/Java", "$env:USERPROFILE/.jdks", "$env:LOCALAPPDATA/Programs/Eclipse Adoptium")) {
        Get-ChildItem -LiteralPath $base -Directory -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending | ForEach-Object FullName
    }
    foreach ($base in @('HKLM:\SOFTWARE\JavaSoft\JDK', 'HKCU:\SOFTWARE\JavaSoft\JDK')) {
        Get-ChildItem $base -ErrorAction SilentlyContinue | ForEach-Object {
            (Get-ItemProperty $_.PSPath -Name JavaHome -ErrorAction SilentlyContinue).JavaHome
        }
    }
}
function Test-EasyKnackJdk([string]$JdkDirectory) {
    if (!$JdkDirectory) { return $false }
    if (![IO.Path]::IsPathRooted($JdkDirectory)) { return $false }
    foreach ($part in @('bin/java.exe','bin/javaw.exe','bin/javac.exe','release')) {
        if (!(Test-Path -LiteralPath (Join-Path $JdkDirectory $part) -PathType Leaf)) { return $false }
    }
    $release = Get-Content -LiteralPath (Join-Path $JdkDirectory 'release') -Raw
    # Use the tested LTS family and the architecture of our native host/OpenGL libraries.
    if ($release -notmatch '(?m)^JAVA_VERSION="25(?:[.\-"]|$)' -or
        $release -notmatch '(?m)^OS_ARCH="(?:amd64|x86_64)"' -or
        $release -notmatch '(?m)^OS_NAME="Windows"') { return $false }
    try {
        $start = New-Object Diagnostics.ProcessStartInfo
        $start.FileName = Join-Path $JdkDirectory 'bin/javac.exe'
        $start.Arguments = '-version'
        $start.UseShellExecute = $false; $start.CreateNoWindow = $true
        $start.RedirectStandardOutput = $true; $start.RedirectStandardError = $true
        $process = [Diagnostics.Process]::Start($start)
        try {
            if (!$process.WaitForExit(5000)) { $process.Kill(); return $false }
            $version = $process.StandardOutput.ReadToEnd() + $process.StandardError.ReadToEnd()
            return $process.ExitCode -eq 0 -and $version -match '^javac 25(?:[.\-\s]|$)'
        } finally { $process.Dispose() }
    } catch { return $false }
}
function Find-EasyKnackJdk {
    foreach ($candidate in @(Get-EasyKnackJdkCandidates | Where-Object { $_ } | Select-Object -Unique)) {
        if (Test-EasyKnackJdk $candidate) { return [IO.Path]::GetFullPath($candidate) }
    }
    return $null
}
function ConvertTo-EasyKnackArgument([string]$Value) {
    # Windows CommandLineToArgvW quoting, including trailing backslashes.
    return '"' + ([regex]::Replace([regex]::Replace($Value, '(\\*)"', '$1$1\"'), '(\\+)$', '$1$1')) + '"'
}

# Shared interactive prerequisite flow for the installer and application launcher.
# Noninteractive setup never downloads Java or displays selection dialogs.
function Resolve-EasyKnackJdk([switch]$NonInteractive) {
    $jdk = Find-EasyKnackJdk
    if ($jdk -or $NonInteractive) { return $jdk }
    Add-Type -AssemblyName System.Windows.Forms
    while (!$jdk) {
        $choice = [Windows.Forms.MessageBox]::Show(
            "EasyKnack needs a 64-bit JDK 25, including the Java compiler.`n`nYes: install Microsoft OpenJDK 25 using Windows Package Manager (Internet required; the provider's installation and license prompts remain visible).`nNo: select an existing JDK folder.`nCancel: exit.`n`nJava is installed separately and is not removed when EasyKnack is uninstalled.",
            'EasyKnack - Java setup', 'YesNoCancel', 'Information')
        if ($choice -eq 'Cancel') { return $null }
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
    return $jdk
}
