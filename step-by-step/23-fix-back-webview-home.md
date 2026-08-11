# 23 — Fix navegação: BACK engolido pela WebView + GONE/VISIBLE

## Problema
KEYCODE_BACK aparecia no Logcat sem `navigateToHome()` — Chromium WebView registra `OnBackInvokedCallback` e consome o back antes do `BackHandler` Compose. Home sem menus: surface WebView por cima.

## Correção
- `MainActivity`: `OnBackPressedCallback` na Activity + `reassertBackCallback()` (fica acima da WebView)
- Seta azul → sempre `navigateToHome()`
- `CentralWebView`: `LaunchedEffect(isVisible)` força `GONE`/`VISIBLE` na mesma instância
- Logs `CENTRAL_NAV`

## Preservado
Cookies, sessão, login, FCM, bridge, Room, design.
