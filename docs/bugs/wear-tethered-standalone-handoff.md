# Wear OS tethered-to-standalone transcription handoff

Reported: 2026-10-21. Priority: **low**. Status: **reported; awaiting investigation**.
No fix, release assignment or implementation authorization is recorded. Current task
is documentation only. Source findings below come from another Codex session via
the owner's report; they were not independently rechecked during this intake.

## Affected baseline and impact

- Repository: `jack-chung/dictate-keyboard`.
- Owner-described released baseline: `99805943618077059d95204f34407cfb7f8e92ed`.
  Live GitHub main was confirmed at that commit during intake. Artifact identity and
  distribution/publication status were not independently verified here.
- Phone and watch: Dictate Custom, `net.devemperor.dictate.jack`,
  `6.4.0-jack.1`, versionCode 143.
- Devices: Samsung Galaxy Z Fold7 and Samsung Galaxy Watch8 Classic.
  Android/Wear OS versions were not supplied.
- Owner reports settings synchronization and connected-phone dictation work.
  Standalone dictation has succeeded, but transition after phone disconnection is
  unreliable. These are partial device observations, not complete release acceptance.
- Impact: delayed or failed watch dictation when the phone becomes unavailable,
  despite the watch having independent internet access.

## Reproduction and observed behavior

1. Put the phone in airplane mode and explicitly disable its Wi-Fi and Bluetooth.
2. Wait until the watch reports losing its phone connection.
3. Keep the watch connected to the internet independently.
4. Start dictation on the watch.

Expected: promptly use standalone transcription when the intended phone app is
unavailable and synchronized configuration/credentials allow it.

Observed by owner: the watch appears to continue attempting tethered dictation before
recognizing standalone availability. Attempts produced connection errors mentioning
`api.groq.com`, long waits around 30 seconds, cancellations and eventual successful
transcription. Other watch apps could access the internet. Frequency, exact error text,
selected Groq model, timings by phase and device logs were not supplied.

## Evidence and hypotheses

The delayed-handoff explanation is a **working hypothesis**, supported by source
inspection reported from the other session. It is not confirmed by device logs.
Groq connection errors may have an additional network cause; other apps' internet
access does not establish successful connectivity to this particular endpoint.

Reported source findings (paths relative to repository root):

- `wear/src/main/kotlin/net/devemperor/dictate/wear/ui/WearSettingsActivity.kt`:
  `phoneConnected` updates when settings composition starts and synchronization is
  requested. “Transcribes via phone/on watch” is not continuously refreshed and is
  not evidence of the route a particular request actually used.
- `wear/src/main/kotlin/net/devemperor/dictate/wear/sync/WearSyncClient.kt`:
  `findPhoneNodeId()` queries reachable capabilities, then falls back to connected
  nodes. Availability comes from Google Play services without an application-level
  acknowledgment from the intended phone app.
- `wear/src/main/kotlin/net/devemperor/dictate/wear/transcribe/WearTranscription.kt`:
  `transcribe()` discovers on each request and prefers tethering if a phone node is
  returned. `tether()` awaits channel opening/output-stream acquisition and uploads
  audio without a short app-enforced setup deadline. The explicit 30-second silence
  check applies while awaiting a response after audio transmission. Transport failures
  can fall back to standalone. `PhoneSilent` deliberately prevents automatic fallback
  after audio was sent, protecting against duplicate processing/billing.

No new reproduction, tests, network probes or runtime traces were collected in intake.
Root cause is unconfirmed. No reliable workaround was established.

## Proposed investigation and smallest safe fix

The following is a proposal retained for later scope approval, not an implemented or
fully specified design:

1. Verify that the intended custom phone app responds promptly before selecting
   tethering, using a short, bounded availability check.
2. Bound tether setup and distinguish failures before audio delivery from uncertain
   outcomes after the phone may have accepted audio. Define the delivery boundary
   from actual transport/protocol evidence rather than assuming it from UI status.
3. Fall back promptly when the phone is unavailable and synchronized credentials
   permit standalone transcription.
4. Preserve protection against duplicate processing/billing after possible delivery.
5. Refresh connection status on connectivity changes/resume and, where practical,
   make displayed routing information reflect actual request state.
6. Preserve cancellation, definitive provider-error handling, credential-sync controls
   and original/custom package-and-certificate isolation.

Do not simply shorten every timeout or fall back whenever a phone response is slow.
Any application-level availability exchange must be assessed for existing protocol
and mixed-version compatibility before implementation.

Useful evidence to obtain: sanitized per-phase discovery/setup/upload/response/fallback
timings and errors, actual request route, phone acknowledgment/delivery state, model
and relevant non-secret settings, OS versions and independently reproduced Groq
connectivity behavior. Avoid retaining sensitive dictated audio or credentials in logs.

## Required validation for a future fix

Focused automated cases:

- Stale discovery and an unresponsive phone.
- Setup timeout and successful tethering.
- Standalone unavailable because credentials are absent or synchronization disallows it.
- Cancellation without unintended fallback.
- Uncertain post-upload outcomes without duplicate requests.
- Disconnection before dictation and during a request.
- Definitive provider errors remain definitive rather than triggering duplicate ASR.

Physical Watch8 Classic acceptance:

- Prompt handoff after phone loss and successful standalone dictation using independent
  internet access.
- Return to tethered transcription after reconnection.
- Accurate connection/request-routing status.
- No duplicate transcription requests or billing; record the evidence and its limits.

All validation remains pending. Tests and device observations must be recorded
separately from hypotheses and source-only findings.

## Implementation and release boundaries

When implementation is separately authorized, inspect current refs and working tree,
create a dedicated bug-fix branch with a clear base, and preserve unrelated work and
existing Groq, packaging and signing changes. Apply the project's independent-review
workflow for the transport/protocol change.

Do not change main, publish, install, retrieve signing credentials or produce a new
signed release without separate authorization. A future distributed update must
increase custom versionCode and retain the existing signing identity. No code changes,
fix branch, test execution or signed build were produced by this documentation intake.

Tracking: [roadmap](../ROADMAP.md#known-bugs--triage).
