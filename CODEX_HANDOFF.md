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
10-alarm-volume-protection
```

Base branch:

```text
master
```

Latest commits at handoff time:

```text
9b995fc fix: restore alarm volume after process death
e650026 feat: enforce protected volume while alarm rings
4ec5a63 feat: apply volume protection while alarm rings
e8851b0 feat: add alarm volume protection controller
4ccdfd1 feat: block alarm silencing hardware keys
020c854 9. Harden alarm challenge navigation (#9)
61c10bc 8. Add reader challenge alarm flow (#8)
ef22d9a 7. Restore enabled alarms during Direct Boot (#7)
```

Current status:

```text
PR 10 implementation and device testing are complete.
The handoff update is the final documentation commit-step before pushing and opening the PR.
```

Run `git status` for the exact working-tree and remote state.

## Android Configuration

- `minSdk = 34` (Android 14)
- `targetSdk = 36`
- `compileSdk = 36.1`
- Application ID: `com.ruslanataev.razbudilnik.alarm`

The alarm-flavored application ID is intentional. On Tecno Android 14, alarm/background behavior was
more reliable after the package name clearly identified the app as an alarm app.

## Completed PR 8

PR 8 builds the first MVP of the reader challenge and connects it to the alarm.

Expected MVP behavior:

1. The reader shows static pages from app code.
2. Every page has its own reading timer.
3. Reading time counts only while the finger is down and moving enough.
4. Slight stationary finger movement must not count as reading.
5. The user can go back to previous pages.
6. Going back does not reset completed progress.
7. The final page shows a finish action after its timer is complete.
8. When the alarm rings, `AlarmActivity` opens the reader instead of the old stop-only alarm screen.
9. Finishing the reader challenge stops the alarm.

The temporary manual reader entry was removed in PR 9. The alarm is now the only production reader
entry point.

## Completed PR 9

PR 9 hardened challenge navigation:

1. `AlarmActivity` consumes Back and predictive-back navigation with Compose `BackHandler`.
2. Completing the reader challenge is the only in-app dismissal path.
3. Pressing Home is not blocked because Android reserves system navigation for the user.
4. Leaving the activity restores audible alarm playback.
5. The temporary `Open reader` button and its callback chain were removed.

The user verified Back, Home/return, lock/unlock, challenge completion, sound, and notification
cleanup on the physical Android 14 device. `testDebugUnitTest` and `assembleDebug` also passed.

## PR 10 Status

Branch:

```text
10-alarm-volume-protection
```

Proposed PR title:

```text
10. Protect active alarm volume
```

Goal: prevent a ringing alarm from being silenced by lowering or muting the alarm volume, while
restoring the user's original alarm-stream volume after successful challenge completion or the next
process start.

Completed logical commit-steps:

1. Consume Volume Down and Volume Mute hardware events in foreground `AlarmActivity`.
2. Add a runtime `AlarmVolumeController` that captures, enforces, and restores
   `AudioManager.STREAM_ALARM`.
3. Integrate the controller with `AlarmRingingService`.
4. Detect and reverse external alarm-volume reductions while the service is active.
5. Persist the original alarm volume in device-protected preferences.
6. Restore a stale original volume from `App.onCreate()` after process death.

Important platform conclusions:

- `Ringtone.volume` is only the ringtone player's local `0f..1f` multiplier. A low system alarm
  stream can still make `AUDIBLE_VOLUME = 1f` quiet.
- `AudioManager.STREAM_ALARM` represents the system alarm stream.
- If PR 10 changes the global alarm stream, it must capture and restore the original value.
- Foreground key interception alone is insufficient because the user can press Home and change
  volume through system UI.
- Do not override `ComponentActivity.dispatchKeyEvent()`: AndroidX restricts it to its library
  group. Use public `onKeyDown()` and `onKeyUp()` callbacks instead.
- Volume Up remains available.

`AlarmActivity` uses:

```text
onKeyDown() / onKeyUp()
KEYCODE_VOLUME_DOWN
KEYCODE_VOLUME_MUTE
```

`AlarmRingingService` also polls the alarm stream every 250 milliseconds and restores the protected
volume if it was lowered through system UI while the service is active.

The user verified on the physical Android 14 Tecno device:

- Volume Down and Volume Mute do not silence the alarm while `AlarmActivity` is visible.
- Lowering the alarm stream through the system volume panel is reversed.
- Pressing Home does not stop the ringing service or its volume protection.
- The ongoing alarm notification cannot be dismissed.
- Tapping the notification returns to the reader with its progress preserved.
- Completing the challenge stops the alarm and restores the original alarm volume.
- Killing the process leaves the protected volume in place, but reopening the app restores the
  saved original volume.
- Reopening the app after normal completion does not overwrite a later manual volume change.
- All tests for the final commit-step passed.

## Tecno Process-Kill Finding

On the Tecno device, swiping the app card from Recents while the challenge is active kills the
activity, foreground service, notification, and alarm sound.

Investigated approaches that did not solve this OEM behavior:

- `singleTask`, a separate task affinity, and `stopWithTask=false`
- `Service.onTaskRemoved()`
- running the ringing service in a private application process

After the swipe, `dumpsys package` reported `stopped=false`. The package was process-killed, not
force-stopped, so AlarmManager is still allowed to wake it later.

The realistic consumer-Android solution is automatic recovery, not making the process impossible to
kill. Device Owner / kiosk deployment could impose stronger restrictions, but it is intended for
fully managed dedicated devices and is not appropriate for this consumer alarm app.

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

1. Review and commit this handoff update as `docs: finalize alarm volume protection handoff`.
2. Push `10-alarm-volume-protection`.
3. Open PR `10. Protect active alarm volume`.
4. Merge PR 10 after its checks pass.
5. Create `11-alarm-kill-recovery`.

Planned scope for PR 11:

1. Add a distinct AlarmManager watchdog with its own PendingIntent identity and request code.
2. While the ringing service is healthy, keep postponing the watchdog trigger.
3. Cancel the watchdog after successful challenge completion.
4. If the OEM kills the process, let the watchdog restart the alarm runtime and challenge.
5. Verify recovery after a Recents swipe and verify that normal completion does not restart the
   alarm.

Do not add Wake Up Check, QR/barcode missions, backup alarms, or other unrelated anti-oversleep
features to PR 11.

## Known Product Gaps

- Multiple alarms are not implemented.
- Real book/article storage is not implemented.
- User-imported books are not implemented.
- Reading content is hardcoded static text.
- Required reading time is temporarily `10.seconds` for smoke tests.
- Movement uses finger motion on screen, not physical walking.
- Reader UI is intentionally bare MVP.
- Snooze is not implemented.
- A post-dismissal Wake Up Check is not implemented.
- Backup re-ringing is not implemented.
- QR/barcode, math, squat, photo, and other alternative challenges are not implemented.
- Power-off and force-stop prevention are not portable Android guarantees.
- Notification still uses `android.R.drawable.ic_lock_idle_alarm` instead of an app-owned icon.
- Direct Boot behavior has only been tested on one physical Android 14 Tecno device.
