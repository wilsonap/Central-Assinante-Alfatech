# 35 — PDF no mesmo fluxo ACTION_SEND da imagem

## Auditoria (antes)

| Item | Valor |
|------|--------|
| Função da imagem | `ReceiptShareHelper.shareFileToWhatsApp` → ramo imagem + `createChooser` |
| Função do PDF | `sharePdfToWhatsApp` (privada), chamada via `if (isPdf)` |
| Separação | `shareFileToWhatsApp` linha `if (isPdf) return sharePdfToWhatsApp(...)` |
| Lógica especial PDF | package único WhatsApp/W4B, logs `RECEIPT_PDF_SHARE`, UI lifecycle + diálogo + wa.me |
| Arquivos alterados | `ReceiptShareHelper.kt`, `ReceiptSenderScreen.kt` |

## Depois

Imagem e PDF passam pela **mesma** `shareFileToWhatsApp`:

- `ACTION_SEND` + `EXTRA_STREAM` + `EXTRA_TEXT` + `ClipData("comprovante")` + chooser
- MIME: resolvido (`image/*` ou `application/pdf`)
- Logs `RECEIPT_SHARE_FLOW` com `kind=image|pdf`

Removidos: `sharePdfToWhatsApp`, fallback pós-retorno, diálogo, “Abrir conversa com mensagem”.

Histórico local, FileProvider, WebView/FCM/navegação intactos.
