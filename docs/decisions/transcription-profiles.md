# Provider accounts, transcription profiles and language routing

Status: architecture selected and approved in owner handoff; implementation is planned
for Releases 5 and 6 and requires separate scope authorization. Recorded 2026-10-20.

## Problem and requirements

Different engines need different transcription settings while sharing provider
credentials. Cantonese fidelity requires precise audio instructions and thinking
configuration; Whisper and local SenseVoice do not share generic LLM capabilities.
Fallback must resolve its own engine configuration. Existing users must retain legacy
behavior when they have no profiles. Rewording stays optional and separate.

## Approved ownership and UX

1. **Dictate → AI providers** owns accounts, credentials, provider-default models,
   provider capabilities and appropriate provider-default transcription thinking.
2. **Dictate → Transcription profiles** owns reusable named configurations with stable
   identities referencing existing provider accounts. Do not copy credentials.
3. **Dictate → Transcription languages** owns each selected language's primary profile
   and optional fallback profile. Provide explicit routes for English, Cantonese
   (Hong Kong) and Auto-detect. Auto-detect needs a default route before ASR knows
   the language.
4. Top-level **Languages & Layouts** remains keyboard IME layout management.
5. **Rewording AI** remains independent; do not enable it through profile migration.

## Profile resolution

Each profile includes a name/stable ID, provider-account reference, inherited model
with optional override, inherited thinking with optional supported override,
instruction mode (**Inherit**, **Append**, **Exact Replace**), engine-specific
configuration, optional deterministic post-processing, and a view of effective
resolved settings. Exact Replace must allow literal replacement rather than wrapping
the instruction in a generic style/context prompt.

Model and thinking inheritance follow provider defaults unless overridden. Validation
and UI must reflect engine capabilities:

- Gemini: rich native audio instructions, supported thinking controls, exact replacement.
- Groq Whisper: model/language and supported prompt hints, not generic LLM instructions.
- SenseVoice: supported local recognition settings and optional deterministic OpenCC;
  no arbitrary generative instruction field.

A fallback resolves from its own account and profile. Never carry an incompatible
Gemini prompt into Whisper/SenseVoice implicitly. No-profile installations retain the
existing behavior; adding profiles must not silently change legacy requests.

## Alternatives and rationale

Per-language credential copies would duplicate sensitive state and account management.
A universal prompt field would imply capabilities engines do not have. Routing under
keyboard layouts confuses ASR choices with typing layouts. A cleanup LLM would add
latency and semantic risk to faithful dictation. These alternatives were rejected.

## Acceptance and compatibility

Test stable references, inherited versus overridden effective settings, exact/append/
inherit serialization, unsupported capability validation, legacy behavior, explicit
Auto-detect routing and fallback configuration isolation. Define behavior for missing
accounts/models/profiles before implementation. Keep small independent releases.

Wear compatibility needs explicit optional sync fields, defaults, phone projection,
watch consumption and mixed-version fixtures where the feature applies. Existing
unknown-key tolerance does not mean an old watch implements a new setting.

## Open questions and evidence

Persistence/schema migration, deletion behavior, route/profile editing UX, fallback
triggers and phone-versus-standalone-watch support need a concrete specification and
owner discussion before coding. This record approves architectural boundaries, not
unresolved implementation details or automatic cloud-to-cloud fallback policy.

At source base `99805943618077059d95204f34407cfb7f8e92ed`, provider accounts and native
Gemini style wrapping already exist, but these reusable profiles and routes do not.
See [source map](../PROJECT_CONTEXT.md#source-map) and [release dependencies](../ROADMAP.md).
