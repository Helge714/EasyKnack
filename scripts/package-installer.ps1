#Requires -Version 7.0
param(
    [Parameter(Mandatory=$true)][string]$Image,
    [string]$JdkHome, # Retained as a deprecated build-script argument; never bundled.
    [string]$WixDirectory,
    [ValidateSet('exe','msi')][string]$Type = 'exe',
    [string]$Destination
)
$ErrorActionPreference = 'Stop'
$Image = (Resolve-Path -LiteralPath $Image).Path
if (Test-Path -LiteralPath "$Image/runtime") { throw 'This installer requires a Java-external image, without a bundled runtime.' }
if (!$Destination) { $Destination = Split-Path -Parent $Image }
$Destination = [IO.Path]::GetFullPath($Destination)
if (!$WixDirectory) {
    $candle = Get-Command candle.exe -ErrorAction SilentlyContinue
    if ($candle) { $WixDirectory = Split-Path -Parent $candle.Source }
    else { $WixDirectory = Get-ChildItem "$PSScriptRoot/../.local/tools" -Directory -Filter 'wix*' | Sort-Object Name -Descending | Select-Object -First 1 -ExpandProperty FullName }
}
foreach ($name in @('candle.exe','light.exe','WixUIExtension.dll','WixBalExtension.dll')) {
    if (!(Test-Path -LiteralPath "$WixDirectory/$name")) { throw "WiX 3 is required: missing $name" }
}
$reviewedTools = (Get-Content -LiteralPath "$PSScriptRoot/sources.lock.json" -Raw | ConvertFrom-Json).installerTools
foreach ($file in $reviewedTools.files) {
    $path = Join-Path $WixDirectory $file.path
    if (!(Test-Path -LiteralPath $path) -or (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash -ine $file.sha256) {
        throw "Unreviewed WiX component: $($file.path). Review its version, sources and license before packaging."
    }
}
$line = @(Get-Content -LiteralPath "$Image/app/installation.properties" | Where-Object { $_ -like 'product.version=*' })
if ($line.Count -ne 1) { throw 'Missing or ambiguous packaged product version.' }
$version = ($line[0].Substring('product.version='.Length)) -replace '-.*$', ''
$expected = Join-Path $Destination "EasyKnack-Setup-$version-x64.$Type"
if (Test-Path -LiteralPath $expected) { throw "Refusing to replace existing installer: $expected" }
New-Item -ItemType Directory -Force -Path $Destination | Out-Null
$work = Join-Path $Destination ('installer-work-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $work | Out-Null
function Xml([string]$text) { [Security.SecurityElement]::Escape($text) }
function Id([string]$prefix,[string]$path) {
    $hash = [Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes($path.ToLowerInvariant()))
    $prefix + [Convert]::ToHexString($hash).Substring(0,24)
}
$components = [Collections.Generic.List[string]]::new()
function DirectoryXml([string]$folder) {
    $builder = [Text.StringBuilder]::new()
    foreach ($file in Get-ChildItem -LiteralPath $folder -File | Sort-Object Name) {
        $relative = [IO.Path]::GetRelativePath($Image,$file.FullName)
        $component = Id 'C' $relative; $fileId = Id 'F' $relative
        $components.Add($component)
        [void]$builder.Append("<Component Id='$component' Guid='*' Win64='yes'><File Id='$fileId' Source='$(Xml $file.FullName)' KeyPath='yes' /></Component>")
    }
    foreach ($child in Get-ChildItem -LiteralPath $folder -Directory | Sort-Object Name) {
        [void]$builder.Append("<Directory Id='$(Id 'D' ([IO.Path]::GetRelativePath($Image,$child.FullName)))' Name='$(Xml $child.Name)'>$(DirectoryXml $child.FullName)</Directory>")
    }
    $builder.ToString()
}
$tree = DirectoryXml $Image
$refs = ($components | ForEach-Object { "<ComponentRef Id='$_'/>" }) -join ''
$icon = Join-Path $Image 'app/EKIcon.ico'
if (!(Test-Path -LiteralPath $icon)) { throw "Missing packaged application icon: $icon" }
$launcher = Id 'F' 'EasyKnack.exe'
# Separate optional features provide persistent, independent shortcut choices.
$xml = @"
<Wix xmlns='http://schemas.microsoft.com/wix/2006/wi'>
<Product Id='*' Name='EasyKnack' Language='1033' Version='$version' Manufacturer='EasyKnack' UpgradeCode='50cf4639-3e07-4dd7-8ccf-14eb8df748c6'>
<Package InstallerVersion='500' Compressed='yes' InstallScope='perUser' Platform='x64'/>
<MajorUpgrade AllowSameVersionUpgrades='yes' DowngradeErrorMessage='A newer EasyKnack version is already installed.'/>
<MediaTemplate EmbedCab='yes'/>
<Icon Id='EasyKnackIcon' SourceFile='$(Xml $icon)'/><Property Id='ARPPRODUCTICON' Value='EasyKnackIcon'/>
<Condition Message='EasyKnack requires 64-bit Windows.'>VersionNT64</Condition>
<Directory Id='TARGETDIR' Name='SourceDir'>
<Directory Id='LocalAppDataFolder'><Directory Id='ProgramsFolder' Name='Programs'><Directory Id='INSTALLDIR' Name='EasyKnack'>$tree
<Component Id='FileAssociation' Guid='*' Win64='yes'>
<RegistryValue Root='HKCU' Key='Software\Classes\.knack' Value='EasyKnack.Document' Type='string' KeyPath='yes'/>
<RegistryValue Root='HKCU' Key='Software\Classes\EasyKnack.Document' Value='EasyKnack document' Type='string'/>
<RegistryValue Root='HKCU' Key='Software\Classes\EasyKnack.Document\DefaultIcon' Value='&quot;[INSTALLDIR]EasyKnack.exe&quot;,0' Type='string'/>
<RegistryValue Root='HKCU' Key='Software\Classes\EasyKnack.Document\shell\open\command' Value='&quot;[INSTALLDIR]EasyKnack.exe&quot; &quot;%1&quot;' Type='string'/>
</Component>
</Directory></Directory></Directory>
<Directory Id='ProgramMenuFolder'><Directory Id='EasyKnackMenu' Name='EasyKnack'>
<Component Id='MenuShortcut' Guid='*' Win64='yes'><Shortcut Id='MenuLink' Name='EasyKnack' Target='[INSTALLDIR]EasyKnack.exe' WorkingDirectory='INSTALLDIR' Icon='EasyKnackIcon' IconIndex='0'/><RemoveFolder Id='RemoveMenu' On='uninstall'/><RegistryValue Root='HKCU' Key='Software\EasyKnack\Installer' Name='StartMenu' Type='integer' Value='1' KeyPath='yes'/></Component>
</Directory></Directory>
<Directory Id='DesktopFolder'><Component Id='DesktopShortcut' Guid='*' Win64='yes'><Shortcut Id='DesktopLink' Name='EasyKnack' Target='[INSTALLDIR]EasyKnack.exe' WorkingDirectory='INSTALLDIR' Icon='EasyKnackIcon' IconIndex='0'/><RegistryValue Root='HKCU' Key='Software\EasyKnack\Installer' Name='Desktop' Type='integer' Value='1' KeyPath='yes'/></Component></Directory>
</Directory>
<Feature Id='Application' Title='EasyKnack and C++ compiler' Description='Required application files and LLVM-MinGW for Bake and plugins. Java is installed separately.' Level='1' Absent='disallow' ConfigurableDirectory='INSTALLDIR'>$refs<ComponentRef Id='FileAssociation'/>
<Feature Id='StartMenu' Title='Start Menu shortcut' Level='1'><ComponentRef Id='MenuShortcut'/></Feature>
<Feature Id='Desktop' Title='Desktop shortcut' Level='2'><ComponentRef Id='DesktopShortcut'/></Feature>
</Feature>
<Property Id='INSTALLLEVEL' Value='1'/>
<Property Id='WIXUI_EXITDIALOGOPTIONALCHECKBOXTEXT' Value='Check Java prerequisites and start EasyKnack'/>
<Property Id='WIXUI_EXITDIALOGOPTIONALCHECKBOX' Value='1'/>
<CustomAction Id='LaunchEasyKnack' FileKey='$launcher' ExeCommand='' Execute='immediate' Impersonate='yes' Return='asyncNoWait'/>
<UIRef Id='WixUI_FeatureTree'/>
<UI><Publish Dialog='ExitDialog' Control='Finish' Event='DoAction' Value='LaunchEasyKnack'>WIXUI_EXITDIALOGOPTIONALCHECKBOX = 1 AND NOT Installed</Publish></UI>
<WixVariable Id='WixUILicenseRtf' Value='$(Xml "$work/license.rtf")'/>
</Product></Wix>
"@
$xml | Set-Content -LiteralPath "$work/product.wxs" -Encoding utf8
$license = Get-Content -LiteralPath "$Image/app/LICENSE" -Raw
$license = $license.Replace('\','\\').Replace('{','\{').Replace('}','\}').Replace("`r",'').Replace("`n",'\par ')
('{\rtf1\ansi\deff0 {\fonttbl {\f0 Arial;}}\f0\fs18 ' + $license + '}') | Set-Content -LiteralPath "$work/license.rtf" -Encoding ascii
$log = "$Destination/installer-$Type.log"
& "$WixDirectory/candle.exe" -nologo -arch x64 -out "$work/product.wixobj" "$work/product.wxs" *> $log
if ($LASTEXITCODE -ne 0) { throw "WiX compile failed: $log" }
$msi = if ($Type -eq 'msi') { $expected } else { "$work/EasyKnack-$version.msi" }
& "$WixDirectory/light.exe" -nologo -ext "$WixDirectory/WixUIExtension.dll" -sice:ICE38 -sice:ICE64 -cultures:en-us -loc "$PSScriptRoot/windows/Installer.en-us.wxl" -out $msi "$work/product.wixobj" *>> $log
# ICE38/64 expect HKCU keypaths/removal rows for each per-user file directory;
# our file components use their installed files, and MSI removes those directories.
if ($LASTEXITCODE -ne 0) { throw "WiX link failed: $log" }
if ($Type -eq 'exe') {
    @"
<Wix xmlns='http://schemas.microsoft.com/wix/2006/wi' xmlns:bal='http://schemas.microsoft.com/wix/BalExtension'>
<Bundle IconSourceFile='$(Xml $icon)' Name='EasyKnack' Version='$version' Manufacturer='EasyKnack' UpgradeCode='e393418e-2805-43c3-b492-e32794465778'>
<BootstrapperApplicationRef Id='WixStandardBootstrapperApplication.HyperlinkLicense'><bal:WixStandardBootstrapperApplication LicenseUrl='' SuppressOptionsUI='yes'/></BootstrapperApplicationRef>
<Chain><MsiPackage SourceFile='$(Xml $msi)' DisplayInternalUI='yes' /></Chain>
</Bundle></Wix>
"@ | Set-Content -LiteralPath "$work/bundle.wxs" -Encoding utf8
    & "$WixDirectory/candle.exe" -nologo -ext "$WixDirectory/WixBalExtension.dll" -out "$work/bundle.wixobj" "$work/bundle.wxs" *>> $log
    if ($LASTEXITCODE -ne 0) { throw "Bundle compile failed: $log" }
    & "$WixDirectory/light.exe" -nologo -ext "$WixDirectory/WixBalExtension.dll" -out $expected "$work/bundle.wixobj" *>> $log
    if ($LASTEXITCODE -ne 0) { throw "Bundle link failed: $log" }
}
Write-Host "Installer: $expected"
