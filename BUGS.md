# Deferred Bugs

This file records confirmed defects that the user explicitly chose not to fix immediately.

## BUG-001: Alarm Trigger Does Not Open The Reader Automatically

Status:

```text
Fixed
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

Historical decision:

```text
Do not interrupt PR 12 to fix this defect. Investigate it in a dedicated alarm-entry bug-fix step.
```

Cause:

- Android intentionally shows a heads-up notification instead of launching a full-screen intent
  over an unlocked foreground application.
- `MainActivity` checked `AlarmSessionStore` only during creation and resume. When it was already
  resumed on the setup screen, nothing notified it that `AlarmReceiver` had received a new alarm.

Resolution:

- Fixed on branch `13-foreground-alarm-entry`.
- `AlarmReceiver` now emits a process-local alarm-start event.
- A visible `MainActivity` collects the event and opens `AlarmActivity` immediately.
- Locked and background behavior continues to use the full-screen or tappable notification path.
- After `AlarmActivity` becomes visible, the alerting notification remains for two seconds and is
  then replaced by a low-importance ongoing foreground-service notification.

Verification:

- Foreground setup screen automatically opens the reader challenge.
- Locked screen wakes and opens `AlarmActivity`.
- An unlocked background application receives the expected heads-up notification, which opens the
  challenge when tapped.
- Challenge completion stops sound and removes the foreground notification.
