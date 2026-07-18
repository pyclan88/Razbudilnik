# Codex Handoff: Razbudilnik

## Start Here

Read `AGENT.md` before doing anything.

Important rules:

- Respond only in English and correct the user's English.
- Do not modify files unless the user says `hellgate`.
- Even after `hellgate`, modify only what the user directly requested.
- Git is read-only unless the user explicitly requests a particular Git mutation.
- Show concrete code suggestions and clearly highlight edited code.
- Explain suggested code clearly enough that the user understands what they are writing.
- Keep PR work split into small, testable commits.
- PR titles must include the branch's ordinal number. Example:
  `6. Reschedule alarm after it rings`.

## Repository State

Project on the original PC:

- `C:\Users\Pyclan\AndroidStudioProjects\Razbudilnik`

Current branch:

- `6-daily-alarm-rescheduling`

Latest commits:

- `d5ab67b test: cover next alarm trigger calculation`
- `e728e70 refactor: extract next alarm trigger calculation`
- `b93aae4 5. Restore enabled alarm after reboot (#5)`
- `e1d5698 Add reliable background and lock-screen alarm delivery (#4)`

Current working tree before this handoff update:

- Clean.

To continue on another PC:

```powershell
git fetch origin
git switch 6-daily-alarm-rescheduling
git pull
```

## Product Goal

Razbudilnik is an Android alarm clock with a future reader challenge.

Current MVP direction:

- one alarm
- reliable alarm delivery
- alarm rescheduling after reboot
- daily rescheduling after the alarm fires
- future reading challenge lasting approximately five minutes
- future movement requirement intended to keep the user awake

Do not add reader, movement, multiple-alarm, snooze, or Direct Boot behavior in this branch.

## Android Configuration

- `minSdk = 34` (Android 14)
- `targetSdk = 36`
- `compileSdk = 36.1`

Because `minSdk` is 34, current Android 14 permission APIs do not need older-version guards.

## Current PR Goal

Make the one enabled alarm behave as a daily alarm.

The app now supports this flow:

1. User enables the alarm.
2. Android schedules the next occurrence.
3. Alarm fires.
4. `AlarmReceiver` starts `AlarmRingingService` immediately.
5. `AlarmReceiver` calls `RescheduleEnabledAlarmUseCase`.
6. If the saved alarm is still enabled, Android schedules the next occurrence, usually tomorrow at
   the same time.

## Implemented Pieces

- Extracted `CalculateNextAlarmTriggerAtMillisUseCase`.
- Added Hilt `TimeModule` to provide `java.time.Clock`.
- Updated `AlarmSchedulerImpl` to delegate trigger-time calculation to the use case.
- Added unit tests for next trigger-time calculation.
- Updated `AlarmReceiver` to reschedule the enabled alarm after it fires.
- `AlarmReceiver` uses `goAsync()` so suspend rescheduling can finish safely.
- `AlarmReceiver` starts ringing before rescheduling, so alarm sound is not delayed by DataStore
  reads or scheduling work.

## Tests Already Passed

Gradle:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat testDebugUnitTest assembleDebug
```

Unit tests confirmed:

- future alarm time schedules today
- past alarm time schedules tomorrow
- alarm time equal to current time schedules tomorrow

Manual Tecno device smoke test:

- Alarm set for `17:53` fired and was stopped.
- After stopping it, ADB confirmed the next exact alarm was scheduled for
  `2026-07-19 17:53:00.000`.

Useful ADB check:

```powershell
adb shell dumpsys alarm | Select-String -Pattern "com.ruslanataev.razbudilnik.alarm" -Context 5,10
```

Expected proof in output:

```text
packageName =com.ruslanataev.razbudilnik.alarm
tag=*walarm*:com.ruslanataev.razbudilnik.action.TRIGGER_ALARM
type=RTC_WAKEUP origWhen=<tomorrow same alarm time>
Alarm clock:
  triggerTime=<tomorrow same alarm time>
```

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
- Multiple alarms are not implemented yet.
- Reader challenge is not implemented yet.
- Snooze is not implemented yet.
- The notification uses `android.R.drawable.ic_lock_idle_alarm`; replace it later with an app-owned
  monochrome notification icon.
