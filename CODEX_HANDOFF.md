# Codex Handoff: Razbudilnik

## Start Here

Read `AGENT.md` before doing anything.

Important rules:

- Respond only in English and always correct the user's English first unless the user explicitly
  asks for an explanation in another language.
- Do not modify anything unless the user explicitly requests that action and says `hellgate`.
- The user performs code changes, builds, tests, and Git operations by default.
- Treat Git as read-only unless the user explicitly requests a particular Git mutation.
- Do not inspect or report the Git staging state unless the user explicitly asks about it.
- Always show concrete code suggestions with exact full file paths.
- Teach new code at the concept, runtime-flow, and method/API/syntax levels.
- Put teaching comments above unfamiliar code.
- Use the knowledge profile in `AGENT.md`; do not infer understanding from a successful build.
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
11-alarm-kill-recovery
```

Base branch:

```text
master
```

Latest commits before this handoff edit:

```text
039a87b fix: return to active reader from launcher
70cdd08 docs: finalize alarm kill recovery handoff
bd8fde2 feat: maintain recovery watchdog while alarm rings
dae3909 feat: alarm recovery watchdog infrastructure
35bdef4 docs: add code learning protocol
71b2773 10. Protect active alarm volume (#10)
020c854 9. Harden alarm challenge navigation (#9)
61c10bc 8. Add reader challenge alarm flow (#8)
ef22d9a 7. Restore enabled alarms during Direct Boot (#7)
3726747 6. Reschedule alarm after it rings (#6)
```

Current status:

```text
PR 11 implementation, final branch review, automated checks, and physical-device testing are
complete. The branch is ready to push and open as a pull request.
```

Run `git status` for the exact working-tree and remote state.

## Android Configuration

- `minSdk = 34` (Android 14)
- `targetSdk = 36`
- `compileSdk = 36.1`
- Application ID: `com.ruslanataev.razbudilnik.alarm`

The alarm-flavored application ID is intentional. On the Tecno Android 14 device, alarm and
background behavior became reliable only after the package identity clearly represented an alarm
application.

The ringing service is a `mediaPlayback` foreground service. The manifest declares:

```text
android.permission.FOREGROUND_SERVICE
android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
android:foregroundServiceType="mediaPlayback"
```

`AlarmRingingService.startForeground()` also passes
`ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK`.

## Current Product Behavior

The application currently has one enabled/disabled alarm with a selected time.

When the alarm fires:

1. `AlarmReceiver` starts `AlarmRingingService`.
2. The foreground service creates an ongoing full-screen alarm notification and plays alarm audio.
3. `AlarmActivity` opens the reader challenge.
4. Reading time counts only while the finger is pressed and moves far enough.
5. Valid movement smoothly mutes the alarm.
6. Stopping movement restores sound after a one-second grace period.
7. Every page has its own reading timer.
8. The user may revisit completed pages without repeating their timers.
9. Completing the final page stops the alarm and removes the notification.

The temporary reading requirement remains `10.seconds` per page for smoke testing.

## Implemented Architecture

Main layer boundaries:

- `domain`: models, repository/scheduler interfaces, and use cases without Android framework logic.
- `data`: DataStore repositories and the Android alarm scheduler implementation.
- `presentation`: Compose screens, activities, UI state, mappers, and ViewModels.
- `runtime`: Android services and broadcast receivers.
- `di`: Hilt bindings.

Reader domain:

- `app/src/main/java/com/ruslanataev/razbudilnik/domain/reader/models/ReaderChallenge.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/domain/reader/models/ReaderChallengeProgress.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/domain/reader/models/ReaderPage.kt`
-
`app/src/main/java/com/ruslanataev/razbudilnik/domain/reader/usecases/GetReaderChallengeUseCase.kt`
-
`app/src/main/java/com/ruslanataev/razbudilnik/domain/reader/usecases/CreateInitialReaderChallengeProgressUseCase.kt`
-
`app/src/main/java/com/ruslanataev/razbudilnik/domain/reader/usecases/UpdateReaderChallengeProgressUseCase.kt`

Reader presentation:

- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/reader/ReaderRoute.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/reader/ReaderScreen.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/reader/states/ReaderUiState.kt`
-
`app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/reader/mappers/ReaderChallengeToReaderUiStateMapper.kt`
-
`app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/reader/viewmodel/ReaderViewModel.kt`

## Completed PR 10

PR 10 protects active alarm volume:

- Volume Down and Volume Mute hardware events are consumed while `AlarmActivity` is foreground.
- `AlarmVolumeController` captures and protects `AudioManager.STREAM_ALARM`.
- External alarm-volume reductions are detected every 250 milliseconds and reversed.
- The original alarm-stream volume is restored after successful completion.
- If the process dies first, the saved original volume is restored from `App.onCreate()`.

The physical Tecno device tests passed for hardware keys, system volume UI, Home/return,
notification behavior, challenge completion, process death, and original-volume restoration.

## PR 11 Status

Branch:

```text
11-alarm-kill-recovery
```

Proposed PR title:

```text
11. Recover active alarm after process death
```

Goal: automatically restart the active alarm runtime after the Tecno OEM kills the Razbudilnik
process when the app is removed from Recents.

### Problem Confirmed on Tecno

Swiping the active application from Recents killed:

- `AlarmActivity`
- `AlarmRingingService`
- the ongoing notification
- alarm sound

The package was not force-stopped. `dumpsys package` reported `stopped=false`, so Android was still
allowed to deliver a later `AlarmManager` alarm.

Approaches that previously failed:

- `singleTask`
- separate task affinity
- `stopWithTask=false`
- `Service.onTaskRemoved()`
- a private service process

The chosen solution is recovery after process death, not attempting to make a consumer Android
process impossible to kill.

### PR 11 Implementation

New runtime files:

- `app/src/main/java/com/ruslanataev/razbudilnik/runtime/alarm/recovery/AlarmRecoveryReceiver.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/runtime/alarm/recovery/AlarmRecoveryScheduler.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/runtime/alarm/session/ActiveAlarmSession.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/runtime/alarm/session/AlarmSessionStore.kt`

Edited runtime files:

- `app/src/main/java/com/ruslanataev/razbudilnik/runtime/alarm/AlarmRingingService.kt`
- `app/src/main/AndroidManifest.xml`

Edited presentation entry point:

- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/MainActivity.kt`

The watchdog uses:

```text
AlarmManager.ELAPSED_REALTIME_WAKEUP
setExactAndAllowWhileIdle()
PendingIntent.getBroadcast()
ACTION_RECOVER_ALARM
REQUEST_CODE_ALARM_RECOVERY = 3001
```

Watchdog timing:

```text
Recovery timeout: 5 seconds
Healthy-service refresh interval: 2 seconds
Expected recovery gap after process death: approximately 3-5 seconds plus OEM startup delay
```

Runtime flow:

1. `AlarmRingingService.startAlarm()` enters foreground mode.
2. `startRecoveryWatchdog()` immediately schedules recovery for five seconds later.
3. A coroutine postpones the same watchdog every two seconds.
4. Reusing the same receiver, action, request code, and PendingIntent type replaces one watchdog
   instead of creating many.
5. If the process dies, the coroutine disappears but Android retains the final watchdog deadline.
6. Android starts a new process and calls `AlarmRecoveryReceiver.onReceive()`.
7. The receiver starts `AlarmRingingService` with `ACTION_START`, hour, and minute.
8. The service recreates alarm audio, notification, and the full-screen reader flow.
9. Normal challenge completion calls `stopRecoveryWatchdog()` before the service fades out.

Active-session launcher flow:

1. `AlarmRingingService` synchronously saves the active alarm hour and minute in dedicated
   device-protected `SharedPreferences`.
2. Normal challenge completion clears the active session before the service stops.
3. Unexpected process death intentionally leaves the active session stored.
4. `MainActivity.onCreate()` and `onResume()` check the stored session before showing setup.
5. When a session exists, `MainActivity` starts `AlarmRingingService` with the saved alarm time.
6. It opens `AlarmActivity` with `FLAG_ACTIVITY_CLEAR_TOP`, reusing the existing reader when that
   activity is still alive.
7. It finishes `MainActivity`, preventing the setup screen from becoming an escape from an active
   reader challenge.

`AlarmRingingService` intentionally returns `START_NOT_STICKY`. Android does not generically restart
the service with a potentially null Intent; the watchdog performs a controlled restart with the
correct action and alarm time.

The watchdog is intentionally not cancelled from `AlarmRingingService.onDestroy()`. Abnormal
destruction must leave Android's scheduled recovery alive. Only legitimate challenge completion
cancels it.

### PR 11 Verification

Automated checks:

```text
testDebugUnitTest: passed
assembleDebug: passed
git diff --check: passed
```

Physical Android 14 Tecno verification:

- Active alarm recovered automatically after removing the app from Recents.
- Recovery occurred with the reduced 5-second timeout.
- Alarm sound, foreground notification, and reader flow returned.
- Normal challenge completion cancelled the watchdog.
- Waiting after normal completion did not restart the alarm.
- Opening Razbudilnik from its launcher icon during an active challenge returned to the existing
  reader with its current in-memory progress.
- After normal challenge completion, opening the launcher icon showed setup and did not restart the
  alarm.

### PR 11 Limitations

- Explicit Android Force stop cancels alarms and prevents background starts until the user launches
  the application again. On the next manual launch, the stored active session restores the reader
  and alarm runtime. The stopped-state restriction itself cannot be bypassed by a normal consumer
  application.
- Powering the device off cannot be prevented.
- Reader progress currently lives in `ReaderViewModel` memory and restarts from the beginning after
  process death.
- A severe main-thread stall longer than the watchdog safety margin could cause an unnecessary
  recovery start. The current application is simple, and the device test did not reproduce this.

## Teaching Protocol

`AGENT.md` now contains a persistent teaching system:

- Explain the feature concept.
- Explain the chronological Android runtime flow.
- Explain every new method, parameter, return value, side effect, constant, string, flag,
  annotation, and unfamiliar Kotlin/Android construct.
- Put teaching comments above the relevant suggested code.
- Ask short checkpoint questions.
- Record only explicitly confirmed understanding in the knowledge profile.

Confirmed concepts currently include:

- Android's `AlarmManager` schedule survives Razbudilnik process death.
- The ringing service must postpone the watchdog before its timeout.
- Android, not `AlarmActivity`, calls `AlarmRingingService.onStartCommand()` after a service-start
  request.

## Next Action

1. Commit this final handoff update if it is not included with the existing documentation change.
2. Push `11-alarm-kill-recovery`.
3. Open PR:

   ```text
   11. Recover active alarm after process death
   ```

4. Merge PR 11 after its checks pass.
5. Create:

   ```text
   12-bundled-book-content
   ```

## Planned PR 12

Proposed PR title:

```text
12. Add bundled public-domain reading content
```

Goal: replace the three hardcoded paragraphs with one real bundled public-domain book or story and
introduce stable content identity before reading-progress persistence.

Reason for this order:

- The current reader identifies pages only by list indexes.
- Persisting those indexes now would create disposable storage code.
- Stable book and page IDs should exist before progress is saved.

Planned scope:

1. Add a book model with stable `id`, `title`, `author`, and pages.
2. Add stable page IDs.
3. Add a domain repository abstraction for obtaining reader content.
4. Add a data implementation that reads one bundled public-domain text from app assets.
5. Wire the existing reader challenge to the bundled content.
6. Preserve the current movement, timer, alarm, and navigation behavior.
7. Add focused domain/ViewModel tests.

Out of scope for PR 12:

- User-imported files
- TXT file picker
- FB2 or EPUB parsing
- book-selection UI
- reading-progress persistence
- reader design polish

Likely sequence after PR 12:

```text
13. Persist active reader progress
14. Import user TXT books
15. Add FB2/EPUB support
```

## Known Product Gaps

- Multiple alarms are not implemented.
- Reading content is still hardcoded static text until PR 12.
- Real book/article storage is not implemented.
- User-imported books are not implemented.
- Reader progress is not persisted.
- Required reading time remains `10.seconds` for smoke testing.
- Movement uses finger motion on screen, not physical walking.
- Reader UI is intentionally bare MVP.
- Snooze is not implemented.
- Post-dismissal Wake Up Check is not implemented.
- Backup re-ringing is not implemented.
- QR/barcode, math, squat, photo, and other alternative challenges are not implemented.
- Power-off and force-stop prevention are not portable Android guarantees.
- Notification still uses `android.R.drawable.ic_lock_idle_alarm` instead of an app-owned icon.
- Direct Boot and watchdog recovery have been tested only on one physical Android 14 Tecno device.
