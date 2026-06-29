# Codex Handoff: Razbudilnik

## Current State

Project path on this PC: `C:\Users\Pyclan\AndroidStudioProjects\Razbudilnik`

Current branch: `4-alarm-notification`

Base:
- `origin/master`
- latest merged PR: `3-Wire setup flow to alarm scheduling (#3)`

Working tree status at handoff:
- `AGENT.md` is modified with a new safety rule:
  - do not touch, edit, stage, commit, move, delete, generate, or otherwise modify anything unless the user directly asks for that specific action.
- `CODEX_HANDOFF.md` is being updated for this branch.

## Important Project Rules

Read `AGENT.md` first.

Critical rules:
- Always answer in English.
- Always correct the user's English.
- Do not edit project files unless the user says `hellgate`.
- Even after `hellgate`, only perform the specific action the user directly requested.
- Git is read-only by default unless the user explicitly asks for a specific mutating Git action.
- Always show concrete code suggestions in chat.
- When showing code, clearly highlight new or edited code.
- PRs should be planned as logical commit-steps.
- At the end of each commit-step: review, tell what to test, provide commit message/comments, then state the next step.

## Product Goal

The app is an Android alarm clock app with a future reader feature.

Long-term MVP direction:
- one daily alarm
- alarm forces the user to wake up before dismissal
- future reader challenge: movement keeps sound muted while the user reads several pages

## Current PR Goal

Make fired alarms user-visible through Android's notification system instead of relying on direct activity launch from `AlarmReceiver`.

Why this PR exists:
- `AlarmManager` schedules when the alarm fires.
- Notification/full-screen intent is the more reliable way to get user-visible alarm UI when the app is in the background or the screen is locked.
- Directly starting `AlarmActivity` from a `BroadcastReceiver` is fragile on modern Android because of background activity launch restrictions.

This PR should include:
- notification channel for alarm alerts
- high-priority alarm notification
- full-screen intent for urgent alarm UI
- notification permission handling on Android 13+
- removal or reduction of direct `startActivity` from `AlarmReceiver`

This PR should not include:
- reader flow
- reboot restore
- snooze
- multiple alarms
- movement detection
- final ringtone/foreground-service hardening unless required by the notification implementation

## Planned Commit Steps

### Commit 1: Cleanup Imports

Intent:
- carry the tiny cleanup from the previous branch/start of this PR.

Likely file:
- `app/src/main/java/com/ruslanataev/razbudilnik/data/alarm/scheduler/AlarmSchedulerImpl.kt`

Outcome:
- remove unused imports.

Check:
- `.\gradlew.bat :app:assembleDebug`

Suggested commit message:
- `chore: delete unused imports`

### Commit 2: Add Alarm Notification Channel

Intent:
- prepare runtime notification support for fired alarms.

Likely files:
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/ruslanataev/razbudilnik/runtime/alarm/AlarmNotificationHelper.kt`

Outcome:
- add required notification/full-screen permissions where appropriate
- create a high-importance alarm notification channel
- keep channel creation in runtime/platform code, not domain

Check:
- build succeeds
- app launches without crashing

Suggested commit message:
- `Add alarm notification channel`

### Commit 3: Show Alarm Notification From Receiver

Intent:
- make fired alarms visible through notification infrastructure.

Likely files:
- `app/src/main/java/com/ruslanataev/razbudilnik/runtime/alarm/AlarmReceiver.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/runtime/alarm/AlarmNotificationHelper.kt`
- possibly `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/alarm/AlarmActivity.kt`

Outcome:
- `AlarmReceiver` posts an alarm notification when the alarm fires
- notification uses:
  - high priority
  - alarm category
  - full-screen intent
  - content intent to open `AlarmActivity`
- direct background activity launch is removed or kept only if explicitly justified for the MVP

Check:
- schedule alarm 1 minute ahead
- put app in background
- alarm fires and opens/alerts through notification path

Suggested commit message:
- `Show alarm notification when alarm fires`

### Commit 4: Request Notification Permission Before Enabling Alarm

Intent:
- handle Android 13+ notification permission before relying on notification delivery.

Likely files:
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/setup/SetupRoute.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/setup/SetupScreen.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/setup/states/SetupUiState.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/setup/viewmodel/SetupViewModel.kt`

Outcome:
- on Android 13+, user cannot enable the alarm without notification permission
- if permission is missing, app asks/explains before enabling
- if denied, switch remains disabled

Check:
- fresh install on Android 13/14
- enabling alarm requests notification permission
- grant enables normal alarm scheduling path
- deny keeps switch off

Suggested commit message:
- `Request notification permission before enabling alarm`

### Commit 5: Manual Device Hardening

Intent:
- fix small issues discovered during manual alarm notification testing.

Possible test cases:
- app foreground
- app background
- screen locked
- app swiped away
- exact-alarm access granted
- exact-alarm access revoked
- notification permission granted
- notification permission denied

Suggested commit message if fixes are needed:
- `Fix alarm notification edge cases`

## Current Architecture

Layers:
- `domain`
- `data`
- `presentation`
- `runtime` for non-UI Android entry points like `BroadcastReceiver`

Important rule:
- Activity/UI belongs to presentation.
- BroadcastReceiver/Service belongs to runtime/framework.
- data and presentation must not depend on each other.
- ViewModels depend on domain use cases/interactors.

Alarm runtime files currently relevant:
- `app/src/main/java/com/ruslanataev/razbudilnik/runtime/alarm/AlarmReceiver.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/data/alarm/scheduler/AlarmSchedulerImpl.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/alarm/AlarmActivity.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/alarm/AlarmScreen.kt`

Setup flow files currently relevant:
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/setup/SetupRoute.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/setup/SetupScreen.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/setup/states/SetupUiState.kt`
- `app/src/main/java/com/ruslanataev/razbudilnik/presentation/ui/setup/viewmodel/SetupViewModel.kt`

## Current Behavior

Setup screen can:
- show current setup state
- open time picker
- save selected time
- switch alarm on/off
- check exact-alarm access when enabling
- show exact-alarm access dialog when needed
- open Android Alarms & Reminders settings
- retry enabling after exact-alarm access is granted
- turn switch off if exact-alarm access is revoked

Alarm scheduling:
- uses `AlarmManager.setAlarmClock()`
- uses a broadcast `PendingIntent` to trigger `AlarmReceiver`
- uses a show-alarm `PendingIntent` from `AlarmClockInfo`

Known current weakness:
- `AlarmReceiver` directly starts `AlarmActivity`
- this should be replaced with notification/full-screen intent flow in this PR

## Device Testing

Current manual test device:
- Android 14 phone

Already tested in previous PR:
- setup exact-alarm access flow works
- enabling without access shows dialog
- granting access and returning enables alarm
- revoking access and returning turns switch off

Testing needed for this PR:
- alarm fires through notification path while app is in foreground
- alarm fires through notification path while app is in background
- alarm fires with screen locked
- behavior when notification permission is denied on Android 13+
- behavior when exact-alarm access is revoked

