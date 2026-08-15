# 34 — DEBUG: OneTimeWork 5 min (teste Worker fechado)

## Objetivo

Validar `InvoiceReminderWorker` com app fechado, sem alterar o periódico de 12h nem o checker.

## O que foi adicionado (somente DEBUG)

- `InvoiceReminderScheduler.scheduleDebugOneTimeTest()`  
  - `OneTimeWorkRequest` → mesmo `InvoiceReminderWorker`  
  - delay **5 minutos**  
  - unique name `invoice_reminder_debug_test`  
  - `ExistingWorkPolicy.REPLACE`  
  - log: `debugTestScheduled=true delayMinutes=5`

- Limpa **somente** a chave fired de teste:  
  `invoice_749428_due_date_2026-08-15`  
  via `clearFiredForDebug` (não apaga Room / prefs gerais).

## Onde agenda

1. `AlfatechApp.onCreate` (DEBUG)
2. Após check `app_start` na `MainActivity` (DEBUG) — evita markFired do check imediato
3. Após check `invoice_sync` no `MainViewModel` (DEBUG) — reinicia os 5 min após sync

## Como testar

1. Abrir o app (DEBUG)
2. Sincronizar fatura
3. Ver log `debugTestScheduled=true delayMinutes=5`
4. Fechar o app e **não reabrir**
5. Após ~5 min, esperar:
   - `workerStarted=true`
   - `wasFired=false`
   - `notifyAttempt=true`
   - `notificationPosted=true`
   - `markFired=true`
   - `workerFinished=true`

Remover este agendamento DEBUG após validação.
