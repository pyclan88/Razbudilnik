# Deferred Bugs

This file records confirmed defects that the user explicitly chose not to fix immediately.

## BUG-001: Alarm Trigger Does Not Open The Reader Automatically

Status:

```text
Deferred
```

Observed behavior:

1. The alarm reaches its scheduled time and starts ringing.
2. The application remains on the setup screen.
3. The alarm notification appears.
4. The reader challenge opens only after the user taps the notification.

Expected behavior:

```text
The alarm trigger should bring AlarmActivity and its reader challenge to the foreground
automatically, without requiring a notification tap.
```

Known scope:

- Reproduced during physical-device smoke testing while working on branch
  `12-bundled-book-content`.
- The alarm sound and notification still work.
- Tapping the notification successfully opens the reader.
- The automatic foreground/full-screen activity launch is the failing part.

Decision:

```text
Do not interrupt PR 12 to fix this defect. Investigate it in a dedicated alarm-entry bug-fix step.
```

Future investigation should inspect:

- full-screen notification intent behavior;
- whether the app was foreground, background, or locked during each reproduction;
- `AlarmReceiver` and `AlarmRingingService` startup order;
- notification full-screen intent eligibility and device/OEM behavior;
- activity launch flags and current task state.
