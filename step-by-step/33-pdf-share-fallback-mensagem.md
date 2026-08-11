# 33 — PDF: EXTRA_TEXT + fallback “Abrir conversa com mensagem”

## Problema

- **Imagem:** arquivo + `EXTRA_TEXT` chegam no WhatsApp (fluxo ok, não alterar).
- **PDF:** arquivo chega; WhatsApp/Business costumam **ignorar** `Intent.EXTRA_TEXT` com `application/pdf`.

## O que foi feito

### `ReceiptShareHelper.kt`

- Fluxo de **imagem** intacto (`resolvedMime`, chooser, stream + text).
- Fluxo **PDF** dedicado (`sharePdfToWhatsApp`):
  - `ACTION_SEND`
  - `type = application/pdf`
  - `EXTRA_STREAM` + `EXTRA_TEXT`
  - `ClipData.newRawUri("comprovante_pdf", pdfUri)`
  - `FLAG_GRANT_READ_URI_PERMISSION`
  - tenta `com.whatsapp` → `com.whatsapp.w4b` → chooser
- Logs temporários tag **`RECEIPT_PDF_SHARE`**: mime, hasStream, hasText, hasClipData, package (sem PII/URI completa).
- `openAlfatechChat`: `wa.me/{digits}?text=...` com número **dinâmico** da Central.
- `buildMessage`: template solicitado; omite linhas vazias (Cliente/Código/Contrato).

### `ReceiptSenderScreen.kt`

- Após share de PDF bem-sucedido: `showPdfMessageFallback = true`.
- Botão **só no fluxo PDF:** “Abrir conversa com mensagem” → `openAlfatechChat`.
- Fluxo de imagem mantém “Abrir conversa da Alfatech” (comportamento anterior).

## Não alterado

WebView, login, cookies, Firebase/FCM, navegação, captura WhatsApp/cliente, FileProvider/URI, fluxo de imagem.

## Como validar

1. Logcat: filtrar `RECEIPT_PDF_SHARE`.
2. PDF → Enviar PDF → arquivo no WhatsApp; se texto sumir → “Abrir conversa com mensagem”.
3. Imagem → Enviar comprovante → arquivo + mensagem (igual ao antes).

## Arquivos

| Arquivo | Função |
|---------|--------|
| `ReceiptShareHelper.kt` | Share imagem vs PDF; wa.me; mensagem |
| `ReceiptSenderScreen.kt` | UI fallback PDF pós-share |
