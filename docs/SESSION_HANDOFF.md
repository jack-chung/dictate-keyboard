# Planner session handoff

Snapshot: 2026-10-21. This is a temporary session-transition note, not a permanent
planner branch or a substitute for current main and GitHub Issues. Recheck live refs,
issue state and available tools in the new session; runtime files and agent identities
may not survive the transition.

## Recover the planning role

Continue as the owner's primary planner, architect and engineering coordinator.
Read current main's AGENTS.md, docs/PROJECT_CONTEXT.md, docs/ROADMAP.md and relevant
docs/decisions/ records. The owner discusses requirements with the coordinator; bounded
implementers and fresh read-only reviewers report automatically when supported.
Do not assume this chat's full transcript or agent sessions are accessible.

Main holds authoritative repository docs. GitHub Issues is the shared backlog.
Use short-lived documentation branches and assigned implementation-branch doc edits.
Current main contains the approved ownership and labeling rules. Owner approval is
required for each main merge unless explicitly included in an authorized task.
No transcription feature implementation or release action is authorized by this handoff.

## Immediate task: create four priority label definitions and colors

The owner explicitly requested creation of these repository labels, updating existing
definitions as needed, in `jack-chung/dictate-keyboard`:

| Name | Color, without # |
| --- | --- |
| `priority: critical` | `B60205` |
| `priority: high` | `D93F0B` |
| `priority: medium` | `FBCA04` |
| `priority: low` | `0075CA` |

The previous session's GitHub connector could create issues and attach labels but
exposed no standalone label-definition/color editing tool. Its CLI route to
`api.github.com` was blocked by environment network policy.

The owner now reports allowing `api.github.com`; they expect that change to apply in
a newly opened chat/runtime. Verify the new runtime's network policy and authentication
readiness using the applicable runtime skill before retrying the CLI/API route.
A CLI token was configured and `gh auth status` exited zero in the old session, but
actual API acceptance and label-editing permission were NOT established. Do not claim
the auth-status result proves a successful API check. Do not print credentials,
inspect secret values or request pasted tokens. Do not bypass a network-policy denial.

Inspect existing definitions, create/update the four names idempotently and verify
names/colors by reading them back. Preserve existing issue labels and priority choices;
creating the definitions does not assign all four priorities to an issue. No main merge
or signed build is needed for this GitHub metadata task.

## Verified live state at handoff

- Main: `4b56780e99434e87e4345d6cd0f2de1cc5d06ea9`.
- Owner-approved merges already completed:
  - `docs/codex-project-handoff` through `e7622cdc`.
  - `docs/planner-agent-workflow` through `1df3f88b`.
  - `docs/issue-label-policy` through `4b56780e`.
- [Issue #1: Wear tethered-to-standalone handoff](https://github.com/jack-chung/dictate-keyboard/issues/1)
  was successfully migrated after the owner enabled Issues. It is open, owner-priority
  low, awaiting investigation, with no implementation or release assignment authorized.
  It contains the full reproduction report, hypotheses, proposed safeguards and tests.
- Issue #1 labels were read back as `bug` (color `d73a4a`) and `priority: low`
  (color `ededed`, still awaiting the requested blue). Other priority definitions were
  not independently checked. Do not infer their existence from policy text.

## Unmerged documentation

`docs/issue-tracker-setup` at `834943731922eced22569b9583d96a959379df0e`
contains the successful issue migration links in ROADMAP, PROJECT_CONTEXT and the
historical Wear intake report. It also records the earlier disabled-Issues blocker and
its resolution. This branch has not received merge approval. Main's corresponding
migration prose is therefore stale; the live GitHub issue is authoritative.

Review that branch against current main, preserving the merged AGENTS label policy.
Do not silently merge it. No broad automatic documentation-merge authorization exists.
This session-handoff branch also has no merge approval and need not be merged merely
to read the note. Retire/update the temporary note when its pending work is resolved.

## Other pending planning decisions

The owner asked about migrating other known limitations/tweaks from PROJECT_CONTEXT.
The coordinator recommended selective issue intake for:

- Tethered versus standalone style-prompt differences.
- Manual refresh needed for some Wear settings.
- Missing phone settings in standalone Wear configuration.
- Keyless-server standalone eligibility.
- Custom Wear install shortcut pointing to the original Play listing.
- Stale Gemini endpoint comment.

These remain candidate investigations/enhancements/chores, not six confirmed device
bugs or approved implementations. They have not been migrated; approval to bulk-create
these additional issues was not established. Keep constraints in context unless the
owner chooses to change behavior. Do not duplicate Releases 2–6 as alleged bugs.

The roadmap remains provisional: SenseVoice OpenCC, native Gemini LOW thinking,
quota fallback, profiles and language routing. No next feature has been authorized.
Release/signing/artifact and device-validation claims retain their existing evidence
labels; the Wear report supplies only partial owner-reported device observations.

## Next user-facing response

After reading this note and current sources, briefly confirm recovered scope and
continue the authorized priority-label creation task. Do not ask the owner to paste
the old conversation or repeat settled policy. If blocked, identify the actual
network/authentication/tool failure and preserve the distinction between configured
credentials and verified API access.
