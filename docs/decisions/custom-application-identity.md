# Separate custom identity with shared sources

Status: owner-approved; implemented and merged in the commits below. Physical-device
acceptance remains pending. Recorded during onboarding on 2026-10-20.

## Problem and requirements

The owner needs a personally signed phone/watch fork that can coexist with original
Dictate, preserve the original build, and remain reasonably compatible with upstream.
Switching back must not depend on artificially high private version codes.

## Decision and rationale

Use an `identity` flavor dimension (`original`, `custom`) in both Android modules.
The custom application ID is `net.devemperor.dictate.jack` on both devices. Keep
existing phone and Wear namespaces/source trees. Limit packaging differences to
flavor configuration/resources and independent `custom-version.properties` values.

Custom phone and Wear initially share code 143 / name `6.4.0-jack.1`; optional
`wearVersionCode` and `wearVersionName` permit independent later watch releases.
Each device's installed custom app needs an increased code for updates. The original
Wear Play version band remains intact; it is not a custom versioning precedent.
Future distribution through one Play listing would require distinct form-factor codes.

Use the same existing owner production certificate for custom phone and watch.
Retain the existing Wear Data Layer protocol and package/certificate isolation.
Preserve the custom phone capability resource during shrinking with its keep rule.
Discovery of a physical node does not prove successful delivery to the matching app.

## Alternatives and consequences

- Replacing the original package ID would complicate coexistence and switching.
- A high private version-code band is unnecessary with separate IDs and independent
  upgrade sequences.
- Renaming source packages or inventing a custom Wear protocol adds maintenance
  cost without meeting an additional requirement.
- Separate IDs create separate sandboxes: explicit backup import/configuration is
  required, and offline models must be downloaded separately. No silent data access.

## Acceptance and evidence

Verify original/custom manifests, IDs, labels, versions, authorities and IME metadata
in final APKs; check production signatures and matching certificates with `apksigner`.
Test side-by-side installation, selection, phone/watch isolation, sync, cancellation,
text delivery, standalone operation and later data-preserving upgrades on devices.

Source and ancestry rechecked at `99805943618077059d95204f34407cfb7f8e92ed`:

- Phone: `997d4c09853846230f5d4196a89b98709c2fd6ad`.
- Wear and capability keep rule: `99805943618077059d95204f34407cfb7f8e92ed`.
- Implementation/source maps: [phone guide](../custom-application.md) and
  [Wear guide](../custom-wear-companion.md).
- Current artifact and device evidence: [roadmap](../ROADMAP.md).

Final signed APKs and installed original-ID certificate were not inspected during
onboarding. Real pairing and migration remain open acceptance questions.
