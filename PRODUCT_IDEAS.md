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

Compact-surface direction:

- Make PiP look like a miniature, read-only reader rather than a generic alarm badge.
- Show the current page number, a short excerpt, completed reading time, required reading time, and
  whether the alarm is sounding.
- Include a concise return prompt such as `Tap to continue`.
- Do not accept reading gestures or advance progress inside PiP.
- Use the same `ReaderViewModel` state for the full reader and PiP so expanding the window restores
  the same challenge.

Related work:

- Planned branch `14-alarm-challenge-pip-return`.
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
- Let every user import one personal book for free once so they can test the complete import and
  alarm challenge flow before purchasing.
- Unlock importing personal books with one permanent premium purchase.
- Treat the purchase as a non-consumable entitlement, not a subscription.
- Describe the feature as `Import books`, not `Upload books`, while files remain on the device.
- Do not require an account or server merely to import and read a local book.

Free-import enforcement decision:

- After the first successful personal-book import, persist `hasUsedFreeImport = true`.
- Include only the free-import entitlement state in Android Auto Backup so Android may restore it
  after reinstallation.
- Exclude alarm-enabled state, active alarm sessions, scheduled-alarm state, and temporary reader
  state from backup.
- Treat Auto Backup as soft enforcement because backup may be delayed, disabled, unavailable on
  some devices, or bypassed by changing accounts or reinstalling before a backup occurs.
- Test backup and restore behavior on supported physical devices before relying on it.
- Do not introduce a Razbudilnik account, backend, persistent phone identifier, or Play Integrity
  Device Recall for the initial implementation.
- Reconsider server-side enforcement only if real usage data later shows meaningful abuse.

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
- Use green while valid movement keeps the alarm quiet.
- Use amber during the grace period after movement stops.
- Use red or coral once the alarm sound returns.
- When sound returns, briefly pulse the screen edges and shake the status message once.
- Do not continuously flash the whole screen or shake the book text. The reading content must remain
  stable, and warning animation must avoid unnecessary accessibility risk.

Related ideas:

- IDEA-004: Explain The Alarm Challenge Contract

### IDEA-006: Pastel Green Visual Theme

Status: `Candidate`

Use muted pastel-green shades as the app's main visual identity in both light and dark themes.
Green should be the brand and successful-interaction color rather than the only color used
throughout the interface.

Semantic color direction:

- Green represents valid movement, a quiet alarm, completed reading progress, and selected states.
- Amber represents movement stopping and the alarm sound beginning to return.
- Red or coral represents an actively sounding alarm, errors, and missing required access.
- Neutral gray backgrounds and surfaces keep text readable and prevent the interface from becoming
  monochromatic.

Initial palette proposal:

| Role                    | Light theme | Dark theme |
|-------------------------|-------------|------------|
| Background              | `#F6F8F6`   | `#101512`  |
| Surface                 | `#FFFFFF`   | `#181E1A`  |
| Primary                 | `#326B4C`   | `#9BD3AC`  |
| Primary container       | `#CDE8D5`   | `#254F37`  |
| Main text               | `#18211B`   | `#E3EAE4`  |
| Secondary text          | `#566159`   | `#B8C2BA`  |
| Outline                 | `#AEB8B0`   | `#849087`  |
| Warning                 | `#A66000`   | `#FFB95C`  |
| Error or sounding alarm | `#B3261E`   | `#FFB4AB`  |

Finger-trail proposal:

- Light theme trail: `#42A66B`.
- Dark theme trail: `#7FE7A3`.
- Keep the trail translucent and short-lived so it confirms movement without covering the text.

The exact color values remain proposals until the main screens and alarm challenge are designed and
checked for contrast in both themes.

Related ideas:

- IDEA-004: Explain The Alarm Challenge Contract
- IDEA-005: Finger Movement Feedback

### IDEA-007: Reader-First Product With Premium Alarm Challenges

Status: `Candidate`

Position Razbudilnik as a reader with a distinctive wake-up alarm rather than as an alarm app that
temporarily displays book pages.

Reader behavior:

- Let the user open the reader without starting an alarm.
- Let the user continue reading after completing and dismissing an alarm challenge.
- Persist normal reading progress so the reader is useful during the day.
- Let the user import personal books for ordinary reading without requiring premium.

Challenge-to-reader transition decision:

- Completing the challenge must stop alarm sound, remove the notification, cancel recovery, and
  persist completed challenge progress immediately.
- After cleanup, keep the reader open and switch the current session from challenge mode to normal
  reader mode instead of automatically closing `AlarmActivity`.
- Keep the completed challenge page visible and show a short `Alarm dismissed` confirmation.
- Do not automatically advance to the next page; let the user choose when to continue.
- In normal reader mode, remove the movement requirement and timer, unlock ordinary Back and Next
  navigation, and allow the user to leave freely.
- Pressing Next after dismissal should continue from the page following the completed challenge.
- Separate alarm cleanup from closing the activity. The current `stopAlarm()` behavior couples
  cleanup with `finish()` and must be split when this transition is implemented.

Alarm-challenge monetization:

- Keep bundled public-domain books available for free alarm challenges.
- Require permanent premium entitlement to select personally imported books as alarm-challenge
  content.
- Describe the premium value as `Use your books for wake-up challenges`, not as payment for access
  to the user's own files.
- Keep imported books readable outside alarm challenges even when premium is not owned.

Product-positioning direction:

- The main product promise remains waking up by reading; do not market the application as a generic
  replacement for mature ebook readers before its reading experience can support that claim.
- Free reading gives users a reason to return between alarms and makes challenge reading continue
  naturally instead of ending at dismissal.
- The alarm challenge remains the differentiating feature rather than becoming a settings panel
  attached to an undistinguished reader.

Alternative to preserve:

- IDEA-003 currently proposes one free personal-book import followed by a permanent premium import
  unlock.
- This reader-first model instead makes importing and ordinary reading free, while premium controls
  whether imported content may be selected for alarm challenges.
- Do not implement both models simultaneously. Choose one monetization boundary before building
  import entitlement logic.

Open decisions:

- Whether one imported book may be used in an alarm challenge for free before premium is required.
- Whether the initial application navigation should lead with the library or with the next alarm.
- How much ordinary-reader functionality belongs in the MVP before reader-first positioning is
  credible.
- Whether the product name and store description should use `reader alarm`, `wake-up reader`, or
  another category phrase.

Related ideas:

- IDEA-002: Persist Active Reader Progress
- IDEA-003: User-Imported Books
- IDEA-004: Explain The Alarm Challenge Contract

### IDEA-008: Debug-Only Challenge Bypass

Status: `Planned`

Add a fast way to complete an active reader challenge during development so repeated alarm,
notification, recovery, and UI tests do not require reading five pages for ten seconds each.

Constraints:

- Keep the bypass under `src/debug`; release builds must not contain or expose it.
- Route the bypass through the real challenge-completion behavior so alarm sound, foreground
  notification, active-session state, volume protection, and recovery are stopped normally.
- Do not implement the bypass by killing the process or service directly because that would skip
  the cleanup behavior the test is meant to verify.
- Keep the bypass visually obvious in debug builds so it cannot be confused with production UX.

Related work:

- `CODEX_HANDOFF.md` records the related deferred debug reader preview activity.
- Planned branch: `15-debug-challenge-bypass`.
- Proposed PR title: `15. Add a debug-only challenge bypass`.
- Implement the bypass after the Picture-in-Picture branch rather than mixing it into that product
  feature or another unrelated change.
- Add a later `DEBUG: Trigger alarm` control that starts an alarm immediately through the real
  receiver, ringing-service, notification, active-session, and reader-entry flow. Keep the control
  under `src/debug` so release builds cannot contain or expose it.

### IDEA-009: English And Russian UI Localization

Status: `Planned`

Support both English and Russian throughout the application UI.

Initial localization direction:

- Keep English in the default `values/strings.xml` resources.
- Add Russian translations under `values-ru/strings.xml`.
- Put user-facing text in string resources instead of hardcoding it in Kotlin or Compose.
- Follow the device or application language automatically.
- Keep Kotlin identifiers, package names, logs, and developer-only text in English.
- Treat UI language and book language as separate concerns; a Russian book must not force the
  application interface to Russian.
- Check compact layouts, buttons, dialogs, notifications, alarm text, and PiP in both languages
  because translated strings do not share humanity's belief in fixed-width buttons.

Future decision:

- Decide later whether the application needs an explicit in-app language selector in addition to
  Android's per-app language settings.

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
