param([switch]$Quiet, [switch]$CheckOnly)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/JavaPrerequisite.ps1"
try {
    $jdk = Resolve-EasyKnackJdk -NonInteractive:($Quiet -or $CheckOnly)
    if (!$jdk) {
        if ($Quiet -or $CheckOnly) {
            $message = 'A compatible Windows x64 JDK 25 including javac is required. Run the EasyKnack Setup EXE to install Java before the application MSI, or install Java separately first.'
            [Console]::Error.WriteLine($message)
            if (!$Quiet) { Add-Type -AssemblyName System.Windows.Forms; [Windows.Forms.MessageBox]::Show($message,'EasyKnack - Java required','OK','Information') | Out-Null }
            exit 1603
        }
        exit 1602
    }
    [Console]::WriteLine('Compatible Windows x64 JDK 25 found.')
    exit 0
} catch {
    [Console]::Error.WriteLine($_.Exception.Message)
    if (!$Quiet) { Add-Type -AssemblyName System.Windows.Forms; [Windows.Forms.MessageBox]::Show($_.Exception.Message,'EasyKnack - Java setup','OK','Error') | Out-Null }
    exit 1603
}
