# 28 — Fluxo final Enviar comprovante

## Separação oficial (Android)
- **Enviar comprovante:** só `ACTION_SEND` + `EXTRA_STREAM` + `EXTRA_TEXT` + `ClipData` + `createChooser` (sem `setPackage`).
  - Imagem: `type = "image/*"`
  - PDF: `type = "application/pdf"`
- **Abrir conversa da Alfatech:** só `wa.me` / `ACTION_VIEW` com `supportWhatsAppNumber` (sem anexo).
- Não há API oficial para `EXTRA_STREAM` + conversa específica; sem `jid`/hacks.
