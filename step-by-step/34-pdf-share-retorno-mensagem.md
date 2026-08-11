# 34 — PDF: share do arquivo + diálogo ao retornar (mensagem via wa.me)

## Contexto

WhatsApp entrega o PDF via `ACTION_SEND`, mas **ignora `EXTRA_TEXT`** com `application/pdf`. FileProvider/URI/MIME já estão corretos e **não foram alterados**.

## Fluxo PDF

1. **Enviar pelo WhatsApp** → `ACTION_SEND` com **PDF + mensagem** (`EXTRA_STREAM` + `EXTRA_TEXT` + ClipData).
2. Destino **único**: `com.whatsapp`, senão `com.whatsapp.w4b`, senão chooser (não inicia os dois).
3. Usuário volta ao app (`ON_PAUSE` + `ON_RESUME`).
4. Diálogo/card **PDF compartilhado** com botão **Abrir conversa com mensagem** (fallback se o WhatsApp ignorar o texto; não abre sozinho).
5. Botão → `https://wa.me/{número dinâmico}?text={mensagem encoded}`.

`awaitingPdfShareReturn`: uma única declaração em `ReceiptSenderScreen`.

## UI

Texto explicativo no preview PDF:

> O WhatsApp pode enviar PDFs sem a mensagem de texto. Após compartilhar o arquivo, use “Abrir conversa com mensagem” para enviar os dados do comprovante.

## Imagem

Fluxo intacto (arquivo + mensagem no mesmo Intent).

## Arquivos

| Arquivo | Papel |
|---------|--------|
| `ReceiptSenderScreen.kt` | Lifecycle pós-share + AlertDialog + card |
| `ReceiptShareHelper.kt` | Share PDF (sem mudança de URI/MIME); `openAlfatechChat` / `buildMessage` |
| `step-by-step/34-pdf-share-retorno-mensagem.md` | Este registro |
