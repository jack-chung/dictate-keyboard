# Dictate Custom project context

## Authority and verification

This knowledge base was established from the owner's master handoff on 2026-10-20.
The owner is the decision maker; the primary Codex planner coordinates discussion,
implementation and independent review. See [AGENTS.md](../AGENTS.md) for policy and
[ROADMAP.md](ROADMAP.md) for changing status and acceptance evidence.

Evidence labels used here:

- **Owner decision**: explicitly selected requirement or architecture; not proof of implementation.
- **Source verified**: inspected at base `99805943618077059d95204f34407cfb7f8e92ed`
  during onboarding, including a read-only agent audit assessed by the coordinator.
  This does not establish runtime success.
- **Historical report**: supplied by the owner or recorded in earlier build guides;
  tests/artifacts were not rerun or independently checked during onboarding.
- **Provisional / open**: recommendation, intended future behavior or unresolved question.

Source, verified artifacts and explicit owner decisions override stale prose. Preserve
dated rationale when correcting a conclusion. Model/API/pricing claims need current
provider evidence before future implementation.

## Product and priorities

**Owner requirements.** Fork: [jack-chung/dictate-keyboard](https://github.com/jack-chung/dictate-keyboard).
Upstream: [DevEmperor/DictateKeyboard](https://github.com/DevEmperor/DictateKeyboard).
Dictate is an Android dictation keyboard with a Wear OS companion. Primary devices
are Samsung Galaxy Z Fold7 and Samsung Galaxy Watch8 Classic.

Optimize for faithful conversational Hong Kong Cantonese, English including technical
vocabulary, and Cantonese with embedded English. Preserve particles, colloquial grammar,
natural wording and English actually spoken. Use Hong Kong Traditional Chinese and
sensible punctuation. Avoid Mandarin-style written paraphrases, missing/invented
content, polished rewrites, answers to dictated questions, and execution of dictated
instructions. Fidelity takes precedence over stylistic elegance.

Measure complete usable transcript availability from the end of recording, targeting
about 10 seconds or less, ideally faster. Streaming first-token latency is a different
metric. Prefer inexpensive providers/free allowances when fidelity remains acceptable.
Historical Cantonese use was below 20 requests/day; quota/pricing availability is open
to change and not reverified during onboarding.

Transcription and rewording are distinct. The owner's preferred automatic-rewording
default is disabled. Historical separate cleanup-LLM experiments answered questions,
followed speech instructions or changed wording. Do not automatically chain another
LLM onto ordinary transcription. This preference does not assert that every existing
upstream setting or installed device already has that value.

## Application identities and build architecture

**Source verified.** Phone `:app` and watch `:wear` share their existing sources across
the `original` and `custom` identity flavors; original is the default flavor. Phone
namespace stays `dev.patrickgold.florisboard`; watch stays `net.devemperor.dictate.wear`.

| Release application | Application ID | Label | Version name | Code |
| --- | --- | --- | --- | --- |
| Original phone | `net.devemperor.dictate` | Dictate | `6.4.0` | 142 |
| Custom phone | `net.devemperor.dictate.jack` | Dictate Custom | `6.4.0-jack.1` | 143 |
| Original watch | `net.devemperor.dictate` | Dictate / Dictate (Wear) | `6.4.0-w4` | 100146 |
| Custom watch | `net.devemperor.dictate.jack` | Dictate Custom | `6.4.0-jack.1` | 143 |

Values are derived from checked-in configuration, not freshly inspected APKs.
`custom-version.properties` owns custom versions independently of upstream overrides.
Optional Wear-specific overrides exist but are unset. Original Wear retains its
`projectVersionCode + 100000 + wearRevision` formula, with revision 4. Debug/beta
suffixes must match across phone/watch pairs. Minimum SDK is 26 phone / 30 watch;
target SDK is 36 for both, as configured in source.

**Owner decision.** Keep separate IDs/sandboxes, shared sources and isolated packaging;
retain the original variant. Do not use an artificially high private custom code range.
See [identity decision](decisions/custom-application-identity.md) and the existing
[phone](custom-application.md) / [Wear](custom-wear-companion.md) build guides.

## Existing transcription architecture

**Source verified.** Shared `lib/dictate-core` contains provider configuration,
request/response models, network client and Wear wire contracts. The phone owns
provider accounts/preferences, dictation control and local sherpa-onnx recognition.
Profiles must reference existing accounts, not duplicate their API credentials.

The Groq fix at `70a3b2b85d053870c5bd6c9b38645f8dfe55b498` normalizes case-insensitive
`yue-HK` / `yue-CN` to `yue` in multipart requests only when the parsed endpoint
hostname is exactly `api.groq.com` and model is exactly `whisper-large-v3` or
`whisper-large-v3-turbo`. This is host/model gating, not provider-ID gating or equality
of the entire base URL. Other codes, auto-detect and other hosts/models retain their
existing handling. Forty regression cases are present, including captured multipart
requests, casing, detection and exclusions; their presence was checked, not execution.
The original HTTP failure's precise cause remains historically unproven by a new live
failing request. Earlier acceptance of `language=yue` is a historical report; physical
Cantonese dictation remains an acceptance test. Do not generalize this mapping globally.

Gemini has multiple paths in `OpenAiCompatibleClient.kt`:

- Native `transcribeGeminiGenerateContent` sends inline base64 audio plus a strict
  transcript instruction, infers MIME type, uses `x-goog-api-key`, and sets temperature
  to zero. `GeminiGenerationConfigDto` exposes only temperature, not thinking.
  Temperature zero does not establish LOW thinking or disabled thinking.
- The instruction builder wraps `request.prompt` as a labeled style/context hint.
  That is not literal instruction replacement.
- Models whose names contain `transcribe` (case-insensitive after prefix removal)
  take the native Interactions path instead. It sends transcription configuration,
  smart mode, language hints and vocabulary, not the same rich instruction payload.
  The checked-in Gemini preset defaults transcription to `gemini-3.5-transcribe`;
  this is a source default, not verified provider availability or device selection.
- `config.useChatAudio` selects an alternate `chat/completions` input-audio path.
  The phone dictation router excludes local and dedicated Gemini-transcribe models
  from this path; the UI switch alone does not establish which request will run.
  Do not infer behavior from the “Single-call multimodal” label alone.
- Rewording `reasoning_effort` is a separate chat setting; it does not configure
  native audio thinking. Its OFF value means omission/provider default.

Local SenseVoice uses sherpa-onnx, language mapping and inverse text normalization.
Custom OpenCC `s2hk` post-processing is planned, not implemented. The phone's
`DictateController.localFallbackProvider` currently accepts NETWORK/TIMEOUT errors
when fallback is enabled, active provider is non-local and the local model is installed.
HTTP 429 is classified as QUOTA_EXCEEDED and is outside that fallback gate. Existing
generic retries must be assessed before extending quota policy.

## Wear communication and limitations

**Source verified.** `WearSyncClient.findPhoneNodeId` uses reachable CapabilityClient
nodes, then NodeClient connected-node fallback, preferring nearby nodes. Finding a
node does not establish that it hosts the matching app. Data Layer is designed around
matching package identities and certificates; physical delivery/isolation remains untested.

`DictateWearPublisher.publish` sends settings through DataClient. Watch reception saves
the JSON snapshot in app-private SharedPreferences. `PhoneWearSettingsResolver` gates
both transcription and rewording keys on `wearStandaloneEnabled`, which defaults true
upstream. Disabling the gate publishes empty keys; an offline watch receives changes
only once sync resumes. This onboarding does not change that credential policy.

Watch tethered audio goes through ChannelClient; phone-side `DictateWearService`
transcribes and replies through MessageClient. Progress/cancel and named request paths
retain existing protocol/legacy support. `WearImeService.deliver` inserts through
InputConnection. The custom phone has a resource keep rule for the Wear capability
array; final post-shrink retention is historically reported, not reverified in an APK.

Standalone needs previously synchronized provider configuration and usable credentials/
network. The existing eligibility predicate requires a nonblank key and endpoint, so
keyless custom servers are not enabled by it. Definitive phone errors and silence after
accepting audio avoid a duplicate standalone request; transport failures may fall back.

Important parity limits for future work:

- Synced schema does not carry all phone options: proxy, certificate trust, reasoning
  effort, expected-language lists and chat-audio selection are absent.
- Tethered `PhoneTranscriber` includes expected languages but currently does not set
  a style prompt; standalone supplies its synced style prompt. Do not claim identical
  prompt behavior merely because both use the shared client.
- Publisher observers cover a subset of snapshot fields. Explicit sync refreshes
  other rewording/prompt/timeout settings; unknown-key tolerance is not feature support.
- Standalone does not acquire phone offline models or first-time independent setup.
- The phone's Wear-install shortcut still opens the original Play listing. Custom
  APKs need separate installation; no custom listing exists in this workflow.

Protocol details/source links and dated test reports remain in the [Wear guide](custom-wear-companion.md).
No physical pairing, tethered transcription, standalone transcription or IME delivery
was verified during onboarding.

## Historical ASR research

**Historical reports only.** An approximately 26.752-second real Hong Kong Cantonese
recording with conversational wording and English technical terms was compared using
normalized character error rate (CER) and latency after recording ended.

| Engine/configuration | Approximate CER | Reported full-response latency / limits |
| --- | --- | --- |
| Gemini API `gemini-3.8-flash`, LOW | 5.35% | 2.601 s median; 3 completed samples |
| Gemini 3.8 Flash, default thinking | 4.74% | 8.738 s median; small sample |
| Gemini 3.8 Flash, MEDIUM | 7.30% | 13.534 s median |
| Gemini 3.8 Flash, HIGH | 5.11% | About 71.6 s |
| Earlier Gemini default trial | 2.92% | About 11.789 s median |
| Groq Whisper Large V3, `language=yue` | 13.14% | About 2.3 s |
| Groq Whisper, automatic language | About 35% | Substantial Mandarin-style rewriting reported |
| Qwen3 ASR 0.6B | 17.52% | About 1.24 s |
| Qwen3 ASR 1.7B | 12.4% | About 47 s |
| Gemini Flash-Lite variants | About 18–23% | About 1.3–1.6 s |
| Local SenseVoice Small int8 | Not supplied | Offline candidate; Simplified output and content loss reported |

One recording, small samples, varying Gemini results and quota-limited repetitions
cannot establish universal rankings. The audio/reference, normalization script, raw
responses and timing logs were not located or examined during onboarding. Historical
model labels, prices and quotas are not current catalog verification.

**Provisional finding.** Native Gemini audio with LOW thinking appeared to balance
Cantonese fidelity and complete-response latency best in those trials, with punctuation
and Hong Kong Traditional output in one call. Intended strategy: Groq Large V3 for
English, Gemini Flash LOW for Cantonese, SenseVoice plus deterministic script conversion
offline. This strategy is not implemented as automatic language-based routing.
See [fidelity/engine decision](decisions/transcription-fidelity-and-engines.md).

## Approved future architecture

**Owner decision, future implementation.** Keep accounts/defaults/capabilities in
Dictate → AI providers; add reusable Transcription profiles and per-language primary/
optional-fallback routing under Dictate → Transcription languages. Keep top-level
Languages & Layouts for IME layouts. Preserve no-profile legacy behavior and optional
rewording. A profile references an account and resolves model, supported thinking,
instruction inheritance/append/exact replacement, engine settings and deterministic
post-processing; fallback resolves its own configuration. Auto-detect has an explicit
default route. See the [profile decision](decisions/transcription-profiles.md) for
requirements, alternatives, acceptance criteria and open design work.

## Signing, artifacts and migration

**Owner policy.** Both custom form factors use the owner's existing private production
identity. Existing release signing configuration is optional in both module build files;
without credentials builds are unsigned. Private signing assets/credentials are reported
to reside in dedicated project Google Drive storage. Connection/folder access and secret
readiness were not validated during onboarding; never infer access from the handoff.
Do not record secrets or private signing paths here. Reuse intact signed APKs when
available, verify actual signatures, and do not generate or silently substitute keys.

**Historical report.** A later release-preparation task signed both custom APKs and
verified matching fingerprints. Those artifacts are absent locally during onboarding.
Expected public certificate/hash/size evidence is in [ROADMAP](ROADMAP.md#release-1-artifact-record).
The original-ID installed certificate was not independently inspected; no completed
comparison with that installation is claimed.

**Artifact evidence inspected during onboarding.** One retained original-ID debug APK
reports `net.devemperor.dictate.debug`, code 142, `6.4.0-debug+f77871bd`; `aapt2` read
its metadata and `apksigner verify --print-certs` verified an Android Debug signature.
It is not either expected production custom APK. Retained core XML reports total 185
tests with zero recorded failures/errors/skips, dated 2026-10-11, and contain neither
the Groq regression nor new Wear compatibility suite. These older reports do not
validate the current custom commits. No application tests were rerun during onboarding.

**Historical migration findings, not device results.** Settings/prompt backups can be
explicitly imported across IDs; a version warning may occur and credentials may require
re-entry. Backups may contain provider API keys and must be handled privately. Download
offline models again. Old clipboard image/video authorities may require recopying media.
Original/custom keyboards and watch apps are designed to coexist, with custom watch
using custom phone. Never implement silent access to another app's sandbox.

## Source map

Paths are relative to repository root. Method names provide stable search anchors.

| Area | Relevant source |
| --- | --- |
| Phone/watch identity | `app/build.gradle.kts`, `wear/build.gradle.kts`, `gradle.properties`, `custom-version.properties`, `app/src/custom/`, `wear/src/custom/` |
| Shared requests and Gemini/Groq paths | `lib/dictate-core/src/main/kotlin/dev/patrickgold/florisboard/dictate/provider/{OpenAiCompatibleClient,ProviderConfig,ProviderModels,ProviderRegistry,DictateApiException}.kt` |
| Groq regressions | `lib/dictate-core/src/test/kotlin/dev/patrickgold/florisboard/dictate/provider/GroqWhisperLanguageTest.kt` |
| Accounts and local recognition | `app/src/main/kotlin/dev/patrickgold/florisboard/dictate/provider/{ProviderAccount,LocalTranscriptionProvider,LocalModelCatalog}.kt` |
| Dictation and fallback | `app/src/main/kotlin/dev/patrickgold/florisboard/dictate/DictateController.kt`: `localFallbackProvider` |
| Preferences and rewording | `app/src/main/kotlin/dev/patrickgold/florisboard/app/AppPrefs.kt`, `app/src/main/kotlin/dev/patrickgold/florisboard/dictate/DictateReasoningEffort.kt` |
| Settings UI and ASR languages | `app/src/main/kotlin/dev/patrickgold/florisboard/app/settings/dictate/{DictateScreen,DictateProvidersScreen,DictateLanguagesScreen}.kt`, `app/src/main/kotlin/dev/patrickgold/florisboard/dictate/DictateLanguages.kt` |
| Wear contract and fixtures | `lib/dictate-core/src/main/kotlin/dev/patrickgold/florisboard/dictate/sync/DictateWearProtocol.kt`, matching `src/test/.../sync/DictateWearSettingsCompatibilityTest.kt` |
| Phone Wear integration | `app/src/main/kotlin/dev/patrickgold/florisboard/dictate/wear/{PhoneWearSettingsResolver,DictateWearPublisher,DictateWearService,PhoneTranscriber}.kt` |
| Watch integration | `wear/src/main/kotlin/net/devemperor/dictate/wear/sync/`, `transcribe/WearTranscription.kt`, `ime/WearImeService.kt` under the same watch package |
| APK validation | `tools/check-wear-companion.py` |
| Backup behavior | `app/src/main/kotlin/dev/patrickgold/florisboard/app/settings/advanced/{BackupScreen,BackupRedaction}.kt` |

## Documentation reconciliation and open evidence

No pre-existing AGENTS.md or other applicable repository agent instructions were found.
README, SECURITY.md, PRIVACY_POLICY.md and both custom build guides are preserved.
The build guides' unsigned/key-unavailable observations describe their 2026-10-18
implementation stages; they do not disprove the owner's later signing report. Added
scope notes point to the current roadmap without rewriting their recorded validation.

`ProviderConfig.kt` still describes Gemini as having no dedicated STT endpoint; actual
client/registry routing includes Interactions. Treat that comment as stale, not as an
architectural restriction. Application source was left unchanged during onboarding.
Wear tether/standalone prompt and settings differences are existing limitations to
consider in later scope discussions, not authorization to repair them now.

Tests, live provider behavior, final APK metadata/signatures, device acceptance, backup
migration, broader ASR quality, current quotas and installed certificate comparison all
need their own evidence. The [roadmap](ROADMAP.md) records the immediate blockers.
