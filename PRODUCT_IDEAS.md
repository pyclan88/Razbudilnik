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
- Let every user import one personal book for free so they can test the complete import and alarm
  challenge flow before purchasing.
- Unlock importing personal books with one permanent premium purchase.
- Treat the purchase as a non-consumable entitlement, not a subscription.
- Describe the feature as `Import books`, not `Upload books`, while files remain on the device.
- Do not require an account or server merely to import and read a local book.

Alternative preserved:

- Monthly import access at approximately 50 RUB was considered.
- It is not currently selected because expiration would make access to the user's imported books
  confusing, while a permanent unlock matches a local device capability more naturally.

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

### IDEA-004: Explain The Alarm Challenge Contract

Status: `Candidate`

Before the user enables an alarm for the first time, clearly explain that completing the reading
challenge is the normal way to dismiss the alarm.

The explanation must also teach the temporary silencing gesture:

- Keep moving a finger across the reader to keep the alarm quiet.
- Stopping the movement causes the alarm sound to return.
- Completing every required page dismisses the alarm.

UX direction:

- Show a one-time confirmation sheet before enabling the first alarm.
- Include a `Try gesture` action so the user can practise the interaction while fully awake.
- Let the practice surface demonstrate both successful movement and what happens when movement
  stops.
- During a real challenge, show short state guidance such as `Move your finger to quiet the alarm`,
  `Quiet while moving`, and `Keep moving to stay quiet`.
- Do not claim that stopping the alarm is technically impossible because Android still permits
  force-stop, uninstalling, and powering off the device.

Related ideas:

- IDEA-005: Finger Movement Feedback

### IDEA-005: Finger Movement Feedback

Status: `Candidate`

During the alarm challenge, draw a short translucent trail behind the user's finger. The trail
should provide immediate visual confirmation that the app detected meaningful movement and that the
movement is currently keeping the alarm quiet.

UX direction:

- Fade the trail after approximately 500-800 milliseconds.
- Make meaningful movement produce a clearer or longer trail.
- Make tiny movements produce little or no trail so the visual feedback matches the anti-cheating
  movement threshold.
- Remove the trail when movement stops.
- Keep it translucent and short enough that it does not obscure the book text.
- Consider a small progress ring around the touch point to show whether enough movement has been
  accumulated for the current timer tick.
- Return the alarm sound gradually when valid movement stops.

Related ideas:

- IDEA-004: Explain The Alarm Challenge Contract

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
