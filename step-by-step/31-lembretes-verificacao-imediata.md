# 31 — Lembretes de fatura: verificação imediata

## Problema

O PeriodicWorkRequest era agendado, mas não rodava na abertura do app; fatura que vence hoje não gerava notificação.

## Correção

- `InvoiceReminderChecker`: lógica única (Room + prefs + dedupe + notify)
- `InvoiceReminderWorker` delega ao checker
- Verificação imediata em `AlfatechApp` (`trigger=app_start`)
- Verificação após sync OK em `MainViewModel` (`trigger=invoice_sync`)

## Dedupe

Mesma chave: `invoice_[idReceber]_[kind]_[dueDate]`
