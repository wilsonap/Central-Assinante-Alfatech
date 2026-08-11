# 27 — Envio direto WhatsApp (ClipData + FileProvider)

## Correção anexo (somente ACTION_SEND)

- Intent final: `ACTION_SEND` + `EXTRA_STREAM` + `EXTRA_TEXT` + `ClipData.newRawUri` + `FLAG_GRANT_READ_URI_PERMISSION`
- Nunca `ACTION_VIEW` neste botão
- Antes do start: `openInputStream` obrigatório; `scheme=content`; authority = `${applicationId}.fileprovider`
- `grantUriPermission(whatsappPackage, uri, READ)` antes de abrir
- Logs `RECEIPT_SHARE`: action, mime, scheme, hasStream, canOpenUri, uriAuthority, package

## FileProvider
- Manifest: `android:authorities="${applicationId}.fileprovider"` + `grantUriPermissions=true`
- `file_paths.xml`: `<cache-path name="receipt_cache" path="receipts/" />`
