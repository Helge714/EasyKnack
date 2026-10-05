#Requires -Version 7.0
param([Parameter(Mandatory=$true)][string]$Image, [switch]$Window, [switch]$WindowOnly)
$ErrorActionPreference = 'Stop'
$Image = (Resolve-Path -LiteralPath $Image).Path
$root = Split-Path -Parent $PSScriptRoot
$work = Join-Path $root "target/package-smoke/$([guid]::NewGuid().ToString('N'))"
New-Item -ItemType Directory -Force -Path $work, "$work/home", "$work/classes" | Out-Null
$jars = @(Get-ChildItem -LiteralPath "$Image/app" -Filter 'easy-knack-*.jar')
if ($jars.Count -ne 1) { throw 'Expected exactly one packaged application JAR.' }
$classpath = "$($jars[0].FullName);$Image/app/lib/*"
$oldPath = $env:PATH
$oldJavaHome = $env:JAVA_HOME
try {
    $env:PATH = "$env:SystemRoot/System32;$env:SystemRoot"
    $env:JAVA_HOME = "$Image/runtime"
    Push-Location $work
    try {
        if (!$WindowOnly) {
        & "$Image/runtime/bin/javac.exe" -encoding UTF-8 -proc:none -cp $classpath -d "$work/classes" "$PSScriptRoot/WindowsPackageSmoke.java"
        if ($LASTEXITCODE -ne 0) { throw 'Package smoke compilation failed.' }
        & "$Image/runtime/bin/java.exe" --enable-native-access=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.desktop/sun.awt=ALL-UNNAMED --add-opens=java.desktop/sun.java2d=ALL-UNNAMED "-Duser.home=$work/home" "-Deasyknack.plugins=$work/plugins" "-Deasyknack.examples=$work/examples" '-Deasyknack.language=en' "-Dorg.bytedeco.javacpp.cachedir=$work/media-native" -cp "$work/classes;$classpath" com.vin.easyknack.ui.WindowsPackageSmoke $work *> "$work/smoke.log"
        $result = $LASTEXITCODE
        Get-Content -LiteralPath "$work/smoke.log"
        if ($result -ne 0) { throw "Packaged smoke test failed: $work/smoke.log" }
        }
        if ($Window -or $WindowOnly) {
            if (-not ('EasyKnackWindowProbe' -as [type])) {
                Add-Type @'
using System;
using System.Runtime.InteropServices;
public static class EasyKnackWindowProbe {
    [StructLayout(LayoutKind.Sequential)] public struct Rect { public int Left, Top, Right, Bottom; }
    [DllImport("user32.dll")] static extern bool GetWindowRect(IntPtr window, out Rect rect);
    public static bool IsWorkspace(IntPtr window) {
        Rect r; return GetWindowRect(window, out r) && r.Right-r.Left >= 900 && r.Bottom-r.Top >= 600;
    }
}
'@
            }
            $oldOptions = $env:JAVA_TOOL_OPTIONS
            $process = $null
            try {
                $env:JAVA_TOOL_OPTIONS = "-Duser.home=`"$work/home`" -Deasyknack.plugins=`"$work/window-plugins`" -Deasyknack.examples=`"$work/examples`" -Deasyknack.language=en"
                $process = Start-Process -FilePath "$Image/EasyKnack.exe" -WorkingDirectory $work -WindowStyle Hidden -PassThru -RedirectStandardOutput "$work/launcher.log" -RedirectStandardError "$work/launcher-error.log"
                $deadline = [DateTime]::UtcNow.AddSeconds(60)
                $windowProcess = $null
                do {
                    Start-Sleep -Milliseconds 500
                    $process.Refresh()
                    if ($process.HasExited) { throw "Launcher exited before showing its window: $work/launcher-error.log" }
                    # jpackage may fork a child process; the workspace updates its title to the document name.
                    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId=$($process.Id)" | ForEach-Object { Get-Process -Id $_.ProcessId -ErrorAction SilentlyContinue })
                    $windowProcess = @(@($process) + $children | Where-Object { $_.MainWindowHandle -ne 0 -and $_.MainWindowTitle -like '*EasyKnack*' -and [EasyKnackWindowProbe]::IsWorkspace($_.MainWindowHandle) } | Select-Object -First 1)
                } while ($windowProcess.Count -eq 0 -and [DateTime]::UtcNow -lt $deadline)
                if ($windowProcess.Count -eq 0) { throw 'Main application window did not open.' }
                Write-Host "Launcher window OK: $($windowProcess[0].MainWindowTitle)"
                Start-Sleep -Seconds 3
                $windowProcess[0].CloseMainWindow() | Out-Null
                if (!$process.WaitForExit(30000)) { throw 'Application did not close normally.' }
                if ($process.ExitCode -ne 0) { throw "Launcher exited with code $($process.ExitCode)" }
            } finally {
                $env:JAVA_TOOL_OPTIONS = $oldOptions
                if ($process -and !$process.HasExited) { $process.Kill($true) }
            }
        }
        Write-Host "Validation logs: $work"
    } finally { Pop-Location }
} finally { $env:PATH=$oldPath; $env:JAVA_HOME=$oldJavaHome }
