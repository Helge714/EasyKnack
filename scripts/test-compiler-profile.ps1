#Requires -Version 7.0
$ErrorActionPreference='Stop'
if (!('EasyKnackPackageFiles' -as [type])) { Add-Type -Path "$PSScriptRoot/PackageFiles.cs" }
$work=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot ('../target/compiler-profile-test-'+[guid]::NewGuid().ToString('N'))))
$source=Join-Path $work 'source';$destination=Join-Path $work 'selected'
$files=@('LICENSE.TXT','bin/clang++.exe','bin/clang.exe','bin/clang-20.exe','bin/ld.lld.exe',
 'bin/libLLVM-20.dll','bin/libclang-cpp.dll','bin/libc++.dll','bin/libunwind.dll',
 'bin/mingw32-common.cfg','bin/x86_64-w64-windows-gnu.cfg','include/c++/v1/cstdint',
 'lib/clang/20/include/stddef.h','x86_64-w64-mingw32/lib/libc++.a','share/licenses/LLVM.txt',
 'armv7-w64-mingw32/lib/unused.a','aarch64-w64-mingw32/lib/unused.a','i686-w64-mingw32/lib/unused.a',
 'python/unused.py','bin/liblldb.dll','bin/aarch64-w64-mingw32-clang.exe')
foreach ($file in $files) { $path=Join-Path $source $file;[IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($path))|Out-Null;[IO.File]::WriteAllText($path,$file) }
$before=[EasyKnackPackageFiles]::Inventory($source)|ConvertTo-Json -Depth 3 -Compress
[EasyKnackPackageFiles]::CopyCompiler($source,$destination)
foreach ($file in $files[0..14]) { if (!(Test-Path -LiteralPath (Join-Path $destination $file))) { throw "Required profile file missing: $file" } }
foreach ($file in $files[15..20]) { if (Test-Path -LiteralPath (Join-Path $destination $file)) { throw "Excluded profile file present: $file" } }
$after=[EasyKnackPackageFiles]::Inventory($source)|ConvertTo-Json -Depth 3 -Compress
if ($before -cne $after) { throw 'Source toolchain changed' }
$refused=$false
try { [EasyKnackPackageFiles]::CopyCompiler($source,$destination) } catch { $refused=$true }
if (!$refused) { throw 'Existing profile destination was overwritten' }
$refused=$false
try { [EasyKnackPackageFiles]::CopyCompiler($source,(Join-Path $source 'nested')) } catch { $refused=$true }
if (!$refused) { throw 'Nested destination was accepted' }
Write-Host 'Compiler profile retention/exclusion, source preservation and destination checks passed.'
