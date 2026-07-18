# Codex Handoff: Razbudilnik

## Start Here

Read `AGENT.md` before doing anything.

Important rules:

- Respond only in English and correct the user's English.
- Do not modify files unless the user says `hellgate`.
- Even after `hellgate`, modify only what the user directly requested.
- Git is read-only unless the user explicitly requests a particular Git mutation.
- Show concrete code suggestions and clearly highlight edited code.
- Keep PR work split into small, testable commits.

## Repository State

Project on the original PC:

- `C:\Users\Pyclan\AndroidStudioProjects\Razbudilnik`

Current branch:

- `5-alarm-reboot-rescheduling`

Latest commits:

- `851b636 test: cover enabled alarm rescheduling`
- `5314b40 feat: restore enabled alarm after reboot`
- `47e7857 feat: add enabled alarm rescheduling use case`
- `e1d5698 Add reliable background and lock-screen alarm delivery (#4)`

Current working tree:

- Clean before this handoff update.

To continue on another PC:

```powershell
git fetch origin
git switch 5-alarm-reboot-rescheduling
git pull
```

## Product Goal

Razbudilnik is an Android alarm clock with a future reader challenge.

Current MVP direction:

- one alarm
- reliable alarm delivery
- alarm rescheduling after reboot
- future reading challenge lasting approximately five minutes
- future movement requirement intended to keep the user awake

Do not add reader, movement, multiple-alarm, snooze, or daily repeat behavior in this branch.

## Android Configuration

- `minSdk = 34` (Android 14)
- `targetSdk = 36`
- `compileSdk = 36.1`

Because `minSdk` is 34, current Android 14 permission APIs do not need older-version guards.

## Current PR Goal

Restore the enabled alarm after device reboot.

The app supports normal post-unlock reboot recovery:

1. User enables the alarm.
2. Device reboots.
3. Android sends `BOOT_COMPLETED` after the first unlock.
4. `AlarmBootReceiver` runs.
5. `RescheduleEnabledAlarmUseCase` reads saved alarm settings.
6. If the alarm is enabled, it schedules the saved time again.

This branch does not implement Direct Boot / before-unlock alarm recovery.

## Implemented Pieces

- `RescheduleEnabledAlarmUseCase`
- `AlarmBootReceiver`
- `RECEIVE_BOOT_COMPLETED` permission
- Manifest receiver registration for `BOOT_COMPLETED`
- Unit tests for enabled alarm rescheduling

## Tests Already Passed

Gradle:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebugAndroidTest
```

Manual Tecno device smoke test:

- Enabled alarm survived reboot.
- Alarm was rescheduled after boot/unlock.
- Alarm fired successfully after reboot.

## Permission Model

Exact alarm permission:

- Manifest declares `android.permission.USE_EXACT_ALARM`.
- It is automatically granted for qualifying alarm-clock use cases.
- Razbudilnik does not appear in the user-controlled **Alarms & Reminders** list.
- `AlarmSchedulerImpl` still checks `alarmManager.canScheduleExactAlarms()` defensively.

Notification permission:

- Manifest declares `android.permission.POST_NOTIFICATIONS`.
- Android 13+ disables notifications by default for fresh installations.
- It is requested contextually when the user enables the alarm.
- If granted, the app continues enabling and scheduling.
- If denied, the alarm stays disabled.

Full-screen intent:

- Manifest declares `android.permission.USE_FULL_SCREEN_INTENT`.
- Device/OEM policy may still control whether a full-screen activity is shown.

## Reliable Delivery Notes

The app package name is intentionally:

```text
com.ruslanataev.razbudilnik.alarm
```

Do not change it casually. On Tecno, background/lock-screen alarm delivery started working reliably
only after the package name included an alarm-related word.

## Architecture Boundaries

- `presentation`: Activities, Compose screens, routes, ViewModels
- `domain`: use cases and framework-free abstractions
- `data`: scheduler and repository implementations
- `runtime`: Android entry points such as `BroadcastReceiver` and `Service`
- `di`: dependency wiring

Keep Android permission APIs in presentation/runtime code. Do not move `Context`, `Manifest`,
`BroadcastReceiver`, or Activity Result APIs into the ViewModel or domain layer.

## Known Gaps

- Direct Boot / before-unlock reboot recovery is not implemented.
- The current alarm is one-shot; daily rescheduling is not implemented yet.
- The reader challenge is not implemented yet.
- Multiple alarms are not implemented yet.
- The notification uses `android.R.drawable.ic_lock_idle_alarm`; replace it later with an app-owned
  monochrome notification icon.
