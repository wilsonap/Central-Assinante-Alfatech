# 34b — DEBUG OneTime: agendar UMA vez só

## Problema

`REPLACE` reiniciava o delay a cada:
- AlfatechApp
- app_start
- invoice_sync (múltiplos)

## Correção

- Removido DEBUG de `AlfatechApp` e `MainActivity` (app_start).
- Único ponto: após 1ª sync bem-sucedida → `scheduleDebugOneTimeTestOnceAfterFirstSync`.
- Gate: `AtomicBoolean` + SharedPreferences `debugInvoiceTestScheduledThisSession`.

Para novo teste explícito: limpar o pref `invoice_reminder_debug_gate` / reinstalar, ou limpar o boolean.
