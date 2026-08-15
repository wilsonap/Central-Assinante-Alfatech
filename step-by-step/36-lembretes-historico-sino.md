# 36 — Lembretes de fatura no histórico do sino

## Auditoria de schema

`notifications` **já suportava** sem migration:

- `type` → `INVOICE_REMINDER`
- `messageId` (unique) → chave `invoice_[id]_kind_[dueDate]`
- `timestamp` / `isRead` / `title` / `body` / `targetUrl`

## Mudanças

- `PushNotificationRepository.persistInvoiceReminder` após `notificationPosted=true`
- `NotificationsScreen`: origem Central vs Fatura + clique
- Clique em fatura → `navigateToInvoices()` (tela nativa)
- Badge de não lidas já inclui lembretes (`isRead`)
