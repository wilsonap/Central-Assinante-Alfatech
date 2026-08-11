# 25 — Enviar comprovante (nativo)

## Auditoria de dados
- Nome/código/contrato **não** existem no Kotlin nativo.
- Fonte: `localStorage`/`sessionStorage` (`dados`) + `app.cliente_nome` na Central.
- Extraídos via WebView (read-only), sem API/login novos.
- Campos ausentes são omitidos.

## Fluxo
1. Home → **Enviar comprovante**
2. Tirar foto / imagem / PDF
3. Prévia (imagem com rodapé em cópia cache; PDF original)
4. Enviar pelo WhatsApp usando `supportWhatsAppNumber`

## Arquivos
- `ReceiptSenderScreen.kt`, `ReceiptImageStamper.kt`, `ReceiptShareHelper.kt`
- `HomeScreen.kt`, `MainViewModel.kt`, `CentralWebView.kt`, `MainActivity.kt`
- `AndroidManifest.xml` (CAMERA + FileProvider + queries)
- `res/xml/file_paths.xml`

## Permissão
- `CAMERA` sob demanda; sem armazenamento amplo.

## Preservado
WhatsApp capture, login, cookies, FCM, navegação Home/WebView, demais menus.
