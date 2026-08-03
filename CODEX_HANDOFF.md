# Codex Handoff: Razbudilnik

## Start Here

`AGENTS.md` is loaded automatically. Follow its startup sequence before doing project work.

Important rules:

- Correct the user's English first and respond in English unless another language is explicitly
  requested.
- Do not modify anything unless the user requests that exact action and says `hellgate`.
- The user performs project edits, builds, tests, and Git operations by default.
- Do not inspect or report the staging state unless explicitly asked.
- Show exact absolute file paths and explain unfamiliar code at the concept, runtime-flow, and
  API/syntax levels.
- Work one logical commit-step at a time.
- End code steps with review, verification, a commit message, and the next step.
- Do not start the next implementation step until the user confirms the previous commit or
  explicitly defers it.
- PR titles begin with the branch ordinal number.

Also read:

- `PRODUCT_IDEAS.md`
- `BUGS.md`
- `AI_CORRESPONDENCE.md`
- `THIRD_PARTY_CONTENT.md` when working with bundled content

## Repository State

Current-PC path:

```text
C:\Users\Pyclan\AndroidStudioProjects\Razbudilnik
```

Other-PC path:

```text
C:\Users\Asus\AndroidStudioProjects\Razbudilnik
```

Current branch:

```text
14-alarm-challenge-pip-return
```

Base branch:

```text
master
```

Relevant commits:

```text
79440aa feat: show reader challenge in picture-in-picture
efbcc1d feat: enable alarm challenge picture-in-picture
29bc047 docs: plan debug challenge bypass branch
b63447a 13. Open alarm challenge from foreground (#13)
8848a46 12. Add bundled book content to reader challenges (#12)
```

PR 13 is merged. PR 14 is implemented and verified on the physical Tecno Android 14 device. The
local branch contains unpushed commits and must be pushed before opening or updating its pull
request.

## Android Configuration

- `minSdk = 34` (Android 14)
- `targetSdk = 36`
- `compileSdk = 36.1`
- Application ID: `com.ruslanataev.razbudilnik.alarm`

The alarm-flavored application ID is intentional. Alarm and background behavior on the Tecno
Android 14 test device became reliable only after adopting that package identity.

The ringing service is a `mediaPlayback` foreground service and uses:

```text
android.permission.FOREGROUND_SERVICE
android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
android:foregroundServiceType="mediaPlayback"
```

## Current Product Behavior

The application currently has one enabled or disabled alarm with a selected time.

When the alarm fires:

1. `AlarmReceiver` starts `AlarmRingingService`.
2. The foreground service creates an ongoing full-screen alarm notification and plays alarm audio.
3. `AlarmActivity` hosts the reader challenge.
4. Reading time counts only while a finger is pressed and moves far enough.
5. Valid movement mutes the alarm.
6. Stopping valid movement restores sound after a one-second grace period.
7. Every challenge page has its own timer.
8. Completed pages may be revisited without repeating their timers.
9. Completing the final page stops the alarm and removes the notification.

The temporary reading requirement remains `10.seconds` per page for smoke testing.

Process-death recovery from PR 11 remains active:

- the ringing service refreshes an `AlarmManager` watchdog;
- process death leaves the last watchdog scheduled;
- the recovery receiver restarts the service with the alarm time;
- opening the launcher during an active session returns to the reader;
- normal challenge completion cancels recovery.

## PR 12 Goal

Proposed title:

```text
12. Add bundled public-domain reading content
```

Goal:

- replace the hardcoded English paragraphs with Leo Tolstoy's `Кавказский пленник`;
- introduce stable book and page identity;
- load content through a domain repository;
- keep file work off the main thread;
- preserve the existing alarm challenge behavior.

Out of scope:

- user-imported files;
- TXT file picker;
- FB2 or EPUB parsing;
- book selection;
- persisted reading progress;
- reader design polish;
- production standalone-reader navigation;
- debug challenge bypass.

## Completed PR 12 Commits

### Stable Content Models

Commit:

```text
9e87076 feat: add stable reader content models
```

Implemented:

- `ReaderBook` with stable ID, title, author, and complete pages;
- stable `ReaderPage.id`;
- challenge start index and required page count;
- challenge selection with `drop()` and `take()`;
- five selected pages by default;
- unit coverage for normal selection and reaching the book end.

Important distinction:

- `ReaderBook.pages` is the complete book.
- `ReaderChallenge.pages` is the subset selected for one alarm challenge.
- Future persistence should identify progress with both `bookId` and `pageId`.

### Reader Data Contract

Commit:

```text
c11f75a feat: add reader book data contract
```

Implemented:

- suspend `ReaderBookRepository`;
- reader DTOs;
- DTO-to-domain mappers;
- mapper tests.

### Bundled Story Asset

Commit:

```text
1adba61 feat: add bundled public-domain reader text
```

Implemented:

- `app/src/main/assets/reader_books/tolstoy_caucasian_prisoner.txt`;
- UTF-8 Russian story text;
- removal of RoyalLib wrapper material;
- source and public-domain documentation in `THIRD_PARTY_CONTENT.md`.

### Asset Repository

Commit:

```text
44b63af feat: load bundled reader book from assets
```

Implemented:

- `ReaderBookRepositoryImpl` reads through `AssetManager`;
- `withContext(Dispatchers.IO)` performs file work off the main thread;
- stable page IDs contain the book ID and padded page number;
- Hilt binds the implementation to the domain interface;
- `GetReaderChallengeUseCase` loads the bundled book by stable ID;
- repository tests cover loading, page IDs, page limits, and unknown IDs.

## Current Uncommitted PR 12 Work

### Asynchronous Reader Loading

Current source behavior:

- `ReaderViewModel` launches suspend challenge loading in `viewModelScope`;
- challenge and UI state remain nullable until loading finishes;
- public actions return early before content exists;
- `ReaderRoute` shows `CircularProgressIndicator` while state is null;
- reader timers and mute effects begin only after content is available;
- `MainDispatcherRule` replaces `Dispatchers.Main` in local tests;
- ViewModel tests use `runTest` and `advanceUntilIdle`.

Suggested commit message:

```text
feat: load reader challenge asynchronously
```

### Pagination Regression Fix

The first page originally contained only the chapter marker and two sentences because pagination
split each paragraph into fixed blocks before filling pages.

Current fix:

- words are appended directly to the current page;
- paragraph separators are preserved;
- the beginning of a long next paragraph may use space remaining on the current page;
- page text remains limited to 350 characters;
- the user wrote the regression test
  `uses remaining page space for start of next paragraph` under guidance.

Suggested commit message:

```text
fix: fill reader pages across paragraph boundaries
```

## Verification Reported This Session

Automated:

```text
ReaderViewModel tests: 5 passed
ReaderBookRepositoryImpl targeted tests: passed
testDebugUnitTest: passed
assembleDebug: passed repeatedly
latest assembleDebug after stopping a blocked Gradle daemon: passed in 14 seconds
```

Physical-device reader checks confirmed:

- the bundled Russian story loads;
- the challenge selects five pages;
- meaningful finger movement controls reading time and muting;
- tiny movement is insufficient;
- completed-page progress survives backward navigation;
- challenge completion still works.

The pagination regression is covered automatically. A final visual check of the corrected first
page is still desirable.

## PR 13: Foreground Alarm Entry

`BUGS.md` records `BUG-001` as fixed on branch `13-foreground-alarm-entry`.

Cause:

- Android intentionally uses a heads-up notification rather than forcing a full-screen intent over
  an unlocked foreground application.
- `MainActivity` previously checked active-session state only during creation and resume, so an
  already-resumed setup screen did not react to a newly triggered alarm.

Implementation:

- `AlarmRuntimeEvents` uses a process-local `SharedFlow` event with one extra buffer slot.
- `AlarmReceiver` starts `AlarmRingingService` and emits the alarm-start event.
- `MainActivity` collects while at least `Lifecycle.State.STARTED` and opens `AlarmActivity`.
- Persisted `AlarmSessionStore` remains the source of truth after process death; the event is not
  replayed.
- The ringing service initially posts the high-importance alarm notification required for
  full-screen entry and foreground execution.
- `AlarmActivity.onStart()` waits two seconds, then tells the service that the challenge UI is
  visible.
- The service replaces the alerting notification with a low-importance ongoing notification using
  the same notification ID.
- The delayed transition job is cancelled in `AlarmActivity.onStop()`.

Verification:

- Final `.\gradlew.bat testDebugUnitTest assembleDebug`: passed in 20 seconds.
- Final `assembleDebug` after the two-second delay: passed.
- Foreground setup screen automatically opens the challenge.
- Locked screen wakes and opens the challenge.
- Unlocked background behavior keeps the Android heads-up notification and opens the challenge
  when tapped.
- The alerting notification transitions to the quiet ongoing notification.
- Two seconds was selected on the physical Tecno Android 14 device after one second felt too abrupt.
- Challenge completion still stops sound and removes the foreground notification.

Residual risks accepted for PR 13:

- Foreground event delivery and notification replacement are verified on a physical device but do
  not yet have automated instrumentation coverage.
- The two-second notification transition has been evaluated only on the Tecno Android 14 device;
  animation timing and presentation may differ between OEMs.
- `AlarmRuntimeEvents` is intentionally process-local and does not replay old events. Persisted
  `AlarmSessionStore` state remains responsible for recovery after process death.
- Add broader supported-Android-version and OEM testing later, when a repeatable device or emulator
  test matrix exists. These are follow-up quality tasks, not blockers for this PR.

## PR 14: Picture-in-Picture Return

Goal:

- provide a compact return surface when an active challenge is minimized;
- keep alarm sound, notification, active-session state, and completion behavior independent of PiP;
- never let closing PiP stop or complete the alarm.

Completed commit-step 1, `efbcc1d`:

- `AlarmActivity` declares PiP support in `AndroidManifest.xml`;
- the required PiP `configChanges` prevent unnecessary activity recreation during resizing;
- `AlarmActivity` configures `PictureInPictureParams` during `onCreate()`;
- automatic PiP entry is enabled with `setAutoEnterEnabled(true)`;
- seamless resizing is disabled because the reader is non-video Compose content;
- the chosen portrait aspect ratio is `Rational(9, 16)`.

Physical Tecno Android 14 verification:

- pressing Home from the unlocked challenge enters PiP;
- tapping PiP returns to the full challenge;
- closing PiP removes only the floating activity surface;
- alarm sound and the ongoing notification remain active after PiP closes;
- tapping the notification restores the challenge.

Accepted platform behavior:

- merely opening the Recents overview does not enter PiP on the Tecno device;
- Android may reject PiP while the keyguard is locked, even though `AlarmActivity` can be displayed
  over the lock screen;
- the ongoing notification remains the return path for Recents and locked-keyguard cases.

Completed commit-step 2, `79440aa`:

- `AlarmActivity` owns one activity-scoped `ReaderViewModel`;
- the full `ReaderRoute` and compact PiP surface receive the same ViewModel state;
- `AlarmActivity` tracks PiP mode and switches between the complete reader and compact surface;
- entering PiP clears the active finger interaction so reading progress pauses and alarm sound
  returns;
- the compact PiP surface shows the current page, text excerpt, reading progress, alarm status, and
  return prompt;
- English and Russian PiP strings use Android resource localization;
- the PiP alarm icon is an application-owned vector drawable;
- expanding PiP restores the same page and progress;
- closing PiP removes only the floating activity surface and leaves the alarm and ongoing
  notification active.

Final verification:

```text
.\gradlew.bat testDebugUnitTest assembleDebug
BUILD SUCCESSFUL in 7s
```

Physical Tecno Android 14 verification:

- partial reading progress is preserved when entering and leaving PiP;
- challenge progress does not increase while PiP is visible;
- tapping PiP restores the full reader;
- valid finger movement continues to control progress after restoration;
- alarm sound resumes when interaction is cleared for PiP;
- closing PiP does not stop the alarm or remove its ongoing notification.

Out of scope for PR 14:

- finger trails;
- green, amber, and red movement feedback;
- warning edge pulses and status-message shake animation;
- broader reader or alarm-screen visual polish.

Those behaviors remain recorded in `PRODUCT_IDEAS.md` and should be implemented in a dedicated
reader-feedback branch rather than expanding the PiP return PR.

PR 14 is ready to push and open as a pull request. Do not begin branch 15 until PR 14 is merged.

## Deferred Debug Preview Idea

The user wants:

- a quick way to open the reader without scheduling an alarm;
- later, a debug-only challenge bypass for faster reader testing.

An unfinished experiment briefly added `ReaderPreviewActivity` under `src/main` and enabled
`buildConfig`. Both changes were removed before the session ended because they did not belong in
PR 12.

Important source-set explanation:

- `src/main` is included in debug and release builds;
- `src/debug` is included only in debug builds;
- a debug manifest supplements the main manifest through manifest merging;
- `src/main` cannot directly reference a class that exists only under `src/debug`.

Decision:

```text
Keep PR 12 focused on bundled content. Implement debug reader tools later in a dedicated small
branch. When implemented, keep both the preview activity and its manifest declaration under
src/debug so release builds omit them.
```

## IBM PC Android Studio Reminder

At the beginning of the next session on the IBM PC, remind the user to configure Android Studio
consistently with the ASUS PC:

1. Enable removing trailing spaces when files are saved.
2. Enable ensuring that every saved file ends with a line break.

This is currently an IDE configuration reminder. The repository does not yet contain an
`.editorconfig` that enforces these rules automatically.

## Next Session

Finish PR 14:

1. Commit this handoff update.
2. Push `14-alarm-challenge-pip-return`.
3. Open or update PR 14.
4. Merge PR 14 after its checks pass.
5. Synchronize `master`.
6. Create branch `15-debug-challenge-bypass`.

## Current PR And Planned Follow-Up

The current branch is:

```text
14-alarm-challenge-pip-return
```

Proposed title:

```text
14. Add Picture-in-Picture return to the active challenge
```

Its goal is to provide a compact return surface when an active challenge is minimized. PiP must not
stop or complete the alarm. Implementation and verification are complete; only push, pull-request,
and merge work remains.

The next dedicated developer-tooling branch after PR 14 merges is:

```text
15-debug-challenge-bypass
```

Proposed title:

```text
15. Add a debug-only challenge bypass
```

The bypass must exist only in debug builds and must invoke the real challenge-completion path.

Likely later sequence:

```text
Persist active reader progress
Import user TXT books
Add FB2/EPUB support
```

## Known Product Gaps

- Multiple alarms are not implemented.
- Bundled content currently consists of one public-domain Russian story.
- User-imported books are not implemented.
- Reader progress is not persisted.
- Recents and locked-keyguard exits still rely on the launcher or ongoing notification.
- PiP behavior has physical-device coverage but no automated instrumentation coverage.
- Required reading time remains `10.seconds` for smoke testing.
- Movement uses finger motion on screen, not physical walking.
- Reader UI is intentionally bare MVP.
- Snooze is not implemented.
- Post-dismissal Wake Up Check is not implemented.
- Backup re-ringing is not implemented.
- Alternative challenges are not implemented.
- Power-off and force-stop prevention are not portable Android guarantees.
- Notification still uses `android.R.drawable.ic_lock_idle_alarm`.
- Direct Boot and watchdog recovery have been tested only on one physical Android 14 Tecno device.
