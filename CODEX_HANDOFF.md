# Codex Handoff: Razbudilnik

## Start Here

Read `AGENT.md` before doing anything.

Important rules:

- Respond only in English and always correct the user's English first.
- Do not modify anything unless the user explicitly requests that action and says `hellgate`.
- The user performs code changes, builds, tests, and Git operations by default.
- Treat Git as read-only unless the user explicitly requests a particular Git mutation.
- Always show concrete code suggestions with exact full file paths and explanatory code comments.
- Show the complete relevant edited section, not scattered fragments.
- Follow Clean Architecture boundaries.
- Work in one logical commit-step at a time.
- End every commit-step with review, testing, a commit message, and the next step.
- PR titles begin with the branch ordinal number.

## Repository State

Project path on this PC:

```text
C:\Users\Asus\AndroidStudioProjects\Razbudilnik
```

Current branch:

```text
8-reader-mvp
```

Base branch:

```text
master
```

Latest local commits at handoff time:

```text
6e6b11f fix: resume alarm when reader loses focus
543c760 feat: mute alarm during valid reader interaction
798981d docs: update reader handoff
5c0658c feat: launch reader challenge from alarm
22cdbd7 test: cover reader view model navigation
dcdfba2 docs: require full file addresses in code suggestions
030c558 feat: handle reader challenge completion
4da2669 feat: show reader challenge completion action
```

Before moving to another PC, the user should normally run:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
git status
git add CODEX_HANDOFF.md
git commit -m "docs: finalize reader MVP handoff"
git push
```

On another PC:

```powershell
git fetch origin
git switch 8-reader-mvp
git pull
```

## Android Configuration

- `minSdk = 34` (Android 14)
- `targetSdk = 36`
- `compileSdk = 36.1`
- Application ID: `com.ruslanataev.razbudilnik.alarm`

The alarm-flavored application ID is intentional. On Tecno Android 14, alarm/background behavior was
more reliable after the package name clearly identified the app as an alarm app.

## Current PR Goal

PR 8 builds the first MVP of the reader challenge and connects it to the alarm.

Expected MVP behavior:

1. The app can open a reader challenge manually from the setup screen.
2. The reader shows static pages from app code.
3. Every page has its own reading timer.
4. Reading time counts only while the finger is down and moving enough.
5. Slight stationary finger movement must not count as reading.
6. The user can go back to previous pages.
7. Going back does not reset completed progress.
8. The final page shows a finish action after its timer is complete.
9. When the alarm rings, `AlarmActivity` opens the reader instead of the old stop-only alarm screen.
10. Finishing the reader challenge stops the alarm.

## Implemented Reader Architecture

Domain:

- `app/src/main/java/com/ruslanataev/razbudilnik/domain/reader/models/ReaderChallenge.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/domain/reader/models/ReaderChallengeProgress.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/domain/reader/models/ReaderPage.kt`
-
`app/src/main/java/com/ruslanataev/razbudilnik/domain/reader/usecases/GetReaderChallengeUseCase.kt`
-
`app/src/main/java/com/ruslanataev/razbudilnik/domain/reader/usecases/CreateInitialReaderChallengeProgressUseCase.kt`
-
`app/src/main/java/com/ruslanataev/razbudilnik/domain/reader/usecases/UpdateReaderChallengeProgressUseCase.kt`

Presentation:

- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/reader/ReaderRoute.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/reader/ReaderScreen.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/reader/states/ReaderUiState.kt`
-
`app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/reader/mappers/ReaderChallengeToReaderUiStateMapper.kt`
-
`app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/reader/viewmodel/ReaderViewModel.kt`

Tests:

-
`app/src/test/java/com/ruslanataev/razbudilnik/domain/reader/usecases/UpdateReaderChallengeProgressUseCaseTest.kt`
-
`app/src/test/java/com/ruslanataev/razbudilnik/presentation/ui/reader/viewmodel/ReaderViewModelTest.kt`

## Current Reader Behavior

`ReaderRoute` owns pointer interaction state:

- `isFingerDown`
- `movementDistanceSinceLastTick`
- `requiredMovementDistancePx = 64.dp.toPx()`

Every second, it checks whether the accumulated movement distance crossed the threshold. If yes,
`ReaderViewModel.onReadingInteractionTick()` receives one second of active reading time.

`UpdateReaderChallengeProgressUseCase` currently counts time only when:

```text
isFingerDown && isFingerMoving
```

This is intentional. A pressed but stationary finger must not count.

The smoke-test reading time is currently:

```text
10.seconds
```

in `CreateInitialReaderChallengeProgressUseCase`. This is temporary for fast testing. The product
idea is closer to several minutes per page later.

## Current Alarm Connection

`AlarmActivity` is annotated with `@AndroidEntryPoint` because it hosts `ReaderRoute`, and
`ReaderRoute` uses `hiltViewModel()`.

Current alarm flow:

1. `AlarmReceiver` starts `AlarmRingingService`.
2. `AlarmReceiver` launches `AlarmActivity`.
3. `AlarmActivity` displays `ReaderRoute`.
4. `ReaderRoute` runs the reading challenge.
5. `onChallengeFinished` calls `AlarmActivity.stopAlarm()`.
6. `stopAlarm()` sends `AlarmRingingService.createStopIntent(this)` and finishes the activity.

Verified by the user:

- Alarm opens the reader.
- The activity no longer aborts after adding `@AndroidEntryPoint`.
- Finishing the challenge stops the alarm.

## Alarm Sound Interaction

The reader challenge now controls alarm playback:

1. Alarm sound starts with a smooth fade-in.
2. Valid finger movement smoothly mutes the sound.
3. Stopping movement starts a one-second grace period.
4. If movement resumes during that period, the pending resume is cancelled.
5. Otherwise, the sound smoothly fades back in.
6. Leaving `AlarmActivity` restores audible playback.
7. Finishing the challenge fades out and stops the service.

`AlarmRingingService` owns its fade coroutine scope and cancels it in `onDestroy()`.

Verified by the user:

- Valid movement mutes the alarm.
- Stopping movement restores the sound after the grace period.
- Rapid movement changes reverse the fade smoothly.
- Leaving the activity restores audible playback.
- Finishing the challenge stops the service and removes the notification.

## Next Action

PR 8 is feature-complete. Do not add more reader functionality to this branch.

Next steps:

1. Review the complete branch against `master`.
2. Prepare PR 8.
3. Merge after checks pass.
4. Plan challenge-navigation hardening in a new numbered branch.

## Known Product Gaps

- Multiple alarms are not implemented.
- Real book/article storage is not implemented.
- User-imported books are not implemented.
- Reading content is hardcoded static text.
- Required reading time is temporarily `10.seconds` for smoke tests.
- Movement uses finger motion on screen, not physical walking.
- Reader UI is intentionally bare MVP.
- Snooze is not implemented.
- Notification still uses `android.R.drawable.ic_lock_idle_alarm` instead of an app-owned icon.
- Direct Boot behavior has only been tested on one physical Android 14 Tecno device.
