# License and interface preflight - 2026-11-13

Scope: the unchanged EasyKnack **0.1.0** download set, release commit
`fcf85a5f61ccbe94045cf8393d4cf79c28228ca0`, and a preliminary Germany/EU
assessment. This records technical evidence and legal questions, **not legal
clearance, a warranty or a certified originality assessment**.

## Result

The checked release contains the reviewed library versions, license notices and
matching source archives. No obvious incompatible license combination was found
in the application dependencies reviewed below. This is a conditional technical
finding: notices and hashes alone cannot establish every permission or settle
whether two works form a legal combination.

Two matters must remain separate from that result:

1. Every binary recipient must have the required access to corresponding sources.
   A private GitHub release is insufficient for friends without repository access.
2. Visual inspiration and similar interaction behavior do not by themselves prove
   infringement. The actual protected artwork/overall impression and any relevant
   design or trademark rights require case-specific assessment.

## Checks on the actual release

The five files listed in the released checksum file still match their SHA-256
values. All **eight** dependency JARs match the reviewed binary lock. The source
materials contain **13** upstream archives and the existing successful exact-source
review; no missing source or unreviewed runtime JAR is reported. Required notice
files checked in the installed application are present. The detailed filenames,
hashes, source components and limitations are in
[the machine-readable evidence](legal-preflight-2026-11-13.json).

| Component | License route and checked evidence | Assessment |
| --- | --- | --- |
| EasyKnack | GPL-3.0-only; LICENSE, notices and exact project sources/build scripts accompany the release. | Source access must extend to recipients, not just repository members. |
| GLSwing | Apache-2.0; runtime matches the lock, license is embedded, matching source/build inputs are supplied. No separate upstream NOTICE was found in the captured Core input archive. | Apache-2.0 can be included in a GPLv3 work; preserve its own notices. |
| ASIO headers | The exact vendored LICENSE explicitly offers GPLv3 as an alternative to the proprietary agreement; GPLv3 is the selected route. COPYING and headers are retained. | No proprietary Steinberg agreement is assumed. ASIO trademark/logo usage remains a separate matter. |
| Loris / Ooura FFT | Loris source headers allow GPLv2 or later, used here under GPLv3. COPYING, AUTHORS, local-change provenance and the Ooura permission/attribution are retained. | Matching source and modification notices are supplied. |
| miniaudio / dr_mp3 | MIT-0 selected; complete notices retained. dr_mp3 also retains minimp3's CC0 material. | Permissive terms with retained notices; no new license mismatch identified. |
| ANTLR / JOGL / GlueGen | Exact locked artifacts; supplemental upstream public-domain/BSD/component notices are installed because several Maven JARs contain no license entry themselves. | Do not infer licensing from a single Maven label; retain the collected component notices. |
| LLVM-MinGW x64 profile | LLVM exception/component licenses and MinGW notices retained; source trees/build recipes remain in the paired download. The shipped bin directory contains 34 files. | Current profile omits Make/gendef executables, the Python interpreter and LLDB programs; some overview wording still described the older full bundle. Retained extra sources/notices are not proof those binaries ship. |
| WiX/Burn and adapted setup theme | MS-RL original notice and modified-theme sources are supplied; application sources identify the adaptation. | MS-RL is not GPL-compatible for combining code into one program. The current design treats the installer/bootstrapper and application as separate programs packaged together. That aggregation assessment is not established by a hash test and merits legal review for a binding sign-off. |
| Java / Microsoft runtimes | No JDK/runtime directory or separate VC/UCRT runtime DLL matches were found in this application image. Java is an external prerequisite. | Historical Temurin redistribution questions are not evidence that the current package bundles those files. Windows system services are a separate dependency. |

Primary references: [Apache/GPLv3 compatibility](https://apache.org/licenses/GPL-compatibility.html),
[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html),
[MS-RL terms](https://opensource.org/license/ms-rl),
[FSF license list](https://www.gnu.org/licenses/license-list.html#ms-rl).
For the particular vendored native revisions, the delivered license files are
the evidence; a newer upstream license must not silently replace them.

## Sharing the installer with friends

For the straightforward download route, provide these together to the same
recipients, without charging extra for the source:

- `EasyKnack-Setup-0.1.0-x64.exe`
- `EasyKnack-project-sources.zip`
- `EasyKnack-source-materials.zip`

Recipients only need the EXE to install; they must be able to obtain the matching
sources without needing additional repository permissions you have not granted.
A shared cloud folder is a practical way to provide equivalent access. Keep
license notices and checksums with the files. This does **not** require making
the entire Git history public. Other GPL distribution routes have their own
conditions and are not evaluated here. See [GPLv3 section 6(d)](https://www.gnu.org/licenses/gpl-3.0.html#section6)
and the [FSF FAQ](https://www.gnu.org/licenses/gpl-faq.en.html#GPLRequireSourcePostedPublic).

## Blender resemblance

The user states that the interface was independently implemented with visual
inspiration from Blender; Blender code, icons and theme files were not taken.
Slider/editor interaction is intentionally similar. This is an authorship
statement, not an independently proven history of every asset.

The inspected application sources contain no Blender/Bforartists attribution or
identifier hits. That negative search is **not** proof of originality. Core
implements the controls in Java; the published default theme selects the logical
font family `SansSerif`. No standalone font files or TTF/OTF/WOFF resources were
found in the application/dependency JARs checked. The 15 Core SVG resource names
are listed in the evidence. Their authorship is not certified by the umbrella
Apache license. Earlier project evidence records author permission for the
EasyKnack icon/splash.

The available node export shows dark gray nodes, a colored heading, input/output
sockets and compact numerical fields. These are concrete similarities worth
documenting, but their presence is not a legal infringement test. The EasyKnack
icon inspected is a rotary-knob image, not the Blender logo.

| Concern | Preliminary conclusion |
| --- | --- |
| Drag-to-change values, modifiers, direct numeric entry and node wiring | Functional behavior/ideas are distinguished from protected program expression. Close independently implemented behavior alone is not sufficient to conclude program-copyright infringement. |
| Specific graphics and overall screen composition | A sufficiently original GUI can receive copyright protection as a work. Functional-only elements and the creative selection/arrangement must be distinguished. No percentage-similarity safe harbor is claimed. |
| Blender name/logo or implied endorsement | Blender has a separate trademark policy. Do not present EasyKnack as an official Blender product or use Blender branding as its own. Own branding reduces one source of confusion but is not complete clearance. |
| Copied Blender icons/code, if later discovered | Reassess their exact licenses and attribution. In particular, importing GPL Blender code into an Apache-only GLSwing library would need separate treatment; the application's GPL license would not automatically resolve Core's licensing. |
| Design rights / unfair competition | Copyright is not the only possible right. No design/trademark register search or case-specific unfair-competition opinion has been performed. |

Legal references: [UrhG section 69a](https://www.gesetze-im-internet.de/urhg/__69a.html),
[CJEU C-406/10, SAS Institute](https://eur-lex.europa.eu/legal-content/en/SUM/?uri=CELEX%3A62010CJ0406),
[CJEU C-393/09, BSA, paragraphs 45-51](https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=celex%3A62009CJ0393),
[UWG section 4(3)](https://www.gesetze-im-internet.de/uwg_2004/__4.html),
[DesignG section 38](https://www.gesetze-im-internet.de/geschmmg_2004/__38.html),
[Blender trademark policy](https://www.blender.org/about/trademark-policy/),
[Blender FAQ on artwork/icons](https://www.blender.org/support/faq/).

## Visual and legal limits

The initial preflight used historical application images and could not complete
live capture. That limitation has now been followed up by a
[live EasyKnack 0.1.0 / Blender 4.3.2 comparison](GUI-COMPARISON-2026-11-13.md).
The release identity was confirmed in About, representative windows/controls were
inspected and three screenshots retained locally. This replaces the earlier
live-capture gap for the states actually listed in that report; it is not
exhaustive editor/theme coverage or a pixel-similarity/legal-clearance test.

For a binding publication assessment, an IP/software lawyer should review the
actual release screens, this dependency/source record and the installer license
boundary. No patent search, exhaustive code-origin comparison, ownership-contract
review or trademark/design-register clearance was conducted. A general disclaimer
or changing a few colors would not substitute for that assessment.

## Making the existing repository public

Changing repository visibility also exposes reachable historical revisions. A
filename-level screen of reachable Git objects found no EXE/DLL/JAR or font paths;
the sole archive path was an old routing benchmark ZIP, whose entries are CSV
measurements and JSON metadata. The three artwork paths are the recorded product
icon/splash files. This is a useful narrow screen, not a historical source-license,
ownership or secret scan. The current-release preflight must not be presented as
clearance for every earlier revision. A separately distributed clean release
source snapshot does not require publishing the entire development history.
