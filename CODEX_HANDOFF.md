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
C:\Users\Pyclan\AndroidStudioProjects\Razbudilnik
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
5c0658c feat: launch reader challenge from alarm
22cdbd7 test: cover reader view model navigation
dcdfba2 docs: require full file addresses in code suggestions
030c558 feat: handle reader challenge completion
4da2669 feat: show reader challenge completion action
ceb6d64 chore: remove unused reader initial state
4006d15 feat: allow reader back navigation
b50105e docs: clarify code edit suggestion format
```

Before moving PCs, the user should normally run:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
git status
git add CODEX_HANDOFF.md
git commit -m "docs: update reader handoff"
git push
```

On the Asus laptop:

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

## Current Limitation

The reader state is not yet connected to alarm sound muting.

Right now:

- `ReaderUiState.shouldMuteAlarm` exists.
- The domain logic updates it based on finger movement.
- The UI uses it only as state.
- `AlarmRingingService` still only supports start and stop.
- Alarm sound keeps playing during the reader challenge until the challenge is finished.

This is the next real feature. The app is dramatically close to the point where it annoys the user
correctly. Society trembles.

## Next Commit-Step

Goal:

```text
Mute alarm sound while the reader interaction is valid, resume sound when interaction stops.
```

Recommended scope:

1. Add mute/resume actions to `AlarmRingingService`.
2. Add an `onAlarmMuteChanged: (Boolean) -> Unit` callback to `ReaderRoute`.
3. In `ReaderRoute`, call that callback from `LaunchedEffect(state.shouldMuteAlarm)`.
4. In `AlarmActivity`, send mute/resume intents to `AlarmRingingService`.

Suggested affected files:

-
`C:\Users\Pyclan\AndroidStudioProjects\Razbudilnik\app\src\main\java\com\ruslanataev\razbudilnik\runtime\alarm\AlarmRingingService.kt`
-
`C:\Users\Pyclan\AndroidStudioProjects\Razbudilnik\app\src\main\java\com\ruslanataev\razbudilnik\presentation\ui\reader\ReaderRoute.kt`
-
`C:\Users\Pyclan\AndroidStudioProjects\Razbudilnik\app\src\main\java\com\ruslanataev\razbudilnik\presentation\ui\alarm\AlarmActivity.kt`

Expected behavior after the next step:

- Alarm starts with sound.
- If the user presses and moves enough on the reader screen, the sound stops.
- If the user stops moving or lifts the finger, the sound resumes.
- The foreground notification stays alive while muted.
- Finishing the reader challenge still stops the service and removes the notification.

Suggested test:

1. Set an alarm one minute ahead.
2. Let it ring and open the reader.
3. Keep finger down and move meaningfully: sound should stop.
4. Stop moving while keeping finger down: sound should resume within about one second.
5. Lift finger: sound should resume.
6. Complete the reader challenge: service stops, activity closes, notification is removed.
7. Run:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

Suggested commit message after that step:

```text
feat: mute alarm during valid reader interaction
```

## Known Product Gaps

- Multiple alarms are not implemented.
- Real book/article storage is not implemented.
- User-imported books are not implemented.
- Reading content is hardcoded static text.
- Required reading time is temporarily `10.seconds` for smoke tests.
- Movement uses finger motion on screen, not physical walking.
- Alarm sound muting is the next step, not done yet.
- Reader UI is intentionally bare MVP.
- Snooze is not implemented.
- Notification still uses `android.R.drawable.ic_lock_idle_alarm` instead of an app-owned icon.
- Direct Boot behavior has only been tested on one physical Android 14 Tecno device.
