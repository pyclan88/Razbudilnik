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
12-bundled-book-content
```

Base branch:

```text
master
```

Relevant commits:

```text
b4856fc docs: record reader-first product direction
c075a78 docs: record deferred alarm-entry bug
f9bda27 docs: require permission before tracking deferred bugs
44b63af feat: load bundled reader book from assets
1adba61 feat: add bundled public-domain reader text
c11f75a feat: add reader book data contract
9e87076 feat: add stable reader content models
d58896b 11. Recover an active alarm after process death (#11)
```

PR 11 is merged. PR 12 remains in progress.

The working source contains additional PR 12 changes after `44b63af`. They passed the reported
checks but still require final review and commit grouping.

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

## Deferred Alarm Entry Bug

`BUGS.md` contains `BUG-001`.

Observed:

- the alarm sound and notification start;
- the setup screen remains visible;
- the reader opens only after tapping the notification.

Decision:

```text
Do not interrupt PR 12. Fix automatic AlarmActivity entry in a dedicated bug-fix step.
```

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

## Next Session

On the ASUS PC:

```powershell
git fetch origin
git switch 12-bundled-book-content
git pull
```

Then:

1. Read all startup Markdown files.
2. Review the current PR 12 source without inspecting the staging state.
3. Commit asynchronous loading and wait for the user to confirm that commit before touching the
   pagination change.
4. Review and commit the pagination regression fix as its own commit.
5. Run:

   ```powershell
   .\gradlew.bat testDebugUnitTest assembleDebug
   ```

6. Perform one final physical-device smoke test of the corrected first page and alarm challenge.
7. Finish PR 12 before PiP, persistent progress, imported books, or reader design work.

## Planned Follow-Up

The currently recorded next product branch is:

```text
13-alarm-challenge-pip-return
```

Proposed title:

```text
13. Add Picture-in-Picture return to the active challenge
```

Its goal is to provide a compact return surface when an active challenge is minimized. PiP must not
stop or complete the alarm. See `PRODUCT_IDEAS.md` for the full product direction and open
monetization alternatives.

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
- Returning to a minimized active challenge still requires the launcher icon or notification.
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
