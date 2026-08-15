# 33 — Lembretes: fluxo robusto (Activity-only + gate + 12h)

## Auditoria (antes)

`immediate_check_started trigger=app_start` era disparado em:

- **`AlfatechApp.onCreate()`** → `InvoiceReminderChecker.run(..., trigger = "app_start")`

Problema: `Application.onCreate()` também roda quando o JobScheduler/WorkManager sobe o processo sem UI → check imediato competia com o Worker e podia marcar dedupe sem notificação visível.

## Correções

| Item | Mudança |
|------|---------|
| 1 | Removido check de `AlfatechApp`; movido para `MainActivity.onCreate` (após `setContent`) |
| 2 | `InvoiceReminderChecker` permanece única lógica (MainActivity + Worker + invoice_sync) |
| 3 | Gate: POST_NOTIFICATIONS, `areNotificationsEnabled`, canal existe, `importance != NONE` |
| 4 | Canal novas instalações: `IMPORTANCE_HIGH`; builder `PRIORITY_HIGH` + `CATEGORY_REMINDER`; se canal silenciado → Toast + abrir settings (não recria) |
| 5 | Ordem: wasFired → gate → notifyAttempt → notify → posted → markFired |
| 6 | `Mutex` serializa execuções concorrentes |
| 7 | Periódico **12h**; nome `invoice_due_reminder_periodic_12h` + `KEEP`; cancela legado diário |
| 8 | `InvoiceReminderDiagnostics` (SharedPreferences sem PII) |
| 9 | Logs pedidos mantidos |

## Arquivos

- `AlfatechApp.kt` — só canais + schedule (sem check)
- `MainActivity.kt` — `app_start` após Activity criada
- `InvoiceReminderChecker.kt` — mutex + gate + logs
- `InvoiceReminderDiagnostics.kt` — diagnóstico persistente
- `InvoiceReminderScheduler.kt` — 12h + KEEP
- `NotificationChannels.kt` — canal faturas HIGH (criação)

## Fluxos esperados

- Usuário abre app → `trigger=app_start` na MainActivity
- Só Worker → `workerStarted=true`, **sem** `app_start`
- Notif OK → `notificationPosted=true` → `markFired=true`
- Notif bloqueada → `notificationPosted=false` → **sem** markFired
