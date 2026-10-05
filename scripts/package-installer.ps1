#Requires -Version 7.0
param(
    [Parameter(Mandatory=$true)][string]$Image,
    [Parameter(Mandatory=$true)][string]$JdkHome,
    [string]$WixDirectory,
    [ValidateSet('exe','msi')][string]$Type = 'exe',
    [string]$Destination
)
$ErrorActionPreference = 'Stop'
$Image = (Resolve-Path -LiteralPath $Image).Path
$JdkHome = (Resolve-Path -LiteralPath $JdkHome).Path
if (!$Destination) { $Destination = Split-Path -Parent $Image }
$Destination = [IO.Path]::GetFullPath($Destination)
$line = Get-Content -LiteralPath "$Image/app/installation.properties" | Where-Object { $_ -like 'product.version=*' }
if (@($line).Count -ne 1) { throw 'Missing or ambiguous packaged product version.' }
$version = ($line.Substring('product.version='.Length)) -replace '-.*$', ''
$expected = Join-Path $Destination "EasyKnack-$version.$Type"
if (Test-Path -LiteralPath $expected) { throw "Refusing to replace existing installer: $expected" }
New-Item -ItemType Directory -Force -Path $Destination | Out-Null
$oldPath = $env:PATH
try {
    if ($WixDirectory) { $env:PATH = "$( (Resolve-Path -LiteralPath $WixDirectory).Path );$env:PATH" }
    Write-Host "Building $Type installer from verified application image..."
    & "$JdkHome/bin/jpackage.exe" --verbose --type $Type --app-image $Image --dest $Destination --name EasyKnack --app-version $version --vendor EasyKnack --win-per-user-install --win-menu --win-menu-group EasyKnack --win-shortcut --win-shortcut-prompt --win-dir-chooser --win-upgrade-uuid '50cf4639-3e07-4dd7-8ccf-14eb8df748c6' *> "$Destination/installer-$Type.log"
    if ($LASTEXITCODE -ne 0) {
        Get-Content -LiteralPath "$Destination/installer-$Type.log" -Tail 30 | ForEach-Object { Write-Host $_ }
        throw "Installer build failed: $Destination/installer-$Type.log"
    }
    if (!(Test-Path -LiteralPath $expected)) { throw "Installer output not found: $expected" }
    Write-Host "Installer: $expected"
} finally { $env:PATH = $oldPath }
