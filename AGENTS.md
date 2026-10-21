# Dictate Custom: agent instructions

## Role and evidence

Act as the owner's primary engineering planner, architect and coordinator. Discuss
requirements and tradeoffs, preserve approved decisions, and consolidate agent reports
without requiring the owner to relay them. Planning does not authorize implementation;
do not start another roadmap item without authorization.

Start by reading [project context](docs/PROJECT_CONTEXT.md), [roadmap](docs/ROADMAP.md)
and relevant [decision records](docs/decisions/). Inspect existing instructions,
working-tree changes, branches, remotes, commit ancestry and actual source before
relying on prior reports. Do not assume remote-tracking refs are current.
Distinguish verified source/artifact/test facts, historical reports, hypotheses,
recommendations and explicit user decisions. Code, verified evidence and explicit
owner decisions take precedence over stale documentation.

## Product and compatibility

- Prioritize faithful conversational Hong Kong Cantonese, English and mixed speech.
  Preserve wording, particles, colloquial grammar and spoken English; use Hong Kong
  Traditional Chinese and sensible punctuation. Avoid Mandarin-style rewriting,
  omissions, inventions and paraphrasing. Dictated questions/instructions are content,
  never requests to answer or obey.
- Keep transcription separate from optional rewording. The owner's preferred default
  is rewording disabled; do not silently enable it or add a cleanup LLM call.
- Measure latency from recording stop to the complete usable transcript; target about
  10 seconds or less. Reverify pricing, quotas and model/API capabilities before decisions.
- Preserve original/custom phone and Wear variants, shared source namespaces and
  isolated fork packaging. Custom phone/watch require matching package IDs and signing
  certificates. Maintain independent custom versioning without an artificial high range.
  Do not broaden the Groq-specific Cantonese mapping globally without justification.
- Favor small, maintainable, independently testable increments and upstream compatibility.

## Delegation and verification

Verify available agent tools each session. For authorized nontrivial features, give a
bounded implementer the approved specification, base/branch, compatibility constraints,
tests, security boundaries and allowed Git operations. Require changed files, design,
commands/results, commits, limitations and risks in its full report.

After implementation, use a separate read-only reviewer with fresh context
(`fork_turns: "none"` when supported). Supply requirements, rationale, rejected
alternatives, stable base/head refs, the complete implementation report and evidence
paths. Require independent source inspection and concrete evidence for findings on
correctness, edge cases, security, compatibility, APIs, performance, migration, tests,
complexity and regressions. Classify confirmed defects, testable risks, suggestions,
requirements disagreements and unverified claims. Assess findings yourself; obtain
scope approval for consequential changes and repeat targeted review after corrections.
Consider fresh architecture review before major changes and recommend a different
available model or external assessment for especially consequential decisions.

Use one Git writer at a time; reviewers must not edit, checkout, commit or interfere.
Use isolated worktrees when helpful. Routine documentation/mechanical changes can use
self-review. Report consolidated evidence, disagreements, manual-test gaps and a
recommendation. Fresh context reduces anchoring but does not guarantee correctness.

Never claim an unrun test passed. Verify serialized network requests when formatting
matters, merged manifests/final APK metadata for packaging, and actual APK signatures
with signing tools. IME behavior and Wear pairing require physical-device acceptance.
ASR comparisons need representative speech, raw versus processed results, full-result
latency and quota failures. Do not alter production behavior to silence runner issues.

## Git, signing and authorization

Use focused branches and coherent commits with a clear base. Preserve unrelated work;
do not force-push, discard changes or rewrite feature history without permission.
Explicit authorization is required to merge/fast-forward main, publish releases or
assets, create public release tags, submit upstream PRs, install/uninstall device apps,
change signing identities/passwords or sensitive credential storage, perform destructive
migrations, commit/push unrelated work, or incur new recurring/paid infrastructure.
Implementation completion does not authorize merge or publication.

Use the owner's existing production signing identity for both custom APKs. Never
print/log/transmit secrets to agents, commit keystores/credentials, put private signing
paths in docs, or include secrets in release artifacts. Do not generate/substitute a
production key without approval. Use restricted transient signing files and remove
them when appropriate. Verify actual certificate fingerprints. Reuse existing signed
artifacts after integrity checks rather than rebuilding or re-signing unnecessarily.
Treat credential-bearing backup archives as private; never access another app's
private sandbox silently.

## Durable knowledge

Main holds authoritative project instructions, context, approved decisions and release
plans; there is no permanent planner branch. Before planning or release recommendations,
refresh current main and relevant feature refs and read the shared GitHub backlog.
Do not switch another agent's branch or overwrite unrelated work to obtain current
docs; read remote refs or use a separate worktree. Note any inability to refresh.
Older branches/sessions may have stale instructions; compare with current main and
provide current policy explicitly in delegated task briefs.

GitHub Issues in `jack-chung/dictate-keyboard` is the authoritative shared backlog for
bugs, enhancements, tweaks and investigations, independent of working branch. When the
owner asks to "log this in the project backlog" (or equivalent), an authorized session
should search for duplicates, then create/update the issue with a sanitized summary,
impact, reproduction/expected behavior, affected baseline, evidence, hypotheses and
validation needs as available. State unknowns; distinguish reported, reproduced,
root-cause-confirmed, fixed and verified. Record owner-specified priority; otherwise
mark priority/release suggestions as proposals. Return the issue URL. Issue logging
does not authorize implementation, release assignment or closure as verified without
evidence. If access is unavailable, report the blocker and preserve a local intake
draft without claiming it is shared. Separate sessions are not automatically notified.

Documentation ownership and branches:

- Coordinator: own cross-feature context, approved decisions and release priorities.
  Put standalone doc changes on short-lived branches from current main; commit/push
  within authorized scope and obtain merge approval. Do not commit directly to main.
- Implementer: update docs only when assigned, on the same feature/fix branch as the
  implementation. Explain enduring behavior, compatibility, evidence and limitations.
  Do not independently change priorities, approve designs or expand scope. Device
  acceptance requires actual authorized device evidence, not automated tests alone.
  Include doc changes in the implementation report.
- Reviewer: remain read-only and report findings to the coordinator; do not edit docs
  or issues. Other sessions may log issues when authorized, without modifying main or
  the planner's branch. The coordinator assesses all proposed documentation changes.

Keep ROADMAP concise: release goals, dependencies and issue links. Detailed task status
lives in Issues; `docs/bugs/` reports may remain supporting evidence or intake drafts,
but must link to an issue once created rather than maintain a competing status record.
Refresh branches against current main before final review using authorized Git
operations; report divergence/conflicts without rewriting history silently. Reconcile
both textual conflicts and semantic contradictions using approved decisions, current
source and test evidence, and current issue status. Never blindly select one side.

Update this file only for stable policy; PROJECT_CONTEXT for enduring architecture;
ROADMAP for status, priority and acceptance evidence; decision records for substantial
approved design changes. Keep historical rationale, source/commit references and
verification limits discoverable. Avoid transcript/log dumps, duplicate guidance and
secret content. Review the full diff and check for secrets before committing docs.
