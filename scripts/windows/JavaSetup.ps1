param([switch]$Quiet)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/JavaPrerequisite.ps1"
try {
    $jdk = Resolve-EasyKnackJdk -NonInteractive:$Quiet
    if (!$jdk) {
        if ($Quiet) { [Console]::Error.WriteLine('A compatible Windows x64 JDK 25 including javac is required.'); exit 1603 }
        exit 1602
    }
    [Console]::WriteLine('Compatible Windows x64 JDK 25 found.')
    exit 0
} catch {
    [Console]::Error.WriteLine($_.Exception.Message)
    if (!$Quiet) { Add-Type -AssemblyName System.Windows.Forms; [Windows.Forms.MessageBox]::Show($_.Exception.Message,'EasyKnack - Java setup','OK','Error') | Out-Null }
    exit 1603
}
