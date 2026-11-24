#Requires -Version 7.0
param(
    [Parameter(Mandatory=$true)][string]$Image,
    [string]$JdkHome, # Retained as a deprecated build-script argument; never bundled.
    [string]$WixDirectory,
    [ValidateSet('exe','msi')][string]$Type = 'exe',
    [string]$Destination,
    [ValidateSet('Archive','Files')][string]$CompilerLayout = 'Archive'
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
foreach ($name in @('candle.exe','light.exe','WixUIExtension.dll','WixBalExtension.dll','WixUtilExtension.dll')) {
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
$compiler = [IO.Path]::GetFullPath((Join-Path $Image 'app/tools/llvm-mingw'))
$archiveCompiler = $CompilerLayout -eq 'Archive' -and (Test-Path -LiteralPath $compiler -PathType Container)
$compilerActions = ''
if ($archiveCompiler) {
    . "$PSScriptRoot/SourcePackage.ps1"
    $inventory = [EasyKnackPackageFiles]::Inventory($compiler)
    $entries = [ordered]@{}
    $manifest = [Text.StringBuilder]::new("EasyKnack compiler archive v1`n")
    [long]$allocated = 0
    foreach ($item in $inventory) {
        $relative = $item.path.Replace('\','/')
        $entries[$relative] = Join-Path $compiler $item.path
        [void]$manifest.Append("$($item.sha256)`t$($item.bytes)`t$relative`n")
        # Conservative 64 KiB allocation allowance, including per-file metadata.
        $allocated += ([long][Math]::Ceiling($item.bytes / 65536.0) + 1) * 65536
    }
    $entries['.easyknack-compiler.manifest'] = [Text.Encoding]::UTF8.GetBytes($manifest.ToString())
    $compilerZip = Join-Path $work 'llvm-mingw.zip'
    Write-DeterministicZip $compilerZip $entries
    $compilerHash = Join-Path $work 'compiler.sha256'
    [IO.File]::WriteAllText($compilerHash,(Get-FileHash -LiteralPath $compilerZip -Algorithm SHA256).Hash)
    # ReserveCost uses bytes, independently of MSI's archive-file cost. Split large
    # totals into signed-32-bit rows. Covers extracted files plus transaction staging.
    $reserveXml = ''
    [long]$remaining = 2 * $allocated + 4MB
    $reserveIndex = 0
    while ($remaining -gt 0) {
        [long]$chunk = [Math]::Min($remaining, 2000000000)
        $reserveXml += "<ReserveCost Id='CompilerSpace$reserveIndex' Directory='INSTALLDIR' RunLocal='$chunk' RunFromSource='0'/>"
        $reserveIndex++
        $remaining -= $chunk
    }
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
    if ($archiveCompiler -and [IO.Path]::GetRelativePath($Image,$folder).Replace('\','/') -eq 'app/tools') {
        $components.Add('CompilerArchive')
        [void]$builder.Append("<Component Id='CompilerArchive' Guid='*' Win64='yes'><File Id='CompilerArchiveFile' Name='llvm-mingw.zip' Source='$(Xml $compilerZip)' KeyPath='yes'/>$reserveXml</Component>")
    }
    foreach ($child in Get-ChildItem -LiteralPath $folder -Directory | Sort-Object Name) {
        if ($archiveCompiler -and $child.FullName -eq $compiler) { continue }
        [void]$builder.Append("<Directory Id='$(Id 'D' ([IO.Path]::GetRelativePath($Image,$child.FullName)))' Name='$(Xml $child.Name)'>$(DirectoryXml $child.FullName)</Directory>")
    }
    $builder.ToString()
}
$tree = DirectoryXml $Image
$refs = ($components | ForEach-Object { "<ComponentRef Id='$_'/>" }) -join ''
$icon = Join-Path $Image 'app/EKIcon.ico'
if (!(Test-Path -LiteralPath $icon)) { throw "Missing packaged application icon: $icon" }
$launcher = Id 'F' 'EasyKnack.exe'
$csc = "$env:WINDIR/Microsoft.NET/Framework64/v4.0.30319/csc.exe"
$javaSetup = Join-Path $work 'JavaSetup.exe'
$javaSetupLog = Join-Path $work 'java-prerequisite-build.log'
& $csc /nologo /target:winexe /platform:x64 /reference:System.Windows.Forms.dll "/out:$javaSetup" `
    "/resource:$((Resolve-Path "$PSScriptRoot/windows/JavaPrerequisite.ps1").Path),JavaPrerequisite.ps1" `
    "/resource:$((Resolve-Path "$PSScriptRoot/windows/JavaSetup.ps1").Path),JavaSetup.ps1" `
    (Resolve-Path "$PSScriptRoot/windows/JavaSetup.cs").Path *> $javaSetupLog
if ($LASTEXITCODE -ne 0) { throw "Java prerequisite build failed: $javaSetupLog" }

if ($archiveCompiler) {
    $compilerSetup = Join-Path $work 'CompilerSetup.exe'
    $compilerSetupLog = Join-Path $work 'compiler-setup-build.log'
    & $csc /nologo /target:winexe /platform:x64 /reference:System.IO.Compression.dll /reference:System.IO.Compression.FileSystem.dll "/out:$compilerSetup" "/resource:$compilerHash,compiler.sha256" (Resolve-Path "$PSScriptRoot/windows/CompilerSetup.cs").Path *> $compilerSetupLog
    if ($LASTEXITCODE -ne 0) { throw "Compiler setup build failed: $compilerSetupLog" }
    $compilerActions = @"
<Binary Id='CompilerSetup' SourceFile='$(Xml $compilerSetup)'/>
<CustomAction Id='RollbackCompiler' BinaryKey='CompilerSetup' ExeCommand='rollback &quot;[INSTALLDIR].&quot;' Execute='rollback' Impersonate='yes' Return='check'/>
<CustomAction Id='RemoveCompiler' BinaryKey='CompilerSetup' ExeCommand='remove &quot;[INSTALLDIR].&quot;' Execute='deferred' Impersonate='yes' Return='check'/>
<CustomAction Id='InstallCompiler' BinaryKey='CompilerSetup' ExeCommand='install &quot;[INSTALLDIR].&quot;' Execute='deferred' Impersonate='yes' Return='check'/>
<CustomAction Id='PublishCompiler' BinaryKey='CompilerSetup' ExeCommand='publish &quot;[INSTALLDIR].&quot;' Execute='deferred' Impersonate='yes' Return='check'/>
<CustomAction Id='CommitCompiler' BinaryKey='CompilerSetup' ExeCommand='commit &quot;[INSTALLDIR].&quot;' Execute='commit' Impersonate='yes' Return='ignore'/>
<InstallExecuteSequence>
<Custom Action='RollbackCompiler' Before='RemoveFiles'>NOT UPGRADINGPRODUCTCODE</Custom>
<Custom Action='RemoveCompiler' After='RollbackCompiler'>REMOVE~="ALL" AND NOT UPGRADINGPRODUCTCODE</Custom>
<Custom Action='InstallCompiler' After='InstallFiles'>NOT (REMOVE~="ALL") AND NOT UPGRADINGPRODUCTCODE</Custom>
<Custom Action='PublishCompiler' After='RemoveExistingProducts'>NOT (REMOVE~="ALL") AND NOT UPGRADINGPRODUCTCODE</Custom>
<Custom Action='CommitCompiler' Before='InstallFinalize'>NOT UPGRADINGPRODUCTCODE</Custom>
</InstallExecuteSequence>
<UI><ProgressText Action='InstallCompiler'>Preparing the C++ compiler</ProgressText><ProgressText Action='RemoveCompiler'>Removing the C++ compiler</ProgressText></UI>
"@
}

# Separate optional features provide persistent, independent shortcut choices.
$xml = @"
<Wix xmlns='http://schemas.microsoft.com/wix/2006/wi'>
<Product Id='*' Name='EasyKnack' Language='1033' Version='$version' Manufacturer='EasyKnack' UpgradeCode='50cf4639-3e07-4dd7-8ccf-14eb8df748c6'>
<Package InstallerVersion='500' Compressed='yes' InstallScope='perUser' Platform='x64'/>
<MajorUpgrade Schedule='afterInstallExecute' AllowSameVersionUpgrades='yes' DowngradeErrorMessage='A newer EasyKnack version is already installed.'/>
<MediaTemplate EmbedCab='yes'/>
<Icon Id='EasyKnackIcon' SourceFile='$(Xml $icon)'/><Property Id='ARPPRODUCTICON' Value='EasyKnackIcon'/>
<Condition Message='EasyKnack requires 64-bit Windows.'>VersionNT64</Condition>
<Directory Id='TARGETDIR' Name='SourceDir'>
<Directory Id='LocalAppDataFolder'><Directory Id='ProgramsFolder' Name='Programs'><Directory Id='INSTALLDIR' Name='EasyKnack'>$tree
<Component Id='FileAssociation' Guid='*' Win64='yes'>
<RegistryValue Root='HKCU' Key='Software\EasyKnack\Installer' Name='InstallPath' Value='[INSTALLDIR]' Type='string'/>
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
<Feature Id='StartMenu' Title='Start Menu shortcut' Level='1'><Condition Level='0'>EK_BUNDLE=1 AND EK_STARTMENU=0</Condition><Condition Level='1'>EK_BUNDLE=1 AND EK_STARTMENU=1</Condition><ComponentRef Id='MenuShortcut'/></Feature>
<Feature Id='Desktop' Title='Desktop shortcut' Level='2'><Condition Level='0'>EK_BUNDLE=1 AND EK_DESKTOP=0</Condition><Condition Level='1'>EK_BUNDLE=1 AND EK_DESKTOP=1</Condition><ComponentRef Id='DesktopShortcut'/></Feature>
</Feature>
<Property Id='INSTALLLEVEL' Value='1'/>
<Property Id='EK_BUNDLE' Secure='yes'/><Property Id='EK_STARTMENU' Secure='yes'/><Property Id='EK_DESKTOP' Secure='yes'/>
<Property Id='ARPNOMODIFY' Value='1'/><Property Id='ARPINSTALLLOCATION' Value='[INSTALLDIR]'/>

<Property Id='WIXUI_EXITDIALOGOPTIONALCHECKBOXTEXT' Value='Check Java prerequisites and start EasyKnack'/>
<Property Id='WIXUI_EXITDIALOGOPTIONALCHECKBOX' Value='1'/>
$compilerActions
<Binary Id='JavaPrerequisite' SourceFile='$(Xml $javaSetup)'/>
<CustomAction Id='CheckJavaBeforeInstall' BinaryKey='JavaPrerequisite' ExeCommand='/check /ui [UILevel]' Execute='immediate' Impersonate='yes' Return='check'/>
<InstallExecuteSequence><Custom Action='CheckJavaBeforeInstall' Before='InstallValidate'>NOT (REMOVE~="ALL") AND NOT EK_BUNDLE</Custom></InstallExecuteSequence>
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
<Wix xmlns='http://schemas.microsoft.com/wix/2006/wi' xmlns:bal='http://schemas.microsoft.com/wix/BalExtension' xmlns:util='http://schemas.microsoft.com/wix/UtilExtension'>
<Bundle IconSourceFile='$(Xml $icon)' Name='EasyKnack' Version='$version' Manufacturer='EasyKnack' UpgradeCode='e393418e-2805-43c3-b492-e32794465778'>
<Variable Name='InstallFolder' Type='string' Value='[LocalAppDataFolder]Programs\EasyKnack' Persisted='yes'/>
<Variable Name='StartMenuShortcut' Type='numeric' Value='1' Persisted='yes'/>
<Variable Name='DesktopShortcut' Type='numeric' Value='0' Persisted='yes'/>
<util:RegistrySearch Id='HasInstalledFolder' Variable='HasInstalledFolder' Root='HKCU' Key='Software\EasyKnack\Installer' Value='InstallPath' Result='exists' Win64='yes'/>
<util:RegistrySearch Id='InstalledFolder' Variable='InstallFolder' Root='HKCU' Key='Software\EasyKnack\Installer' Value='InstallPath' Result='value' Win64='yes' After='HasInstalledFolder' Condition='HasInstalledFolder'/>
<util:RegistrySearch Id='HasInstalledStartMenu' Variable='HasInstalledStartMenu' Root='HKCU' Key='Software\EasyKnack\Installer' Value='StartMenu' Result='exists' Win64='yes'/>
<util:RegistrySearch Id='InstalledStartMenu' Variable='StartMenuShortcut' Root='HKCU' Key='Software\EasyKnack\Installer' Value='StartMenu' Result='value' Win64='yes' After='HasInstalledStartMenu' Condition='HasInstalledStartMenu'/>
<util:RegistrySearch Id='HasInstalledDesktop' Variable='HasInstalledDesktop' Root='HKCU' Key='Software\EasyKnack\Installer' Value='Desktop' Result='exists' Win64='yes'/>
<util:RegistrySearch Id='InstalledDesktop' Variable='DesktopShortcut' Root='HKCU' Key='Software\EasyKnack\Installer' Value='Desktop' Result='value' Win64='yes' After='HasInstalledDesktop' Condition='HasInstalledDesktop'/>
<BootstrapperApplicationRef Id='WixStandardBootstrapperApplication.RtfLicense'>
<bal:WixStandardBootstrapperApplication LicenseFile='$(Xml "$work/license.rtf")'
 ThemeFile='$(Xml "$PSScriptRoot/windows/InstallerTheme.xml")' LocalizationFile='$(Xml "$PSScriptRoot/windows/InstallerTheme.en-us.wxl")'
 LaunchTarget='[InstallFolder]\EasyKnack.exe' LaunchWorkingFolder='[InstallFolder]' LaunchHidden='yes'/>
</BootstrapperApplicationRef>
<Chain>
<ExePackage Id='JavaPrerequisite' SourceFile='$(Xml $javaSetup)' DisplayName='Checking Java 25 (64-bit JDK)'
 InstallCommand='/ui [WixBundleUILevel]' RepairCommand='/ui [WixBundleUILevel]' DetectCondition='0'
 PerMachine='no' Permanent='yes' Vital='yes' Cache='no'/>
<MsiPackage SourceFile='$(Xml $msi)' DisplayInternalUI='no' Visible='no'>
<MsiProperty Name='MSIFASTINSTALL' Value='5'/>
<MsiProperty Name='INSTALLDIR' Value='[InstallFolder]'/><MsiProperty Name='EK_BUNDLE' Value='1'/>
<MsiProperty Name='EK_STARTMENU' Value='[StartMenuShortcut]'/><MsiProperty Name='EK_DESKTOP' Value='[DesktopShortcut]'/>
</MsiPackage></Chain>
</Bundle></Wix>
"@ | Set-Content -LiteralPath "$work/bundle.wxs" -Encoding utf8
    & "$WixDirectory/candle.exe" -nologo -ext "$WixDirectory/WixBalExtension.dll" -ext "$WixDirectory/WixUtilExtension.dll" -out "$work/bundle.wixobj" "$work/bundle.wxs" *>> $log
    if ($LASTEXITCODE -ne 0) { throw "Bundle compile failed: $log" }
    & "$WixDirectory/light.exe" -nologo -ext "$WixDirectory/WixBalExtension.dll" -ext "$WixDirectory/WixUtilExtension.dll" -out $expected "$work/bundle.wixobj" *>> $log
    if ($LASTEXITCODE -ne 0) { throw "Bundle link failed: $log" }
}
Write-Host "Installer: $expected"
