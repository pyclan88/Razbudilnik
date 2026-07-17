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

- `4-alarm-notification`

Latest commit:

- `c79474f Use automatic exact alarm permission flow`

Current uncommitted change:

- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/setup/SetupRoute.kt`
- Adds the Android runtime `POST_NOTIFICATIONS` request when the user tries to enable the alarm.

To continue on another PC:

```powershell
git fetch origin
git switch 4-alarm-notification
git pull
```

The uncommitted `SetupRoute.kt` change must be committed and pushed on the original PC before it can
be pulled elsewhere.

## Product Goal

Razbudilnik is an Android alarm clock with a future reader challenge.

Current MVP direction:

- one alarm
- reliable alarm delivery
- future reading challenge lasting approximately five minutes
- future movement requirement intended to keep the user awake

Do not add reader, movement, multiple-alarm, snooze, or reboot behavior in the current commit.

## Android Configuration

- `minSdk = 34` (Android 14)
- `targetSdk = 36`
- `compileSdk = 36.1`

Because `minSdk` is 34, current Android 14 permission APIs do not need older-version guards.

## Current PR Goal

Deliver fired alarms through a high-priority notification and full-screen intent so alarms remain
visible when the app is backgrounded or the screen is locked.

Implemented pieces:

- alarm notification channel
- high-priority alarm notification
- full-screen/content intent opening `AlarmActivity`
- `AlarmReceiver` posts the notification
- exact scheduling uses `AlarmManager.setAlarmClock()`
- manifest declares `USE_EXACT_ALARM`
- obsolete `SCHEDULE_EXACT_ALARM` settings flow has been removed
- project now supports Android 14+

Still being completed:

- request notification permission before enabling the alarm
- test notification and full-screen behavior in all relevant device states

## Permission Model

Exact alarm permission:

- Manifest declares `android.permission.USE_EXACT_ALARM`.
- It is automatically granted for qualifying alarm-clock use cases.
- Razbudilnik therefore does not appear in the user-controlled **Alarms & Reminders** list.
- `AlarmSchedulerImpl` still checks `alarmManager.canScheduleExactAlarms()` defensively.
- The user intentionally kept `@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)`
  despite the manifest using `USE_EXACT_ALARM`; do not change it without discussing it first.

Notification permission:

- Manifest declares `android.permission.POST_NOTIFICATIONS`.
- Android 13+ disables notifications by default for fresh installations.
- It cannot be granted automatically to a normal third-party app.
- It should be requested contextually when the user tries to enable the alarm.
- If granted, continue enabling and scheduling the alarm.
- If denied, leave the switch disabled.

Full-screen intent:

- Manifest declares `android.permission.USE_FULL_SCREEN_INTENT`.
- Device/OEM policy may still control whether a full-screen activity is shown.

These permissions are independent. `USE_EXACT_ALARM` does not grant `POST_NOTIFICATIONS`.

## Uncommitted Notification Permission Change

`SetupRoute.kt` now:

- gets `LocalContext.current`
- registers `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`
- checks `POST_NOTIFICATIONS` before enabling
- launches Android's system permission dialog when permission is missing
- calls `viewModel.onEnabledChange(true)` after the user grants permission
- does nothing after denial, leaving the switch off

No ViewModel pending flag is needed. Unlike the old exact-alarm settings flow, the Activity Result
API returns the permission result directly through its callback.

Before committing:

- add the missing newline at the end of `SetupRoute.kt`
- review `git diff --check`
- test the permission flow on the Android 14 phone

Suggested commit message:
- `Request notification permission before enabling alarm`

Suggested commit body:

```text
Request POST_NOTIFICATIONS when the user enables the alarm.
Continue scheduling after permission is granted.
Keep the alarm disabled when permission is denied.
```

## Confirmed Device Diagnosis

Physical test device:

- Tecno Pova Neo 3
- Android 14
- ADB serial used on the original PC: `1006925392001182`

ADB confirmed:

- `USE_EXACT_ALARM: granted=true`
- `USE_FULL_SCREEN_INTENT: granted=true`
- `POST_NOTIFICATIONS: granted=false`
- Android scheduled the `RTC_WAKEUP` alarm successfully
- `AlarmReceiver` was triggered

The previous silent alarm was caused by `AlarmNotificationHelper` returning when notification
permission was denied. Exact alarm scheduling was working.

## Required Tests For Current Commit

Fresh permission flow:

1. Revoke notification permission or reinstall the app.
2. Set the alarm two minutes ahead.
3. Enable the switch.
4. Confirm Android shows the notification permission dialog.
5. Grant permission.
6. Confirm the switch becomes enabled and the alarm is scheduled.

Denial flow:

1. Revoke notification permission.
2. Try to enable the alarm.
3. Deny permission.
4. Confirm the switch remains disabled.

Alarm delivery smoke test:

1. Grant notification permission.
2. Schedule the alarm two minutes ahead.
3. Press Home and lock the phone.
4. Confirm the screen turns on, `AlarmActivity` appears, sound loops, and Stop ends the sound.
5. Repeat with the phone unlocked.
6. Repeat after swiping the app from Recents.

Do not use Android **Force stop** as a normal alarm test. Force-stopped apps are intentionally
blocked until launched again.

## Known Gaps

- If notification permission is revoked while an alarm is already enabled, app state is not yet
  reconciled on resume.
- Permanent notification denial has no explanatory dialog or app-settings fallback yet.
- Alarm sound currently starts inside `AlarmActivity`. If Android shows only a heads-up notification
  and does not launch the full-screen activity, looping sound may not start.
- Alarm rescheduling after device reboot is not implemented.
- The current alarm is one-shot; daily rescheduling is not implemented yet.
- The notification uses `android.R.drawable.ic_lock_idle_alarm`; replace it later with an app-owned
  monochrome notification icon.
- Gradle compilation was not completed in Codex because downloading the Java 21 toolchain failed.
  Build from Android Studio or retry when the toolchain/network is available.

## Architecture Boundaries

- `presentation`: Activities, Compose screens, routes, ViewModels
- `domain`: use cases and framework-free abstractions
- `data`: scheduler and repository implementations
- `runtime`: Android entry points such as `BroadcastReceiver` and future services
- `di`: dependency wiring

Keep Android permission APIs in presentation/runtime code. Do not move `Context`, `Manifest`, or
Activity Result APIs into the ViewModel or domain layer.
