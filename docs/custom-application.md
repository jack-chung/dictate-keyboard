# Dictate Custom builds

This guide retains the implementation-stage findings and validation recorded on
2026-10-18. Its unsigned/key-unavailable observations refer to those builds. The
owner later reported separately prepared production-signed APKs; their current
availability and verification status are tracked in [ROADMAP.md](ROADMAP.md).
See [AGENTS.md](../AGENTS.md) for current authorization and signing policy.

The `identity` flavor dimension shares the existing app sources and namespace
(`dev.patrickgold.florisboard`). No source packages or directories were renamed.
`original` is the default Android Studio flavor; `custom` must be selected explicitly.
Build-type suffixes still apply (`.debug`, `.beta`, `.bench`).

| Release variant | Application ID | Launcher / keyboard label | Version source |
| --- | --- | --- | --- |
| originalRelease | net.devemperor.dictate | Dictate (unchanged) | gradle.properties |
| customRelease | net.devemperor.dictate.jack | Dictate Custom | custom-version.properties |

Original release version remains `6.4.0` / `142`. Initial custom version is
`6.4.0-jack.1` / `143`. Future custom releases must increase `versionCode` in
`custom-version.properties`, independently of upstream. There is no reserved range.
Custom release/beta names never inherit upstream prerelease suffixes; custom debug
and benchmark builds retain their diagnostic suffixes. Do not use upstream
`-PprojectVersionCode` / `-PprojectVersionName` to version custom releases.

As of 2026-10-18, the fork has no GitHub releases or Actions runs and no previous
custom-ID artifact was found in the workspace. Code 143 is suitable on that evidence;
check any privately distributed APK before the first release. An APK signed with
Jack's key under the **original** ID does not constrain this new ID's version code.

## Build

Use JDK 17, the SDK/NDK versions in `gradle/tools.versions.toml`, and the existing
native-library setup (`tools/fetch-sherpa-onnx.sh` and
`tools/bergamot/build-android.sh`) if these dependencies have not been prepared.

```sh
./gradlew :app:assembleOriginalDebug :app:assembleCustomDebug
./gradlew :app:testCustomDebugUnitTest :lib:dictate-core:testDebugUnitTest
./gradlew :app:assembleOriginalRelease :app:assembleCustomRelease
```

Release APKs are under `app/build/outputs/apk/original/release/` and
`app/build/outputs/apk/custom/release/`. Unsigned custom output is
`app-custom-release-unsigned.apk`. Debug builds use distinct IDs ending in `.debug`
and the Android debug certificate, not the production key.

Use **`:app:assembleCustomRelease`** for a custom release. Aggregate tasks such as
`assembleRelease` now build both flavors; never publish their output with a wildcard.
The existing reproducible-build helper (`utils/repr_build/scripts/assemble.sh`)
explicitly builds **original** variants and uses their flavor-qualified output paths,
preserving its existing track defaults. Build custom APKs with the command above.
The optional benchmark module is commented out in settings.gradle.kts; if enabled
later, select its target identity flavor explicitly.

## Signing and later release automation

Both release flavors use the existing optional root `keystore.properties` mechanism.
Use `keystore.properties.template` for field names and point `storeFile` at the
existing private keystore containing Jack's original signing key. Keep the file,
keystore, passwords, and backups out of Git. No new production key is generated.
Without that file releases are unsigned; Android requires signing before installation.
The key was unavailable for this implementation, so no claim is made that a test
artifact carries Jack's certificate.

Before signing a future custom release, compare the candidate certificate SHA-256
with a known APK signed with Jack's key, using `apksigner verify --print-certs`.
The different package ID makes this a fresh installation even with the same key;
subsequent custom upgrades require that same certificate and a larger custom code.
The original app's signature and data are untouched.

The legacy reproducible-build helper also supports external signing via `FLSEC_*`
variables; those credentials were unavailable too. Its signing logic is preserved.

There are no checked-in GitHub Actions workflows on this branch or checked-in
Actions signing-secret bindings to reuse. No workflow or release was triggered here. For the
separate release task, use a manually dispatched, explicitly authorized workflow:

1. Check out the intended custom commit, prepare dependencies, and read custom-version.properties.
   Require its code to exceed the last published custom release code.
2. Materialize the existing private key and keystore.properties in the runner from
   the owner's existing private signing storage; establish the secret bindings then.
   Do not log their contents. Fail the publishing job if credentials are unavailable.
3. Run only `./gradlew :app:assembleCustomRelease`.
4. Verify the exact APK's ID is `net.devemperor.dictate.jack`, its version matches
   custom-version.properties, its label is `Dictate Custom`, and its certificate
   matches the known signing fingerprint. Reject unsigned or original-ID output.
5. Give that one verified APK an explicit filename, e.g.
   `Dictate-Custom-6.4.0-jack.1-143.apk`. Publish only with explicit authorization.

## Identity and keyboard audit

The main keyboard service remains
`dev.patrickgold.florisboard.FlorisImeService` inside each application's own package.
Its exported `android.view.InputMethod` registration, BIND_INPUT_METHOD permission,
`android.view.im` metadata, direct-boot setting, keyboard subtype and settings alias
are inherited. Android identifies components by both package ID and class name;
shared class names therefore do not conflict. Runtime keyboard enable/selection
checks use BuildConfig.APPLICATION_ID / context.packageName and the actual class.
The custom manifest labels the app, launcher alias, keyboard and spellchecker as
Dictate Custom, and distinguishes the separate voice IME as Dictate Custom voice input.

Clipboard, file-sharing and AndroidX Startup authorities already use
`${applicationId}`; runtime file/clipboard URI creation uses the matching runtime
package. Dependency providers and generated permissions must also be checked in the
merged release manifest / APK. Explicit intents use context or runtime packages.
Notifications/PendingIntents and internal files/caches belong to the creating app's
sandbox. No sharedUserId, custom global permission, app widget or launcher shortcut
registration was found in the source manifest.

Existing `ui://florisboard/` and generic sharing/import filters are preserved. Android
may offer either installation for external links; internal navigation stays within
the current app. Fixed Play Store links still refer to the upstream listing. Legacy
preference names and extension IDs are format identifiers inside each sandbox.
Wear OS Data Layer requires matching application IDs and certificates, so the
existing original-ID companion does not pair with the custom phone app. A matching
custom Wear flavor is documented in [the custom companion guide](custom-wear-companion.md).
Google Play billing products for the original listing should not be assumed available to the
custom sideloaded ID. Neither integration is changed by this packaging work.

## Settings transfer

Export through the original app's Backup screen, then explicitly choose the archive
in Dictate Custom's Restore screen. There is no private-data access or silent copy.
The existing metadata check accepts `net.devemperor.dictate` and its suffixes; both
IDs pass. Code 142 backups meet the minimum supported code 64; the 142/143 mismatch
shows the existing different-version warning, not a rejection. No validation,
archive extraction or sanitization rules were changed.

Preferences include provider accounts and selected settings; saved rewording prompts
have their own JSON export. History/retained audio, personal/learned dictionaries,
keyboard/theme extensions and optional clipboard data have existing export controls.
File backups include provider credentials by default; sharing defaults them off.
Credential-free backups retain account configuration but require keys to be entered
again in the new sandbox. Private credential-bearing backups must be handled privately.
No real API credentials were exported or inspected during this work.

Downloaded transcription/translation models and other downloaded packs are not copied
by the selected settings export; download them again. Re-grant microphone,
notifications, accessibility/overlay access and any external-file access, then enable
and select the new keyboard independently. Review Bluetooth/audio settings. Clipboard
image/video restore retains old content URIs in the existing implementation, so avoid
using those optional components for cross-ID transfer; re-copy media instead. Text
clipboard and prompt/settings transfer have no such authority dependency. Full
cross-ID restore remains a device acceptance test, not an automated migration claim.

## Validation on 2026-10-18

- Both original/custom debug APKs compiled and packaged. Both release assemble tasks
  completed successfully, including R8/resource shrinking and release vital lint.
- Android SDK `aapt2` and `apkanalyzer` checks of the final release APKs confirmed the
  IDs, versions and labels in the table, valid keyboard service/packaged subtype and
  settings alias, and no shared provider authority or declared permission. Custom
  labels resolve to Dictate Custom across locales. ML Kit's generated provider also
  uses each application's ID, as does DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION.
- `apksigner` confirmed both release APKs are unsigned. Debug APKs use the Android
  Debug certificate (SHA-256
  `ec431eaf65427ce3d4671972bc355b173c736d692019417d286dc451eb38723d`),
  not a verified production identity.
- Overriding upstream version properties changed original beta's generated version
  while custom beta stayed at 143 / 6.4.0-jack.1; no override was saved to source.
- Core unit suite: **225 passed**, including all **40 Groq language regression tests**.
  The Groq commit object remains exactly
  `70a3b2b85d053870c5bd6c9b38645f8dfe55b498`; its source/tests are unchanged.
- Custom app suite: **1,205 passed** with a JDK 21 test worker. Production compilation
  used JDK 17. On JDK 17, 1,204 passed and the unchanged SelectionMetrics family-emoji
  assertion failed: Java 17 BreakIterator returns five graphemes, Java 21 returns one.
  A standalone probe confirmed the runtime difference; no unrelated source was changed.
- An initial combined build/test run exhausted the environment's memory. Sequential
  release builds and lower-memory test reruns completed; shell syntax and diff checks
  passed. The reproducible-build helper was syntax checked; its Docker/AAB/signing
  pipeline was not executed.

## Fold7 acceptance checks

No real Android device or emulator was available for this change. With a privately
signed custom release, keeping the original app installed:

- Install the custom APK, confirm a separate Dictate Custom launcher and data sandbox.
- Enable both keyboards; select each independently. Open custom keyboard settings
  through Android's keyboard settings and verify the custom settings activity opens.
- Grant microphone access; test recording, Groq Cantonese transcription and insertion
  into several apps, then switch back to the original keyboard.
- Import an original settings/prompt backup; verify account configuration and prompts,
  restoring omitted credentials privately. Re-download offline models and test them.
- Test clipboard/file sharing, recognition/voice IME and any optional accessibility use.
- On a later build, verify an upgrade using the same signing certificate and a larger
  custom version code preserves custom data while leaving the original app intact.
