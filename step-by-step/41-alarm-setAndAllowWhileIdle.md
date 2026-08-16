# 41 — AlarmManager setAndAllowWhileIdle (12:00)

## Substituição

Única chamada trocada em `InvoiceAlarmScheduler.scheduleKind`:

- **ANTES:** `AlarmManager.setWindow(RTC_WAKEUP, windowStart, windowLength, pi)`
- **DEPOIS:** `AlarmManager.setAndAllowWhileIdle(RTC_WAKEUP, triggerAt, pi)`

Trigger: **12:00** local no dia-alvo (`computeTriggerAt`).
Após 13:00 → não agenda. Entre 12–13 → `now + 3 min` se ainda < 13:00.

Sem `SCHEDULE_EXACT_ALARM` / exact / AlarmClock.
