# Dictate Custom Wear OS companion

This change adds an `identity` flavor dimension to `:wear`, matching the phone's
existing `original` / `custom` flavors. It shares the same watch sources, icons and
namespace (`net.devemperor.dictate.wear`); no application logic or protocol is forked.
`original` remains the default Android Studio flavor. Select custom tasks explicitly.

| Release variant | Application ID | App / keyboard label | Version name / code |
| --- | --- | --- | --- |
| Original phone | net.devemperor.dictate | Dictate / Dictate | 6.4.0 / 142 |
| Custom phone | net.devemperor.dictate.jack | Dictate Custom / Dictate Custom | 6.4.0-jack.1 / 143 |
| Original watch | net.devemperor.dictate | Dictate / Dictate (Wear) | 6.4.0-w4 / 100146 |
| Custom watch | net.devemperor.dictate.jack | Dictate Custom / Dictate Custom | 6.4.0-jack.1 / 143 |

The original watch retains its existing Play version formula:
`projectVersionCode + 100000 + wearRevision`, with wearRevision = 4, and its `-w4`
name. Custom Wear uses `custom-version.properties`, without that upstream band.
It defaults to the same `versionCode` / `versionName` as the custom phone. Optional
`wearVersionCode` / `wearVersionName` entries override only the watch. For example,
a watch-only release can set those to 144 / 6.4.0-jack.2 while leaving phone values
at 143 / 6.4.0-jack.1. To release only the phone later, pin the watch's two overrides
before changing the common values. Each installed package on each device requires
monotonically increasing codes. No override is set for this initial release.
The shared initial code 143 is for separate APK installation. If both form factors
are later uploaded to one Play listing, allocate distinct codes using the watch
overrides; the original module already uses its separate band for that Play constraint.

Debug pairs use `.debug` on **both** devices; beta pairs use `.beta` on both.
Do not pair a release phone with a debug watch. Version names/codes need not match
across devices; package ID, certificate and protocol compatibility do.

## Data Layer identity and routing

[Android's Data Layer security rules](https://developer.android.com/training/wearables/data/overview#security_of_communications)
require the same package name and signing certificate on the phone and watch.
Google Play services enforces that isolation for Data Layer clients. The proposed
custom watch ID therefore intentionally equals the custom phone ID, on a different
device. On each device the original and custom apps have different IDs and sandboxes.

All calls use `Wearable.get…Client(context)` in the calling application's context.
There is no hand-written certificate bypass or cross-package IPC bridge. Discovery
prefers the phone capability `dictate_phone_transcriber` through CapabilityClient;
NodeClient's existing nearby-node fallback chooses a physical device, not another
application's endpoint. MessageClient/ChannelClient/DataClient remain scoped to the
calling package/certificate even on that same physical node. An official phone app
cannot receive a custom app's messages solely because it advertises the same capability.

Consequently, the existing `/dictate/*` paths, `settings_json` key, capability name,
request IDs, cancellation/progress messages and protocol-0 compatibility are preserved.
No new custom protocol namespace or routing logic is needed. Multiple matching nodes
still use the existing nearby-first/first-available selection; this task does not
redesign multi-phone selection.

One packaging issue was found in the baseline phone releases: resource shrinking
removes `android_wear_capabilities`, which Play services locates dynamically. The
new custom-only `app/src/custom/res/raw/wear_capability_keep.xml` uses an
[Android resource keep rule](https://developer.android.com/topic/performance/app-optimization/customize-which-resources-to-keep)
to retain it for
custom release discovery. Original release behavior is unchanged, including its
existing connected-node fallback; original debug already retains the capability.
The APK checker requires the array in both custom phone APK types.

## Source map and complete dictation flow

| Responsibility | Source and methods |
| --- | --- |
| Shared wire contract | [DictateWearProtocol.kt](../lib/dictate-core/src/main/kotlin/dev/patrickgold/florisboard/dictate/sync/DictateWearProtocol.kt): `requestPath`, `responsePath`, `progressPath`, `cancelPath`, `DictateSyncedSettings.encode/decode` |
| Phone discovery and requests | [WearSyncClient.kt](../wear/src/main/kotlin/net/devemperor/dictate/wear/sync/WearSyncClient.kt): `findPhoneNodeId`, `requestSettingsSync`, `fetchPublishedSettings`, `setStandalone`, `setAutoRewording` |
| Phone settings projection | [PhoneWearSettingsResolver.kt](../app/src/main/kotlin/dev/patrickgold/florisboard/dictate/wear/PhoneWearSettingsResolver.kt): `resolve` |
| Settings publication | [DictateWearPublisher.kt](../app/src/main/kotlin/dev/patrickgold/florisboard/dictate/wear/DictateWearPublisher.kt): `publish`, `startPublishingOnChange`; started by FlorisApplication |
| Watch reception and cache | [WearSyncListenerService.kt](../wear/src/main/kotlin/net/devemperor/dictate/wear/sync/WearSyncListenerService.kt): `onDataChanged`; [WearSettingsStore.kt](../wear/src/main/kotlin/net/devemperor/dictate/wear/sync/WearSettingsStore.kt): `load`, `save`, `current` |
| Microphone capture | [WearAudioRecorder.kt](../wear/src/main/kotlin/net/devemperor/dictate/wear/audio/WearAudioRecorder.kt): `start`, `stop`, `cancel` |
| Transport and standalone ASR | [WearTranscription.kt](../wear/src/main/kotlin/net/devemperor/dictate/wear/transcribe/WearTranscription.kt): `transcribe`, `tetherWithRetry`, `tether`, `standalone`, `maybeReword` |
| Phone tether reception | [DictateWearService.kt](../app/src/main/kotlin/dev/patrickgold/florisboard/dictate/wear/DictateWearService.kt): `onMessageReceived`, `onChannelOpened`, `transcribeForWatch`, `sendResponseWithRetry` |
| Phone ASR and rewording | [PhoneTranscriber.kt](../app/src/main/kotlin/dev/patrickgold/florisboard/dictate/wear/PhoneTranscriber.kt): `transcribe`, `maybeReword`; [WearTetherService.kt](../app/src/main/kotlin/dev/patrickgold/florisboard/dictate/wear/WearTetherService.kt): `hold`, `release` |
| Watch output | [WearImeService.kt](../wear/src/main/kotlin/net/devemperor/dictate/wear/ime/WearImeService.kt): `startRecording`, `stopAndTranscribe`, `transcribeAudio`, `deliver`; [WearSettingsActivity.kt](../wear/src/main/kotlin/net/devemperor/dictate/wear/ui/WearSettingsActivity.kt): `stopDemoAndTranscribe` |

The following flow is verified by source inspection and protocol tests, **not** by
paired-device execution:

1. The watch IME checks RECORD_AUDIO and starts its 16 kHz mono PCM16 WAV recorder.
2. On stop, `WearTranscription.transcribe` finds the matching phone using Data Layer.
   `tether` opens a ChannelClient channel, using a request UUID for protocol 1, and
   streams the recorded WAV to the selected node.
3. The matching phone's listener receives that channel in its own application context.
   It keeps the phone process/CPU active, reads into a private cache file, applies the
   existing silence gate and calls PhoneTranscriber with that phone's live settings.
   On-device phone transcription or the selected network provider/rewording remain unchanged.
4. The phone sends progress and the transcript/error back to the source node using
   MessageClient. Named requests have separate response paths; cancellation and
   legacy fixed-path replies retain their existing behavior.
5. The watch parses the reply and `WearImeService.deliver` calls the current
   InputConnection's `commitText`. The existing auto-send/editor-action setting or
   confirmation button follows. If insertion fails, text/audio retry behavior is retained.
   The settings activity's Try dictation demo displays text locally instead.

Standalone still uses the shared `OpenAiCompatibleClient` with synced endpoint,
API type, selected model, key, language, style prompt and timeout; optional rewording
uses the synced chat configuration/prompts. The Groq language fix is in that same
unchanged shared core. Definitive phone errors and phone silence do not trigger a
second standalone request; transport failures may fall back when credentials exist.
No provider defaults, retries, network policy or error handling were modified.

The phone's existing `DictateWearScreen` install button still opens the original
Play listing. It does not install the custom watch APK; install the custom APK on
the watch directly. No uncreated custom Play listing is substituted.

The watch can install without the phone (`com.google.android.wearable.standalone=true`).
That does **not** provide independent first-time provider setup: a fresh installation
needs a settings sync from its matching phone. Afterwards its private cached config
can support standalone dictation with Wi-Fi/LTE and synced credentials. The watch
does not gain the phone's offline models, proxy or per-account certificate-trust
configuration through this task; the existing schema does not carry those settings.

## Settings and credentials

The shared schema continues to carry:

- The active transcription provider/account selection, provider label, API type,
  endpoint and model, language/name, style prompt including custom vocabulary.
- Accent color, haptic feedback, request timeout and tether protocol version.
- Watch standalone-key-sync and auto-rewording toggles, phone rewording/auto-formatting
  flags, selected chat endpoint/model/API, system prompt and auto-apply prompts.
- Only the active transcription and rewording credentials, under the existing
  `wearStandaloneEnabled` master toggle. This is **on by default upstream**. Turning
  it off publishes empty transcription and rewording keys; no additional secrets or
  account keyring entries are introduced here. Changes reach an offline watch only
  after synchronization resumes.

DataClient replicates the JSON snapshot; watch reception and explicit cached-item
fetches save it in private `dictate_wear_sync` SharedPreferences. Watch backup remains
disabled. Original and custom caches are separate; no private-data migration occurs.
Watch keyboard auto-send (`WearKeyboardPrefs`) and emoji recents stay watch-local.

The existing publisher observes provider/account/language/style/vocabulary/accent and
standalone-toggle changes. Other payload fields are refreshed by an explicit sync
request (including on watch settings/keyboard open); this task does not expand its
live-change subscriptions. If a value appears stale, request a sync on the watch.

For later Gemini thinking options or language/fallback profiles, the JSON decoder
already ignores unknown keys, defaults permit old snapshots, and tether protocol
versioning exists. A later feature must add explicit optional schema fields/defaults,
project only intended settings on the phone, update the watch consumer and add mixed-
version fixtures. Ignoring a field is not support for its behavior. No profiles or
new synchronization fields are implemented here.

## Manifest and signing audit

The watch keeps minSdk 30, targetSdk 36, the watch hardware feature, microphone /
internet / network-state permissions, icons and standalone metadata. Its exported
IME remains `net.devemperor.dictate.wear.ime.WearImeService`, protected by
BIND_INPUT_METHOD and `android.view.im` metadata; its settings class and Data Layer
listener also retain their original namespace. Flavor resources change both app
and keyboard labels to Dictate Custom. No custom FileProvider, app-specific broadcast,
extra communication permission or cross-app package-visibility query is introduced.
Dependency-generated authorities/permissions are checked in the final APKs.

Both modules retain the existing root `keystore.properties` release configuration.
Point it at **Jack's existing private signing key**, with the same alias/certificate
for phone and watch; do not substitute the upstream developer's key or assume an
upload-key fingerprint equals a distributed app's signing certificate. Verify the
actual two signed APK certificates with `apksigner verify --print-certs` before
installation. No replacement production key is generated. Without that file the
release APKs are unsigned. Private production credentials were unavailable here.

Debug builds from this workspace use its existing Android Debug keystore. Building
on separate machines can produce different debug certificates and break Data Layer;
the checker requires the actual phone/watch certificate fingerprints to match.
Matching debug packages/certificates establish a build prerequisite, not pairing success.

## Build and validation commands

Use the existing JDK 17 / SDK setup and native dependencies described in
[the phone build guide](custom-application.md). Build release APKs with:

```sh
./gradlew :app:assembleOriginalRelease
./gradlew :app:assembleCustomRelease
./gradlew :wear:assembleOriginalRelease
./gradlew :wear:assembleCustomRelease
```

Replace Release with Debug for paired debug builds. Aggregate `assembleRelease`
builds both identities; use module-qualified flavor tasks when collecting artifacts.
Custom outputs are `app/build/outputs/apk/custom/release/app-custom-release-unsigned.apk`
and `wear/build/outputs/apk/custom/release/wear-custom-release-unsigned.apk` without keys.
Future authorized release filenames should distinguish device and version, e.g.
`Dictate-Custom-Phone-6.4.0-jack.1-143.apk` and `Dictate-Custom-Wear-6.4.0-jack.1-143.apk`.
No release workflow is added and no APK is published by this task. A later publishing
job must sign both with the existing key and verify ID, version, certificate and
correct form factor before publishing only those two explicitly selected files.

```sh
./gradlew :lib:dictate-core:testDebugUnitTest
python3 tools/check-wear-companion.py --sdk "$ANDROID_HOME" --build-type debug
python3 tools/check-wear-companion.py --sdk "$ANDROID_HOME" --build-type release --allow-unsigned
```

The APK checker uses aapt2, apkanalyzer and apksigner from the SDK. It checks both
phone/watch pairs, original/custom versions and labels, matching identities,
certificates for signed pairs, merged manifests, Wear feature/standalone/IME/listener
metadata, capability retention and disjoint per-device provider/permission names.
`--allow-unsigned` permits metadata validation only; it cannot verify production trust.
The core tests cover current/legacy protocol paths plus settings wire fixtures,
key-free snapshots, unknown-field compatibility and Cantonese transcript/error replies.
No test asserts that Google Play services delivered a real message without paired devices.

## Recorded validation (2026-10-18)

- Built original/custom Debug and Release APKs in both `:app` and `:wear` on JDK 17.
  Release vital lint passed; phone release R8/resource shrinking passed. Builds ran
  with one worker and bounded Gradle/compiler heaps for this workspace.
- Both invocations of `tools/check-wear-companion.py` passed across all eight APKs.
  Final APKs and merged manifests match the table, Wear requirements and component
  identities; per-device authorities and declared permissions do not conflict.
  The custom phone release retains `dictate_phone_transcriber` after shrinking.
- All four release APKs are verified unsigned. Both debug pairs use the existing
  Android Debug certificate, SHA-256
  `ec431eaf65427ce3d4671972bc355b173c736d692019417d286dc451eb38723d`.
  Production certificate matching and device pairing remain unverified.
- A temporary watch-only version override produced a Wear merged manifest at
  144 / 6.4.0-jack.wear-test while the phone stayed 143 / 6.4.0-jack.1. Restoring
  the file regenerated the original 143 / 6.4.0-jack.1 watch manifest. No override
  values remain in source or in the release APKs.
- `:lib:dictate-core:testDebugUnitTest`: **229 passed**, zero failures/errors/skips.
  This includes **40 Groq**, **5 existing Wear protocol**, and **4 new settings/
  response compatibility** cases. Tests use synthetic credentials and local fixtures.
- `:wear:testOriginalDebugUnitTest` and `:wear:testCustomDebugUnitTest` completed
  **NO-SOURCE**: the watch has no module-local unit tests. Build identity tests run
  against its actual APKs, and wire tests run in the shared core. No device-based
  watch lifecycle, microphone, network-provider or delivery test was performed.
- Phone Gradle configuration, phone/watch runtime source and the Groq fix source/test
  are unchanged from the base branch; only the custom phone resource keep rule is
  needed on the phone. Resource XML, Python syntax and Git diff checks passed.

## Physical-device acceptance procedure

No paired emulator or real phone/watch is available in this environment. With both
original apps kept installed, use either the matching custom debug pair from one
workspace or custom release APKs privately signed with the same existing key:

1. Install the correct APK on each device, confirm separate Dictate Custom launcher
   entries and the watch keyboard label. Grant microphone permission and enable the
   custom watch IME independently. Do not expect old app data to migrate.
2. Configure the custom phone, using recognizable non-secret provider/model/language
   choices different from the original phone. Request a settings sync on the custom
   watch; confirm only custom values arrive. Repeat with the original pair and ensure
   it still receives original values. Test standalone/auto-reword toggles in both directions.
3. In a watch text field, record/stop Cantonese speech through the custom keyboard.
   Verify transcription, rewording if enabled, text insertion and the editor action.
   Also test the watch's Try dictation demo. Exercise cancel/retry and a dropped connection.
4. With standalone disabled and the original phone app still available, make the custom
   phone app unavailable. Confirm the custom watch cannot transcribe through the original
   phone. Repeat the inverse isolation check. Cached capability discovery may still show
   a node; delivery/failure, not its presence alone, determines the result.
5. Reconnect and enable existing key synchronization, let it complete, then make the
   phone unreachable while keeping watch Wi-Fi/LTE. Verify standalone transcription and
   optional rewording. Reconnect, disable key sync and sync again; offline dictation
   should then report no usable transport. Do not inspect or share the keys themselves.
6. Verify microphone denial, bad-key/quota/no-speech errors and return to the original
   keyboard. Later, test a same-certificate upgrade with a greater custom watch code
   preserving only its own watch-local settings; independently test phone upgrades.
