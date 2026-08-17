# 50 — Hierarquia visual do card de fatura

## Composable

`InvoiceCard` em `InvoicesScreen.kt` (somente UI).

## Mudança

- Valor 22.sp Bold (principal)
- Status em chip suave (`InvoiceStatusChip`) — mesmo `getInvoiceDisplayStatus`
- Vencimento: label 12.sp + data 15.sp SemiBold
- Cobrança / Tipo: 12.sp onSurfaceVariant
- Padding 16; gaps 12 / 8
- Sem mudança de regras, dados ou hero da tela
