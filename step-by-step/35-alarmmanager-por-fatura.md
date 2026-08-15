# 35 — AlarmManager por fatura (lembretes)

## Removido (DEBUG / obsoleto)

- `clearFiredForDebug`
- `scheduleDebugOneTimeTestOnceAfterFirstSync`
- `invoice_reminder_debug_test` / `INVOICE_REMINDER_DEBUG_TEST`
- `debugInvoiceTestScheduledThisSession` / `invoice_reminder_debug_gate`
- OneTimeWorkRequest de 5 minutos
- Chamadas DEBUG em Application / app_start / invoice_sync

## Novo mecanismo principal

- `InvoiceAlarmScheduler` — `setWindow` 11:00–13:00 (2h), sem exact alarm
- `InvoiceReminderReceiver` — valida Room antes de notificar
- `InvoiceAlarmBootReceiver` — `BOOT_COMPLETED` + `MY_PACKAGE_REPLACED`
- `InvoiceAlarmTiming` — janelas e status terminal
- Sync em `InvoiceRepository` agenda/cancela/reagenda por fatura

## Fallback

- WorkManager `invoice_due_reminder_periodic_12h` + `InvoiceReminderChecker` (mesma dedupe)

## Permissão

- `RECEIVE_BOOT_COMPLETED` apenas (sem SCHEDULE_EXACT_ALARM)
