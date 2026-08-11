# 41 — Pós-login: refresh + Home nativa

## Auditoria

1. Home controlada por `MainViewModel.currentScreen` (0 = Home, 1 = Central).
2. Estado Home: `currentScreen == 0`.
3. Central: `openCentral` / URL inicial `baseUrl` com WebView `isVisible = currentScreen == 1`.
4. Auth: probe `sessao` em `FcmBridgeController` (já na #40).
5. Transição: `onSessionAuthenticated` → `login_authenticated` | `session_restored`.
6. Refresh: `scheduleCentralCustomerDataRefresh` / `refreshCentralCustomerData`.
7. Manual vs auto: trigger `login_authenticated` vs `session_restored`.
8. Ir à Home: `navigateToHome()`.
9. Anti-loop: `postLoginHomeHandled`; reset em URL de login.
10. Arquivos: `CentralWebView.kt`, `MainViewModel.kt`, `MainActivity.kt`.

## Fluxo

AUTH (transição única)
→ refreshCentralCustomerData (+ retries)
→ onPostLoginReady
→ navigateToHome (uma vez)

WebView permanece montada (GONE na Home). Sem alterar cookies/FCM/comprovantes.
