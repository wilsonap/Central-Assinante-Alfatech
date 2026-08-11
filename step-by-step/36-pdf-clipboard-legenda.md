# 36 — PDF: mensagem na área de transferência (legenda WhatsApp)

## Problema

WhatsApp recebe o PDF, mas ignora `EXTRA_TEXT` e abre o campo vazio **Adicione uma legenda...**.

## Solução (somente PDF)

Na mesma `shareFileToWhatsApp`, se `kind=pdf`:

1. Monta o Intent igual à imagem (`ACTION_SEND` + stream + text + ClipData URI).
2. Copia a **mesma mensagem** com `ClipboardManager` + `ClipData.newPlainText`.
3. Toast: `Mensagem copiada. No WhatsApp, toque em "Adicione uma legenda..." e cole.`
4. Abre o chooser.

Imagem: sem clipboard/toast extras.

## Arquivo

`ReceiptShareHelper.kt` — ramo `kind == "pdf"` antes do `startActivity`.
