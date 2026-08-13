# 28 — Startup offline com Home local

## Problema

Sem internet o app ficava preso na WebView (`isError` / login IXC) e não abria a Home, mesmo com faturas/comprovantes no Room.

## Correção mínima

- `OfflineStartup`: detecta rede, cookies e flag `had_authenticated_session`
- `MainViewModel.evaluateOfflineStartup()`:
  - offline + dados/sessão local → Home nativa (`isLoggedIn=true`)
  - offline sem dados → `NeedsFirstOnlineAuthScreen`
  - online → fluxo atual
- Funções da Central offline → Toast “Sem conexão”
- Ao voltar a internet → `reload` da WebView (cookies intactos)

## Logs

`OFFLINE_STARTUP`: networkAvailable, localSessionAvailable, localDataAvailable, navigateHomeOffline
