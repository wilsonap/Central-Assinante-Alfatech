# 22 — Correção: retorno WebView → Home nativa completa

## Causa
WebView sempre na composition sob a Home. Surface nativo do `AndroidView` pode cobrir os atalhos Compose. Seta azul usava histórico web em vez de `navigateToHome()`.

## Alterações
| Arquivo | Mudança |
|---------|---------|
| `CentralWebView.kt` | `isVisible` → `View.GONE`/`VISIBLE` sem destruir sessão |
| `MainActivity.kt` | Seta azul → Home; `isVisible = currentScreen == 1`; Back: raiz/sem histórico → Home |
| `MainViewModel.kt` | `isAtCentralRoot()` |

## Preservado
Login, cookies, sessão, FCM, bridge, Room, design Home/Central.
