# 39 — Chamar no WhatsApp com identificação do assinante

## Auditoria

| Fonte | Enviar comprovante | Chamar no WhatsApp (antes) |
|-------|--------------------|----------------------------|
| Nome/código/contrato | `MainViewModel.clientFullName/Code/Contract` (WebView → `updateClientProfile`) | não usava |
| Mensagem | `ReceiptShareHelper.buildMessage` | `supportWhatsAppMessage` / `DEFAULT_MESSAGE` (“Olá! Preciso de atendimento.”) |
| Número | `supportWhatsAppNumber` dinâmico | idem |

## Depois

- `WhatsAppSupport.buildSupportMessage(...)` com os mesmos StateFlows.
- `MainActivity` atalho WhatsApp monta essa mensagem + `openChat` (número dinâmico, URL encode).
- Sem dados: fallback sem linhas Cliente/Código/Contrato.

Não alterou captura, comprovante, Room, WebView, FCM.
