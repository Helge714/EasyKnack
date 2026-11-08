param([ValidateSet('Debug', 'Release')][string]$Configuration = 'Debug')
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
& "$PSScriptRoot/build-driver-reference.ps1" -PrepareOnly
$manifest = Get-Content "$root/native/experimental/virtual-audio/sysvad-source-manifest.json" -Raw | ConvertFrom-Json
$upstream = "$root/target/driver-research/Windows-driver-samples"
$buildRoot = "$root/target/virtual-audio/source"
foreach ($entry in $manifest.files) {
    $destination = Join-Path $buildRoot $entry.path
    New-Item -ItemType Directory -Force (Split-Path $destination) | Out-Null
    Copy-Item -LiteralPath (Join-Path $upstream $entry.path) -Destination $destination
}
$sample = "$buildRoot/audio/sysvad"
Copy-Item "$root/native/experimental/virtual-audio/*.hpp" -Destination $sample
Copy-Item "$root/native/experimental/virtual-audio/KernelBridge.cpp" -Destination $sample

function Replace-One([string]$text, [string]$pattern, [string]$replacement) {
    $regex = [regex]::new($pattern, [Text.RegularExpressions.RegexOptions]::Singleline)
    if ($regex.Matches($text).Count -ne 1) { throw "Unexpected upstream shape: $pattern" }
    return $regex.Replace($text, [Text.RegularExpressions.MatchEvaluator]{ param($match) $replacement })
}
function Write-Source([string]$path, [string]$text) {
    [IO.File]::WriteAllText($path, $text, [Text.UTF8Encoding]::new($false))
}

$path = "$sample/adapter.cpp"
$text = '#include "KernelBridge.hpp"' + "`n" + (Get-Content $path -Raw)
$text = Replace-One $text 'DriverObject->DriverUnload = DriverUnload;' 'DriverObject->DriverUnload = DriverUnload;
    ntStatus = EasyKnackBridgeInitialize(DriverObject);
    if (!NT_SUCCESS(ntStatus)) goto Done;'
$text = Replace-One $text 'DPF\(D_TERSE, \("\[DriverUnload\]"\)\);' 'DPF(D_TERSE, ("[DriverUnload]"));
    EasyKnackBridgeShutdown();'
$text = Replace-One $text 'ntStatus =  PcInitializeAdapterDriver' '#ifdef SYSVAD_BTH_BYPASS
    g_DisableBthScoBypass = 1;
#endif
#ifdef SYSVAD_USB_SIDEBAND
    g_DisableUsbSideband = 1;
#endif
#ifdef SYSVAD_A2DP_SIDEBAND
    g_DisableA2dpSideband = 1;
#endif
    ntStatus =  PcInitializeAdapterDriver'
Write-Source $path $text

$path = "$sample/EndpointsCommon/minwavertstream.cpp"
$text = '#include "KernelBridge.hpp"' + "`n" + (Get-Content $path -Raw)
$text = Replace-One $text 'KeFlushQueuedDpcs\(\);\s*#ifdef SYSVAD_BTH_BYPASS' 'KeFlushQueuedDpcs();
    EasyKnackBridgeRelease(this);
#ifdef SYSVAD_BTH_BYPASS'
$text = Replace-One $text 'm_ulDmaMovementRate = pWfEx->nAvgBytesPerSec;' 'm_ulDmaMovementRate = pWfEx->nAvgBytesPerSec;
    if (pWfEx->nSamplesPerSec != 48000 || pWfEx->nChannels != 2 ||
        pWfEx->wBitsPerSample != 16 || pWfEx->nBlockAlign != 4 ||
        pWfEx->nAvgBytesPerSec != 192000 || m_pMiniport->IsLoopbackPin(Pin_) ||
        m_pMiniport->IsOffloadPin(Pin_)) return STATUS_NOT_SUPPORTED;
    if (!EasyKnackBridgeClaim(this, Capture_ != FALSE)) return STATUS_DEVICE_BUSY;'
$text = Replace-One $text '    if \(m_bCapture\)\s*\{\s*ReadRegistrySettings\(\);.*?(?=    //\s*// Register this stream)' ''
$text = Replace-One $text '        if \(!g_DoNotCreateDataFiles\)\s*\{\s*// Read from buffer and write to a file\.\s*ReadBytes\(ByteDisplacement\);\s*\}' '        ReadBytes(ByteDisplacement);'
$text = $text.Replace('m_ToneGenerator.GenerateSine(m_pDmaBuffer + bufferOffset, runWrite);', 'EasyKnackBridgeAudio(this, true, m_pDmaBuffer + bufferOffset, runWrite);')
$text = $text.Replace('m_SaveData.WriteData(m_pDmaBuffer + bufferOffset, runWrite);', 'EasyKnackBridgeAudio(this, false, m_pDmaBuffer + bufferOffset, runWrite);')
# Never traverse more than the currently allocated DMA ring after a delayed timer.
$text = $text.Replace('ULONG bufferOffset = m_ullLinearPosition % m_ulDmaBufferSize;', 'if (!m_ulDmaBufferSize) return;
    ByteDisplacement = min(ByteDisplacement, m_ulDmaBufferSize);
    ULONG bufferOffset = m_ullLinearPosition % m_ulDmaBufferSize;')
$text = Replace-One $text 'm_KsState\s*=\s*State_;' 'm_KsState = State_;
        EasyKnackBridgeRunning(this, State_ == KSSTATE_RUN);'
# Disable sample file-output branches even if a registry entry requests them.
$text = $text.Replace('!g_DoNotCreateDataFiles', 'false')
$text = $text.Replace('(0 == RequestedSize_)', '(RequestedSize_ > 65536 || 0 == RequestedSize_)')
$text = Replace-One $text 'm_SaveData.Disable\(drmRights->CopyProtect\);' 'EasyKnackBridgeProtect(this, drmRights->CopyProtect || drmRights->DigitalOutputDisable);'
Write-Source $path $text

$path = "$sample/TabletAudioSample/minipairs.h"
$text = Get-Content $path -Raw
$text = Replace-One $text 'g_RenderEndpoints\[\] =\s*\{.*?\};' 'g_RenderEndpoints[] = { &SpeakerMiniports };'
$text = Replace-One $text 'g_CaptureEndpoints\[\] =\s*\{.*?\};' 'g_CaptureEndpoints[] = { &MicInMiniports };'
$text = Replace-One $text 'ENDPOINT_OFFLOAD_SUPPORTED,\s*SpeakerModulesWaveFilter,\s*SIZEOF_ARRAY\(SpeakerModulesWaveFilter\),\s*&SpeakerModuleNotificationDeviceId,' 'ENDPOINT_NO_FLAGS, NULL, 0, NULL,'
Write-Source $path $text

# Advertise only the format actually transported; Windows performs application
# mixing/resampling. Keep upstream topology pin numbering, but disable offload
# and loopback instances. They must not consume the microphone queue.
foreach ($relative in @('EndpointsCommon/speakerwavtable.h', 'TabletAudioSample/micinwavtable.h')) {
    $path = "$sample/$relative"
    $text = Get-Content $path -Raw
    $names = if ($relative -match 'speaker') { @('SpeakerAudioEngineSupportedDeviceFormats','SpeakerHostPinSupportedDeviceFormats','SpeakerOffloadPinSupportedDeviceFormats') } else { @('MicInPinSupportedDeviceFormats') }
    foreach ($name in $names) {
        $format = @"
$name[] = {
    { { sizeof(KSDATAFORMAT_WAVEFORMATEXTENSIBLE), 0, 0, 0,
        STATICGUIDOF(KSDATAFORMAT_TYPE_AUDIO), STATICGUIDOF(KSDATAFORMAT_SUBTYPE_PCM),
        STATICGUIDOF(KSDATAFORMAT_SPECIFIER_WAVEFORMATEX) },
      { { WAVE_FORMAT_EXTENSIBLE, 2, 48000, 192000, 4, 16,
          sizeof(WAVEFORMATEXTENSIBLE) - sizeof(WAVEFORMATEX) },
        16, KSAUDIO_SPEAKER_STEREO, STATICGUIDOF(KSDATAFORMAT_SUBTYPE_PCM) } }
};
"@
        $text = Replace-One $text "$name\[\] =\s*\{.*?\};" $format
        $text = [regex]::Replace($text, "$name\[\d+\]", "${name}[0]")
    }
    $text = [regex]::Replace($text, '(?m)(#define\s+\w+_(?:MIN|MAX)_SAMPLE_RATE\s+)\d+', '${1}48000')
    $text = [regex]::Replace($text, '(?m)(#define\s+MICIN_DEVICE_MAX_CHANNELS\s+)1', '${1}2')
    $text = [regex]::Replace($text, '(?m)(#define\s+(?:MICIN_MAX_INPUT_STREAMS|SPEAKER_MAX_INPUT_SYSTEM_STREAMS)\s+)\d+', '${1}1')
    $text = [regex]::Replace($text, '(?m)(#define\s+SPEAKER_MAX_(?:INPUT_OFFLOAD|OUTPUT_LOOPBACK)_STREAMS\s+)\w+', '${1}0')
    Write-Source $path $text
}
$path = "$sample/TabletAudioSample/micintoptable.h"
Write-Source $path ((Get-Content $path -Raw).Replace('KSAUDIO_SPEAKER_MONO', 'KSAUDIO_SPEAKER_STEREO'))

foreach ($relative in @('EndpointsCommon/EndpointsCommon.vcxproj', 'TabletAudioSample/TabletAudioSample.vcxproj')) {
    $path = "$sample/$relative"
    $text = Get-Content $path -Raw
    if ($relative -match 'TabletAudioSample/') {
        $text = $text.Replace('<TargetName>TabletAudioSample</TargetName>', '<TargetName>EasyKnackAudio</TargetName>')
        $text = $text.Replace('portcls.lib;', 'wdmsec.lib;portcls.lib;')
        $text = Replace-One $text '<Inf Exclude="@\(Inx\)" Include="\*\.inx" />' '<Inf Include="EasyKnackAudio.inx" />'
        $text = $text.Replace('</Project>', '<ItemGroup><ClCompile Include="..\KernelBridge.cpp" /></ItemGroup></Project>')
    }
    Write-Source $path $text
}
Copy-Item "$root/native/experimental/virtual-audio/EasyKnackAudio.inx" "$sample/TabletAudioSample/EasyKnackAudio.inx"
$path = "$sample/TabletAudioSample/TabletAudioSample.rc"
Write-Source $path ((Get-Content $path -Raw).Replace('Microsoft Virtual Audio Tablet Sample Driver', 'EasyKnack Virtual Audio (SysVAD derived)').Replace('TabletAudioSample.sys', 'EasyKnackAudio.sys'))
$vswhere = "${env:ProgramFiles(x86)}/Microsoft Visual Studio/Installer/vswhere.exe"
$msbuild = & $vswhere -latest -products '*' -requires Microsoft.Component.MSBuild -find 'MSBuild\**\Bin\amd64\MSBuild.exe' | Select-Object -First 1
if (-not $msbuild) { throw '64-bit MSBuild is required.' }
foreach ($project in @('EndpointsCommon/EndpointsCommon.vcxproj','TabletAudioSample/TabletAudioSample.vcxproj')) {
    & $msbuild "$sample/$project" /nologo /m:2 /v:minimal "/p:Configuration=$Configuration" /p:Platform=x64 /p:WindowsTargetPlatformVersion=10.0.28000.0 /p:SignMode=Off
    if ($LASTEXITCODE -ne 0) { throw "EasyKnack driver build failed: $project" }
}
$package = "$root/target/virtual-audio/$Configuration/package"
New-Item -ItemType Directory -Force $package | Out-Null
foreach ($file in @('EasyKnackAudio.sys', 'EasyKnackAudio.inf')) {
    Copy-Item "$sample/TabletAudioSample/x64/$Configuration/$file" -Destination $package
}
$inf2cat = "${env:ProgramFiles(x86)}/Windows Kits/10/bin/10.0.28000.0/x86/Inf2Cat.exe"
& $inf2cat "/driver:$package" /os:10_NI_X64,10_GE_X64 /uselocaltime
if ($LASTEXITCODE -ne 0) { throw 'Driver package catalog validation failed.' }
Copy-Item "$buildRoot/LICENSE" "$package/Microsoft-SysVAD-LICENSE.txt"
Get-FileHash "$package/EasyKnackAudio.sys", "$package/EasyKnackAudio.inf", "$package/EasyKnackAudio.cat" | Format-Table -AutoSize
Write-Output 'Unsigned EasyKnack driver package built and validated; installation and hardware validation are separate steps.'
