# Codex Handoff: Razbudilnik

## Start Here

`AGENTS.md` is loaded automatically. Follow its startup sequence before doing project work.

Important collaboration rules:

- Correct the user's English first and respond in English unless another language is requested.
- Do not modify anything unless the user requests that exact action and says `hellgate`.
- The user performs project edits, builds, tests, and Git operations by default.
- Do not inspect or report staging unless explicitly asked.
- Work one logical commit-step at a time and stop at the commit gate.
- Show exact absolute file paths, integrated code, and explanations of unfamiliar behavior.
- For tests and Hilt, guide the user to write the code before showing a complete solution.
- Avoid repeating Gradle tasks when no relevant source has changed.

Also read:

- `PRODUCT_IDEAS.md`;
- `BUGS.md`;
- `AI_CORRESPONDENCE.md`;
- `THIRD_PARTY_CONTENT.md` only when working with external content.

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
18-import-txt-books
```

Base branch:

```text
master
```

Latest relevant commits:

```text
a26950b feat: add imported book text storage
c850f2f chore: remove completed pagination benchmark
54cb9e7 feat: add imported book metadata database
3b45b12 docs: update TXT import handoff and decisions
922169c feat: add TXT encoding detection and decoding
d16fb93 17. Add regular reader foundation (#17)
```

Branch 17 is merged. Branch 18 is active. Encoding, Room metadata storage, and canonical UTF-8 text
storage are complete; end-to-end import orchestration is next.

## Android Configuration

- `minSdk = 34` (Android 14)
- `targetSdk = 36`
- `compileSdk = 36.1`
- Application ID: `com.ruslanataev.razbudilnik.alarm`
- Build variants:
    - `debug`: debuggable and includes explicit cheat/testing controls;
    - `development`: debuggable, debug-signed, and excludes cheat controls;
    - `release`: publication build and excludes cheat controls.

All variants intentionally share the application ID. On the Tecno Android 14 device, reliable alarm
background behavior depends on the current package identity, so installing one variant replaces
another.

## Current Product Behavior

The app currently provides:

- one enabled or disabled alarm with a selected time;
- exact alarm scheduling, notification and full-screen alarm entry;
- foreground ringing audio and watchdog recovery after process death;
- Direct Boot rescheduling;
- a legacy alarm reading challenge;
- a standalone regular reader with canonical source text;
- adaptive, screen-measured pagination;
- two-finger forward and backward reader gestures;
- separate visible and confirmed reading offsets;
- debug-only reader, alarm, and challenge-completion controls;
- a no-cheat `development` build for realistic personal alarm testing.

The bundled content remains Leo Tolstoy's public-domain `Кавказский пленник`.

The alarm challenge still uses its older page model. Rebuilding the challenge around the ordinary
reader remains later work.

## Regular Reader Architecture

Accepted ownership:

- canonical book text is stable domain content;
- `RegularReaderViewModel` owns the current visible source offset and confirmed reading-progress
  offset;
- `RegularReaderScreen` owns measurements based on actual Compose constraints and text metrics;
- generated pages are layout-dependent views over canonical text, not persisted content identity;
- source-text offsets remain stable when orientation, dimensions, or system font scale changes;
- the current `ArrayDeque` backward history is temporary and process-local.

The reader can move backward after rotation by regenerating ranges from canonical text. A future
page-layout cache will replace repeated reconstruction and volatile history.

Reader layout benchmark on the physical Tecno device:

```text
Book: War and Peace benchmark text
Characters: 3,090,605
Generated pages: 3,571
Elapsed: approximately 65 seconds
```

The paginator measures bounded chunks instead of sending the complete unread remainder to
`TextMeasurer`. The benchmark confirms that complete synchronous pagination must not block import
or opening the reader. The temporary War and Peace asset and benchmark UI were removed after the
measurement; the result above remains the architectural evidence.

Accepted future generation behavior:

- open the book as soon as the first page is available;
- generate remaining page offsets incrementally;
- show `current / ???` until generation completes;
- allow sequential reading through generated content;
- disable direct navigation into content whose pages are not generated yet;
- cache offset ranges by book revision and layout-affecting configuration;
- retain only configuration variants the user actually uses.

These rules apply equally to bundled and imported books.

## Branch 18: Import TXT Books

Proposed PR title:

```text
18. Import TXT books
```

Goal:

- let a user select a TXT file from the device;
- decode its original bytes safely;
- store canonical UTF-8 text and stable metadata in app-private storage;
- expose imported books to the ordinary reader;
- establish the smallest usable local library path.

Out of scope:

- FB2 and EPUB;
- billing, premium entitlement, or free-import enforcement;
- cloud upload or accounts;
- alarm-challenge selection for imported books;
- complete background page-cache generation;
- reader visual polish.

### Completed Commit-Step: Encoding Detection And Decoding

Commit:

```text
922169c feat: add TXT encoding detection and decoding
```

Implemented:

- domain `ReaderTextEncoding` values for UTF-8, UTF-16 LE, UTF-16 BE, and Windows-1251;
- deterministic BOM detection;
- strict UTF-8 validation when no BOM exists;
- explicit decoding with BOM removal;
- no automatic Windows-1251 guess for ambiguous non-UTF-8 bytes;
- eight focused decoder tests written by the user under guidance.

Current decoding contract:

1. A BOM identifies UTF-8 or UTF-16 and its byte order.
2. Without a BOM, strictly valid UTF-8 is accepted.
3. Other bytes currently return `null` from detection.
4. Decoding uses an explicitly selected charset and creates a new Kotlin `String`.
5. The original file and original bytes remain unchanged.

Final UX direction:

- normal users should not have to choose an encoding during ordinary imports;
- later add confidence-based detection for common legacy encodings;
- keep manual encoding selection as a repair option when confidence is low or text looks wrong;
- never silently assume every non-UTF-8 Russian file is Windows-1251.

Verification:

```text
.\gradlew.bat testDebugUnitTest
BUILD SUCCESSFUL in 4s
```

### Completed Commit-Step: Imported Book Persistence Foundation

Commits:

```text
54cb9e7 feat: add imported book metadata database
a26950b feat: add imported book text storage
```

Implemented metadata storage:

- Room 3 database `reader.db`;
- `ImportedBookEntity` with stable ID, title, optional author, and import timestamp;
- DAO operations to upsert, load by ID, observe all imports, and delete by ID;
- Hilt providers for the singleton database and DAO;
- exported Room schema version 1 committed under `app/schemas`.

Implemented canonical text storage:

- app-private path `<filesDir>/reader_books/imported/<bookId>.txt`;
- explicit UTF-8 writes and reads on `Dispatchers.IO`;
- `null` for a missing text file;
- idempotent deletion that throws when an existing file cannot be removed;
- no absolute filesystem path persisted in Room because it is derived deterministically from the
  stable book ID.

Storage tests use a mocked Android `Context` only to supply `filesDir`. JUnit `TemporaryFolder`
provides a real isolated directory, so Java filesystem operations remain real. Five tests cover
save/read round trips, missing files, deletion, idempotent deletion, and raw UTF-8 bytes.

Verification:

```text
ImportedBookTextStorageTest: 5 tests passed
.\gradlew.bat assembleRelease
BUILD SUCCESSFUL in 44s
```

Maintenance completed during this step:

```text
c850f2f chore: remove completed pagination benchmark
```

The 5.5 MB War and Peace benchmark asset was never committed and is not required on the ASUS PC.
Its debug control and tracked benchmark implementation were removed together.

## Next Commit-Step

Coordinate Room metadata and canonical text storage behind one imported-book repository operation.

Required design work:

1. Define the domain contract and imported-book model needed by the import flow.
2. Generate a stable, filesystem-safe internal book ID; never derive it from a user-visible title.
3. Save canonical text and metadata as one repository-level operation.
4. Define cleanup behavior because Room and the filesystem cannot share one atomic transaction.
5. Load and delete an imported book through the same repository boundary.

Expected outcome:

- callers do not coordinate the DAO and text storage manually;
- a failed metadata write does not leave an orphaned canonical text file;
- imported content can be saved, loaded, observed, and deleted through one domain-facing boundary;
- original document access is still not retained;
- no Android file picker or library UI is included in this commit yet.

Likely remaining branch sequence:

```text
1. Coordinate canonical text and metadata through an imported-book repository
2. Add Android TXT file selection and import orchestration
3. Add minimal imported-book listing and selection
4. Connect the selected imported book to the ordinary reader
5. Complete branch verification and PR review
```

## Product Decisions To Preserve

- The product direction is reader-first, while wake-up reading remains the differentiating feature.
- Imported books are intended to remain readable in ordinary mode.
- Monetization has two preserved alternatives:
    - one free import followed by permanent import unlock;
    - free ordinary importing with premium required only for alarm-challenge use.
- Do not implement entitlement checks until the user chooses the monetization boundary.
- TXT is the first supported import format; FB2 and EPUB are separate later extensions.
- Imported source files are never modified.
- Canonical app-owned text is normalized to UTF-8.
- Android Auto Backup may later provide soft restoration of a free-import entitlement, but it is
  not strong anti-abuse enforcement.
- Do not introduce an account, backend, phone identifier, or device-recall system for the initial
  import implementation.

## Known Gaps

- User file selection and coordinated end-to-end imported-book persistence are not implemented yet.
- Reader progress is not persisted.
- The full page-layout cache is not implemented.
- The alarm challenge has not been rebuilt around the ordinary reader.
- Multiple alarms are not implemented.
- Reader UI remains intentionally minimal.
- Snooze, Wake Up Check, backup re-ringing, and alternative challenges are not implemented.
- Required challenge reading time remains `10.seconds` for smoke testing.
- Direct Boot and watchdog recovery have been verified only on one physical Android 14 Tecno
  device.
- PiP behavior has physical-device coverage but no automated instrumentation coverage.
- Notification still uses `android.R.drawable.ic_lock_idle_alarm`.
- Power-off and force-stop prevention are not portable Android guarantees.
