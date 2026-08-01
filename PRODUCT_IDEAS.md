# Razbudilnik Product Ideas

This file preserves product ideas across PCs and Codex sessions.

## How To Use This File

- Preserve the user's original intent when recording an idea.
- Give every idea a stable ID.
- Do not overwrite an earlier idea with a similar or conflicting one.
- Link related ideas and record alternatives until the user chooses between them.
- Move implementation details into `CODEX_HANDOFF.md` once an idea becomes active branch work.
- Review relevant entries before planning a new branch, PR, or feature.

## Statuses

- `Captured`: recorded but not yet evaluated.
- `Candidate`: worth considering for a future branch.
- `Planned`: accepted and placed in the development sequence.
- `Active`: currently being implemented.
- `Implemented`: completed in the application.
- `Rejected`: deliberately not planned; keep the reason.

## Ideas

### IDEA-001: Picture-in-Picture Return To Active Challenge

Status: `Planned`

When the user minimizes an active alarm challenge, show a compact Picture-in-Picture return surface
so they can return without finding the launcher icon or notification.

Constraints:

- PiP must not dismiss, stop, or complete the alarm.
- Reading progress remains paused while the full reader is not visible.
- Closing PiP hides only the return surface.
- The ongoing alarm notification remains the fallback return path.

Related work:

- Planned branch `13-alarm-challenge-pip-return`.
- Detailed implementation scope is maintained in `CODEX_HANDOFF.md`.

### IDEA-002: Persist Active Reader Progress

Status: `Planned`

Persist the active book, page, and completed reading progress so process death or recovery does not
restart the challenge from the beginning.

Related work:

- Planned after the Picture-in-Picture branch.
- Stable `bookId` and `pageId` should identify persisted progress.

### IDEA-003: User-Imported Books

Status: `Candidate`

Allow the user to add personal reading content.

Monetization update:

- Keep the reliable alarm, reader challenge, and bundled public-domain books free.
- Unlock importing personal books with one permanent premium purchase.
- Treat the purchase as a non-consumable entitlement, not a subscription.
- Describe the feature as `Import books`, not `Upload books`, while files remain on the device.
- Do not require an account or server merely to import and read a local book.

Premium value:

- Import personal books from the device.
- Build a personal book library.
- Select imported books as alarm-challenge content.
- Receive later supported-format additions as part of the permanent unlock.

Current format direction:

- Start with TXT import because it has the smallest parsing and security surface.
- Consider FB2 and EPUB later as separate extensions.
- Do not treat later format support as a reason to delay the first import flow.

Billing constraints:

- Keep premium entitlement behind a domain abstraction instead of importing a store SDK into reader
  domain logic.
- A Google Play build should use Google Play Billing.
- A RuStore build should use RuStore Pay SDK.
- Development builds need an explicit test entitlement instead of pretending a purchase occurred.

Open decisions:

- Purchase price and localized pricing.
- Whether to offer a temporary introductory discount.
- Which formats are included when the premium feature first ships.
- Final paywall text and where the locked import entry point appears.

Related work:

- Depends on stable book identity and reader-progress persistence.
- Book selection and library UI remain separate product decisions.

## Idea Template

```markdown
### IDEA-XXX: Short Name

Status: `Captured`

Original idea:

<Preserve the user's intent.>

Alternatives or updates:

- <Record later variations without deleting the original.>

Related ideas:

- IDEA-XXX

Decision:

<Leave empty until a decision is made.>
```
