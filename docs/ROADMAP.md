# Dictate Custom roadmap and release state

Last updated: 2026-10-20 onboarding. This is the owner's agreed **provisional** release
sequence. Discuss evidence-based changes before changing order or design. Completing
one task does not authorize the next feature. See [project context](PROJECT_CONTEXT.md),
[decision records](decisions/) and [agent policy](../AGENTS.md).

## Status vocabulary and current scope

Planned = proposed future work; approved = owner selected requirements/scope;
implemented = code exists; merged = integrated into main; signed = actual APK signature
verified (or explicitly attributed historical report); device-tested = recorded physical
acceptance evidence; published = release made available through an authorized channel.
These states are separate. In particular, automated tests do not establish device success.

Onboarding authorizes documentation inspection, a documentation-only branch, commit
and non-force push. It does not authorize merge, new transcription implementation,
APK distribution, device installation, upstream PRs, tags or release publication.

## Repository baseline verified during onboarding

- Initial local branch `work`, clean working tree, HEAD
  `99805943618077059d95204f34407cfb7f8e92ed`.
- Live GitHub `main` and `feature/custom-wear-companion` point to that same commit.
  Local cached `origin/main` initially pointed to
  `f77871bd97b9441b3315a1abe4ce22986937923a`; it was stale.
- `origin` is `https://github.com/jack-chung/dictate-keyboard.git`; no upstream remote
  was configured. No upstream merge was attempted.
- Three changes form a direct parent chain, independently checked in Git:

| Feature | Commit | Source / merge status |
| --- | --- | --- |
| Groq Cantonese request compatibility | `70a3b2b85d053870c5bd6c9b38645f8dfe55b498` | Implemented; ancestor of live main |
| Custom phone identity | `997d4c09853846230f5d4196a89b98709c2fd6ad` | Implemented; direct child of Groq fix; ancestor of live main |
| Custom Wear companion and capability keep rule | `99805943618077059d95204f34407cfb7f8e92ed` | Implemented; direct child of phone change; live main |

The original branches `fix/groq-cantonese-language` and `feature/custom-application-id`
remain on GitHub at their respective feature commits. Linear ancestry is verified;
the historical fast-forward integration procedure itself was not replayed.
GitHub's releases collection was empty during onboarding, and a live
`git ls-remote --tags origin '*jack*'` returned no matching custom tags. No release/tag
was created by this task. Recheck live refs before future integration or release operations.

## Release 1 — Dictate Custom 6.4.0-jack.1

**Implemented and merged: source verified. Signed: historically reported, pending
artifact recovery/reverification. Device-tested: no acceptance results supplied.
Published: no GitHub release; private distribution pending approval.**

Scope: Groq regional Cantonese normalization; independently installable custom phone;
matching custom Wear companion; later production-signing preparation. Both custom
form factors are configured as `net.devemperor.dictate.jack`, code 143, name
`6.4.0-jack.1`. Full original/custom identity table is in [project context](PROJECT_CONTEXT.md).

Historical validation, not rerun during onboarding:

- Groq stage: 225 core tests, including 40 new language regression cases, and Android
  Kotlin compilation reported passing. Original live-failure root cause not proven.
- Phone identity stage: original/custom variants and packaging checks reported passing;
  1,205 phone tests passed using a JDK 21 test worker. The older guide records a JDK 17
  grapheme assertion discrepancy; do not modify product behavior to mask it.
- Wear/release preparation: 229 core tests and 1,205 phone tests reported passing;
  original/custom builds, manifests, IME metadata, provider isolation, capability,
  minimum/target SDK checks reported successful. Earlier Wear module tests were
  NO-SOURCE, not an executed watch unit suite.
- Later signed-artifact preparation reportedly verified production signatures and
  matching certificates. This is separate from earlier unsigned implementation APKs.
- No installation or physical pairing/transcription was performed in the reported
  release preparation. Installed original-ID certificate comparison was not completed.

### Release 1 artifact record

All values below are **owner-reported historical expectations**, not independently
verified artifact properties. Certificate fingerprints and APK hashes are public
verification metadata, not signing credentials.

Expected signing certificate SHA-256 (both):
`b24d288b92a0dc8c0e76ec49a4cba6c4d2b6ed6b93de59cbea6c3f996a201756`.

| APK | Reported bytes | Reported SHA-256 |
| --- | --- | --- |
| `Dictate-Custom-6.4.0-jack.1-Phone.apk` | 146,145,256 | `f4993fa6b48c9e111e075eb28361fb70a650fd5a3ad20e94cd5be76e250bdf17` |
| `Dictate-Custom-6.4.0-jack.1-Wear.apk` | 45,655,149 | `50ad3b547d1f681f912a4188e7abe8260600ab1c45e8af5f21abe373d5487bb2` |

Last reported preparation directory: `/workspace/dictate-release-6.4.0-jack.1`.
**Onboarding observation:** that directory and the two named custom APKs are absent
in this environment. A filename search including ignored build directories found:

- One older `app/build/outputs/apk/debug/app-debug.apk`, 173,913,923 bytes. Actual
  `aapt2 dump badging` reports `net.devemperor.dictate.debug`, code 142,
  `6.4.0-debug+f77871bd`, min SDK 26 / target 36. Actual
  `apksigner verify --print-certs` succeeds with Android Debug certificate SHA-256
  `ec431eaf65427ce3d4671972bc355b173c736d692019417d286dc451eb38723d`.
  This is not a production custom release candidate.
- 21 retained core `TEST-*.xml` suites totaling 185 tests, zero recorded failures,
  errors or skips, dated 2026-10-11. Groq language and new Wear compatibility suites
  are absent. These are older report files, not tests rerun or evidence of current HEAD.

This does not establish that release artifacts are absent from another task or storage.
Project Google Drive folder access and artifact availability there were not checked.
No signing assets were accessed, copied, rebuilt or re-signed during onboarding.

### Immediate next action — awaiting owner authorization

After onboarding review, locate the **existing** signed phone/watch APKs using a
supported transfer or the dedicated private project Google Drive folder. Check exact
sizes and SHA-256, actual `apksigner` signatures/certificates, package/version/form-factor
metadata and appropriate private destination permissions before making them available
for device testing. Reuse verified artifacts; do not rebuild or re-sign unnecessarily.
Distribution authorization does not automatically authorize device installation or a
public release. Never place signing assets or credential-bearing backups with public assets.

Blockers: current local artifacts unavailable; private recovery/distribution not yet
authorized; physical acceptance outstanding. If artifacts cannot be recovered, discuss
the evidence and a build/signing plan rather than silently replacing them.

### Physical acceptance checklist

All boxes are **pending**. Record device/OS, APK hash, test date and observed result
when actual tests occur. Existing build/source evidence cannot check a box.

Samsung Galaxy Z Fold7:

- [ ] Original and custom apps install side by side with independent data.
- [ ] Both keyboards appear independently in Android settings; custom can be enabled/selected.
- [ ] Original continues operating and can be selected again.
- [ ] Custom microphone recording works.
- [ ] Transcription and insertion work in intended text fields.
- [ ] Groq Whisper accepts Cantonese (Hong Kong) and completes faithful transcription.
- [ ] Original settings/prompt backup import succeeds; version warnings are understood.
- [ ] Provider credentials are usable or privately re-entered.
- [ ] Offline models work after downloading them separately.
- [ ] A later same-certificate build with increased custom code upgrades without data loss.

Samsung Galaxy Watch8 Classic:

- [ ] Matching custom Wear APK installs beside the original where applicable.
- [ ] Custom watch discovers the custom phone.
- [ ] Settings synchronize correctly, including explicit refresh where required.
- [ ] Credential sync follows its configured gate; verify enabled and disabled behavior
  after synchronization without displaying keys.
- [ ] Tethered recording/transcription works.
- [ ] Cancellation works without unintended duplicate transcription.
- [ ] Results reach the intended InputConnection/text field.
- [ ] Standalone transcription works after initial configuration/credential sync.
- [ ] Custom watch does not transcribe through the original-ID phone app; verify actual
  delivery/failure, not just physical node discovery.

## Release 2 — SenseVoice OpenCC

**Planned; direction selected, implementation not authorized.** Add deterministic
Simplified → Hong Kong Traditional (`s2hk`) conversion for relevant SenseVoice output.

Acceptance: entirely offline, no LLM call, no invented missing content, no unnecessary
changes to English/already-Traditional text, suitable legacy behavior. Evaluate dependency
size, licensing, performance and Android compatibility. Test raw recognition separately
from conversion, representative Cantonese/mixed speech and conversion eligibility.
Dependency: approved library/configuration and concrete scope; no code started here.

## Release 3 — Gemini native audio LOW thinking

**Planned; direction selected, implementation not authorized.** Add native audio
thinking-level configuration independently of Rewording AI, usable before profiles.

Acceptance: LOW selectable immediately; correct supported Gemini request serialization;
unset preserves behavior; alternate chat-completion transcription remains compatible;
measure complete-response latency where practical. Verify model IDs and thinking API
support first. Current source includes both GenerateContent and Interactions, so define
model/path applicability explicitly. Provider-default setting belongs under AI providers
where appropriate, with future profile overrides.

## Release 4 — Quota-aware fallback

**Planned; direction selected, implementation not authorized.** Extend existing local
fallback to appropriate cloud quota failures, including relevant HTTP 429 responses.

Acceptance: distinguish transient RPM/rate limits from daily exhaustion where provider
signals permit; respect retry metadata; bound retries; require local model availability;
respect deliberate preferences. Current source's NETWORK/TIMEOUT fallback does not
cover QUOTA_EXCEEDED. Document ambiguous errors and test provider-specific fixtures.
Dependency: agree trigger/retry/user policy; avoid assuming every 429 means daily quota.

## Release 5 — Transcription profiles

**Architecture approved; implementation not authorized.** Implement reusable profiles
and effective-settings UI per [decision record](decisions/transcription-profiles.md).

Acceptance: stable IDs; existing account references without credential duplication;
model/thinking inheritance and supported overrides; Inherit/Append/Exact Replace
instructions; engine-specific validation; optional deterministic processing; visible
effective configuration; fallback configuration isolation; no-profile compatibility.
Resolve persistence, invalid/deleted references, migration and Wear behavior in the
implementation specification. Prior native thinking/conversion increments supply
capabilities, not authorization to start profile implementation.

## Release 6 — Language-based profile routing

**Architecture approved; implementation not authorized.** Under Dictate → Transcription
languages, route selected language → primary profile → optional fallback profile.

Acceptance: English, Cantonese (Hong Kong) and explicit Auto-detect route; fallback
resolves its own effective settings and never inherits incompatible engine prompts;
legacy behavior and optional rewording remain compatible. Top-level Languages & Layouts
continues to manage IME layouts. Dependency: stable profiles, approved route UX and
fallback policy, plus explicit Wear compatibility decisions and tests.

## Review and release gates

For meaningful authorized features: requirements/design discussion, bounded implementer
report, fresh read-only source review, coordinator assessment, focused corrections and
consolidated evidence. Major architecture may need review before implementation.
Keep scope/behavior changes subject to owner decisions. A different model/external
assessment may help for consequential security, signing, migration or release work.

Merge requires explicit approval. Public tags/releases, upstream PRs, device installs,
signing/credential changes and paid infrastructure need separate explicit authorization.
The small Groq fix is a potential upstream contribution only; no submission is approved.

## Release history

No GitHub releases were present at the 2026-10-20 onboarding inspection. Release 1
remains the prepared-but-unvalidated candidate described above. Add later history with
exact source commit, artifact hashes, signature verification, acceptance evidence,
distribution channel and approval; do not equate a signed candidate with a public release.
