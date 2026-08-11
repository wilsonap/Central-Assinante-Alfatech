# 38 — Status do histórico: prepared / shared (sem “enviado”)

## Auditoria (antes)

| Onde | O quê |
|------|--------|
| `ReceiptSenderScreen` | Após `shareFileToWhatsApp` retornar true → `archiveAfterShareStarted` |
| `ReceiptHistoryStore.archiveAfterShareStarted` | Cópia local + Room com `status=shared` |
| `sentAt` | Sempre `null` (já não confirmava envio) |
| UI detalhe | Texto fixo “compartilhamento iniciado” (não lia `status`) |
| `status=sent` | **Não existia** no código |

Evento disparador: sucesso de `startActivity` do chooser/`ACTION_SEND` — **não** confirma Enviar no WhatsApp.

## Depois

1. `archivePrepared` → `status=prepared` (arquivo salvo).
2. Se `startActivity` ok → `markShared` → `status=shared`.
3. UI: prepared = “Salvo localmente”; shared = “Compartilhamento iniciado”.
4. Sem `sent`, sem ON_RESUME como confirmação.

Reenviar: se share iniciar, promove para `shared`.
