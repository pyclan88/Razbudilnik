# Codex Handoff: Razbudilnik

## Start Here

Read `AGENT.md` before doing anything.

Important rules:

- Respond only in English and always correct the user's English.
- Do not modify anything unless the user explicitly requests that action and says `hellgate`.
- The user performs code changes, builds, tests, and Git operations by default.
- Treat Git as read-only unless the user explicitly requests a particular Git mutation.
- Always show concrete code suggestions with exact paths and explanatory code comments.
- Follow strict Clean Architecture boundaries and the project's model naming rules.
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
7-direct-boot-alarm-restore
```

Base branch:

```text
master
```

Latest local commits:

```text
b2912c7 chore: clean up direct boot runtime code
0142feb feat: deliver direct boot alarms on the lock screen
f2130b4 feat: reschedule alarm from direct boot snapshot
bb63d7e feat: synchronize direct boot alarm snapshot
d1a0943 feat: persist direct boot alarm snapshot
a8e7de8 feat: add direct boot alarm snapshot contract
3726747 6. Reschedule alarm after it rings (#6)
```

Important remote state at handoff time:

- `origin/7-direct-boot-alarm-restore` points to `0142feb`.
- Local commit `b2912c7` has not been pushed yet.
- This handoff update also needs to be committed before changing PCs.

Before leaving this PC, the user should run:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
git status
git add codex_handoff.md
git commit -m "docs: update project handoff"
git push
```

On the other PC:

```powershell
git fetch origin
git switch 7-direct-boot-alarm-restore
git pull
```

## Android Configuration

- `minSdk = 34` (Android 14)
- `targetSdk = 36`
- `compileSdk = 36.1`
- Application ID: `com.ruslanataev.razbudilnik.alarm`

The alarm-related application ID is intentional. Tecno background and lock-screen behavior became
more reliable after the package name identified the app as an alarm application.

## Current PR Goal

PR 7 makes the enabled alarm survive a reboot and ring before the first device unlock.

Expected flow:

1. Saving alarm time or enabled state updates normal DataStore settings.
2. The same operation synchronizes a minimal snapshot into device-protected storage.
3. After reboot, `LOCKED_BOOT_COMPLETED` reaches `AlarmBootReceiver` while the user is locked.
4. `RescheduleDirectBootAlarmUseCase` reads the device-protected snapshot.
5. Android schedules the exact alarm without accessing credential-protected DataStore.
6. When the alarm fires before unlock, `AlarmReceiver` starts the ringing service and activity.
7. The receiver uses the snapshot to schedule the next daily occurrence while still locked.
8. After user unlock, normal DataStore settings become authoritative again.

## Implemented Architecture

Domain:

- `domain/alarm/api/DirectBootAlarmSnapshotRepository.kt`
- `domain/alarm/models/DirectBootAlarmSnapshot.kt`
- `domain/alarm/usecases/SynchronizeDirectBootAlarmSnapshotUseCase.kt`
- `domain/alarm/usecases/RescheduleDirectBootAlarmUseCase.kt`

Data:

- `data/alarm/models/DirectBootAlarmSnapshotDto.kt`
- `data/alarm/mappers/DirectBootAlarmSnapshotDtoToDirectBootAlarmSnapshotMapper.kt`
- `data/alarm/mappers/DirectBootAlarmSnapshotToDirectBootAlarmSnapshotDtoMapper.kt`
- `data/alarm/repository/DirectBootAlarmSnapshotRepositoryImpl.kt`

Runtime:

- `AlarmBootReceiver` handles both `LOCKED_BOOT_COMPLETED` and `BOOT_COMPLETED`.
- `AlarmReceiver` selects direct-boot or normal rescheduling through `UserManager.isUserUnlocked`.
- `AlarmActivity`, `AlarmRingingService`, `AlarmReceiver`, and `AlarmBootReceiver` are
  `directBootAware` in the manifest.
- `AlarmRingingService` contains a bounded screen-wake fallback for OEM firmware.

DI and synchronization:

- `AlarmModule` binds `DirectBootAlarmSnapshotRepositoryImpl` to the domain repository.
- `SaveAlarmTimeUseCase` synchronizes the direct-boot snapshot after saving time.
- `SaveAlarmEnabledUseCase` synchronizes the snapshot after saving enabled state.

Tests:

- `RescheduleDirectBootAlarmUseCaseTest` covers enabled, disabled, and missing snapshots.

## Device-Protected Storage

The snapshot uses synchronous `SharedPreferences.commit()` on `Dispatchers.IO` because the data must
be durable before a reboot. It is stored under the device-protected context:

```text
/data/user_de/0/com.ruslanataev.razbudilnik.alarm/shared_prefs/direct_boot_alarm_snapshot.xml
```

Example verification command:

```powershell
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
& $adb shell run-as com.ruslanataev.razbudilnik.alarm cat /data/user_de/0/com.ruslanataev.razbudilnik.alarm/shared_prefs/direct_boot_alarm_snapshot.xml
```

Expected values are `hour`, `minute`, and `enabled`.

## Verified Device Behavior

Physical-device Direct Boot test passed on the connected Tecno Android 14 device:

- Alarm was enabled for `21:22`.
- Device rebooted and remained in `RUNNING_LOCKED` state.
- `LOCKED_BOOT_COMPLETED` was sent at approximately `21:17:46`.
- Android started the app process for `AlarmBootReceiver`.
- The exact `21:22` alarm appeared as the next wake-from-idle alarm.
- Tecno froze the app process, then unfroze it at exactly `21:22:00` for the alarm.
- `AlarmRingingService` started successfully.
- `AlarmActivity` opened, drew, and received focus before first unlock.
- The screen turned on and the alarm sound played.
- The Stop action finished the activity and foreground service at `21:22:12`.
- Android scheduled the next occurrence for the following day at `21:22`.
- No application crash occurred.

## OEM Screen-Wake Finding

The modern APIs remain enabled:

- `Activity.setShowWhenLocked(true)`
- `Activity.setTurnScreenOn(true)`
- manifest `showWhenLocked="true"`
- manifest `turnScreenOn="true"`

However, a controlled Tecno test at `20:49` showed that the activity launched and rendered while
the device remained `Asleep`. Therefore, `setTurnScreenOn()` alone is insufficient on this firmware.

`AlarmRingingService` now uses a compatibility fallback only when `PowerManager.isInteractive` is
false:

- `SCREEN_BRIGHT_WAKE_LOCK`
- `ACQUIRE_CAUSES_WAKEUP`
- ten-second timeout
- explicit release when the alarm stops or the service is destroyed

The APIs are deprecated, but the fallback is intentional, bounded, documented, and validated. A
second test at `20:59` changed the device from `Asleep` to `Awake` and displayed `AlarmActivity`.

The manifest declares `android.permission.WAKE_LOCK`; it is a normal permission and has no runtime
dialog.

## Permissions and OEM Setup

The app currently depends on:

- exact alarm access through `USE_EXACT_ALARM`
- notification permission on Android 13+
- full-screen intent access
- foreground-service media playback permission
- Tecno's separate lock-screen overlay permission

On the tested Tecno device, the OS displayed:

```text
Razbudilnik requests to display an overlay on the lock screen. Allow?
```

This OEM permission had to be allowed. It is not a standard Android runtime-permission dialog.

## Review Status

The complete branch was reviewed against `master`:

- no blocking functional findings
- working tree was clean before this handoff edit
- branch contains small logical commits
- `git diff --check` passed
- strict layer boundaries are preserved
- manual end-to-end Direct Boot delivery passed

The cleanup commit `b2912c7` removed temporary suggestion markers, fixed wake-lock indentation, and
added the missing final newline. A final Gradle result after this cleanup was not reported yet, so
run `testDebugUnitTest assembleDebug` before opening the PR.

## PR Content

Title:

```text
7. Restore enabled alarms during Direct Boot
```

Description:

```markdown
## Summary

Restore an enabled alarm after reboot before the user unlocks the device.

## Changes

- Add a minimal direct-boot alarm snapshot stored in device-protected storage.
- Synchronize the snapshot when alarm time or enabled state changes.
- Handle `LOCKED_BOOT_COMPLETED` using the direct-boot snapshot.
- Continue using normal alarm settings after the user unlocks the device.
- Mark alarm runtime components as direct-boot aware.
- Reschedule the next alarm after one fires while the device is locked.
- Add a bounded screen-wake fallback for OEMs that ignore `turnScreenOn`.

## Testing

- Ran `testDebugUnitTest` and `assembleDebug`.
- Enabled an alarm and rebooted the device.
- Kept the device in the `RUNNING_LOCKED` state.
- Verified that `LOCKED_BOOT_COMPLETED` restored the exact alarm.
- Verified that the alarm fired at 21:22 before the first unlock.
- Verified that the sound started and the screen turned on.
- Verified that `AlarmActivity` appeared and the Stop action worked.
- Verified that the next day's alarm was scheduled.

## Limitations

- The app still supports one alarm configuration.
- The OEM wake fallback uses deprecated APIs intentionally and releases automatically.
- Direct Boot behavior has currently been tested on one physical Android 14 device.
```

Only claim the Gradle test line after the final command succeeds.

## Next Action

This PR is feature-complete. Do not add more functionality to branch 7.

Next steps:

1. Run the final Gradle tests and build.
2. Commit this handoff update.
3. Push the local cleanup and handoff commits.
4. Open PR 7 using the title and description above.
5. Merge only after the PR checks and review pass.
6. Start a new numbered branch for the next feature.

## Known Product Gaps

- Multiple alarms are not implemented.
- Reader challenge is not implemented.
- Movement requirement is not implemented.
- Snooze is not implemented.
- Notification still uses `android.R.drawable.ic_lock_idle_alarm` instead of an app-owned icon.
- Direct Boot has not yet been tested across every supported device/OEM combination.
