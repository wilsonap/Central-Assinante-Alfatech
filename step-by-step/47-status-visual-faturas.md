# 47 — Status visual das faturas

## Auditoria IXC (código existente)

| Código | Uso no app |
|--------|------------|
| `A` | Aberta |
| `P` | Paga (terminal) |
| `R` | Recebida/baixada (terminal) |
| `C` | Cancelada (terminal) |

Grupos: `abertas` / `pendentes` / `vencidas` (abertas p/ vencimento); `pagas`; `canceladas`.

## Implementação

- `InvoiceDisplayStatusMapper.getInvoiceDisplayStatus(invoice, today)`
- Card usa label + cor (texto sempre presente)
- Paga: ícone CheckCircle discreto
- Sem mudanças em AlarmManager / Room schema / sync

## Testes

`InvoiceDisplayStatusTest` — paga, 5 dias, amanhã, hoje, 1/3 dias atraso, data inválida
