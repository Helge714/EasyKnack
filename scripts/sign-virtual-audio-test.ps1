param([ValidateSet('Debug', 'Release')][string]$Configuration = 'Debug')
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$source = "$root/target/virtual-audio/$Configuration/package"
$destination = "$root/target/virtual-audio/$Configuration/test-signed"
$tools = "${env:ProgramFiles(x86)}/Windows Kits/10/bin/10.0.28000.0"
$record = "$root/target/virtual-audio/test-certificate-thumbprint.txt"
foreach ($file in @('EasyKnackAudio.sys', 'EasyKnackAudio.inf')) {
    if (-not (Test-Path "$source/$file")) { throw "Build the driver first: missing $file" }
}
$certificate = $null
if (Test-Path $record) {
    $thumbprint = (Get-Content $record -Raw).Trim()
    if ($thumbprint -notmatch '^[A-Fa-f0-9]{40}$') { throw 'Invalid certificate record.' }
    $certificate = Get-Item "Cert:/CurrentUser/My/$thumbprint" -ErrorAction SilentlyContinue
}
if (-not $certificate -or $certificate.NotAfter -le (Get-Date) -or -not $certificate.HasPrivateKey) {
    $certificate = New-SelfSignedCertificate -Type CodeSigningCert -Subject 'CN=EasyKnack Development Test Only' `
        -CertStoreLocation Cert:/CurrentUser/My -KeyAlgorithm RSA -KeyLength 2048 `
        -HashAlgorithm SHA256 -KeyExportPolicy NonExportable -NotAfter (Get-Date).AddMonths(6)
    $certificate.Thumbprint | Set-Content $record
}
New-Item -ItemType Directory -Force $destination | Out-Null
Copy-Item "$source/EasyKnackAudio.sys", "$source/EasyKnackAudio.inf", "$source/Microsoft-SysVAD-LICENSE.txt" $destination
Export-Certificate -Cert $certificate -FilePath "$destination/EasyKnack-Test.cer" | Out-Null
& "$tools/x64/signtool.exe" sign /fd SHA256 /s My /sha1 $certificate.Thumbprint "$destination/EasyKnackAudio.sys"
if ($LASTEXITCODE -ne 0) { throw 'SYS test signature failed.' }
# Catalog hashes must be generated after the embedded SYS signature.
& "$tools/x86/Inf2Cat.exe" "/driver:$destination" /os:10_NI_X64,10_GE_X64 /uselocaltime
if ($LASTEXITCODE -ne 0) { throw 'Signed-image catalog generation failed.' }
& "$tools/x64/signtool.exe" sign /fd SHA256 /s My /sha1 $certificate.Thumbprint "$destination/EasyKnackAudio.cat"
if ($LASTEXITCODE -ne 0) { throw 'Catalog test signature failed.' }
foreach ($file in @('EasyKnackAudio.sys', 'EasyKnackAudio.cat')) {
    $signature = Get-AuthenticodeSignature "$destination/$file"
    if ($signature.SignerCertificate.Thumbprint -ne $certificate.Thumbprint -or
        $signature.Status -in @('HashMismatch', 'NotSigned')) { throw "Invalid signature: $file" }
    Write-Output "$file signature status: $($signature.Status)"
}
Write-Output "Test package: $destination"
Write-Output "Certificate: $($certificate.Thumbprint), expires $($certificate.NotAfter.ToString('yyyy-MM-dd'))"
Write-Output 'No trusted certificate store, device installation or boot configuration was changed.'
