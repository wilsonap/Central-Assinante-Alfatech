# 29 — Sync automático de faturas pós-login

## Problema

Faturas só entravam no Room ao abrir `/faturas` na Central.

## Solução

Após auth IXC + `refreshCentralCustomerData`:

1. `requestSilentGetFaturas` (hs_web.buscarFaturas ou XHR getFaturas)
2. Hook existente → `onInvoicesJson` → `InvoiceRepository` (upsert)
3. `InvoiceReminderScheduler`
4. Home nativa sem esperar o sync

## Proteção

`invoiceSyncHandled` por ciclo autenticado; reset no login IXC; `force` em `network_recovered`.

## Logs

`INVOICE_AUTO_SYNC`: trigger, authValid, syncStarted/Finished/Skipped/Failed, invoiceCount
