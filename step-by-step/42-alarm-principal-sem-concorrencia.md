# 42 — AlarmManager principal; app_start/sync sem notify

## Chamadas desativadas para notify/markFired

1. `MainActivity` → `InvoiceReminderChecker.run(app_start)`  
   → agora só loga `action=no_notify_alarm_managed` (sem `maybeNotify`).

2. `MainViewModel` → `InvoiceReminderChecker.run(invoice_sync)`  
   → **removido**; sync só agenda alarmes no Repository + log `action=schedule_only`.

## Principal / fallback

- Receiver → `trigger=alarm` + `markFiredCaller=InvoiceReminderReceiver`
- Worker → só após 13:00; `trigger=worker_fallback`
- DEBUG: `scheduleDebugAlarmInFiveMinutes` (+5 min, mesmo Receiver)
