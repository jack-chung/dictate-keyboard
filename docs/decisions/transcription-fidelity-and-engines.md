# Faithful transcription and provisional engine strategy

Status: fidelity/rewording policy explicitly approved by owner; engine strategy and
release order provisional. Routing, OpenCC and audio thinking configuration are future
work, not implemented by this record. Recorded from owner handoff on 2026-10-20.

## Objective and constraints

Accurately transcribe conversational Hong Kong Cantonese, English technical speech
and mixed speech. Preserve exact wording, particles, colloquial grammar and spoken
English. Cantonese output should be Hong Kong Traditional Chinese with sensible
punctuation. Speech containing a question or instruction must remain transcript
content. Stylistic elegance must not displace fidelity.

Target a full usable result within about 10 seconds after recording stops, ideally
faster. Minimize cost where fidelity is comparable. Historical Cantonese usage was
fewer than 20 requests/day; free quotas and model availability require fresh checks.

## Selected approach and alternatives

Use one ASR/audio-plus-instruction request by default. Keep optional AI rewording
separate and disabled for the owner's preferred use. Historical cleanup-LLM trials
answered questions, followed dictated instructions or rewrote content, so an automatic
second generative cleanup call is rejected as the default.

Provisional engines:

| Use | Intended engine | Reason and limit |
| --- | --- | --- |
| English | Groq Whisper Large V3 | Fast, economical candidate; validate technical vocabulary |
| Cantonese (Hong Kong) | Gemini Flash native audio, LOW thinking | Best observed fidelity/latency compromise in limited historical trials |
| Offline fallback | SenseVoice Small with deterministic OpenCC `s2hk` | Offline script conversion without another LLM; cannot restore missing speech |

These are intended configurations, not automatic production language routing.
The historical Gemini model label `gemini-3.8-flash` is reported evidence, not a
freshly verified catalog ID or API compatibility guarantee. Benchmark limitations
and numbers are retained in [project context](../PROJECT_CONTEXT.md).

Separate releases first add deterministic conversion, then native audio thinking
control, then quota fallback, profiles and language routing. Discuss evidence-based
changes to this order with the owner before changing the plan.

## Compatibility and acceptance

- Preserve legacy behavior when new settings are absent and profiles do not exist.
- Keep the Groq `yue-HK`/`yue-CN` mapping scoped to the official endpoint and the two
  supported Whisper models; no global Cantonese remapping is approved.
- Thinking control belongs to native Gemini audio transcription, independently of
  Rewording AI. Inspect model-dependent GenerateContent/Interactions and alternate
  chat-completion paths; the single-call UI label alone is insufficient evidence.
- Verify serialized requests, exact prompt semantics, appropriate API support and
  full-result latency. Compare raw ASR and deterministic conversion separately.
- Test representative Cantonese/mixed technical speech, adversarial dictated
  questions/instructions, English, already-Traditional output and quota failures.

## Unresolved work

Reverify model IDs, supported thinking values, pricing and quotas; broaden the corpus
and repetitions. Evaluate OpenCC dependency size, license, Android compatibility,
runtime cost and appropriate conversion eligibility before implementation. Daily
exhaustion versus transient rate limits needs provider evidence and bounded policy.
No new live API benchmark or feature implementation was authorized for onboarding.
