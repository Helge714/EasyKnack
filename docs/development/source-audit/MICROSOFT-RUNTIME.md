# Microsoft runtime redistribution review

Checked 2026-10-07. This records source evidence and its limits, not a legal
certification. Scope: the Microsoft DLLs in the audited Temurin Windows x64
runtime. No runtime files were changed and no inquiry was submitted.

## What is established

The earlier archive comparison established that the runtime files match the
official Temurin archive. The new [file inventory and document hashes](microsoft-runtime-evidence.json)
identify all 19 Microsoft DLLs individually.

Adoptium's [DevKit reproduction instructions](https://github.com/adoptium/temurin-build/wiki/Temurin-reproducible-verification-instructions-for-building-current-Windows-ReDist-DevKit-DLL-package-%28vs2022_redist_14.40.33807_10.0.26100.1742%29)
specify Visual Studio 17.10.3, toolset 14.40.33807 and Windows SDK
10.0.26100.1742. Its [copy recipe](https://github.com/adoptium/temurin-build/blob/master/docs/CreateWindowsReDistDevKit.md)
takes the three VC DLLs from the MSVC Redist directory and UCRT files from the SDK
Redist directory. This documents their upstream origin; we did not independently
rebuild that DevKit or compare its DLLs to a fresh Microsoft installation.

| Bundled files | Origin and applicable document family |
|---|---|
| `msvcp140.dll`, `vcruntime140.dll`, `vcruntime140_1.dll` | MSVC release runtime; Visual Studio product/component terms and VS 2022 REDIST list |
| `ucrtbase.dll` and 15 `api-ms-win-crt-*.dll` files | Windows SDK UCRT; SDK section 2(a) and SDK REDIST list |

## Actual license documents inspected

### Windows SDK

Downloaded the installer linked by Adoptium for 10.0.26100.1742, checked its valid
Microsoft Authenticode signature and extracted `UX/license.rtf` using WiX dark.
The installer was **not run**. Embedded package metadata reports
10.1.26100.1742. The EULA ID is `WIN10SDK.RTM.AUG_2018_en-US`; that older identifier
is present in this specific installer, not inferred from a generic web copy.

Section 2(a) permits distribution of listed object code and permits the licensee
to authorize distributors of its programs to redistribute that code with them.
It requires substantial application functionality, protective terms agreed by
recipients, a valid program copyright notice and an indemnity to Microsoft.
It also restricts platform, notices, branding and relicensing. Conditions for
other SDK components are not automatically relevant to these DLLs.

The EULA's REDIST link resolves to Microsoft's [SDK list](https://learn.microsoft.com/en-us/legal/windows-sdk/redist).
All 16 bundled UCRT filenames appear in its x64 section, which permits these
unmodified binaries with Classic Windows applications, subject to the SDK terms.

### Visual Studio

The [Build Tools terms](https://visualstudio.microsoft.com/license-terms/vs2022-ga-diagnosticbuildtools/)
(`VS_2022_Tools_2024Mar_ENU.1033`) describe use/build rights and separate component
agreements. They do not themselves provide a general runtime redistribution
grant. Installing Build Tools alone is therefore not evidence of that grant.

The [Community 2022 terms](https://visualstudio.microsoft.com/license-terms/vs2022-ga-community/)
(`VS_Comm_2022_ENU.1033`), section 4, provide a conditional object-code distribution
grant and allow distributors of applications to redistribute the included code.
They require substantial application functionality and protective terms agreed
by recipients. They restrict prerelease components, branding and certain
relicensing. This establishes a documented product-license route, but does not
prove which Visual Studio edition/agreement Adoptium used or that EasyKnack has
acquired a direct license under that route.

The [VS 2022 REDIST list](https://learn.microsoft.com/en-us/visualstudio/releases/2022/redistribution)
covers release files in `VC/redist`, excludes debug directories and makes
redistribution conditional on the applicable license. It is a file list, not an
independent unconditional license.

## What remains to establish before public distribution

The DLLs are identified redistributable components, not unexplained binaries.
However, a clause allowing an upstream licensee to authorize its distributors
does not by itself document that authorization and all recipient conditions in
our specific distribution chain.

1. Record the terms through which Adoptium authorizes downstream redistribution
   of its bundled Microsoft components, or establish a separately applicable
   direct Microsoft licensing route. Do not silently accept a new agreement for
   the project owner or assume a paid license is necessary.
2. Determine how the required recipient agreement is satisfied for these
   components in both installer and portable packages. A notice file by itself
   does not demonstrate agreement. Do not impose Microsoft restrictions on
   EasyKnack's own GPL code or relicense the Microsoft DLLs as GPL.
3. Preserve the unchanged DLLs and their notices and verify the final package.
   Keep corresponding-source obligations for OpenJDK separate from Microsoft
   proprietary DLLs. Adding OpenJDK source does not settle their terms.

The useful remaining upstream question is now about the downstream authorization
and recipient terms, not where the DLLs came from or whether REDIST lists exist.
The [draft inquiry](UPSTREAM-QUESTIONS.md) is updated accordingly. No evidence here
establishes that Temurin is prohibited from redistribution or requires purchase.

Only provenance/attribution information is added to the package overview in this
change. No guessed Microsoft EULA is installed as binding end-user terms and no
publication-readiness checkbox is marked complete.
