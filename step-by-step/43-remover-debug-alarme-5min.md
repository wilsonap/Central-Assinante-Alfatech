# 43 — Remoção do DEBUG AlarmManager +5 minutos

## Objetivo

Remover **apenas** o mecanismo temporário de teste (`+5 min` / URI `/debug5`) após validação em device real. Fluxo de produção intacto.

## Removido

| Item | Onde |
|------|------|
| `scheduleDebugAlarmInFiveMinutes` | `InvoiceAlarmScheduler.kt` |
| `debug_clear_fired` / limpeza de `fired_*` | mesma função |
| `debugTestScheduled` / `delayMinutes=5` | logs da função |
| URI `.../debug5` + requestCode `+77_777` | PendingIntent exclusivo do teste |
| Chamada DEBUG em `app_start` | `MainActivity.kt` |

## Mantido (produção)

- `AlarmManager.setAndAllowWhileIdle` → `InvoiceReminderReceiver` → Room → notify → histórico → `markFired`
- Alarmes 12:00 `day_before` / `due_date`
- `app_start` = `no_notify_alarm_managed`
- `invoice_sync` = `schedule_only`
- WorkManager fallback só após 13:00
- Dedupe, histórico do sino, vibração, `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`

## Validação

```
:app:assembleDebug
:app:testDebugUnitTest --tests "com.example.invoice.*"
→ BUILD SUCCESSFUL
```

## Arquivos alterados

- `app/src/main/java/com/example/MainActivity.kt` — remove agendamento DEBUG
- `app/src/main/java/com/example/invoice/InvoiceAlarmScheduler.kt` — remove função de teste
