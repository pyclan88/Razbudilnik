# Codex Handoff

## Product
Single daily alarm that forces the user to stay active and read before dismissal.

## Agreed MVP Flow
- Alarm rings.
- Movement silences the alarm.
- After about 10 seconds without movement, volume gradually returns.
- User reads 5 pages.
- Each page unlocks after about 60 seconds.
- Alarm can be dismissed after the final page.

## Current State
- Single-module Compose Android project.
- Setup screen exists.
- Setup state is exposed through SetupViewModel and StateFlow.
- Enabled toggle works.
- Debug APK builds successfully.

## Next Step
Add a Material 3 time picker.
Store alarm time as hour and minute integers.
Format the time only when displaying it.

## Later
- Required-page-count selector.
- Test alarm flow.
- Movement detection.
- Foreground service and sound playback.
- Session persistence.
- Exact daily scheduling.
- Reboot recovery.

## Notes
- Keep commits small.
- Do not commit .idea files.