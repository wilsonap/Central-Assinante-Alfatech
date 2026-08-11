# 24 — WhatsApp dinâmico da Central + atalho nativo

## O que foi feito
1. Captura do link WhatsApp no DOM da Central (pós-auth) e ao navegar para `api.whatsapp.com` / `wa.me`
2. Estados em `MainViewModel`: `supportWhatsAppNumber`, `supportWhatsAppMessage`, `supportWhatsAppUrl`
3. Atalho **Chamar no WhatsApp** na Home (após Suporte), ícone `Icons.Default.Chat`
4. Abertura preferindo app WhatsApp / Business; fallback web; Toast se ainda sem número

## Arquivos
- `WhatsAppSupport.kt` (novo helper)
- `MainViewModel.kt`
- `CentralWebView.kt`
- `HomeScreen.kt`
- `MainActivity.kt`

## Log
Tag `WHATSAPP_CONFIG` — número mascarado (`********0199`)

## Não alterado
Login, cookies, sessão, FCM, bridge, Room, navegação Home/WebView, demais menus, design base.
