# 27 — Faturas locais e lembretes de vencimento

## Objetivo

Persistir faturas reais do IXC (`getFaturas`) no Room e avisar 1 dia antes / no dia do vencimento, sem alterar login, FCM, comprovantes ou sessão WebView.

## Estratégia de captura

Hook passivo em `XMLHttpRequest`/`fetch` na WebView autenticada. Não altera request/response. Filtra `faturas.php` + `getFaturas` e envia JSON para `AndroidBridge.onInvoicesJson`.

## Arquivos criados

| Arquivo | Função |
|---------|--------|
| `InvoiceEntity.kt` | Entidade Room `invoices` |
| `InvoiceDao.kt` | Upsert / observe / consulta |
| `InvoiceMigrations.kt` | `MIGRATION_3_4` |
| `InvoiceParser.kt` | Parser JSON → entidades (centavos) |
| `InvoiceBillingType.kt` | STORE / BANK / UNKNOWN |
| `InvoiceRepository.kt` | Sync + Flow |
| `InvoiceReminderPrefs.kt` | Preferências + dedupe de lembretes |
| `InvoiceReminderScheduler.kt` | Periodic WorkManager |
| `InvoiceReminderWorker.kt` | Dispara notificações |
| `InvoiceMigration3to4Test.kt` | Teste migração 3→4 |
| `InvoiceParserTest.kt` | Testes de parser |

## Arquivos alterados

- `AppDatabase.kt` → versão 4 + `invoiceDao`
- `CentralWebView.kt` → hook getFaturas; remove `IXC_INVOICE_AUDIT`
- `MainViewModel.kt` / `MainActivity.kt` → tela Faturas offline + prefs
- `InvoicesScreen.kt` → lista Room + toggles
- `AlfatechApp.kt` → agenda WorkManager
- `NotificationChannels.kt` → canal `invoice_reminders_channel`
- `libs.versions.toml` / `app/build.gradle.kts` → WorkManager

## Regras

- Chave: `idReceber` = `fatura.id` (UNIQUE)
- Upsert atualiza valores/status; ausência na sync **não** marca paga/cancelada
- PIX não vira billingType de pagamento; status só muda com IXC
- Código de barras opcional
- Lembretes: `invoice_[idReceber]_day_before_[dueDate]` / `_due_date_[dueDate]`

## Testes manuais sugeridos

1. Sync na Central → lista nativa
2. Segunda sync sem duplicar
3. Offline: “Dados salvos no aparelho”
4. STORE/BANK sem barcode
5. Preferências desligadas não notificam
6. Login manual/automático e comprovantes intactos
