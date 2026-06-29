# Codex Handoff: Razbudilnik

## Current State

Project path on this PC: `C:\Users\Pyclan\AndroidStudioProjects\Razbudilnik`

Current branch: `3-alarm-setup-scheduling`

Latest branch commits:
- `d54a304 Align setup models and mappers across layers`
- `330e803 Handle setup alarm scheduling and exact alarm access flow`
- `59f412b Fix first-run setup defaults`
- `d11115a Handle setup scheduling failure and exact alarm access`
- base: `b971195 Merge pull request #2 from pyclan88/alarm-runtime-mvp`

Working tree status at handoff:
- tracked files are clean
- untracked local folders exist:
  - `.gradle-user-home/`
  - `.vscode/`

PR is intended to be opened as draft, not merged yet.

## Important Project Rules

Read `AGENT.md` first.

Critical rules:
- Always answer in English.
- Always correct the user's English.
- Do not edit project files unless user says `hellgate`.
- Git is read-only by default unless the user explicitly asks for a specific mutating Git action.
- Always show concrete code suggestions in chat.
- When suggesting code, include useful `// comments`.
- PRs should be planned as logical commit-steps.
- At the end of each commit-step: review, tell what to test, provide commit message/comments, then state the next step.

## Product Goal

The app is an Android alarm clock app with a future reader feature.

Current PR goal:
Wire the setup screen to control one exact alarm and handle exact-alarm access cleanly.

This PR includes:
- setup time selection
- switch-based scheduling/canceling
- exact-alarm access error state
- opening Android Alarms & Reminders settings
- rechecking access on resume
- setup model/mapping cleanup

This PR does not include:
- reliable alarm delivery via notification/full-screen intent
- reboot restore
- snooze
- multiple alarms
- reader flow
- final alarm-platform hardening

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

Model naming convention:
- data model: `AlarmSettingsDto`
- domain model: `AlarmSettings`
- presentation model: `AlarmSettingsVO`
- screen state: `SetupUiState`

Mapping direction:
- data -> domain
- domain -> presentation
- ViewModel fills `SetupUiState` from presentation model; no separate VO-to-UiState mapper for now.

Current files:
- `data/setup/models/AlarmSettingsDto.kt`
- `domain/setup/models/AlarmSettings.kt`
- `presentation/ui/setup/models/AlarmSettingsVO.kt`
- `data/setup/mappers/AlarmSettingsDtoToAlarmSettingsMapper.kt`
- `presentation/ui/setup/mappers/AlarmSettingsToAlarmSettingsVOMapper.kt`
- `presentation/ui/setup/states/SetupUiState.kt`
- `presentation/ui/setup/viewmodel/SetupViewModel.kt`

## Current Behavior

Setup screen can:
- show current setup state
- open time picker
- save selected time
- switch alarm on/off
- schedule alarm when enabled
- cancel alarm when disabled
- reschedule alarm when time changes while enabled
- show error when exact alarms are unavailable
- open Android exact-alarm settings
- recheck exact-alarm access after returning from settings

Exact alarm flow:
- Android 12 (S, API 31)+ uses special access: Alarms & Reminders.
- On Android 14 phone, enabling without access shows:
  `Exact alarms are unavailable on this device right now.`
- Button opens system settings.
- On resume, app checks `AlarmManager.canScheduleExactAlarms()`.

## Known Issue To Fix Next

`shouldEnableAfterPermissionGrant` currently lives inside `SetupUiState`.

The user thinks this is a hack, and I agree.

Why:
- it is not UI-rendered state
- it is internal ViewModel control state
- UI does not need to know about deferred retry intent

Next commit-step:
`Keep deferred enable intent inside setup view model`

Change:
- remove `shouldEnableAfterPermissionGrant` from `SetupUiState`
- add private field in `SetupViewModel`:
  `private var pendingEnableAfterPermissionGrant: Boolean = false`
- preserve current behavior:
  - user tries to enable alarm without exact-alarm access
  - app shows error and opens settings
  - after access is granted and app resumes, ViewModel retries scheduling
  - if scheduling succeeds, switch becomes enabled

Suggested commit message:
`Keep deferred enable intent inside setup view model`

Suggested body:
- remove permission retry flag from setup ui state
- keep pending enable intent as internal view model state
- preserve exact alarm access recovery behavior

## PR Draft Text

Title:
`Wire setup flow to alarm scheduling`

Description:
- wire setup state to schedule, cancel, and reschedule alarms
- show setup error when exact alarms are unavailable
- add exact-alarm access action from setup
- recheck exact-alarm access after returning from settings
- default fresh setup to current time with alarm disabled
- add setup DTO/domain/VO models and explicit mappers

Known follow-up before merge:
- improve `shouldEnableAfterPermissionGrant` by moving it out of `SetupUiState`

## Device Testing

Current manual test device:
- Android 14 phone

Tested / expected flow:
- fresh install opens with current time and switch off
- enabling without exact-alarm access shows error
- Grant exact alarm access opens Alarms & Reminders settings
- returning after grant clears error and should recover intended enable flow
- changing time while enabled should reschedule
- disabling should cancel scheduled alarm

Later cross-version verification needed:
- Android 12 (S, API 31)
- Android 13 (Tiramisu, API 33)
- Android 14 (Upside Down Cake, API 34)
- modern target SDK background alarm behavior in later PR

## Important Future PR

Do not solve this in the current PR unless explicitly asked.

Next future PR:
Reliable fired-alarm delivery.

Problem:
`AlarmReceiver` currently starts `AlarmActivity` directly from a `BroadcastReceiver`. This is risky on modern Android because of background activity launch restrictions.

Likely future direction:
- notification channel
- high-priority alarm notification
- full-screen intent
- notification permission handling on Android 13+
- keep ringtone/ringing screen behavior separate and test carefully
